//
// $Id$

package com.threerings.msoy.party.server;

import com.google.inject.Inject;

import com.threerings.presents.net.BootstrapData;
import com.threerings.presents.server.PresentsSession;

import com.threerings.msoy.party.data.PartierObject;
import com.threerings.msoy.party.data.PartyBootstrapData;
import com.threerings.msoy.party.data.PartyCredentials;
import com.threerings.msoy.server.MsoyObjectAccess;

import static com.threerings.msoy.Log.log;

/**
 * Handles a partier session.
 */
public class PartySession extends PresentsSession
{
    @Override // from PresentsSession
    protected void sessionWillStart ()
    {
        super.sessionWillStart();

        // set up our partier object
        _partierObj = (PartierObject) _clobj;
        _partierObj.setAccessController(MsoyObjectAccess.USER);
        _partierObj.setPartyId(((PartyCredentials)_areq.getCredentials()).partyId);
    }

    @Override // from PresentsSession
    protected void sessionDidEnd ()
    {
        super.sessionDidEnd();

        // clear out our partier object
        _partierObj = null;
    }

    @Override // from PresentsSession
    protected BootstrapData createBootstrapData ()
    {
        return new PartyBootstrapData();
    }

    @Override // from PresentsSession
    protected void populateBootstrapData (BootstrapData data)
    {
        super.populateBootstrapData(data);

        // fill in the oid
        PartyBootstrapData pdata = (PartyBootstrapData)data;
        int partyId = ((PartyCredentials)_areq.getCredentials()).partyId;
        PartyManager pmgr = _partyReg.getPartyManager(partyId);
        if (pmgr != null) {
            pdata.partyOid = pmgr.getPartyObject().getOid();
        } else {
            log.warning("Pants! Can't find party for partier", "partier", _authname,
                        "pid", partyId);
        }
    }

    @Override // from PresentsSession
    protected long getFlushTime ()
    {
        return 10 * 1000L; // give them just long enough to replace their session
    }

    protected PartierObject _partierObj;

    @Inject protected PartyRegistry _partyReg;
}
