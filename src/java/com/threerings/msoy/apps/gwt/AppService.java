//
// $Id$

package com.threerings.msoy.apps.gwt;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import com.threerings.web.gwt.ServiceException;

import com.threerings.msoy.facebook.gwt.FacebookInfo;
import com.threerings.msoy.facebook.gwt.FacebookTemplate;
import com.threerings.msoy.facebook.gwt.FeedThumbnail;
import com.threerings.msoy.facebook.gwt.KontagentInfo;

/**
 * Methods for the server to define the viewing and editing of application data.
 * @see AppServiceAsync
 */
@RemoteServiceRelativePath(value=AppService.REL_PATH)
public interface AppService extends RemoteService
{
    /**
     * All the data associated with an application.
     */
    public static class AppData
        implements IsSerializable
    {
        public AppInfo info;
        public FacebookInfo facebook;
        public KontagentInfo kontagent;
    }

    /** The entry point for this service. */
    public static final String ENTRY_POINT = "/appsvc";

    /** The relative path for this service. */
    public static final String REL_PATH = "../../.." + AppService.ENTRY_POINT;

    /**
     * Gets a list of all defined applications in info form.
     */
    List<AppInfo> getApps ()
        throws ServiceException;

    /**
     * Creates a new application with the given name and returns the id.
     */
    int createApp (String name)
        throws ServiceException;

    /**
     * Retrieves the application data for the given id.
     */
    AppData getAppData (int appId)
        throws ServiceException;

    /**
     * Deletes the application with the given id.
     */
    void deleteApp (int appId)
        throws ServiceException;

    /**
     * Updates the mutable parts of the given application info.
     */
    void updateAppInfo (AppInfo appInfo)
        throws ServiceException;

    /**
     * Updates the Facebook metadata associated with this application.
     */
    void updateFacebookInfo (FacebookInfo info)
        throws ServiceException;

    /**
     * Loads all facebook templates for an application.
     */
    List<FacebookTemplate> loadTemplates (int appId)
        throws ServiceException;

    /**
     * Saves changes to the given facebook templates and deletes the set of templates specified
     * by the given codes.
     */
    void updateTemplates (
        int appId, Set<FacebookTemplate> changed, Set<FacebookTemplate.Key> removed,
        Map<FacebookTemplate.Key, Boolean> abled)
        throws ServiceException;

    /**
     * Loads the feed thumbnails available for posts from the given application.
     */
    List<FeedThumbnail> loadThumbnails(int appId)
        throws ServiceException;

    /**
     * Sets the feed thumbnails available for posts from the given application.
     */
    void updateThumbnails(int appId, List<FeedThumbnail> thumbnails)
        throws ServiceException;

    /**
     * Sets the given application's Kontagent info.
     */
    void updateKontagentInfo (int appId, KontagentInfo kinfo)
        throws ServiceException;
}
