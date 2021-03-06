//
// $Id$

package client.util;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.WidgetUtil.FlashObject;
import com.threerings.gwt.ui.WidgetUtil;

import com.threerings.msoy.data.all.DeploymentConfig;
import com.threerings.msoy.data.all.UberClientModes;

import client.shell.CShell;

/**
 * Utility methods for generating flash clients.
 */
public class FlashClients
{
    public static final int MIN_WORLD_WIDTH = 300;
    public static final int MIN_WORLD_HEIGHT = 300;

    /** The "id" attribute of the <embed> tag containing the world client. */
    public static final String ELEM_ID = "asclient";

    /**
     * Create a tiny applet for uploading media.
     */
    public static HTML createUploader (String mediaIds, String filetypes)
    {
        FlashObject obj = new FlashObject("uploader",
            clientPath("uploader.swf"), 200, 40,
            "auth=" + URL.encodeComponent(CShell.getAuthToken()) +
            "&mediaIds=" + URL.encodeComponent(mediaIds) +
            "&filetypes=" + URL.encodeComponent(filetypes));
        obj.transparent = true;
        return WidgetUtil.createContainer(obj);
    }

    /**
     * Create a tiny applet that simply detects if the user has a camera and shows
     * a button if so.
     */
    public static HTML createCameraButton (String mediaIds)
    {
        FlashObject obj = new FlashObject(
            "camerabutton", clientPath("camerabutton.swf"), 160, 19,
            "mediaIds=" + URL.encodeComponent(mediaIds));
        obj.transparent = true;
        return WidgetUtil.createContainer(obj);
    }

    /**
     * Create a video player.
     *
     * @param path may be null to create an empty player that can be provided with
     *        video information later.
     */
    public static HTML createVideoPlayer (int width, int height, String path)
    {
        return WidgetUtil.createContainer(
            new FlashObject("videoPlayer", clientPath("videoplayer.swf"), width, height,
            (path == null) ? null : "video=" + URL.encodeComponent(path)));
    }

    /**
     * Create an audio player.
     */
    public static HTML createAudioPlayer (int width, int height, String path)
    {
        FlashObject player = new FlashObject("audioPlayer", clientPath("audioplayer.swf"),
            width, height, "audio=" + URL.encodeComponent(path));
        player.bgcolor = "#FFFFFF";
        return WidgetUtil.createContainer(player);
    }

    /**
     * Create the image editor swf.
     *
     * @param currentURL may be null
     * @param maxWidth or -1 to allow any width
     * @param maxHeight or -1 to allow any height
     * @param maxRequired whether the maxes are  maximums, or a _required_ size.
     */
    public static HTML createImageEditor (
        int width, int height, String mediaIds, boolean takeSnapshot, String currentURL,
        int maxWidth, int maxHeight, boolean maxRequired)
    {
        String flashVars = "auth=" + URL.encodeComponent(CShell.getAuthToken()) +
            "&mediaIds=" + URL.encodeComponent(mediaIds);
        if (takeSnapshot) {
            flashVars += "&takeSnapshot=true";
        }
        if (currentURL != null) {
            flashVars += "&url=" + URL.encodeComponent(currentURL);
        }
        if (maxWidth > 0 && maxHeight > 0) {
            String prefix = maxRequired ? "req" : "max";
            flashVars += "&" + prefix + "Width=" + maxWidth +
                "&" + prefix + "Height=" + maxHeight;
        }
        return WidgetUtil.createContainer(new FlashObject(
            "imageEditor", clientPath("imageeditor.swf"), width, height, flashVars));
    }

    /**
     * Create the Whirled Map.
     */
    public static void embedWhirledMap (Panel container, String flashvars)
    {
        FlashObject obj = new FlashObject(
            "map", clientPath("whirledmap.swf"), "100%", "100%", flashvars);
        obj.transparent = true;

        Widget embed = WidgetUtil.embedFlashObject(container, WidgetUtil.createDefinition(obj));
        embed.setHeight("100%");
        embed.setStyleName("whirledMap");
    }

    /**
     * Creates a world client, and embeds it in a container object, with which it can communicate
     * via the Flash/Javascript interface.
     */
    public static void embedWorldClient (Panel container, String flashVars)
    {
        if (!shouldShowFlash(container, 0, 0)) {
            return;
        }

        FlashObject obj = new FlashObject(
            ELEM_ID, clientPath("world-client.swf"), "100%", "100%", flashVars);
        obj.bgcolor = "#ffffff";

        Widget embed = WidgetUtil.embedFlashObject(container, WidgetUtil.createDefinition(obj));
        embed.setHeight("100%");
    }

    /**
     * Creates a featured places world client, and embeds it in the container object.
     */
    public static void embedFeaturedPlaceView (Panel container, String flashVars)
    {
        if (shouldShowFlash(container, FEATURED_PLACE_WIDTH, FEATURED_PLACE_HEIGHT)) {
            WidgetUtil.embedFlashObject(
                container, WidgetUtil.createDefinition(new FlashObject(
                    "featuredplace", clientPath("world-client.swf"),
                    FEATURED_PLACE_WIDTH, FEATURED_PLACE_HEIGHT, flashVars)));
        }
    }

    /**
     * Creates a decor viewer, and embeds it in the supplied HTML object which *must* be already
     * added to the DOM.
     */
    public static void embedDecorViewer (HTML html)
    {
        // see if we need to emit a warning instead
        String definition = CShell.frame.checkFlashVersion(600, 400);
        if (definition != null) {
            html.setHTML(definition);
            return;
        }

        html.setHTML(WidgetUtil.createDefinition(
            new FlashObject("decorViewer", clientPath("world-client.swf"), 600, 400,
            "mode=" + UberClientModes.DECOR_EDITOR + "&username=Tester")));
    }

    /**
     * Creates a solo game definition, as an object definition string.
     */
    public static String createSoloGameDefinition (String media)
    {
        String definition = CShell.frame.checkFlashVersion(800, 600);
        return definition != null ? definition :
            WidgetUtil.createDefinition(new FlashObject("game", media, 800, 600, null));
    }

    /**
     * Checks if the flash client can be found on this page.
     */
    public static boolean clientExists ()
    {
        return findClient() != null;
    }

    /**
     * Checks to see if the flash client exists and is connected to a server.
     */
    public static boolean clientConnected ()
    {
        return clientConnectedNative(findClient());
    }

    /**
     * Get the current sceneId of the flash client, or 0.
     */
    public static int getSceneId ()
    {
        return getSceneIdNative(findClient());
    }

    /**
     * Checks with the actionscript client to find out if our current scene is in fact a room.
     */
    public static boolean inRoom ()
    {
        return inRoomNative(findClient());
    }

    /**
     * Tells the actionscript client that we'd like to use this item in the current room.  This can
     * be used to add furni, or set the background audio or decor.
     */
    public static void useItem (byte itemType, int itemId)
    {
        useItemNative(findClient(), itemType, itemId);
    }

    /**
     * Tells the actionscript client to remove the given item from use.
     */
    public static void clearItem (byte itemType, int itemId)
    {
        clearItemNative(findClient(), itemType, itemId);
    }

    /**
     * Tells the actionscript client that we'd like to use this avatar.  If 0 is passed in for the
     * avatarId, the current avatar is simply cleared away, leaving you tofulicious.
     */
    public static void useAvatar (int avatarId)
    {
        useAvatarNative(findClient(), avatarId);
    }

    /**
     * Called to start the whirled tour.
     */
    public static void startTour ()
    {
        startTourNative(findClient());
    }

    /**
     * Returns the element that represents the Flash client.
     */
    public static native Element findClient () /*-{
        var id = @client.util.FlashClients::ELEM_ID;
        var client = $wnd.document.getElementById(id);
        try {
            if (client == null) {
                client = $wnd.parent.document.getElementById(id);
            }
        } catch (e) {
            // we may be running in an iframe on Facebook in which case touching parent will throw
            // an exception, so we just catch that here and go on about our business
        }
        return client;
    }-*/;

    /**
     * Checks if we have a specilized flash object to show, and if so, adds it to the container
     * and returns false, otherwise returns true.
     */
    protected static boolean shouldShowFlash (Panel container, int width, int height)
    {
        String definition = CShell.frame.checkFlashVersion(width, height);
        if (definition != null) {
            WidgetUtil.embedFlashObject(container, definition);
            return false;
        }
        return true;
    }

    /**
     * TEMP: Changes the height of the client already embedded in the page.
     */
    protected static native void setClientHeightNative (Element client, String height) /*-{
        if (client != null) {
            client.style.height = height;
        }
    }-*/;

    /**
     * Does the actual <code>getSceneId()</code> call.
     */
    protected static native int getSceneIdNative (Element client) /*-{
        if (client) {
            // exceptions from JavaScript break GWT; don't let that happen
            try { return client.getSceneId(); } catch (e) {}
        }
        return 0;
    }-*/;

    /**
     * Does the actual <code>inRoom()</code> call.
     */
    protected static native boolean inRoomNative (Element client) /*-{
        if (client) {
            // exceptions from JavaScript break GWT; don't let that happen
            try { return client.inRoom(); } catch (e) {}
        }
        return false;
    }-*/;

    /**
     * Does the actual <code>useItem()</code> call.
     */
    protected static native void useItemNative (Element client, byte itemType, int itemId) /*-{
        if (client) {
            // exceptions from JavaScript break GWT; don't let that happen
            try { client.useItem(itemType, itemId); } catch (e) {}
        }
    }-*/;

    /**
     * Does the actual <code>clearItem()</code> call.
     */
    protected static native void clearItemNative (Element client, byte itemType, int itemId) /*-{
        if (client) {
            // exceptions from JavaScript break GWT; don't let that happen
            try { client.clearItem(itemType, itemId); } catch (e) {}
        }
    }-*/;

    /**
     * Does the actual <code>useAvatar()</code> call.
     */
    protected static native void useAvatarNative (Element client, int avatarId) /*-{
        if (client) {
            // exceptions from JavaScript break GWT; don't let that happen
            try { client.useAvatar(avatarId); } catch (e) {}
        }
    }-*/;

    /**
     * Does the actual <code>startTour()</code> call.
     */
    protected static native void startTourNative (Element client) /*-{
        if (client) {
            // exceptions from JavaScript break GWT; don't let that happen
            try { client.startTour(); } catch (e) {}
        }
    }-*/;

    /**
     * Does the actual <code>clientConnected()</code> call.
     */
    protected static native boolean clientConnectedNative (Element client) /*-{
        if (client) {
            // exceptions from JavaScript break GWT; don't let that happen
            try { return client.isConnected(); } catch (e) {}
        }
        return false;
    }-*/;

    /** Create a versioned filesystem path for the named client. */
    protected static String clientPath (String filename)
    {
        return "/clients/" + DeploymentConfig.version + "/" + filename;
    }

    protected static final int CLIENT_HEIGHT = 552;
    protected static final int HEADER_HEIGHT = 24;
    protected static final int CTRLBAR_HEIGHT = 28;

    protected static final String HOOD_SKIN_URL = "/media/static/hood_pastoral.swf";
    protected static final int FEATURED_PLACE_WIDTH = 350;
    protected static final int FEATURED_PLACE_HEIGHT = 200;

    protected static final int MIN_INSTALLER_WIDTH = 310;
    protected static final int MIN_INSTALLER_HEIGHT = 137;
}
