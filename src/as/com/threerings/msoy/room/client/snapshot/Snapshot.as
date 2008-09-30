package com.threerings.msoy.room.client.snapshot {

import com.threerings.util.Log;

import flash.display.BitmapData;
import flash.display.DisplayObject;

import flash.geom.Matrix;
import flash.geom.Rectangle;

import flash.utils.ByteArray;

import com.threerings.flash.BackgroundJPGEncoder;

import com.threerings.util.Log;
import com.threerings.util.ValueEvent;

import com.threerings.msoy.client.LayeredContainer;

import com.threerings.msoy.room.client.OccupantSprite;
import com.threerings.msoy.room.client.RoomElement;
import com.threerings.msoy.room.client.RoomView;

/**
 * Represents a particular snapshot
 */ 
public class Snapshot 
{    
    public var bitmap :BitmapData;

    public const log :Log = Log.getLog(this);

    /**
     * Create a 'Snapshot' of the provided view.  With a frame of the provided size.
     */
    public function Snapshot (view :RoomView, framer :Framer, width :int, height :int)
    {
        _view = view;
                
        _frame = new Rectangle(0, 0, width, height);
        _framer = framer;
        bitmap = new BitmapData(width, height);
    }

    /**
     * Update the snapshot.
     */
    public function updateSnapshot (
        includeOccupants :Boolean = true, includeOverlays :Boolean = true) :Boolean
    {
        // first let's fill the bitmap with black or something
        bitmap.fillRect(_frame, 0x000000);

        // draw the room, scaling down to the appropriate size
        var allSuccess :Boolean = true;

        if (!renderChildren(includeOccupants)) {
            allSuccess = false;
        }

        if (includeOverlays) {
            renderOverlays();
        }

        return allSuccess;
    }
    
    /**
     * Render the overlays 
     */ 
    protected function renderOverlays () :void {        
        var d :DisplayObject = _view;
        
        // search up through the containment hierarchy until you find the LayeredContainer
        // or the end of the hierarchy
        while (!(d is LayeredContainer) && d.parent != null) {
            d = d.parent;
        }
        if (d is LayeredContainer) {
            (d as LayeredContainer).snapshotOverlays(bitmap, _framer);
        }
    }

    /**
     * Render the children
     */
    protected function renderChildren (includeOccupants :Boolean) :Boolean 
    {
        var allSuccess:Boolean = true;
        
        for (var ii :int = 0; ii < _view.numChildren; ii++) {
            var child :DisplayObject = _view.getChildAt(ii);
            if (!includeOccupants && (child is OccupantSprite)) {
                continue; // skip it!
            }
            
            var matrix :Matrix = child.transform.matrix; // makes a clone...
            _framer.applyTo(matrix); // apply the framing transformation to the matrix

            if (child is RoomElement) {
                var success :Boolean = RoomElement(child).snapshot(bitmap, matrix);
                allSuccess &&= success;

            } else {
                try {
                    bitmap.draw(child, matrix, null, null, null, true);

                } catch (err :SecurityError) {
                    // not a critical error
                    log.info("Unable to snapshot Room element", err);
                    allSuccess = false;
                }
            }            
        }
        return allSuccess;
    }

    /**
     * Cause the snapshot to be encoded and uploaded.  Encoding is done in the background using
     * a background updater, and 
     */
    public function encodeAndUpload (uploadOperation:Function, uploadDone:Function) :void
    {
        log.debug("starting encoding");
        _uploadOperation = uploadOperation
        _uploadDone = uploadDone;
        _encoder = new BackgroundJPGEncoder(bitmap, 70);
        _encoder.addEventListener("complete", handleJpegEncoded);
        _encoder.start();
    }

    /**
     * Cancel encoding if it's underway.  We don't cancel uploads.
     */
    public function cancelEncoding () :void
    {
        if (_encoder) {
            _encoder.cancel();
        }
    }

    protected function handleJpegEncoded (event :ValueEvent) :void
    {
        log.debug("jpeg encoded");
        
        // call whatever we're supposed to call with the jpeg data now that we have it
        _uploadOperation(event.value as ByteArray);
        
        // now call whatever the next operation in the chain is
        _uploadDone();
    }

    protected var _encoder :BackgroundJPGEncoder;

    protected var _uploadOperation :Function;
    protected var _uploadDone :Function;
    
    protected var _view :RoomView;
    protected var _frame :Rectangle;
    protected var _framer :Framer;
}
}
