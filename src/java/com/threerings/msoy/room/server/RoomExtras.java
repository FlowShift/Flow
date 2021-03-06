//
// $Id$

package com.threerings.msoy.room.server;

import java.util.List;

import com.threerings.msoy.item.data.all.Audio;
import com.threerings.msoy.room.server.persist.MemoriesRecord;

/**
 * A class to hold additional scene data resolved in MsoySceneRepository.
 */
public class RoomExtras
{
    /** The startup memory records for the furni in this room. */
    public List<MemoriesRecord> memories;

    /** The startup playlist for the room. */
    public List<Audio> playlist;
}
