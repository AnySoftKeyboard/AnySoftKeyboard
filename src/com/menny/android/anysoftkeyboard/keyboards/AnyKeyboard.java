package com.menny.android.anysoftkeyboard.keyboards;

import java.util.HashMap;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
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
	protected static class ShiftedKeyData
	{
		public final char ShiftCharacter;
		public final AnyKey KeyboardKey;
		
		public ShiftedKeyData(AnyKey key)
		{
			KeyboardKey = key;
			ShiftCharacter = (char) key.codes[1]; 
		}
	}
	public final static int KEYCODE_LANG_CHANGE = -99;
	public final static int KEYCODE_ALTER_LAYOUT = -98;
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
	
	private static final int SHIFT_OFF = 0;
    private static final int SHIFT_ON = 1;
    private static final int SHIFT_LOCKED = 2;
    
    private int mShiftState = SHIFT_OFF;
    
    private final boolean mDebug;
	private HashMap<Character, ShiftedKeyData> mSpecialShiftKeys;
    
    //private Drawable mShiftLockIcon;
    //private Drawable mShiftLockPreviewIcon;
    private final Drawable mOffShiftIcon;
    private final Drawable mOnShiftIcon;
    //private Drawable mOldShiftPreviewIcon;
    private Key mShiftKey;
    private EnterKey mEnterKey;
	private Key mSmileyKey;
	private Key mQuestionMarkKey;

	private boolean mRightToLeftLayout = false;//the "super" ctor will create keys, and we'll set the correct value there.
	
    private final Context mKeyboardContext;
    private final AnyKeyboardContextProvider mASKContext;
	
    protected AnyKeyboard(AnyKeyboardContextProvider askContext, Context context,//note: the context can be from a different package!
    		int xmlLayoutResId) 
    {
        //should use the package context for creating the layout
        super(context, xmlLayoutResId);
        
        mDebug = AnySoftKeyboardConfiguration.getInstance().getDEBUG();
        mKeyboardContext = context;
        mASKContext = askContext;
        
        mOnShiftIcon = askContext.getApplicationContext().getResources().getDrawable(R.drawable.sym_keyboard_shift_on);
        mOffShiftIcon = askContext.getApplicationContext().getResources().getDrawable(R.drawable.sym_keyboard_shift);
    }
    
    public void initKeysMembers()
    {
    	final Resources localResources = getASKContext().getApplicationContext().getResources();
        for(final Key key : getKeys())
        {
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
                    key.icon = localResources.getDrawable(R.drawable.sym_keyboard_delete_small);
                    break;
                case AnyKeyboard.KEYCODE_SHIFT:
                	mShiftKey = key;//I want the reference used by the super.
                    key.icon = localResources.getDrawable(R.drawable.sym_keyboard_shift);
                    break;
                case AnyKeyboard.KEYCODE_CTRL:
                    key.icon = localResources.getDrawable(R.drawable.sym_keyboard_ctrl);
                    break;
                case 32://SPACE
                    key.icon = localResources.getDrawable(R.drawable.sym_keyboard_space);
                    break;
                case 9://TAB
                    key.icon = localResources.getDrawable(R.drawable.tab_key);
                    break;
                case 63:
                    if ((key.edgeFlags & Keyboard.EDGE_BOTTOM) != 0)
                    {
                    	mQuestionMarkKey = key;
                    }
                    break;
                case Keyboard.KEYCODE_MODE_CHANGE:
                case AnyKeyboard.KEYCODE_LANG_CHANGE:
                	if ((key.edgeFlags & Keyboard.EDGE_TOP) != 0)
                	{//these keys should only be resized if they are in the top row.
	                	final String keysMode = AnySoftKeyboardConfiguration.getInstance().getChangeLayoutKeysSize();
	                    if (keysMode.equals("None"))
	                    {
	                        key.label = null;
	                        key.height = 0;
	                        key.width = 0;
	                    }
	                    else if (keysMode.equals("Big"))
	                    {
	                        String keyText = (primaryCode == Keyboard.KEYCODE_MODE_CHANGE)?
	                                mASKContext.getApplicationContext().getString(R.string.change_symbols_regular) :
	                                	mASKContext.getApplicationContext().getString(R.string.change_lang_regular);
	                        key.label = keyText;
	                        //key.height *= 1.5;
	                    }
	                    else
	                    {
	                        String keyText = (primaryCode == Keyboard.KEYCODE_MODE_CHANGE)?
	                        		mASKContext.getApplicationContext().getString(R.string.change_symbols_wide) :
	                        			mASKContext.getApplicationContext().getString(R.string.change_lang_wide);
	                        key.label = keyText;
	                    }
                	}
                    break;
                    default:
                        //setting the character label
                        if (isAlphabetKey(key))
                        {
                            key.label = ""+((char)primaryCode); 
                        }
                        else
                        {
                        	onInitUnknownKey(key);
                        }
                }
            }
        }
    }
    
	protected void onInitUnknownKey(Key key) {
		
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
    	if (mSpecialShiftKeys == null) mSpecialShiftKeys = new HashMap<Character, ShiftedKeyData>();
    	
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
        
        if ((key.codes != null) && (key.codes.length > 1))
        {
        	final int primaryCode = key.codes[0];
        	if ((primaryCode>0) && (primaryCode<Character.MAX_VALUE))
        	{
        		Character primary = new Character((char)primaryCode);
        		ShiftedKeyData keyData = new ShiftedKeyData(key);
	        	if (!mSpecialShiftKeys.containsKey(primary))
	        		mSpecialShiftKeys.put(primary, keyData);
	        	if (mDebug)
	            	Log.v(TAG, "Adding mapping ("+primary+"->"+keyData.ShiftCharacter+") to mSpecialShiftKeys.");
	        }
        }
        		
        return key;
    }

    @Override
    protected Row createRowFromXml(Resources res, XmlResourceParser parser) 
    {
    	Row aRow = super.createRowFromXml(res, parser);
    	if ((aRow.rowEdgeFlags&EDGE_TOP) != 0)
    	{
    		String layoutChangeType = AnySoftKeyboardConfiguration.getInstance().getChangeLayoutKeysSize();
    		//top row
    		if (layoutChangeType.equals("None"))
    			aRow.defaultHeight = 0;
    		else if (layoutChangeType.equals("Big"))
    			aRow.defaultHeight *= 1.5;
    	}
    	
    	if (res.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
    		aRow.defaultHeight = (int)(aRow.defaultHeight * AnySoftKeyboardConfiguration.getInstance().getKeysHeightFactorInPortrait());
    	else if (res.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
    		aRow.defaultHeight = (int)(aRow.defaultHeight * AnySoftKeyboardConfiguration.getInstance().getKeysHeightFactorInLandscape());
    		
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
	                mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_search);
	                mEnterKey.label = null;
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
		            	mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_return);
			            mEnterKey.label = null;
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
            return super.isShifted();
        }
    }
    
	@Override
	public boolean setShifted(boolean shiftState) 
	{
		final boolean superResult = super.setShifted(shiftState);
		//making sure it is off. Only caps turn it on. The super will turn the lit on when
		//shift is ON, and not when CAPS is on.
        if (mShiftKey != null) mShiftKey.on = false;
        
		final boolean changed = (shiftState == (mShiftState == SHIFT_OFF));
		
		if (mDebug) Log.d(TAG, "setShifted: shiftState:"+shiftState+". super result:"+superResult + " changed: "+changed);
		
		if (changed || superResult)
		{//layout changed. Need to change labels.
			mShiftState = shiftState? SHIFT_ON : SHIFT_OFF;
			
			//going over the special keys only.
			for(ShiftedKeyData data : mSpecialShiftKeys.values())
			{
				onKeyShifted(data, shiftState);
			}
			
			if (mShiftKey != null) {
	            if (shiftState) {
	            	if (mDebug) Log.d(TAG, "Switching to regular ON shift icon - shifted");
	            	mShiftKey.icon = mOnShiftIcon;
	            } else {
	            	if (mDebug) Log.d(TAG, "Switching to regular OFF shift icon - un-shifted");
	            	mShiftKey.icon = mOffShiftIcon;
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

	protected void onKeyShifted(ShiftedKeyData data, boolean shiftState) 
	{
		AnyKey aKey = data.KeyboardKey;
		aKey.label = shiftState? ""+data.ShiftCharacter : ""+((char)aKey.codes[0]);
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
	        		mSmileyKey.iconPreview = null;// res.getDrawable(sym_keyboard_key_domain_preview);
	        		mSmileyKey.icon = res.getDrawable(R.drawable.sym_keyboard_key_domain);
		        	mSmileyKey.label = null;
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
	        		//Log.d("AnySoftKeyboard", "Changing smiley key to smiley.");
	        		mSmileyKey.icon = res.getDrawable(R.drawable.sym_keyboard_smiley);
		        	mSmileyKey.label = null;
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
		if ((primaryCode>0) && (primaryCode<Character.MAX_VALUE))
		{
			Character c = new Character((char)primaryCode);
			if (mSpecialShiftKeys.containsKey(c))
			{
				char shifted = mSpecialShiftKeys.get(c).ShiftCharacter;
				if (mDebug)
		        	Log.v(TAG, "Returned the shifted mapping ("+c+"->"+shifted+") from mSpecialShiftKeys.");
				return shifted;
			}
		}
		//else...best try.
		return Character.toUpperCase(primaryCode);
	}
	
	class AnyKey extends Keyboard.Key {
        //private boolean mShiftLockEnabled;
        
        public AnyKey(Resources res, Keyboard.Row parent, int x, int y, 
                XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            if (popupCharacters != null && popupCharacters.length() == 0) {
                // If there is a keyboard with no keys specified in popupCharacters
                popupResId = 0;
            }
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
	
	private class LessSensitiveAnyKey extends AnyKey {
        
		private int mStartX;
		private int mStartY;
		private int mEndX;
		private int mEndY;
		
        public LessSensitiveAnyKey(Resources res, Keyboard.Row parent, int x, int y, 
                XmlResourceParser parser) {
            super(res, parent, x, y, parser);
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

	private class EnterKey extends LessSensitiveAnyKey
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
