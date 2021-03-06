//
// $Id$

package com.threerings.msoy.room.server.persist;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.StringUtil;

import com.samskivert.depot.CountRecord;
import com.samskivert.depot.DataMigration;
import com.samskivert.depot.DatabaseException;
import com.samskivert.depot.DepotRepository;
import com.samskivert.depot.Exps;
import com.samskivert.depot.Funcs;
import com.samskivert.depot.Key;
import com.samskivert.depot.KeySet;
import com.samskivert.depot.Ops;
import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.SchemaMigration;
import com.samskivert.depot.clause.FromOverride;
import com.samskivert.depot.clause.Limit;
import com.samskivert.depot.clause.OrderBy;
import com.samskivert.depot.clause.QueryClause;
import com.samskivert.depot.clause.Where;
import com.samskivert.depot.expression.ColumnExp;
import com.samskivert.depot.expression.FluentExp;
import com.samskivert.depot.expression.SQLExpression;

import com.threerings.presents.annotation.BlockingThread;

import com.threerings.whirled.data.SceneUpdate;

import com.threerings.orth.data.MediaDesc;

import com.threerings.msoy.room.data.FurniData;
import com.threerings.msoy.room.data.FurniUpdate;
import com.threerings.msoy.room.data.MsoyLocation;
import com.threerings.msoy.room.data.MsoySceneModel;
import com.threerings.msoy.room.data.SceneAttrsUpdate;
import com.threerings.msoy.room.data.SceneOwnershipUpdate;
import com.threerings.msoy.server.MediaDescFactory;
import com.threerings.msoy.server.persist.HotnessConfig;
import com.threerings.msoy.server.persist.RatingRecord;
import com.threerings.msoy.server.persist.RatingRepository;

import static com.threerings.msoy.Log.log;

/**
 * Provides scene storage services for the msoy server.
 */
@Singleton @BlockingThread
public class MsoySceneRepository extends DepotRepository
{
    @Inject public MsoySceneRepository (PersistenceContext ctx)
    {
        super(ctx);

        _ratingRepo = new RatingRepository(
            ctx, SceneRecord.SCENE_ID, SceneRecord.RATING_SUM, SceneRecord.RATING_COUNT) {
            @Override
            protected Class<? extends PersistentRecord> getTargetClass () {
                return SceneRecord.class;
            }
            @Override
            protected Class<RatingRecord> getRatingClass () {
                return coerceRating(SceneRatingRecord.class);
            }
        };

        ctx.registerMigration(SceneRecord.class, new SchemaMigration.Drop(12, "audioId"));
        ctx.registerMigration(SceneRecord.class, new SchemaMigration.Drop(12, "audioMediaHash"));
        ctx.registerMigration(SceneRecord.class, new SchemaMigration.Drop(12, "audioMediaType"));
        ctx.registerMigration(SceneRecord.class, new SchemaMigration.Drop(12, "audioVolume"));

        registerMigration(new DataMigration("2010-02-24 room_null_property_cleanup") {
            @Override public void invoke () throws DatabaseException {
                int count;

                count = deleteAll(RoomPropertyRecord.class, new Where(Exps.literal(
                    "encode(\"value\", 'hex') = '0000'")));
                log.info("Deleted null RoomPropertyRecord properties", "count", count);
            }
        });
    }

    /**
     * Returns the total number of scenes in the repository.
     */
    public int getSceneCount ()
    {
        return load(CountRecord.class, new FromOverride(SceneRecord.class)).count;
    }

    /**
     * Returns the number of rooms owned by the specified member.
     */
    public int getRoomCount (int memberId)
    {
        Where where = new Where(
            Ops.and(SceneRecord.OWNER_TYPE.eq(MsoySceneModel.OWNER_TYPE_MEMBER),
                    SceneRecord.OWNER_ID.eq(memberId)));
        return load(CountRecord.class, new FromOverride(SceneRecord.class), where).count;
    }

    /**
     * Retrieve a list of all the scenes that the user owns.
     */
    public List<SceneRecord> getOwnedScenes (byte ownerType, int memberId)
    {
        Where where = new Where(Ops.and(SceneRecord.OWNER_TYPE.eq(ownerType),
                                        SceneRecord.OWNER_ID.eq(memberId)));
        return findAll(SceneRecord.class, where);
    }

    /**
     * Retrieve a list of all the member scenes that the user directly owns.
     */
    public List<SceneRecord> getOwnedScenes (int memberId)
    {
        return getOwnedScenes(MsoySceneModel.OWNER_TYPE_MEMBER, memberId);
    }

    public RatingRepository getRatingRepository ()
    {
        return _ratingRepo;
    }

    /**
     * Return the scene name for the specified id, or null (no exception) if the scene
     * doesn't exist.
     */
    public String identifyScene (int sceneId)
    {
        // TODO: use a @Computed record?
        SceneRecord scene = load(SceneRecord.class, SceneRecord.getKey(sceneId));
        return (scene == null) ? null : scene.name;
    }

    /**
     * Publishes a scene, marking it as a Whirled Tourist trap.
     */
    public void publishScene (int sceneId)
    {
        updatePartial(SceneRecord.getKey(sceneId),
            SceneRecord.LAST_PUBLISHED, new Timestamp(System.currentTimeMillis()));
    }

    /**
     * Given a list of scene ids, return a map containing the current names, indexed by scene id.
     */
    public Map<Integer, String> identifyScenes (Set<Integer> sceneIds)
    {
        Map<Integer, String> names = Maps.newHashMap();
        // TODO: use a @Computed record?
        for (SceneRecord scene : loadAll(SceneRecord.class, sceneIds)) {
            names.put(scene.sceneId, scene.name);
        }
        return names;
    }

    /**
     * Loads all of the furni records for the specified scene.
     */
    public List<SceneFurniRecord> loadFurni (int sceneId)
    {
        // load up all of our furni data, specifically using a safe caching strategy (TODO: do we
        // need to be skipping the cache here? we're smarter about collection caching...)
        return findAll(SceneFurniRecord.class, CacheStrategy.RECORDS,
                       new Where(SceneFurniRecord.SCENE_ID, sceneId));
    }

    /**
     * Loads all of the furni records in the given scenes of the given type.
     */
    public List<SceneFurniRecord> loadFurni (byte itemType, Collection<Integer> scenes)
    {
        return findAll(SceneFurniRecord.class, new Where(Ops.and(
            SceneFurniRecord.ITEM_TYPE.eq(itemType),
            SceneFurniRecord.SCENE_ID.in(scenes))));
    }

    /**
     * Load the SceneRecord for the given sceneId
     */
    public SceneRecord loadScene (int sceneId)
    {
        return load(SceneRecord.class, SceneRecord.getKey(sceneId));
    }

    /**
     * Load the SceneRecords for the given sceneIds.
     */
    public List<SceneRecord> loadScenes (Collection<Integer> sceneIds)
    {
        return loadAll(SceneRecord.class, sceneIds);
    }

    /**
     * Loads "new and hot" rooms...
     */
    public List<SceneRecord> loadScenes (int offset, int rows)
    {
        List<QueryClause> clauses = Lists.newArrayList();
        clauses.add(new Limit(offset, rows));

        List<SQLExpression<?>> exprs = Lists.newArrayList();
        List<OrderBy.Order> orders = Lists.newArrayList();

        // only load public, published rooms
        clauses.add(new Where(Ops.and(
                                  Ops.not(SceneRecord.LAST_PUBLISHED.isNull()),
                                  SceneRecord.ACCESS_CONTROL.eq(MsoySceneModel.ACCESS_EVERYONE))));

        exprs.add(NEW_AND_HOT_ORDER);
        orders.add(OrderBy.Order.DESC);

        clauses.add(new OrderBy(exprs.toArray(new SQLExpression<?>[exprs.size()]),
                                orders.toArray(new OrderBy.Order[orders.size()])));

        return findAll(SceneRecord.class, clauses);
    }

    /**
     * Returns the canonical snapshot image for the specified scene or null if it has none.
     */
    public MediaDesc loadSceneSnapshot (int sceneId)
    {
        SceneRecord scene = loadScene(sceneId);
        return (scene == null) ? null : scene.getSnapshotFull();
    }

    /**
     * Insert just a new scene record.
     */
    public void insertScene (SceneRecord record)
    {
        insert(record);
    }

    /**
     * Insert a new scene, with furni and all, into the database and return the newly assigned
     * sceneId.
     */
    public SceneRecord insertScene (MsoySceneModel model)
    {
        SceneRecord scene = new SceneRecord(model);
        insert(scene);
        for (FurniData data : model.furnis) {
            insert(new SceneFurniRecord(scene.sceneId, data));
        }
        return scene;
    }

    public void insertFurni (SceneFurniRecord record)
    {
        insert(record);
    }

    /**
     * Saves the specified update to the database.
     */
    public void persistUpdate (SceneUpdate update)
    {
        int finalVersion = update.getSceneVersion() + update.getVersionIncrement();
        persistUpdates(Collections.singleton(update), finalVersion);
    }

    /**
     * Saves the provided set of updates to the database. Errors applying any of the individual
     * updates are caught and logged so that one update application failure does not prevent
     * subsequent updates from failing.
     */
    public void persistUpdates (Iterable<? extends SceneUpdate> updates, int finalVersion)
    {
        int sceneId = 0;
        for (SceneUpdate update : updates) {
            sceneId = update.getSceneId();
            try {
                applyUpdate(update);
            } catch (Exception e) {
                log.warning("Failed to apply scene update " + update +
                        " from " + StringUtil.toString(updates) + ".", e);
            }
        }
        if (sceneId != 0) {
            try {
                updatePartial(SceneRecord.getKey(sceneId), SceneRecord.VERSION, finalVersion);
            } catch (Exception e) {
                log.warning("Failed to update scene to final version", "id", sceneId,
                        "fvers", finalVersion, e);
            }
        }
    }

    /**
     * Applies an update that adds, removes or changes furni.
     */
    protected void applyUpdate (SceneUpdate update)
    {
        if (update instanceof FurniUpdate.Add) {
            insert(new SceneFurniRecord(update.getSceneId(), ((FurniUpdate)update).data));

        } else if (update instanceof FurniUpdate.Change) {
            update(new SceneFurniRecord(update.getSceneId(), ((FurniUpdate)update).data));

        } else if (update instanceof FurniUpdate.Remove) {
            delete(SceneFurniRecord.getKey(update.getSceneId(), ((FurniUpdate)update).data.id));

        } else if (update instanceof SceneAttrsUpdate) {
            SceneAttrsUpdate scup = (SceneAttrsUpdate)update;
            updatePartial(SceneRecord.getKey(update.getSceneId()),
                          SceneRecord.NAME, scup.name,
                          SceneRecord.ACCESS_CONTROL, scup.accessControl,
                          SceneRecord.PLAYLIST_CONTROL, scup.playlistControl,
                          SceneRecord.DECOR_ID, scup.decor.itemId,
                          SceneRecord.ENTRANCE_X, scup.entrance.x,
                          SceneRecord.ENTRANCE_Y, scup.entrance.y,
                          SceneRecord.ENTRANCE_Z, scup.entrance.z,
                          SceneRecord.BACKGROUND_COLOR, scup.backgroundColor);

            int removeFlags = scup.noPuppet ? 0 : SceneRecord.Flag.SUPPRESS_PUPPET.getMask();
            if (removeFlags != 0) {
                updatePartial(SceneRecord.getKey(update.getSceneId()),
                    SceneRecord.FLAGS, SceneRecord.FLAGS.bitAnd(~removeFlags));
            }

            int addFlags = scup.noPuppet ? SceneRecord.Flag.SUPPRESS_PUPPET.getMask() : 0;
            if (addFlags != 0) {
                updatePartial(SceneRecord.getKey(update.getSceneId()),
                    SceneRecord.FLAGS, SceneRecord.FLAGS.bitOr(addFlags));
            }

        } else if (update instanceof SceneOwnershipUpdate) {
            SceneOwnershipUpdate sou = (SceneOwnershipUpdate)update;
            Map<ColumnExp<?>,Object> updates = Maps.newHashMap();
            updates.put(SceneRecord.OWNER_TYPE, sou.ownerType);
            updates.put(SceneRecord.OWNER_ID, sou.ownerId);
            if (sou.lockToOwner) {
                updates.put(SceneRecord.ACCESS_CONTROL, MsoySceneModel.ACCESS_OWNER_ONLY);
            }
            updatePartial(SceneRecord.getKey(update.getSceneId()), updates);

        } else {
            log.warning("Unable to apply unknown furni update", "class", update.getClass(),
                        "update", update);
        }
    }

    /** Loads the room properties. */
    public List<RoomPropertyRecord> loadProperties (int ownerId, int sceneId)
    {
        return findAll(RoomPropertyRecord.class, new Where(
            RoomPropertyRecord.OWNER_ID, ownerId,
            RoomPropertyRecord.SCENE_ID, sceneId));
    }

    /** Saves a room property, deleting if the value is null. */
    public void storeProperty (RoomPropertyRecord record)
    {
        if (record.value == null) {
            delete(record);
        } else {
            store(record);
        }
    }

    public void setCanonicalImage (int sceneId, byte[] canonicalHash, byte canonicalType,
        byte[] thumbnailHash, byte thumbnailType)
    {
        updatePartial(SceneRecord.getKey(sceneId),
            SceneRecord.CANONICAL_IMAGE_HASH, canonicalHash,
            SceneRecord.CANONICAL_IMAGE_TYPE, canonicalType,
            SceneRecord.THUMBNAIL_HASH, thumbnailHash,
            SceneRecord.THUMBNAIL_TYPE, thumbnailType);
    }

    /**
     * Deletes all data associated with the supplied members. This is done as a part of purging
     * member accounts.
     */
    public void purgeMembers (Collection<Integer> memberIds)
    {
        // delete all scenes owned by these members
        List<Key<SceneRecord>> skeys = findAllKeys(
            SceneRecord.class, false, new Where(SceneRecord.OWNER_ID.in(memberIds)));
        if (!skeys.isEmpty()) {
            deleteAll(SceneRecord.class, KeySet.newKeySet(SceneRecord.class, skeys));
            // delete all furni from all of those scenes
            List<Integer> scids = Lists.transform(skeys, Key.<SceneRecord>toInt());
            deleteAll(SceneFurniRecord.class, new Where(SceneFurniRecord.SCENE_ID.in(scids)));
        }
        // delete all scene ratings by these members
        _ratingRepo.purgeMembers(memberIds);
    }

    /**
     * Link the given scene with the given theme. Returns true if a room was stamped.
     */
    public boolean stampRoom (int sceneId, int groupId)
    {
        return 1 == updatePartial(SceneRecord.getKey(sceneId), ImmutableMap.of(
            SceneRecord.THEME_GROUP_ID, groupId));
    }

    /**
     * Temporary function for a migration in {@link LauncherRepository}. Sets the actionType
     * of a specific itemId in the given scene.
     */
    public int updateActionType (int sceneId, int itemId, int actionType)
    {
        return updatePartial(SceneFurniRecord.class,
            new Where(SceneFurniRecord.SCENE_ID, sceneId,
                      SceneFurniRecord.ITEM_ID, itemId),
            null,
            SceneFurniRecord.ACTION_TYPE, actionType);
    }

    protected void checkCreateStockScene (SceneRecord.Stock stock)
    {
        // if it's already created, we're good to go
        if (load(SceneRecord.class, SceneRecord.getKey(stock.getSceneId())) != null) {
            return;
        }

        MsoySceneModel model = MsoySceneModel.blankMsoySceneModel();
        model.sceneId = stock.getSceneId();
        model.version = 1;
        model.name = stock.getName();

        if (stock == SceneRecord.Stock.PUBLIC_ROOM) {
            // set it up to be owned by group 1
            model.ownerType = MsoySceneModel.OWNER_TYPE_GROUP;
            model.ownerId = 1;

        } else {
            // add a door to the PUBLIC_ROOM
            FurniData f = new FurniData();
            f.id = 1;
            f.media = MediaDescFactory
                .createMediaDesc("e8b660ec5aa0aa30dab46b267daf3b80996269e7.swf");
            f.loc = new MsoyLocation(1, 0, 0.5, 0);
            f.scaleX = 1.4f;
            f.actionType = FurniData.ACTION_PORTAL;
            f.actionData = SceneRecord.Stock.PUBLIC_ROOM.getSceneId() + ":" +
                SceneRecord.Stock.PUBLIC_ROOM.getName();
            model.addFurni(f);
        }

        log.info("Creating stock scene " + stock + ".");
        insertScene(model);
    }

    @Override // from DepotRepository
    protected void init ()
    {
        super.init();

        // create our stock scenes if they are not yet created
        for (SceneRecord.Stock stock : SceneRecord.Stock.values()) {
            checkCreateStockScene(stock);
        }
    }

    protected static FluentExp<? extends Number> getRatingExpression ()
    {
        // TODO: PostgreSQL flips out when you CREATE INDEX using a prepared statement
        // TODO: with parameters. So we trick Depot using a literal expression here. :/
        return SceneRecord.RATING_SUM.div(
            Funcs.greatest(SceneRecord.RATING_COUNT, Exps.<Number>literal("1.0")));
    }

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(SceneRecord.class);
        classes.add(SceneFurniRecord.class);
        classes.add(RoomPropertyRecord.class);
        classes.add(SceneRatingRecord.class);
    }

    protected RatingRepository _ratingRepo;

    /** Order for New & Hot. If you change this, also migrate the {@link SceneRecord} index. */
    protected static final SQLExpression<Number> NEW_AND_HOT_ORDER =
        getRatingExpression().plus(
            // TODO: PostgreSQL flips out when you CREATE INDEX
            // using a prepared statement with parameters. So we
            // trick Depot using a literal expression here. This is PG only! :/
            Exps.<Number>literal("date_part('epoch', \"lastPublished\")/" +
                                 HotnessConfig.DROPOFF_SECONDS));
}
