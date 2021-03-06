//
// $Id$

package com.threerings.msoy.person.server.persist;

import java.sql.Timestamp;
import java.util.Map;

import com.samskivert.depot.Key;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.expression.ColumnExp;

import com.threerings.msoy.data.all.GroupName;
import com.threerings.msoy.data.all.MemberName;
import com.threerings.msoy.person.gwt.FeedMessage;

/**
 * Contains persistent data for a global feed message.
 */
@Entity
public class GlobalFeedMessageRecord extends FeedMessageRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<GlobalFeedMessageRecord> _R = GlobalFeedMessageRecord.class;
    public static final ColumnExp<Integer> MESSAGE_ID = colexp(_R, "messageId");
    public static final ColumnExp<Integer> TYPE = colexp(_R, "type");
    public static final ColumnExp<String> DATA = colexp(_R, "data");
    public static final ColumnExp<Timestamp> POSTED = colexp(_R, "posted");
    // AUTO-GENERATED: FIELDS END

    /** Increment this value if you modify the definition of this persistent object in a way that
     * will result in a change to its SQL counterpart. */
    public static final int SCHEMA_VERSION = 2;

    @Override // from FeedMessageRecord
    public FeedMessage toMessage (
        Map<Integer, MemberName> memberNames, Map<Integer, GroupName> groupNames)
    {
        return new FeedMessage(getType(), getData(), getPosted());
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link GlobalFeedMessageRecord}
     * with the supplied key values.
     */
    public static Key<GlobalFeedMessageRecord> getKey (int messageId)
    {
        return newKey(_R, messageId);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(MESSAGE_ID); }
    // AUTO-GENERATED: METHODS END
}
