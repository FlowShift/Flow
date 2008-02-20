//
// $Id$

package client.people;

import com.threerings.gwt.ui.SmartTable;

import com.threerings.msoy.game.data.all.Trophy;
import com.threerings.msoy.web.client.ProfileService;

import client.games.TrophyGrid;
import client.shell.Application;
import client.shell.Args;
import client.shell.Page;

/**
 * Displays a member's recently earned trophies.
 */
public class TrophiesBlurb extends Blurb
{
    // @Override // from Blurb
    public boolean shouldDisplay (ProfileService.ProfileResult pdata)
    {
        return (pdata.trophies != null && pdata.trophies.size() > 0);
    }

    // @Override // from Blurb
    public void init (ProfileService.ProfileResult pdata)
    {
        super.init(pdata);
        setBlurbTitle(CPeople.msgs.trophiesTitle());

        // display our trophies in a nice grid
        SmartTable grid = new SmartTable(0, 4);
        Trophy[] tvec = (Trophy[])pdata.trophies.toArray(new Trophy[pdata.trophies.size()]);
        TrophyGrid.populateTrophyGrid(grid, tvec);
        setContent(grid);

        setFooterLink(CPeople.msgs.seeAll(), Page.GAMES,
                      Args.compose("t", pdata.name.getMemberId()));
    }
}
