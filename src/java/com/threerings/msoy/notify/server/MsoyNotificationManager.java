//
// $Id$

package com.threerings.msoy.notify.server;

import com.google.inject.Singleton;

import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.dobj.DObject;

import com.threerings.orth.notify.data.NotificationLocal;
import com.threerings.orth.notify.server.NotificationManager;

import com.threerings.msoy.comment.data.all.CommentType;
import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.data.all.MemberName;
import com.threerings.msoy.notify.data.EntityCommentedNotification;
import com.threerings.msoy.notify.data.FollowInviteNotification;
import com.threerings.msoy.notify.data.GameInviteNotification;
import com.threerings.msoy.notify.data.InviteAcceptedNotification;
import com.threerings.msoy.notify.data.PokeNotification;
import com.threerings.msoy.server.MemberLocal;
import com.threerings.msoy.server.MemberNodeActions;

/**
 * Manages most notifications to users.
 */
@Singleton @EventThread
public class MsoyNotificationManager extends NotificationManager
{
    public MsoyNotificationManager ()
    {
        super(MemberObject.NOTIFICATION);
    }

    /**
     * Notify the specified member that an invitation they sent has been accepted by a new whirled
     * member!
     */
    public void notifyInvitationAccepted (
        int inviterId, MemberName invitee, String inviteeEmail)
    {
        MemberNodeActions.sendNotification(inviterId,
            new InviteAcceptedNotification(invitee, inviteeEmail));
    }

    /**
     * Notifies the member that someone commented on something they own or created
     */
    public void notifyEntityCommented (
        int targetId, CommentType entityType, int entityId, String entityName)
    {
        MemberNodeActions.sendNotification(targetId,
            new EntityCommentedNotification(entityType, entityId, entityName));
    }

    /**
     * Notifies the target player that they've been invited to play a game.
     */
    public void notifyGameInvite (MemberObject target, MemberName inviter,
                                  String game, int gameId)
    {
        notify(target, new GameInviteNotification(inviter, game, gameId));
    }

    /**
     * Notifies the target player that they've been invited to follow someone.
     */
    public void notifyFollowInvite (MemberObject target, MemberName inviter)
    {
        notify(target, new FollowInviteNotification(inviter));
    }

    /**
     * Notifies a player that they've received a poke.
     */
    public void notifyPoke (int memberId, MemberName poker)
    {
        MemberNodeActions.sendNotification(memberId, new PokeNotification(poker));
    }

    @Override protected NotificationLocal getLocal (DObject target)
    {
        return target.getLocal(MemberLocal.class);
    }
}
