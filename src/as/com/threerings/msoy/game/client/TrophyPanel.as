//
// $Id$

package com.threerings.msoy.game.client {

import mx.containers.Grid;
import mx.containers.GridRow;
import mx.containers.HBox;
import mx.containers.VBox;
import mx.controls.Label;
import mx.controls.Text;
import mx.core.ScrollPolicy;

import com.threerings.io.TypedArray;

import com.threerings.orth.ui.MediaWrapper;

import com.threerings.flex.CommandButton;
import com.threerings.flex.FlexUtil;
import com.threerings.flex.GridUtil;

import com.threerings.msoy.client.Msgs;
import com.threerings.msoy.data.MsoyCodes;
import com.threerings.msoy.game.data.all.Trophy;
import com.threerings.msoy.item.data.all.TrophySource;
import com.threerings.msoy.ui.FloatingPanel;
import com.threerings.msoy.ui.MsoyMediaContainer;

/**
 * Displays all of the trophies available in a game along with info on earned trophies if the
 * player is logged in.
 */
public class TrophyPanel extends FloatingPanel
{
    public static function show (ctx :GameContext, gameId :int, gameName :String,
        gameDescription :String) :void
    {
        (ctx.getClient().requireService(GameGameService) as GameGameService).getTrophies(
            gameId, ctx.getWorldContext().resultListener(
                function (trophies :TypedArray) :void {
                    new TrophyPanel(ctx, trophies, gameName, gameDescription).open();
                }, MsoyCodes.GAME_MSGS));
    }

    /**
     * Returns the subset of the given array of trophies which have been earned.
     */
    public static function filterEarned (trophies :Array) :Array
    {
        if (trophies == null) {
            return [];
        }
        return trophies.filter(function (trophy :Trophy, ... ignored) :Boolean {
            return (trophy.whenEarned != null);
        });
    }

    public function TrophyPanel (ctx :GameContext, trophies :Array, gameName :String,
        gameDescription :String)
    {
        super(ctx.getWorldContext(), Msgs.GAME.get("t.trophy"));
        _gctx = ctx;
        _trophies = trophies;
        _gameName = gameName;
        _gameDescription = gameDescription;
    }

    override protected function createChildren () :void
    {
        super.createChildren();

        showCloseButton = true;
        // make this window completely opaque, so that the dimmed-out trophies look good
        setStyle("backgroundAlpha", 1);

        if (_trophies.length == 0) {
            addChild(FlexUtil.createLabel(Msgs.GAME.get("m.tp_title_none", _gameName)));
            addButtons(OK_BUTTON);
            return;
        }

        var title :Label = new Label();
        title.text = Msgs.GAME.get("m.tp_title", _gameName);
        title.styleName = "trophyPanelTitle";
        addChild(title);

        var grid :Grid = new Grid();
        grid.maxHeight = 380;
        grid.horizontalScrollPolicy = ScrollPolicy.OFF;
        grid.verticalScrollPolicy = ScrollPolicy.ON;
        addChild(grid);

        var row :GridRow = null;
        var cell :int = 0;
        for each (var trophy :Trophy in _trophies) {
            if (row == null) {
                row = new GridRow();
                grid.addChild(row);
            }
            var tbox :HBox = new HBox();
            tbox.addChild(new MediaWrapper(new MsoyMediaContainer(trophy.trophyMedia),
                    TrophySource.TROPHY_WIDTH, TrophySource.TROPHY_HEIGHT));
            var bits :VBox = new VBox();
            bits.setStyle("verticalGap", 0);
            var name :Label = FlexUtil.createLabel(trophy.name, "trophyPanelName");
            name.maxWidth = CELL_WIDTH;
            bits.addChild(name);
            var descrip :Text = FlexUtil.createText(
               (trophy.description == null) ? Msgs.GAME.get("m.tp_secret") : trophy.description,
               CELL_WIDTH, (trophy.description == null) ? "trophyPanelHidden" : null);
            bits.addChild(descrip);
            tbox.addChild(bits);
            if (trophy.whenEarned == null) {
                tbox.alpha = 0.35;
                name.setStyle("color", 0xAAAAAA);
                descrip.setStyle("color", 0xAAAAAA);
            }
            GridUtil.addToRow(row, tbox);
            if (++cell % 2 == 0) {
                row = null;
            }
        }

        var buttons :Array = [ OK_BUTTON ];

        // TODO: Disabled until FB Connect works again. Currently there's a problem where
        // TODO: requests end up in the Java layer with appId == 0 which is translated to
        // TODO: siteId == 0, where FBConnect's default siteId seems to be hard-coded to
        // TODO: 1, but not enough cycles to investigate how this should be done properly
        // TODO: at the moment.
//        if (filterEarned(_trophies).length > 0) {
//            buttons.push(new CommandButton(Msgs.GAME.get("b.tp_publish"), publishTrophies));
//        }
        addButtons.apply(this, buttons);
    }

    // TODO: removed until needed, see above
//    protected function publishTrophies () :void
//    {
//        close();
//        TrophyFeederPanel.showExisting(
//            _gctx.getWorldContext(), _gameName, _gameDescription, filterEarned(_trophies));
//    }

    protected var _gctx :GameContext;
    protected var _trophies :Array /*of Trophy*/;
    protected var _gameName :String;
    protected var _gameDescription :String;

    protected static const CELL_WIDTH :int = 200;
}
}
