//
// $Id$

package client.issues;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.msoy.fora.gwt.ForumMessage;
import com.threerings.msoy.web.gwt.Pages;

import client.msgs.SimpleMessagePanel;
import client.util.Link;

/**
 * A message panel that display a forum message with a link back to the thread.
 */
public class IssueMessagePanel extends SimpleMessagePanel
{
    public IssueMessagePanel ()
    {
    }

    public IssueMessagePanel (ForumMessage message)
    {
        setMessage(message);
    }

    @Override // from SimpleMessagePanel
    public void setMessage (ForumMessage message)
    {
        _threadId = message.threadId;
        super.setMessage(message);
    }

    @Override // from MessagePanel
    public void addInfo (FlowPanel info)
    {
        super.addInfo(info);

        Widget link = Link.create(_msgs.iThread(), Pages.GROUPS, "t", _threadId);
        link.setStyleName("issueMessageLink");
        link.addStyleName("actionLabel");
        info.add(link);
    }

    protected int _threadId;

    protected static final IssuesMessages _msgs = (IssuesMessages)GWT.create(IssuesMessages.class);
}
