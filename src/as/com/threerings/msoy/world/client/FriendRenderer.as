//
// $Id$

package com.threerings.msoy.world.client {

import flash.events.MouseEvent;

import mx.containers.HBox;
import mx.containers.VBox;

import mx.controls.Label;
import mx.controls.Text;

import mx.controls.scrollClasses.ScrollBar;

import mx.core.ScrollPolicy;

import com.threerings.util.CommandEvent;
import com.threerings.flex.CommandMenu;
import com.threerings.flex.FlexUtil;

import com.threerings.msoy.ui.PlayerRenderer;

import com.threerings.msoy.client.Msgs;
import com.threerings.msoy.client.MsoyController;
import com.threerings.msoy.data.all.FriendEntry;
import com.threerings.msoy.data.all.PlayerEntry;

public class FriendRenderer extends PlayerRenderer 
{
    public function FriendRenderer () 
    {
        super();

        addEventListener(MouseEvent.CLICK, handleClick);
    }

    override protected function addCustomControls (content :VBox) :void
    {
        var friend :FriendEntry = FriendEntry(this.data);

        var name :Label = FlexUtil.createLabel(friend.name.toString(), "friendLabel");
        name.width = content.width;
        content.addChild(name);

        var statusContainer :HBox = new HBox();
        statusContainer.setStyle("paddingLeft", 3);

        var status :Label = FlexUtil.createLabel(friend.status, "friendStatusLabel");
        status.width = content.width;
        statusContainer.addChild(status);

        content.addChild(statusContainer);
    }

    protected function handleClick (event :MouseEvent) :void
    {
        var friend :FriendEntry = this.data as FriendEntry;
        if (friend != null) {
            var menuItems :Array = [];
            mctx.getMsoyController().addFriendMenuItems(friend.name, menuItems);
            CommandMenu.createMenu(menuItems, mctx.getTopPanel()).popUpAtMouse();
        }
    }
}
}
