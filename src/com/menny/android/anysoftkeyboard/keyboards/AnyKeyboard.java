
package com.menny.android.anysoftkeyboard.keyboards;

import java.util.HashMap;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.inputmethod.EditorInfo;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfigurationImpl;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.Workarounds;

public abstract class AnyKeyboard extends Keyboard 
{
	protected class ShiftedKeyData
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
	public final static int KEYCODE_SMILEY = -10;
	
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

	private class KeyboardMetaData
	{
		public String PrefString;
		public String KeyboardName;
		public int IconResId;
		public String DefaultDictionaryLanguage;
		
		public KeyboardMetaData()
		{
			PrefString = null;
			KeyboardName = "";
			IconResId = -1;
			DefaultDictionaryLanguage = "None";
		}
	}
	
	private static final String XML_META_DATA_TAG = "AnySoftKeyboardMetaData";
	private static final String XML_PREF_ID_ATTRIBUTE = "PrefString";
	private static final String XML_NAME_RES_ID_ATTRIBUTE = "KeyboardNameResId";
	private static final String XML_ICON_RES_ID_ATTRIBUTE = "KeyboardIconResId";
	private static final String XML_DICTIONARY_NAME_ATTRIBUTE = "DefaultDictionaryLanguage";
	
	private static final int SHIFT_OFF = 0;
    private static final int SHIFT_ON = 1;
    private static final int SHIFT_LOCKED = 2;
    
    private int mShiftState = SHIFT_OFF;
    
    private final boolean mDebug;
    private final KeyboardMetaData mKeyboardMetaData;
	private HashMap<Character, ShiftedKeyData> mSpecialShiftKeys;
    
    private Drawable mShiftLockIcon;
    //private Drawable mShiftLockPreviewIcon;
    private Drawable mOldShiftIcon;
    //private Drawable mOldShiftPreviewIcon;
    private Key mShiftKey;
    private Key mEnterKey;
	private Key mSmileyKey;
	private Key mQuestionMarkKey;
	
	private boolean mRightToLeftLayout;//the "super" ctor will create keys, and we'll set the correct value there.
	
    private final AnyKeyboardContextProvider mKeyboardContext;
    
    protected AnyKeyboard(AnyKeyboardContextProvider context,
    		int xmlLayoutResId) 
    {
        super(context.getApplicationContext(), xmlLayoutResId);
        mKeyboardMetaData = loadKeyboard(context.getApplicationContext(), xmlLayoutResId);
        
        Log.d("AnySoftKeyboard", "loadKeyboard result: "+"" +
        		"PrefString:"+ ((mKeyboardMetaData.PrefString!=null)? mKeyboardMetaData.PrefString : "NULL")+
        		" KeyboardName:" + mKeyboardMetaData.KeyboardName +
        		" IconResId:" + mKeyboardMetaData.IconResId +
        		" DefaultDictionaryLanguage:" + ((mKeyboardMetaData.PrefString!=null)? mKeyboardMetaData.DefaultDictionaryLanguage : "NULL"));
        
        mDebug = AnySoftKeyboardConfigurationImpl.getInstance().getDEBUG();
        mKeyboardContext = context;
        Log.i("AnySoftKeyboard", "Done creating keyboard: "+mKeyboardMetaData.KeyboardName+", which is LTR:"+isLeftToRightLanguage());
    	
        mShiftLockIcon = context.getApplicationContext().getResources().getDrawable(R.drawable.sym_keyboard_shift_locked);
//        mShiftLockPreviewIcon = context.getApplicationContext().getResources().getDrawable(R.drawable.sym_keyboard_feedback_shift_locked);
//        mShiftLockPreviewIcon.setBounds(0, 0, mShiftLockPreviewIcon.getIntrinsicWidth(),mShiftLockPreviewIcon.getIntrinsicHeight());
    }
    
    

	private KeyboardMetaData loadKeyboard(Context applicationContext, int xmlLayoutResId) {
		KeyboardMetaData result = new KeyboardMetaData();
		XmlPullParser parser = applicationContext.getResources().getXml(xmlLayoutResId);
		
        Resources res = applicationContext.getResources();
        boolean inMetaData = false;
        
        try {
            int event;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) 
            {
                if (event == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if (XML_META_DATA_TAG.equals(tag)) {
                    	inMetaData = true;
                    	Log.d("AnySoftKeyboard", "Starting parsing "+XML_META_DATA_TAG);
                    	AttributeSet attrs = Xml.asAttributeSet(parser);
                    	result.PrefString = attrs.getAttributeValue(null, XML_PREF_ID_ATTRIBUTE);
                    	final int nameResId = attrs.getAttributeResourceValue(null, XML_NAME_RES_ID_ATTRIBUTE, -1);
                    	result.KeyboardName = (nameResId > 0)? res.getString(nameResId) : "";
                    	result.IconResId = attrs.getAttributeResourceValue(null, XML_ICON_RES_ID_ATTRIBUTE, R.drawable.sym_keyboard_notification_icon);
                    	result.DefaultDictionaryLanguage = attrs.getAttributeValue(null, XML_DICTIONARY_NAME_ATTRIBUTE);
                    }
                }
                else if (event == XmlPullParser.END_TAG && inMetaData) {
                	Log.d("AnySoftKeyboard", "Finished parsing "+XML_META_DATA_TAG);
                	break;
                }
            }
        } catch (Exception e) {
            Log.e("AnySoftKeyboard", "Parse error:" + e);
            e.printStackTrace();
        }
        
        return result;
	}

	protected AnyKeyboardContextProvider getKeyboardContext()
    {
    	return mKeyboardContext;
    }
    
    public String getDefaultDictionaryLanguage()
    {
    	return mKeyboardMetaData.DefaultDictionaryLanguage;
    }
    
    //this function is called from within the super constructor.
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, 
            XmlResourceParser parser) {
    	if (mSpecialShiftKeys == null) mSpecialShiftKeys = new HashMap<Character, ShiftedKeyData>();
    	
    	AnyKey key = new AnyKey(res, parent, x, y, parser);
        
        if ((key.codes != null) && (key.codes.length > 0))
        {
        	final int primaryCode = key.codes[0];
        	//detecting LTR languages
        	if (Workarounds.isRightToLeftCharacter((char)primaryCode))
    			mRightToLeftLayout = true;//one is enough
    		
        	//creating less sensitive keys if required
        	switch(primaryCode)
        	{
        	case 10://enter
        	case KEYCODE_DELETE://delete
        	case KEYCODE_SHIFT://shift
        		key = new LessSensitiveAnyKey(res, parent, x, y, parser);
        	}
        	
	        if (primaryCode == 10) 
	        {
	            mEnterKey = key;	            
	        }
	        else if (primaryCode == KEYCODE_SHIFT) 
	        {
	            mShiftKey = key;
	            //saving the drawable for reseting purposes.
	            mOldShiftIcon = mShiftKey.icon;
	        }
	        else if ((primaryCode == AnyKeyboard.KEYCODE_SMILEY) && (parent.rowEdgeFlags == Keyboard.EDGE_BOTTOM)) 
	        {
	            mSmileyKey = key;
	        }
	        else if ((primaryCode == 63)  && (parent.rowEdgeFlags == Keyboard.EDGE_BOTTOM)) 
	        {
	            mQuestionMarkKey = key;
	        }
	        else if ((primaryCode == Keyboard.KEYCODE_MODE_CHANGE) ||
	        		 (primaryCode == AnyKeyboard.KEYCODE_LANG_CHANGE))
	        {
	        	final String keysMode = AnySoftKeyboardConfigurationImpl.getInstance().getChangeLayoutKeysSize();
	        	if (keysMode.equals("None"))
	        	{
	        		key.label = null;
	        		key.height = 0;
	        		key.width = 0;
	        	}
	        	else if (keysMode.equals("Big"))
	        	{
	        		String keyText = (primaryCode == Keyboard.KEYCODE_MODE_CHANGE)?
	        				res.getString(R.string.change_symbols_regular) :
	        					res.getString(R.string.change_lang_regular);
	        		key.label = keyText;
	        		//key.height *= 1.5;
	        	}
	        	else
	        	{
	        		String keyText = (primaryCode == Keyboard.KEYCODE_MODE_CHANGE)?
	        				res.getString(R.string.change_symbols_wide) :
	        					res.getString(R.string.change_lang_wide);
	        		key.label = keyText;
	        	}
	        }
	        else
	        {
	        	//setting the character label
	        	if (isAlphabetKey(key))
	        	{
	        		key.label = ""+((char)primaryCode); 
	        	}
	        }
        }
        
        if (mDebug)
        {
        	final int primaryKey = ((key.codes != null) && key.codes.length > 0)?
        			key.codes[0] : -1;
        	Log.v("AnySoftKeyboard", "Key '"+primaryKey+"' will have - width: "+key.width+", height:"+key.height+", text: '"+key.label+"'.");
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
	            	Log.v("AnySoftKeyboard", "Adding mapping ("+primary+"->"+keyData.ShiftCharacter+") to mSpecialShiftKeys.");
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
    		String layoutChangeType = AnySoftKeyboardConfigurationImpl.getInstance().getChangeLayoutKeysSize();
    		//top row
    		if (layoutChangeType.equals("None"))
    			aRow.defaultHeight = 0;
    		else if (layoutChangeType.equals("Big"))
    			aRow.defaultHeight *= 1.5;
    	}
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
    public void setImeOptions(Resources res, int options) {
    	if (mDebug)
    		Log.d("AnySoftKeyboard", "AnyKeyboard.setImeOptions");
        if (mEnterKey == null) {
            return;
        }
        
        switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
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
            default:
                mEnterKey.icon = res.getDrawable(R.drawable.sym_keyboard_return);
                mEnterKey.label = null;
                break;
        }
    }
    
    public String getKeyboardName()
    {
    	//TODO: this should be taken from the strings.xml, right?
    	return mKeyboardMetaData.KeyboardName;
    }
    
    public boolean isLeftToRightLanguage()
    {
    	return !mRightToLeftLayout;
    }
    
    /*
     * This function is overridden by other alphabet keyboards, for nifty icons
     */
    public int getKeyboardIcon()
    {
    	return mKeyboardMetaData.IconResId;
    }
    
//    public String getKeyboardKey()
//    {
//    	return mKeyboardPrefId;
//    }
    
//    public boolean isLetter(char letterCode)
//    {
//    	if (Character.isLetter(letterCode)) 
//    		return true;
//        else
//        	return false;
//    }
    
//	public void addSuggestions(String currentWord, ArrayList<String> list) 
//	{
//	}
	
	public void setShiftLocked(boolean shiftLocked) {
        if (mShiftKey != null) {
            if (shiftLocked) {
            	Log.d("AnySoftKeyboard", "Switching to LOCKED shift icon - capslock");
                mShiftKey.on = true;
                mShiftKey.icon = mShiftLockIcon;
                mShiftState = SHIFT_LOCKED;
            } else {
            	Log.d("AnySoftKeyboard", "Switching to regular shift icon - un-shifted");
                mShiftKey.on = false;
                mShiftKey.icon = mOldShiftIcon;
                mShiftState = SHIFT_OFF;
            }
        }
    }
    
    @Override
    public boolean isShifted() {
//        if (mShiftKey != null) {
//            return mShiftState != SHIFT_OFF;
//        } else {
            return super.isShifted();
//        }
    }
    
//    @Override
//    public boolean setShifted(boolean shiftState) {
//        boolean shiftChanged = false;
//        if (mShiftKey != null) {
//            if (shiftState == false) {
//                shiftChanged = mShiftState != SHIFT_OFF;
//                mShiftState = SHIFT_OFF;
//                mShiftKey.on = false;
//                mShiftKey.icon = mOldShiftIcon;
//            } else {
//                if (mShiftState == SHIFT_OFF) {
//                    shiftChanged = mShiftState == SHIFT_OFF;
//                    mShiftState = SHIFT_ON;
//                    mShiftKey.icon = mShiftLockIcon;
//                }
//            }
//        } else {
//            return super.setShifted(shiftState);
//        }
//        return shiftChanged;
//    }
    
	@Override
	public boolean setShifted(boolean shiftState) 
	{
		boolean result = super.setShifted(shiftState);
		if (mDebug)
    		Log.d("AnySoftKeyboard", "setShifted: shiftState:"+shiftState+". result:"+result);
		mShiftState = shiftState? SHIFT_ON : SHIFT_OFF;
		if (result)
		{//layout changed. Need to change labels.
			//going over the special keys only.
			for(ShiftedKeyData data : mSpecialShiftKeys.values())
			{
				onKeyShifted(data, shiftState);
			}
			
			if (mShiftKey != null) {
	            if (shiftState == false) {
	                mShiftState = SHIFT_OFF;
	                mShiftKey.on = false;
	                mShiftKey.icon = mOldShiftIcon;
	            } else {
	                if (mShiftState == SHIFT_OFF) {
	                	mShiftKey.on = true;
	                    mShiftState = SHIFT_ON;
	                    mShiftKey.icon = mOldShiftIcon;
	                }
	            }
	        }
		}
		return result;
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
		if ((aKey.codes != null) && (aKey.codes.length > 0))
        {
			switch(((char)aKey.codes[0]))
			{
			case '\''://in the generic bottom row
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = "\"";
				break;
//			case '-':
//				aKey.popupResId = R.xml.popup;
//				aKey.popupCharacters = "\'\"";
//				break;
			case '.'://in the generic bottom row
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = ";:-_\u00b7";
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
				aKey.popupCharacters = "!/@\u00bf\u00a1";
				break;
			case '@'://in the generic Internet mode
				aKey.popupResId = R.xml.popup;
				aKey.popupCharacters = "!/?\u00bf\u00a1";
				break;
			}
        }
	}

	public void setTextVariation(Resources res, int inputType) 
	{
		if (mDebug)
    		Log.d("AnySoftKeyboard", "setTextVariation");
		int variation = inputType &  EditorInfo.TYPE_MASK_VARIATION;
		
		switch (variation) {
	        case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
	        case EditorInfo.TYPE_TEXT_VARIATION_URI:
	        	if (mSmileyKey != null)
	        	{
	        		Log.d("AnySoftKeyboard", "Changing smiley key to domains.");
	        		mSmileyKey.iconPreview = null;// res.getDrawable(sym_keyboard_key_domain_preview);
	        		mSmileyKey.icon = res.getDrawable(R.drawable.sym_keyboard_key_domain);
		        	mSmileyKey.label = null;
		        	mSmileyKey.text = AnySoftKeyboardConfigurationImpl.getInstance().getDomainText();
		        	mSmileyKey.popupResId = R.xml.popup_domains;
	        	}
	        	if (mQuestionMarkKey != null)
	        	{
	        		Log.d("AnySoftKeyboard", "Changing question mark key to AT.");
		        	mQuestionMarkKey.codes[0] = (int)'@';
		        	mQuestionMarkKey.label = "@";
		        	mQuestionMarkKey.popupCharacters = "!/?\u00bf\u00a1";
	        	}
	        	break;
	        default:
	        	if (mSmileyKey != null)
	        	{
	        		Log.d("AnySoftKeyboard", "Changing smiley key to smiley.");
	        		mSmileyKey.icon = res.getDrawable(R.drawable.sym_keyboard_smiley);
		        	mSmileyKey.label = null;
		        	mSmileyKey.text = null;// ":-) ";
		        	mSmileyKey.popupResId = R.xml.popup_smileys;
	        	}
	        	if (mQuestionMarkKey != null)
	        	{
	        		Log.d("AnySoftKeyboard", "Changing question mark key to question.");
		        	mQuestionMarkKey.codes[0] = (int)'?';
		        	mQuestionMarkKey.label = "?";
		        	mQuestionMarkKey.popupCharacters = "!/@\u00bf\u00a1";
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
		        	Log.v("AnySoftKeyboard", "Returned the shifted mapping ("+c+"->"+shifted+") from mSpecialShiftKeys.");
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
	
	class LessSensitiveAnyKey extends AnyKey {
        
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
        	
        	switch(codes[0])
        	{
        	case 10://the enter key!
        		//we want to "click" it only if it in the lower
        		mStartY += (this.height * 0.15);
        		break;
        	case KEYCODE_DELETE:
        		//we want to "click" it only if it in the middle
        		mStartY += (this.height * 0.05);
        		mEndY -= (this.height * 0.05);
        		mStartX += (this.width * 0.15);
        		break;
        	case KEYCODE_SHIFT:
        		//we want to "click" it only if it in the left
        		mEndX -= (this.width * 0.1);
        		break;
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

	public String getKeyboardPrefId() {
		return mKeyboardMetaData.PrefString;
	}
}
