//
// $Id$

package com.threerings.msoy.mail.gwt;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import com.threerings.web.gwt.ServiceException;

import com.threerings.msoy.data.all.MemberName;

/**
 * Defines mail services available to the GWT/AJAX web client.
 */
@RemoteServiceRelativePath(MailService.REL_PATH)
public interface MailService extends RemoteService
{
    /** Communicates results for {@link #loadConversation}. */
    public static class ConvosResult implements IsSerializable
    {
        /** The number of unread conversations (if requested). */
        public int unreadConvoCount;

        /** The conversations. */
        public List<Conversation> convos;
    }

    /** Communicates results for {@link #loadConversation}. */
    public static class ConvoResult implements IsSerializable
    {
        /** The name of the other party to this conversation. Null if this result is for a member
         * of support staff reviewing a complained conversation. */
        public MemberName other;

        /** The subject of the conversation. */
        public String subject;

        /** The sent time of the last message in this conversation the caller has read. */
        public long lastRead;

        /** The messages in this conversation. */
        public List<ConvMessage> messages;
    }

    /** The entry point for this service. */
    public static final String ENTRY_POINT = "/mailsvc";

    /** The relative path for this service. */
    public static final String REL_PATH = "../../.." + MailService.ENTRY_POINT;

    /**
     * Loads the specified range of conversations in which the caller is a participant.
     */
    ConvosResult loadConversations (int offset, int count, boolean needCount)
        throws ServiceException;

    /**
     * Loads the specified conversation.
     */
    ConvoResult loadConversation (int convoId)
        throws ServiceException;

    /**
     * Starts a conversation with another member.
     */
    void startConversation (int recipientId, String subject, String body, MailPayload attachment)
        throws ServiceException;

    /**
     * Posts a message to an existing conversation.
     */
    ConvMessage continueConversation (int convoId, String text, MailPayload attachment)
        throws ServiceException;

    /**
     * Deletes a conversation (for the calling user).
     *
     * @return true if the conversation was deleted, false if it could not be deleted because it
     * has unread messages and ignoreUnread was not set to true.
     */
    boolean deleteConversation (int convoId, boolean ignoreUnread)
        throws ServiceException;

    /**
     * Deletes multiple conversations (for the calling user).
     */
    void deleteConversations (List<Integer> convoIds)
        throws ServiceException;

    /**
     * Updates the payload on the specified message.
     */
    void updatePayload (int convoId, long sent, MailPayload payload)
        throws ServiceException;

    /**
     * Marks this conversation as under review and sends a support event.
     */
    void complainConversation (int convoId, String reason)
        throws ServiceException;
}
