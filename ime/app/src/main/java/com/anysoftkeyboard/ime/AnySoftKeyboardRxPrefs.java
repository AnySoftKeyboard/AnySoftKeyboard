package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.utils.LocaleTools;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class AnySoftKeyboardRxPrefs extends AnySoftKeyboardDialogProvider {

    private RxSharedPrefs mRxPrefs;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private SharedPreferences mSharedPrefsNotToUse;
    private final SharedPreferences.OnSharedPreferenceChangeListener
            mGeneralShardPrefChangedListener =
                    (sharedPreferences, key) -> onSharedPreferenceChange(key);

    protected boolean mPrefsAutoSpace;
    protected boolean mHideKeyboardWhenPhysicalKeyboardUsed;
    protected boolean mUseFullScreenInputInLandscape;
    protected boolean mUseFullScreenInputInPortrait;
    protected boolean mSwitchKeyboardOnSpace;
    protected boolean mIsDoubleSpaceChangesToPeriod;
    protected int mMultiTapTimeout;
    protected int mLongPressTimeout;
    protected boolean mSwapPunctuationAndSpace;

    @Override
    public void onCreate() {
        super.onCreate();
        mRxPrefs = AnyApplication.prefs(this);

        mSharedPrefsNotToUse = PreferenceManager.getDefaultSharedPreferences(this);

        mSharedPrefsNotToUse.registerOnSharedPreferenceChangeListener(
                mGeneralShardPrefChangedListener);

        addDisposable(
                mRxPrefs.getString(
                                R.string.settings_key_force_locale,
                                R.string.settings_default_force_locale_setting)
                        .asObservable()
                        .subscribe(
                                forceLocaleValue ->
                                        LocaleTools.applyLocaleToContext(
                                                getApplicationContext(), forceLocaleValue),
                                GenericOnError.onError("settings_key_force_locale")));
        addDisposable(
                mRxPrefs.getBoolean(
                                R.string.settings_key_auto_space,
                                R.bool.settings_default_auto_space)
                        .asObservable()
                        .subscribe(
                                value -> mPrefsAutoSpace = value,
                                GenericOnError.onError("settings_key_auto_space")));
        addDisposable(
                mRxPrefs.getBoolean(
                                R.string.settings_key_hide_soft_when_physical,
                                R.bool.settings_default_hide_soft_when_physical)
                        .asObservable()
                        .subscribe(
                                value -> mHideKeyboardWhenPhysicalKeyboardUsed = value,
                                GenericOnError.onError("settings_key_hide_soft_when_physical")));
        addDisposable(
                mRxPrefs.getBoolean(
                                R.string.settings_key_landscape_fullscreen,
                                R.bool.settings_default_landscape_fullscreen)
                        .asObservable()
                        .subscribe(
                                value -> mUseFullScreenInputInLandscape = value,
                                GenericOnError.onError("settings_key_landscape_fullscreen")));
        addDisposable(
                mRxPrefs.getBoolean(
                                R.string.settings_key_portrait_fullscreen,
                                R.bool.settings_default_portrait_fullscreen)
                        .asObservable()
                        .subscribe(
                                value -> mUseFullScreenInputInPortrait = value,
                                GenericOnError.onError("settings_key_portrait_fullscreen")));
        addDisposable(
                mRxPrefs.getBoolean(
                                R.string.settings_key_switch_keyboard_on_space,
                                R.bool.settings_default_switch_to_alphabet_on_space)
                        .asObservable()
                        .subscribe(
                                value -> mSwitchKeyboardOnSpace = value,
                                GenericOnError.onError("settings_key_switch_keyboard_on_space")));
        addDisposable(
                mRxPrefs.getBoolean(
                                R.string.settings_key_double_space_to_period,
                                R.bool.settings_default_double_space_to_period)
                        .asObservable()
                        .subscribe(
                                value -> mIsDoubleSpaceChangesToPeriod = value,
                                GenericOnError.onError("settings_key_double_space_to_period")));
        addDisposable(
                mRxPrefs.getString(
                                R.string.settings_key_multitap_timeout,
                                R.string.settings_default_multitap_timeout)
                        .asObservable()
                        .map(Integer::parseInt)
                        .subscribe(
                                value -> mMultiTapTimeout = value,
                                GenericOnError.onError("settings_key_multitap_timeout")));
        addDisposable(
                mRxPrefs.getBoolean(
                                R.string.settings_key_bool_should_swap_punctuation_and_space,
                                R.bool.settings_default_bool_should_swap_punctuation_and_space)
                        .asObservable()
                        .subscribe(
                                value -> mSwapPunctuationAndSpace = value,
                                GenericOnError.onError(
                                        "settings_key_bool_should_swap_punctuation_and_space")));
        addDisposable(
                mRxPrefs.getString(
                                R.string.settings_key_long_press_timeout,
                                R.string.settings_default_long_press_timeout)
                        .asObservable()
                        .map(Integer::parseInt)
                        .subscribe(
                                value -> mLongPressTimeout = value,
                                GenericOnError.onError("settings_key_long_press_timeout")));
    }

    @NonNull
    protected RxSharedPrefs prefs() {
        return mRxPrefs;
    }

    protected void onSharedPreferenceChange(String key) {
        if (key.equals("zoom_factor_keys_in_portrait")
                || key.equals("zoom_factor_keys_in_landscape")
                || key.equals(getString(R.string.settings_key_smiley_icon_on_smileys_key))
                || key.equals(getString(R.string.settings_key_always_hide_language_key))) {
            onAddOnsCriticalChange();
        } else if (key.startsWith(KeyboardFactory.PREF_ID_PREFIX)
                || key.startsWith(QuickTextKeyFactory.PREF_ID_PREFIX)
                || key.startsWith(KeyboardExtensionFactory.EXT_PREF_ID_PREFIX)
                || key.startsWith(KeyboardExtensionFactory.BOTTOM_ROW_PREF_ID_PREFIX)
                || key.startsWith(KeyboardExtensionFactory.TOP_ROW_PREF_ID_PREFIX)) {
            onAddOnsCriticalChange();
        }
    }

    protected void addDisposable(@NonNull Disposable disposable) {
        mDisposables.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposables.dispose();
        mSharedPrefsNotToUse.unregisterOnSharedPreferenceChangeListener(
                mGeneralShardPrefChangedListener);
    }
}
