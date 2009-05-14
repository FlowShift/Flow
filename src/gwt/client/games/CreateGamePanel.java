//
// $Id$

package client.games;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;

import com.threerings.msoy.data.all.MediaDesc;
import com.threerings.msoy.game.gwt.GameInfo;
import com.threerings.msoy.item.data.all.Item;
import com.threerings.msoy.web.gwt.Args;
import com.threerings.msoy.web.gwt.Pages;

import client.ui.MsoyUI;
import client.util.ClickCallback;
import client.util.Link;

/**
 * Displays an interface for creating a brand new game.
 */
public class CreateGamePanel extends BaseEditorPanel
{
    public CreateGamePanel ()
    {
        addStyleName("createGame");

        int row = 0;
        setWidget(row++, 0, MsoyUI.createHTML(_msgs.cgIntro(), "Intro"), 2, null);

        RadioButton parlor = new RadioButton("type", _msgs.cgTypeParlor());
        parlor.setValue(true);
        final RadioButton avrg = new RadioButton("type", _msgs.cgTypeAVRG());
        addRow(_msgs.cgType(), MsoyUI.createFlowPanel(null, parlor, avrg), new Command() {
            public void execute () {
                _isAVRG = avrg.getValue();
            }
        });

        final TextBox name = MsoyUI.createTextBox("", GameInfo.MAX_NAME_LENGTH, 20);
        addRow(_msgs.egName(), name, new Command() {
            public void execute () {
                _name = checkName(name.getText().trim());
            }
        });

        final MediaBox tbox = new MediaBox(MediaDesc.THUMBNAIL_SIZE, Item.THUMB_MEDIA, null);
        addRow(_msgs.egThumb(), _msgs.egThumbTip(), tbox, new Command() {
            public void execute () {
                _thumbMedia = requireImageMedia(_msgs.egThumb(), tbox.getMedia());
            }
        });

        final CodeBox ccbox = new CodeBox(Item.MAIN_MEDIA, null);
        addRow(_msgs.egClientCode(), _msgs.egClientCodeTip(), ccbox, new Command() {
            public void execute () {
                _clientMedia = checkClientMedia(ccbox.getMedia());
            }
        });

        Button save = addSaveRow();
        new ClickCallback<Integer>(save) {
            protected boolean callService () {
                if (!bindChanges()) {
                    return false;
                }
                _gamesvc.createGame(_isAVRG, _name, _thumbMedia, _clientMedia, this);
                return true;
            }
            protected boolean gotResult (Integer gameId) {
                Link.go(Pages.GAMES, Args.compose("e", gameId));
                return true;
            }
        };
    }

    protected boolean _isAVRG;
    protected String _name;
    protected MediaDesc _thumbMedia;
    protected MediaDesc _clientMedia;
}
