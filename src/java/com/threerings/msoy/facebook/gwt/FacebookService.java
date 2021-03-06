//
// $Id$

package com.threerings.msoy.facebook.gwt;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import com.threerings.web.gwt.ServiceException;

import com.threerings.msoy.web.gwt.ArgNames;

/**
 * Provides services for munging Facebook and Whirled data for display.
 */
@RemoteServiceRelativePath(value=FacebookService.REL_PATH)
public interface FacebookService extends RemoteService
{
    public static final String ENTRY_POINT = "/fbpage";
    public static final String REL_PATH = "../../.." + FacebookService.ENTRY_POINT;

    /** Hard-wired feed story and thumbnail code for trophy posting. */
    public static final String TROPHY = "trophy";

    /** Hard-wired feed story and thumbnail code for challenge posts. */
    public static final String CHALLENGE = "challenge";

    /** Hard-wired feed story and thumbnail code for levl up posts. */
    public static final String LEVELUP = "levelup";

    /**
     * Genders for the purposes of a facebook invite.
     */
    public static enum Gender { MALE, FEMALE, NEUTRAL };

    /**
     * Provides data for the invitaion request form.
     */
    public static class InviteInfo
        implements IsSerializable
    {
        /** The sending user's name. */
        public String username;

        /** The sending user's gender, for pronoun selection. */
        public Gender gender;

        /** The game being invited to, only used when requesting a info for a game. */
        public String gameName;

        /** The friends to exclude (ones that are already using the application). */
        public List<Long> excludeIds;

        /** The id to stick into the acceptance URL's tracking parameter. */
        public String trackingId;

        /** The name of the application in use. */
        public String appName;

        /** The facebook canvas name of the application. */
        public String canvasName;

        /**
         * Returns an array of the tracking parameter name and tracking id.
         */
        public String[] trackingArgs ()
        {
            return new String[] {ArgNames.FBParam.TRACKING.name, trackingId};
        }
    }

    /**
     * Represents a kind of feed story, not the fields themselves.
     */
    public static class StoryKey
        implements IsSerializable
    {
        /** The application the story is for. */
        public int appId;

        /** The type of feed story, also a database key for the template lookup. */
        public String code;

        /** The game the feed story is for, also a database key for finding custom thumbnails. */
        public FacebookGame game;

        /**
         * Creates a new story key for the given application and type code.
         */
        public StoryKey (int appId, String code)
        {
            this(appId, code, null);
        }

        /**
         * Creates a new story key for the given application, type code and game.
         */
        public StoryKey (int appId, String code, FacebookGame game)
        {
            this.appId = appId;
            this.code = code;
            this.game = game;
        }

        // for serialization
        protected StoryKey () {}
    }

    /**
     * Data required for publishing a simple feed story.
     */
    public static class StoryFields
        implements IsSerializable
    {
        /** The template bundle to use, normally a randomly selected variant. */
        public FacebookTemplate template;

        /** The generated tracking id to embed into the story's links. */
        public String trackingId;

        /** The thumbnail URLs to use in the story. */
        // TODO: we'd like to use MediaDesc here, but it is hard-wired for only s3 hash-based URLs
        public List<String> thumbnails;

        /** The game name. */
        public String name;

        /** The game description, normally used as the body of the story. */
        public String description;

        /** The facebook user id of the requesting user. */
        public long fbuid;

        /** The facebook canvas name of the application. */
        public String canvasName;
    }

    /**
     * Gets the story fields for the given key. If no templates are found, throws an exception.
     */
    StoryFields getStoryFields (StoryKey key)
        throws ServiceException;

    /**
     * Notes that the user published a feed story. This may involve sending a message to Kontagent
     * or other things.
     */
    void trackStoryPosted (StoryKey key, String ident, String trackingId)
        throws ServiceException;

    /**
     * Retrieves the list of friends and their associated info for the currently logged in user.
     */
    List<FacebookFriendInfo> getAppFriendsInfo (int appId)
        throws ServiceException;

    /**
     * Retrieves the list of friends who have played the given game and their associated info for
     * the currently logged in user.
     * TODO: this is not yet being used because mochi games are totally unintegrated (no ratings or
     * trophies).
     */
    List<FacebookFriendInfo> getGameFriendsInfo (int appId, int gameId)
        throws ServiceException;

    /**
     * Retrieves the information for sending an invite to the given game, or the application if
     * if game is null.
     */
    InviteInfo getInviteInfo (int appId, FacebookGame game)
        throws ServiceException;

    /**
     * Lets the server know that a user is loading the given page. The page is in "url" form as
     * returned by {@link com.threerings.msoy.web.gwt.Args#toPath()}.
     */
    void trackPageRequest (int appId, String page)
        throws ServiceException;
}
