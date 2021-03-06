//
// $Id$

package com.threerings.msoy.applets.remixer {

import flash.events.Event;

import com.whirled.remix.data.EditableDataPack;

import mx.controls.CheckBox;
import mx.controls.ColorPicker;
import mx.controls.ComboBox;
import mx.controls.HSlider;
import mx.controls.Label;
import mx.controls.Spacer;
import mx.events.ListEvent;
import mx.validators.NumberValidator;
import mx.validators.Validator;

import com.threerings.util.ValueEvent;

import com.threerings.flex.CommandButton;

public class DataEditor extends FieldEditor
{
    public function DataEditor (ctx :RemixContext, name :String)
    {
        var entry :Object = ctx.pack.getDataEntry(name);
        _value = entry.value;
        super(ctx, name, entry);
    }

    override protected function getUI (entry :Object) :Array
    {
        try {
            return Object(this)["setup" + entry.type](entry);
        } catch (err :Error) {
            // fall through
        }
        // use the string editor with basic validation
        return setupString(entry);
    }

    protected function setupChoice (entry :Object) :Array
    {
        var box :ComboBox = new ComboBox();
        box.dataProvider = entry.choices;
        box.selectedItem = entry.value;
        box.addEventListener(ListEvent.CHANGE, function (... ignored) :void {
            if (box.selectedIndex != -1) {
                updateValue(box.selectedItem);
            }
        });

        return [ box, new Spacer(), box ];
    }

    protected function setupBoolean (entry :Object) :Array
    {
        var tog :CheckBox = new CheckBox();
        tog.styleName = "oldCheckBox";
        tog.selected = Boolean(entry.value);
        tog.addEventListener(Event.CHANGE, function (... ignored) :void {
            updateValue(tog.selected);
        });

        return [ tog, new Spacer(), tog ];
    }

    protected function setupString (entry :Object, validator :Validator = null) :Array
    {
        var createFn :Function = function () :PopupEditor {
            return new PopupStringEditor(validator);
        };
        return setupPopper(entry, createFn);
    }

    protected function setupint (entry :Object) :Array
    {
        return setupNumber(entry, true);
    }

    protected function setupNumber (entry :Object, isInt :Boolean = false) :Array
    {
        var min :Number = Number(entry.min);
        var max :Number = Number(entry.max);

        // TODO: allow min/max either way, but allow a hint to specify to use the slider
        if (!isNaN(max) && !isNaN(min)) {
            var hslider :HSlider = new HSlider();
            hslider.minimum = min;
            hslider.maximum = max;
            var value :Number = Number(entry.value);
            if (isNaN(value)) {
                value = min;
            } else if (value > max || value < min) {
                // set it to the closer one... (breaks if things are too close to MAX_VALUE)
                value = (Math.abs(max - value) < Math.abs(min - value)) ? max : min;
            }
            _value = value;
            hslider.value = value;
            hslider.addEventListener(Event.CHANGE, function (... ignored) :void {
                updateValue(hslider.value);
            });
            if (isInt) {
                hslider.snapInterval = 1;
            }

            return [ hslider, new Spacer(), hslider ];

        } else {
            var val :NumberValidator = new NumberValidator();
            val.minValue = min;
            val.maxValue = max;
            if (isInt) {
                val.domain = "int";
            }
            return setupString(entry, val);
        }
    }

    protected function setupColor (entry :Object) :Array
    {
        var picker :ColorPicker = new ColorPicker();
        picker.selectedColor = uint(entry.value);
        _value = picker.selectedColor;
        picker.addEventListener(Event.CLOSE, function (... ignored) :void {
            updateValue(picker.selectedColor);
        });

        return [ picker, new Spacer(), picker ];
    }

    protected function setupPoint (entry :Object) :Array
    {
        var createFn :Function = function () :PopupEditor {
            return new PopupPointEditor();
        };
        return setupPopper(entry, createFn);
    }

    protected function setupRectangle (entry :Object) :Array
    {
        var createFn :Function = function () :PopupEditor {
            return new PopupRectangleEditor();
        };
        return setupPopper(entry, createFn);
    }

    protected function setupArray (entry :Object) :Array
    {
        var createFn :Function = function () :PopupEditor {
            return new PopupArrayEditor();
        };
        return setupPopper(entry, createFn);
    }

    /**
     * Configure an editor that uses a pop-up to edit the actual value.
     */
    protected function setupPopper (entry :Object, createPopper :Function) :Array
    {
        var label :Label = new Label();
        label.selectable = false;
        label.setStyle("color", NAME_AND_VALUE_COLOR);
        label.text = entry.toString();

        var handleEntryChange :Function = function (event :ValueEvent) :void {
            if (event.value === entry.name) {
                label.text = entry.toString(); // update the label
            }
        };
        _ctx.pack.addEventListener(EditableDataPack.DATA_CHANGED, handleEntryChange);

        var dataEditor :DataEditor = this;
        var doPopFn :Function = function () :void {
            // here also, we don't use entry, we fetch it fresh
            var popper :PopupEditor = createPopper();
            popper.open(_ctx, dataEditor, _ctx.pack.getDataEntry(entry.name), updateValue);
        };
        var change :CommandButton = createEditButton(doPopFn);
        return [ label, change, change ];
    }

    protected function updateValue (value :*) :void
    {
        if (value != _value) {
            _value = value;
            updateEntry();
        }
    }

    override protected function updateEntry () :void
    {
        _ctx.pack.setData(_name, _used.selected ? _value : null);
        setChanged();
    }

    protected var _value :*;
}
}
