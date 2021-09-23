package gov.nysenate.openleg.api.admin;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import gov.nysenate.openleg.api.response.BaseResponse;
import gov.nysenate.openleg.api.response.ListViewResponse;
import gov.nysenate.openleg.api.response.SimpleResponse;
import gov.nysenate.openleg.api.response.error.ErrorCode;
import gov.nysenate.openleg.api.response.error.ErrorResponse;
import gov.nysenate.openleg.api.search.view.SearchIndexInfoView;
import gov.nysenate.openleg.api.BaseCtrl;
import gov.nysenate.openleg.common.dao.LimitOffset;
import gov.nysenate.openleg.search.SearchIndex;
import gov.nysenate.openleg.search.ClearIndexEvent;
import gov.nysenate.openleg.search.RebuildIndexEvent;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static gov.nysenate.openleg.api.BaseCtrl.BASE_ADMIN_API_PATH;

@RestController
@RequestMapping(value = BASE_ADMIN_API_PATH + "/index")
public class SearchIndexCtrl extends BaseCtrl
{
    private static final Logger logger = LoggerFactory.getLogger(SearchIndexCtrl.class);

    @Autowired private EventBus eventBus;

    @PostConstruct
    private void init() {
        eventBus.register(this);
    }

    /**
     * Search Index Rebuild API
     *
     * Rebuild the specified search indices: (PUT) /api/3/admin/index/{indexType}
     * 'indexType' can be set to 'all' to reindex everything, or to one of the values in the
     * {@link SearchIndex} enumeration.
     *
     * Re-indexing in this context means dropping all the existing data in an index and re-inserting
     * using data pulled from the backing store. Probably don't want to do this while a data processing
     * job is running.
     */
    @RequiresPermissions("admin:searchIndexEdit")
    @RequestMapping(value = "/{indexType}", method = RequestMethod.PUT)
    public BaseResponse rebuildIndex(@PathVariable String indexType) {
        BaseResponse response;
        try {
            Set<SearchIndex> targetIndices = getTargetIndices(indexType);
            eventBus.post(new RebuildIndexEvent(targetIndices));
            response = new SimpleResponse(true, "Search index rebuild request completed", "index-rebuild");
        }
        catch (IllegalArgumentException ex) {
            response = new ErrorResponse(ErrorCode.INVALID_ARGUMENTS);
            response.setMessage("Invalid search index: " + indexType);
        }
        return response;
    }

    @RequiresPermissions("admin:searchIndexEdit")
    @RequestMapping(value = "/{indexType}", method = RequestMethod.DELETE)
    public BaseResponse clearIndex(@PathVariable String indexType) {
        BaseResponse response;
        try {
            Set<SearchIndex> targetIndices = getTargetIndices(indexType);
            eventBus.post(new ClearIndexEvent(targetIndices));
            response = new SimpleResponse(true, "Search index clear request completed", "index-clear");
        }
        catch (IllegalArgumentException ex) {
            response = new ErrorResponse(ErrorCode.INVALID_ARGUMENTS);
            response.setMessage("Invalid search index: " + indexType);
        }
        return response;
    }

    // returns the index names. Can later be expanded to return index information as well.
    @RequiresPermissions("admin:searchIndexEdit")
    @RequestMapping(value = "", method = RequestMethod.GET)
    public ListViewResponse<SearchIndexInfoView> getIndices() {
        List<SearchIndexInfoView> names = Arrays.stream(SearchIndex.values())
                .map(SearchIndexInfoView::new)
                .collect(Collectors.toList());
        return ListViewResponse.of(names, names.size(), LimitOffset.ALL);
    }

    /** --- Internal --- */

    private Set<SearchIndex> getTargetIndices(String indexType) throws IllegalArgumentException {
        Set<SearchIndex> targetIndices;
        if (indexType.equalsIgnoreCase("all")) {
            // Exclude principal indices from "all" grouping.
            targetIndices = Arrays.stream(SearchIndex.values())
                    .filter(i -> !i.isPrimaryStore())
                    .collect(Collectors.toSet());
        }
        else {
            targetIndices = Sets.newHashSet(SearchIndex.valueOf(indexType.toUpperCase()));
        }
        return targetIndices;
    }
}
