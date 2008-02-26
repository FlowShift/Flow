//
// $Id$

package client.util;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.threerings.gwt.ui.SmartTable;

import com.threerings.msoy.item.data.all.MediaDesc;

import client.shell.Application;

/**
 * Displays an item (or anything with a thumbnail image, a name and potentially extra info below)
 * with a grey box around it in our preferred grid of items style.
 */
public class ItemBox extends SmartTable
{
    public ItemBox (MediaDesc media, String name, final String page, final String args)
    {
        super("itemBox", 0, 0);

        ClickListener onClick = new ClickListener() {
            public void onClick (Widget widget) {
                Application.go(page, args);
            }
        };
        addWidget(new ThumbBox(media, onClick), getColumns(), null);
        getFlexCellFormatter().setHorizontalAlignment(getRowCount()-1, 0, HasAlignment.ALIGN_CENTER);
        addWidget(MsoyUI.createActionLabel(name, "Name", onClick), getColumns(), null);
    }

    protected int getColumns ()
    {
        return 1;
    }
}
