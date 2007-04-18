//
// $Id$

package com.threerings.msoy.item.data.all;

/**
 * A digital item representing a simple text document.
 */
public class Document extends Item
{
    /** The document media. */
    public MediaDesc docMedia;

    // @Override // from Item
    public byte getType ()
    {
        return DOCUMENT;
    }

    // @Override // from Item
    public boolean isConsistent ()
    {
        return super.isConsistent() && (docMedia != null) && nonBlank(name);
    }

    // @Override // from Item
    public MediaDesc getPreviewMedia ()
    {
        return getThumbnailMedia();
    }
}
