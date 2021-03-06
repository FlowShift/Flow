//
// $Id$

package client.adminz;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.SmartTable;

import com.threerings.msoy.admin.gwt.AdminService;
import com.threerings.msoy.admin.gwt.AdminServiceAsync;
import com.threerings.msoy.data.all.DeploymentConfig;
import com.threerings.msoy.web.gwt.Pages;
import com.threerings.msoy.web.gwt.WebUserService;
import com.threerings.msoy.web.gwt.WebUserServiceAsync;

import client.shell.CShell;
import client.ui.BorderedPopup;
import client.ui.MsoyUI;
import client.util.ClickCallback;
import client.util.Link;

/**
 * Displays the various services available to support and admin personnel.
 */
public class DashboardPanel extends SmartTable
{
    public DashboardPanel ()
    {
        super("dashboardPanel", 0, 10);
        int row = 0, col = 0;

        // display some infoez
        setHTML(row, 0, _msgs.dashVersion(DeploymentConfig.version));
        setHTML(row++, 1, _msgs.dashBuilt(DeploymentConfig.buildTime));

        // admin-only controls
        if (CShell.isAdmin()) {
            FlowPanel admin = new FlowPanel();
            admin.add(MsoyUI.createLabel(_msgs.adminControls(), "Title"));
            admin.add(makeLink("Configuration", "config"));
            admin.add(makeLink(_msgs.viewExchange(), "exchange"));
            admin.add(makeLink(_msgs.cashOutButton(), "cashout"));
            admin.add(makeLink(_msgs.statsButton(), "stats"));
            admin.add(makeLink(_msgs.viewABTests(), "testlist"));
            admin.add(makeLink(_msgs.viewBureaus(), "bureaus"));
            admin.add(makeLink(_msgs.panopticonStatus(), "panopticonStatus"));
            admin.add(makeLink(_msgs.viewSurveys(), "survey", "e"));
            admin.add(Link.create(_msgs.appsButton(), Pages.APPS));
            setWidget(row, col, admin);
            getFlexCellFormatter().setVerticalAlignment(row, col++, HasAlignment.ALIGN_TOP);

            FlowPanel reboot = new FlowPanel();
            reboot.add(MsoyUI.createLabel(_msgs.adminReboot(), "Title"));
            reboot.add(MsoyUI.createLabel(_msgs.adminRebootMessage(), null));
            TextArea message = MsoyUI.createTextArea("", 30, 4);
            reboot.add(message);
            reboot.add(makeReboot(_msgs.rebootInTwo(), 2, message));
            reboot.add(makeReboot(_msgs.rebootInFive(), 5, message));
            reboot.add(makeReboot(_msgs.rebootInFifteen(), 15, message));
            reboot.add(makeReboot(_msgs.rebootInThirty(), 30, message));
            // TODO: support reboot cancellation
            //reboot.add(makeReboot(_msgs.rebootCancel(), -1));
            setWidget(row, col, reboot);
            getFlexCellFormatter().setVerticalAlignment(row, col++, HasAlignment.ALIGN_TOP);
        }

        // support controls
        FlowPanel support = new FlowPanel();
        support.add(MsoyUI.createLabel(_msgs.supportControls(), "Title"));
        support.add(makeLink(_msgs.reviewButton(), "review"));
        support.add(makeLink(_msgs.promosButton(), "promos"));
        support.add(makeLink(_msgs.contestsButton(), "contests"));
        support.add(makeLink(_msgs.broadcastButton(), "broadcasts"));
        support.add(MsoyUI.createActionLabel(_msgs.blacklistButton(), new ClickHandler() {
            public void onClick (ClickEvent event) {
                final BorderedPopup popup = new BorderedPopup();
                popup.setWidget(new BlacklistWidget(new ClickHandler() {
                    @Override public void onClick (ClickEvent event) {
                        popup.hide();
                    }
                }));
                popup.show();
            }
        }));

        setWidget(row, col, support);
        getFlexCellFormatter().setVerticalAlignment(row++, col++, HasAlignment.ALIGN_TOP);
    }

    protected Widget makeLink (String title, Object... args)
    {
        Widget link = Link.create(title, Pages.ADMINZ, args);
        link.removeStyleName("inline");
        return link;
    }

    protected Widget makeReboot (String title, final int minutes, final TextArea messageWidget)
    {
        Button reboot = new Button(title);
        new ClickCallback<Void>(reboot) {
            protected boolean callService () {
                _adminsvc.scheduleReboot(minutes, messageWidget.getText(), this);
                return true;
            }
            protected boolean gotResult (Void result) {
                MsoyUI.info(minutes < 0 ? _msgs.rebootCancelled() :
                            _msgs.rebootScheduled(""+minutes));
                return true;
            }
        };
        return reboot;
    }

    protected static final AdminMessages _msgs = GWT.create(AdminMessages.class);
    protected static final AdminServiceAsync _adminsvc = GWT.create(AdminService.class);
    protected static final WebUserServiceAsync _usersvc = GWT.create(WebUserService.class);
}
