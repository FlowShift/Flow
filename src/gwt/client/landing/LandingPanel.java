//
// $Id$

package client.landing;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;

import com.threerings.gwt.ui.WidgetUtil;

import com.threerings.msoy.landing.gwt.LandingData;
import com.threerings.msoy.landing.gwt.LandingService;
import com.threerings.msoy.landing.gwt.LandingServiceAsync;
import com.threerings.msoy.web.gwt.Pages;

import client.shell.LogonPanel;
import client.ui.MsoyUI;
import client.ui.RoundBox;
import client.util.Link;
import client.util.InfoCallback;
import client.util.ServiceUtil;

/**
 * Displays a summary of what Whirled is, featuring games, avatars and whirleds.
 * Spans the entire width of the page, with an active content area 800 pixels wide and centered.
 */
public class LandingPanel extends SimplePanel
{
    public LandingPanel (boolean compact)
    {
        // LandingPanel contains LandingBackground contains LandingContent
        setStyleName("LandingPanel");
        SimplePanel headerBackground = new SimplePanel();
        headerBackground.setStyleName(compact ? "LandingBackgroundCompact" : "LandingBackground");
        AbsolutePanel content = new AbsolutePanel();
        content.setStyleName(compact ? "LandingContentCompact" : "LandingContent");
        headerBackground.setWidget(content);
        this.setWidget(headerBackground);

        if (!compact) {
            // splash with animated characters (left goes over right)
            final HTML titleAnimation = WidgetUtil.createTransparentFlashContainer(
                "preview", "/images/landing/splash_left.swf", 500, 300, null);
            content.add(titleAnimation, -23, 10);
        }

        // join now
        ClickHandler onJoin = Link.createListener(Pages.ACCOUNT, "create");
        content.add(MsoyUI.createImageButton("JoinButton", onJoin), 475, 0);

        // logon box
        final FlowPanel logon = new FlowPanel();
        PushButton logonButton = new PushButton(_msgs.landingLogon());
        logonButton.addStyleName("LogonButton");
        logon.add(new LogonPanel(LogonPanel.Mode.LANDING, logonButton));
        logon.add(logonButton);
        content.add(logon, 590, 0);

        if (!compact) {
            // intro video with click-to-play button
            final AbsolutePanel video = new AbsolutePanel();
            video.setStyleName("Video");
            ClickHandler onClick = new ClickHandler() {
                public void onClick (ClickEvent event) {
                    video.remove(0);
                    // controls skin hardcoded in the swf as /images/landing/landing_movie_skin.swf
                    video.add(WidgetUtil.createFlashContainer(
                        "preview", "/images/landing/landing_movie.swf", 208, 154, null), 34, 1);
                }
            };
            final Image clickToPlayImage = MsoyUI.createActionImage(
                "/images/landing/play_screen.png", _msgs.landingClickToStart(), onClick);
            video.add(clickToPlayImage, 0, 0);
            content.add(video, 465, 90);
    
            // tagline
            final HTML tagline = MsoyUI.createHTML(_msgs.landingTagline(), null);
            tagline.setStyleName("LandingTagline");
            content.add(tagline, 425, 275);
        }

        int yoffset = compact ? -201 : 0;

        // background for the rest of the page
        FlowPanel background = new FlowPanel();
        background.setStyleName("Background");
        FlowPanel leftBorder = new FlowPanel();
        leftBorder.setStyleName("LeftBorder");
        background.add(leftBorder);
        FlowPanel center = new FlowPanel();
        center.setStyleName("Center");
        background.add(center);
        FlowPanel rightBorder = new FlowPanel();
        rightBorder.setStyleName("RightBorder");
        background.add(rightBorder);
        content.add(background, 0, 310 + yoffset);

        // top games
        RoundBox games = new RoundBox(RoundBox.DARK_BLUE);
        final TopGamesPanel topGamesPanel = new TopGamesPanel();
        games.add(topGamesPanel);
        content.add(games, 68, 312 + yoffset);

        // featured avatar
        content.add(_avatarPanel = new AvatarPanel(), 67, 618 + yoffset);

        // featured group panel is beaten into place using css
        _featuredGroup = new FeaturedGroupPanel(true);
        content.add(_featuredGroup, 290, 618 + yoffset);

        // copyright, about, terms & conditions, help
        content.add(new LandingCopyright(), 48, 1012 + yoffset);

        // collect the data for this page
        _landingsvc.getLandingData(new InfoCallback<LandingData>() {
            public void onSuccess (LandingData data) {
                topGamesPanel.setGames(data.topGames);
                _featuredGroup.setWhirleds(data.featuredWhirleds);
                _avatarPanel.setAvatars(data.topAvatars);
            }
        });
        
    }

    protected FeaturedGroupPanel _featuredGroup;
    protected AvatarPanel _avatarPanel;

    protected static final LandingMessages _msgs = GWT.create(LandingMessages.class);
    protected static final LandingServiceAsync _landingsvc = (LandingServiceAsync)
        ServiceUtil.bind(GWT.create(LandingService.class), LandingService.ENTRY_POINT);
}
