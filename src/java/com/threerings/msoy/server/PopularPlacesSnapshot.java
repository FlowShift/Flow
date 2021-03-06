//
// $Id$

package com.threerings.msoy.server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.RandomUtil;

import com.threerings.presents.annotation.EventThread;
import com.threerings.presents.server.PresentsDObjectMgr;

import com.threerings.msoy.data.all.GroupName;
import com.threerings.msoy.game.server.GameUtil;
import com.threerings.msoy.peer.data.HostedPlace;
import com.threerings.msoy.peer.data.HostedRoom;
import com.threerings.msoy.peer.data.MemberGame;
import com.threerings.msoy.peer.data.MemberScene;
import com.threerings.msoy.peer.data.MsoyNodeObject;
import com.threerings.msoy.peer.server.MsoyPeerManager;
import com.threerings.msoy.room.data.MsoySceneModel;
import com.threerings.msoy.server.persist.MemberRepository;

/**
 * Contains a snapshot of all of the popular places in the Whirled.
 */
public class PopularPlacesSnapshot
{
    /** We keep track of only this many top scenes and games. */
    public static final int MAX_TRACKED_PLACES = 50;

    /** Contains id, name and population of a place. */
    public static class Place implements Comparable<Place> {
        /** The place id. */
        public int placeId;

        /** The place's name. */
        public String name;

        /** The place's population (as of the last snapshot calculation). */
        public int population;

        /** Whether this is a room and there are good DJs here. */
        public boolean hopping;

        public float getWeight ()
        {
            // Hopping rooms are weighted higher
            return hopping ? 1.5f * population : population;
        }

        // from interface Comparable<Place>
        public int compareTo (Place other) {
            // higher weight sorts first, then sort alphabetically
            return ComparisonChain.start().compare(other.getWeight(), getWeight())
                .compare(name, other.name).result();
        }
    }

    /**
     * Iterates over all the games, lobbies and the scenes in the world, finds out the N most
     * populated ones and sorts all scenes by owner, caching the values. Also builds lists of
     * greeters, sorting by who is online.
     * @param popularThemes
     *
     * @param greeterIds the most recently read list of greeter ids from {@link
     * MemberRepository#loadGreeterIds()} (sorted by last online).
     */
    @EventThread
    public static PopularPlacesSnapshot takeSnapshot (
        PresentsDObjectMgr omgr, MsoyPeerManager peerMan, List<GroupName> popularThemes,
        List<Integer> greeterIds)
    {
        omgr.requireEventThread();
        return new PopularPlacesSnapshot(peerMan, popularThemes, greeterIds);
    }

    /**
     * Returns a list of the {@link #MAX_TRACKED_PLACES} most popular scenes in the world, sorted
     * in descending order of population.
     */
    public List<Place> getTopScenes ()
    {
        return _sclist;
    }

    /**
     * Returns a list of the {@link #MAX_TRACKED_PLACES} most popular groups in the world, sorted
     * in descending order of population. <em>Note:</em> the {@link Place#name} field for these
     * records is not valid and the {@link Place#placeId} field is the Whirled's id.
     */
    public List<Place> getTopGroups ()
    {
        return _grlist;
    }

    /**
     * Returns a list of the {@link #MAX_TRACKED_PLACES} most popular games in the world, sorted in
     * descending order of population.
     */
    public List<Place> getTopGames ()
    {
        return _glist;
    }

    /**
     * Returns a list of the {@link #MAX_TRACKED_PLACES} most popular themes in the world, sorted
     * in descending order of population.
     */
    public List<Place> getTopThemes ()
    {
        return _thlist;
    }

    /**
     * Returns a list of the greeters currently registered on the system. Online ones are first
     * and they are sorted by last online time.
     */
    public List<Integer> getGreeters ()
    {
        return _greeters;
    }

    /**
     * Returns a list of the greeters currently online.
     */
    public List<Integer> getOnlineGreeters ()
    {
        return _onlineGreeters;
    }

    /**
     * Returns the place summary information for the specified groupd.
     */
    public Place getGroup (int groupId)
    {
        return _groups.get(groupId);
    }

    /**
     * Returns the place summary information for the specified scene.
     */
    public Place getScene (int sceneId)
    {
        return _scenes.get(sceneId);
    }

    /**
     * Pick a random resolved scene from the list.
     */
    public Place getRandomResolvedScene ()
    {
        return _scenes.isEmpty() ? null : RandomUtil.pickRandom(_scenes.values());
    }

    /**
     * Returns the place summary information for the specified game.
     */
    public Place getGame (int gameId)
    {
        return _games.get(gameId);
    }

    public int getThemePopulation (int themeId)
    {
        Place place = _themes.get(themeId);
        return (place != null) ? place.population : 0;
    }

    /**
     * Return the total population count in the whirled.
     */
    public int getPopulationCount ()
    {
        return _totalPopulation;
    }

    protected PopularPlacesSnapshot (
        MsoyPeerManager peerMan, List<GroupName> popularThemes, List<Integer> greeterIds)
    {
        // intermediate greeter hash tables
        final Set<Integer> greeters = Sets.newHashSet(greeterIds);
        final Set<Integer> onlineGreeters = Sets.newHashSet();

        // use to count up members online
        final Set<Integer> seenIds = Sets.newHashSet();

        // make a list of the most popular themes, as places
        for (GroupName theme : popularThemes) {
            Place place = new Place();
            place.name = theme.toString();
            place.placeId = theme.getGroupId();
            _themes.put(place.placeId, place);
            _thlist.add(place);
        }

        // now we can count up the population in all scenes, whirleds and games. collect greeter
        // online info while we're at it
        for (MsoyNodeObject mnobj : peerMan.getMsoyNodeObjects()) {
            for (MemberScene ms : mnobj.memberScenes) {
                seenIds.add(ms.memberId);

                HostedRoom room = mnobj.hostedScenes.get(ms.sceneId);
                if (room != null && room.accessControl == MsoySceneModel.ACCESS_EVERYONE) {
                    if (room.ownerType == MsoySceneModel.OWNER_TYPE_GROUP) {
                        // map whirled rooms by whirled id
                        increment(_groups, _grlist, room.ownerId, room);
                    }
                    increment(_scenes, _sclist, room.placeId, room);

                    // mark as an online greeter if appropriate
                    if (greeters.contains(ms.memberId)) {
                        onlineGreeters.add(ms.memberId);
                    }

                    if (room.themeId != 0) {
                        increment(_themes, null, room.themeId, room);
                    }
                }
            }

            for (MemberGame mg : mnobj.memberGames) {
                seenIds.add(mg.memberId);

                if (!GameUtil.isDevelopmentVersion(mg.gameId)) {
                    HostedPlace game = mnobj.hostedGames.get(mg.gameId);
                    if (game != null) {
                        // map games by game id
                        increment(_games, _glist, game.placeId, game);
                    }
                }
            }
        }

        // note our total population
        _totalPopulation = seenIds.size();

        // sort and prune our top places list
        sortAndPrune(_grlist);
        sortAndPrune(_sclist);
        sortAndPrune(_glist);

        // build sorted lists of greeter ids (do two passes for a cheap stable sort)
        List<Integer> glist = Lists.newArrayListWithExpectedSize(greeters.size());
        List<Integer> oglist = Lists.newArrayListWithExpectedSize(onlineGreeters.size());
        for (Integer id : greeterIds) {
            if (onlineGreeters.contains(id)) {
                glist.add(id);
                oglist.add(id);
            }
        }
        for (Integer id : greeterIds) {
            if (greeters.contains(id) && !onlineGreeters.contains(id)) {
                glist.add(id);
            }
        }

        // and voila, we are read-only lists
        _greeters = Collections.unmodifiableList(glist);
        _onlineGreeters = Collections.unmodifiableList(oglist);
    }

    protected static void increment (Map<Integer, Place> places, List<Place> plist,
                                     int placeId, HostedPlace hp)
    {
        Place place = places.get(placeId);
        if (place == null) {
            place = new Place();
            place.placeId = placeId;
            place.name = hp.name;
            if (hp instanceof HostedRoom) {
                place.hopping = ((HostedRoom) hp).hopping;
            }
            places.put(placeId, place);
            if (plist != null) {
                plist.add(place);
            }
        }
        place.population++;
    }

    protected static void sortAndPrune (List<Place> list)
    {
        Collections.sort(list);
        CollectionUtil.limit(list, MAX_TRACKED_PLACES);
    }

    /** The total number of people in the Whirled. */
    protected int _totalPopulation;

    /** A mapping of all resolved groups in the whole wide Whirled. */
    protected final Map<Integer, Place> _groups = Maps.newHashMap();

    /** A mapping of all resolved scenes in the whole wide Whirled. */
    protected final Map<Integer, Place> _scenes = Maps.newHashMap();

    /** A mapping of all resolved games in the whole wide Whirled. */
    protected final Map<Integer, Place> _games = Maps.newHashMap();

    /** A mapping of all resolved themes in the whole wide Whirled. */
    protected final Map<Integer, Place> _themes = Maps.newHashMap();

    /** The most popular groups, sorted. */
    protected final List<Place> _grlist = Lists.newArrayList();

    /** The most popular scenes, sorted. */
    protected final List<Place> _sclist = Lists.newArrayList();

    /** The most popular games, sorted. */
    protected final List<Place> _glist = Lists.newArrayList();

    /** The most popular themes, sorted. */
    protected final List<Place> _thlist = Lists.newArrayList();

    /** Greeters, sorted by online now then last online time. */
    protected final List<Integer> _greeters;

    /** Greeters, only online ones. */
    protected final List<Integer> _onlineGreeters;
}
