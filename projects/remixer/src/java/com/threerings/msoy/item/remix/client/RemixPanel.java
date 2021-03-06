//
// $Id$

package com.threerings.msoy.item.remix.client;

import java.applet.Applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.samskivert.swing.AWTResultListener;
import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.samskivert.swing.util.SwingUtil;

import com.samskivert.util.ObjectUtil;
import com.samskivert.util.ResultListener;
import com.samskivert.util.RunQueue;

import com.whirled.DataPack;

import com.whirled.remix.data.EditableDataPack;

import com.threerings.msoy.utils.Base64Sender;

public class RemixPanel extends JPanel
{
    public RemixPanel (String url, Applet container)
    {
        super(new BorderLayout());

        _cancel = new AbstractAction("Cancel") {
            public void actionPerformed (ActionEvent e) {
                // TODO
            }
        };
        _remix = new AbstractAction("Remix") {
            public void actionPerformed (ActionEvent e) {
                // TODO
            }
        };
        _remix.setEnabled(false);

        JPanel butPan = GroupLayout.makeButtonBox(GroupLayout.LEFT);
        butPan.add(new JButton(_cancel));
        butPan.add(new JButton(_remix));
        add(butPan, BorderLayout.SOUTH);

        _sender = new Base64Sender(container, "remixPreview", "setMediaBytes");

        startPackLoading(url);
    }

    protected void startPackLoading (String url)
    {
        ResultListener<EditableDataPack> rl = new ResultListener<EditableDataPack>() {
            public void requestCompleted (EditableDataPack pack) {
                packAvailable();
            }

            public void requestFailed (Exception cause) {
                throw new RuntimeException(cause);
            }
        };
        _pack = new EditableDataPack(url, new AWTResultListener<EditableDataPack>(rl));
    }

    /**
     * Called once our pack is ready to go.
     */
    protected void packAvailable ()
    {
        JPanel panel = GroupLayout.makeVBox(GroupLayout.NONE, GroupLayout.TOP, GroupLayout.STRETCH);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addFields(panel, _pack.getDataFields(), true);
        addFields(panel, _pack.getFileFields(), false);

        add(new JScrollPane(panel), BorderLayout.CENTER);
        SwingUtil.refresh(this);

        updatePreview();
    }

    protected void addFields (JPanel panel, List<String> fields, final boolean areData)
    {
        if (fields.isEmpty()) {
            return;
        }

        panel.add(new JLabel(areData ? "Data fields" : "Files"));

        for (String name : fields) {
            JPanel hbox = GroupLayout.makeHBox(GroupLayout.NONE, GroupLayout.LEFT);
            hbox.add(new JLabel(name));
            JButton but = new JButton("Change");
            final String fname = name;
            but.addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent e) {
                    changeEntry(fname, areData);
                }
            });
            hbox.add(but);
            panel.add(hbox);
        }
    }

    protected void changeEntry (String name, boolean isData)
    {
        if (isData) {
            changeEntry(_pack.getDataEntry(name));
        } else {
            changeEntry(_pack.getFileEntry(name));
        }
    }

    protected void changeEntry (DataPack.FileEntry entry)
    {
        // TODO
    }

    protected void changeEntry (final DataPack.DataEntry entry)
    {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.fill = GridBagConstraints.HORIZONTAL;
        labelConstraints.anchor = GridBagConstraints.NORTHWEST;
        GridBagConstraints compConstraints = (GridBagConstraints) labelConstraints.clone();
        compConstraints.gridwidth = GridBagConstraints.REMAINDER;

        // set up the editor panel
        setupEdit(entry, panel, labelConstraints, compConstraints);

        final JCheckBox presentBox = entry.optional ? new JCheckBox() : null;

        // if we're editing a data entry, we might have a default value that can be reverted-to.
        JButton revButton = null;
        // TODO: since values are called null by not specifying them, there's currently
        // no way to specify the defaultValue as null, but if it's optional we say that
        // null is the default. We may want to change this.
        boolean hasDefault = (entry.defaultValue != null) || (entry.optional);
        if (hasDefault) {
            revButton = new JButton("Revert to default");
            panel.add(new JLabel(""), labelConstraints);
            panel.add(revButton, compConstraints);
        }
        final JButton revertButton = revButton;

        panel.add(new JLabel("Present:"), labelConstraints);
        if (presentBox == null) {
            panel.add(new JLabel("<Required>"), compConstraints);

        } else {
            presentBox.setSelected(entry.value != null);
            panel.add(presentBox, compConstraints);
        }

        final JDialog dialog = new JDialog();
        dialog.setTitle("Edit '" + entry.name + "'");

        final DataPack.DataType type = (DataPack.DataType) entry.getType();
        final Editor editor = getEditor(type);
        final JComponent editorComp = editor.getComponent();
        editor.setValue(entry.value);
        panel.add(editorComp, compConstraints);

        editorComp.setVisible(presentBox == null || presentBox.isSelected());
        ActionListener presentAction = null;
        if (presentBox != null) {
            presentAction = new ActionListener() {
                public void actionPerformed (ActionEvent e) {
                    boolean present = presentBox.isSelected();
                    editorComp.setVisible(present);
                    if (revertButton != null) {
                        Object newValue = present ? editor.getValue() : null;
                        revertButton.setEnabled(!ObjectUtil.equals(newValue, entry.defaultValue));
                    }
                    SwingUtil.refresh(panel);
                    dialog.pack();
                }
            };
            presentBox.addActionListener(presentAction);
        }
        if (revertButton != null) {
            revertButton.setEnabled(!ObjectUtil.equals(entry.value, entry.defaultValue));
            final ActionListener fPresentAction = presentAction;
            revertButton.addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent e) {
                    editor.setValue(entry.defaultValue);
                    revertButton.setEnabled(false);
                    if (entry.defaultValue == null) {
                        presentBox.setSelected(false);
                    }
                    if (fPresentAction != null) {
                        fPresentAction.actionPerformed(e);
                    }
                }
            });
        }

        final Action cancelAction = new AbstractAction("Cancel") {
            public void actionPerformed (ActionEvent e) {
                dialog.dispose();
            }
        };
        final Action okAction = new AbstractAction("OK") {
            public void actionPerformed (ActionEvent e) {
                Object newValue;
                if (presentBox == null || presentBox.isSelected()) {
                    newValue = editor.getValue();
                } else {
                    newValue = null;
                }
                if (!ObjectUtil.equals(newValue, entry.value)) {
                    entry.value = newValue;
                    updatePreview();
                }
                dialog.dispose();
            }
        };

        editor.setActionListeners(okAction, cancelAction);

        JPanel butPan = GroupLayout.makeButtonBox(GroupLayout.RIGHT);
        butPan.add(new JButton(cancelAction));
        butPan.add(new JButton(okAction));
        panel.add(butPan);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.pack();
        dialog.setVisible(true);
    }

    protected void setupEdit (
        DataPack.AbstractEntry entry, JPanel panel, GridBagConstraints labelConstraints,
        GridBagConstraints compConstraints)
    {
        panel.add(new JLabel("Name:"), labelConstraints);
        panel.add(new JLabel(entry.name), compConstraints);

        panel.add(new JLabel("Description:"), labelConstraints);
        panel.add(new JLabel(entry.info), compConstraints);

        DataPack.AbstractType type = entry.getType();
        panel.add(new JLabel("Type:"), labelConstraints);
        panel.add(new JLabel(type.toString() + " (" + type.getDescription() + ")"), compConstraints);
    }

    protected void updatePreview ()
    {
        System.err.println("Now updating preview..");
        _sender.sendBytes(_pack.toByteArray());
        _remix.setEnabled(true);
    }

    protected Editor getEditor (DataPack.DataType type)
    {
        switch (type) {
        case BOOLEAN:
            return new BooleanEditor();

        case COLOR:
            return new ColorEditor();

        case STRING:
            return new StringEditor();

        case NUMBER:
            return new NumberEditor();

        default:
            throw new RuntimeException("No editor defined for DataPack DataType: " + type);
        }
    }

    /** The datapack we're editing. */
    protected EditableDataPack _pack;

    protected Action _cancel;

    protected Action _remix;

    protected Base64Sender _sender;
}

abstract class Editor
{
    public abstract void setValue (Object value);

    public abstract Object getValue ();

    public abstract JComponent getComponent ();

    public void setActionListeners (ActionListener ok, ActionListener cancel)
    {
        // nada by default
    }
}

class BooleanEditor extends Editor
{
    public BooleanEditor ()
    {
        _check = new JCheckBox();
    }

    public void setValue (Object value)
    {
        _check.setSelected(Boolean.TRUE.equals(value));
    }

    public Object getValue ()
    {
        return Boolean.valueOf(_check.isSelected());
    }

    public JComponent getComponent ()
    {
        return _check;
    }

    protected JCheckBox _check;
}

class ColorEditor extends Editor
{
    public ColorEditor ()
    {
        _chooser = new JColorChooser();
    }

    public void setValue (Object value)
    {
        _chooser.setColor((Color) value);
    }

    public Object getValue ()
    {
        return _chooser.getColor();
    }

    public JComponent getComponent ()
    {
        return _chooser;
    }

    protected JColorChooser _chooser;
}

class StringEditor extends Editor
{
    public StringEditor ()
    {
        _field = new JTextField();
    }

    public void setValue (Object value)
    {
        _field.setText((String) value);
    }

    public Object getValue ()
    {
        return _field.getText().trim();
    }

    public JComponent getComponent ()
    {
        return _field;
    }

    @Override
    public void setActionListeners (ActionListener ok, ActionListener cancel)
    {
        _field.addActionListener(ok);
    }

    protected JTextField _field;
}

class NumberEditor extends StringEditor
{
    public void setValue (Object value)
    {
        if (value instanceof Number) {
            value = DataPack.DataType.NUMBER.formatValue(value);
        }
        super.setValue(value);
    }

    public Object getValue ()
    {
        Object val = super.getValue();
        return (val == null) ? val : DataPack.DataType.NUMBER.parseValue((String) val, true);
    }
}
