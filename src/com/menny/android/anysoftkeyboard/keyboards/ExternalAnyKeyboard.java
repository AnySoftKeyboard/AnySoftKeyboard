package com.menny.android.anysoftkeyboard.keyboards;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;
import com.menny.android.anysoftkeyboard.AnySoftKeyboardConfiguration;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;


public class ExternalAnyKeyboard extends AnyKeyboard implements HardKeyboardTranslator {

	private final static String TAG = "ASK - EAK";
	
	private static final String XML_TRANSLATION_TAG = "PhysicalTranslation";
	private static final String XML_QWERTY_ATTRIBUTE = "QwertyTranslation";
	private static final String XML_SEQUENCE_TAG = "SequenceMapping";
	private static final String XML_KEYS_ATTRIBUTE = "keySequence";
	private static final String XML_ALT_ATTRIBUTE = "altModifier";
	private static final String XML_SHIFT_ATTRIBUTE = "shiftModifier";
	private static final String XML_TARGET_ATTRIBUTE = "targetChar";
	private static final String XML_TARGET_CHAR_CODE_ATTRIBUTE = "targetCharCode";
	private final String mPrefId;
	private final int mNameResId;
	private final int mIconId;
	private final String mDefaultDictionary;
	private final HardKeyboardSequenceHandler mHardKeyboardTranslator;
	private final String mAdditionalIsLetterExceptions;

	public ExternalAnyKeyboard(AnyKeyboardContextProvider askContext, Context context,
			int xmlLayoutResId,
			int xmlLandscapeResId,
			String prefId,
			int nameResId,
			int iconResId,
			int qwertyTranslationId,
			String defaultDictionary,
			String additionalIsLetterExceptions,
			int mode) 
	{
		super(askContext, context, getKeyboardId(askContext.getApplicationContext(), xmlLayoutResId, xmlLandscapeResId), mode);
		mPrefId = prefId;
		mNameResId = nameResId;
		mIconId = iconResId;
		mDefaultDictionary = defaultDictionary;
		if (qwertyTranslationId != -1)
		{
			Log.d(TAG, "Creating qwerty mapping:"+qwertyTranslationId);
			mHardKeyboardTranslator = createPhysicalTranslatorFromResourceId(context, qwertyTranslationId);
		}
		else
		{
			mHardKeyboardTranslator = null;
		}

		mAdditionalIsLetterExceptions = additionalIsLetterExceptions;
	}
	
	

	private HardKeyboardSequenceHandler createPhysicalTranslatorFromResourceId(Context context, int qwertyTranslationId) {
		HardKeyboardSequenceHandler translator = new HardKeyboardSequenceHandler();
		XmlPullParser parser = context.getResources().getXml(qwertyTranslationId);
		final String TAG = "ASK Hard Translation Parser";
		try {
            int event;
            boolean inTranslations = false;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT)
            {
            	String tag = parser.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (XML_TRANSLATION_TAG.equals(tag)) {
                    	inTranslations = true;
                    	AttributeSet attrs = Xml.asAttributeSet(parser);
                    	final String qwerty = attrs.getAttributeValue(null, XML_QWERTY_ATTRIBUTE);
                    	translator.addQwertyTranslation(qwerty);
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Starting parsing "+XML_TRANSLATION_TAG+". Qwerty:"+qwerty);
                    }
                    else if (inTranslations && XML_SEQUENCE_TAG.equals(tag))
                    {
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Starting parsing "+XML_SEQUENCE_TAG);
                    	AttributeSet attrs = Xml.asAttributeSet(parser);

                    	final int[] keyCodes = getKeyCodesFromPhysicalSequence(attrs.getAttributeValue(null, XML_KEYS_ATTRIBUTE));
                    	final boolean isAlt = attrs.getAttributeBooleanValue(null, XML_ALT_ATTRIBUTE, false);
                    	final boolean isShift = attrs.getAttributeBooleanValue(null, XML_SHIFT_ATTRIBUTE, false);
                    	final String targetChar = attrs.getAttributeValue(null, XML_TARGET_ATTRIBUTE);
                    	final String targetCharCode = attrs.getAttributeValue(null, XML_TARGET_CHAR_CODE_ATTRIBUTE);
                        final String target;
                        if (targetChar == null)
                        	target = Character.toString((char)Integer.parseInt(targetCharCode));
                        else
                        	target = targetChar;
                    	//asserting
                        if ((keyCodes == null) || (keyCodes.length == 0) || (target == null))
                        {
                            Log.e(TAG, "Physical translator sequence does not include mandatory fields "+XML_KEYS_ATTRIBUTE+" or "+XML_TARGET_ATTRIBUTE);
                        }
                        else
                        {
                        	if (!isAlt && !isShift)
                        	{
	                        	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Physical translation details: keys:"+printInts(keyCodes)+" target:"+target);
	                        	translator.addSequence(keyCodes, target.charAt(0));
                        	}
                        	else if (isAlt)
                        	{
                        		final int keyCode = keyCodes[0];
                        		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Physical translation details: ALT+key:"+keyCode+" target:"+target);
	                        	translator.addAltMapping(keyCode, target.charAt(0));
                        	}
                        	else if (isShift)
                        	{
                        		final int keyCode = keyCodes[0];
                        		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Physical translation details: ALT+key:"+keyCode+" target:"+target);
	                        	translator.addShiftMapping(keyCode, target.charAt(0));
                        	}
                        }
                    }
                }
                else if (event == XmlPullParser.END_TAG) {
                	if (XML_TRANSLATION_TAG.equals(tag)) {
                    	inTranslations = false;
                    	if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Finished parsing "+XML_TRANSLATION_TAG);
                    	break;
                    }
                	else if (inTranslations && XML_SEQUENCE_TAG.equals(tag))
                    {
                		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG()) Log.d(TAG, "Finished parsing "+XML_SEQUENCE_TAG);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }
		return translator;
	}

	private String printInts(int[] keyCodes) {
		String r = "";
		for(int code : keyCodes)
			r += (Integer.toString(code)+",");

		return r;
	}

	private int[] getKeyCodesFromPhysicalSequence(String keyCodesArray) {
		String[] splitted = keyCodesArray.split(",");
		int[] keyCodes = new int[splitted.length];
		for (int i = 0; i < keyCodes.length; i++) {
			try {
				keyCodes[i] = Integer.parseInt(splitted[i]);//try parsing as an integer
			} catch (final NumberFormatException nfe) {//no an integer
				final String v = splitted[i];
				try {
					keyCodes[i] = android.view.KeyEvent.class.getField(v)
							.getInt(null);//here comes the reflection. No bother of performance.
					//First hit takes just 20 milliseconds, the next hits <2 Milliseconds.
				} catch (final Exception ex) {//crap :(
					throw new RuntimeException(ex);//bum
				}
			}
		}

		return keyCodes;
	}

	@Override
	public String getDefaultDictionaryLocale() {
		return mDefaultDictionary;
	}

	@Override
	public String getKeyboardPrefId() {
		return mPrefId;
	}

	@Override
	public int getKeyboardIconResId() {
		return mIconId;
	}

	@Override
	protected int getKeyboardNameResId() {
		return mNameResId;
	}

	private static int getKeyboardId(Context context, int portraitId, int landscapeId)
	{
		final boolean inPortraitMode =
			(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
		{
			Log.d(TAG, "inPortraitMode:"+inPortraitMode+" portrait ID:"+portraitId+" landscape ID:"+landscapeId);
		}
		
		if (inPortraitMode)
			return portraitId;
		else
			return landscapeId;
	}

	//this class implements the HardKeyboardTranslator interface in an empty way, the physical keyboard is Latin...
	public void translatePhysicalCharacter(HardKeyboardAction action)
	{
		if (mHardKeyboardTranslator != null)
		{
			final char translated;
			if (action.isAltActive())
				translated = mHardKeyboardTranslator.getAltCharacter(action.getKeyCode());
			else if (action.isShiftActive())
			{
				//shift is a special case, we might have a special shift mapping, but
				//we may not... in the latter case, we'll use the regualr translation
				//but upper-case the output
				char shiftTranslation = mHardKeyboardTranslator.getShiftCharacter(action.getKeyCode());
				if (shiftTranslation <= 0)
				{
					shiftTranslation = mHardKeyboardTranslator.getSequenceCharacter(action.getKeyCode(), getASKContext());
					//uppercasing
					shiftTranslation = Character.toUpperCase(shiftTranslation);
				}
				
				translated = shiftTranslation;
			}
			else
				translated = mHardKeyboardTranslator.getSequenceCharacter(action.getKeyCode(), getASKContext());

			if (translated != 0)
				action.setNewKeyCode(translated);
		}
	}

	@Override
	public boolean isLetter(char keyValue) {
		if (mAdditionalIsLetterExceptions == null)
			return super.isLetter(keyValue);
		else
			return super.isLetter(keyValue) ||
				(mAdditionalIsLetterExceptions.indexOf(keyValue) >= 0);
	}

	protected void setPopupKeyChars(Key aKey)
	{
		if (aKey.popupResId > 0)
			return;//if the keyboard XML already specified the popup, then no need to override

		//filling popup res for external keyboards
		//if ((aKey.popupCharacters != null) && (aKey.popupCharacters.length() > 0)){
		if (aKey.popupCharacters != null){
		    if(aKey.popupCharacters.length() > 0){
			aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
		    }
			return;
		}

		if ((aKey.codes != null) && (aKey.codes.length > 0))
        {
			switch((char)aKey.codes[0])
			{
				case 'a':
					aKey.popupCharacters = "àáâãäåæą";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'c':
					aKey.popupCharacters = "çćĉč";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'd':
					aKey.popupCharacters = "đ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'e':
					aKey.popupCharacters = "èéêëę€";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'g':
					aKey.popupCharacters = "ĝ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'h':
					aKey.popupCharacters = "ĥ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'i':
					aKey.popupCharacters = "ìíîïł";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'j':
					aKey.popupCharacters = "ĵ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'l':
					aKey.popupCharacters = "ł";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'o':
					aKey.popupCharacters = "òóôõöøőœ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 's':
					aKey.popupCharacters = "§ßśŝš";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'u':
					aKey.popupCharacters = "ùúûüŭű";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'n':
					aKey.popupCharacters = "ñ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'y':
					aKey.popupCharacters = "ýÿ";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				case 'z':
					aKey.popupCharacters = "żžź";
					aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
					break;
				default:
					super.setPopupKeyChars(aKey);
			}
        }
	}
}
