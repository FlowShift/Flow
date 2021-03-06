//
// $Id$

package com.threerings.msoy.game.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.Cloneable;
import com.threerings.util.Equalable;

import com.threerings.orth.data.MediaDesc;

/**
 * Contains metadata about a game for which a player is currently matchmaking.
 */
public class GameSummary extends SimpleStreamableObject
    implements Cloneable
{
    /** The game id. This will be negative if the summary is for the dev version. */
    public var gameId :int;

    /** The name of the game - used as a tooltip */
    public var name :String;

    /** The description of the game - used for sharing */
    public var description :String;

    /** Whether or not this is an AVRGame. */
    public var avrGame :Boolean;

    /** The thumbnail media for the game we're summarizing. */
    public var thumbMedia :MediaDesc;

    /** The member id of the creator of the game. */
    public var creatorId :int;

    public function GameSummary ()
    {
        // only used for unserialization
    }

    // documentation from Cloneable
    public function clone () :Object
    {
        var data :GameSummary = new GameSummary();
        data.gameId = this.gameId;
        data.name = this.name;
        data.description = this.description;
        data.avrGame = this.avrGame;
        data.thumbMedia = this.thumbMedia;
        data.creatorId = this.creatorId;
        return data;
    }

    // documentation from Equalable
    public function equals (other :Object) :Boolean
    {
        if (other is GameSummary) {
            var data :GameSummary = other as GameSummary;
            return data.gameId == this.gameId;
        }
        return false;
    }

    // documentation from Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        gameId = ins.readInt();
        name = (ins.readField(String) as String);
        description = (ins.readField(String) as String);
        avrGame = ins.readBoolean();
        thumbMedia = MediaDesc(ins.readObject());
        creatorId = ins.readInt();
    }

    // documntation from Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(gameId);
        out.writeField(name);
        out.writeField(description);
        out.writeBoolean(avrGame);
        out.writeObject(thumbMedia);
        out.writeInt(creatorId);
    }
}
}
