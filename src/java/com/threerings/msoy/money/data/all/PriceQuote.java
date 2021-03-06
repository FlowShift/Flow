//
// $Id$

package com.threerings.msoy.money.data.all;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.msoy.money.data.all.Currency;

/**
 * Contains secured prices when a member views an item. This can be cached and identified by a
 * PriceKey.
 *
 * @author Kyle Sampson <kyle@threerings.net>
 * @author Ray Greenwell <ray@threerings.net>
 */
public class PriceQuote extends SimpleStreamableObject
    implements Serializable, IsSerializable
{
    public PriceQuote (
        Currency listedCurrency, int coins, int bars, int coinChangeForBars,
        float rate, int centsPerBar)
    {
        _listedCurrency = listedCurrency;
        _coins = coins;
        _bars = bars;
        _coinChange = coinChangeForBars;
        _rate = rate;
        _centsPerBar = centsPerBar;
    }

    /** For serialization. */
    public PriceQuote () { }

    /**
     * Is the specified purchase valid?
     */
    public boolean isPurchaseValid (
        Currency buyCurrency, int authorizedAmount, float currentExchangeRate)
    {
        // the current exchange rate can't vary too much..
        if (buyCurrency != _listedCurrency) {
            float variance = _rate / currentExchangeRate;
            if ((variance > (1 + MAX_RATE_VARIANCE)) || (variance < (1 - MAX_RATE_VARIANCE))) {
                return false;
            }
        }

        // otherwise, as long as they have authorized enough dough...
        int amount = getAmount(buyCurrency);
        return (amount >= 0) && (amount <= authorizedAmount);
    }

    /**
     * Get the currency with which this item was listed.
     */
    public Currency getListedCurrency ()
    {
        return _listedCurrency;
    }

    /**
     * Get the listed amount, be it coins or bars.
     */
    public int getListedAmount ()
    {
        return getAmount(_listedCurrency);
    }

    /**
     * Get the amount of this quote in the specified currency.
     */
    public int getAmount (Currency currency)
    {
        return (currency == Currency.BARS) ? _bars : _coins;
    }

    public int getCoins ()
    {
        return _coins;
    }

    public int getBars ()
    {
        return _bars;
    }

    /**
     * Retrieve the amount of change, in coins, when bar-buying a coin-listed item.
     * If the listedType is BARS, this will always be 0.
     */
    public int getCoinChange ()
    {
        return _coinChange;
    }

    /**
     * Get the raw exchange rate at the time the quote was generated.
     */
    public float getExchangeRate ()
    {
        return _rate;
    }

    /**
     * Return the current cost in cents for a single bar.
     */
    public int getCentsPerBar ()
    {
        return _centsPerBar;
    }

    /* For debugging */
    public String toString ()
    {
        return "PriceQuote[" + _listedCurrency + "-listed]: " +
            _coins + "coins, " + _bars + "bars.";
    }

    protected Currency _listedCurrency;
    protected int _coins;
    protected int _bars;
    protected int _coinChange;
    protected float _rate;
    protected int _centsPerBar;

    /** The maximum amount the exchange rate can vary at purchase time. */
    protected static final float MAX_RATE_VARIANCE = .05f;
}
