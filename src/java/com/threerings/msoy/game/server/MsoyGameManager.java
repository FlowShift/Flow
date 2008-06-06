//
// $Id$

package com.threerings.msoy.game.server;

import com.samskivert.util.StringUtil;

import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.server.PlaceManagerDelegate;

import com.threerings.bureau.server.BureauRegistry;

import com.whirled.game.server.WhirledGameManager;
import com.whirled.game.server.GameCookieManager;

import com.threerings.msoy.game.data.MsoyGameConfig;

import com.threerings.msoy.server.MsoyBaseServer;

/**
 * Manages a MetaSOY game.
 */
@EventThread
public class MsoyGameManager extends WhirledGameManager
{
    public MsoyGameManager ()
    {
        super();
    }

    @Override
    public void addDelegate (PlaceManagerDelegate delegate)
    {
        super.addDelegate(delegate);

        if (delegate instanceof MsoyGameManagerDelegate) {
            _awardDelegate = (MsoyGameManagerDelegate) delegate;
        }
    }

    // from interface WhirledGameProvider
    public void awardTrophy (
        ClientObject caller, String ident, InvocationService.InvocationListener listener)
        throws InvocationException
    {
        _awardDelegate.awardTrophy(caller, ident, listener);
    }

    // from interface WhirledGameProvider
    public void awardPrize (
        ClientObject caller, String ident, InvocationService.InvocationListener listener)
        throws InvocationException
    {
        _awardDelegate.awardPrize(caller, ident, listener);
    }

    // from interface WhirledGameProvider
    public void endGameWithScores (
        ClientObject caller, int[] playerOids, int[] scores, int payoutType,
        InvocationService.InvocationListener listener)
        throws InvocationException
    {
        _awardDelegate.endGameWithScores(caller, playerOids, scores, payoutType, listener);
    }

    // from interface WhirledGameProvider
    public void endGameWithWinners (
        ClientObject caller, int[] winnerOids, int[] loserOids, int payoutType,
        InvocationService.InvocationListener listener)
        throws InvocationException
    {
        _awardDelegate.endGameWithWinners(caller, winnerOids, loserOids, payoutType, listener);
    }
    
    @Override // from WhirledGameManager
    public void agentTrace (ClientObject caller, String trace)
    {
        super.agentTrace(caller, trace);
        _awardDelegate.recordAgentTrace(trace);
    }

    @Override // from PlaceManager
    public String where ()
    {
        if (_config == null || _plobj == null) {
            return super.where();
        }
        MsoyGameConfig cfg = (MsoyGameConfig)_config;
        return "[" + cfg.name + ":" + cfg.getGameId() + ":" + _gameobj.getOid() +
            "(" + StringUtil.toString(_gameobj.players) + ")";
    }

    @Override // from WhirledGameManager
    protected GameCookieManager createCookieManager ()
    {
        return new GameCookieManager(MsoyGameServer.gameCookieRepo);
    }

    @Override // from GameManager
    protected long getNoShowTime ()
    {
        // because in Whirled we start the game before the client begins downloading the game
        // media, we have to be much more lenient about noshow timing (or revamp a whole bunch of
        // other shit which maybe we'll do later)
        return 1000L * ((getPlayerSlots() == 1) ? 180 : 90);
    }

    @Override // from WhirledGameManager
    protected BureauRegistry getBureauRegistry ()
    {
        return MsoyBaseServer.breg;
    }

    /** A delegate that takes care of flow, ratings, trophy, prizes.. */
    protected MsoyGameManagerDelegate _awardDelegate;
}
