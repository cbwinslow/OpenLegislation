package gov.nysenate.openleg.search.transcripts.session;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import gov.nysenate.openleg.config.Environment;
import gov.nysenate.openleg.common.dao.LimitOffset;
import gov.nysenate.openleg.search.SearchIndex;
import gov.nysenate.openleg.common.dao.SortOrder;
import gov.nysenate.openleg.legislation.transcripts.session.Transcript;
import gov.nysenate.openleg.legislation.transcripts.session.TranscriptId;
import gov.nysenate.openleg.search.*;
import gov.nysenate.openleg.legislation.transcripts.session.dao.TranscriptDataService;
import gov.nysenate.openleg.updates.transcripts.session.BulkTranscriptUpdateEvent;
import gov.nysenate.openleg.updates.transcripts.session.TranscriptUpdateEvent;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElasticTranscriptSearchService implements TranscriptSearchService, IndexedSearchService<Transcript> {

    private static final Logger logger = LoggerFactory.getLogger(ElasticTranscriptSearchService.class);

    @Autowired protected Environment env;
    @Autowired protected EventBus eventBus;
    @Autowired protected ElasticTranscriptSearchDao transcriptSearchDao;
    @Autowired protected TranscriptDataService transcriptDataService;

    @PostConstruct
    protected void init() {
        eventBus.register(this);
    }

    /** {@inheritDoc} */
    @Override
    public SearchResults<TranscriptId> searchTranscripts(String sort, LimitOffset limOff) throws SearchException {
        return search(QueryBuilders.matchAllQuery(), null, sort, limOff);
    }

    /** {@inheritDoc} */
    @Override
    public SearchResults<TranscriptId> searchTranscripts(int year, String sort, LimitOffset limOff) throws SearchException {
        return search(QueryBuilders.matchAllQuery(), year, sort, limOff);
    }

    /** {@inheritDoc} */
    @Override
    public SearchResults<TranscriptId> searchTranscripts(String query, String sort, LimitOffset limOff) throws SearchException {
        return search(QueryBuilders.queryStringQuery(query), null, sort, limOff);
    }

    /** {@inheritDoc} */
    @Override
    public SearchResults<TranscriptId> searchTranscripts(String query, int year, String sort, LimitOffset limOff) throws SearchException {
        return search(QueryBuilders.queryStringQuery(query), year, sort, limOff);
    }

    private SearchResults<TranscriptId> search(QueryBuilder query, Integer year,
                                               String sort, LimitOffset limOff) throws SearchException {
        if (limOff == null) {
            limOff = LimitOffset.TEN;
        }
        RangeQueryBuilder rangeFilter = null;
        if (year != null) {
            rangeFilter = new RangeQueryBuilder("dateTime")
                    .from(LocalDate.of(year, 1, 1).toString())
                    .to(LocalDate.of(year, 12, 31).toString());
        }
        try {
            return transcriptSearchDao.searchTranscripts(query, rangeFilter,
                    ElasticSearchServiceUtils.extractSortBuilders(sort), limOff);
        }
        catch (SearchParseException ex) {
            throw new SearchException("Invalid query string", ex);
        }
        catch (ElasticsearchException ex) {
            throw new UnexpectedSearchException(ex.getMessage(), ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    @Subscribe
    public void handleTranscriptUpdate(TranscriptUpdateEvent transcriptUpdateEvent) {
        if (transcriptUpdateEvent.getTranscript() != null) {
            updateIndex(transcriptUpdateEvent.getTranscript());
        }
    }

    /** {@inheritDoc} */
    @Override
    @Subscribe
    public void handleBulkTranscriptUpdate(BulkTranscriptUpdateEvent bulkTranscriptUpdateEvent) {
        if (bulkTranscriptUpdateEvent.getTranscripts() != null) {
            updateIndex(bulkTranscriptUpdateEvent.getTranscripts());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateIndex(Transcript transcript) {
        if (env.isElasticIndexing() && transcript != null) {
            logger.info("Indexing transcript {} into elastic search.", transcript.getDateTime().toString());
            transcriptSearchDao.updateTranscriptIndex(transcript);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void updateIndex(Collection<Transcript> transcripts) {
        if (env.isElasticIndexing() && !transcripts.isEmpty()) {
            List<Transcript> indexableTranscripts = transcripts.stream().filter(Objects::nonNull).collect(Collectors.toList());
            logger.info("Indexing {} valid transcripts into elasticsearch.", indexableTranscripts.size());
            transcriptSearchDao.updateTranscriptIndex(indexableTranscripts);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clearIndex() {
        transcriptSearchDao.purgeIndices();
        transcriptSearchDao.createIndices();
    }

    /** {@inheritDoc} */
    @Override
    public void rebuildIndex() {
        clearIndex();
        final int bulkSize = 500;
        Queue<TranscriptId> transcriptIdQueue =
                new ArrayDeque<>(transcriptDataService.getTranscriptIds(SortOrder.DESC, LimitOffset.ALL));
        while(!transcriptIdQueue.isEmpty()) {
            List<Transcript> transcripts = new ArrayList<>(bulkSize);
            for (int i = 0; i < bulkSize && !transcriptIdQueue.isEmpty(); i++) {
                TranscriptId tid = transcriptIdQueue.remove();
                transcripts.add(transcriptDataService.getTranscript(tid));
            }
            updateIndex(transcripts);
        }
        logger.info("Finished reindexing transcripts.");
    }

    /** {@inheritDoc} */
    @Override
    @Subscribe
    public void handleRebuildEvent(RebuildIndexEvent event) {
        if (event.affects(SearchIndex.TRANSCRIPT)) {
            logger.info("Handling transcript re-index event.");
            rebuildIndex();
        }
    }

    /** {@inheritDoc} */
    @Override
    @Subscribe
    public void handleClearEvent(ClearIndexEvent event) {
        if (event.affects(SearchIndex.TRANSCRIPT)) {
            clearIndex();
        }
    }
}
