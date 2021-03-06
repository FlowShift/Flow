//
// $Id$

package com.threerings.msoy.party.server;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.msoy.party.client.PartyService;

/**
 * Defines the server-side of the {@link PartyService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from PartyService.java.")
public interface PartyProvider extends InvocationProvider
{
    /**
     * Handles a {@link PartyService#assignLeader} request.
     */
    void assignLeader (ClientObject caller, int arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link PartyService#bootMember} request.
     */
    void bootMember (ClientObject caller, int arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link PartyService#inviteMember} request.
     */
    void inviteMember (ClientObject caller, int arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link PartyService#moveParty} request.
     */
    void moveParty (ClientObject caller, int arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link PartyService#setGame} request.
     */
    void setGame (ClientObject caller, int arg1, byte arg2, int arg3, InvocationService.InvocationListener arg4)
        throws InvocationException;

    /**
     * Handles a {@link PartyService#updateDisband} request.
     */
    void updateDisband (ClientObject caller, boolean arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link PartyService#updateRecruitment} request.
     */
    void updateRecruitment (ClientObject caller, byte arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link PartyService#updateStatus} request.
     */
    void updateStatus (ClientObject caller, String arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;
}
