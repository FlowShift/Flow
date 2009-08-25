//
// $Id$

package com.threerings.msoy.facebook.server;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.samskivert.util.StringUtil;
import com.threerings.msoy.data.all.DeploymentConfig;
import com.threerings.msoy.server.ServerConfig;
import com.threerings.msoy.web.gwt.SharedNaviUtil;

import static com.threerings.msoy.Log.log;

@Singleton
public class KontagentLogic
{
    /**
     * Kinds of links that we send, on behalf of a user or globally.
     */
    public enum LinkType
    {
        INVITE("inv", MessageType.INVITE_RESPONSE),
        NOTIFICATION("ntf", MessageType.NOTIFICATION_RESPONSE);

        /** The unique id for composing and parsing. */
        public String id;

        /** The type of message to send to Kontagent on click-through. */
        public MessageType responseType;

        LinkType (String id, MessageType responseType) {
            this.id = id;
            this.responseType = responseType;
        }
    }

    /**
     * A unique instance of a link we've sent out ourselves or on behalf of a user.
     */
    public static class SentLink
    {
        /** The type of link. */
        public LinkType type;

        /** The subtype (e.g. a game id). */
        public String subtype;

        /** The Kontagent tracking tag. */
        public String uuid;

        /**
         * Creates a new sent link of the given type, a null subtype and a new uuid using the given
         * initiator as a seed.
         */
        public SentLink (LinkType type, long initiator)
        {
            this(type, null, initiator);
        }

        /**
         * Creates a new sent link of the given type and subtype and a new uuid using the given
         * initiator as a seed.
         */
        public SentLink (LinkType type, String subtype, long initiator)
        {
            this.type = type;
            this.subtype = subtype;
            this.uuid = genUUID(initiator);
        }

        /**
         * Creates a new sent link from a previosuly composed tracking id. We assume the tracking
         * id is sent directly from the web, therefore an exception is thrown if it is not in the
         * expected format.
         * @throws IllegalArgumentException if the tracking id or any component is invalid
         */
        public SentLink (String trackingId)
        {
            String[] components = trackingId.split("-");
            if (components.length < 2 || components.length > 3) {
                throw new IllegalArgumentException();
            }
            for (LinkType type : LinkType.values()) {
                if (type.id.equals(components[0])) {
                    this.type = type;
                    break;
                }
            }
            if (type == null) {
                throw new IllegalArgumentException();
            }
            uuid = components[components.length - 1];
            if (!uuid.matches("[0-9a-f]{16}")) {
                throw new IllegalArgumentException();
            }
            if (components.length == 3) {
                subtype = components[1];
            }
        }

        /**
         * Composes a flattened tracking id representing this sent link that can be later picked up
         * from a URL to reconstruct the instance.
         */
        public String composeTrackingId ()
        {
            // type-subtype-uuid or type-uuid
            return type.id + (StringUtil.isBlank(subtype) ? "" : "-" + subtype) + "-" + uuid;
        }
    }

    /**
     * Builds the url for sending a kontagent message using the given name/value pairs, type,
     * timestamp and secrect. Blank values are removed. The time stamp and signature are appended
     * as required.
     */
    public static String buildMessageUrl (
        String baseUrl, String timeStamp, String secret, String... nameValuePairs)
    {
        // convert to map, eliminate blank parameters
        Map<String, String> params = Maps.newHashMap();
        for (int ii = 0; ii < nameValuePairs.length; ii += 2) {
            if (StringUtil.isBlank(nameValuePairs[ii+1])) {
                continue;
            }
            params.put(nameValuePairs[ii], nameValuePairs[ii+1]);
        }

        // add the time stamp
        params.put("ts", timeStamp);

        // sort by name
        List<String> names = Lists.newArrayListWithCapacity(params.size());
        names.addAll(params.keySet());
        Collections.sort(names);

        // build parameter list and signature buffer
        List<String> urlParams = Lists.newArrayListWithCapacity((names.size() + 1) * 2);
        StringBuilder sig = new StringBuilder();
        for (String name : names) {
            String value = params.get(name);
            sig.append(name).append("=").append(value);
            urlParams.add(name);
            urlParams.add(value);
        }
        sig.append(secret);

        // append signature
        urlParams.add("an_sig");
        urlParams.add(StringUtil.md5hex(sig.toString()));

        return SharedNaviUtil.buildRequest(baseUrl, urlParams);
    }

    /**
     * Generates a 16 hex digit Kontagent UUID for use in tracking invites etc.
     */
    public static String genUUID (long uid)
    {
        long salt = System.currentTimeMillis();
        for (int shift = 60; shift >= 0; shift -= 4, salt >>= 4) {
            uid ^= (salt & 0xF) << shift;
        }
        uid ^= _rand.nextLong();
        String hex = Long.toHexString(uid);
        if (hex.length() < 16) {
            hex = "0000000000000000".substring(hex.length()) + hex;
        }
        return hex;
    }

    /**
     * Tracks a visit to the application.
     * @param uid id of the user visiting
     * @param trackingId the tracking id given via the canvas url
     * @param newInstall if this is the user's first visit after the login redirect
     */
    public void trackUsage (long uid, String trackingId, boolean newInstall)
    {
        SentLink sent;
        try {
            sent = StringUtil.isBlank(trackingId) ? null : new SentLink(trackingId);
        } catch (IllegalArgumentException ex) {
            log.warning("Invalid tracking id", trackingId);
            return;
        }

        String uidStr = String.valueOf(uid);
        if (newInstall) {
            if (sent != null) {
                sendMessage(MessageType.APP_ADDED, "s", uidStr, "u", sent.uuid);
            } else {
                sendMessage(MessageType.APP_ADDED, "s", uidStr, "su", ShortTag.UNKNOWN.id);
            }
        }

        if (sent == null) {
            // TODO: hmm, new short tag for different sources? (e.g. app bookmark vs. clicking the
            // app link in an invite)
            sendMessage(MessageType.UNDIRECTED, "s", uidStr, "tu", ShortTag.UNKNOWN.id, "i", "1");

        } else {
            switch (sent.type.responseType) {
            case INVITE_RESPONSE:
            case NOTIFICATION_RESPONSE:
                sendMessage(sent.type.responseType, "r", uidStr, "i", "1", "u", sent.uuid,
                    "st1", sent.subtype, "tu", sent.type.responseType.id);
                break;
            default:
                log.warning("Unhandled response type", "trackingId", trackingId);
                break;
            }
        }
    }

    /**
     * Tracks the removal of the application.
     */
    public void trackApplicationRemoved (long uid)
    {
        sendMessage(MessageType.APP_REMOVED, "s", String.valueOf(uid));
    }

    /**
     * Tracks an invite sent by the user. The tracking id is normally obtained from a previously
     * constructed {@link SentLink} and passed via the cgi invite submission parameters.
     */
    public void trackInviteSent (long senderId, String trackingId, String[] recipients)
    {
        SentLink link = parseTrackingId(trackingId, false);
        if (link == null) {
            return;
        }

        sendMessage(MessageType.INVITE_SENT, "s", String.valueOf(senderId),
            "r", StringUtil.join(recipients, StringUtil.encode(",")), "u", link.uuid,
            "st1", link.subtype);
    }

    /**
     * Track the sending of a notification. The tracking id is normally generated during the
     * scheduling of the notification and passed back after it is actually sent.
     */
    public void trackNotificationSent (
        long senderId, String trackingId, Collection<String> recipients)
    {
        SentLink link = parseTrackingId(trackingId, false);
        if (link == null) {
            return;
        }

        sendMessage(MessageType.NOTIFICATION_SENT, "s", String.valueOf(senderId),
            "r", StringUtil.join(recipients.toArray(), StringUtil.encode(",")), "u", link.uuid,
            "st1", link.subtype);
    }

    protected SentLink parseTrackingId (String trackingId, boolean allowBlank)
    {
        if (allowBlank && StringUtil.isBlank(trackingId)) {
            return null;
        }

        try {
            return new SentLink(trackingId);

        } catch (IllegalArgumentException ex) {
            log.warning("Invalid tracking id", trackingId);
            return null;
        }
    }

    protected void sendMessage (MessageType type, String... nameValuePairs)
    {
        String url = buildMessageUrl(MSG_URL + type.id + "/",
            String.valueOf(System.currentTimeMillis()), SECRET, nameValuePairs);

        if (StringUtil.isBlank(API_KEY)) {
            // this is a dev deployment or kontagent is disabled
            log.info("Kontagent disabled, skipping message", "url", url);
            return;
        }

        if (DeploymentConfig.devDeployment) {
            log.info("Sending message", "url", url);
        }

        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                log.warning("Response code not OK", "code", conn.getResponseCode(), "url", url);
            }
            conn.disconnect();

        } catch (Exception ex) {
            log.warning("Failed to send message", "url", url, ex);
        }
    }

    /**
     * Kontagent message types.
     */
    protected enum MessageType
    {
        APP_ADDED("apa"),
        APP_REMOVED("apr"),
        UNDIRECTED("ucc"),
        INVITE_RESPONSE("inr"),
        INVITE_SENT("ins"),
        NOTIFICATION_SENT("nts"),
        NOTIFICATION_RESPONSE("ntr");

        public String id;

        MessageType (String id) {
            this.id = id;
        }
    }

    /**
     * Kontagent short tag types.
     */
    protected enum ShortTag
    {
        UNKNOWN("unknown");

        public String id;

        ShortTag (String id) {
            this.id = id;
        }
    }

    protected static final Random _rand = new Random();

    protected static final String API_URL = DeploymentConfig.devDeployment ?
        "http://api.test.kontagent.net/api/" : "http://api.geo.kontagent.net/api/";
    protected static final String VERSION = "v1";
    protected static final String API_KEY = ServerConfig.config.getValue("kontagent.api_key", "");
    protected static final String SECRET = ServerConfig.config.getValue("kontagent.secret", "");
    protected static final String MSG_URL = API_URL + VERSION + "/" + API_KEY + "/";
}
