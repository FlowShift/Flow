//
// $Id$

package client.rooms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.PagedGrid;
import com.threerings.gwt.ui.SmartTable;
import com.threerings.gwt.util.SimpleDataModel;

import com.threerings.msoy.room.gwt.RoomInfo;
import com.threerings.msoy.room.gwt.WebRoomService;
import com.threerings.msoy.room.gwt.WebRoomServiceAsync;
import com.threerings.msoy.web.gwt.Pages;

import client.room.RoomWidget;
import client.ui.Marquee;
import client.ui.MsoyUI;
import client.ui.TongueBox;
import client.util.FlashClients;
import client.util.InfoCallback;
import client.util.Link;

public class RoomsPanel extends FlowPanel
{
    public RoomsPanel ()
    {
        setStyleName("roomsPanel");

        SmartTable header = new SmartTable("Info", 0, 0);
        header.setWidget(0, 0, new Marquee(null, _msgs.roomsMarquee()), 1);
        header.getFlexCellFormatter().setVerticalAlignment(0, 0, HasAlignment.ALIGN_TOP);
        header.setText(1, 0, _msgs.roomsIntro(), 1, "Intro");

        Widget button = MsoyUI.createImageButton("TourButton", new ClickHandler () {
            public void onClick (ClickEvent event) {
                if (FlashClients.clientExists()) {
                    FlashClients.startTour();
                } else {
                    Link.go(Pages.WORLD, "tour");
                }
            }
        });
        header.setWidget(0, 1, button, 1, "Tour");
        header.getFlexCellFormatter().setRowSpan(0, 1, 2);
        header.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasAlignment.ALIGN_CENTER);

        add(header);

        _worldsvc.loadOverview(new InfoCallback<WebRoomService.OverviewResult>() {
            public void onSuccess (WebRoomService.OverviewResult overview) {
                init(overview);
            }
        });
    }

    protected void init (WebRoomService.OverviewResult overview)
    {
        RoomsGrid active = new RoomsGrid();
        active.setModel(new SimpleDataModel<RoomInfo>(overview.activeRooms), 0);
        add(new TongueBox(_msgs.activeRooms(), active));

        RoomsGrid cool = new RoomsGrid();
        cool.setModel(new SimpleDataModel<RoomInfo>(overview.coolRooms), 0);
        add(new TongueBox(_msgs.coolRooms(), cool));

        // give a title to each contest winning room based on its location in the list
        for (int ii = 0; ii < overview.winningRooms.size(); ii++) {
            RoomInfo room = overview.winningRooms.get(ii);
            if (ii == 0) {
                room.winnerRank = _msgs.winnerFirst();
            } else if (ii == 1) {
                room.winnerRank = _msgs.winnerSecond();
            } else if (ii == 2) {
                room.winnerRank = _msgs.winnerThird();
            } else if (ii < 8) {
                room.winnerRank = _msgs.winnerHonorable();
            } else {
                room.winnerRank = _msgs.winnerOther();
            }
        }
        RoomsGrid winners = new RoomsGrid();
        winners.setModel(new SimpleDataModel<RoomInfo>(overview.winningRooms), 0);
        add(new TongueBox(_msgs.winningRooms(), winners));
    }

    protected static class RoomsGrid extends PagedGrid<RoomInfo>
    {
        public RoomsGrid () {
            super(2, 3, NAV_ON_BOTTOM);
            _cellVertAlign = HasAlignment.ALIGN_TOP;
        }

        @Override // from PagedGrid
        protected Widget createWidget (RoomInfo room) {
            return new RoomWidget(room);
        }

        @Override // from PagedGrid
        protected String getEmptyMessage () {
            return _msgs.emptyGrid(); // This should almost never happen
        }
    }

    protected static final RoomsMessages _msgs = GWT.create(RoomsMessages.class);
    protected static final WebRoomServiceAsync _worldsvc = GWT.create(WebRoomService.class);
}
