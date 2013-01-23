package com.anysoftkeyboard.keyboards;

import java.util.HashSet;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.menny.android.anysoftkeyboard.AnyApplication;

public class ExternalAnyKeyboard extends AnyKeyboard implements
		HardKeyboardTranslator {

	public static final int KEYCODE_EXTENSION_KEYBOARD = -210;

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
	private final String mName;
	private final int mIconId;
	private final String mDefaultDictionary;
	private final HardKeyboardSequenceHandler mHardKeyboardTranslator;
	private final HashSet<Character> mAdditionalIsLetterExceptions;
	private final HashSet<Character> mSentenceSeparators;

	private KeyboardExtension mExtensionLayout = null;

	public ExternalAnyKeyboard(Context askContext,
			Context context, int xmlLayoutResId, int xmlLandscapeResId,
			String prefId, String name, int iconResId,
			int qwertyTranslationId, String defaultDictionary,
			String additionalIsLetterExceptions, String sentenceSeparators,
			int mode) {
		super(askContext, context, getKeyboardId(
				askContext, xmlLayoutResId,
				xmlLandscapeResId), mode);
		mPrefId = prefId;
		mName = name;
		mIconId = iconResId;
		mDefaultDictionary = defaultDictionary;
		if (qwertyTranslationId != AddOn.INVALID_RES_ID) {
			if (AnyApplication.DEBUG)
				Log.d(TAG, "Creating qwerty mapping:" + qwertyTranslationId);
			mHardKeyboardTranslator = createPhysicalTranslatorFromResourceId(
					context, qwertyTranslationId);
		} else {
			mHardKeyboardTranslator = null;
		}

		mAdditionalIsLetterExceptions = new HashSet<Character>(
				additionalIsLetterExceptions != null ? additionalIsLetterExceptions
						.length() : 0);
		if (additionalIsLetterExceptions != null) {
			for (int i = 0; i < additionalIsLetterExceptions.length(); i++)
				mAdditionalIsLetterExceptions.add(additionalIsLetterExceptions
						.charAt(i));
		}
		mSentenceSeparators = new HashSet<Character>(
				sentenceSeparators != null ? sentenceSeparators.length() : 0);
		if (sentenceSeparators != null) {
			for (int i = 0; i < sentenceSeparators.length(); i++)
				mSentenceSeparators.add(sentenceSeparators.charAt(i));
		}

		setExtensionLayout(KeyboardExtensionFactory
				.getCurrentKeyboardExtension(
						askContext.getApplicationContext(),
						KeyboardExtension.TYPE_EXTENSION));
	}

	protected void setExtensionLayout(KeyboardExtension extKbd) {
		mExtensionLayout = extKbd;
	}

	public KeyboardExtension getExtensionLayout() {
		return mExtensionLayout;
	}

	private HardKeyboardSequenceHandler createPhysicalTranslatorFromResourceId(
			Context context, int qwertyTranslationId) {
		HardKeyboardSequenceHandler translator = new HardKeyboardSequenceHandler();
		XmlPullParser parser = context.getResources().getXml(
				qwertyTranslationId);
		final String TAG = "ASK Hard Translation Parser";
		try {
			int event;
			boolean inTranslations = false;
			while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
				String tag = parser.getName();
				if (event == XmlPullParser.START_TAG) {
					if (XML_TRANSLATION_TAG.equals(tag)) {
						inTranslations = true;
						AttributeSet attrs = Xml.asAttributeSet(parser);
						final String qwerty = attrs.getAttributeValue(null,
								XML_QWERTY_ATTRIBUTE);
						if (qwerty != null)
							translator.addQwertyTranslation(qwerty);
					} else if (inTranslations && XML_SEQUENCE_TAG.equals(tag)) {
						// if
						// (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
						// Log.d(TAG, "Starting parsing "+XML_SEQUENCE_TAG);
						AttributeSet attrs = Xml.asAttributeSet(parser);

						final int[] keyCodes = getKeyCodesFromPhysicalSequence(attrs
								.getAttributeValue(null, XML_KEYS_ATTRIBUTE));
						final boolean isAlt = attrs.getAttributeBooleanValue(
								null, XML_ALT_ATTRIBUTE, false);
						final boolean isShift = attrs.getAttributeBooleanValue(
								null, XML_SHIFT_ATTRIBUTE, false);
						final String targetChar = attrs.getAttributeValue(null,
								XML_TARGET_ATTRIBUTE);
						final String targetCharCode = attrs.getAttributeValue(
								null, XML_TARGET_CHAR_CODE_ATTRIBUTE);
						final Integer target;
						if (targetCharCode == null)
							target = Integer
									.valueOf((int) targetChar.charAt(0));
						else
							target = Integer.valueOf(targetCharCode);

						// asserting
						if ((keyCodes == null) || (keyCodes.length == 0)
								|| (target == null)) {
							Log.e(TAG,
									"Physical translator sequence does not include mandatory fields "
											+ XML_KEYS_ATTRIBUTE + " or "
											+ XML_TARGET_ATTRIBUTE);
						} else {
							if (!isAlt && !isShift) {
								// if
								// (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
								// Log.d(TAG,
								// "Physical translation details: keys:"+printInts(keyCodes)+" target:"+target);
								translator.addSequence(keyCodes,
										target.intValue());
								// http://code.google.com/p/softkeyboard/issues/detail?id=734
								translator.addShiftSequence(keyCodes, Character
										.toUpperCase(target.intValue()));
							} else if (isAlt) {
								// if
								// (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
								// Log.d(TAG,
								// "Physical translation details: ALT+key:"+keyCode+" target:"+target);
								translator.addAltSequence(keyCodes,
										target.intValue());
							} else if (isShift) {
								// if
								// (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
								// Log.d(TAG,
								// "Physical translation details: ALT+key:"+keyCode+" target:"+target);
								translator.addShiftSequence(keyCodes,
										target.intValue());
							}
						}
					}
				} else if (event == XmlPullParser.END_TAG) {
					if (XML_TRANSLATION_TAG.equals(tag)) {
						inTranslations = false;
						// if
						// (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
						// Log.d(TAG, "Finished parsing "+XML_TRANSLATION_TAG);
						break;
					} else if (inTranslations && XML_SEQUENCE_TAG.equals(tag)) {
						// if
						// (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
						// Log.d(TAG, "Finished parsing "+XML_SEQUENCE_TAG);
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Parse error:" + e);
			e.printStackTrace();
		}
		return translator;
	}

	private int[] getKeyCodesFromPhysicalSequence(String keyCodesArray) {
		String[] splitted = keyCodesArray.split(",");
		int[] keyCodes = new int[splitted.length];
		for (int i = 0; i < keyCodes.length; i++) {
			try {
				keyCodes[i] = Integer.parseInt(splitted[i]);// try parsing as an
															// integer
			} catch (final NumberFormatException nfe) {// no an integer
				final String v = splitted[i];
				try {
					keyCodes[i] = android.view.KeyEvent.class.getField(v)
							.getInt(null);// here comes the reflection. No
											// bother of performance.
					// First hit takes just 20 milliseconds, the next hits <2
					// Milliseconds.
				} catch (final Exception ex) {// crap :(
					throw new RuntimeException(ex);// bum
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
	public String getKeyboardName() {
		return mName;
	}

	private static int getKeyboardId(Context context, int portraitId,
			int landscapeId) {
		final boolean inPortraitMode = (context.getResources()
				.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);

		if (inPortraitMode)
			return portraitId;
		else
			return landscapeId;
	}

	// this class implements the HardKeyboardTranslator interface in an empty
	// way, the physical keyboard is Latin...
	public void translatePhysicalCharacter(HardKeyboardAction action,
			AnySoftKeyboard ime) {
		if (mHardKeyboardTranslator != null) {
			final int translated;
			if (action.isAltActive())
				if (!mHardKeyboardTranslator.addSpecialKey(KeyCodes.ALT))
					return;
			if (action.isShiftActive())
				if (!mHardKeyboardTranslator.addSpecialKey(KeyCodes.SHIFT))
					return;

			translated = mHardKeyboardTranslator.getCurrentCharacter(
					action.getKeyCode(), ime);

			if (translated != 0)
				action.setNewKeyCode(translated);
		}
	}

	@Override
	public boolean isInnerWordLetter(char keyValue) {
		return super.isInnerWordLetter(keyValue)
				|| mAdditionalIsLetterExceptions.contains(keyValue);
	}

	@Override
	public HashSet<Character> getSentenceSeparators() {
		return mSentenceSeparators;
	}

	protected void setPopupKeyChars(Key aKey) {
		if (aKey.popupResId > 0)
			return;// if the keyboard XML already specified the popup, then no
					// need to override

		// filling popup res for external keyboards
		// if ((aKey.popupCharacters != null) && (aKey.popupCharacters.length()
		// > 0)){
		if (aKey.popupCharacters != null) {
			if (aKey.popupCharacters.length() > 0) {
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
			}
			return;
		}

		if ((aKey.codes != null) && (aKey.codes.length > 0)) {
			switch ((char) aKey.codes[0]) {
			case 'a':
				aKey.popupCharacters = "\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u00e6\u0105";// "àáâãäåæą";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'c':
				aKey.popupCharacters = "\u00e7\u0107\u0109\u010d";// "çćĉč";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'd':
				aKey.popupCharacters = "\u0111"; // "đ";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'e':
				aKey.popupCharacters = "\u00e8\u00e9\u00ea\u00eb\u0119\u20ac\u0113";// "èéêëę€";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'g':
				aKey.popupCharacters = "\u011d";// "ĝ";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'h':
				aKey.popupCharacters = "\u0125";// "ĥ";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'i':
				aKey.popupCharacters = "\u00ec\u00ed\u00ee\u00ef\u0142\u012B";// "ìíîïł";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'j':
				aKey.popupCharacters = "\u0135";// "ĵ";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'l':
				aKey.popupCharacters = "\u0142";// "ł";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'o':
				aKey.popupCharacters = "\u00f2\u00f3\u00f4\u00f5\u00f6\u00f8\u0151\u0153\u014D";// "òóôõöøőœ";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 's':
				aKey.popupCharacters = "\u00a7\u00df\u015b\u015d\u0161";// "§ßśŝš";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'u':
				aKey.popupCharacters = "\u00f9\u00fa\u00fb\u00fc\u016d\u0171\u016B";// "ùúûüŭű";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'n':
				aKey.popupCharacters = "\u00f1";// "ñ";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'y':
				aKey.popupCharacters = "\u00fd\u00ff";// "ýÿ";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			case 'z':
				aKey.popupCharacters = "\u017c\u017e\u017a";// "żžź";
				aKey.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup;
				break;
			default:
				super.setPopupKeyChars(aKey);
			}
		}
	}
}
