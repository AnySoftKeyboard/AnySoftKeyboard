package com.anysoftkeyboard.keyboards;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.AnyKeyboardContextProvider;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.utils.Workarounds;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnyKeyboard extends Keyboard 
{
	private final static String TAG = "ASK - AK";
	
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
	
	private static final int STICKY_KEY_OFF = 0;
    private static final int STICKY_KEY_ON = 1;
    private static final int STICKY_KEY_LOCKED = 2;
    
    private int mShiftState = STICKY_KEY_OFF;
    private int mControlState = STICKY_KEY_OFF;
    
    //private final boolean mDebug;
	
//    private final Drawable mShiftIcon;
//    private final Drawable mShiftOnIcon;
//    private final Drawable mShiftLockedIcon;
    //private final Drawable mShiftFeedbackIcon;
    //private final Drawable mShiftOnFeedbackIcon;
    //private final Drawable mShiftLockedFeedbackIcon;
    
    private Key mShiftKey;
    private Key mControlKey;
    private EnterKey mEnterKey;
    //public Key langSwitch;

	private boolean mRightToLeftLayout = false;//the "super" ctor will create keys, and we'll set the correct value there.
	
    private boolean mTopRowWasCreated;
	private boolean mBottomRowWasCreated;
	
	private int mGenericRowsHeight = 0;
	private int mTopRowKeysCount = 0;
	// max(generic row widths)
	private int mMaxGenericRowsWidth = 0;
	
	private KeyboardCondensor mKeyboardCondensor;
	private int mKeyboardActionType = EditorInfo.IME_ACTION_NONE;
	
	//for popup keyboard
	protected AnyKeyboard(AnyKeyboardContextProvider askContext, Context context,//note: the context can be from a different package!
    		int xmlLayoutResId) 
    {
        //should use the package context for creating the layout
        super(askContext, context, xmlLayoutResId, -1);
        //no generic rows in popup
    }
	
	//for the External
    protected AnyKeyboard(AnyKeyboardContextProvider askContext, Context context,//note: the context can be from a different package!
    		int xmlLayoutResId, int mode) 
    {
        //should use the package context for creating the layout
        super(askContext, context, xmlLayoutResId, mode);
    }
    
    public void loadKeyboard(final KeyboardDimens keyboardDimens) {
    	super.loadKeyboard(keyboardDimens);
    	
    	addGenericRows(mASKContext, mKeyboardContext, mKeyboardMode, keyboardDimens);
		initKeysMembers();
    };
    
    private void initKeysMembers()
    {
    	for(final Key key : getKeys())
        {
        	if (key.y == 0) key.edgeFlags |= Keyboard.EDGE_TOP;
        	
        	//Log.d(TAG, "Key x:"+key.x+" y:"+key.y+" width:"+key.width+" height:"+key.height);
            if ((key.codes != null) && (key.codes.length > 0))
            {
            	final int primaryCode = key.codes[0];
            	if (key instanceof AnyKey)
            	{
            		switch(primaryCode)
                    {
                    case KeyCodes.DELETE:
                    case KeyCodes.CTRL:
                    case KeyCodes.MODE_ALPHABET:
                    case KeyCodes.KEYBOARD_MODE_CHANGE:
                    case KeyCodes.KEYBOARD_CYCLE:
                    case KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE:
                    case KeyCodes.KEYBOARD_REVERSE_CYCLE:
                    case KeyCodes.ALT:
                    case KeyCodes.MODE_SYMOBLS:
                    case KeyCodes.QUICK_TEXT:
                    case KeyCodes.DOMAIN:
                    case KeyCodes.CANCEL:
                    	((AnyKey)key).setAsFunctional();
                    	break;
                    }
            	}
                
                //detecting LTR languages
                if (Workarounds.isRightToLeftCharacter((char)primaryCode))
                	mRightToLeftLayout = true;//one is enough
                switch(primaryCode)
                {
                case KeyCodes.QUICK_TEXT:
					QuickTextKey quickKey = QuickTextKeyFactory.getCurrentQuickTextKey(getASKContext());
					if (quickKey == null) { //No plugins. Weird, but we can't do anything
						Log.w(TAG, "Could not locate any quick key plugins! Hopefully nothing will crash...");
						break;
					}

					Resources quickTextKeyResources = quickKey.getPackageContext().getResources();

					key.label = quickKey.getKeyLabel();

					int iconResId = quickKey.getKeyIconResId();
					int previewResId = quickKey.getIconPreviewResId();
					if (iconResId > 0) {
						setKeyIcons(key, quickTextKeyResources, iconResId, previewResId);
					}

					/* Popup resource may be from another context, requires special handling when
					the key is long-pressed! */
                	key.popupResId = quickKey.getPopupKeyboardResId();
                    break;
            	case KeyCodes.DOMAIN:
            		//fixing icons
                	key.label = AnyApplication.getConfig().getDomainText().trim();
            		key.popupResId = R.xml.popup_domains;
                	break;
               default:
	                //setting the character label
	                if (isAlphabetKey(key) && (key.icon == null))
	                {
	                	if (TextUtils.isEmpty(key.label))
	                	{
		                	final char code = (char)key.codes[0];
		                	if (code > 0 && !Character.isWhitespace(code))
		                		key.label = ""+code;
	                	}
	                	if (key instanceof AnyKey)
	                	{
	                		AnyKey anyKey = (AnyKey)key;
		                	if (TextUtils.isEmpty(anyKey.shiftedKeyLabel))
		                	{
			                	final char code = (char)anyKey.shiftedCodes[0];
			                	if (code > 0 && !Character.isWhitespace(code))
			                		anyKey.shiftedKeyLabel = ""+code;
		                	}
	                	}
	                }
                }
            }
        }
		
		mKeyboardCondensor = new KeyboardCondensor(this);
    }

	protected void addGenericRows(AnyKeyboardContextProvider askContext, Context context, int mode, final KeyboardDimens keyboardDimens) {
		final KeyboardMetadata topMd;
		if (!mTopRowWasCreated)
		{
			final KeyboardExtension topRowPlugin = KeyboardExtensionFactory.getCurrentKeyboardExtension(getASKContext().getApplicationContext(), KeyboardExtension.TYPE_TOP);
	        if (topRowPlugin == null || //no plugin found
	        	topRowPlugin.getKeyboardResId() == -1 || //plugin specified to be empty
	        	topRowPlugin.getKeyboardResId() == -2)//could not parse layout res id
	        {
	        	if (AnyApplication.DEBUG) Log.d(TAG, "No top row layout");
	        	topMd = null;
	        	//adding EDGE_TOP to top keys. See issue 775
	        	List<Key> keys = getKeys();
	    		for(int keyIndex = 0; keyIndex < keys.size(); keyIndex++)
	            {
	    			final Key key = keys.get(keyIndex);
	    			if (key.y == 0) key.edgeFlags = key.edgeFlags | Keyboard.EDGE_TOP;
	            }
	        }
	        else
	        {
	        	if (AnyApplication.DEBUG) Log.d(TAG, "Top row layout id "+topRowPlugin.getId());
	        	topMd = addKeyboardRow(topRowPlugin.getPackageContext(), topRowPlugin.getKeyboardResId(), mode, keyboardDimens);
	        }
        
			if (topMd != null)
				fixKeyboardDueToGenericRow(topMd, (int)keyboardDimens.getRowVerticalGap());
		}
		if (!mBottomRowWasCreated)
		{
			final KeyboardExtension bottomRowPlugin = KeyboardExtensionFactory.getCurrentKeyboardExtension(getASKContext().getApplicationContext(), KeyboardExtension.TYPE_BOTTOM);
			if (AnyApplication.DEBUG) Log.d(TAG, "Bottom row layout id "+bottomRowPlugin.getId());
			KeyboardMetadata bottomMd = addKeyboardRow(bottomRowPlugin.getPackageContext(), bottomRowPlugin.getKeyboardResId(), mode, keyboardDimens);
			fixKeyboardDueToGenericRow(bottomMd, (int)keyboardDimens.getRowVerticalGap());
		}
	}

    private void fixKeyboardDueToGenericRow(KeyboardMetadata md, int rowVerticalGap) {
    	final int additionalPixels = (md.rowHeight + md.verticalGap + rowVerticalGap);
    	mGenericRowsHeight += additionalPixels;
    	if (md.isTopRow)
    	{
    		mTopRowKeysCount += md.keysCount;
    		List<Key> keys = getKeys();
    		for(int keyIndex = md.keysCount; keyIndex < keys.size(); keyIndex++)
            {
    			final Key key = keys.get(keyIndex);
    			key.y += additionalPixels;
//    			if (key instanceof LessSensitiveAnyKey)
//            		((LessSensitiveAnyKey)key).resetSenitivity();//reseting cause the key may be offseted now (generic rows)
            }
    	}/* else {
    		// The height should not include any gap below that last row
    		// this corresponds to
    		// mTotalHeight = y - mDefaultVerticalGap;
    		// in the Keyboard class from Android sources

    		// Note that we are using keyboard default vertical gap (instead of row vertical gap)
    		// as this is done also in Android sources.
    		mGenericRowsHeight -= getVerticalGap();
    	}*/
	}

	private KeyboardMetadata addKeyboardRow(Context context, int rowResId, int mode, final KeyboardDimens keyboardDimens) {
		XmlResourceParser parser = context.getResources().getXml(rowResId);
    	List<Key> keys = getKeys();
        boolean inKey = false;
        boolean inRow = false;
        //boolean leftMostKey = false;
        boolean skipRow = false;

        final float keyHorizontalGap = keyboardDimens.getKeyHorizontalGap();
        final float rowVerticalGap = keyboardDimens.getRowVerticalGap();
        int row = 0;
        float x = 0;
        float y = rowVerticalGap;
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
                        currentRow = createRowFromXml(mASKContext, res, parser);
                        skipRow = currentRow.mode != 0 && currentRow.mode != mode;
                        if (skipRow) {
                        	currentRow = null;
                            skipToEndOfRow(parser);
                            inRow = false;
                        }
                        else
                        {
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
	                        m.rowHeight = 0;
	                        m.verticalGap = currentRow.verticalGap;
                        }
                   } else if (TAG_KEY.equals(tag)) {
                        inKey = true;
                        x += (keyHorizontalGap/2);
                        key = createKeyFromXml(mASKContext, res, currentRow, keyboardDimens, (int)x, (int)y, parser);
                        key.width -= keyHorizontalGap;//the gap is on both sides
                        if (m.isTopRow)
                        	keys.add(m.keysCount, key);
                        else
                        	keys.add(key);
                        m.keysCount++;
                        
                        m.rowHeight = Math.max(key.height, m.rowHeight);
                    }
                } else if (event == XmlResourceParser.END_TAG) {
                    if (inKey) {
                        inKey = false;
                        x += (key.gap + key.width);
                        x += (keyHorizontalGap/2);
                        if (x > m.rowWidth) {
                        	m.rowWidth = (int)x;
                        	// We keep generic row max width updated
                    		mMaxGenericRowsWidth = Math.max(mMaxGenericRowsWidth, m.rowWidth);
                        }
                    } else if (inRow) {
                        inRow = false;
                        y += currentRow.verticalGap;
                        y += m.rowHeight;
                        y += rowVerticalGap;
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

	private void skipToEndOfRow(XmlResourceParser parser) throws XmlPullParserException, IOException
	{
		int event;
		while ((event = parser.next()) != XmlResourceParser.END_DOCUMENT) {
		    if (event == XmlResourceParser.END_TAG 
		            && parser.getName().equals(TAG_ROW)) {
		        break;
		    }
		}
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
    
    /*
	private void setIconIfNeeded(Key key, Resources localResources,
			int iconId, int iconWideId,
			int iconFeedbackId) {

		Drawable wider = localResources.getDrawable(iconWideId);
		if ((wider.getMinimumWidth()*1.1) < key.width)
			iconId = iconWideId;
		
		setKeyIcons(key, localResources, iconId, iconFeedbackId);
	}

	private void setIconIfNeeded(Key key, Resources localResources, int iconId, int iconFeedbackId) {
		if ((key.icon != null) || (!TextUtils.isEmpty(key.label)))
			return;
		setKeyIcons(key, localResources, iconId, iconFeedbackId);
	}
*/
	private void setKeyIcons(Key key, Resources localResources, int iconId, int iconFeedbackId) {
		key.icon = localResources.getDrawable(iconId);
		if (iconFeedbackId > 0)
		{
			Drawable preview = localResources.getDrawable(iconFeedbackId);
    		preview.setBounds(0, 0, 
    				preview.getIntrinsicWidth(), preview.getIntrinsicHeight());
    		key.iconPreview = preview;
		}
		
		if (key.icon != null)
			key.label = null;
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
    protected Key createKeyFromXml(AnyKeyboardContextProvider askContext, Resources res, Row parent, KeyboardDimens keyboardDimens, int x, int y, 
            XmlResourceParser parser) {
    	AnyKey key = new AnyKey(askContext, res, parent, keyboardDimens, x, y, parser);
    	
        if ((key.codes != null) && (key.codes.length > 0))
        {
        	final int primaryCode = key.codes[0];
    		
        	//creating less sensitive keys if required
        	switch(primaryCode)
        	{
        	case KeyCodes.DISABLED://disabled
        		key.disable();
        		break;
            case KeyCodes.ENTER://enter
        		key = mEnterKey = new EnterKey(mASKContext, res, parent, keyboardDimens, x, y, parser);
        		break;
        	case KeyCodes.SHIFT:
        		mShiftKey = key;//I want the reference used by the super.
        		break;
        	case KeyCodes.CTRL:
        		mControlKey = key;
        		break;
//        	case KeyCodes.DELETE://delete
//        		key = new LessSensitiveAnyKey(mASKContext, res, parent, x, y, parser);
//        		break;
	        }
        }
        
        setPopupKeyChars(key);
        
        if (!TextUtils.isEmpty(key.label))
        	key.label = key.label;
        		
        return key;
    }

    @Override
    protected Row createRowFromXml(AnyKeyboardContextProvider askContext, Resources res, XmlResourceParser parser) 
    {
    	Row aRow = super.createRowFromXml(askContext, res, parser);
    	if (aRow.mode > 0)
    		aRow.mode = res.getInteger(aRow.mode);//switching to the mode!
    	
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

    public boolean isStartOfWordLetter(char keyValue)
    {
    	return Character.isLetter(keyValue)/* || (keyValue == '\'')*/;
    }
    
    public boolean isInnerWordLetter(char keyValue)
    {
    	return Character.isLetter(keyValue) || (keyValue == '\'');
    }
    
    public abstract HashSet<Character> getSentenceSeparators();
    
	/**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    public void setImeOptions(Resources res, EditorInfo editor) {
    	if (AnyApplication.DEBUG)
    	{
	    	if (editor == null)
	    		Log.d(TAG, "AnyKeyboard.setImeOptions");
			else
				Log.d(TAG, "AnyKeyboard.setImeOptions. package: "+editor.packageName+", id:"+editor.fieldId);
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
//        CharSequence imeLabel = (editor == null)? null :editor.actionLabel;
//        int imeActionId = (editor == null)? -1 :editor.actionId;
        
        mEnterKey.enable();
        
        //Used in conjunction with a custom action, this indicates that the action should not be available in-line 
        //as a replacement for the "enter" key. Typically this is because the action has such a significant impact 
        //or is not recoverable enough that accidentally hitting it should be avoided, such as sending a message. 
        //Note that TextView  will automatically set this flag for you on multi-line text views. 
        boolean inNoEnterActionMode = ((options&EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0);
        
    	final int action = (options&EditorInfo.IME_MASK_ACTION);
    	
    	if (AnyApplication.DEBUG) 
    		Log.d(TAG, "Input Connection ENTER key with action: "+action + " and NO_ACTION flag is: "+inNoEnterActionMode);

    	if (inNoEnterActionMode)
    	{
    		//this means that the ENTER should not be replaced with a custom action.
    		//maybe in future ASK releases, we'll add the custom action key.
    		//setKeyIcons(mEnterKey, res, R.drawable.sym_keyboard_return, -1/*R.drawable.sym_keyboard_feedback_return*/);
    		mKeyboardActionType = EditorInfo.IME_ACTION_NONE;
    	}
    	else
    	{
    		mKeyboardActionType  = action;
	        switch (action) {
	            case EditorInfo.IME_ACTION_GO:
	                mEnterKey.label = res.getText(R.string.label_go_key);
	            	break;
	            case EditorInfo.IME_ACTION_NEXT:
	                mEnterKey.label = res.getText(R.string.label_next_key);
	            	break;
	            case EditorInfo.IME_ACTION_DONE:
	            	mEnterKey.label = null;
	            	//setKeyIcons(mEnterKey, res, R.drawable.sym_keyboard_done, -1/*R.drawable.sym_keyboard_done_black*/);
	            	break;
	            case EditorInfo.IME_ACTION_SEARCH:
	            	mEnterKey.label = null;
	            	//setKeyIcons(mEnterKey, res, R.drawable.sym_keyboard_search, -1/*R.drawable.sym_keyboard_feedback_search*/);
	                break;
	            case EditorInfo.IME_ACTION_SEND:
	            	mEnterKey.label = res.getText(R.string.label_send_key);
	            	break;
	            case EditorInfo.IME_ACTION_NONE:
	            case EditorInfo.IME_ACTION_UNSPECIFIED:
	            default:
	            	mEnterKey.label = null;
	            	mKeyboardActionType = EditorInfo.IME_ACTION_UNSPECIFIED;
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
//	            		setKeyIcons(mEnterKey, res, R.drawable.sym_keyboard_return, -1/*R.drawable.sym_keyboard_feedback_return*/);
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
    


    public boolean setShiftLocked(boolean shiftLocked) {
        if (mShiftKey != null) {
        	final int initialState = mShiftState;
            if (shiftLocked) {
                mShiftState = STICKY_KEY_LOCKED;
            } else if (mShiftState == STICKY_KEY_LOCKED) {
                mShiftState = STICKY_KEY_ON;
            }
            
            setShiftViewAsState();
            return initialState != mShiftState;
        }
        
        return false;
    }
    
    @Override
    public boolean isShifted() {
        if (mShiftKey != null) {
            return mShiftState != STICKY_KEY_OFF;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean setShifted(boolean shiftState) {
        if (mShiftKey != null) {
        	final int initialState = mShiftState;
            if (shiftState) {
            	if (mShiftState == STICKY_KEY_OFF) {
                    mShiftState = STICKY_KEY_ON;
                }
            	//else this is already ON, or at caps lock.
            } else {
                mShiftState = STICKY_KEY_OFF;
            }

            setShiftViewAsState();
            return mShiftState != initialState;
        } else {
            return super.setShifted(shiftState);
        }
    }

	private void setShiftViewAsState() {
		//the "on" led is just like the caps-lock led
		mShiftKey.on = (mShiftState == STICKY_KEY_LOCKED);
	}
    
	public boolean isShiftLocked() {
		return mShiftState == STICKY_KEY_LOCKED;
	}
    
    public boolean isControl() {
        if (mControlKey != null) {
            return mControlState != STICKY_KEY_OFF;
        } else {
            return false;
        }
    }
    
    public boolean setControl(boolean control) {
        boolean controlChanged = false;
        if (mControlKey != null) {
            if (control == false) {
            	controlChanged = mControlState != STICKY_KEY_OFF;
            	mControlState = STICKY_KEY_OFF;
            } else {
                if (mControlState == STICKY_KEY_OFF) {
                	controlChanged = mControlState == STICKY_KEY_OFF;
                	mControlState = STICKY_KEY_ON;
                }
            }
            
            setControlViewAsState();
        } else {
            return false;
        }
        return controlChanged;
    }

    public void setControlLocked(boolean controlLocked) {
        if (mControlKey != null) {
            if (controlLocked) {
            	mControlState = STICKY_KEY_LOCKED;
            } else if (mControlState == STICKY_KEY_LOCKED) {
            	mControlState = STICKY_KEY_ON;
            }
            
            setControlViewAsState();
        }
    }
    
	private void setControlViewAsState() {
		//the "on" led is just like the caps-lock led
		mControlKey.on = (mControlState == STICKY_KEY_LOCKED);
	}
    
	public boolean isControlLocked() {
		return mControlState == STICKY_KEY_LOCKED;
	}
	
	protected void setPopupKeyChars(Key aKey)
	{
		
	}
	
	public int getKeyboardActionType() {
		return mKeyboardActionType;
	}
	
	public static class AnyKey extends Keyboard.Key {
		private final int[] KEY_STATE_FUNCTIONAL_NORMAL = {
				R.attr.key_type_function
        };

        // functional pressed state (with properties)
        private final int[] KEY_STATE_FUNCTIONAL_PRESSED = {
                R.attr.key_type_function,
                android.R.attr.state_pressed
        };
        
        public int[] shiftedCodes;
        public CharSequence shiftedKeyLabel;
        public int longPressCode;
        private boolean mFunctionalKey;
		private boolean mEnabled;
		
		public AnyKey(Row row, KeyboardDimens keyboardDimens)
		{
			super(row, keyboardDimens);
		}
		
        public AnyKey(AnyKeyboardContextProvider askContext, Resources res, Keyboard.Row parent, KeyboardDimens keyboardDimens, int x, int y, 
                XmlResourceParser parser) {
            super(askContext, res, parent, keyboardDimens, x, y, parser);
            mEnabled = true;
            mFunctionalKey = false;
            
            if (popupCharacters != null && popupCharacters.length() == 0) {
                // If there is a keyboard with no keys specified in popupCharacters
                popupResId = 0;
            }
            
            TypedArray a = res.obtainAttributes(Xml.asAttributeSet(parser), R.styleable.Keyboard);
            /*shifted codes support*/
            TypedValue shiftedCodesValue = new TypedValue();
            
            if (a.getValue(R.styleable.Keyboard_Key_shiftedCodes, shiftedCodesValue))
            {
	            if (shiftedCodesValue.type == TypedValue.TYPE_INT_DEC 
	            		|| shiftedCodesValue.type == TypedValue.TYPE_INT_HEX) {
	                shiftedCodes = new int[] { shiftedCodesValue.data };
	            } else if (shiftedCodesValue.type == TypedValue.TYPE_STRING) {
	            	shiftedCodes = parseCSV(shiftedCodesValue.string.toString());
	            }
            }
            else
            {
            	//shifted codes were not specified. Using char.toupper
            	shiftedCodes = new int[0];
            	//I'll let the IF below fix the shiftedCodes array
            }
            //ensuring codes and shiftedCodes are the same size
            if (shiftedCodes.length != codes.length)
            {
            	int[] wrongSizedShiftCodes = shiftedCodes;
            	shiftedCodes = new int[codes.length];
            	int i=0;
            	for(i=0;i<wrongSizedShiftCodes.length&&i<codes.length;i++)
            		shiftedCodes[i] = wrongSizedShiftCodes[i];
            	for(/*starting from where i finished above*/;i<codes.length;i++)
            	{
            		final int code = codes[i];
            		if (Character.isLetter(code))
            			shiftedCodes[i] = Character.toUpperCase(code);
            		else
            			shiftedCodes[i] = code;
            	}
            }
            /*Shift label support*/
            shiftedKeyLabel = a.getString(R.styleable.Keyboard_Key_v2_shiftedKeyLabel);
            
            /*long press support*/
            longPressCode = a.getInt(R.styleable.Keyboard_Key_longPressCode, 0);
            mFunctionalKey = a.getBoolean(R.styleable.Keyboard_Key_isFunctional, false);
            
            a.recycle();
        }
        
		public void enable()
        {
        	mEnabled = true;
        }

        public void disable()
		{
			iconPreview = null;
            icon = null;
            label = "  ";//can not use NULL.
            mEnabled = false;
		}
        		
		public boolean isInside(int clickedX, int clickedY) {
			if (mEnabled)
				return super.isInside(clickedX, clickedY);
			else
				return false;//disabled.
		}
		
        public void setAsFunctional() {
        	mFunctionalKey = true;
		}
        
        @Override
        public int[] getCurrentDrawableState() {
            if (mFunctionalKey) {
                if (pressed) {
                    return KEY_STATE_FUNCTIONAL_PRESSED;
                } else {
                    return KEY_STATE_FUNCTIONAL_NORMAL;
                }
            }
            return super.getCurrentDrawableState();
        }
    }	
	
	private static class EnterKey extends AnyKey
	{
		private final int[] KEY_STATE_ACTION_NORMAL = {
				R.attr.key_type_action
        };

        // functional pressed state (with properties)
        private final int[] KEY_STATE_ACTION_PRESSED = {
        		R.attr.key_type_action,
                android.R.attr.state_pressed
        };
        
		private final int mOriginalHeight;
		
		public EnterKey(AnyKeyboardContextProvider askContext, Resources res, Row parent,KeyboardDimens keyboardDimens, int x, int y,
				XmlResourceParser parser) {
			super(askContext, res, parent, keyboardDimens, x, y, parser);
			mOriginalHeight = this.height;
		}
		
		@Override
		public void disable()
		{
			if (AnyApplication.getConfig().getActionKeyInvisibleWhenRequested())
				this.height = 0;
			super.disable();
		}
		
		@Override
		public void enable()
		{
			this.height = mOriginalHeight;
			super.enable();
		}
		
		@Override
        public int[] getCurrentDrawableState() {
			if (pressed) {
                return KEY_STATE_ACTION_PRESSED;
            } else {
                return KEY_STATE_ACTION_NORMAL;
            }
        }
	}
	
	public abstract String getKeyboardPrefId();

	public boolean requiresProximityCorrection() {
		return getKeys().size() > 20;
	}

	public int getKeyboardMode() {
		return mKeyboardMode;
	}
	
	public void setCondensedKeys(boolean condensed)
	{		
		mKeyboardCondensor.setCondensedKeys(condensed);
		computeNearestNeighbors();
	}
}
