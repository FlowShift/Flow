//
// $Id$

package com.threerings.msoy.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import com.threerings.msoy.web.data.WebIdent;

/**
 * The asynchronous (client-side) version of {@link CommentService}.
 */
public interface CommentServiceAsync
{
    /**
     * The asynchronous version of {@link CommentService#loadComments}.
     */
    public void loadComments (int entityType, int entityId, int offset, int count,
                              boolean needCount, AsyncCallback callback);

    /**
     * The asynchronous version of {@link CommentService#postComment}.
     */
    public void postComment (WebIdent ident, int entityType, int entityId, String text,
                             AsyncCallback callback);

    /**
     * The asynchronous version of {@link CommentService#deleteComment}.
     */
    public void deleteComment (WebIdent ident, int entityType, int entityId, long when,
                               AsyncCallback callback);

    /**
     * The asynchronous version of {@link CommentService#complainComment}.
     */
    public void complainComment (WebIdent ident, String subject, int entityType, int entityId,
                                 long when, AsyncCallback callback);
}
