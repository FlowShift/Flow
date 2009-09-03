//
// $Id$

package com.threerings.msoy.money.data.all;

import java.util.EnumSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.IsSerializable;

/** The format of a transaction report. */
public enum ReportType
    implements IsSerializable
{
    COINS(1, null, Currency.COINS, Currency.COINS.getLargeIcon()),
    BARS(2, null, Currency.BARS, Currency.BARS.getLargeIcon()),
    BLING(3, null, Currency.BLING, Currency.BLING.getLargeIcon()),
    CREATOR(4, EnumSet.of(TransactionType.CREATOR_PAYOUT), null, "/images/profile/browseitems.png"),
    ;

    public int toIndex ()
    {
        return _index;
    }

    public static ReportType fromIndex (int index)
    {
        for (ReportType r : values()) {
            if (r.toIndex() == index) {
                return r;
            }
        }
        return COINS; // Fall back to default
    }

    private ReportType (
        int index, Set<TransactionType> transactions, Currency currency, String icon)
    {
        _index = index;
        this.transactions = transactions;
        this.currency = currency;
        this.icon = icon;
    }

    public transient Set<TransactionType> transactions;
    public transient Currency currency;
    public transient String icon;

    protected transient int _index;
}
