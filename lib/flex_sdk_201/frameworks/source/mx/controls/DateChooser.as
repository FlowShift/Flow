////////////////////////////////////////////////////////////////////////////////
//
//  Copyright (C) 2003-2006 Adobe Macromedia Software LLC and its licensors.
//  All Rights Reserved. The following is Source Code and is subject to all
//  restrictions on such code as contained in the End User License Agreement
//  accompanying this product.
//
////////////////////////////////////////////////////////////////////////////////

package mx.controls
{

import flash.display.DisplayObject;
import flash.display.GradientType;
import flash.display.Graphics;
import flash.display.Sprite;
import flash.events.Event;
import flash.events.EventPhase;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.geom.Matrix;
import flash.text.TextFormat;
import flash.ui.Keyboard;
import mx.core.FlexSprite;
import mx.core.UIComponent;
import mx.core.UITextField;
import mx.core.mx_internal;
import mx.events.CalendarLayoutChangeEvent;
import mx.events.DateChooserEvent;
import mx.events.DateChooserEventDetail;
import mx.events.FlexEvent;
import mx.graphics.RectangularDropShadow;
import mx.managers.IFocusManagerComponent;
import mx.managers.ISystemManager;
import mx.managers.SystemManager;
import mx.resources.ResourceBundle;
import mx.styles.StyleManager;
import mx.utils.GraphicsUtil;

use namespace mx_internal;

//--------------------------------------
//  Events
//--------------------------------------

/**
 *  Dispatched when a date is selected or changed.
 *
 *  @eventType mx.events.CalendarLayoutChangeEvent.CHANGE
 *  @helpid 3601
 *  @tiptext change event
 */
[Event(name="change", type="mx.events.CalendarLayoutChangeEvent")]

/**
 *  Dispatched when the month changes due to user interaction.
 *
 *  @eventType mx.events.DateChooserEvent.SCROLL
 *  @helpid 3600
 *  @tiptext scroll event
 */
[Event(name="scroll", type="mx.events.DateChooserEvent")]

//--------------------------------------
//  Styles
//--------------------------------------

include "../styles/metadata/FocusStyles.as"
include "../styles/metadata/GapStyles.as" 
include "../styles/metadata/LeadingStyle.as"
include "../styles/metadata/SkinStyles.as"
include "../styles/metadata/TextStyles.as"

/**
 *  Alpha level of the color defined by the <code>backgroundColor</code>
 *  property.
 *  Valid values range from 0.0 to 1.0.
 *  @default 1.0
 */
[Style(name="backgroundAlpha", type="Number", inherit="no")]

/**
 *  Background color of the DateChooser control.
 *  
 *  @default 0xFFFFF
 */
[Style(name="backgroundColor", type="uint", format="Color", inherit="no")]

/**
 *  Bounding box thickness.
 *  Only used when <code>borderStyle</code> is set to <code>"solid"</code>.
 *  @default 1
 */
[Style(name="borderThickness", type="Number", format="Length", inherit="no")]

/**
 *  Colors of the band at the top of the DateChooser control.
 *  The default value is <code>[ 0xE1E5EB, 0xF4F5F7 ]</code>.
 */
[Style(name="headerColors", type="Array", arrayType="uint", format="Color", inherit="yes")]

/**
 *  Name of the style sheet definition to configure the text (month name and year)
 *  and appearance of the header area of the control.
 */
[Style(name="headerStyleName", type="String", inherit="no")]

/**
 *  Name of the class to use as the skin for the next month arrow
 *  when the arrow is disabled.
 *  The default value is the DateChooserMonthArrowSkin class.
 */
[Style(name="nextMonthDisabledSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the next month arrow
 *  when the user presses the mouse button down on the arrow.
 *  The default value is the DateChooserMonthArrowSkin class.
 */
[Style(name="nextMonthDownSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the next month arrow
 *  when the user moves the mouse pointer over the arrow.
 *  The default value is the DateChooserMonthArrowSkin class.
 */
[Style(name="nextMonthOverSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the next month arrow
 *  when the mouse pointer is not over the arrow.
 *  The default value is the DateChooserMonthArrowSkin class.
 */
[Style(name="nextMonthUpSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the next year arrow
 *  when the arrow is disabled. 
 *  The default value is the DateChooserYearArrowSkin class.
 */
[Style(name="nextYearDisabledSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the next Year arrow
 *  when the user presses the mouse button down on the arrow.
 *  The default value is the DateChooserYearArrowSkin class.
 */
[Style(name="nextYearDownSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the next Year arrow
 *  when the user moves the mouse pointer over the arrow.
 *  The default value is the DateChooserYearArrowSkin class.
 */
[Style(name="nextYearOverSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the next Year arrow
 *  when the mouse pointer is not over the arrow.
 *  The default value is the DateChooserYearArrowSkin class.
 */
[Style(name="nextYearUpSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the previous month arrow
 *  when the arrow is disabled.
 *  The default value is the DateChooserMonthArrowSkin class.
 */
[Style(name="prevMonthDisabledSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the previous month arrow
 *  when the user presses the mouse button down over the arrow.
 *  The default value is the DateChooserMonthArrowSkin class.
 */
[Style(name="prevMonthDownSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the previous month arrow
 *  when the user holds the mouse pointer over the arrow.
 *  The default value is the DateChooserMonthArrowSkin class.
 */
[Style(name="prevMonthOverSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the previous month arrow
 *  when the mouse pointer is not over the arrow.
 *  The default value is the DateChooserMonthArrowSkin class.
 */
[Style(name="prevMonthUpSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the previous Year arrow
 *  when the arrow is disabled.
 *  The default value is the DateChooserYearArrowSkin class.
 */
[Style(name="prevYearDisabledSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the previous Year arrow
 *  when the user presses the mouse button down over the arrow.
 *  The default value is the DateChooserYearArrowSkin class.
 */
[Style(name="prevYearDownSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the previous Year arrow
 *  when the user holds the mouse pointer over the arrow.
 *  The default value is the DateChooserYearArrowSkin class.
 */
[Style(name="prevYearOverSkin", type="Class", inherit="no")]

/**
 *  Name of the class to use as the skin for the previous Year arrow
 *  when the mouse button not over the arrow.
 *  The default value is the DateChooserYearArrowSkin class.
 */
[Style(name="prevYearUpSkin", type="Class", inherit="no")]

/**
 *  Color of the highlight area of the date when the user holds the
 *  mouse pointer over a date in the DateChooser control.
 *  @default 0xE3FFD6
 */
[Style(name="rollOverColor", type="uint", format="Color", inherit="yes")]

/**
 *  Name of the class to use as the skin for the 
 *  highlight area of the date when the user holds the
 *  mouse pointer over a date in the DateChooser control.
 *
 *  @default mx.skins.halo.DateChooserIndicator
 */
[Style(name="rollOverIndicatorSkin", type="Class", inherit="no")]

/**
 *  Color of the highlight area of the currently selected date
 *  in the DateChooser control.
 *  @default 0xCDFFC1
 */
[Style(name="selectionColor", type="uint", format="Color", inherit="yes")]

/**
 *  Name of the class to use as the skin for the 
 *  highlight area of the currently selected date
 *  in the DateChooser control.
 *
 *  @default mx.skins.halo.DateChooserIndicator
 */
[Style(name="selectionIndicatorSkin", type="Class", inherit="no")]

/**
 *  Color of the background of today's date.
 *  The default value is <code>0x2B333C</code>.
 */
[Style(name="todayColor", type="uint", format="Color", inherit="yes")]

/**
 *  Name of the class to use as the skin for the 
 *  highlight area of today's date
 *  in the DateChooser control.
 *
 *  @default mx.skins.halo.DateChooserIndicator
 */
[Style(name="todayIndicatorSkin", type="Class", inherit="no")]

/**
 *  Name of the style sheet definition to configure the appearance of the current day's
 *  numeric text, which is highlighted
 *  in the control when the <code>showToday</code> property is <code>true</code>.
 *  Specify a "color" style to change the font color.
 *  If omitted, the current day text inherits
 *  the text styles of the control.
 */
[Style(name="todayStyleName", type="String", inherit="no")]

/**
 *  Name of the style sheet definition to configure the weekday names of
 *  the control. If omitted, the weekday names inherit the text
 *  styles of the control.
 */
[Style(name="weekDayStyleName", type="String", inherit="no")]

//--------------------------------------
//  Other metadata
//--------------------------------------

[AccessibilityClass(implementation="mx.accessibility.DateChooserAccImpl")]

[DefaultBindingProperty(source="selectedDate", destination="selectedDate")]

[DefaultTriggerEvent("change")]

[IconFile("DateChooser.png")]

[RequiresDataBinding(true)]

/**
 *  The DateChooser control displays the name of a month, the year,
 *  and a grid of the days of the month, with columns labeled
 *  for the day of the week.
 *  The user can select a date, a range of dates, or multiple dates.
 *  The control contains forward and back arrow buttons
 *  for changing the month and year.
 *  You can let users select multiple dates, disable the selection
 *  of certain dates, and limit the display to a range of dates.
 *
 *  @mxml
 *
 *  <p>The <code>&lt;mx:DateChooser&gt;</code> tag inherits all of the tag attributes
 *  of its superclass, and adds the following tag attributes:</p>
 *
 *  <pre>
 *  &lt;mx:DateChooser
 *    <strong>Properties</strong>
 *    allowDisjointSelection="true|false"
 *    allowMultipleSelection="false|true"
 *    dayNames="["S", "M", "T", "W", "T", "F", "S"]"
 *    disabledDays="<i>No default</i>"
 *    disabledRanges="<i>No default</i>"
 *    displayedMonth="<i>Current month</i>"
 *    displayedYear="<i>Current year</i>"
 *    firstDayOfWeek="0"
 *    maxYear="2100"
 *    minYear="1900"
 *    monthNames="["January", "February", "March", "April", "May",
 *      "June", "July", "August", "September", "October", "November",
 *      "December"]"
 *    monthSymbol=""
 *    selectableRange="<i>No default</i>"
 *    selectedDate="<i>No default</i>"
 *    selectedRanges="<i>No default</i>"
 *    showToday="true|false"
 *    yearNavigationEnabled="false|true"
 *    yearSymbol=""
 * 
 *    <strong>Styles</strong>
 *    backgroundColor="0xFFFFFF"
 *    backgroundAlpha=1.0
 *    borderColor="0xAAB3B3"
 *    borderThickness="1"
 *    color="0x0B333C"
 *    cornerRadius="0"
 *    disabledColor="0xAAB3B3"
 *    fillAlphas="[0.6, 0.4]"
 *    fillColors="[0xFFFFFF, 0xCCCCCC]"
 *    focusAlpha="0.5"
 *    focusRoundedCorners"tl tr bl br"
 *    fontAntiAliasType="advanced"
 *    fontFamily="Verdana"
 *    fontGridFitType="pixel"
 *    fontSharpness="0"
 *    fontSize="10"
 *    fontStyle="normal|italic"
 *    fontThickness="0"
 *    fontWeight="normal|bold"
 *    headerColors="[0xE1E5EB, 0xF4F5F7]"
 *    headerStyleName="<i>No default</i>"
 *    highlightAlphas="[0.3, 0.0]"
 *    horizontalGap="8"
 *    leading="2"
 *    nextMonthDisabledSkin="DateChooserMonthArrowSkin"
 *    nextMonthDownSkin="DateChooserMonthArrowSkin"
 *    nextMonthOverSkin="DateChooserMonthArrowSkin"
 *    nextMonthUpSkin="DateChooserMonthArrowSkin"
 *    nextYearDisabledSkin="DateChooserYearArrowSkin"
 *    nextYearDownSkin="DateChooserYearArrowSkin"
 *    nextYearOverSkin="DateChooserYearArrowSkin"
 *    nextYearUpSkin="DateChooserYearArrowSkin"
 *    prevMonthDisabledSkin="DateChooserMonthArrowSkin"
 *    prevMonthDownSkin="DateChooserMonthArrowSkin"
 *    prevMonthOverSkin="DateChooserMonthArrowSkin"
 *    prevMonthUpSkin="DateChooserMonthArrowSkin"
 *    prevYearDisabledSkin="DateChooserYearArrowSkin"
 *    prevYearDownSkin="DateChooserYearArrowSkin"
 *    prevYearOverSkin="DateChooserYearArrowSkin"
 *    prevYearUpSkin="DateChooserYearArrowSkin"
 *    rollOverColor="0xEEFEE6"
 *    rollOverIndicatorSkin="DateChooserIndicator"
 *    selectionColor="0xB7F39B"
 *    selectionIndicatorSkin="DateChooserIndicator"
 *    textAlign="left|right|center"
 *    textDecoration="none|underline"
 *    textIndent="0"
 *    todayColor="0x2B333C"
 *    todayIndicatorSkin="DateChooserIndicator"
 *    todayStyleName="<i>White</i>"
 *    verticalGap="6"
 *    weekDayStyleName="<i>Bold</i>"
 * 
 *    <strong>Events</strong>
 *    change="<i>No default</i>"
 *    scroll="<i>No default</i>"
 *  /&gt;
 *  </pre>
 *
 *  @see mx.controls.DateField
 *
 *  @helpid 3602
 *  @tiptext DateChooser enables a user to select a date
 *  @includeExample examples/DateChooserExample.mxml
 */
public class DateChooser extends UIComponent implements IFocusManagerComponent
{
    include "../core/Version.as";

    //--------------------------------------------------------------------------
    //
    //  Class initialization
    //
    //--------------------------------------------------------------------------

    loadResources();

    //--------------------------------------------------------------------------
    //
    //  Class constants
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    private var HEADER_WIDTH_PAD:Number = 5;

    /**
     *  @private
     *  Width pad month skins
     */
    private var SKIN_WIDTH_PAD:Number = 6; 

    /**
     *  @private
     */
    private var SKIN_HEIGHT_PAD:Number = 4;

    /**
     *  @private
     *  Padding between buttons and also at the sides.
     */
    private var YEAR_BUTTONS_PAD:Number = 6; 
    
    //--------------------------------------------------------------------------
    //
    //  Class mixins
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Placeholder for mixin by DateChooserAccImpl.
     */
    mx_internal static var createAccessibilityImplementation:Function;

    //--------------------------------------------------------------------------
    //
    //  Class resources
    //
    //--------------------------------------------------------------------------

    [ResourceBundle("SharedResources")]

    /**
     *  @private
     */
    private static var sharedResources:ResourceBundle;

    [ResourceBundle("controls")]

    /**
     *  @private
     */
    private static var packageResources:ResourceBundle;

    /**
     *  @private
     */
    private static var resourceDayNames:Array;

    /**
     *  @private
     */
    private static var resourceDateFormat:String;
    
    /**
     *  @private
     */
    private static var resourceMonthNames:Array;

    /**
     *  @private
     */
    private static var resourceFirstDayOfWeek:Number;

    /**
     *  @private
     */
    private static var resourceYearSymbol:String;

    /**
     *  @private
     */
    private static var resourceMonthSymbol:String;
    
    //--------------------------------------------------------------------------
    //
    //  Class methods
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Loads resources for this class.
     */
    private static function loadResources():void
    {
        resourceMonthNames = sharedResources.getStringArray("monthNames");

        resourceDateFormat = sharedResources.getString("dateFormat");
        
        resourceMonthSymbol = sharedResources.getString("monthSymbol");
        
        resourceYearSymbol = packageResources.getString("yearSymbol");
                    
        resourceDayNames = packageResources.getStringArray("dayNamesShortest");

        resourceFirstDayOfWeek = packageResources.getNumber("firstDayOfWeek");
    }

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     *  Constructor.
     */
    public function DateChooser()
    {
        super();

        bundleChanged();

        tabEnabled = true;
        tabChildren = false;
    }

    //--------------------------------------------------------------------------
    //
    //  Variables
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    mx_internal var background:UIComponent;
    
    /**
     *  @private
     */
    mx_internal var border:UIComponent;
    
    /**
     *  @private
     */
    mx_internal var headerDisplay:UIComponent;
    
    /**
     *  @private
     *  The internal UITextField that displays
     *  the currently visible month and year.
     */
    mx_internal var monthDisplay:UITextField;
    
    /**
     *  @private
     */
    mx_internal var fwdMonthHit:Sprite;
    
    /**
     *  @private
     */
    mx_internal var backMonthHit:Sprite;
    
    /**
     *  @private
     */
    mx_internal var upYearHit:Sprite;
    
    /**
     *  @private
     */
    mx_internal var downYearHit:Sprite;
    
    /**
     *  @private
     */
    mx_internal var calHeader:UIComponent;
    
    /**
     *  @private
     */
    mx_internal var yearDisplay:UITextField;
    
    /**
     *  @private
     *  The internal Button which, when clicked,
     *  makes the DateChooser display the next year.
     */
    mx_internal var upYearButton:Button;
    
    /**
     *  @private
     *  The internal Button which, when clicked,
     *  makes the DateChooser display the previous year.
     */
    mx_internal var downYearButton:Button;
    
    /**
     *  @private
     *  The internal Button which, when clicked,
     *  makes the DateChooser display the next month.
     */
    mx_internal var fwdMonthButton:Button;
    
    /**
     *  @private
     *  The internal Button which, when clicked,
     *  makes the DateChooser display the previous month.
     */
    mx_internal var backMonthButton:Button;
    
    /**
     *  @private
     *  The internal CalendarLayout that displays the grid of dates.
     */
    mx_internal var dateGrid:CalendarLayout;

    /**
     *  @private
     */
    mx_internal var dropShadow:RectangularDropShadow;

    /**
     *  @private
     */
    private var previousSelectedCellIndex:Number = NaN;

    /**
     *  @private
     */
    private var monthSkinWidth:Number = 6;

    /**
     *  @private
     */
    private var monthSkinHeight:Number = 11;

    /**
     *  @private
     */
    private var yearSkinWidth:Number = 10;

    /**
     *  @private
     */
    private var yearSkinHeight:Number = 8;

    /**
     *  @private
     */
    private var headerHeight:Number = 30;

    //--------------------------------------------------------------------------
    //
    //  Overridden properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  enabled
    //----------------------------------

    /**
     *  @private
     *  Storage for the enabled property.
     */
    private var _enabled:Boolean = true;
    
    /**
     *  @private
     */
    private var enabledChanged:Boolean = false;

    [Bindable("enabledChanged")]
    [Inspectable(category="General", enumeration="true,false", defaultValue="true")]

    /**
     *  @private
     */
    override public function get enabled():Boolean
    {
        return _enabled;
    }

    /**
     *  @private
     */
    override public function set enabled(value:Boolean):void
    {
        if (value == _enabled)
            return;

        _enabled = value;
        super.enabled = value;
        enabledChanged = true;

        invalidateProperties();
    }

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  allowDisjointSelection
    //----------------------------------

    /**
     *  @private
     *  Storage for the allowDisjointSelection property.
     */
    private var _allowDisjointSelection:Boolean = true;

    /**
     *  @private
     */
    private var allowDisjointSelectionChanged:Boolean = false;

    [Bindable("allowDisjointSelectionChanged")]
    [Inspectable(category="General", defaultValue="true")]

    /**
     *  If <code>true</code>, specifies that non-contiguous(disjoint)
     *  selection is allowed in the DateChooser control.
     *  This property has an effect only if the
     *  <code>allowMultipleSelection</code> property is <code>true</code>.
     *  Setting this property changes the appearance of the
     *  DateChooser control.
     *
     *  @default true;
     *  @helpid
     *  @tiptext Non-contiguous is allowed if true
     */
    public function get allowDisjointSelection():Boolean
    {
        return _allowDisjointSelection;
    }

    /**
     *  @private
     */
    public function set allowDisjointSelection(value:Boolean):void
    {
        _allowDisjointSelection = value;
        allowDisjointSelectionChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  allowMultipleSelection
    //----------------------------------

    /**
     *  @private
     *  Storage for the allowMultipleSelection property.
     */
    private var _allowMultipleSelection:Boolean = false;

    /**
     *  @private
     */
    private var allowMultipleSelectionChanged:Boolean = false;

    [Bindable("allowMultipleSelectionChanged")]
    [Inspectable(category="General", defaultValue="false")]

    /**
     *  If <code>true</code>, specifies that multiple selection
     *  is allowed in the DateChooser control.
     *  Setting this property changes the appearance of the DateChooser control.
     *
     *  @default false
     *  @helpid
     *  @tiptext Multiple selection is allowed if true
     */
    public function get allowMultipleSelection():Boolean
    {
        return _allowMultipleSelection;
    }

    /**
     *  @private
     */
    public function set allowMultipleSelection(value:Boolean):void
    {
        _allowMultipleSelection = value;
        allowMultipleSelectionChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  dayNames
    //----------------------------------

    /**
     *  @private
     *  Storage for the dayNames property.
     */
    private var _dayNames:Array;

    /**
     *  @private
     */
    private var dayNamesChanged:Boolean = false;

    [Bindable("dayNamesChanged")]
    [Inspectable(arrayType="String", defaultValue="S,M,T,W,T,F,S")]

    /**
     *  The weekday names for DateChooser control.
     *  Changing this property changes the day labels
     *  of the DateChooser control.
     *  Sunday is the first day (at index 0).
     *  The rest of the week names follow in the normal order.
     *
     *  @default [ "S", "M", "T", "W", "T", "F", "S" ].
     *  @helpid 3607
     *  @tiptext The names of days of week in a DateChooser
     */
    public function get dayNames():Array
    {
        return _dayNames;
    }

    /**
     *  @private
     */
    public function set dayNames(value:Array):void
    {
        _dayNames = value;
        dayNamesChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  disabledDays
    //----------------------------------

    /**
     *  @private
     *  Storage for the disabledDays property.
     */
    private var _disabledDays:Array = [];

    /**
     *  @private
     */
    private var disabledDaysChanged:Boolean = false;

    [Bindable("disabledDaysChanged")]
    [Inspectable(arrayType="Date")]

    /**
     *  The days to disable in a week.
     *  All the dates in a month, for the specified day, are disabled.
     *  This property changes the appearance of the DateChooser control.
     *  The elements of this array can have values from 0 (Sunday) to
     *  6 (Saturday).
     *  For example, a value of <code>[ 0, 6 ]</code>
     *  disables Sunday and Saturday.
     *
     *  @default []
     *  @helpid 3608
     *  @tiptext The disabled days in a week
     */
    public function get disabledDays():Array
    {
        return _disabledDays;
    }

    /**
     *  @private
     */
    public function set disabledDays(value:Array):void
    {
        _disabledDays = value;
        disabledDaysChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  disabledRanges
    //----------------------------------

    /**
     *  @private
     *  Storage for the disabledRanges property.
     */
    private var _disabledRanges:Array = [];

    /**
     *  @private
     */
    private var disabledRangesChanged:Boolean = false;

    [Bindable("disabledRangesChanged")]
    [Inspectable(arrayType="Object")]

    /**
     *  Disables single and multiple days.
     *
     *  <p>This property accepts an Array of objects as a parameter.
     *  Each object in this array is a Date object, specifying a
     *  single day to disable; or an object containing either or both
     *  of the <code>rangeStart</code> and <code>rangeEnd</code> properties,
     *  each of whose values is a Date object.
     *  The value of these properties describes the boundaries
     *  of the date range.
     *  If either is omitted, the range is considered
     *  unbounded in that direction.
     *  If you specify only <code>rangeStart</code>,
     *  all the dates after the specified date are disabled,
     *  including the <code>rangeStart</code> date.
     *  If you specify only <code>rangeEnd</code>,
     *  all the dates before the specified date are disabled,
     *  including the <code>rangeEnd</code> date.
     *  To disable a single day, use a single Date object specifying a date
     *  in the Array.</p>
     *
     *  <p>The following example, disables the following dates: January 11
     *  2006, the range January 23 - February 10 2006, and March 1 2006
     *  and all following dates.</p>
     *
     *  <p><code>disabledRanges="{[ new Date(2006,0,11), {rangeStart:
     *  new Date(2006,0,23), rangeEnd: new Date(2006,1,10)},
     *  {rangeStart: new Date(2006,2,1)} ]}"</code></p>
     *
     *  @default []
     *  @helpid 3610
     *  @tiptext The disabled dates inside the selectableRange
     */
    public function get disabledRanges():Array
    {
        return _disabledRanges;
    }

    /**
     *  @private
     */
    public function set disabledRanges(value:Array):void
    {
        _disabledRanges = value;
        disabledRangesChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  displayedMonth
    //----------------------------------

    /**
     *  @private
     *  Storage for the displayedMonth property.
     */
    private var _displayedMonth:int = (new Date()).getMonth();

    /**
     *  @private
     */
    private var displayedMonthChanged:Boolean = false;

    [Bindable("scroll")]
    [Bindable("viewChanged")]
    [Inspectable(category="General")]

    /**
     *  Used together with the <code>displayedYear</code> property,
     *  the <code>displayedMonth</code> property specifies the month
     *  displayed in the DateChooser control.
     *  Month numbers are zero-based, so January is 0 and December is 11.
     *  Setting this property changes the appearance of the DateChooser control.
     *
     *  <p>The default value is the current month.</p>
     *
     *  @helpid 3605
     *  @tiptext The currently displayed month in the DateChooser
     */
    public function get displayedMonth():int
    {
        if (dateGrid && _displayedMonth != dateGrid.displayedMonth)
            return dateGrid.displayedMonth;
        else
            return _displayedMonth;
    }

    /**
     *  @private
     */
    public function set displayedMonth(value:int):void
    {
        if (isNaN(value) || _displayedMonth == value)
            return;
        
        _displayedMonth = value;
        displayedMonthChanged = true;
        
        invalidateProperties();
        
        if (dateGrid)
            dateGrid.displayedMonth = value; // if it's already this value shouldn't do anything
    }

    //----------------------------------
    //  displayedYear
    //----------------------------------

    /**
     *  @private
     *  Storage for the displayedYear property.
     */
    private var _displayedYear:int = (new Date()).getFullYear();

    /**
     *  @private
     */
    private var displayedYearChanged:Boolean = false;

    [Bindable("scroll")]
    [Bindable("viewChanged")]
    [Inspectable(category="General")]

    /**
     *  Used together with the <code>displayedMonth</code> property,
     *  the <code>displayedYear</code> property specifies the month
     *  displayed in the DateChooser control.
     *  Setting this property changes the appearance of the DateChooser control.
     *
     *  <p>The default value is the current year.</p>
     *
     *  @helpid 3606
     *  @tiptext The currently displayed year in DateChooser
     */
    public function get displayedYear():int
    {
        if (dateGrid)
            return dateGrid.displayedYear;
        else
            return _displayedYear;
    }

    /**
     *  @private
     */
    public function set displayedYear(value:int):void
    {
        if (isNaN(value) || _displayedYear == value)
            return;
        
        _displayedYear = value;
        displayedYearChanged = true;
        
        invalidateProperties();
        
        if (dateGrid)
            dateGrid.displayedYear = value;// if it's already this value shouldn't do anything
    }

    //----------------------------------
    //  firstDayOfWeek
    //----------------------------------

    /**
     *  @private
     *  Storage for the firstDayOfWeek property.
     */
    private var _firstDayOfWeek:int = 0;

    /**
     *  @private
     */
    private var firstDayOfWeekChanged:Boolean = false;

    /**
     *  @private
     */
    private var firstDayOfWeekSet:Boolean = false;

    [Bindable("firstDayOfWeekChanged")]
    [Inspectable(defaultValue="0")]

    /**
     *  Number representing the day of the week to display in the
     *  first column of the DateChooser control.
     *  The value must be in the range 0 to 6, where 0 corresponds to Sunday,
     *  the first element of the <code>dayNames</code> Array.
     *
     *  <p>Setting this property changes the order of the day columns.
     *  For example, setting it to 1 makes Monday the first column
     *  in the control.</p>
     *
     *  @default 0 (Sunday)
     *  @tiptext Sets the first day of week for DateChooser
     */
    public function get firstDayOfWeek():int
    {
        return _firstDayOfWeek;
    }

    /**
     *  @private
     */
    public function set firstDayOfWeek(value:int):void
    {
        _firstDayOfWeek = value;
        firstDayOfWeekChanged = true;
        firstDayOfWeekSet = true;

        invalidateProperties();
    }

    //----------------------------------
    //  maxYear
    //----------------------------------

    /**
     *  @private
     *  Storage for the maxYear property.
     */
    private var _maxYear:int = 2100;

    /**
     *  The last year selectable in the control.
     *
     *  @default 2100
     *  @helpid
     *  @tiptext Maximum year limit
     */
    public function get maxYear():int
    {
        return _maxYear;
    }

    /**
     *  @private
     */
    public function set maxYear(value:int):void
    {
        if (_maxYear == value)
            return;

        _maxYear = value;
    }

    //----------------------------------
    //  minYear
    //----------------------------------

    /**
     *  @private
     *  Storage for the minYear property.
     */
    private var _minYear:int = 1900;

    /**
     *  The first year selectable in the control.
     *
     *  @default 1900
     *  @helpid
     *  @tiptext Minimum year limit
     */
    public function get minYear():int
    {
        return _minYear;
    }

    /**
     *  @private
     */
    public function set minYear(value:int):void
    {
        if (_minYear == value)
            return;

        _minYear = value;
    }

    //----------------------------------
    //  monthNames
    //----------------------------------

    /**
     *  @private
     *  Storage for the monthNames property.
     */
    private var _monthNames:Array;
    
    /**
     *  @private
     */
    private var monthNamesChanged:Boolean = false;

    [Bindable("monthNamesChanged")]
    [Inspectable(arrayType="String", defaultValue="January,February,March,April,May,June,July,August,September,October,November,December")]

    /**
     *  Names of the months displayed at the top of the DateChooser control.
     *  The <code>monthSymbol</code> property is appended to the end of 
     *  the value specified by the <code>monthNames</code> property, 
     *  which is useful in languages such as Japanese.
     *
     *  @default [ "January", "February", "March", "April", "May", "June", 
     *  "July", "August", "September", "October", "November", "December" ]
     *  @tiptext The name of the months displayed in the DateChooser
     */
    public function get monthNames():Array
    {
        return _monthNames;
    }

    /**
     *  @private
     */
    public function set monthNames(value:Array):void
    {
        _monthNames = value;
        monthNamesChanged = true;

        invalidateProperties();
        invalidateSize();
    }

    //----------------------------------
    //  monthSymbol
    //----------------------------------

    /**
     *  @private
     *  Storage for the monthNavigationEnabled property.
     */
    private var _monthSymbol:String = "";

    /**
     *  @private
     */
    private var monthSymbolChanged:Boolean = false;

    [Bindable("monthSymbolChanged")]
    [Inspectable(defaultValue="")]

    /**
     *  This property is appended to the end of the value specified 
     *  by the <code>monthNames</code> property to define the names 
     *  of the months displayed at the top of the DateChooser control.
     *  Some languages, such as Japanese, use an extra 
     *  symbol after the month name. 
     *
     *  @default ""
     */
    public function get monthSymbol():String
    {
        return _monthSymbol;
    }

    /**
     *  @private
     */
    public function set monthSymbol(value:String):void
    {
        _monthSymbol = value;
        monthSymbolChanged = true;

        invalidateProperties();
    }
        
    //----------------------------------
    //  selectableRange
    //----------------------------------

    /**
     *  @private
     *  Storage for the selectableRange property.
     */
    private var _selectableRange:Object;

    /**
     *  @private
     */
    private var selectableRangeChanged:Boolean = false;

    [Bindable("selectableRangeChanged")]

    /**
     *  Range of dates between which dates are selectable.
     *  For example, a date between 04-12-2006 and 04-12-2007
     *  is selectable, but dates out of this range are disabled.
     *
     *  <p>This property accepts an Object as a parameter.
     *  The Object contains two properties, <code>rangeStart</code>
     *  and <code>rangeEnd</code>, of type Date.
     *  If you specify only <code>rangeStart</code>,
     *  all the dates after the specified date are enabled.
     *  If you only specify <code>rangeEnd</code>,
     *  all the dates before the specified date are enabled.
     *  To enable only a single day in a DateChooser control,
     *  you can pass a Date object directly.</p>
     *
     *  <p>The following example enables only the range
     *  January 1, 2006 through June 30, 2006. Months before January
     *  and after June do not appear in the DateChooser.</p>
     *
     *  <p><code>selectableRange="{{rangeStart : new Date(2006,0,1),
     *  rangeEnd : new Date(2006,5,30)}}"</code></p>
     *
     *  @default null
     *  @helpid 3609
     *  @tiptext The start and end dates between which a date can be selected
     */
    public function get selectableRange():Object
    {
        return _selectableRange;
    }

    /**
     *  @private
     */
    public function set selectableRange(value:Object):void
    {
        _selectableRange = value;
        selectableRangeChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  selectedDate
    //----------------------------------

    /**
     *  @private
     *  Storage for the selectedDate property.
     */
    private var _selectedDate:Date;

    /**
     *  @private
     */
    private var selectedDateChanged:Boolean = false;

    [Bindable("change")]
    [Bindable("valueCommit")]
    [Inspectable(category="General")]

    /**
     *  Date selected in the DateChooser control.
     *
     *  <p>Selecting the currently selected date in the control deselects it, 
     *  sets the <code>selectedDate</code> property to <code>null</code>, 
     *  and then dispatches the <code>change</code> event.</p>
     *
     *  @default null
     *  @helpid 3611
     *  @tiptext The selected date in DateChooser
     */
    public function get selectedDate():Date
    {
        return _selectedDate;
    }

    /**
     *  @private
     */
    public function set selectedDate(value:Date):void
    {
        _selectedDate = value;
        selectedDateChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  selectedRanges
    //----------------------------------

    /**
     *  @private
     *  Storage for the selectedRanges property.
     */
    private var _selectedRanges:Array = [];

    /**
     *  @private
     */
    private var selectedRangesChanged:Boolean = false;

    [Bindable("change")]
    [Bindable("valueCommit")]
    [Inspectable(arrayType="Date")]

    /**
     *  Selected date ranges.
     *
     *  <p>This property accepts an Array of objects as a parameter.
     *  Each object in this array has two date Objects,
     *  <code>rangeStart</code> and <code>rangeEnd</code>.
     *  The range of dates between each set of <code>rangeStart</code>
     *  and <code>rangeEnd</code> (inclusive) are selected.
     *  To select a single day, set both <code>rangeStart</code> and <code>rangeEnd</code>
     *  to the same date.</p>
     * 
     *  <p>The following example, selects the following dates: January 11
     *  2006, the range January 23 - February 10 2006. </p>
     *
     *  <p><code>selectedRanges="{[ {rangeStart: new Date(2006,0,11),
     *  rangeEnd: new Date(2006,0,11)}, {rangeStart:new Date(2006,0,23),
     *  rangeEnd: new Date(2006,1,10)} ]}"</code></p>
     *
     *  @default []
     *  @helpid 0000
     *  @tiptext The selected dates
     */
    public function get selectedRanges():Array
    {
        _selectedRanges = dateGrid.selectedRanges;
        return _selectedRanges;
    }

    /**
     *  @private
     */
    public function set selectedRanges(value:Array):void
    {
        _selectedRanges = value;
        selectedRangesChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  showToday
    //----------------------------------

    /**
     *  @private
     *  Storage for the showToday property.
     */
    private var _showToday:Boolean = true;

    /**
     *  @private
     */
    private var showTodayChanged:Boolean = false;

    [Bindable("showTodayChanged")]
    [Inspectable(category="General", defaultValue="true")]

    /**
     *  If <code>true</code>, specifies that today is highlighted
     *  in the DateChooser control.
     *  Setting this property changes the appearance of the DateChooser control.
     *
     *  @default true
     *  @helpid 3603
     *  @tiptext The highlight on the current day of the month
     */
    public function get showToday():Boolean
    {
        return _showToday;
    }

    /**
     *  @private
     */
    public function set showToday(value:Boolean):void
    {
        _showToday = value;
        showTodayChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  yearNavigationEnabled
    //----------------------------------

    /**
     *  @private
     *  Storage for the yearNavigationEnabled property.
     */
    private var _yearNavigationEnabled:Boolean = false;

    /**
     *  @private
     */
    private var yearNavigationEnabledChanged:Boolean = false;

    [Bindable("yearNavigationEnabledChanged")]
    [Inspectable(defaultValue="false")]

    /**
     *  Enables year navigation. When <code>true</code>
     *  an up and down button appear to the right
     *  of the displayed year. You can use these buttons
     *  to change the current year.
     *  These button appear to the left of the year in locales where year comes 
     *  before the month in the date format.
     *
     *  @default false
     *  @tiptext Enables yearNavigation
     */
    public function get yearNavigationEnabled():Boolean
    {
        return _yearNavigationEnabled;
    }

    /**
     *  @private
     */
    public function set yearNavigationEnabled(value:Boolean):void
    {
        _yearNavigationEnabled = value;
        yearNavigationEnabledChanged = true;

        invalidateProperties();
    }

    //----------------------------------
    //  yearSymbol
    //----------------------------------

    /**
     *  @private
     *  Storage for the yearNavigationEnabled property.
     */
    private var _yearSymbol:String = "";

    /**
     *  @private
     */
    private var yearSymbolChanged:Boolean = false;

    [Bindable("yearSymbolChanged")]
    [Inspectable(defaultValue="")]

    /**
     *  This property is appended to the end of the year 
     *  displayed at the top of the DateChooser control.
     *  Some languages, such as Japanese, 
     *  add a symbol after the year. 
     *
     *  @default ""
     */
    public function get yearSymbol():String
    {
        return _yearSymbol;
    }

    /**
     *  @private
     */
    public function set yearSymbol(value:String):void
    {
        _yearSymbol = value;
        yearSymbolChanged = true;

        invalidateProperties();
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden methods: UIComponent
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    override protected function initializeAccessibility():void
    {
        if (DateChooser.createAccessibilityImplementation != null)
            DateChooser.createAccessibilityImplementation(this);
    }

    /**
     *  @private
     *  Create subobjects in the component. This method creates textfields for
     *  dates in a month, month scroll buttons, header row, background and border.
     */
    override protected function createChildren():void
    {
        super.createChildren();

        if (!background)
        {
            background = new UIComponent();
            addChild(background);
            UIComponent(background).styleName = this;
        }

        if (!border)
        {
            border = new UIComponent();
            addChild(border);
            UIComponent(border).styleName = this;
        }

        // Create the dateGrid.
        // This must be created before calling updateDateDisplay().
        if (!dateGrid)
        {
            dateGrid = new CalendarLayout();
            dateGrid.styleName = this;
            addChild(dateGrid);
            dateGrid.addEventListener(CalendarLayoutChangeEvent.CHANGE,
                                      dateGrid_changeHandler);
            dateGrid.addEventListener(DateChooserEvent.SCROLL,
                                      dateGrid_scrollHandler);
        }

        if (!calHeader)
        {
            calHeader = new UIComponent();
            addChild(calHeader);
            UIComponent(calHeader).styleName = this;
        }

        if (!monthDisplay)
        {
            monthDisplay = new UITextField();
            var textFormat:TextFormat = determineTextFormatFromStyles();
            monthDisplay.defaultTextFormat = textFormat;
            monthDisplay.visible = false;
            monthDisplay.selectable = false;
            addChild(monthDisplay);
            var dateHeaderStyleName:Object = getStyle("headerStyleName");
            if (!dateHeaderStyleName)
                dateHeaderStyleName = this;
            monthDisplay.styleName = dateHeaderStyleName;
            setMonthWidth();
        }

        if (!yearDisplay)
        {
            yearDisplay = new UITextField();
            textFormat = determineTextFormatFromStyles();
            yearDisplay.defaultTextFormat = textFormat;
            yearDisplay.visible = false;
            yearDisplay.selectable = false;
            addChild(yearDisplay);
            dateHeaderStyleName = getStyle("headerStyleName");
            if (!dateHeaderStyleName)
                dateHeaderStyleName = this;
            yearDisplay.styleName = dateHeaderStyleName;
        }

        if (_yearNavigationEnabled)
            getYearNavigationButtons();

       // Create the next-month button.
        if (!fwdMonthButton)
        {
            fwdMonthButton = new Button();
            fwdMonthButton.styleName = this;
            fwdMonthButton.autoRepeat = true;
            fwdMonthButton.focusEnabled = false;
            fwdMonthButton.upSkinName = "nextMonthUpSkin";
            fwdMonthButton.overSkinName = "nextMonthOverSkin";
            fwdMonthButton.downSkinName = "nextMonthDownSkin";
            fwdMonthButton.disabledSkinName = "nextMonthDisabledSkin";
            fwdMonthButton.upIconName = "";
            fwdMonthButton.overIconName = "";
            fwdMonthButton.downIconName = "";
            fwdMonthButton.disabledIconName = "";
            fwdMonthButton.addEventListener(FlexEvent.BUTTON_DOWN,
                                            fwdMonthButton_buttonDownHandler);
            addChild(fwdMonthButton);
        }

        // Create the previous-month button.
        if (!backMonthButton)
        {
            backMonthButton = new Button();
            backMonthButton.styleName = this;
            backMonthButton.focusEnabled = false;
            backMonthButton.autoRepeat = true;
            backMonthButton.upSkinName = "prevMonthUpSkin";
            backMonthButton.overSkinName = "prevMonthOverSkin";
            backMonthButton.downSkinName = "prevMonthDownSkin";
            backMonthButton.disabledSkinName = "prevMonthDisabledSkin";
            backMonthButton.upIconName = "";
            backMonthButton.overIconName = "";
            backMonthButton.downIconName = "";
            backMonthButton.disabledIconName = "";
            backMonthButton.addEventListener(FlexEvent.BUTTON_DOWN,
                                             backMonthButton_buttonDownHandler);
            addChild(backMonthButton);
        }

        if (!fwdMonthHit)
        {
            fwdMonthHit = new FlexSprite();
            fwdMonthHit.name = "fwdMonthHit";
            addChild(fwdMonthHit);
            fwdMonthHit.visible = false;
            fwdMonthButton.hitArea = fwdMonthHit;
        }

        if (!backMonthHit)
        {
            backMonthHit = new FlexSprite();
            backMonthHit.name = "backMonthHit";
            addChild(backMonthHit);
            backMonthHit.visible = false;
            backMonthButton.hitArea = backMonthHit;
        }
    }

    /**
     *  @private
     */
    override protected function commitProperties():void
    {
        super.commitProperties();

        if (showTodayChanged)
        {
            showTodayChanged = false;
            dateGrid.showToday = _showToday;
            dispatchEvent(new Event("showTodayChanged"));
        }

        if (enabledChanged)
        {
            enabledChanged = false;
            fwdMonthButton.enabled = _enabled;
            backMonthButton.enabled = _enabled;
            monthDisplay.enabled = _enabled;
            yearDisplay.enabled = _enabled;
            if (_yearNavigationEnabled)
            {
                upYearButton.enabled = _enabled;
                downYearButton.enabled = _enabled;
            }
            dateGrid.enabled = _enabled;
            dispatchEvent(new Event("enabledChanged"));
        }

        if (firstDayOfWeekChanged)
        {
            firstDayOfWeekChanged = false;
            dateGrid.firstDayOfWeek = _firstDayOfWeek;
            dispatchEvent(new Event("firstDayOfWeekChanged"));
        }

        if (displayedMonthChanged)
        {
            displayedMonthChanged = false;
            dateGrid.displayedMonth = _displayedMonth;
            invalidateDisplayList();
            dispatchEvent(new Event("viewChanged"));
        }

        if (displayedYearChanged)
        {
            displayedYearChanged = false;
            dateGrid.displayedYear = _displayedYear;
            invalidateDisplayList();
            dispatchEvent(new Event("viewChanged"));
        }

        if (dayNamesChanged)
        {
            dayNamesChanged = false;
            dateGrid.dayNames = dayNames.slice(0);
            dispatchEvent(new Event("dayNamesChanged"));
        }

        if (disabledDaysChanged)
        {
            disabledDaysChanged = false;
            dateGrid.disabledDays = _disabledDays.slice(0);
            dispatchEvent(new Event("disabledDaysChanged"));
        }

        if (selectableRangeChanged)
        {
            selectableRangeChanged = false;
            dateGrid.selectableRange = _selectableRange is Array ? _selectableRange.slice(0) : _selectableRange;
            dispatchEvent(new Event("selectableRangeChanged"));
            invalidateDisplayList();
        }

        if (disabledRangesChanged)
        {
            disabledRangesChanged = false;
            dateGrid.disabledRanges = _disabledRanges.slice(0);
            dispatchEvent(new Event("disabledRangesChanged"));
        }

        if (selectedDateChanged)
        {
            selectedDateChanged = false;
            dateGrid.selectedDate = _selectedDate;
            invalidateDisplayList();
            dispatchEvent(new FlexEvent(FlexEvent.VALUE_COMMIT));
        }

        if (selectedRangesChanged)
        {
            selectedRangesChanged = false;
            dateGrid.selectedRanges = _selectedRanges;
            invalidateDisplayList();
            dispatchEvent(new FlexEvent(FlexEvent.VALUE_COMMIT));
        }

        if (allowMultipleSelectionChanged)
        {
            allowMultipleSelectionChanged = false;
            dateGrid.allowMultipleSelection = _allowMultipleSelection;
            invalidateDisplayList();
            dispatchEvent(new Event("allowMultipleSelectionChanged"));
        }

        if (allowDisjointSelectionChanged)
        {
            allowDisjointSelectionChanged = false;
            dateGrid.allowDisjointSelection = _allowDisjointSelection;
            invalidateDisplayList();
            dispatchEvent(new Event("allowDisjointSelectionChanged"));
        }

        if (monthNamesChanged)
        {
            monthNamesChanged = false;
            if (_monthSymbol)
                for(var i:uint=0; i<_monthNames.length; i++)
                    _monthNames[i] += _monthSymbol;    
            setMonthWidth();
            invalidateDisplayList();
            dispatchEvent(new Event("monthNamesChanged"));
        }

        if (yearNavigationEnabledChanged)
        {
            if (_yearNavigationEnabled)
            {
                getYearNavigationButtons();
            }
            else if (upYearButton && downYearButton)
            {
                removeChild(upYearButton);
                removeChild(downYearButton);
                removeChild(upYearHit);
                removeChild(downYearHit);
                upYearButton = null;
                downYearButton = null;
                upYearHit = null;
                downYearHit = null;
            }
            yearNavigationEnabledChanged = false;
            invalidateSize();
            invalidateDisplayList();
            dispatchEvent(new Event("yearNavigationEnabledChanged"));
        }
        
        if (yearSymbolChanged)
        {
            yearSymbolChanged = false;
            invalidateSize();
            invalidateDisplayList();
            dispatchEvent(new Event("yearSymbolChanged"));
        }
        
        if (monthSymbolChanged)
        {
            monthSymbolChanged = false;
            invalidateSize();
            invalidateDisplayList();
            dispatchEvent(new Event("monthSymbolChanged"));
        }       
    }

    /**
     *  @private
     */
    override protected function measure():void
    {
        super.measure();

        updateDateDisplay();
        setMonthWidth();

        var borderThickness:Number = getStyle("borderThickness");

        // Wait until the initial style values have been set on this element,
        // and then pass those initial values down to my children
        monthSkinWidth = fwdMonthButton.getExplicitOrMeasuredWidth();
        monthSkinHeight = fwdMonthButton.getExplicitOrMeasuredHeight();
        if (_yearNavigationEnabled)
        {
            yearSkinWidth = upYearButton.getExplicitOrMeasuredWidth();
            yearSkinHeight = upYearButton.getExplicitOrMeasuredHeight();
        }
        else
        {
            yearSkinWidth = 0;
            yearSkinHeight = 0;
        }
        headerHeight = Math.max(monthSkinHeight,
            monthDisplay.getExplicitOrMeasuredHeight())
            + SKIN_HEIGHT_PAD * 2;

        //monthDisplay.width = dateGrid.getExplicitOrMeasuredWidth() - allPads - yearWidth;

        measuredWidth = Math.max(dateGrid.getExplicitOrMeasuredWidth()
            + borderThickness*2,
            monthDisplay.width + yearDisplay.getExplicitOrMeasuredWidth() +
            HEADER_WIDTH_PAD + yearSkinWidth + YEAR_BUTTONS_PAD +
            (monthSkinWidth + SKIN_WIDTH_PAD * 2) * 2);
        measuredHeight = headerHeight + dateGrid.getExplicitOrMeasuredHeight() + borderThickness * 2;
        measuredMinWidth = dateGrid.minWidth;
        measuredMinHeight = dateGrid.minHeight + headerHeight;
    }

    /**
     *  @private
     */
    override protected function updateDisplayList(unscaledWidth:Number,
                                                  unscaledHeight:Number):void
    {
        super.updateDisplayList(unscaledWidth, unscaledHeight);

        updateDateDisplay();

        var borderThickness:Number = getStyle("borderThickness");
        var cornerRadius:Number = getStyle("cornerRadius");
        var borderColor:Number = getStyle("borderColor");

        var w:Number = unscaledWidth - borderThickness*2;
        var h:Number = unscaledHeight - borderThickness*2;

        // Wait until the initial style values have been set on this element,
        // and then pass those initial values down to my children
        monthSkinWidth = fwdMonthButton.getExplicitOrMeasuredWidth();
        monthSkinHeight = fwdMonthButton.getExplicitOrMeasuredHeight();

        var monthHeight:Number = monthDisplay.getExplicitOrMeasuredHeight();
        var yearWidth:Number = yearDisplay.getExplicitOrMeasuredWidth();
        if (_yearNavigationEnabled)
        {
            yearSkinWidth = upYearButton.getExplicitOrMeasuredWidth();
            yearSkinHeight = upYearButton.getExplicitOrMeasuredHeight();
        }
        var swapOrder:Boolean = yearBeforeMonth(resourceDateFormat);
        var yearX:Number;
        if (swapOrder)
            yearX = borderThickness + monthSkinWidth + SKIN_WIDTH_PAD + HEADER_WIDTH_PAD;
        else
            yearX = w - (monthSkinWidth + HEADER_WIDTH_PAD +
                yearSkinWidth + YEAR_BUTTONS_PAD) + borderThickness;
        var dateHeight:Number = borderThickness + (headerHeight - monthHeight) / 2;

        var allPads:Number = HEADER_WIDTH_PAD + yearSkinWidth +
            (monthSkinWidth + SKIN_WIDTH_PAD * 2) * 2;

        monthDisplay.setActualSize(Math.max(w - allPads - yearWidth, 0), monthHeight);
        if (swapOrder)
            monthDisplay.move(yearX + yearWidth + YEAR_BUTTONS_PAD + yearSkinWidth, dateHeight);
        else
            monthDisplay.move(borderThickness + monthSkinWidth + SKIN_WIDTH_PAD + HEADER_WIDTH_PAD, dateHeight);
        monthDisplay.visible = true;

        yearDisplay.setActualSize(yearWidth + YEAR_BUTTONS_PAD, monthHeight);
        if (swapOrder)
            yearDisplay.move(yearX + YEAR_BUTTONS_PAD, dateHeight);
        else
            yearDisplay.move(yearX - yearWidth - YEAR_BUTTONS_PAD, dateHeight);
        yearDisplay.visible = true;

        dateGrid.setActualSize(w, h - headerHeight);
        dateGrid.move(borderThickness, headerHeight + borderThickness);

        var g:Graphics = background.graphics;
        g.clear();
        g.beginFill(0xFFFFFF);
        g.drawRoundRect(0, 0, w, h, cornerRadius * 2, cornerRadius * 2);
        g.endFill();
        background.$visible = true;

        g = border.graphics;
        g.clear();
        g.beginFill(borderColor);
        g.drawRoundRect(0, 0, unscaledWidth, unscaledHeight, 
                        cornerRadius * 2, cornerRadius * 2);
        g.endFill();
        var bgColor:uint = StyleManager.NOT_A_COLOR;
        bgColor = getStyle("backgroundColor");
        if (bgColor == StyleManager.NOT_A_COLOR)
            bgColor = 0xFFFFFF;
        var bgAlpha:Number = 1;
        bgAlpha = getStyle("backgroundAlpha");
        g.beginFill(bgColor, bgAlpha);
        g.drawRoundRect(borderThickness, borderThickness, w, h, 
                        cornerRadius > 0 ? (cornerRadius - 1) * 2 : 0,
                        cornerRadius > 0 ? (cornerRadius - 1) * 2 : 0);
        g.endFill();
        border.visible = true;

        var headerColors:Array = getStyle("headerColors");
        StyleManager.getColorNames(headerColors);

        var calHG:Graphics = calHeader.graphics;
        calHG.clear();
        var matrix:Matrix = new Matrix();
        matrix.createGradientBox(w, headerHeight, Math.PI / 2, 0, 0);
        calHG.beginGradientFill(GradientType.LINEAR,
                                headerColors,
                                //[0xFF0000,0x00FF00],
                                [1.0,1.0],
                                [ 0, 0xFF ],
                                matrix);
        GraphicsUtil.drawRoundRectComplex(calHG, borderThickness, borderThickness,
            w, headerHeight, cornerRadius, cornerRadius, 0, 0);
        calHG.endFill();
        calHG.lineStyle(borderThickness, borderColor);
        calHG.moveTo(borderThickness, headerHeight + borderThickness);
        calHG.lineTo(w + borderThickness, headerHeight + borderThickness);

        calHeader.$visible = true;

        // Note: Height of header is not dependent on unscaledHeight.

        fwdMonthButton.setActualSize(monthSkinWidth, monthSkinHeight);
        backMonthButton.setActualSize(monthSkinWidth, monthSkinHeight);

        fwdMonthButton.move(w - (monthSkinWidth + HEADER_WIDTH_PAD) + borderThickness,
                            Math.round(borderThickness + (headerHeight - monthSkinHeight) / 2));
        backMonthButton.move(HEADER_WIDTH_PAD + borderThickness,
                             Math.round(borderThickness + (headerHeight - monthSkinHeight) / 2));

        if (_yearNavigationEnabled)
        {
            // Assumption, up and down Button's are symmettrical.
            upYearButton.setActualSize(yearSkinWidth, yearSkinHeight);
            downYearButton.setActualSize(yearSkinWidth, yearSkinHeight);

            upYearButton.x = yearX;
            upYearButton.y = headerHeight / 2 - yearSkinHeight / 2 - 2;
            downYearButton.x = yearX;
            downYearButton.y = headerHeight / 2 + yearSkinHeight / 2 - 2;

            pointX = upYearButton.x - SKIN_WIDTH_PAD / 2;
            pointY = upYearButton.y - SKIN_HEIGHT_PAD / 2;
            var upG:Graphics = upYearHit.graphics;
            upG.clear();
            upG.beginFill(0xCC0000, 0);
            upG.moveTo(pointX, pointY);
            upG.lineTo(pointX + yearSkinWidth + SKIN_WIDTH_PAD, pointY);
            upG.lineTo(pointX + yearSkinWidth + SKIN_WIDTH_PAD, pointY + yearSkinHeight + SKIN_HEIGHT_PAD / 2);
            upG.lineTo(pointX, pointY + yearSkinHeight + SKIN_HEIGHT_PAD / 2);
            upG.lineTo(pointX, pointY);
            upG.endFill();

            pointX = downYearButton.x - SKIN_WIDTH_PAD / 2;
            pointY = downYearButton.y;
            var downG:Graphics = downYearHit.graphics;
            downG.clear();
            downG.beginFill(0xCC0000, 0);
            downG.moveTo(pointX, pointY);
            downG.lineTo(pointX + yearSkinWidth + SKIN_WIDTH_PAD, pointY);
            downG.lineTo(pointX + yearSkinWidth + SKIN_WIDTH_PAD, pointY + yearSkinHeight + SKIN_HEIGHT_PAD / 2);
            downG.lineTo(pointX, pointY + yearSkinHeight + SKIN_HEIGHT_PAD / 2);
            downG.lineTo(pointX, pointY);
            downG.endFill();
        }

        var pointX:Number = fwdMonthButton.x - SKIN_WIDTH_PAD / 2;
        var pointY:Number = fwdMonthButton.y - SKIN_HEIGHT_PAD;

        var fwdG:Graphics = fwdMonthHit.graphics;
        fwdG.clear();
        fwdG.beginFill(0xCC0000, 0);
        fwdG.moveTo(pointX, pointY);
        fwdG.lineTo(pointX + monthSkinWidth + SKIN_WIDTH_PAD / 2, pointY);
        fwdG.lineTo(pointX + monthSkinWidth + SKIN_WIDTH_PAD / 2, pointY + monthSkinHeight + SKIN_HEIGHT_PAD);
        fwdG.lineTo(pointX, pointY + monthSkinHeight + SKIN_HEIGHT_PAD);
        fwdG.lineTo(pointX, pointY);
        fwdG.endFill();

        pointX = backMonthButton.x - SKIN_WIDTH_PAD / 2;
        pointY = backMonthButton.y - SKIN_HEIGHT_PAD;
        var bkG:Graphics = backMonthHit.graphics;
        bkG.clear();
        bkG.beginFill(0xCC0000, 0);
        bkG.moveTo(pointX, pointY);
        bkG.lineTo(pointX + monthSkinWidth + SKIN_WIDTH_PAD / 2, pointY);
        bkG.lineTo(pointX + monthSkinWidth + SKIN_WIDTH_PAD / 2, pointY + monthSkinHeight + SKIN_HEIGHT_PAD);
        bkG.lineTo(pointX, pointY + monthSkinHeight + SKIN_HEIGHT_PAD);
        bkG.lineTo(pointX, pointY);
        bkG.endFill();

        var dsStyle:Object = getStyle("dropShadowEnabled");
        graphics.clear();
        if (dsStyle == true || (dsStyle is String && String(dsStyle).toLowerCase() == "true"))
        {
            // Calculate the angle and distance for the shadow
            var distance:Number = getStyle("shadowDistance");
            var direction:String = getStyle("shadowDirection");
            var angle:Number;
            angle = 90; // getDropShadowAngle(distance, direction);
            distance = Math.abs(distance) + 2;

            // Create a RectangularDropShadow object, set its properties, and
            // draw the shadow
            if (!dropShadow)
                dropShadow = new RectangularDropShadow();

            dropShadow.distance = distance;
            dropShadow.angle = angle;
            dropShadow.color = getStyle("dropShadowColor");
            dropShadow.alpha = 0.4;

            dropShadow.tlRadius = cornerRadius;
            dropShadow.trRadius = cornerRadius;
            dropShadow.blRadius = cornerRadius;
            dropShadow.brRadius = cornerRadius;

            dropShadow.drawShadow(graphics, borderThickness, borderThickness, w, h);
        }
    }

    /**
     *  @private
     */
    override public function styleChanged(styleProp:String):void
    {
        super.styleChanged(styleProp);

        if (styleProp == null || styleProp == "styleName" ||
            styleProp == "borderColor" || styleProp == "headerColor" ||
            styleProp == "headerColors" || styleProp == "backgroundColor" ||
            styleProp =="horizontalGap" || styleProp == "verticalGap" ||
            styleProp =="backgroundAlpha")
        {
            invalidateDisplayList();
        }

        if (styleProp == null || styleProp == "styleName" ||
            styleProp == "headerStyleName" && monthDisplay)
        {
            var dateHeaderStyleName:Object = getStyle("headerStyleName");
            if (!dateHeaderStyleName)
                dateHeaderStyleName = this;
            if (monthDisplay)
                monthDisplay.styleName = dateHeaderStyleName;
            if (yearDisplay)
                yearDisplay.styleName = dateHeaderStyleName;
        }

    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  Populates localizable properties from the loaded
     *  bundle for this class.
     */
    private function bundleChanged():void
    {
        _monthNames = resourceMonthNames.slice();
        _dayNames = resourceDayNames;
        _firstDayOfWeek = resourceFirstDayOfWeek;
        if (_firstDayOfWeek != 0)
            firstDayOfWeekChanged = true;
        // Currently there is no way to get a null 
        // string from resourceBundles using getString
        // Hence year/monthSymbol is a space in English,
        // but we actually want it to be a null string.
        if (resourceYearSymbol != " ")          
            _yearSymbol = resourceYearSymbol;
        if (resourceMonthSymbol != " ")
        {
            _monthSymbol = resourceMonthSymbol;
            monthNamesChanged = true;
            invalidateProperties();
        }
    }

    /**
     *  @private
     */
    private function updateDateDisplay():void
    {
        monthDisplay.text = monthNames[dateGrid.displayedMonth];
        yearDisplay.text = displayedYear.toString() + _yearSymbol;
    }

    /**
     *  @private
     */
    private function getYearNavigationButtons():void
    {
        // Create the next-Year button.
        if (!upYearButton)
        {
            upYearButton = new Button();
            upYearButton.styleName = this;
            upYearButton.autoRepeat = true;
            upYearButton.focusEnabled = false;
            upYearButton.upSkinName = "nextYearUpSkin";
            upYearButton.overSkinName = "nextYearOverSkin";
            upYearButton.downSkinName = "nextYearDownSkin";
            upYearButton.disabledSkinName = "nextYearDisabledSkin";
            upYearButton.upIconName = "";
            upYearButton.overIconName = "";
            upYearButton.downIconName = "";
            upYearButton.disabledIconName = "";
            upYearButton.addEventListener(FlexEvent.BUTTON_DOWN,
                                          upYearButton_buttonDownHandler);
            addChild(upYearButton);
        }

        // Create the previous-Year button.
        if (!downYearButton)
        {
            downYearButton = new Button();
            downYearButton.styleName = this;
            downYearButton.focusEnabled = false;
            downYearButton.autoRepeat = true;
            downYearButton.upSkinName = "prevYearUpSkin";
            downYearButton.overSkinName = "prevYearOverSkin";
            downYearButton.downSkinName = "prevYearDownSkin";
            downYearButton.disabledSkinName = "prevYearDisabledSkin";
            downYearButton.upIconName = "";
            downYearButton.overIconName = "";
            downYearButton.downIconName = "";
            downYearButton.disabledIconName = "";
            downYearButton.addEventListener(FlexEvent.BUTTON_DOWN,
                                            downYearButton_buttonDownHandler);
            addChild(downYearButton);
        }

        if (!upYearHit)
        {
            upYearHit = new FlexSprite();
            upYearHit.name = "upYearHit";
            addChild(upYearHit);
            upYearHit.visible = false;
            upYearButton.hitArea = upYearHit;
        }

        if (!downYearHit)
        {
            downYearHit = new FlexSprite();
            downYearHit.name = "downYearHit";
            addChild(downYearHit);
            downYearHit.visible = false;
            downYearButton.hitArea = downYearHit;
        }
    }

    /**
     *  @private
     */
    private function setMonthWidth():void
    {
        var tempWidth:Number = 0;
        var longestMonth:int;
        var longestMonthWidth:Number = 0;
        for (var i:int = 0; i < 12; i++)
        {
            tempWidth = measureText(monthNames[i]).width;
            if (longestMonthWidth < tempWidth)
            {
                longestMonthWidth = tempWidth;
                longestMonth = i;
            }
        }

        var longestMonthUITextField:UITextField = monthDisplay;
        longestMonthUITextField.text = monthNames[longestMonth];
        monthDisplay.width = longestMonthWidth * longestMonthUITextField.getExplicitOrMeasuredWidth()
            / measureText(monthNames[longestMonth]).width;
    }

    /**
     *  @private
     *  Returns true if year comes before month in DateFormat.
     *  Used for correct placement of year and month in header.
     */ 
    private function yearBeforeMonth(dateFormat:String):Boolean
    {
        for(var i:uint=0; i<dateFormat.length; i++)
        {
            if (dateFormat.charAt(i) == "M")
                return false;
            else if (dateFormat.charAt(i) == "Y")
                return true;
        }
        return false;
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden event handlers: UIComponent
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     */
    override protected function keyDownHandler(event:KeyboardEvent):void
    {
        // Ignore events that bubble up from the child CalendarLayout.
        // such as those we redispatch below.
        if (event.eventPhase != EventPhase.AT_TARGET)
            return;

        if (event.keyCode == Keyboard.PAGE_UP)
        {
            backMonthButton_buttonDownHandler(event);
        }
        else if (event.keyCode == Keyboard.PAGE_DOWN)
        {
            fwdMonthButton_buttonDownHandler(event);
        }
        else if (event.keyCode == 189) // - or _ key used to step down year
        {
            if (_yearNavigationEnabled)
                downYearButton_buttonDownHandler(event);
        }
        else if (event.keyCode == 187) // + or = key used to step up year
        {
            if (_yearNavigationEnabled)
                upYearButton_buttonDownHandler(event);
        }

        // Redispatch the event from the CalendarLayout
        // to let its keyDownHandler() handle it.
        dateGrid.dispatchEvent(event);
        // Prevent keys from going to scrollBars when 
        // the DateChooser is handling them.
        event.stopPropagation();
    }

    //--------------------------------------------------------------------------
    //
    //  Event handlers
    //
    //--------------------------------------------------------------------------

    /**
     *  @private
     *  event is either a KeyboardEvent or a FlexEvent
     */
    private function upYearButton_buttonDownHandler(event:Event):void
    {
        if (maxYear < displayedYear + 1)
            return;

        if ((selectableRange) &&
            (dateGrid.selRangeMode == 1 || dateGrid.selRangeMode == 3))
        {
            var testDate:Date = new Date(displayedYear, displayedMonth,
                                         selectableRange.rangeEnd.getDate());

            if (selectableRange.rangeEnd > testDate)
            {
                dateGrid.stepDate(1, 0, event);
                invalidateDisplayList();
            }

        }
        else if (dateGrid.selRangeMode != 4 || !selectableRange)
        {
            dateGrid.stepDate(1, 0, event);
            invalidateDisplayList();
        }
    }

    /**
     *  @private
     *  event is either a KeyboardEvent or a FlexEvent
     */
    private function downYearButton_buttonDownHandler(event:Event):void
    {
        if (minYear > displayedYear - 1)
            return;

        if (selectableRange &&
            (dateGrid.selRangeMode == 1 || dateGrid.selRangeMode == 2))
        {
            var testDate:Date = new Date(displayedYear, displayedMonth,
                                         selectableRange.rangeStart.getDate());

            if (selectableRange.rangeStart < testDate)
            {
                dateGrid.stepDate(-1, 0, event);
                invalidateDisplayList();
            }

        }
        else if (dateGrid.selRangeMode != 4 || !selectableRange)
        {
            dateGrid.stepDate(-1, 0, event);
            invalidateDisplayList();
        }
    }

    /**
     *  @private
     *  event is either a KeyboardEvent or a FlexEvent
     */
    private function fwdMonthButton_buttonDownHandler(event:Event):void
    {
        if ((maxYear < displayedYear + 1) && (displayedMonth == 11))
            return;

        if ((selectableRange) &&
            (dateGrid.selRangeMode == 1 || dateGrid.selRangeMode == 3))
        {
            var testDate:Date = new Date(displayedYear, displayedMonth,
                                         selectableRange.rangeEnd.getDate());

            if (selectableRange.rangeEnd > testDate)
            {
                dateGrid.stepDate(0, 1, event);
                invalidateDisplayList();
            }

        }
        else if (dateGrid.selRangeMode != 4 || !selectableRange)
        {
            dateGrid.stepDate(0, 1, event);
            invalidateDisplayList();
        }
    }

    /**
     *  @private
     *  event is either a KeyboardEvent or a FlexEvent
     */
    private function backMonthButton_buttonDownHandler(event:Event):void
    {
        if ((minYear > displayedYear - 1) && (displayedMonth == 0))
            return;

        if (selectableRange &&
            (dateGrid.selRangeMode == 1 || dateGrid.selRangeMode == 2))
        {
            var testDate:Date = new Date(displayedYear, displayedMonth,
                                         selectableRange.rangeStart.getDate());

            if (selectableRange.rangeStart < testDate)
            {
                dateGrid.stepDate(0, -1, event);
                invalidateDisplayList();
            }

        }
        else if (dateGrid.selRangeMode != 4 || !selectableRange)
        {
            dateGrid.stepDate(0, -1, event);
            invalidateDisplayList();
        }
    }

    /**
     *  @private
     */
    private function dateGrid_scrollHandler(event:DateChooserEvent):void
    {
        dispatchEvent(event);
    }

    /**
     *  @private
     */
    private function dateGrid_changeHandler(event:CalendarLayoutChangeEvent):void
    {
        _selectedDate = CalendarLayout(event.target).selectedDate;
        
        var e:CalendarLayoutChangeEvent = new 
            CalendarLayoutChangeEvent(CalendarLayoutChangeEvent.CHANGE);
        e.newDate = event.newDate;
        e.triggerEvent = event.triggerEvent;
        dispatchEvent(e);
    }
}

}
