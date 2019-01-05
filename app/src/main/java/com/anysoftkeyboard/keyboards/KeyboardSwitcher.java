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

import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_EMAIL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_IM;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_URL;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
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
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.reactivex.disposables.CompositeDisposable;

public class KeyboardSwitcher {

    public static final int INPUT_MODE_TEXT = 1;
    public static final int INPUT_MODE_SYMBOLS = 2;
    public static final int INPUT_MODE_PHONE = 3;
    public static final int INPUT_MODE_URL = 4;
    public static final int INPUT_MODE_EMAIL = 5;
    public static final int INPUT_MODE_IM = 6;
    public static final int INPUT_MODE_DATETIME = 7;
    public static final int INPUT_MODE_NUMBERS = 8;
    private final CompositeDisposable mDisposable = new CompositeDisposable();
    private boolean mUse16KeysSymbolsKeyboards;
    private boolean mPersistLayoutForPackageId;
    private boolean mCycleOverAllSymbols;
    private boolean mShowPopupForLanguageSwitch;


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INPUT_MODE_TEXT, INPUT_MODE_SYMBOLS, INPUT_MODE_PHONE, INPUT_MODE_URL, INPUT_MODE_EMAIL, INPUT_MODE_IM, INPUT_MODE_DATETIME, INPUT_MODE_NUMBERS})
    protected @interface InputModeId {
    }

    private static final String PACKAGE_ID_TO_KEYBOARD_ID_TOKEN = "\\s+->\\s+";
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
    @Nullable
    private InputViewBinder mInputView;
    @Keyboard.KeyboardRowModeId
    private int mKeyboardRowMode;
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
    private String mInternetInputLayoutId;
    private int mInternetInputLayoutIndex;
    /**
     * This field will be used to map between requested mode, and enabled mode.
     */
    @Keyboard.KeyboardRowModeId
    private final int[] mRowModesMapping = new int[]{
            Keyboard.KEYBOARD_ROW_MODE_NONE,
            Keyboard.KEYBOARD_ROW_MODE_NORMAL,
            Keyboard.KEYBOARD_ROW_MODE_IM,
            Keyboard.KEYBOARD_ROW_MODE_URL,
            Keyboard.KEYBOARD_ROW_MODE_EMAIL,
            Keyboard.KEYBOARD_ROW_MODE_PASSWORD
    };

    public KeyboardSwitcher(@NonNull KeyboardSwitchedListener ime, @NonNull Context context) {
        mDefaultAddOn = new DefaultAddOn(context, context);
        mKeyboardSwitchedListener = ime;
        mContext = context;
        final Resources res = mContext.getResources();
        mKeyboardDimens = new KeyboardDimens() {

            @Override
            public int getSmallKeyHeight() {
                return res.getDimensionPixelOffset(R.dimen.default_key_half_height);
            }

            @Override
            public float getRowVerticalGap() {
                return res.getDimensionPixelOffset(R.dimen.default_key_vertical_gap);
            }

            @Override
            public int getNormalKeyHeight() {
                return res.getDimensionPixelOffset(R.dimen.default_key_height);
            }

            @Override
            public int getLargeKeyHeight() {
                return res.getDimensionPixelOffset(R.dimen.default_key_tall_height);
            }

            @Override
            public int getKeyboardMaxWidth() {
                return mContext.getResources().getDisplayMetrics().widthPixels;
            }

            @Override
            public float getKeyHorizontalGap() {
                return res.getDimensionPixelOffset(R.dimen.default_key_horizontal_gap);
            }
        };
        mKeyboardRowMode = KEYBOARD_ROW_MODE_NORMAL;
        //loading saved package-id from prefs
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            loadKeyboardAppMapping();
        }

        final RxSharedPrefs prefs = AnyApplication.prefs(mContext);
        mDisposable.add(prefs.getString(R.string.settings_key_layout_for_internet_fields, R.string.settings_default_keyboard_id)
                .asObservable().subscribe(keyboardId -> {
                    mInternetInputLayoutId = keyboardId;
                    mInternetInputLayoutIndex = findIndexOfInternetInputLayout();
                }));
        mDisposable.add(prefs.getBoolean(R.string.settings_key_support_keyboard_type_state_row_type_2, R.bool.settings_default_true)
                .asObservable().subscribe(enabled -> mRowModesMapping[Keyboard.KEYBOARD_ROW_MODE_IM] = enabled ? Keyboard.KEYBOARD_ROW_MODE_IM : Keyboard.KEYBOARD_ROW_MODE_NORMAL));
        mDisposable.add(prefs.getBoolean(R.string.settings_key_support_keyboard_type_state_row_type_3, R.bool.settings_default_true)
                .asObservable().subscribe(enabled -> mRowModesMapping[Keyboard.KEYBOARD_ROW_MODE_URL] = enabled ? Keyboard.KEYBOARD_ROW_MODE_URL : Keyboard.KEYBOARD_ROW_MODE_NORMAL));
        mDisposable.add(prefs.getBoolean(R.string.settings_key_support_keyboard_type_state_row_type_4, R.bool.settings_default_true)
                .asObservable().subscribe(enabled -> mRowModesMapping[Keyboard.KEYBOARD_ROW_MODE_EMAIL] = enabled ? Keyboard.KEYBOARD_ROW_MODE_EMAIL : Keyboard.KEYBOARD_ROW_MODE_NORMAL));
        mDisposable.add(prefs.getBoolean(R.string.settings_key_support_keyboard_type_state_row_type_5, R.bool.settings_default_true)
                .asObservable().subscribe(enabled -> mRowModesMapping[Keyboard.KEYBOARD_ROW_MODE_PASSWORD] = enabled ? Keyboard.KEYBOARD_ROW_MODE_PASSWORD : Keyboard.KEYBOARD_ROW_MODE_NORMAL));
        mDisposable.add(prefs.getBoolean(R.string.settings_key_use_16_keys_symbols_keyboards, R.bool.settings_default_use_16_keys_symbols_keyboards)
                .asObservable().subscribe(enabled -> mUse16KeysSymbolsKeyboards = enabled));
        mDisposable.add(prefs.getBoolean(R.string.settings_key_persistent_layout_per_package_id, R.bool.settings_default_persistent_layout_per_package_id)
                .asObservable().subscribe(enabled -> mPersistLayoutForPackageId = enabled));
        mDisposable.add(prefs.getBoolean(R.string.settings_key_cycle_all_symbols, R.bool.settings_default_cycle_all_symbols)
                .asObservable().subscribe(enabled -> mCycleOverAllSymbols = enabled));
        mDisposable.add(prefs.getBoolean(R.string.settings_key_lang_key_shows_popup, R.bool.settings_default_lang_key_shows_popup)
                .asObservable().subscribe(enabled -> mShowPopupForLanguageSwitch = enabled));
    }

    @Keyboard.KeyboardRowModeId
    private int getKeyboardMode(EditorInfo attr) {
        if (attr == null) return KEYBOARD_ROW_MODE_NORMAL;

        int variation = attr.inputType & EditorInfo.TYPE_MASK_VARIATION;

        switch (variation) {
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                return returnModeIfEnabled(KEYBOARD_ROW_MODE_EMAIL);
            case EditorInfo.TYPE_TEXT_VARIATION_URI:
                return returnModeIfEnabled(KEYBOARD_ROW_MODE_URL);
            case EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
            case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_SUBJECT:
            case EditorInfo.TYPE_TEXT_VARIATION_LONG_MESSAGE:
                return returnModeIfEnabled(KEYBOARD_ROW_MODE_IM);
            case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
            case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                return returnModeIfEnabled(Keyboard.KEYBOARD_ROW_MODE_PASSWORD);
            default:
                return KEYBOARD_ROW_MODE_NORMAL;
        }
    }

    @Keyboard.KeyboardRowModeId
    private int returnModeIfEnabled(@Keyboard.KeyboardRowModeId int modeId) {
        return mRowModesMapping[modeId];
    }

    public void setInputView(@NonNull InputViewBinder inputView) {
        mInputView = inputView;
        flushKeyboardsCache();
    }

    @NonNull
    private AnyKeyboard getSymbolsKeyboard(int keyboardIndex) {
        ensureKeyboardsAreBuilt();
        AnyKeyboard keyboard = mSymbolsKeyboardsArray[keyboardIndex];

        if (keyboard == null || keyboard.getKeyboardMode() != mKeyboardRowMode) {
            switch (keyboardIndex) {
                case SYMBOLS_KEYBOARD_REGULAR_INDEX:
                    if (mUse16KeysSymbolsKeyboards) {
                        keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols_16keys, R.xml.symbols, mContext.getString(R.string.symbols_keyboard), "symbols_keyboard",
                                mKeyboardRowMode);
                    } else {
                        keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols, R.xml.symbols, mContext.getString(R.string.symbols_keyboard), "symbols_keyboard", mKeyboardRowMode);
                    }
                    break;
                case SYMBOLS_KEYBOARD_ALT_INDEX:
                    if (mUse16KeysSymbolsKeyboards) {
                        keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols_alt_16keys, R.xml.symbols_alt, mContext.getString(R.string.symbols_alt_keyboard),
                                "alt_symbols_keyboard", mKeyboardRowMode);
                    } else {
                        keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.symbols_alt, R.xml.symbols_alt, mContext.getString(R.string.symbols_alt_keyboard), "alt_symbols_keyboard",
                                mKeyboardRowMode);
                    }
                    break;
                case SYMBOLS_KEYBOARD_ALT_NUMBERS_INDEX:
                    keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.simple_alt_numbers, R.xml.simple_alt_numbers, mContext.getString(R.string.symbols_alt_num_keyboard),
                            "alt_numbers_symbols_keyboard", mKeyboardRowMode);
                    break;
                case SYMBOLS_KEYBOARD_PHONE_INDEX:
                    keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.simple_phone, R.xml.simple_phone, mContext.getString(R.string.symbols_phone_keyboard), "phone_symbols_keyboard",
                            mKeyboardRowMode);
                    break;
                case SYMBOLS_KEYBOARD_NUMBERS_INDEX:
                    keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.simple_numbers, R.xml.simple_numbers, mContext.getString(R.string.symbols_numbers_keyboard),
                            "numbers_symbols_keyboard", mKeyboardRowMode);
                    break;
                case SYMBOLS_KEYBOARD_DATETIME_INDEX:
                    keyboard = createGenericKeyboard(mDefaultAddOn, mContext, R.xml.simple_datetime, R.xml.simple_datetime, mContext.getString(R.string.symbols_time_keyboard),
                            "datetime_symbols_keyboard", mKeyboardRowMode);
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

    protected GenericKeyboard createGenericKeyboard(AddOn addOn, Context context, int layoutResId, int landscapeLayoutResId, String name, String keyboardId, int mode) {
        return new GenericKeyboard(addOn, context, layoutResId, landscapeLayoutResId, name, keyboardId, mode);
    }

    private AnyKeyboard[] getAlphabetKeyboards() {
        ensureKeyboardsAreBuilt();
        return mAlphabetKeyboards;
    }

    @NonNull
    public List<KeyboardAddOnAndBuilder> getEnabledKeyboardsBuilders() {
        ensureKeyboardsAreBuilt();
        return Arrays.asList(mAlphabetKeyboardsCreators);
    }

    public void flushKeyboardsCache() {
        mAlphabetKeyboards = EMPTY_AnyKeyboards;
        mSymbolsKeyboardsArray = EMPTY_AnyKeyboards;
        mAlphabetKeyboardsCreators = EMPTY_Creators;
        mInternetInputLayoutIndex = -1;
        mLastEditorInfo = null;
    }

    private void ensureKeyboardsAreBuilt() {
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
                if (mLastSelectedSymbolsKeyboard >= mSymbolsKeyboardsArray.length) {
                    mLastSelectedSymbolsKeyboard = 0;
                }
            }
        }
    }

    private int findIndexOfInternetInputLayout() {
        for (int index = 0; index < mAlphabetKeyboardsCreators.length; index++) {
            final KeyboardAddOnAndBuilder builder = mAlphabetKeyboardsCreators[index];
            if (TextUtils.equals(builder.getId(), mInternetInputLayoutId)) {
                return index;
            }
        }

        return -1;
    }

    public void setKeyboardMode(@InputModeId final int inputModeId, final EditorInfo attr, final boolean restarting) {
        ensureKeyboardsAreBuilt();
        final boolean keyboardGlobalModeChanged = attr.inputType != (mLastEditorInfo == null ? 0 : mLastEditorInfo.inputType);
        mLastEditorInfo = attr;
        mKeyboardRowMode = getKeyboardMode(attr);
        boolean resubmitToView = true;
        AnyKeyboard keyboard;

        switch (inputModeId) {
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
                if ((!restarting && mInternetInputLayoutIndex >= 0) && (inputModeId == INPUT_MODE_URL || inputModeId == INPUT_MODE_EMAIL)) {
                    // starting with English, but only in non-restarting mode
                    // this is a fix for issue #62
                    mLastSelectedKeyboardIndex = mInternetInputLayoutIndex;
                } else {
                    //trying to re-use last keyboard the user used in this input field.
                    if (mPersistLayoutForPackageId && !TextUtils.isEmpty(attr.packageName) && mAlphabetKeyboardIndexByPackageId.containsKey(attr.packageName)) {
                        final CharSequence reusedKeyboardAddOnId = mAlphabetKeyboardIndexByPackageId.get(attr.packageName);
                        for (int builderIndex = 0; builderIndex < mAlphabetKeyboardsCreators.length; builderIndex++) {
                            KeyboardAddOnAndBuilder builder = mAlphabetKeyboardsCreators[builderIndex];
                            if (TextUtils.equals(builder.getId(), reusedKeyboardAddOnId)) {
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
                if (!restarting || keyboardGlobalModeChanged) {
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

        final List<KeyboardAddOnAndBuilder> enabledKeyboardsBuilders = getEnabledKeyboardsBuilders();
        final int keyboardsCount = enabledKeyboardsBuilders.size();
        for (int keyboardIndex = 0; keyboardIndex < keyboardsCount; keyboardIndex++) {
            if (TextUtils.equals(enabledKeyboardsBuilders.get(keyboardIndex).getId(), keyboardId)) {
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
            if (isAlphabetMode()) {
                selectedKeyboard++;
            }

            if (selectedKeyboard >= keyboardsCount) {
                selectedKeyboard = 0;
            }

            return mAlphabetKeyboardsCreators[selectedKeyboard].getName();
        }
    }

    private AnyKeyboard nextAlphabetKeyboard(EditorInfo currentEditorInfo, boolean supportsPhysical) {
        AnyKeyboard current = getLockedKeyboard(currentEditorInfo);

        if (current == null) {
            final int keyboardsCount = getAlphabetKeyboards().length;
            if (isAlphabetMode()) {
                mLastSelectedKeyboardIndex++;
            }

            mAlphabetMode = true;

            if (mLastSelectedKeyboardIndex >= keyboardsCount) {
                mLastSelectedKeyboardIndex = 0;
            }

            current = getAlphabetKeyboard(mLastSelectedKeyboardIndex, currentEditorInfo);
            // returning to the regular symbols keyboard, no matter what
            mLastSelectedSymbolsKeyboard = 0;

            if (supportsPhysical) {
                int testsLeft = keyboardsCount;
                while (!(current instanceof HardKeyboardTranslator)
                        && (testsLeft > 0)) {
                    mLastSelectedKeyboardIndex++;
                    if (mLastSelectedKeyboardIndex >= keyboardsCount) {
                        mLastSelectedKeyboardIndex = 0;
                    }
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
        if (mCycleOverAllSymbols) {
            if (!isAlphabetMode()) {
                if (nextKeyboardIndex >= SYMBOLS_KEYBOARD_LAST_CYCLE_INDEX) {
                    nextKeyboardIndex = SYMBOLS_KEYBOARD_REGULAR_INDEX;
                } else {
                    nextKeyboardIndex++;
                }
            } else {
                nextKeyboardIndex = SYMBOLS_KEYBOARD_REGULAR_INDEX;
            }
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
    private AnyKeyboard getAlphabetKeyboard(int index, @Nullable EditorInfo editorInfo) {
        AnyKeyboard[] keyboards = getAlphabetKeyboards();
        if (index >= keyboards.length) {
            index = 0;
        }

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
                && mShowPopupForLanguageSwitch;
    }

    public void destroy() {
        mDisposable.dispose();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            storeKeyboardByAppMapping();
        }
        flushKeyboardsCache();
        mAlphabetKeyboardIndexByPackageId.clear();
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private void storeKeyboardByAppMapping() {
        Set<String> mapping = new HashSet<>(mAlphabetKeyboardIndexByPackageId.size());
        for (Map.Entry<String, CharSequence> aMapping : mAlphabetKeyboardIndexByPackageId.entrySet()) {
            mapping.add(String.format(Locale.US, "%s -> %s", aMapping.getKey(), aMapping.getValue()));
        }

        AnyApplication.prefs(mContext).getStringSet(R.string.settings_key_persistent_layout_per_package_id_mapping).set(mapping);
    }

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    private void loadKeyboardAppMapping() {
        Set<String> mapping = AnyApplication.prefs(mContext).getStringSet(R.string.settings_key_persistent_layout_per_package_id_mapping).get();
        for (String aMapping : mapping) {
            String[] mapPair = aMapping.split(PACKAGE_ID_TO_KEYBOARD_ID_TOKEN, -1);
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
}
