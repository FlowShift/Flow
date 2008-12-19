//
// $Id$

package com.threerings.msoy.profile.server;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.samskivert.util.IntSet;

import com.threerings.parlor.rating.server.persist.RatingRecord;
import com.threerings.parlor.rating.server.persist.RatingRepository;

import com.threerings.msoy.data.CoinAwards;
import com.threerings.msoy.data.UserAction;
import com.threerings.msoy.data.all.Award;
import com.threerings.msoy.data.all.DeploymentConfig;
import com.threerings.msoy.data.all.FriendEntry;
import com.threerings.msoy.data.all.MemberName;
import com.threerings.msoy.data.all.Award.AwardType;
import com.threerings.msoy.server.MemberNodeActions;
import com.threerings.msoy.server.persist.MemberRecord;
import com.threerings.msoy.server.persist.UserActionRepository;

import com.threerings.msoy.web.gwt.MemberCard;
import com.threerings.msoy.web.gwt.ServiceCodes;
import com.threerings.msoy.web.gwt.ServiceException;
import com.threerings.msoy.web.server.MemberHelper;
import com.threerings.msoy.web.server.MsoyServiceServlet;
import com.threerings.msoy.web.server.ServletLogic;

import com.threerings.msoy.badge.data.all.Badge;
import com.threerings.msoy.badge.data.all.EarnedBadge;
import com.threerings.msoy.badge.server.persist.BadgeRepository;
import com.threerings.msoy.badge.server.persist.EarnedBadgeRecord;
import com.threerings.msoy.game.data.all.Trophy;
import com.threerings.msoy.game.gwt.GameRating;
import com.threerings.msoy.game.server.persist.MsoyGameRepository;
import com.threerings.msoy.game.server.persist.TrophyRecord;
import com.threerings.msoy.game.server.persist.TrophyRepository;
import com.threerings.msoy.group.gwt.GroupCard;
import com.threerings.msoy.group.server.persist.EarnedMedalRecord;
import com.threerings.msoy.group.server.persist.GroupRepository;
import com.threerings.msoy.group.server.persist.MedalRecord;
import com.threerings.msoy.group.server.persist.MedalRepository;

import com.threerings.msoy.item.server.ItemLogic;
import com.threerings.msoy.item.server.persist.FavoritesRepository;
import com.threerings.msoy.item.server.persist.GameRecord;
import com.threerings.msoy.money.server.MoneyLogic;
import com.threerings.msoy.person.gwt.FeedMessage;
import com.threerings.msoy.person.gwt.Interest;
import com.threerings.msoy.person.server.GalleryLogic;
import com.threerings.msoy.person.server.persist.FeedRepository;
import com.threerings.msoy.person.server.persist.InterestRecord;
import com.threerings.msoy.person.server.persist.ProfileRecord;
import com.threerings.msoy.person.server.persist.ProfileRepository;

import com.threerings.msoy.profile.gwt.Profile;
import com.threerings.msoy.profile.gwt.ProfileService;

import static com.threerings.msoy.Log.log;

/**
 * Provides the server implementation of {@link ProfileService}.
 */
public class ProfileServlet extends MsoyServiceServlet
    implements ProfileService
{
    /**
     * Tests if the supplied member may become a greeter or already is a greeter.
     */
    public static GreeterStatus getGreeterStatus (MemberRecord memrec, int numFriends)
    {
        if (memrec.isGreeter()) {
            return GreeterStatus.GREETER;

        } else if (memrec.isTroublemaker() || memrec.level < MIN_GREETER_LEVEL ||
            numFriends < MIN_GREETER_FRIENDS) {
            return GreeterStatus.DISABLED;

        } else {
            return GreeterStatus.NORMAL;
        }
    }

    // from interface ProfileService
    public ProfileResult loadProfile (final int memberId)
        throws ServiceException
    {
        final MemberRecord memrec = getAuthedUser();
        final MemberRecord tgtrec = _memberRepo.loadMember(memberId);
        if (tgtrec == null) {
            return null;
        }

        final ProfileResult result = new ProfileResult();
        result.name = tgtrec.getName();

        // load profile info
        result.profile = resolveProfileData(memrec, tgtrec);

        // load up the member's interests
        final List<Interest> interests = Lists.newArrayList();
        for (final InterestRecord iRec : _profileRepo.loadInterests(memberId)) {
            interests.add(iRec.toRecord());
        }
        result.interests = interests;

        // load friend info
        result.friends = resolveFriendsData(memrec, tgtrec);
        final IntSet friendIds = _memberRepo.loadFriendIds(tgtrec.memberId);
        result.isOurFriend = (memrec != null) && friendIds.contains(memrec.memberId);
        result.totalFriendCount = friendIds.size();

        // load greeter info
        result.greeterStatus = getGreeterStatus(tgtrec, result.totalFriendCount);

        // load stamp info
        result.stamps = Lists.newArrayList(
            Lists.transform(
                _badgeRepo.loadRecentEarnedBadges(tgtrec.memberId, ProfileResult.MAX_STAMPS),
                EarnedBadgeRecord.TO_BADGE));

        // load medal info
        result.medals = Lists.newArrayList();
        Map<Integer, Award> medals = Maps.newHashMap();
        for (EarnedMedalRecord earnedMedalRec :
                _medalRepo.loadRecentEarnedMedals(memberId, ProfileResult.MAX_STAMPS)) {
            Award medal = new Award();
            medal.whenEarned = earnedMedalRec.whenEarned.getTime();
            medals.put(earnedMedalRec.medalId, medal);
            result.medals.add(medal);
        }
        for (MedalRecord medalRec : _medalRepo.loadMedals(medals.keySet())) {
            Award medal = medals.get(medalRec.medalId);
            medal.name = medalRec.name;
            medal.description = medalRec.description;
            medal.icon = medalRec.createIconMedia();
        }

        // load gallery info
        result.galleries = _galleryLogic.loadGalleries(tgtrec.memberId);

        // load rating and trophy info
        result.trophies = resolveTrophyData(memrec, tgtrec);
        result.ratings = resolveRatingsData(memrec, tgtrec);

        // load group info
        result.groups = resolveGroupsData(memrec, tgtrec);

        // load feed
        result.feed = loadFeed(memberId, DEFAULT_FEED_DAYS);

        // load recent favorites
        result.faves = _itemLogic.resolveFavorites(
            _faveRepo.loadRecentFavorites(memberId, MAX_PROFILE_FAVORITES));

        return result;
    }

    // from interface ProfileService
    public void updateProfile (String displayName, boolean greeter, final Profile profile)
        throws ServiceException
    {
        final MemberRecord memrec = requireAuthedUser();

        if (displayName != null) {
            displayName = displayName.trim();
        }
        if (!MemberName.isValidDisplayName(displayName) ||
                (!memrec.isSupport() && !MemberName.isValidNonSupportName(displayName))) {
            // you'll only see this with a hacked client
            throw new ServiceException(ServiceCodes.E_INTERNAL_ERROR);
        }

        // don't let the user become a greeter if it is disabled
        if (!memrec.isGreeter() && greeter) {
            int friendCount = _memberRepo.loadFriendIds(memrec.memberId).size();
            if (getGreeterStatus(memrec, friendCount) == GreeterStatus.DISABLED) {
                throw new ServiceException(ServiceCodes.E_ACCESS_DENIED);
            }
        }

        // TODO: whatever filtering and profanity checking that we want

        // load their old profile record for "first time configuration" purposes
        final ProfileRecord orec = _profileRepo.loadProfile(memrec.memberId);

        // stuff their updated profile data into the database
        final ProfileRecord nrec = new ProfileRecord(memrec.memberId, profile);
        if (orec != null) {
            nrec.modifications = orec.modifications+1;
            nrec.realName = orec.realName;
        } else {
            log.warning("Account missing old profile [id=" + memrec.memberId + "].");
        }
        _profileRepo.storeProfile(nrec);

        // record that the user updated their profile
        if (nrec.modifications == 1) {
            _moneyLogic.awardCoins(memrec.memberId, CoinAwards.CREATED_PROFILE, true,
                                   UserAction.createdProfile(memrec.memberId));
        } else {
            _userActionRepo.logUserAction(UserAction.updatedProfile(memrec.memberId));
        }
        _eventLog.profileUpdated(memrec.memberId, memrec.visitorId);

        // handle a display name change if necessary
        final boolean nameChanged = memrec.name == null || !memrec.name.equals(displayName);
        final boolean photoChanged = !orec.getPhoto().equals(nrec.getPhoto());
        final boolean statusChanged = orec.headline != nrec.headline;
        final boolean greeterChanged = memrec.isGreeter() != greeter;

        if (nameChanged) {
            _memberRepo.configureDisplayName(memrec.memberId, displayName);
        }

        if (greeterChanged) {
            memrec.setFlag(MemberRecord.Flag.GREETER, greeter);
            _memberRepo.storeFlags(memrec);
        }

        if (statusChanged || nameChanged || photoChanged) {
            // let the world servers know about the info change
            MemberNodeActions.infoChanged(
                memrec.memberId, displayName, nrec.getPhoto(), nrec.headline);
        }

        if (greeterChanged) {
            // let the world servers know about the info change
            MemberNodeActions.tokensChanged(
                memrec.memberId, memrec.toTokenRing());
        }
    }

    // from interface ProfileService
    public void updateInterests (final List<Interest> interests)
        throws ServiceException
    {
        final MemberRecord memrec = requireAuthedUser();
        // store the supplied interests in the repository; blank interests will be deleted
        _profileRepo.storeInterests(memrec.memberId, interests);
    }

    // from interface ProfileService
    public List<MemberCard> findProfiles (final String search)
        throws ServiceException
    {
        final MemberRecord mrec = getAuthedUser();

        // if the caller is a member, load up their friends set
        final IntSet callerFriendIds = (mrec == null) ? null :
            _memberRepo.loadFriendIds(mrec.memberId);

        // locate the members that match the supplied search
        final IntSet mids = new ArrayIntSet();

        // first check for an email match (and use only that if we have a match)
        final MemberRecord memrec = _memberRepo.loadMember(search);
        if (memrec != null) {
            mids.add(memrec.memberId);

        } else {
            // look for a display name match
            mids.addAll(_memberRepo.findMembersByDisplayName(
                            search, false, MAX_PROFILE_MATCHES));
            // look for a real name match
            mids.addAll(_profileRepo.findMembersByRealName(
                            search, MAX_PROFILE_MATCHES));
            // look for an interests match
            mids.addAll(_profileRepo.findMembersByInterest(search, MAX_PROFILE_MATCHES));
        }

        // finally resolve cards for these members
        List<MemberCard> results = _mhelper.resolveMemberCards(mids, false, callerFriendIds);
        Collections.sort(results, MemberHelper.SORT_BY_LAST_ONLINE);
        return results;
    }

    // from interface ProfileService
    public List<FeedMessage> loadSelfFeed (final int profileMemberId, final int cutoffDays)
        throws ServiceException
    {
        return loadFeed(profileMemberId, cutoffDays);
    }

    /**
     * Helper function for {@link #loadSelfFeed} and {@link #loadProfile}.
     */
    protected List<FeedMessage> loadFeed (final int profileMemberId, final int cutoffDays)
    {
        // load up the feed records for the target member
        long since = System.currentTimeMillis() - cutoffDays * 24*60*60*1000L;
        return _servletLogic.resolveFeedMessages(_feedRepo.loadMemberFeed(profileMemberId, since));
    }

    protected Profile resolveProfileData (MemberRecord reqrec, MemberRecord tgtrec)
    {
        ProfileRecord prec = _profileRepo.loadProfile(tgtrec.memberId);
        int forMemberId = (reqrec == null) ? 0 : reqrec.memberId;
        Profile profile = (prec == null) ? new Profile() : prec.toProfile(tgtrec, forMemberId);

        if (profile.award != null && profile.award.type == AwardType.BADGE) {
            EarnedBadgeRecord earnedBadgeRec =
                _badgeRepo.loadEarnedBadge(tgtrec.memberId, profile.award.awardId);
            profile.award.name = Badge.getLevelName(earnedBadgeRec.level);
            profile.award.whenEarned = earnedBadgeRec.whenEarned.getTime();
            profile.award.icon = EarnedBadge.getImageMedia(
                earnedBadgeRec.badgeCode, earnedBadgeRec.level);

        } else if (profile.award != null && profile.award.type == AwardType.MEDAL) {
            EarnedMedalRecord earnedMedalRec =
                _medalRepo.loadEarnedMedal(tgtrec.memberId, profile.award.awardId);
            MedalRecord medalRec = _medalRepo.loadMedal(profile.award.awardId);
            profile.award.whenEarned = earnedMedalRec.whenEarned.getTime();
            profile.award.name = medalRec.name;
            profile.award.icon = medalRec.createIconMedia();
        }

        // TODO: if they're online right now, show that

        return profile;
    }

    protected List<MemberCard> resolveFriendsData (MemberRecord reqrec, MemberRecord tgtrec)
    {
        final Map<Integer,MemberCard> cards = Maps.newLinkedHashMap();
        for (final FriendEntry entry : _memberRepo.loadFriends(
                 tgtrec.memberId, MAX_PROFILE_FRIENDS)) {
            final MemberCard card = new MemberCard();
            card.name = entry.name;
            cards.put(entry.name.getMemberId(), card);
        }
        for (final ProfileRecord profile : _profileRepo.loadProfiles(cards.keySet())) {
            final MemberCard card = cards.get(profile.memberId);
            card.photo = profile.getPhoto();
            card.headline = profile.headline;
        }

        final List<MemberCard> results = Lists.newArrayList();
        results.addAll(cards.values());
        return results;
    }

    protected List<GroupCard> resolveGroupsData (MemberRecord reqrec, MemberRecord tgtrec)
    {
        final boolean showExclusive = (reqrec != null && reqrec.memberId == tgtrec.memberId);
        return _groupRepo.getMemberGroups(tgtrec.memberId, showExclusive);
    }

    protected List<GameRating> resolveRatingsData (MemberRecord reqrec, MemberRecord tgtrec)
    {
        // fetch all the rating records for the user
        List<RatingRecord> ratings = _ratingRepo.getRatings(tgtrec.memberId, -1, MAX_PROFILE_GAMES);

        // sort them by rating
        Collections.sort(ratings, new Comparator<RatingRecord>() {
            public int compare (final RatingRecord o1, final RatingRecord o2) {
                return (o1.rating > o2.rating) ? -1 : (o1.rating == o2.rating) ? 0 : 1;
            }
        });

        // create GameRating records for all the games we know about
        final List<GameRating> result = Lists.newArrayList();
        final IntMap<GameRating> map = IntMaps.newHashIntMap();
        for (final RatingRecord record : ratings) {
            GameRating rrec = map.get(Math.abs(record.gameId));
            if (rrec == null) {
                // stop adding results
                if (result.size() >= MAX_PROFILE_MATCHES) {
                    continue;
                }
                rrec = new GameRating();
                rrec.gameId = Math.abs(record.gameId);
                result.add(rrec);
                map.put(rrec.gameId, rrec);
            }
            if (GameRecord.isDeveloperVersion(record.gameId)) {
                rrec.singleRating = record.rating;
            } else {
                rrec.multiRating = record.rating;
            }
        }

        // now load up and fill in the game details
        for (final IntMap.IntEntry<GameRating> entry : map.intEntrySet()) {
            final int gameId = entry.getIntKey();
            final GameRecord record = _mgameRepo.loadGameRecord(gameId);
            if (record == null) {
                log.info("Player has rating for non-existent game [id=" + gameId + "].");
                result.remove(entry.getValue());
            } else {
                entry.getValue().gameName = record.name;
                entry.getValue().gameThumb = record.getThumbMediaDesc();
            }
        }

        return result;
    }

    protected List<Trophy> resolveTrophyData (final MemberRecord reqrec, final MemberRecord tgtrec)
    {
        final List<Trophy> list = Lists.newArrayList();
        for (final TrophyRecord record :
                 _trophyRepo.loadRecentTrophies(tgtrec.memberId, MAX_PROFILE_TROPHIES)) {
            list.add(record.toTrophy());
        }
        return list;
    }

    // our dependencies
    @Inject protected ServletLogic _servletLogic;
    @Inject protected ItemLogic _itemLogic;
    @Inject protected MoneyLogic _moneyLogic;
    @Inject protected GalleryLogic _galleryLogic;
    @Inject protected FeedRepository _feedRepo;
    @Inject protected GroupRepository _groupRepo;
    @Inject protected MedalRepository _medalRepo;
    @Inject protected ProfileRepository _profileRepo;
    @Inject protected MsoyGameRepository _mgameRepo;
    @Inject protected RatingRepository _ratingRepo;
    @Inject protected TrophyRepository _trophyRepo;
    @Inject protected UserActionRepository _userActionRepo;
    @Inject protected BadgeRepository _badgeRepo;
    @Inject protected FavoritesRepository _faveRepo;

    protected static final int MAX_PROFILE_MATCHES = 100;
    protected static final int MAX_PROFILE_FRIENDS = 6;
    protected static final int MAX_PROFILE_GAMES = 10;
    protected static final int MAX_PROFILE_TROPHIES = 6;
    protected static final int MAX_PROFILE_FAVORITES = 4;
    protected static final int MIN_GREETER_LEVEL = DeploymentConfig.devDeployment ? 5 : 10;
    protected static final int MIN_GREETER_FRIENDS = DeploymentConfig.devDeployment ? 3 : 20;

    protected static final int DEFAULT_FEED_DAYS = 2;
}
