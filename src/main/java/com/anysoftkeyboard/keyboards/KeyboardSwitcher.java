/*
 * Copyright (c) 2015 Menny Even-Danan
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
import android.content.res.Resources;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.base.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class KeyboardSwitcher {
    private static String TAG = "ASK_KeySwitcher";

    public static final AnyKeyboard[] EMPTY_AnyKeyboards = new AnyKeyboard[]{};

    public enum NextKeyboardType {
        Symbols, Alphabet, AlphabetSupportsPhysical, Any, PreviousAny, AnyInsideMode, OtherMode
    }

    public static final int MODE_TEXT = 1;
    public static final int MODE_SYMBOLS = 2;
    public static final int MODE_PHONE = 3;
    public static final int MODE_URL = 4;
    public static final int MODE_EMAIL = 5;
    public static final int MODE_IM = 6;
    public static final int MODE_DATETIME = 7;
    public static final int MODE_NUMBERS = 8;

    private final int KEYBOARDMODE_NORMAL;
    private final int KEYBOARDMODE_URL;
    private final int KEYBOARDMODE_EMAIL;
    private final int KEYBOARDMODE_IM;

    AnyKeyboardView mInputView;
    AnySoftKeyboard mIME;
    private final Context mContext;

    private static final int SYMBOLS_KEYBOARD_REGULAR_INDEX = 0;
    private static final int SYMBOLS_KEYBOARD_ALT_INDEX = 1;
    private static final int SYMBOLS_KEYBOARD_ALT_NUMBERS_INDEX = 2;
    private static final int SYMBOLS_KEYBOARD_LAST_CYCLE_INDEX = SYMBOLS_KEYBOARD_ALT_NUMBERS_INDEX;
    private static final int SYMBOLS_KEYBOARD_NUMBERS_INDEX = 3;
    private static final int SYMBOLS_KEYBOARD_PHONE_INDEX = 4;
    private static final int SYMBOLS_KEYBOARD_DATETIME_INDEX = 5;
    private static final int SYMBOLS_KEYBOARDS_COUNT = 6;

    private int mMode;
    private int mLastSelectedSymbolsKeyboard = SYMBOLS_KEYBOARD_REGULAR_INDEX;

    private AnyKeyboard[] mSymbolsKeyboardsArray = EMPTY_AnyKeyboards;
    // my working keyboards
    private AnyKeyboard[] mAlphabetKeyboards = EMPTY_AnyKeyboards;
    private KeyboardAddOnAndBuilder[] mAlphabetKeyboardsCreators = null;
    // issue 146
    private boolean mRightToLeftMode = false;

    // this flag will be used for inputs which require specific layout
    // thus disabling the option to move to another layout
    private boolean mKeyboardLocked = false;

    private int mLastSelectedKeyboard = 0;

    // private int mImeOptions;
    private boolean mAlphabetMode = true;
    private int mLastKeyboardMode;
    private int mLatinKeyboardIndex;

    private final KeyboardDimens mKeyboardDimens;

    // Constructor hidden
    public KeyboardSwitcher(AnySoftKeyboard ime) {
        mIME = ime;
        mContext = ime.getApplicationContext();
        final Resources res = mContext.getResources();
        mKeyboardDimens = new KeyboardDimens() {

            public int getSmallKeyHeight() {
                return res.getDimensionPixelOffset(R.dimen.default_key_half_height);
            }

            public float getRowVerticalGap() {
                return res.getDimensionPixelOffset(R.dimen.default_key_vertical_gap);
            }

            public int getNormalKeyHeight() {
                return res.getDimensionPixelOffset(R.dimen.default_key_height);
            }

            public int getLargeKeyHeight() {
                return res.getDimensionPixelOffset(R.dimen.default_key_tall_height);
            }

            public int getKeyboardMaxWidth() {
                return mContext.getResources().getDisplayMetrics().widthPixels;
            }

            public float getKeyHorizontalGap() {
                return res.getDimensionPixelOffset(R.dimen.default_key_horizontal_gap);
            }

            public int getKeyMaxWidth() {
                return Integer.MAX_VALUE;
            }
        };
        KEYBOARDMODE_NORMAL = res.getInteger(R.integer.keyboard_mode_normal);
        KEYBOARDMODE_IM = res.getInteger(R.integer.keyboard_mode_im);
        KEYBOARDMODE_URL = res.getInteger(R.integer.keyboard_mode_url);
        KEYBOARDMODE_EMAIL = res.getInteger(R.integer.keyboard_mode_email);
        mMode = KEYBOARDMODE_NORMAL;
    }

    public void setInputView(AnyKeyboardView inputView) {
        if (mInputView != null) {
            mInputView.setKeyboardSwitcher(null);
        }
        mInputView = inputView;
        if (inputView == null) {
            return;
        }
        mInputView.setKeyboardSwitcher(this);
        makeKeyboards(true);
    }

    private synchronized AnyKeyboard getSymbolsKeyboard(int keyboardIndex,
                                                        int mode) {
        makeKeyboards(false);
        AnyKeyboard keyboard = mSymbolsKeyboardsArray[keyboardIndex];

        if (keyboard == null) {
            switch (keyboardIndex) {
                case SYMBOLS_KEYBOARD_REGULAR_INDEX:
                    if (AnyApplication.getConfig().use16KeysSymbolsKeyboards())
                        keyboard = new GenericKeyboard(mContext,
                                R.xml.symbols_16keys, R.xml.symbols,
                                mContext.getString(R.string.symbols_keyboard),
                                "symbols_keyboard", mode);
                    else
                        keyboard = new GenericKeyboard(mContext, R.xml.symbols,
                                mContext.getString(R.string.symbols_keyboard),
                                "symbols_keyboard", mode, false);
                    break;
                case SYMBOLS_KEYBOARD_ALT_INDEX:
                    if (AnyApplication.getConfig().use16KeysSymbolsKeyboards())
                        keyboard = new GenericKeyboard(mContext,
                                R.xml.symbols_alt_16keys, R.xml.symbols_alt,
                                mContext.getString(R.string.symbols_alt_keyboard),
                                "symbols_keyboard", mode);
                    else
                        keyboard = new GenericKeyboard(mContext, R.xml.symbols_alt,
                                mContext.getString(R.string.symbols_alt_keyboard),
                                "alt_symbols_keyboard", mode, false);
                    break;
                case SYMBOLS_KEYBOARD_ALT_NUMBERS_INDEX:
                    keyboard = new GenericKeyboard(mContext,
                            R.xml.simple_alt_numbers,
                            mContext.getString(R.string.symbols_alt_num_keyboard),
                            "alt_numbers_symbols_keyboard", mode, false);
                    break;
                case SYMBOLS_KEYBOARD_PHONE_INDEX:
                    keyboard = new GenericKeyboard(mContext, R.xml.simple_phone,
                            mContext.getString(R.string.symbols_phone_keyboard),
                            "phone_symbols_keyboard", mode, true);
                    break;
                case SYMBOLS_KEYBOARD_NUMBERS_INDEX:
                    keyboard = new GenericKeyboard(mContext, R.xml.simple_numbers,
                            mContext.getString(R.string.symbols_numbers_keyboard),
                            "numbers_symbols_keyboard", mode, false);
                    break;
                case SYMBOLS_KEYBOARD_DATETIME_INDEX:
                    keyboard = new GenericKeyboard(mContext, R.xml.simple_datetime,
                            mContext.getString(R.string.symbols_time_keyboard),
                            "datetime_symbols_keyboard", mode, false);
                    break;
            }
            mSymbolsKeyboardsArray[keyboardIndex] = keyboard;
            mLastSelectedSymbolsKeyboard = keyboardIndex;
            if (mInputView != null) {
                keyboard.loadKeyboard(mInputView.getThemedKeyboardDimens());
                mIME.setKeyboardStuffBeforeSetToView(keyboard);
                mInputView.setKeyboard(keyboard);
            } else {
                keyboard.loadKeyboard(mKeyboardDimens);
            }
        }

        return keyboard;
    }

    private AnyKeyboard[] getAlphabetKeyboards() {
        makeKeyboards(false);
        return mAlphabetKeyboards;
    }

    public synchronized KeyboardAddOnAndBuilder[] getEnabledKeyboardsBuilders() {
        makeKeyboards(false);
        return mAlphabetKeyboardsCreators;
    }

    public synchronized void makeKeyboards(boolean force) {
        if (mContext == null)
            return;

        if (force) {
            resetKeyboardsCache();
        }

        if ((force || mAlphabetKeyboards.length == 0)
                || (mSymbolsKeyboardsArray.length == 0)) {
            if (mAlphabetKeyboards.length == 0) {
                mAlphabetKeyboardsCreators = KeyboardFactory
                        .getEnabledKeyboards(mContext).toArray(
                                new KeyboardAddOnAndBuilder[]{});
                mLatinKeyboardIndex = findLatinKeyboardIndex();
                mAlphabetKeyboards = new AnyKeyboard[mAlphabetKeyboardsCreators.length];
                if (mLastSelectedKeyboard >= mAlphabetKeyboards.length)
                    mLastSelectedKeyboard = 0;
            }
            if (mSymbolsKeyboardsArray.length == 0) {
                mSymbolsKeyboardsArray = new AnyKeyboard[SYMBOLS_KEYBOARDS_COUNT];
                if (mLastSelectedSymbolsKeyboard >= mSymbolsKeyboardsArray.length)
                    mLastSelectedSymbolsKeyboard = 0;
            }
            // freeing old keyboards.
            System.gc();
        }
    }

    /*
     * This is used for url and emails fields
     */
    private int findLatinKeyboardIndex() {
        if (mAlphabetKeyboardsCreators == null)
            return -1;

        for (int index = 0; index < mAlphabetKeyboardsCreators.length; index++) {
            final KeyboardAddOnAndBuilder builder = mAlphabetKeyboardsCreators[index];
            if (builder.getId().equals("c7535083-4fe6-49dc-81aa-c5438a1a343a"))
                return index;
        }

        return -1;
    }

    synchronized void resetKeyboardsCache() {
        mAlphabetKeyboards = EMPTY_AnyKeyboards;
        mSymbolsKeyboardsArray = EMPTY_AnyKeyboards;
    }

    public void setKeyboardMode(final int mode, final EditorInfo attr,
                                final boolean restarting) {
        final int previousMode = mMode;
        mMode = mode;
        boolean resubmitToView = true;
        AnyKeyboard keyboard = null;

        switch (mode) {
            case MODE_DATETIME:
                mAlphabetMode = false;
                mKeyboardLocked = true;
                keyboard = getSymbolsKeyboard(SYMBOLS_KEYBOARD_DATETIME_INDEX,
                        getKeyboardMode(attr));
                break;
            case MODE_NUMBERS:
                mAlphabetMode = false;
                mKeyboardLocked = true;
                keyboard = getSymbolsKeyboard(SYMBOLS_KEYBOARD_NUMBERS_INDEX,
                        getKeyboardMode(attr));
                break;
            case MODE_SYMBOLS:
                mAlphabetMode = false;
                mKeyboardLocked = true;
                keyboard = getSymbolsKeyboard(SYMBOLS_KEYBOARD_REGULAR_INDEX,
                        getKeyboardMode(attr));
                break;
            case MODE_PHONE:
                mAlphabetMode = false;
                mKeyboardLocked = true;
                keyboard = getSymbolsKeyboard(SYMBOLS_KEYBOARD_PHONE_INDEX,
                        getKeyboardMode(attr));
                break;
            case MODE_URL:
            case MODE_EMAIL:
                // starting with English, but only in non-restarting mode
                // this is a fix for issue #62
                if (!restarting && mLatinKeyboardIndex >= 0) {
                    mLastSelectedKeyboard = mLatinKeyboardIndex;
                }
                // note: letting it fall-through to the default branch
            default:
                mKeyboardLocked = false;
                // I'll start with a new alphabet keyboard if
                // 1) this is a non-restarting session, which means it is a brand
                // new input field.
                // 2) this is a restarting, but the mode what change (probably to
                // Normal).
                if (!restarting || mMode != previousMode) {
                    mAlphabetMode = true;
                    keyboard = getAlphabetKeyboard(mLastSelectedKeyboard, getKeyboardMode(attr));
                } else {
                    // just keep doing what you did before.
                    keyboard = getCurrentKeyboard();
                    resubmitToView = false;
                }
                break;
        }

        keyboard.setImeOptions(mContext.getResources(), attr);
        // now show
        if (resubmitToView) {
            mIME.setKeyboardStuffBeforeSetToView(keyboard);
            if (mInputView != null) {
                mInputView.setKeyboard(keyboard);
            }
        }
    }

    private int getKeyboardMode(EditorInfo attr) {
        if (attr == null)
            return mLastKeyboardMode = KEYBOARDMODE_NORMAL;

        int variation = attr.inputType & EditorInfo.TYPE_MASK_VARIATION;

        switch (variation) {
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                return mLastKeyboardMode = KEYBOARDMODE_EMAIL;
            case EditorInfo.TYPE_TEXT_VARIATION_URI:
                return mLastKeyboardMode = KEYBOARDMODE_URL;
            case EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_SUBJECT:
            case EditorInfo.TYPE_TEXT_VARIATION_LONG_MESSAGE:
                return mLastKeyboardMode = KEYBOARDMODE_IM;
            default:
                return mLastKeyboardMode = KEYBOARDMODE_NORMAL;
        }
    }

    public boolean isAlphabetMode() {
        return mAlphabetMode;
    }

    public AnyKeyboard nextAlphabetKeyboard(EditorInfo currentEditorInfo,
                                            String keyboardId) {
        AnyKeyboard current = getLockedKeyboard(currentEditorInfo);
        if (current != null)
            return current;

        final int keyboardsCount = getAlphabetKeyboards().length;
        for (int keyboardIndex = 0; keyboardIndex < keyboardsCount; keyboardIndex++) {
            current = getAlphabetKeyboard(keyboardIndex,
                    getKeyboardMode(currentEditorInfo));
            if (current.getKeyboardPrefId().equals(keyboardId)) {
                mAlphabetMode = true;
                mLastSelectedKeyboard = keyboardIndex;
                // returning to the regular symbols keyboard, no matter what
                mLastSelectedSymbolsKeyboard = 0;
                // Issue 146
                mRightToLeftMode = !current.isLeftToRightLanguage();

                return setKeyboard(currentEditorInfo, current);
            }
        }

        Log.w(TAG, "For some reason, I can't find keyboard with ID "
                + keyboardId);
        return null;
    }

    private AnyKeyboard getLockedKeyboard(EditorInfo currentEditorInfo) {
        if (mKeyboardLocked) {
            AnyKeyboard current = getCurrentKeyboard();
            Log.i(TAG,
                    "Request for nextAlphabetKeyboard, but the keyboard-switcher is locked! Returning "
                            + current.getKeyboardName());
            // Issue 146
            mRightToLeftMode = !current.isLeftToRightLanguage();
            return setKeyboard(currentEditorInfo, current);
        } else {
            return null;
        }
    }

    public String peekNextSymbolsKeyboard() {
        if (mKeyboardLocked) {
            return mContext.getString(R.string.keyboard_change_locked);
        } else {
            int nextKeyboardIndex = getNextSymbolsKeyboardIndex();
            int tooltipResId;
            switch (nextKeyboardIndex) {
                case SYMBOLS_KEYBOARD_ALT_INDEX:
                    tooltipResId = R.string.symbols_alt_keyboard;
                    break;
                case SYMBOLS_KEYBOARD_ALT_NUMBERS_INDEX:
                    tooltipResId = R.string.symbols_alt_num_keyboard;
                    break;
                case SYMBOLS_KEYBOARD_PHONE_INDEX:
                    tooltipResId = R.string.symbols_phone_keyboard;
                    break;
                case SYMBOLS_KEYBOARD_NUMBERS_INDEX:
                    tooltipResId = R.string.symbols_numbers_keyboard;
                    break;
                case SYMBOLS_KEYBOARD_DATETIME_INDEX:
                    tooltipResId = R.string.symbols_time_keyboard;
                    break;
                default:
                    // case SYMBOLS_KEYBOARD_REGULAR_INDEX:
                    tooltipResId = R.string.symbols_keyboard;
                    break;
            }
            return mContext.getString(tooltipResId);
        }
    }

    public String peekNextAlphabetKeyboard() {
        if (mKeyboardLocked) {
            return mContext.getString(R.string.keyboard_change_locked);
        } else {
            final int keyboardsCount = mAlphabetKeyboardsCreators.length;
            int selectedKeyboard = mLastSelectedKeyboard;
            if (isAlphabetMode())
                selectedKeyboard++;

            if (selectedKeyboard >= keyboardsCount)
                selectedKeyboard = 0;

            return mAlphabetKeyboardsCreators[selectedKeyboard].getName();
        }
    }

    private AnyKeyboard nextAlphabetKeyboard(EditorInfo currentEditorInfo,
                                             boolean supportsPhysical) {
        AnyKeyboard current = getLockedKeyboard(currentEditorInfo);

        if (current == null) {
            final int keyboardsCount = getAlphabetKeyboards().length;
            if (isAlphabetMode())
                mLastSelectedKeyboard++;

            mAlphabetMode = true;

            if (mLastSelectedKeyboard >= keyboardsCount)
                mLastSelectedKeyboard = 0;

            current = getAlphabetKeyboard(mLastSelectedKeyboard,
                    getKeyboardMode(currentEditorInfo));
            // returning to the regular symbols keyboard, no matter what
            mLastSelectedSymbolsKeyboard = 0;

            if (supportsPhysical) {
                int testsLeft = keyboardsCount;
                while (!(current instanceof HardKeyboardTranslator)
                        && (testsLeft > 0)) {
                    mLastSelectedKeyboard++;
                    if (mLastSelectedKeyboard >= keyboardsCount)
                        mLastSelectedKeyboard = 0;
                    current = getAlphabetKeyboard(mLastSelectedKeyboard,
                            getKeyboardMode(currentEditorInfo));
                    testsLeft--;
                }
                // if we scanned all keyboards... we screwed...
                if (testsLeft == 0) {
                    Log.w(TAG,
                            "Could not locate the next physical keyboard. Will continue with "
                                    + current.getKeyboardName());
                }
            }

            // Issue 146
            mRightToLeftMode = !current.isLeftToRightLanguage();

            return setKeyboard(currentEditorInfo, current);
        } else
            return current;
    }

    private AnyKeyboard nextSymbolsKeyboard(EditorInfo currentEditorInfo) {
        AnyKeyboard locked = getLockedKeyboard(currentEditorInfo);
        if (locked != null)
            return locked;

        mLastSelectedSymbolsKeyboard = getNextSymbolsKeyboardIndex();
        mAlphabetMode = false;
        AnyKeyboard current = getSymbolsKeyboard(mLastSelectedSymbolsKeyboard,
                getKeyboardMode(currentEditorInfo));
        return setKeyboard(currentEditorInfo, current);
    }

    private int getNextSymbolsKeyboardIndex() {
        int nextKeyboardIndex = mLastSelectedSymbolsKeyboard;
        if (AnyApplication.getConfig().getCycleOverAllSymbols()) {
            if (!isAlphabetMode()) {
                if (nextKeyboardIndex >= SYMBOLS_KEYBOARD_LAST_CYCLE_INDEX)
                    nextKeyboardIndex = SYMBOLS_KEYBOARD_REGULAR_INDEX;
                else
                    nextKeyboardIndex++;
            } else
                nextKeyboardIndex = SYMBOLS_KEYBOARD_REGULAR_INDEX;
        } else {
            nextKeyboardIndex = SYMBOLS_KEYBOARD_REGULAR_INDEX;
        }
        return nextKeyboardIndex;
    }

    private AnyKeyboard setKeyboard(EditorInfo currentEditorInfo,
                                    AnyKeyboard current) {
        if (mContext == null)
            return current;
        /*
         * //all keyboards start as un-shifted, except the second symbols //due
         * to lazy loading the keyboards, the symbols may not be created yet.
         * current.setShifted(current ==
         * mSymbolsKeyboardsArray[SYMBOLS_KEYBOARD_ALT_INDEX]);
         */
        current.setImeOptions(mContext.getResources(), currentEditorInfo);
        // current.setTextVariation(mContext.getResources(),
        // currentEditorInfo.inputType);

        // now show
        mIME.setKeyboardStuffBeforeSetToView(current);
        if (mInputView != null)
            mInputView.setKeyboard(current);

        return current;
    }

    public AnyKeyboard getCurrentKeyboard() {
        if (isAlphabetMode())
            return getAlphabetKeyboard(mLastSelectedKeyboard, mLastKeyboardMode);
        else
            return getSymbolsKeyboard(mLastSelectedSymbolsKeyboard,
                    mLastKeyboardMode);
    }

    private synchronized AnyKeyboard getAlphabetKeyboard(int index, int mode) {
        AnyKeyboard[] keyboards = getAlphabetKeyboards();
        if (index >= keyboards.length)
            index = 0;

        AnyKeyboard keyboard = keyboards[index];

        if (keyboard == null || keyboard.getKeyboardMode() != mode) {
            KeyboardAddOnAndBuilder creator = mAlphabetKeyboardsCreators[index];
            Log.d(TAG, "About to create keyboard: " + creator.getId());
            keyboards[index] = creator.createKeyboard(mContext, mode);
            keyboard = keyboards[index];
            if (mInputView != null) {
                keyboard.loadKeyboard(mInputView.getThemedKeyboardDimens());
                mIME.setKeyboardStuffBeforeSetToView(keyboard);
                mInputView.setKeyboard(keyboard);
            } else {
                keyboard.loadKeyboard(mKeyboardDimens);
            }
        }
        return keyboard;
    }

    public AnyKeyboard nextKeyboard(EditorInfo currentEditorInfo,
                                    NextKeyboardType type) {
        AnyKeyboard locked = getLockedKeyboard(currentEditorInfo);
        if (locked != null)
            return locked;

        switch (type) {
            case Alphabet:
            case AlphabetSupportsPhysical:
                return nextAlphabetKeyboard(currentEditorInfo,
                        (type == NextKeyboardType.AlphabetSupportsPhysical));
            case Symbols:
                return nextSymbolsKeyboard(currentEditorInfo);
            case Any:
            case PreviousAny:// currently we'll support only one direction
                // cycling through the alphabet, and at the end, going to the
                // symbols.
                final int alphabetKeyboardsCount = getAlphabetKeyboards().length;
                if (mAlphabetMode) {
                    if (mLastSelectedKeyboard >= (alphabetKeyboardsCount - 1)) {// we
                        // are
                        // at
                        // the
                        // last
                        // alphabet
                        // keyboard
                        mLastSelectedKeyboard = 0;
                        return nextSymbolsKeyboard(currentEditorInfo);
                    } else {
                        return nextAlphabetKeyboard(currentEditorInfo, false);
                    }
                } else {
                    if (mLastSelectedSymbolsKeyboard >= SYMBOLS_KEYBOARD_LAST_CYCLE_INDEX) {// we
                        // are
                        // at
                        // the
                        // last
                        // symbols
                        // keyboard
                        mLastSelectedSymbolsKeyboard = 0;
                        return nextAlphabetKeyboard(currentEditorInfo, false);
                    } else {
                        return nextSymbolsKeyboard(currentEditorInfo);
                    }
                }
            case AnyInsideMode:
                if (mAlphabetMode) {
                    // re-calling this function,but with Alphabet
                    return nextKeyboard(currentEditorInfo,
                            NextKeyboardType.Alphabet);
                } else {
                    // re-calling this function,but with Symbols
                    return nextKeyboard(currentEditorInfo, NextKeyboardType.Symbols);
                }
            case OtherMode:
                if (mAlphabetMode) {
                    // re-calling this function,but with Symbols
                    return nextKeyboard(currentEditorInfo, NextKeyboardType.Symbols);
                } else {
                    // re-calling this function,but with Alphabet
                    return nextKeyboard(currentEditorInfo,
                            NextKeyboardType.Alphabet);
                }
            default:
                return nextAlphabetKeyboard(currentEditorInfo, false);
        }
    }

    public AnyKeyboard nextAlterKeyboard(EditorInfo currentEditorInfo) {
        AnyKeyboard locked = getLockedKeyboard(currentEditorInfo);
        if (locked != null)
            return locked;

        AnyKeyboard currentKeyboard = getCurrentKeyboard();

        if (!isAlphabetMode()) {
            if (mLastSelectedSymbolsKeyboard == SYMBOLS_KEYBOARD_REGULAR_INDEX) {
                mLastSelectedSymbolsKeyboard = SYMBOLS_KEYBOARD_ALT_INDEX;
            } else// if (mLastSelectedSymbolsKeyboard ==
            // SYMBOLS_KEYBOARD_ALT_INDEX)
            {
                mLastSelectedSymbolsKeyboard = SYMBOLS_KEYBOARD_REGULAR_INDEX;
            }
            // else return currentKeyboard;

            currentKeyboard = getSymbolsKeyboard(mLastSelectedSymbolsKeyboard,
                    getKeyboardMode(currentEditorInfo));

            return setKeyboard(currentEditorInfo, currentKeyboard);
        }

        return currentKeyboard;
    }

    public boolean isCurrentKeyboardPhysical() {
        AnyKeyboard current = getCurrentKeyboard();
        return (current != null) && (current instanceof HardKeyboardTranslator);
    }

    public void onLowMemory() {
        if (mSymbolsKeyboardsArray != null) {
            for (int index = 0; index < mSymbolsKeyboardsArray.length; index++) {
                AnyKeyboard current = mSymbolsKeyboardsArray[index];
                if ((current != null)
                        && (isAlphabetMode() || (mLastSelectedSymbolsKeyboard != index))) {
                    Log.i(TAG, "KeyboardSwitcher::onLowMemory: Removing "
                            + current.getKeyboardName());
                    mSymbolsKeyboardsArray[index] = null;
                }
            }
            // in alphabet we are a bit cautious..
            // just removing the not selected keyboards.
            for (int index = 0; index < mAlphabetKeyboards.length; index++) {
                AnyKeyboard current = mAlphabetKeyboards[index];
                if ((current != null) && (mLastSelectedKeyboard != index)) {
                    Log.i(TAG, "KeyboardSwitcher::onLowMemory: Removing "
                            + current.getKeyboardName());
                    mAlphabetKeyboards[index] = null;
                }
            }
        }
    }

    public boolean isRightToLeftMode() {
        return mRightToLeftMode;
    }

    public boolean shouldPopupForLanguageSwitch() {
        // only in alphabet mode,
        // and only if there are more than two keyboards
        // and only if user requested to have a popup
        return mAlphabetMode
                && (getAlphabetKeyboards().length > 2)
                && AnyApplication.getConfig()
                .shouldShowPopupForLanguageSwitch();
    }
}
