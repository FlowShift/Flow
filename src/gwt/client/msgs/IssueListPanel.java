//
// $Id$

package client.msgs;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import org.gwtwidgets.client.util.SimpleDateFormat;

import com.threerings.gwt.ui.PagedGrid;

import com.threerings.msoy.fora.data.Issue;

import client.shell.Application;
import client.shell.Page;
import client.util.MsoyUI;
import client.util.RowPanel;

import java.util.List;

/**
 * Displays a list of issues.
 */
public class IssueListPanel extends PagedGrid
{
    public IssueListPanel (IssuePanel parent)
    {
        super(ISSUES_PER_PAGE, 1, NAV_ON_BOTTOM);
        addStyleName("dottedGrid");
        setWidth("100%");
        _parent = parent;
    }

    public void displayIssues (int type, int state, IssueModels imodels, boolean refresh)
    {
        setModel(imodels.getIssues(type, state, refresh), 0);
    }

    // @Override // from PagedGrid
    protected Widget createWidget (Object item)
    {
        return new IssueSummaryPanel((Issue)item);
    }

    // @Override // from PagedGrid
    protected Widget createEmptyContents ()
    {
        HTML empty = new HTML(CMsgs.mmsgs.noIssues());
        empty.setStyleName("Empty");
        return empty;
    }

    // @Override // from PagedGrid
    protected String getEmptyMessage ()
    {
        return CMsgs.mmsgs.noIssues();
    }

    // @Override // from PagedGrid
    protected boolean displayNavi (int items)
    {
        return true; // we always show our navigation for consistency
    }

    // @Override // from PagedGrid
    protected void addCustomControls (FlexTable controls)
    {
        super.addCustomControls(controls);

        // add a button for refreshing the issue list
        _refresh = new Button(CMsgs.mmsgs.tlpRefresh(), new ClickListener() {
            public void onClick (Widget sender) {
                _parent.displayIssues(true);
            }
        });
        controls.setWidget(0, 0, _refresh);
    }

    // @Override // from PagedGrid
    protected void displayResults (int start, int count, List list)
    {
        super.displayResults(start, count, list);
        _refresh.setVisible(true);
        _refresh.setEnabled(true);
    }

    protected class IssueSummaryPanel extends FlexTable
    {
        public IssueSummaryPanel (final Issue issue)
        {
            setStyleName("issueSummaryPanel");
            setCellPadding(0);
            setCellSpacing(0);

            int col = 0;
            Hyperlink toIssue = Application.createLink(
                    issue.description, Page.WHIRLEDS, "i_" + issue.issueId);
            setWidget(0, col, toIssue);
            getFlexCellFormatter().setStyleName(0, col++, "Description");

            setText(0, col, IssueMsgs.priorityMsg(issue, CMsgs.mmsgs));
            getFlexCellFormatter().setStyleName(0, col++, "State");

            setText(0, col, IssueMsgs.categoryMsg(issue, CMsgs.mmsgs));
            getFlexCellFormatter().setStyleName(0, col++, "State");

            setText(0, col, (issue.owner == null ? CMsgs.mmsgs.INone() : issue.owner.toString()));
            getFlexCellFormatter().setStyleName(0, col++, "State");

            VerticalPanel created = new VerticalPanel();
            Hyperlink creator;
            if (issue.state == Issue.STATE_OPEN) {
                created.add(new Label(_pdate.format(issue.createdTime)));
                creator = Application.createLink(
                        issue.creator.toString(), Page.PEOPLE, "" + issue.creator.getMemberId());
            } else {
                created.add(new Label(_pdate.format(issue.closedTime)));
                creator = Application.createLink(
                        issue.owner.toString(), Page.PEOPLE, "" + issue.owner.getMemberId());
            }
            created.add(creator);
            setWidget(0, col, created);
            getFlexCellFormatter().setStyleName(0, col++, "Created");
        }
    }

    /** The forum panel in which we're hosted. */
    protected IssuePanel _parent;

    protected Button _refresh;

    /** Used to format the created date. */
    protected static SimpleDateFormat _pdate = new SimpleDateFormat("MMM dd, yyyy h:mm aa");

    /** The number of issues displayed per page. */
    protected static final int ISSUES_PER_PAGE = 20;
}
