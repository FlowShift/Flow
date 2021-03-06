//
// $Id$

package client.game;

import java.util.Collections;
import java.util.List;

import com.google.gwt.core.client.GWT;

import com.threerings.msoy.game.gwt.ArcadeData;
import com.threerings.msoy.game.gwt.GameGenre;
import com.threerings.msoy.game.gwt.GameInfo;
import com.threerings.msoy.game.gwt.GameService;
import com.threerings.msoy.game.gwt.GameServiceAsync;
import com.threerings.msoy.web.gwt.Pages;

import client.util.InfoCallback;
import client.util.Link;

/**
 * Displays games in a particular genre, or of all genres for the "All Games" page.
 * For genre pages it displays a featured game; for "All Games" page displays a search.
 */
public class GameGenrePanel extends SortedGameListPanel
{
    public GameGenrePanel (ArcadeData.Portal portal, GameGenre genre, final GameInfo.Sort sort,
        String query)
    {
        super(sort);
        _genre = genre;

        String titleText = _dmsgs.xlate("genreTitle_" + genre);;
        add(_header = new GameHeaderPanel(portal, titleText, genre, sort));
        _header.setQuery(query);

        _gamesvc.loadGameGenre(portal, genre, query, new InfoCallback<List<GameInfo>>() {
            public void onSuccess (List<GameInfo> games) {
                Collections.sort(games, GameInfo.Sort.BY_NAME.comparator);
                _header.initWithInfos(games); // set the dropdown list of all games
                Collections.sort(games, sort.comparator);
                add(new GameGrid(games));
            }
        });
    }

    protected void onSortChanged (GameInfo.Sort sort)
    {
        Link.go(Pages.GAMES, "g", _genre.toByte(), sort.toToken(), _header.getQuery());
    }

    /** Our currently displayed genre */
    protected GameGenre _genre;

    /** Header area with title, games dropdown and search */
    protected GameHeaderPanel _header;

    protected static final GameServiceAsync _gamesvc = GWT.create(GameService.class);
}
