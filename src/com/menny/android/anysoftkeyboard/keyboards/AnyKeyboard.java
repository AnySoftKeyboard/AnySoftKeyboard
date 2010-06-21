package com.menny.android.anysoftkeyboard.keyboards;

import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Workarounds;

public abstract class AnyKeyboard extends Keyboard 
{
	public static final String POPUP_FOR_QUESTION = "!/@\u0026\u00bf\u00a1";
	public static final String POPUP_FOR_AT = "!/?\u0026\u00bf\u00a1";
	private final static String TAG = "ASK - AK";
	
	public final static int KEYCODE_LANG_CHANGE = -99;
	//public final static int KEYCODE_ALTER_LAYOUT = -98;
	public final static int KEYCODE_KEYBOARD_CYCLE = -97;
	public final static int KEYCODE_KEYBOARD_REVERSE_CYCLE = -96;
	
	public final static int KEYCODE_SMILEY = -10;
	
	public static final int KEYCODE_LEFT = -20;
	public static final int KEYCODE_RIGHT = -21;
	public static final int KEYCODE_UP = -22;
	public static final int KEYCODE_DOWN = -23;
	
	public static final int	KEYCODE_CTRL = -11;
	
	public interface HardKeyboardAction
	{
		int getKeyCode();
		boolean isAltActive();
		boolean isShiftActive();
		void setNewKeyCode(int keyCode);
	}
	
	public interface HardKeyboardTranslator
	{
		/*
		 * Gets the current state of the hard keyboard, and may change the output key-code.
		 */
		void translatePhysicalCharacter(HardKeyboardAction action);
	}
	
	private static final String TAG_ROW = "Row";
    private static final String TAG_KEY = "Key";

	private static class KeyboardMetadata
	{
		public int keysCount = 0;
		public int rowHeight = 0;
		public int rowWidth = 0;
		public int verticalGap = 0;
		public boolean isTopRow = false;
	}
	
	private static final int SHIFT_OFF = 0;
    private static final int SHIFT_ON = 1;
    private static final int SHIFT_LOCKED = 2;
    
    private int mShiftState = SHIFT_OFF;
    
    private final boolean mDebug;
	
    private final Drawable mOffShiftIcon;
    private final Drawable mOnShiftIcon;
    private final Drawable mOffShiftFeedbackIcon;
    private final Drawable mOnShiftFeedbackIcon;
    private final int mDomainsIconId;

    private Key mShiftKey;
    private EnterKey mEnterKey;
	private Key mSmileyKey;
	private Key mQuestionMarkKey;

	private boolean mRightToLeftLayout = false;//the "super" ctor will create keys, and we'll set the correct value there.
	
    private final Context mKeyboardContext;
    private final AnyKeyboardContextProvider mASKContext;
	
    private boolean mTopRowWasCreated;
	private boolean mBottomRowWasCreated;
	
	private int mGenericRowsHeight = 0;
	private int mTopRowKeysCount = 0;
	// max(generic row widths)
	private int mMaxGenericRowsWidth = 0;
	
    protected AnyKeyboard(AnyKeyboardContextProvider askContext, Context context,//note: the context can be from a different package!
    		int xmlLayoutResId) 
    {
        //should use the package context for creating the layout
        super(context, xmlLayoutResId);
        
        mDebug = AnySoftKeyboardConfiguration.getInstance().getDEBUG();
        mKeyboardContext = context;
        mASKContext = askContext;

		addGenericRows(askContext, context);
		
        //in wide shifts, we'll use the shift with the Globe
        Resources resources = askContext.getApplicationContext().getResources();
		if (mShiftKey != null)
        {
	        Drawable shiftWithGlobes = resources.getDrawable(R.drawable.sym_keyboard_shift_with_globe);
	        //Log.v(TAG, "Deciding which icon to use for the SHIFT. Shift key width is "+mShiftKey.width+" and sym_keyboard_shift_with_globe width is "+shiftWithGlobes.getMinimumWidth());
	        
	        if (mShiftKey.width > shiftWithGlobes.getMinimumWidth())
	        {
	        	mOnShiftIcon = resources.getDrawable(R.drawable.sym_keyboard_shift_with_globes_on);
		        mOffShiftIcon = shiftWithGlobes;
		        mOnShiftFeedbackIcon = resources.getDrawable(R.drawable.sym_keyboard_shift_with_globes_on);
		        mOffShiftFeedbackIcon = shiftWithGlobes;
	        }
	        else
	        {
		        mOnShiftIcon = resources.getDrawable(R.drawable.sym_keyboard_shift_on);
		        mOffShiftIcon = resources.getDrawable(R.drawable.sym_keyboard_shift);
		        mOnShiftFeedbackIcon = resources.getDrawable(R.drawable.sym_keyboard_feedback_shift_on);
		        mOffShiftFeedbackIcon = resources.getDrawable(R.drawable.sym_keyboard_feedback_shift);;
	        }
	        mShiftKey.icon = mOffShiftIcon;
	        mOnShiftFeedbackIcon.setBounds(0, 0, 
	        		mOnShiftFeedbackIcon.getIntrinsicWidth(), mOnShiftFeedbackIcon.getIntrinsicHeight());
	        mOffShiftFeedbackIcon.setBounds(0, 0, 
	        		mOffShiftFeedbackIcon.getIntrinsicWidth(), mOffShiftFeedbackIcon.getIntrinsicHeight());
        }
        else
        {
        	mOnShiftIcon = null;
        	mOffShiftIcon = null;
        	mOnShiftFeedbackIcon = null;
        	mOffShiftFeedbackIcon = null;
        	Log.v(TAG, "No shift key, so no handling images.");
	        
        }
        
        if (mSmileyKey != null)
        {
        	Drawable wideDomains = resources.getDrawable(R.drawable.sym_keyboard_key_domain_wide);
	         
	        if (mSmileyKey.width > wideDomains.getMinimumWidth())
	        {
	        	mDomainsIconId = R.drawable.sym_keyboard_key_domain_wide;
		    }
	        else
	        {
	        	mDomainsIconId = R.drawable.sym_keyboard_key_domain;
	        }
        }
        else
        {
        	mDomainsIconId = -1;
        }
    }
    
    public void initKeysMembers()
    {
    	final Resources localResources = getASKContext().getApplicationContext().getResources();
        for(final Key key : getKeys())
        {
        	if (key.y == 0) key.edgeFlags = Keyboard.EDGE_TOP;
        	//Log.d(TAG, "Key x:"+key.x+" y:"+key.y+" width:"+key.width+" height:"+key.height);
            if ((key.codes != null) && (key.codes.length > 0))
            {
                final int primaryCode = key.codes[0];
                //detecting LTR languages
                if (Workarounds.isRightToLeftCharacter((char)primaryCode))
                	mRightToLeftLayout = true;//one is enough
                switch(primaryCode)
                {
                case AnyKeyboard.KEYCODE_DELETE:
                	setIconIfNeeded(key, localResources, R.drawable.sym_keyboard_delete_small , R.drawable.sym_keyboard_feedback_delete);
                    break;
//                case AnyKeyboard.KEYCODE_SHIFT:
//                	key.icon = localResources.getDrawable(R.drawable.sym_keyboard_shift);
//                    break;
                case AnyKeyboard.KEYCODE_CTRL:
                	setIconIfNeeded(key, localResources, R.drawable.sym_keyboard_ctrl, -1);
                    break;
                case 32://SPACE
                	setIconIfNeeded(key, localResources, R.drawable.sym_keyboard_space, R.drawable.sym_keyboard_feedback_space);
                    break;
                case 9://TAB
                	setIconIfNeeded(key, localResources, R.drawable.tab_key, -1);
                    break;
                case AnyKeyboard.KEYCODE_LANG_CHANGE:
                	setIconIfNeeded(key, localResources, R.drawable.globe, -1);
                    break;
                case 63:
                    if ((key.edgeFlags & Keyboard.EDGE_BOTTOM) != 0)
                    {
                    	mQuestionMarkKey = key;
                    }
                    break;
               default:
                        //setting the character label
                        if (isAlphabetKey(key) && (key.label == null || key.label.length() == 0) && (key.icon == null))
                        {
                        	final char code = (char)key.codes[0];
                        	if (code > 0 && !Character.isWhitespace(code))
                        		key.label = ""+code;
                        	else
                        		key.label = " ";
                        }
                }
            }
        }
    }
    
	private void addGenericRows(AnyKeyboardContextProvider askContext, Context context) {
		final String keysMode = AnySoftKeyboardConfiguration.getInstance().getChangeLayoutKeysSize();
		final KeyboardMetadata topMd;
		if (!mTopRowWasCreated)
		{
	        if (keysMode.equals("None"))
	        {
	        	topMd = null;
	        }
	        else if (keysMode.equals("Big"))
	        {
	        	topMd = addKeyboardRow(askContext.getApplicationContext(), R.xml.generic_top_row);
	        }
	        else
	        {
	        	topMd = addKeyboardRow(askContext.getApplicationContext(), R.xml.generic_half_top_row);
	        }
        
			if (topMd != null)
				fixKeyboardDueToGenericRow(topMd);
		}
		if (!mBottomRowWasCreated)
		{
			KeyboardMetadata bottomMd = addKeyboardRow(askContext.getApplicationContext(), R.xml.generic_bottom_row);
			fixKeyboardDueToGenericRow(bottomMd);
		}
	}

    private void fixKeyboardDueToGenericRow(KeyboardMetadata md) {
    	mGenericRowsHeight += md.rowHeight + md.verticalGap;
    	if (md.isTopRow)
    	{
    		mTopRowKeysCount += md.keysCount;
    		List<Key> keys = getKeys();
    		for(int keyIndex = md.keysCount; keyIndex < keys.size(); keyIndex++)
            {
    			final Key key = keys.get(keyIndex);
    			key.y += md.rowHeight + md.verticalGap;
    			if (key instanceof LessSensitiveAnyKey)
            		((LessSensitiveAnyKey)key).resetSenitivity();//reseting cause the key may be offseted now (generic rows)
            }
    	} else {
    		// The height should not include any gap below that last row
    		// this corresponds to
    		// mTotalHeight = y - mDefaultVerticalGap;
    		// in the Keyboard class from Android sources

    		// Note that we are using keyboard default vertical gap (instead of row vertical gap)
    		// as this is done also in Android sources.
    		mGenericRowsHeight -= getVerticalGap();
    	}
	}

	private KeyboardMetadata addKeyboardRow(Context context, int rowResId) {
		XmlResourceParser parser = context.getResources().getXml(rowResId);
    	List<Key> keys = getKeys();
        boolean inKey = false;
        boolean inRow = false;
        boolean leftMostKey = false;

        int row = 0;
        int x = 0;
        int y = 0;
        Key key = null;
        Row currentRow = null;
        Resources res = context.getResources();

        KeyboardMetadata m = new KeyboardMetadata();

        try {
            int event;
            while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
                if (event == XmlResourceParser.START_TAG) {
                    String tag = parser.getName();
                    if (TAG_ROW.equals(tag)) {
                        inRow = true;
                        x = 0;
                        currentRow = createRowFromXml(res, parser);
                        m.isTopRow = currentRow.rowEdgeFlags == Keyboard.EDGE_TOP;
                        if (!m.isTopRow) {
                        	//the bottom row Y should be last
                        	// The last coordinate is height + keyboard's default vertical gap
                        	// since  mTotalHeight = y - mDefaultVerticalGap; (see loadKeyboard
                        	// in the android sources)
                        	// We use our overriden getHeight method which
                        	// is just fixed so that it includes the first generic row.
                        	y = getHeight() + getVerticalGap();
                        }
                        m.rowHeight = currentRow.defaultHeight;
                        m.verticalGap = currentRow.verticalGap;
                   } else if (TAG_KEY.equals(tag)) {
                        inKey = true;
                        key = createKeyFromXml(res, currentRow, x, y, parser);
                        if (m.isTopRow)
                        	keys.add(m.keysCount, key);
                        else
                        	keys.add(key);
                        m.keysCount++;
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        inKey = false;
                        x += (key.gap + key.width);
                        if (x > m.rowWidth) {
                        	m.rowWidth = x;
                        	// We keep generic row max width updated
                    		mMaxGenericRowsWidth = Math.max(mMaxGenericRowsWidth, m.rowWidth);
                        }
                    } else if (inRow) {
                        inRow = false;
                        y += currentRow.verticalGap;
                        y += currentRow.defaultHeight;
                        row++;
                    } else {
                        // TODO: error or extend?
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
        //mTotalHeight = y - mDefaultVerticalGap;
        return m;
    }

    /*required overrides*/

    @Override
    public int getHeight() {
    	return super.getHeight() + mGenericRowsHeight;
    }

    // minWidth is actually 'total width', see android framework source code
    @Override
    public int getMinWidth() {
    	return Math.max(mMaxGenericRowsWidth, super.getMinWidth());
    }

    @Override
    public int getShiftKeyIndex() {
    	return super.getShiftKeyIndex() + mTopRowKeysCount;
    }
    
	private void setIconIfNeeded(Key key, Resources localResources, int iconId, int iconFeedbackId) {
		if ((key.icon != null) || ((key.label != null) && (key.label.length() > 0)))
			return;
		setKeyIcons(key, localResources, iconId, iconFeedbackId);
	}

	private void setKeyIcons(Key key, Resources localResources, int iconId,
			int iconFeedbackId) {
		key.icon = localResources.getDrawable(iconId);
		if (iconFeedbackId > 0)
		{
			Drawable preview = localResources.getDrawable(iconFeedbackId);
    		preview.setBounds(0, 0, 
    				preview.getIntrinsicWidth(), preview.getIntrinsicHeight());
    		key.iconPreview = preview;
    		key.label = null;
		}
	}

	protected AnyKeyboardContextProvider getASKContext()
    {
        return mASKContext;
    }

	public Context getKeyboardContext()
    {
    	return mKeyboardContext;
    }
    
    public abstract String getDefaultDictionaryLocale();
    
    //this function is called from within the super constructor.
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, 
            XmlResourceParser parser) {
    	AnyKey key = new AnyKey(res, parent, x, y, parser);
    	
        if ((key.codes != null) && (key.codes.length > 0))
        {
        	final int primaryCode = key.codes[0];
    		
        	//creating less sensitive keys if required
        	switch(primaryCode)
        	{
        	case 10://enter
        		key = mEnterKey = new EnterKey(res, parent, x, y, parser);
        		break;
        	case KEYCODE_SHIFT:
        		mShiftKey = key;//I want the reference used by the super.
        		break;
        	case KEYCODE_DELETE://delete
        		key = new LessSensitiveAnyKey(res, parent, x, y, parser);
        		break;
        	case AnyKeyboard.KEYCODE_SMILEY: 
            	mSmileyKey = key;
                break;
	        }
        }
        
        if (mDebug)
        {
        	final int primaryKey = ((key.codes != null) && key.codes.length > 0)?
        			key.codes[0] : -1;
        	Log.v(TAG, "Key '"+primaryKey+"' will have - width: "+key.width+", height:"+key.height+", text: '"+key.label+"'.");
        }
        
        setPopupKeyChars(key);
        
        if (!TextUtils.isEmpty(key.label))
        	key.label = Workarounds.workaroundCorrectStringDirection(key.label);
        		
        return key;
    }

    @Override
    protected Row createRowFromXml(Resources res, XmlResourceParser parser) 
    {
    	Row aRow = super.createRowFromXml(res, parser);
    	
    	AnySoftKeyboardConfiguration config = AnySoftKeyboardConfiguration.getInstance();
		final int orientation = config.getDeviceOrientation();
    	if (orientation != Configuration.ORIENTATION_LANDSCAPE)//I want to support other orientations too (like square)
    		aRow.defaultHeight = (int)(aRow.defaultHeight * config.getKeysHeightFactorInPortrait());
    	else
    		aRow.defaultHeight = (int)(aRow.defaultHeight * config.getKeysHeightFactorInLandscape());
    	
    	if ((aRow.rowEdgeFlags & Keyboard.EDGE_TOP) != 0)
			mTopRowWasCreated = true;
		if ((aRow.rowEdgeFlags & Keyboard.EDGE_BOTTOM) != 0)
			mBottomRowWasCreated = true;
		
    	return aRow;
    }
    
    private boolean isAlphabetKey(Key key) {
		return  (!key.modifier) && 
				(!key.sticky) &&
				(!key.repeatable) &&
				(key.icon == null) &&
				(key.codes[0] > 0);
	}

    public boolean isLetter(char keyValue)
    {
    	return (Character.isLetter(keyValue) || (keyValue == '\''));
    }
	/**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    public void setImeOptions(Resources res, EditorInfo editor) {
    	if (mDebug)
    	{
    		if (editor == null)
    		{
    			Log.d(TAG, "AnyKeyboard.setImeOptions");
    		}
    		else
    		{
    			Log.d(TAG, "AnyKeyboard.setImeOptions. package: "+editor.packageName+", id:"+editor.fieldId);
    		}
    	}
    		
        if (mEnterKey == null) {
            return;
        }
        
        //Issue 254: we know of a known Android Messaging bug
        //http://code.google.com/p/android/issues/detail?id=2739
        if (Workarounds.doubleActionKeyDisableWorkAround(editor))
        {//package: com.android.mms, id:2131361817
        	mEnterKey.disable();
        	return;
        }
        int options = (editor == null)? 0 : editor.imeOptions;
        CharSequence imeLabel = (editor == null)? null :editor.actionLabel;
        int imeActionId = (editor == null)? -1 :editor.actionId;
        
        mEnterKey.enable();
        
        //Used in conjunction with a custom action, this indicates that the action should not be available in-line 
        //as a replacement for the "enter" key. Typically this is because the action has such a significant impact 
        //or is not recoverable enough that accidentally hitting it should be avoided, such as sending a message. 
        //Note that TextView  will automatically set this flag for you on multi-line text views. 
        boolean inNoEnterActionMode = ((options&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0);
        
    	final int action = (options&EditorInfo.IME_MASK_ACTION);
    	
    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) 
    		Log.d(TAG, "Input Connection ENTER key with action: "+action + " and NO_ACTION flag is: "+inNoEnterActionMode);

    	if (inNoEnterActionMode)
    	{
    		//this means that the ENTER should not be replaced with a custom action.
    		//maybe in future ASK releases, we'll add the custom action key.
    		mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_return);
            mEnterKey.label = null;
    	}
    	else
    	{
	        switch (action) {
	            case EditorInfo.IME_ACTION_GO:
	                mEnterKey.iconPreview = null;
	                mEnterKey.icon = null;
	                //there is a problem with LTR languages
	                mEnterKey.label = Workarounds.workaroundCorrectStringDirection(res.getText(R.string.label_go_key));
	                break;
	            case EditorInfo.IME_ACTION_NEXT:
	                mEnterKey.iconPreview = null;
	                mEnterKey.icon = null;
	              //there is a problem with LTR languages
	                mEnterKey.label = Workarounds.workaroundCorrectStringDirection(res.getText(R.string.label_next_key));
	                break;
	            case EditorInfo.IME_ACTION_DONE:
	            	mEnterKey.iconPreview = null;
	                mEnterKey.icon = null;
	                //there is a problem with LTR languages
	                mEnterKey.label = Workarounds.workaroundCorrectStringDirection(res.getText(R.string.label_done_key));
	                break;
	            case EditorInfo.IME_ACTION_SEARCH:
	            	setKeyIcons(mEnterKey, res, R.drawable.sym_keyboard_search, R.drawable.sym_keyboard_feedback_search);
	                break;
	            case EditorInfo.IME_ACTION_SEND:
	            	mEnterKey.iconPreview = null;
		            mEnterKey.icon = null;
		            //there is a problem with LTR languages
		            mEnterKey.label = Workarounds.workaroundCorrectStringDirection(res.getText(R.string.label_send_key));
	            	break;
	            case EditorInfo.IME_ACTION_NONE:
	            case EditorInfo.IME_ACTION_UNSPECIFIED:
	            default:
	            	//TODO: Maybe someday we will support this functionality
//	            	if ((imeLabel != null) && (imeLabel.length() > 0) && (imeActionId > 0))
//	            	{
//	            		Log.d(TAG, "Input has provided its own ENTER label: "+ imeLabel);
//	            		mEnterKey.iconPreview = null;
//	            		mEnterKey.icon = null;
//			            //there is a problem with LTR languages
//	                  	mEnterKey.label = Workarounds.workaroundCorrectStringDirection(imeLabel);
//	            	}
//	            	else
//	            	{
	            		setKeyIcons(mEnterKey, res, R.drawable.sym_keyboard_return, R.drawable.sym_keyboard_feedback_return);
//	            	}
	            	break;
	        }
    	}
    }
    
    protected abstract int getKeyboardNameResId();
    
    public String getKeyboardName()
    {
        return mKeyboardContext.getResources().getString(getKeyboardNameResId());
    }
    
    public boolean isLeftToRightLanguage()
    {
    	return !mRightToLeftLayout;
    }
    
    public abstract int getKeyboardIconResId();
    
	public void setShiftLocked(boolean shiftLocked) {
        if (mShiftKey != null) {
        	if (mDebug) Log.d(TAG, "setShiftLocked: Switching to locked: "+shiftLocked);
        	mShiftKey.on = shiftLocked;
        	if (shiftLocked)
        		mShiftState = SHIFT_LOCKED;
        }
    }
    
    @Override
    public boolean isShifted() {
        if (mShiftKey != null) {
            return mShiftState != SHIFT_OFF;
        } else {
            return false;
        }
    }
    
	@Override
	public boolean setShifted(boolean shiftState) 
	{
		//final boolean superResult = super.setShifted(shiftState);
		//making sure it is off. Only caps turn it on. The super will turn the lit on when
		//shift is ON, and not when CAPS is on.
		if (mShiftKey == null)
			return false;
        mShiftKey.on = false;
        
        /*My shift state - parameter - changed
         * OFF - true - true
         * ON - true - false
         * LOCKED - true - false
         * OFF - false - false
         * ON - false - true
         * LOCKED - false - true
         * 
         * in Other words, ON and LOCKED act the same
         */
		final boolean changed = (shiftState == (mShiftState == SHIFT_OFF));
		
		if (mDebug) Log.d(TAG, "setShifted: shiftState:"+shiftState+". changed: "+changed);
		
		if (changed)
		{//layout changed. Need to change labels.
			mShiftState = shiftState? SHIFT_ON : SHIFT_OFF;
						
			if (mShiftKey != null) {
	            if (shiftState) {
	            	if (mDebug) Log.d(TAG, "Switching to regular ON shift icon - shifted");
	            	mShiftKey.icon = mOnShiftIcon;
	            	mShiftKey.iconPreview = mOnShiftFeedbackIcon;
	            } else {
	            	if (mDebug) Log.d(TAG, "Switching to regular OFF shift icon - un-shifted");
	            	mShiftKey.icon = mOffShiftIcon;
	            	mShiftKey.iconPreview = mOffShiftFeedbackIcon;
	            }
	        }
			return true;
		}
		else
			return false;
	}
	
	public boolean isShiftLocked() {
		return mShiftState == SHIFT_LOCKED;
	}
	
	protected void setPopupKeyChars(Key aKey) 
	{
		if (aKey.popupResId > 0)
			return;//if the keyboard XML already specified the popup, then no need to override
		
		if ((aKey.codes != null) && (aKey.codes.length > 0))
        {
			switch(((char)aKey.codes[0]))
			{
			case '\''://in the generic bottom row
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = "\"\u201e\u201d";
				break;
			case '-':
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = "\u2013";
				break;
			case '.'://in the generic bottom row
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = ";:-_\u00b7\u2026";
				break;
			case ','://in the generic bottom row
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = "()";
				break;
			case '_':
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = ",-";
				break;
			//the two below are switched in regular and Internet mode
			case '?'://in the generic bottom row
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = POPUP_FOR_QUESTION;
				break;
			case '@'://in the generic Internet mode
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = POPUP_FOR_AT;
				break;
			}
        }
	}

	public void setTextVariation(Resources res, int inputType) 
	{
		if (mDebug)
    		Log.d(TAG, "setTextVariation");
		int variation = inputType &  EditorInfo.TYPE_MASK_VARIATION;
		
		switch (variation) {
	        case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
	        case EditorInfo.TYPE_TEXT_VARIATION_URI:
	        	if (mSmileyKey != null)
	        	{
	        		//Log.d("AnySoftKeyboard", "Changing smiley key to domains.");
	        		setKeyIcons(mSmileyKey, res, mDomainsIconId, R.drawable.sym_keyboard_key_domain_preview);
	        		mSmileyKey.text = AnySoftKeyboardConfiguration.getInstance().getDomainText();
		        	mSmileyKey.popupResId = R.xml.popup_domains;
	        	}
	        	if (mQuestionMarkKey != null)
	        	{
	        		//Log.d("AnySoftKeyboard", "Changing question mark key to AT.");
		        	mQuestionMarkKey.codes[0] = (int)'@';
		        	mQuestionMarkKey.label = "@";
		        	mQuestionMarkKey.popupCharacters = POPUP_FOR_AT;
	        	}
	        	break;
	        default:
	        	if (mSmileyKey != null)
	        	{
	        		setKeyIcons(mSmileyKey, res, R.drawable.sym_keyboard_smiley, R.drawable.sym_keyboard_smiley_feedback);
		        	mSmileyKey.text = null;// ":-) ";
		        	mSmileyKey.popupResId = R.xml.popup_smileys;
	        	}
	        	if (mQuestionMarkKey != null)
	        	{
	        		//Log.d("AnySoftKeyboard", "Changing question mark key to question.");
		        	mQuestionMarkKey.codes[0] = (int)'?';
		        	mQuestionMarkKey.label = "?";
		        	mQuestionMarkKey.popupCharacters = POPUP_FOR_QUESTION;
	        	}
	        	break;
        }
	}
	
	public int getShiftedKeyValue(int primaryCode) 
	{
//		if ((primaryCode>0) && (primaryCode<Character.MAX_VALUE))
//		{
//			Character c = new Character((char)primaryCode);
//			if (mSpecialShiftKeys.containsKey(c))
//			{
//				char shifted = mSpecialShiftKeys.get(c).ShiftCharacter;
//				if (mDebug)
//		        	Log.v(TAG, "Returned the shifted mapping ("+c+"->"+shifted+") from mSpecialShiftKeys.");
//				return shifted;
//			}
//		}
		//else...best try.
		return Character.toUpperCase(primaryCode);
	}
	
	static class AnyKey extends Keyboard.Key {
        //private boolean mShiftLockEnabled;
        
        public AnyKey(Resources res, Keyboard.Row parent, int x, int y, 
                XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            if (popupCharacters != null && popupCharacters.length() == 0) {
                // If there is a keyboard with no keys specified in popupCharacters
                popupResId = 0;
            }
        }
        //Issue 395
        protected int[] parseCSV(String value) {
            int count = 0;
            int lastIndex = 0;
            if (value.length() > 0) {
                count++;
                while ((lastIndex = value.indexOf(",", lastIndex + 1)) > 0) {
                    count++;
                }
            }
            int[] values = new int[count];
            count = 0;
            StringTokenizer st = new StringTokenizer(value, ",");
            while (st.hasMoreTokens()) {
            	String nextToken = st.nextToken();
                try {
                	if(nextToken.length() != 1 ){// length==0 means, Letters and perhaps 0-9
                		//It is not interpreted by us as a keyCode :(
                		//There are no printable Codes with length 1
                		values[count++] = Integer.parseInt(nextToken);
                	}else {
                		// length == 1, assume a char!
                		values[count++] = (int)nextToken.charAt(0);
                	}
                } catch (NumberFormatException nfe) {
                    Log.e(TAG, "Error parsing keycodes " + value);
                }
            }
            return values;
        }

        
//        void enableShiftLock() {
//            mShiftLockEnabled = true;
//        }
//
//        @Override
//        public void onReleased(boolean inside) {
//            if (!mShiftLockEnabled) {
//                super.onReleased(inside);
//            } else {
//                pressed = !pressed;
//            }
//        }
    }
	
	protected static class LessSensitiveAnyKey extends AnyKey {
        
		private int mStartX;
		private int mStartY;
		private int mEndX;
		private int mEndY;
		
        public LessSensitiveAnyKey(Resources res, Keyboard.Row parent, int x, int y, 
                XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            resetSenitivity();
        }
        
        void resetSenitivity()
        {
        	mStartX = this.x;
            mStartY = this.y;
            mEndX = this.width + this.x;
            mEndY = this.height + this.y;
        	
            if ((this.edgeFlags & Keyboard.EDGE_BOTTOM) != 0)
            {//the enter key!
            	//we want to "click" it only if it in the lower
        		mStartY += (this.height * 0.15);
            }
            else
            {
	            if ((this.edgeFlags & Keyboard.EDGE_LEFT) != 0)
	            {//usually, shift
	            	mEndX -= (this.width * 0.1);
	            }
	            
	            if ((this.edgeFlags & Keyboard.EDGE_RIGHT) != 0)
	            {//usually, delete
	            	//this is below the ENTER.. We want to be careful with this.
	            	mStartY += (this.height * 0.05);
	        		mEndY -= (this.height * 0.05);
	        		mStartX += (this.width * 0.15);
	            }
            }
        }
        
        
         /**
         * Overriding this method so that we can reduce the target area for certain keys.
         */
        @Override
        public boolean isInside(int clickedX, int clickedY) 
        {
        	return 	clickedX >= mStartX &&
				clickedX <= mEndX &&
				clickedY >= mStartY &&
				clickedY <= mEndY;
        }
    }

	private static class EnterKey extends LessSensitiveAnyKey
	{
		private final int mOriginalHeight;
		private boolean mEnabled;
		
		public EnterKey(Resources res, Row parent, int x, int y,
				XmlResourceParser parser) {
			super(res, parent, x, y, parser);
			mOriginalHeight = this.height;
			mEnabled = true;
		}
		
		public void disable()
		{
			if (AnySoftKeyboardConfiguration.getInstance().getActionKeyInvisibleWhenRequested())
				this.height = 0;
			
			iconPreview = null;
            icon = null;
            label = "  ";//can not use NULL.
            mEnabled = false;
		}
		
		public void enable()
		{
			this.height = mOriginalHeight;
			mEnabled = true;
		}
		
		@Override
		public boolean isInside(int clickedX, int clickedY) {
			if (mEnabled)
				return super.isInside(clickedX, clickedY);
			else
				return false;//disabled.
		}
	}
	
	public abstract String getKeyboardPrefId();
}
