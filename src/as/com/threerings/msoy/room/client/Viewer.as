//
// $Id$

package com.threerings.msoy.room.client {

import flash.display.Bitmap;
import flash.display.Sprite;
import flash.utils.ByteArray;

import com.threerings.util.ValueEvent;

import com.threerings.media.MediaContainer;

import com.threerings.msoy.client.MsoyParameters;

/**
 * A generic viewer, like the AvatarViewer, but for any item.
 * This will grow and change.
 */
[SWF(width="320", height="240")]
public class Viewer extends Sprite
{
    public static const WIDTH :int = 320; // MediaDesc PREVIEW_SIZE
    public static const HEIGHT :int = 240; // MediaDesc PREVIEW_SIZE

    public function Viewer (params :Object = null)
    {
        var bmp :Bitmap = Bitmap(new BACKGROUND());
        graphics.beginBitmapFill(bmp.bitmapData);
        graphics.drawRect(0, 0, WIDTH, HEIGHT);
        graphics.endFill();

        if (params == null) {
            gotParams(MsoyParameters.get());
        } else {
            gotParams(params);
        }
    }

    /**
     * Load the media to display at a ByteArray.
     */
    public function loadBytes (bytes :ByteArray) :void
    {
        (_sprite as EntitySprite).viz.setZippedMediaBytes(bytes);
    }

    protected function gotParams (params :Object) :void
    {
        var media :String = params["media"] as String;
        var mode :int = int(params["mode"]);

        _sprite = new ViewerSprite();
        _sprite.viz.addEventListener(MediaContainer.SIZE_KNOWN, handleSizeKnown);
        _sprite.viz.setMedia(media);
        addChild(_sprite.viz);
    }

    protected function handleSizeKnown (event :ValueEvent) :void
    {
        var width :Number = event.value[0];
        var height :Number = event.value[1];
        var scale :Number = Math.min(1, Math.min(WIDTH / width, HEIGHT / height));
        _sprite.setScale(scale);

        _sprite.viz.x = (WIDTH - (scale * width)) / 2;
        _sprite.viz.y = (HEIGHT - (scale * height)) / 2;
    }

    protected var _sprite :ViewerSprite;

    [Embed(source="../../../../../../../pages/images/item/detail_preview_bg.png")]
    protected static const BACKGROUND :Class;
}
}

import com.threerings.msoy.room.client.FurniSprite;
import com.threerings.msoy.room.data.FurniData;

/**
 * A simple sprite used for viewing.
 */
class ViewerSprite extends FurniSprite
{
    public function ViewerSprite ()
    {
        super(null, new FurniData());
    }

    public function setScale (scale :Number) :void
    {
        _scale = scale;
        _sprite.setSpriteMediaScale(scale, scale);
        scaleUpdated();
    }

    override public function capturesMouse () :Boolean
    {
        return true;
    }

    protected var _scale :Number = 1;
}
