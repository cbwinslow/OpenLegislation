package gov.nysenate.openleg.api.ui;

import com.google.common.eventbus.EventBus;
import gov.nysenate.openleg.common.dao.LimitOffset;
import gov.nysenate.openleg.common.dao.SortOrder;
import gov.nysenate.openleg.legislation.committee.Chamber;
import gov.nysenate.openleg.legislation.member.Member;
import gov.nysenate.openleg.legislation.member.dao.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Common base class for all controllers that are responsible for rending content pages.
 */
public abstract class BaseContentPageCtrl
{
    private static final Logger logger = LoggerFactory.getLogger(BaseContentPageCtrl.class);

    @Autowired private MemberService memberData;
    @Autowired private EventBus eventBus;

    protected static List<Member> senatorsList = null;
    protected static List<Member> assemblyMemList = null;

    protected void baseInit() {
        initializeMembers();
        eventBus.register(this);
    }

    /**
     * Adds common content request attributes. This is needed for things like senator drop downs
     * which would otherwise require additional API calls.
     *
     * @param request HttpServletRequest
     */
    protected void addContentAttributesToRequest(HttpServletRequest request) {
        request.setAttribute("senatorList", senatorsList);
        request.setAttribute("assemblyMemList", assemblyMemList);
    }

    /**
     * Initializes the members list for use in drop downs in non-member centric pages. This data could be retrieved
     * by an API call but it seems overkill to have to make a separate API call just for this listing which is fairly
     * static..
     */
    private void initializeMembers() {
        List<Member> allMembers = memberData.getAllSessionMembers(SortOrder.ASC, LimitOffset.ALL).stream()
            .map(sm -> new Member(sm.getMember()))
            .distinct()
            .collect(Collectors.toList());
        senatorsList = allMembers.stream()
            .filter(m -> m.getChamber().equals(Chamber.SENATE))
            .collect(Collectors.toList());
        assemblyMemList = allMembers.stream()
            .filter(m -> m.getChamber().equals(Chamber.ASSEMBLY))
            .collect(Collectors.toList());
    }
}
