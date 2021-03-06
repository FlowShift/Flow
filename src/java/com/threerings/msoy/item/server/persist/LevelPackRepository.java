//
// $Id$

package com.threerings.msoy.item.server.persist;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.annotation.Entity;

import com.threerings.msoy.server.persist.RatingRecord;
import com.threerings.msoy.server.persist.RatingRepository;
import com.threerings.msoy.server.persist.TagHistoryRecord;
import com.threerings.msoy.server.persist.TagRecord;

/**
 * Manages the persistent store of {@link LevelPackRecord} items.
 */
@Singleton
public class LevelPackRepository extends ItemRepository<LevelPackRecord>
{
    @Entity(name="LevelPackMogMarkRecord")
    public static class LevelPackMogMarkRecord extends MogMarkRecord
    {
    }

    @Entity(name="LevelPackTagRecord")
    public static class LevelPackTagRecord extends TagRecord
    {
    }

    @Entity(name="LevelPackTagHistoryRecord")
    public static class LevelPackTagHistoryRecord extends TagHistoryRecord
    {
    }

    @Inject public LevelPackRepository (PersistenceContext ctx)
    {
        super(ctx);
    }

    @Override
    protected Class<LevelPackRecord> getItemClass ()
    {
        return LevelPackRecord.class;
    }

    @Override
    protected Class<CatalogRecord> getCatalogClass ()
    {
        return coerceCatalog(LevelPackCatalogRecord.class);
    }

    @Override
    protected Class<CloneRecord> getCloneClass ()
    {
        return coerceClone(LevelPackCloneRecord.class);
    }

    @Override
    protected Class<RatingRecord> getRatingClass ()
    {
        return RatingRepository.coerceRating(LevelPackRatingRecord.class);
    }

    @Override
    protected MogMarkRecord createMogMarkRecord ()
    {
        return new LevelPackMogMarkRecord();
    }

    @Override
    protected TagRecord createTagRecord ()
    {
        return new LevelPackTagRecord();
    }

    @Override
    protected TagHistoryRecord createTagHistoryRecord ()
    {
        return new LevelPackTagHistoryRecord();
    }
}
