//
// $Id$

package com.threerings.msoy.fora.gwt;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.threerings.msoy.web.gwt.MemberCard;

/**
 * Contains information on a forum message.
 */
public class ForumMessage
    implements IsSerializable
{
    /** The maximum length in characters of a forum message. */
    public static final int MAX_MESSAGE_LENGTH = 16384;

    /** This message's unique identifier. */
    public int messageId;

    /** This message's index in its owning thread's list. Note that this value is not filled in by
     * the server when a single message operation is being performed. */
    public int messageIndex;

    /** The id of the thread to which this message belongs. */
    public int threadId;

    /** The id of the message to which this message is a reply, or zero. */
    public int inReplyTo;

    /** The name and profile photo of the member that posted this message. */
    public MemberCard poster;

    /** The id of the issue to which is message is associated, or zero. */
    public int issueId;

    /** The time at which this message was created. */
    public Date created;

    /** The time at which this message was last edited. */
    public Date lastEdited;

    /** The text of this message. */
    public String message;
}
