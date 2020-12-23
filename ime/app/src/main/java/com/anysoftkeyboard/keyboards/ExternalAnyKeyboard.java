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
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.ime.AnySoftKeyboardBase;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.anysoftkeyboard.utils.LocaleTools;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ExternalAnyKeyboard extends AnyKeyboard implements HardKeyboardTranslator {

    private static final String TAG = "ASKExtendedAnyKbd";

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
    @NonNull private final CharSequence mName;
    private final int mIconId;
    private final String mDefaultDictionary;
    @NonNull private final Locale mLocale;
    private final HardKeyboardSequenceHandler mHardKeyboardTranslator;
    private final Set<Integer> mAdditionalIsLetterExceptions;
    private final char[] mSentenceSeparators;
    private static final int EXPECTED_CAPACITY_SYMBOLS = 4;
    private static final int EXPECTED_CAPACITY_LETTERS = 16;
    private static final int EXPECTED_CAPACITY_NUMBERS = 4;

    private KeyboardExtension mExtensionLayout;

    public ExternalAnyKeyboard(
            @NonNull AddOn keyboardAddOn,
            @NonNull Context askContext,
            @XmlRes int xmlLayoutResId,
            @XmlRes int xmlLandscapeResId,
            @NonNull CharSequence name,
            int iconResId,
            int qwertyTranslationId,
            String defaultDictionary,
            String additionalIsLetterExceptions,
            String sentenceSeparators,
            @KeyboardRowModeId int mode) {
        this(
                keyboardAddOn,
                askContext,
                xmlLayoutResId,
                xmlLandscapeResId,
                name,
                iconResId,
                qwertyTranslationId,
                defaultDictionary,
                additionalIsLetterExceptions,
                sentenceSeparators,
                mode,
                AnyApplication.getKeyboardExtensionFactory(askContext).getEnabledAddOn());
    }

    public ExternalAnyKeyboard(
            @NonNull AddOn keyboardAddOn,
            @NonNull Context askContext,
            @XmlRes int xmlLayoutResId,
            @XmlRes int xmlLandscapeResId,
            @NonNull CharSequence name,
            int iconResId,
            int qwertyTranslationId,
            String defaultDictionary,
            String additionalIsLetterExceptions,
            String sentenceSeparators,
            @KeyboardRowModeId int mode,
            @Nullable KeyboardExtension extKbd) {
        super(
                keyboardAddOn,
                askContext,
                getKeyboardId(askContext, xmlLayoutResId, xmlLandscapeResId),
                mode);
        mName = name;
        mIconId = iconResId;
        mDefaultDictionary = defaultDictionary;
        mLocale = LocaleTools.getLocaleForLocaleString(mDefaultDictionary);
        mExtensionLayout = extKbd;

        if (qwertyTranslationId != AddOn.INVALID_RES_ID) {
            Logger.d(TAG, "Creating qwerty mapping: %d", qwertyTranslationId);
            mHardKeyboardTranslator =
                    createPhysicalTranslatorFromResourceId(
                            keyboardAddOn.getPackageContext(), qwertyTranslationId);
        } else {
            mHardKeyboardTranslator = null;
        }

        if (additionalIsLetterExceptions != null) {
            mAdditionalIsLetterExceptions =
                    new HashSet<>(
                            additionalIsLetterExceptions.codePointCount(
                                    0, additionalIsLetterExceptions.length()));
            for (int i = 0;
                    i < additionalIsLetterExceptions.length(); /*we increment in the code*/ ) {
                final int codePoint = additionalIsLetterExceptions.codePointAt(i);
                i += Character.charCount(codePoint);
                mAdditionalIsLetterExceptions.add(codePoint);
            }
        } else {
            mAdditionalIsLetterExceptions = Collections.emptySet();
        }

        if (sentenceSeparators != null) {
            mSentenceSeparators = sentenceSeparators.toCharArray();
        } else {
            mSentenceSeparators = new char[0];
        }
    }

    public KeyboardExtension getExtensionLayout() {
        return mExtensionLayout;
    }

    private HardKeyboardSequenceHandler createPhysicalTranslatorFromResourceId(
            Context context, int qwertyTranslationId) {
        HardKeyboardSequenceHandler translator = new HardKeyboardSequenceHandler();
        XmlPullParser parser = context.getResources().getXml(qwertyTranslationId);
        final String TAG = "ASKHardTranslationParser";
        try {
            int event;
            boolean inTranslations = false;
            while ((event = parser.next()) != XmlPullParser.END_DOCUMENT) {
                String tag = parser.getName();
                if (event == XmlPullParser.START_TAG) {
                    if (XML_TRANSLATION_TAG.equals(tag)) {
                        inTranslations = true;
                        AttributeSet attrs = Xml.asAttributeSet(parser);
                        final String qwerty = attrs.getAttributeValue(null, XML_QWERTY_ATTRIBUTE);
                        if (qwerty != null) {
                            translator.addQwertyTranslation(qwerty);
                        }
                    } else if (inTranslations && XML_SEQUENCE_TAG.equals(tag)) {
                        AttributeSet attrs = Xml.asAttributeSet(parser);

                        final int[] keyCodes =
                                getKeyCodesFromPhysicalSequence(
                                        attrs.getAttributeValue(null, XML_KEYS_ATTRIBUTE));
                        final boolean isAlt =
                                attrs.getAttributeBooleanValue(null, XML_ALT_ATTRIBUTE, false);
                        final boolean isShift =
                                attrs.getAttributeBooleanValue(null, XML_SHIFT_ATTRIBUTE, false);
                        final String targetChar =
                                attrs.getAttributeValue(null, XML_TARGET_ATTRIBUTE);
                        final String targetCharCode =
                                attrs.getAttributeValue(null, XML_TARGET_CHAR_CODE_ATTRIBUTE);
                        final int target;
                        if (!TextUtils.isEmpty(targetCharCode)) {
                            target = Integer.parseInt(targetCharCode);
                        } else if (!TextUtils.isEmpty(targetChar)) {
                            target = targetChar.charAt(0);
                        } else {
                            throw new IllegalArgumentException(
                                    "both "
                                            + XML_TARGET_CHAR_CODE_ATTRIBUTE
                                            + " and "
                                            + XML_TARGET_ATTRIBUTE
                                            + "for key-codes "
                                            + Arrays.toString(keyCodes)
                                            + " are empty in "
                                            + XML_SEQUENCE_TAG
                                            + " for keyboard "
                                            + getKeyboardId());
                        }

                        // asserting
                        if (keyCodes.length == 0) {
                            Logger.e(
                                    TAG,
                                    "Physical translator sequence does not include mandatory fields "
                                            + XML_KEYS_ATTRIBUTE
                                            + " or "
                                            + XML_TARGET_ATTRIBUTE);
                        } else {
                            if (!isAlt && !isShift) {
                                translator.addSequence(keyCodes, target);
                                // http://code.google.com/p/softkeyboard/issues/detail?id=734
                                translator.addShiftSequence(
                                        keyCodes, Character.toUpperCase(target));
                            } else if (isAlt) {
                                translator.addAltSequence(keyCodes, target);
                            } else {
                                translator.addShiftSequence(keyCodes, target);
                            }
                        }
                    } else if (inTranslations && XML_MULTITAP_TAG.equals(tag)) {
                        AttributeSet attrs = Xml.asAttributeSet(parser);

                        final int[] keyCodes =
                                getKeyCodesFromPhysicalSequence(
                                        attrs.getAttributeValue(null, XML_MULTITAP_KEY_ATTRIBUTE));
                        if (keyCodes.length != 1) {
                            throw new XmlPullParserException(
                                    "attribute "
                                            + XML_MULTITAP_KEY_ATTRIBUTE
                                            + " should contain exactly one key-code when used in "
                                            + XML_MULTITAP_TAG
                                            + " tag!",
                                    parser,
                                    new ParseException(
                                            XML_MULTITAP_KEY_ATTRIBUTE, parser.getLineNumber()));
                        }

                        final boolean isAlt =
                                attrs.getAttributeBooleanValue(null, XML_ALT_ATTRIBUTE, false);
                        final boolean isShift =
                                attrs.getAttributeBooleanValue(null, XML_SHIFT_ATTRIBUTE, false);
                        final String targetCharacters =
                                attrs.getAttributeValue(null, XML_MULTITAP_CHARACTERS_ATTRIBUTE);
                        if (TextUtils.isEmpty(targetCharacters) || targetCharacters.length() < 2) {
                            throw new XmlPullParserException(
                                    "attribute "
                                            + XML_MULTITAP_CHARACTERS_ATTRIBUTE
                                            + " should contain more than one character when used in "
                                            + XML_MULTITAP_TAG
                                            + " tag!",
                                    parser,
                                    new ParseException(
                                            XML_MULTITAP_CHARACTERS_ATTRIBUTE,
                                            parser.getLineNumber()));
                        }

                        for (int characterIndex = 0;
                                characterIndex <= targetCharacters.length();
                                characterIndex++) {
                            int[] multiTapCodes = new int[characterIndex + 1];
                            for (int tapIndex = 0; tapIndex < multiTapCodes.length; tapIndex++) {
                                multiTapCodes[tapIndex] = keyCodes[0];
                            }
                            if (characterIndex < targetCharacters.length()) {
                                final int target = targetCharacters.charAt(characterIndex);

                                if (!isAlt && !isShift) {
                                    translator.addSequence(multiTapCodes, target);
                                    translator.addShiftSequence(
                                            multiTapCodes, Character.toUpperCase(target));
                                } else if (isAlt) {
                                    translator.addAltSequence(keyCodes, target);
                                } else {
                                    translator.addShiftSequence(keyCodes, target);
                                }
                            } else {
                                // and adding the rewind character
                                if (!isAlt && !isShift) {
                                    translator.addSequence(
                                            multiTapCodes, KeyEventStateMachine.KEYCODE_FIRST_CHAR);
                                    translator.addShiftSequence(
                                            multiTapCodes, KeyEventStateMachine.KEYCODE_FIRST_CHAR);
                                } else if (isAlt) {
                                    translator.addAltSequence(
                                            keyCodes, KeyEventStateMachine.KEYCODE_FIRST_CHAR);
                                } else {
                                    translator.addShiftSequence(
                                            keyCodes, KeyEventStateMachine.KEYCODE_FIRST_CHAR);
                                }
                            }
                        }
                    }
                } else if (event == XmlPullParser.END_TAG && XML_TRANSLATION_TAG.equals(tag)) {
                    break;
                }
            }
        } catch (XmlPullParserException e) {
            Logger.e(
                    TAG,
                    e,
                    "Failed to parse keyboard layout. Keyboard '%s' (id %s, package %s), translatorResourceId %d",
                    getKeyboardName(),
                    getKeyboardId(),
                    getKeyboardAddOn().getPackageName(),
                    qwertyTranslationId);
            if (BuildConfig.DEBUG) throw new RuntimeException("Failed to parse keyboard.", e);
        } catch (IOException e) {
            Logger.e(TAG, e, "Failed to read keyboard file.");
        }
        return translator;
    }

    @NonNull
    private int[] getKeyCodesFromPhysicalSequence(String keyCodesArray) {
        String[] split = keyCodesArray.split(",", -1);
        int[] keyCodes = new int[split.length];
        for (int i = 0; i < keyCodes.length; i++) {
            try {
                keyCodes[i] = Integer.parseInt(split[i]); // try parsing as an
                // integer
            } catch (final NumberFormatException nfe) { // no an integer
                final String v = split[i];
                try {
                    keyCodes[i] =
                            android.view.KeyEvent.class
                                    .getField(v)
                                    .getInt(null); // here comes the reflection. No
                    // bother of performance.
                    // First hit takes just 20 milliseconds, the next hits <2
                    // Milliseconds.
                } catch (final Exception ex) { // crap :(
                    throw new RuntimeException(ex); // bum
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
    public @NonNull Locale getLocale() {
        return mLocale;
    }

    @NonNull
    @Override
    public String getKeyboardId() {
        return getKeyboardAddOn().getId();
    }

    @Override
    public int getKeyboardIconResId() {
        return mIconId;
    }

    @NonNull
    @Override
    public CharSequence getKeyboardName() {
        return mName;
    }

    private static int getKeyboardId(Context context, int portraitId, int landscapeId) {
        final boolean inPortraitMode =
                (context.getResources().getConfiguration().orientation
                        == Configuration.ORIENTATION_PORTRAIT);

        if (inPortraitMode) {
            return portraitId;
        } else {
            return landscapeId;
        }
    }

    @Override
    public void translatePhysicalCharacter(
            HardKeyboardAction action, AnySoftKeyboardBase ime, int multiTapTimeout) {
        if (mHardKeyboardTranslator != null) {
            final int translated;
            if (action.isAltActive()
                    && mHardKeyboardTranslator.addSpecialKey(KeyCodes.ALT, multiTapTimeout)) {
                return;
            }

            if (action.isShiftActive()
                    && mHardKeyboardTranslator.addSpecialKey(KeyCodes.SHIFT, multiTapTimeout)) {
                return;
            }

            translated =
                    mHardKeyboardTranslator.getCurrentCharacter(
                            action.getKeyCode(), ime, multiTapTimeout);

            if (translated != 0) {
                action.setNewKeyCode(translated);
            }
        }
    }

    @Override
    public boolean isInnerWordLetter(int keyValue) {
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
        /* We're assigning greek letters to latin letters
         * by mapping the standard greek layout on a PC to a
         * QWERTY layout.
         * Comparison of several mappings:
         * ABCDEFGHIJKLMNOPQRSTUVWXYZ QWERTY KEYBOARD
         * αβξδεφγθιϊκλμνοπψρστυϋωχηζ VIM digraphs
         * ΑΒΞΔΕΦΓΘΙΪΚΛΜΝΟΠΨΡΣΤΥΫΩΧΗΖ VIM DIGRAPHS
         * αβψδεφγηιξκλμνοπ;ρστθωςχυζ Greek layout
         * ΑΒΨΔΕΦΓΗΙΞΚΛΜΝΟΠ;ΡΣΤΘΩΣΧΥΖ GREEK LAYOUT
         * αβχδεφγηι κλμνοπθρστυ ωχψζ Magicplot
         * ΑΒΧΔΕΦΓΗΙ ΚΛΜΝΟΠΘΡΣΤΥ ΩΧΨΖ MAGICPLOT */
        CharSequence defaultCharacters = "";
        if (key.mCodes.length > 0) {
            switch (key.getPrimaryCode()) {
                case 'a':
                    defaultCharacters = "àáâãāäåæąăαª";
                    break;
                case 'b':
                    defaultCharacters = "β";
                    break;
                case 'c':
                    defaultCharacters = "çćĉčψ";
                    break;
                case 'd':
                    defaultCharacters = "đďδ";
                    break;
                case 'e':
                    defaultCharacters = "èéêëęėěēẽẻε€";
                    break;
                case 'f':
                    defaultCharacters = "φ";
                    break;
                case 'g':
                    defaultCharacters = "ĝğγ";
                    break;
                case 'h':
                    defaultCharacters = "ĥη";
                    break;
                case 'i':
                    defaultCharacters = "ìíîïīǐįıɨι";
                    break;
                case 'j':
                    defaultCharacters = "ĵξ";
                    break;
                case 'k':
                    defaultCharacters = "κ";
                    break;
                case 'l':
                    defaultCharacters = "ľĺłλ";
                    break;
                case 'm':
                    defaultCharacters = "μ";
                    break;
                case 'n':
                    defaultCharacters = "ñńν";
                    break;
                case 'o':
                    defaultCharacters = "òóôǒōõöőøœoº";
                    break;
                case 'p':
                    defaultCharacters = "π";
                    break;
                case 'r':
                    defaultCharacters = "řŕρ";
                    break;
                case 's':
                    defaultCharacters = "ßśŝšș§σ";
                    break;
                case 't':
                    defaultCharacters = "țťţτ";
                    break;
                case 'u':
                    defaultCharacters = "ùúǔûüŭűūųůṷθ";
                    break;
                case 'v':
                    defaultCharacters = "ω";
                    break;
                case 'w':
                    defaultCharacters = "ŵς";
                    break;
                case 'x':
                    defaultCharacters = "χ";
                    break;
                case 'y':
                    defaultCharacters = "ýÿυ";
                    break;
                case 'z':
                    defaultCharacters = "żžźζ";
                    break;
            }
            StringBuilder languageSpecificLetters = new StringBuilder(EXPECTED_CAPACITY_LETTERS);
            StringBuilder symbols = new StringBuilder(EXPECTED_CAPACITY_SYMBOLS);
            StringBuilder numbers = new StringBuilder(EXPECTED_CAPACITY_NUMBERS);
            if (key.popupCharacters != null && key.popupCharacters.length() != 0) {
                int index = 0;
                while (index < key.popupCharacters.length()) {
                    final int codePoint = Character.codePointAt(key.popupCharacters, index);
                    if (Character.isLetter(codePoint)) {
                        languageSpecificLetters.append(Character.toChars(codePoint));
                    } else if (Character.isDigit(codePoint)) {
                        numbers.append(Character.toChars(codePoint));
                    } else {
                        symbols.append(Character.toChars(codePoint));
                    }
                    index += Character.charCount(codePoint);
                }
            }
            // using a fixed, weird order as proof of concept:
            final CharSequence allTheSymbols = symbols.append(languageSpecificLetters).append(numbers).append(defaultCharacters);
            // removing repeated characters (remembering that some Unicode characters can fill up two Java chars)
            HashSet<Integer> popupKeyCodes = new HashSet<>(EXPECTED_CAPACITY_LETTERS + EXPECTED_CAPACITY_NUMBERS + EXPECTED_CAPACITY_SYMBOLS);
            StringBuilder popupCharactersBuilder = new StringBuilder(EXPECTED_CAPACITY_LETTERS + EXPECTED_CAPACITY_NUMBERS + EXPECTED_CAPACITY_SYMBOLS);
            int index = 0;
            while (index < allTheSymbols.length()) {
                final int codePoint = Character.codePointAt(allTheSymbols, index);
                if (popupKeyCodes.add(codePoint)) {
                    popupCharactersBuilder.append(Character.toChars(codePoint));
                }
                index += Character.charCount(codePoint);
            }
            if (popupCharactersBuilder.length() > 0) {
                key.popupCharacters = popupCharactersBuilder;
                key.popupResId = com.menny.android.anysoftkeyboard.R.xml.popup_one_row;
            } else {
                super.setupKeyAfterCreation(key);
            }
            return true;
        }
        return false;
    }
}
