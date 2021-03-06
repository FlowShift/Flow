//
// $Id$

package client.item;

//import com.google.gwt.core.client.GWT;
//import com.google.gwt.user.client.ui.HorizontalPanel;
//import com.google.gwt.user.client.ui.Image;

//import com.threerings.gwt.ui.WidgetUtil;

import com.threerings.orth.data.MediaDesc;

import com.threerings.msoy.web.gwt.Pages;

import client.ui.ClickBox;

/**
 * Displays an item thumbnail and name. If the item is remixable, a special remixable icon will be
 * displayed next to the thumbnail.
 */
public class ItemBox extends ClickBox
{
    public ItemBox (MediaDesc media, String name, boolean remixable, Pages page, Object... args)
    {
        super(media, name, page, args);
        addStyleName("itemBox");

//        if (remixable) {
//            // add a wee icon indicating remixable
//            Image remix = new Image("/images/item/remixable_icon.png");
//            remix.setTitle(_imsgs.remixTip());
//
//            // arrange it next to the thumbnail
//            HorizontalPanel hpan = new HorizontalPanel();
//            hpan.add(getWidget(0, 0));
//            hpan.add(WidgetUtil.makeShim(2, 2));
//            hpan.add(remix);
//            setWidget(0, 0, hpan);
//        }
    }

//    protected static final ItemMessages _imsgs = GWT.create(ItemMessages.class);
}
