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
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_EMAIL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_IM;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_URL;

public class KeyboardSwitcher {

    public static final int INPUT_MODE_TEXT = 1;
    public static final int INPUT_MODE_SYMBOLS = 2;
    public static final int INPUT_MODE_PHONE = 3;
    public static final int INPUT_MODE_URL = 4;
    public static final int INPUT_MODE_EMAIL = 5;
    public static final int INPUT_MODE_IM = 6;
    public static final int INPUT_MODE_DATETIME = 7;
    public static final int INPUT_MODE_NUMBERS = 8;
    static final String PACKAGE_ID_TO_KEYBOARD_ID_TOKEN = "\\s+->\\s+";
    private static final AnyKeyboard[] EMPTY_AnyKeyboards = new AnyKeyboard[0];
    private static final KeyboardAddOnAndBuilder[] EMPTY_Creators = new KeyboardAddOnAndBuilder[0];
    private static final int SYMBOLS_KEYBOARD_REGULAR_INDEX = 0;
    private static final int SYMBOLS_KEYBOARD_ALT_INDEX = 1;
    private static final int SYMBOLS_KEYBOARD_ALT_NUMBERS_INDEX = 2;
    private static final int SYMBOLS_KEYBOARD_LAST_CYCLE_INDEX = SYMBOLS_KEYBOARD_ALT_NUMBERS_INDEX;
    private static final int SYMBOLS_KEYBOARD_NUMBERS_INDEX = 3;
    private static final int SYMBOLS_KEYBOARD_PHONE_INDEX = 4;
    private static final int SYMBOLS_KEYBOARD_DATETIME_INDEX = 5;
    private static final int SYMBOLS_KEYBOARDS_COUNT = 6;
    private static String TAG = "ASK_KeySwitcher";
    @NonNull
    private final KeyboardSwitchedListener mKeyboardSwitchedListener;
    @NonNull
    private final Context mContext;
    //this will hold the last used keyboard ID per app's package ID
    private final ArrayMap<String, CharSequence> mAlphabetKeyboardIndexByPackageId = new ArrayMap<>();
    private final KeyboardDimens mKeyboardDimens;
    private final DefaultAddOn mDefaultAddOn;
    private final SharedPreferences mSharedPrefs;
    @NonNull
    private final String mLayoutForInternetInputPrefId;
    @NonNull
    private final String mDefaultKeyboardId;
    @Nullable
    private InputViewBinder mInputView;
    @Keyboard.KeyboardRowModeId
    private int mKeyboardRowMode = KEYBOARD_ROW_MODE_NORMAL;
    private int mLastSelectedSymbolsKeyboard = SYMBOLS_KEYBOARD_REGULAR_INDEX;
    @NonNull
    @VisibleForTesting
    protected AnyKeyboard[] mSymbolsKeyboardsArray = EMPTY_AnyKeyboards;
    @NonNull
    @VisibleForTesting
    protected AnyKeyboard[] mAlphabetKeyboards = EMPTY_AnyKeyboards;
    @NonNull
    private KeyboardAddOnAndBuilder[] mAlphabetKeyboardsCreators = EMPTY_Creators;
    // this flag will be used for inputs which require specific layout
    // thus disabling the option to move to another layout
    private boolean mKeyboardLocked = false;
    private int mLastSelectedKeyboardIndex = 0;
    private boolean mAlphabetMode = true;
    @Nullable
    private EditorInfo mLastEditorInfo;
    private int mInternetInputLayoutIndex;
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (mLayoutForInternetInputPrefId.equals(key)) {
                mInternetInputLayoutIndex = findIndexOfInternetInputLayout();
            }
        }
    };

    public KeyboardSwitcher(@NonNull KeyboardSwitchedListener ime, @NonNull Context context) {
        mDefaultAddOn = new DefaultAddOn(context, context);
        mKeyboardSwitchedListener = ime;
        mContext = context;
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
        };
        mKeyboardRowMode = KEYBOARD_ROW_MODE_NORMAL;
        //loading saved package-id from prefs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            loadKeyboardAppMapping();
        }

        mLayoutForInternetInputPrefId = context.getString(R.string.settings_key_layout_for_internet_fields);
        mDefaultKeyboardId = context.getString(R.string.settings_default_keyboard_id);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        AnyApplication.getConfig().addChangedListener(mPrefChangedListener);
    }

    @Keyboard.KeyboardRowModeId
    private static int getKeyboardMode(EditorInfo attr) {
        if (attr == null) return KEYBOARD_ROW_MODE_NORMAL;

        int variation = attr.inputType & EditorInfo.TYPE_MASK_VARIATION;

        switch (variation) {
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                return KEYBOARD_ROW_MODE_EMAIL;
            case EditorInfo.TYPE_TEXT_VARIATION_URI:
                return KEYBOARD_ROW_MODE_URL;
            case EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_SUBJECT:
            case EditorInfo.TYPE_TEXT_VARIATION_LONG_MESSAGE:
                return KEYBOARD_ROW_MODE_IM;
            case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                return AnyApplication.getConfig().supportPasswordKeyboardRowMode() ?
                        Keyboard.KEYBOARD_ROW_MODE_PASSWORD : Keyboard.KEYBOARD_ROW_MODE_NORMAL;
            default:
                return KEYBOARD_ROW_MODE_NORMAL;
        }
    }

    public void setInputView(@NonNull InputViewBinder inputView) {
        mInputView = inputView;
        flushKeyboardsCache();
    }

    @NonNull
    private synchronized AnyKeyboard getSymbolsKeyboard(int keyboardIndex) {
        ensureKeyboardsAreBuilt();
        AnyKeyboard keyboard = mSymbolsKeyboardsArray[keyboardIndex];

        if (keyboard == null || keyboard.getKeyboardMode() != mKeyboardRowMode) {
            switch (keyboardIndex) {
                case SYMBOLS_KEYBOARD_REGULAR_INDEX:
                    if (AnyApplication.getConfig().use16KeysSymbolsKeyboards())
                        keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols_16keys, R.xml.symbols, mContext.getString(R.string.symbols_keyboard), "symbols_keyboard", mKeyboardRowMode, false);
                    else
                        keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols, mContext.getString(R.string.symbols_keyboard), "symbols_keyboard", mKeyboardRowMode, false);
                    break;
                case SYMBOLS_KEYBOARD_ALT_INDEX:
                    if (AnyApplication.getConfig().use16KeysSymbolsKeyboards())
                        keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols_alt_16keys, R.xml.symbols_alt, mContext.getString(R.string.symbols_alt_keyboard), "alt_symbols_keyboard", mKeyboardRowMode, false);
                    else
                        keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols_alt, R.xml.symbols_alt, mContext.getString(R.string.symbols_alt_keyboard), "alt_symbols_keyboard", mKeyboardRowMode, false);
                    break;
                case SYMBOLS_KEYBOARD_ALT_NUMBERS_INDEX:
                    keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.simple_alt_numbers, R.xml.simple_alt_numbers, mContext.getString(R.string.symbols_alt_num_keyboard), "alt_numbers_symbols_keyboard", mKeyboardRowMode, false);
                    break;
                case SYMBOLS_KEYBOARD_PHONE_INDEX:
                    keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.simple_phone, R.xml.simple_phone, mContext.getString(R.string.symbols_phone_keyboard), "phone_symbols_keyboard", mKeyboardRowMode, true);
                    break;
                case SYMBOLS_KEYBOARD_NUMBERS_INDEX:
                    keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.simple_numbers, R.xml.simple_numbers, mContext.getString(R.string.symbols_numbers_keyboard), "numbers_symbols_keyboard", mKeyboardRowMode, false);
                    break;
                case SYMBOLS_KEYBOARD_DATETIME_INDEX:
                    keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.simple_datetime, R.xml.simple_datetime, mContext.getString(R.string.symbols_time_keyboard), "datetime_symbols_keyboard", mKeyboardRowMode, false);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown keyboardIndex " + keyboardIndex);
            }
            mSymbolsKeyboardsArray[keyboardIndex] = keyboard;
            mLastSelectedSymbolsKeyboard = keyboardIndex;
            keyboard.loadKeyboard((mInputView != null) ? mInputView.getThemedKeyboardDimens() : mKeyboardDimens);
            mKeyboardSwitchedListener.onSymbolsKeyboardSet(keyboard);
        }

        return keyboard;
    }

    protected GenericKeyboard createGenericKeyboard(AddOn addOn, Context context, int layoutResId, int landscapeLayoutResId, String name, String keyboardId, int mode, boolean disableKeyPreview) {
        return new GenericKeyboard(addOn, context, layoutResId, landscapeLayoutResId, name, keyboardId, mode, disableKeyPreview);
    }

    private AnyKeyboard[] getAlphabetKeyboards() {
        ensureKeyboardsAreBuilt();
        return mAlphabetKeyboards;
    }

    @NonNull
    public synchronized KeyboardAddOnAndBuilder[] getEnabledKeyboardsBuilders() {
        ensureKeyboardsAreBuilt();
        return mAlphabetKeyboardsCreators;
    }

    public void flushKeyboardsCache() {
        mAlphabetKeyboards = EMPTY_AnyKeyboards;
        mSymbolsKeyboardsArray = EMPTY_AnyKeyboards;
        mAlphabetKeyboardsCreators = EMPTY_Creators;
        mInternetInputLayoutIndex = -1;
        mLastEditorInfo = null;
    }

    private synchronized void ensureKeyboardsAreBuilt() {
        if (mAlphabetKeyboards.length == 0 || mSymbolsKeyboardsArray.length == 0 || mAlphabetKeyboardsCreators.length == 0) {
            if (mAlphabetKeyboards.length == 0 || mAlphabetKeyboardsCreators.length == 0) {
                final List<KeyboardAddOnAndBuilder> enabledKeyboardBuilders = AnyApplication.getKeyboardFactory(mContext).getEnabledAddOns();
                mAlphabetKeyboardsCreators = enabledKeyboardBuilders.toArray(new KeyboardAddOnAndBuilder[enabledKeyboardBuilders.size()]);
                mInternetInputLayoutIndex = findIndexOfInternetInputLayout();
                mAlphabetKeyboards = new AnyKeyboard[mAlphabetKeyboardsCreators.length];
                mLastSelectedKeyboardIndex = 0;
                mKeyboardSwitchedListener.onAvailableKeyboardsChanged(enabledKeyboardBuilders);
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

    private int findIndexOfInternetInputLayout() {
        final String internetLayoutId = mSharedPrefs.getString(mLayoutForInternetInputPrefId, mDefaultKeyboardId);
        for (int index = 0; index < mAlphabetKeyboardsCreators.length; index++) {
            final KeyboardAddOnAndBuilder builder = mAlphabetKeyboardsCreators[index];
            if (builder.getId().equals(internetLayoutId))
                return index;
        }

        return -1;
    }

    public void setKeyboardMode(@InputModeId final int mode, final EditorInfo attr, final boolean restarting) {
        ensureKeyboardsAreBuilt();
        final boolean keyboardGlobalModeChanged = attr.inputType != (mLastEditorInfo == null ? 0 : mLastEditorInfo.inputType);
        mLastEditorInfo = attr;
        mKeyboardRowMode = getKeyboardMode(attr);
        boolean resubmitToView = true;
        AnyKeyboard keyboard;

        switch (mode) {
            case INPUT_MODE_DATETIME:
                mAlphabetMode = false;
                mKeyboardLocked = true;
                keyboard = getSymbolsKeyboard(SYMBOLS_KEYBOARD_DATETIME_INDEX);
                break;
            case INPUT_MODE_NUMBERS:
                mAlphabetMode = false;
                mKeyboardLocked = true;
                keyboard = getSymbolsKeyboard(SYMBOLS_KEYBOARD_NUMBERS_INDEX);
                break;
            case INPUT_MODE_SYMBOLS:
                mAlphabetMode = false;
                mKeyboardLocked = true;
                keyboard = getSymbolsKeyboard(SYMBOLS_KEYBOARD_REGULAR_INDEX);
                break;
            case INPUT_MODE_PHONE:
                mAlphabetMode = false;
                mKeyboardLocked = true;
                keyboard = getSymbolsKeyboard(SYMBOLS_KEYBOARD_PHONE_INDEX);
                break;
            case INPUT_MODE_EMAIL:
            case INPUT_MODE_IM:
            case INPUT_MODE_TEXT:
            case INPUT_MODE_URL:
            default:
                mKeyboardLocked = false;
                if ((!restarting && mInternetInputLayoutIndex >= 0) && (mode == INPUT_MODE_URL || mode == INPUT_MODE_EMAIL)) {
                    // starting with English, but only in non-restarting mode
                    // this is a fix for issue #62
                    mLastSelectedKeyboardIndex = mInternetInputLayoutIndex;
                } else {
                    //trying to re-use last keyboard the user used in this input field.
                    if (AnyApplication.getConfig().getPersistLayoutForPackageId() && (!TextUtils.isEmpty(attr.packageName)) && mAlphabetKeyboardIndexByPackageId.containsKey(attr.packageName)) {
                        final CharSequence reusedKeyboardAddOnId = mAlphabetKeyboardIndexByPackageId.get(attr.packageName);
                        for (int builderIndex = 0; builderIndex < mAlphabetKeyboardsCreators.length; builderIndex++) {
                            KeyboardAddOnAndBuilder builder = mAlphabetKeyboardsCreators[builderIndex];
                            if (builder.getId().equals(reusedKeyboardAddOnId)) {
                                Logger.d(TAG, "Reusing keyboard at index %d for app %s", builderIndex, attr.packageName);
                                mLastSelectedKeyboardIndex = builderIndex;
                            }
                        }
                    }
                }
                // I'll start with a new alphabet keyboard if
                // 1) this is a non-restarting session, which means it is a brand
                // new input field.
                // 2) this is a restarting, but the mode changed (probably to Normal).
                if ((!restarting) || keyboardGlobalModeChanged) {
                    mAlphabetMode = true;
                    keyboard = getAlphabetKeyboard(mLastSelectedKeyboardIndex, attr);
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
            mKeyboardSwitchedListener.onAlphabetKeyboardSet(keyboard);
        }
    }

    private boolean isAlphabetMode() {
        return mAlphabetMode;
    }

    public AnyKeyboard nextAlphabetKeyboard(EditorInfo currentEditorInfo, String keyboardId) {
        AnyKeyboard current = getLockedKeyboard(currentEditorInfo);
        if (current != null) return current;

        final KeyboardAddOnAndBuilder[] enabledKeyboardsBuilders = getEnabledKeyboardsBuilders();
        final int keyboardsCount = enabledKeyboardsBuilders.length;
        for (int keyboardIndex = 0; keyboardIndex < keyboardsCount; keyboardIndex++) {
            if (enabledKeyboardsBuilders[keyboardIndex].getId().equals(keyboardId)) {
                //iterating over builders, so we don't create keyboards just for getting ID
                current = getAlphabetKeyboard(keyboardIndex, currentEditorInfo);
                mAlphabetMode = true;
                mLastSelectedKeyboardIndex = keyboardIndex;
                // returning to the regular symbols keyboard, no matter what
                mLastSelectedSymbolsKeyboard = 0;
                current.setImeOptions(mContext.getResources(), currentEditorInfo);
                mKeyboardSwitchedListener.onAlphabetKeyboardSet(current);
                return current;
            }
        }

        Logger.w(TAG, "For some reason, I can't find keyboard with ID " + keyboardId);
        return null;
    }

    @Nullable
    private AnyKeyboard getLockedKeyboard(EditorInfo currentEditorInfo) {
        if (mKeyboardLocked) {
            AnyKeyboard current = getCurrentKeyboard();
            Logger.i(TAG, "Request for keyboard but the keyboard-switcher is locked! Returning " + current.getKeyboardName());
            current.setImeOptions(mContext.getResources(), currentEditorInfo);
            //locked keyboard is always symbols
            mKeyboardSwitchedListener.onSymbolsKeyboardSet(current);
            return current;
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

    public CharSequence peekNextAlphabetKeyboard() {
        if (mKeyboardLocked) {
            return mContext.getString(R.string.keyboard_change_locked);
        } else {
            final int keyboardsCount = mAlphabetKeyboardsCreators.length;
            int selectedKeyboard = mLastSelectedKeyboardIndex;
            if (isAlphabetMode())
                selectedKeyboard++;

            if (selectedKeyboard >= keyboardsCount)
                selectedKeyboard = 0;

            return mAlphabetKeyboardsCreators[selectedKeyboard].getName();
        }
    }

    private AnyKeyboard nextAlphabetKeyboard(EditorInfo currentEditorInfo, boolean supportsPhysical) {
        AnyKeyboard current = getLockedKeyboard(currentEditorInfo);

        if (current == null) {
            final int keyboardsCount = getAlphabetKeyboards().length;
            if (isAlphabetMode())
                mLastSelectedKeyboardIndex++;

            mAlphabetMode = true;

            if (mLastSelectedKeyboardIndex >= keyboardsCount)
                mLastSelectedKeyboardIndex = 0;

            current = getAlphabetKeyboard(mLastSelectedKeyboardIndex, currentEditorInfo);
            // returning to the regular symbols keyboard, no matter what
            mLastSelectedSymbolsKeyboard = 0;

            if (supportsPhysical) {
                int testsLeft = keyboardsCount;
                while (!(current instanceof HardKeyboardTranslator)
                        && (testsLeft > 0)) {
                    mLastSelectedKeyboardIndex++;
                    if (mLastSelectedKeyboardIndex >= keyboardsCount)
                        mLastSelectedKeyboardIndex = 0;
                    current = getAlphabetKeyboard(mLastSelectedKeyboardIndex, currentEditorInfo);
                    testsLeft--;
                }
                // if we scanned all keyboards... we screwed...
                if (testsLeft == 0) {
                    Logger.w(TAG, "Could not locate the next physical keyboard. Will continue with " + current.getKeyboardName());
                }
            }

            current.setImeOptions(mContext.getResources(), currentEditorInfo);
            mKeyboardSwitchedListener.onAlphabetKeyboardSet(current);
            return current;
        } else {
            return current;
        }
    }

    @NonNull
    private AnyKeyboard nextSymbolsKeyboard(EditorInfo currentEditorInfo) {
        AnyKeyboard locked = getLockedKeyboard(currentEditorInfo);
        if (locked != null) return locked;

        mLastSelectedSymbolsKeyboard = getNextSymbolsKeyboardIndex();
        mAlphabetMode = false;
        AnyKeyboard current = getSymbolsKeyboard(mLastSelectedSymbolsKeyboard);
        current.setImeOptions(mContext.getResources(), currentEditorInfo);
        mKeyboardSwitchedListener.onSymbolsKeyboardSet(current);
        return current;
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

    public String getCurrentKeyboardSentenceSeparators() {
        if (isAlphabetMode()) {
            ensureKeyboardsAreBuilt();
            if (mLastSelectedKeyboardIndex < mAlphabetKeyboardsCreators.length) {
                return mAlphabetKeyboardsCreators[mLastSelectedKeyboardIndex].getSentenceSeparators();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private AnyKeyboard getCurrentKeyboard() {
        if (isAlphabetMode()) {
            return getAlphabetKeyboard(mLastSelectedKeyboardIndex, mLastEditorInfo);
        } else {
            return getSymbolsKeyboard(mLastSelectedSymbolsKeyboard);
        }
    }

    @NonNull
    private synchronized AnyKeyboard getAlphabetKeyboard(int index, @Nullable EditorInfo editorInfo) {
        AnyKeyboard[] keyboards = getAlphabetKeyboards();
        if (index >= keyboards.length)
            index = 0;

        AnyKeyboard keyboard = keyboards[index];

        final int mode = getKeyboardMode(editorInfo);
        if (keyboard == null || keyboard.getKeyboardMode() != mode) {
            KeyboardAddOnAndBuilder creator = mAlphabetKeyboardsCreators[index];
            if ((keyboard = keyboards[index] = createKeyboardFromCreator(mode, creator)) == null) {
                //this is bad... Maybe the keyboard plugin was uninstalled and we did not detect.
                flushKeyboardsCache();
                index = 0;//we always have the built-in English keyboard
                return getAlphabetKeyboard(index, editorInfo);
            } else {
                keyboard.loadKeyboard((mInputView != null) ? mInputView.getThemedKeyboardDimens() : mKeyboardDimens);
            }
        }
        if (editorInfo != null && !TextUtils.isEmpty(editorInfo.packageName)) {
            mAlphabetKeyboardIndexByPackageId.put(editorInfo.packageName, keyboard.getKeyboardAddOn().getId());
        }
        return keyboard;
    }

    protected AnyKeyboard createKeyboardFromCreator(int mode, KeyboardAddOnAndBuilder creator) {
        return creator.createKeyboard(mode);
    }

    @NonNull
    public AnyKeyboard nextKeyboard(EditorInfo currentEditorInfo, NextKeyboardType type) {
        AnyKeyboard locked = getLockedKeyboard(currentEditorInfo);
        if (locked != null) return locked;

        switch (type) {
            case Alphabet:
            case AlphabetSupportsPhysical:
                return nextAlphabetKeyboard(currentEditorInfo, (type == NextKeyboardType.AlphabetSupportsPhysical));
            case Symbols:
                return nextSymbolsKeyboard(currentEditorInfo);
            case Any:
            case PreviousAny:
                // currently we'll support only one direction cycling through the alphabet, and at the end, going to the symbols.
                final int alphabetKeyboardsCount = getAlphabetKeyboards().length;
                if (mAlphabetMode) {
                    if (mLastSelectedKeyboardIndex >= (alphabetKeyboardsCount - 1)) {
                        // we are at the last alphabet keyboard
                        mLastSelectedKeyboardIndex = 0;
                        return nextSymbolsKeyboard(currentEditorInfo);
                    } else {
                        return nextAlphabetKeyboard(currentEditorInfo, false);
                    }
                } else {
                    if (mLastSelectedSymbolsKeyboard >= SYMBOLS_KEYBOARD_LAST_CYCLE_INDEX) {
                        // we are at the last symbols keyboard
                        mLastSelectedSymbolsKeyboard = 0;
                        return nextAlphabetKeyboard(currentEditorInfo, false);
                    } else {
                        return nextSymbolsKeyboard(currentEditorInfo);
                    }
                }
            case AnyInsideMode:
                if (mAlphabetMode) {
                    // re-calling this function,but with Alphabet
                    return nextKeyboard(currentEditorInfo, NextKeyboardType.Alphabet);
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
                    return nextKeyboard(currentEditorInfo, NextKeyboardType.Alphabet);
                }
            default:
                return nextAlphabetKeyboard(currentEditorInfo, false);
        }
    }

    public AnyKeyboard nextAlterKeyboard(EditorInfo currentEditorInfo) {
        AnyKeyboard locked = getLockedKeyboard(currentEditorInfo);
        if (locked != null) return locked;

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

            currentKeyboard = getSymbolsKeyboard(mLastSelectedSymbolsKeyboard);
            currentKeyboard.setImeOptions(mContext.getResources(), currentEditorInfo);

            mKeyboardSwitchedListener.onSymbolsKeyboardSet(currentKeyboard);
            return currentKeyboard;
        }

        return currentKeyboard;
    }

    public boolean isCurrentKeyboardPhysical() {
        AnyKeyboard current = getCurrentKeyboard();
        return (current != null) && (current instanceof HardKeyboardTranslator);
    }

    public void onLowMemory() {
        for (int index = 0; index < mSymbolsKeyboardsArray.length; index++) {
            //in alphabet mode we remove all symbols
            //in non-alphabet, we'll keep the currently used one
            if ((isAlphabetMode() || (mLastSelectedSymbolsKeyboard != index))) {
                mSymbolsKeyboardsArray[index] = null;
            }
        }

        for (int index = 0; index < mAlphabetKeyboards.length; index++) {
            //keeping currently used alphabet
            if (mLastSelectedKeyboardIndex != index) {
                mAlphabetKeyboards[index] = null;
            }
        }
    }

    public boolean shouldPopupForLanguageSwitch() {
        // only in alphabet mode,
        // and only if there are more than two keyboards
        // and only if user requested to have a popup
        return mAlphabetMode
                && (getAlphabetKeyboards().length > 2)
                && AnyApplication.getConfig().shouldShowPopupForLanguageSwitch();
    }

    public void destroy() {
        AnyApplication.getConfig().removeChangedListener(mPrefChangedListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            storeKeyboardByAppMapping();
        }
        flushKeyboardsCache();
        mAlphabetKeyboardIndexByPackageId.clear();
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private void storeKeyboardByAppMapping() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> mapping = new HashSet<>(mAlphabetKeyboardIndexByPackageId.size());
        for (Map.Entry<String, CharSequence> aMapping : mAlphabetKeyboardIndexByPackageId.entrySet()) {
            mapping.add(String.format(Locale.US, "%s -> %s", aMapping.getKey(), aMapping.getValue()));
        }
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(mContext.getString(R.string.settings_key_persistent_layout_per_package_id_mapping), mapping);
        editor.commit();
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadKeyboardAppMapping() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Set<String> mapping = sharedPreferences.getStringSet(mContext.getString(R.string.settings_key_persistent_layout_per_package_id_mapping), Collections.<String>emptySet());
        for (String aMapping : mapping) {
            String[] mapPair = aMapping.split(PACKAGE_ID_TO_KEYBOARD_ID_TOKEN);
            if (mapPair.length == 2) {
                mAlphabetKeyboardIndexByPackageId.put(mapPair[0], mapPair[1]);
            }
        }
    }

    public enum NextKeyboardType {
        Symbols, Alphabet, AlphabetSupportsPhysical, Any, PreviousAny, AnyInsideMode, OtherMode
    }

    public interface KeyboardSwitchedListener {
        void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard);

        void onSymbolsKeyboardSet(@NonNull AnyKeyboard keyboard);

        void onAvailableKeyboardsChanged(@NonNull List<KeyboardAddOnAndBuilder> builders);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INPUT_MODE_TEXT, INPUT_MODE_SYMBOLS, INPUT_MODE_PHONE, INPUT_MODE_URL, INPUT_MODE_EMAIL, INPUT_MODE_IM, INPUT_MODE_DATETIME, INPUT_MODE_NUMBERS})
    public @interface InputModeId {
    }
}
