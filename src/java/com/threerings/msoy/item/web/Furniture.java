//
// $Id$

package com.threerings.msoy.item.web;

/**
 * Represents a piece of furniture (any prop really) that a user can place into
 * a virtual world scene and potentially interact with.
 */
public class Furniture extends Item
{
    /** An action associated with this furniture which is dispatched to the
     * virtual world client when the furniture is clicked on (max length 255
     * characters). */
    public String action = "";

    /** A description of this piece of furniture (max length 255 characters). */
    public String description;

    // @Override from Item
    public byte getType ()
    {
        return FURNITURE;
    }

    // @Override from Item
    public String getDescription ()
    {
        return description;
    }

    // @Override
    public boolean isConsistent ()
    {
        return super.isConsistent() && (furniMedia != null) &&
            nonBlank(description);
    }

    // @Override // from Item
    public MediaDesc getPreviewMedia ()
    {
        return getFurniMedia();
    }

    // @Override // from Item
    protected MediaDesc getDefaultThumbnailMedia ()
    {
        if (furniMedia != null && furniMedia.isImage()) {
            return furniMedia;
        }
        return super.getDefaultThumbnailMedia();
    }

    // @Override // from Item
    protected MediaDesc getDefaultFurniMedia ()
    {
        return null; // there is no default
    }
}
