//
// $Id$

package com.threerings.msoy.item.server.persist;

import java.sql.Timestamp;

import com.samskivert.depot.Key;
import com.samskivert.depot.annotation.TableGenerator;
import com.samskivert.depot.expression.ColumnExp;

import com.threerings.msoy.item.data.all.Item;
import com.threerings.msoy.item.data.all.MsoyItemType;
import com.threerings.msoy.item.data.all.Prize;

/**
 * Contains the persistent data for a Prize item.
 */
@TableGenerator(name="itemId", pkColumnValue="PRIZE")
public class PrizeRecord extends IdentGameItemRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<PrizeRecord> _R = PrizeRecord.class;
    public static final ColumnExp<MsoyItemType> TARGET_TYPE = colexp(_R, "targetType");
    public static final ColumnExp<Integer> TARGET_CATALOG_ID = colexp(_R, "targetCatalogId");
    public static final ColumnExp<String> IDENT = colexp(_R, "ident");
    public static final ColumnExp<Integer> GAME_ID = colexp(_R, "gameId");
    public static final ColumnExp<Integer> ITEM_ID = colexp(_R, "itemId");
    public static final ColumnExp<Integer> SOURCE_ID = colexp(_R, "sourceId");
    public static final ColumnExp<Integer> CREATOR_ID = colexp(_R, "creatorId");
    public static final ColumnExp<Integer> OWNER_ID = colexp(_R, "ownerId");
    public static final ColumnExp<Integer> CATALOG_ID = colexp(_R, "catalogId");
    public static final ColumnExp<Integer> RATING_SUM = colexp(_R, "ratingSum");
    public static final ColumnExp<Integer> RATING_COUNT = colexp(_R, "ratingCount");
    public static final ColumnExp<Item.UsedAs> USED = colexp(_R, "used");
    public static final ColumnExp<Integer> LOCATION = colexp(_R, "location");
    public static final ColumnExp<Timestamp> LAST_TOUCHED = colexp(_R, "lastTouched");
    public static final ColumnExp<String> NAME = colexp(_R, "name");
    public static final ColumnExp<String> DESCRIPTION = colexp(_R, "description");
    public static final ColumnExp<Boolean> MATURE = colexp(_R, "mature");
    public static final ColumnExp<byte[]> THUMB_MEDIA_HASH = colexp(_R, "thumbMediaHash");
    public static final ColumnExp<Byte> THUMB_MIME_TYPE = colexp(_R, "thumbMimeType");
    public static final ColumnExp<Byte> THUMB_CONSTRAINT = colexp(_R, "thumbConstraint");
    public static final ColumnExp<byte[]> FURNI_MEDIA_HASH = colexp(_R, "furniMediaHash");
    public static final ColumnExp<Byte> FURNI_MIME_TYPE = colexp(_R, "furniMimeType");
    public static final ColumnExp<Byte> FURNI_CONSTRAINT = colexp(_R, "furniConstraint");
    // AUTO-GENERATED: FIELDS END

    /** Update this version if you change fields specific to this derived class. */
    public static final int ITEM_VERSION = 2;

    /** This combines {@link #ITEM_VERSION} with {@link #BASE_SCHEMA_VERSION} to create a version
     * that allows us to make ItemRecord-wide changes and specific derived class changes. */
    public static final int SCHEMA_VERSION = ITEM_VERSION + BASE_SCHEMA_VERSION * BASE_MULTIPLIER;

    /** The item type of the target prize item. */
    public MsoyItemType targetType;

    /** The catalog id of the target prize item's listing. */
    public int targetCatalogId;

    @Override // from ItemRecord
    public MsoyItemType getType ()
    {
        return MsoyItemType.PRIZE;
    }

    @Override // from ItemRecord
    public void fromItem (Item item)
    {
        super.fromItem(item);

        Prize source = (Prize)item;
        targetType = source.targetType;
        targetCatalogId = source.targetCatalogId;
    }

    @Override // from ItemRecord
    protected Item createItem ()
    {
        Prize object = new Prize();
        object.targetType = targetType;
        object.targetCatalogId = targetCatalogId;
        return object;
    }

    @Override // from GameItemRecord
    public boolean isListingOutOfDate (GameItemRecord master)
    {
        PrizeRecord pmaster = ((PrizeRecord)master);
        return super.isListingOutOfDate(master) ||
            targetType != pmaster.targetType || targetCatalogId != pmaster.targetCatalogId;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link PrizeRecord}
     * with the supplied key values.
     */
    public static Key<PrizeRecord> getKey (int itemId)
    {
        return newKey(_R, itemId);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(ITEM_ID); }
    // AUTO-GENERATED: METHODS END
}
