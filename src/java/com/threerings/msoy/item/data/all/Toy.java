//
// $Id$

package com.threerings.msoy.item.data.all;

import com.threerings.orth.data.MediaDesc;

/**
 * Represents an interactive piece of furniture. Something that lives permanently in a room but
 * which is interactive in some way.
 */
public class Toy extends Item
{
    @Override // from Item
    public MsoyItemType getType ()
    {
        return MsoyItemType.TOY;
    }

    @Override
    public boolean isConsistent ()
    {
        return super.isConsistent() && nonBlank(name, MAX_NAME_LENGTH) && (_furniMedia != null) &&
            (_furniMedia.isSWF() || _furniMedia.isRemixed());
    }

    @Override // from Item
    public MediaDesc getPreviewMedia ()
    {
        return getFurniMedia();
    }

    @Override // from Item
    protected MediaDesc getDefaultFurniMedia ()
    {
        return null; // there is no default
    }
}
