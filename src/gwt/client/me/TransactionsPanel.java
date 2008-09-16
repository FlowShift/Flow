//
// $Id$

package client.me;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.msoy.money.data.all.ReportType;

import client.shell.Args;
import client.shell.Pages;
import client.ui.MsoyUI;
import client.util.Link;

public class TransactionsPanel extends VerticalPanel
{
    public TransactionsPanel (int report, final int memberId)
    {
        setStyleName("transactions");

        final ListBox reportBox = new ListBox();
        for (String name : REPORT_NAMES) {
            reportBox.addItem(name);
        }
        reportBox.addChangeListener(new ChangeListener() {
            public void onChange (Widget widget) {
                Link.go(Pages.ME, Args.compose("transactions",
                        String.valueOf(REPORT_VALUES[reportBox.getSelectedIndex()].ordinal()+1),
                        String.valueOf(memberId)));
            }
        });
        reportBox.setSelectedIndex(report-1);

        add(new BalancePanel(memberId, ReportType.values()[report-1]) {
            @Override protected void addCustomControls (FlexTable controls) {
                controls.setText(0, 0, _msgs.reportFilter());
                controls.getFlexCellFormatter().setStyleName(0, 0, "ReportFilter");
                controls.setWidget(0, 1, reportBox);
            }
        });
    }

    protected static final MeMessages _msgs = GWT.create(MeMessages.class);

    protected static final String[] REPORT_NAMES = {
        _msgs.reportCoins(), _msgs.reportBars(), _msgs.reportBling(), _msgs.reportCreator()
    };

    protected static final ReportType[] REPORT_VALUES = {
        ReportType.COINS, ReportType.BARS, ReportType.BLING, ReportType.CREATOR
    };
}
