//
// $Id$

package com.threerings.msoy.item.gwt;

import java.util.Map;

import com.google.common.collect.Maps;

import com.threerings.msoy.item.data.all.MsoyItemType;
import com.threerings.msoy.money.data.all.Currency;

/**
 * Utility routines relating to item pricing.
 */
public class ItemPrices
{
    /** The default minimum item price. */
    public static final int DEFAULT_MIN_PRICE = 10;

    /**
     * Returns the minimum pricing for an item of the specified type with the specified rating.
     */
    public static int getMinimumPrice (Currency currency, MsoyItemType itemType, byte rating)
    {
        switch (currency) {
        case BARS:
            return 1;
        case COINS:
            rating = (byte)Math.min(4, Math.max(0, rating-1));
            int[] prices = MIN_PRICES.get(itemType);
            return (prices == null) ? DEFAULT_MIN_PRICE : prices[rating];
        default:
            throw new IllegalArgumentException("Can't compute minimum price for " + currency + ".");
        }
    }

    protected static final Map<MsoyItemType, int[]> MIN_PRICES = Maps.newHashMap();
    static {
        MIN_PRICES.put(MsoyItemType.AVATAR, new int[] { 100, 200, 500, 1500, 5000 });
        MIN_PRICES.put(MsoyItemType.FURNITURE, new int[] { 10, 50, 100, 500, 1000 });
        MIN_PRICES.put(MsoyItemType.DECOR, new int[] { 20, 100, 200, 1000, 2000 });
        MIN_PRICES.put(MsoyItemType.TOY, new int[] { 200, 300, 700, 2500, 5000 });
        MIN_PRICES.put(MsoyItemType.PET, new int[] { 100, 200, 500, 1500, 5000 });
        MIN_PRICES.put(MsoyItemType.AVATAR, new int[] { 100, 200, 500, 1500, 5000 });
        MIN_PRICES.put(MsoyItemType.PHOTO, new int[] { 10, 50, 100, 500, 1000 });
        MIN_PRICES.put(MsoyItemType.VIDEO, new int[] { 10, 50, 100, 500, 1000 });
        MIN_PRICES.put(MsoyItemType.AUDIO, new int[] { 10, 50, 100, 500, 1000 });
        MIN_PRICES.put(MsoyItemType.DOCUMENT, new int[] { 10, 50, 100, 500, 1000 });
        MIN_PRICES.put(MsoyItemType.LEVEL_PACK, new int[] { 200, 300, 700, 2500, 5000 });
        MIN_PRICES.put(MsoyItemType.ITEM_PACK, new int[] { 200, 300, 700, 2500, 5000 });
        // these use the default of 10 TROPHY_SOURCE, PRIZE, PROP
    }
}
