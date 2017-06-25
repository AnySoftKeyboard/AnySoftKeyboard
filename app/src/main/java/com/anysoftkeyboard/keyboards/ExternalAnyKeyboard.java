/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.anysoftkeyboard.utils.LocaleTools;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;

import org.xmlpull.v1.XmlPullParser;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;

public class ExternalAnyKeyboard extends AnyKeyboard implements HardKeyboardTranslator {

    private static final String TAG = "ASK - EAK";

    private static final String XML_TRANSLATION_TAG = "PhysicalTranslation";
    private static final String XML_QWERTY_ATTRIBUTE = "QwertyTranslation";
    private static final String XML_SEQUENCE_TAG = "SequenceMapping";
    private static final String XML_KEYS_ATTRIBUTE = "keySequence";
    private static final String XML_TARGET_ATTRIBUTE = "targetChar";
    private static final String XML_TARGET_CHAR_CODE_ATTRIBUTE = "targetCharCode";
    private static final String XML_MULTITAP_TAG = "MultiTap";
    private static final String XML_MULTITAP_KEY_ATTRIBUTE = "key";
    private static final String XML_MULTITAP_CHARACTERS_ATTRIBUTE = "characters";
    private static final String XML_ALT_ATTRIBUTE = "altModifier";
    private static final String XML_SHIFT_ATTRIBUTE = "shiftModifier";
    private final CharSequence mName;
    private final int mIconId;
    private final String mDefaultDictionary;
    private final Locale mLocale;
    private final HardKeyboardSequenceHandler mHardKeyboardTranslator;
    private final HashSet<Character> mAdditionalIsLetterExceptions;
    private final char[] mSentenceSeparators;

    private KeyboardExtension mExtensionLayout = null;

    public ExternalAnyKeyboard(@NonNull AddOn keyboardAddOn, @NonNull Context askContext,
                               @NonNull Context context, int xmlLayoutResId, int xmlLandscapeResId,
                               CharSequence name, int iconResId,
                               int qwertyTranslationId, String defaultDictionary,
                               String additionalIsLetterExceptions, String sentenceSeparators,
                               @KeyboardRowModeId int mode) {
        super(keyboardAddOn, askContext, context, getKeyboardId(
                askContext, xmlLayoutResId,
                xmlLandscapeResId), mode);
        mName = name;
        mIconId = iconResId;
        mDefaultDictionary = defaultDictionary;
        mLocale = LocaleTools.getLocaleForLocaleString(mDefaultDictionary);

        if (qwertyTranslationId != AddOn.INVALID_RES_ID) {
            Logger.d(TAG, "Creating qwerty mapping: %d", qwertyTranslationId);
            mHardKeyboardTranslator = createPhysicalTranslatorFromResourceId(context, qwertyTranslationId);
        } else {
            mHardKeyboardTranslator = null;
        }

        mAdditionalIsLetterExceptions = new HashSet<>(
                additionalIsLetterExceptions != null ? additionalIsLetterExceptions.length() : 0);
        if (additionalIsLetterExceptions != null) {
            for (int i = 0; i < additionalIsLetterExceptions.length(); i++)
                mAdditionalIsLetterExceptions.add(additionalIsLetterExceptions.charAt(i));
        }

        if (sentenceSeparators != null) {
            mSentenceSeparators = sentenceSeparators.toCharArray();
        } else {
            mSentenceSeparators = new char[0];
        }

        setExtensionLayout(AnyApplication.getKeyboardExtensionFactory(askContext).getEnabledAddOn());
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
                            target = (int) targetChar.charAt(0);
                        else
                            target = Integer.valueOf(targetCharCode);

                        // asserting
                        if ((keyCodes == null) || (keyCodes.length == 0) || (target == null)) {
                            Logger.e(TAG,
                                    "Physical translator sequence does not include mandatory fields "
                                            + XML_KEYS_ATTRIBUTE + " or "
                                            + XML_TARGET_ATTRIBUTE);
                        } else {
                            if (!isAlt && !isShift) {
                                translator.addSequence(keyCodes, target);
                                // http://code.google.com/p/softkeyboard/issues/detail?id=734
                                translator.addShiftSequence(keyCodes, Character.toUpperCase(target.intValue()));
                            } else if (isAlt) {
                                translator.addAltSequence(keyCodes, target);
                            } else {
                                translator.addShiftSequence(keyCodes, target);
                            }
                        }
                    } else if (inTranslations && XML_MULTITAP_TAG.equals(tag)) {
                        AttributeSet attrs = Xml.asAttributeSet(parser);

                        final int[] keyCodes = getKeyCodesFromPhysicalSequence(attrs.getAttributeValue(null, XML_MULTITAP_KEY_ATTRIBUTE));
                        if (keyCodes.length != 1)
                            throw new ParseException("attribute " + XML_MULTITAP_KEY_ATTRIBUTE + " should contain exactly one key-code when used in " + XML_MULTITAP_TAG + " tag!", parser.getLineNumber());

                        final boolean isAlt = attrs.getAttributeBooleanValue(
                                null, XML_ALT_ATTRIBUTE, false);
                        final boolean isShift = attrs.getAttributeBooleanValue(
                                null, XML_SHIFT_ATTRIBUTE, false);
                        final String targetCharacters = attrs.getAttributeValue(null,
                                XML_MULTITAP_CHARACTERS_ATTRIBUTE);
                        if (TextUtils.isEmpty(targetCharacters) || targetCharacters.length() < 2)
                            throw new ParseException("attribute " + XML_MULTITAP_CHARACTERS_ATTRIBUTE + " should contain more than one character when used in " + XML_MULTITAP_TAG + " tag!", parser.getLineNumber());

                        for (int characterIndex = 0; characterIndex <= targetCharacters.length(); characterIndex++) {
                            int[] multiTapCodes = new int[characterIndex + 1];
                            for (int tapIndex = 0; tapIndex < multiTapCodes.length; tapIndex++) {
                                multiTapCodes[tapIndex] = keyCodes[0];
                            }
                            if (characterIndex < targetCharacters.length()) {
                                final int target = targetCharacters.charAt(characterIndex);

                                if (!isAlt && !isShift) {
                                    translator.addSequence(multiTapCodes, target);
                                    translator.addShiftSequence(multiTapCodes, Character.toUpperCase(target));
                                } else if (isAlt) {
                                    translator.addAltSequence(keyCodes, target);
                                } else {
                                    translator.addShiftSequence(keyCodes, target);
                                }
                            } else {
                                //and adding the rewind character
                                if (!isAlt && !isShift) {
                                    translator.addSequence(multiTapCodes, KeyEventStateMachine.KEYCODE_FIRST_CHAR);
                                    translator.addShiftSequence(multiTapCodes, KeyEventStateMachine.KEYCODE_FIRST_CHAR);
                                } else if (isAlt) {
                                    translator.addAltSequence(keyCodes, KeyEventStateMachine.KEYCODE_FIRST_CHAR);
                                } else {
                                    translator.addShiftSequence(keyCodes, KeyEventStateMachine.KEYCODE_FIRST_CHAR);
                                }
                            }
                        }
                    }
                } else if (event == XmlPullParser.END_TAG) {
                    if (XML_TRANSLATION_TAG.equals(tag)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format(Locale.US, "Failed to parse keyboard layout. Keyboard '%s' (id %s, package %s), translatorResourceId %d", getKeyboardName(), getKeyboardId(), getKeyboardAddOn().getPackageName(), qwertyTranslationId);
            Logger.e(TAG, errorMessage, e);
            e.printStackTrace();
            if (BuildConfig.DEBUG) throw new RuntimeException(errorMessage, e);
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

    public Locale getLocale() {
        return mLocale;
    }

    @NonNull
    @Override
    public CharSequence getKeyboardId() {
        return getKeyboardAddOn().getId();
    }

    @Override
    public int getKeyboardIconResId() {
        return mIconId;
    }

    @Override
    public CharSequence getKeyboardName() {
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
    public char[] getSentenceSeparators() {
        return mSentenceSeparators;
    }

    @Override
    @CallSuper
    protected boolean setupKeyAfterCreation(AnyKey key) {
        if (super.setupKeyAfterCreation(key)) return true;

        if (key.mCodes.length > 0) {
            switch (key.getPrimaryCode()) {
                case 'a':
                    key.popupCharacters = "\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u00e6\u0105";// "àáâãäåæą";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'c':
                    key.popupCharacters = "\u00e7\u0107\u0109\u010d";// "çćĉč";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'd':
                    key.popupCharacters = "\u0111"; // "đ";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'e':
                    key.popupCharacters = "\u00e8\u00e9\u00ea\u00eb\u0119\u20ac\u0113";// "èéêëę€";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'g':
                    key.popupCharacters = "\u011d";// "ĝ";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'h':
                    key.popupCharacters = "\u0125";// "ĥ";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'i':
                    key.popupCharacters = "\u00ec\u00ed\u00ee\u00ef\u0142\u012B";// "ìíîïł";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'j':
                    key.popupCharacters = "\u0135";// "ĵ";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'l':
                    key.popupCharacters = "\u0142";// "ł";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'o':
                    key.popupCharacters = "\u00f2\u00f3\u00f4\u00f5\u00f6\u00f8\u0151\u0153\u014D";// "òóôõöøőœ";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 's':
                    key.popupCharacters = "\u00a7\u00df\u015b\u015d\u0161";// "§ßśŝš";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'u':
                    key.popupCharacters = "\u00f9\u00fa\u00fb\u00fc\u016d\u0171\u016B";// "ùúûüŭű";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'n':
                    key.popupCharacters = "\u00f1";// "ñ";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'y':
                    key.popupCharacters = "\u00fd\u00ff";// "ýÿ";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case 'z':
                    key.popupCharacters = "\u017c\u017e\u017a";// "żžź";
                    key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
                    break;
                case KeyCodes.SHIFT:
                    if (key.longPressCode == 0) {
                        key.longPressCode = KeyCodes.SHIFT_LOCK;
                    }
                    break;
                case KeyCodes.CTRL:
                    if (key.longPressCode == 0) {
                        key.longPressCode = KeyCodes.CTRL_LOCK;
                    }
                    break;
                default:
                    super.setupKeyAfterCreation(key);
            }
            return true;
        }
        return false;
    }
}
