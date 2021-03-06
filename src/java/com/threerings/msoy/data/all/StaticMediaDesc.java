//
// $Id$

package com.threerings.msoy.data.all;

import com.threerings.orth.data.MediaDesc;

/**
 * Provides a "faked" media descriptor for static media (default thumbnails and
 * furni representations).
 */
public class StaticMediaDesc extends BasicMediaDesc
{
    /** Used for unserialization. */
    public StaticMediaDesc ()
    {
    }

    /**
     * Creates a configured static media descriptor.
     */
    public StaticMediaDesc (byte mimeType, String itemType, String mediaType)
    {
        this(mimeType, itemType, mediaType, NOT_CONSTRAINED);
    }

    /**
     * Creates a configured static media descriptor.
     */
    public StaticMediaDesc (byte mimeType, String itemType, String mediaType, byte constraint)
    {
        super(mimeType, constraint);
        _itemType = itemType;
        _mediaType = mediaType;
    }

    /**
     * Returns the type of item for which we're providing static media.
     */
    public String getItemType ()
    {
        return _itemType;
    }

    // from MediaDesc
    public MediaDesc newWithConstraint (byte constraint)
    {
        return new StaticMediaDesc(getMimeType(), _itemType, _mediaType, constraint);
    }

    /**
     * Returns the media type for which we're obtaining the static default.
     */
    public String getMediaType ()
    {
        return _mediaType;
    }

    public String getMediaPath ()
    {
        return DeploymentConfig.staticMediaURL + _itemType + "/" + _mediaType +
            MediaMimeTypes.mimeTypeToSuffix(getMimeType());
    }

    public boolean equals (Object other)
    {
        return (other instanceof StaticMediaDesc) && super.equals(other) &&
            ((StaticMediaDesc) other).getItemType().equals(_itemType) &&
            ((StaticMediaDesc) other).getMediaType().equals(_mediaType);
    }

    protected String _itemType;
    protected String _mediaType;
}
