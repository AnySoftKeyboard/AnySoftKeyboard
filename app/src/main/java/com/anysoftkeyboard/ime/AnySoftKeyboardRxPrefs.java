package com.anysoftkeyboard.ime;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.utils.LocaleTools;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class AnySoftKeyboardRxPrefs extends AnySoftKeyboardBase {

    private RxSharedPrefs mRxPrefs;
    private final CompositeDisposable mDisposables = new CompositeDisposable();
    private SharedPreferences mSharedPrefsNotToUse;
    private final SharedPreferences.OnSharedPreferenceChangeListener mGeneralShardPrefChangedListener = (sharedPreferences, key) -> onSharedPreferenceChange(key);

    protected boolean mPrefsAutoSpace;
    protected boolean mHideKeyboardWhenPhysicalKeyboardUsed;
    protected boolean mUseFullScreenInputInLandscape;
    protected boolean mUseFullScreenInputInPortrait;
    protected boolean mSwitchKeyboardOnSpace;
    protected boolean mIsDoubleSpaceChangesToPeriod;
    protected int mMultiTapTimeout;
    protected int mLongPressTimeout;
    protected boolean mSwapPunctuationAndSpace;
    private KeyboardTheme mCurrentKeyboardTheme;

    @Override
    public void onCreate() {
        super.onCreate();
        mRxPrefs = AnyApplication.prefs(this);

        mSharedPrefsNotToUse = PreferenceManager.getDefaultSharedPreferences(this);

        mSharedPrefsNotToUse.registerOnSharedPreferenceChangeListener(mGeneralShardPrefChangedListener);

        addDisposable(mRxPrefs.getString(R.string.settings_key_force_locale, R.string.settings_default_force_locale_setting)
                .asObservable().subscribe(forceLocaleValue -> LocaleTools.applyLocaleToContext(getApplicationContext(), forceLocaleValue)));
        addDisposable(mRxPrefs.getBoolean(R.string.settings_key_auto_space, R.bool.settings_default_auto_space)
                .asObservable().subscribe(value -> mPrefsAutoSpace = value));
        addDisposable(mRxPrefs.getBoolean(R.string.settings_key_hide_soft_when_physical, R.bool.settings_default_hide_soft_when_physical)
                .asObservable().subscribe(value -> mHideKeyboardWhenPhysicalKeyboardUsed = value));
        addDisposable(mRxPrefs.getBoolean(R.string.settings_key_landscape_fullscreen, R.bool.settings_default_landscape_fullscreen)
                .asObservable().subscribe(value -> mUseFullScreenInputInLandscape = value));
        addDisposable(mRxPrefs.getBoolean(R.string.settings_key_portrait_fullscreen, R.bool.settings_default_portrait_fullscreen)
                .asObservable().subscribe(value -> mUseFullScreenInputInPortrait = value));
        addDisposable(mRxPrefs.getBoolean(R.string.settings_key_switch_keyboard_on_space, R.bool.settings_default_switch_to_alphabet_on_space)
                .asObservable().subscribe(value -> mSwitchKeyboardOnSpace = value));
        addDisposable(mRxPrefs.getBoolean(R.string.settings_key_double_space_to_period, R.bool.settings_default_double_space_to_period)
                .asObservable().subscribe(value -> mIsDoubleSpaceChangesToPeriod = value));
        addDisposable(mRxPrefs.getString(R.string.settings_key_multitap_timeout, R.string.settings_default_multitap_timeout)
                .asObservable().map(Integer::parseInt).subscribe(value -> mMultiTapTimeout = value));
        addDisposable(mRxPrefs.getBoolean(R.string.settings_key_bool_should_swap_punctuation_and_space, R.bool.settings_default_bool_should_swap_punctuation_and_space)
                .asObservable().subscribe(value -> mSwapPunctuationAndSpace = value));
        addDisposable(mRxPrefs.getString(R.string.settings_key_long_press_timeout, R.string.settings_default_long_press_timeout)
                .asObservable().map(Integer::parseInt).subscribe(value -> mLongPressTimeout = value));
        addDisposable(KeyboardThemeFactory.observeCurrentTheme(getApplicationContext())
                .subscribe(this::onKeyboardThemeChanged));
    }

    @CallSuper
    protected void onKeyboardThemeChanged(@NonNull KeyboardTheme theme) {
        mCurrentKeyboardTheme = theme;
    }

    protected KeyboardTheme getCurrentKeyboardTheme() {
        return mCurrentKeyboardTheme;
    }

    @NonNull
    protected RxSharedPrefs prefs() {
        return mRxPrefs;
    }

    @CallSuper
    protected void onSharedPreferenceChange(String key) {
    }

    protected void addDisposable(@NonNull Disposable disposable) {
        mDisposables.add(disposable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposables.dispose();
        mSharedPrefsNotToUse.unregisterOnSharedPreferenceChangeListener(mGeneralShardPrefChangedListener);
    }
}
