package com.anysoftkeyboard.ime;

import android.text.TextUtils;
import android.view.View;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.ui.DefaultSkinTonePrefTracker;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextPagerView;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextViewFactory;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardWithQuickText extends AnySoftKeyboardMediaInsertion {

    private boolean mDoNotFlipQuickTextKeyAndPopupFunctionality;
    private String mOverrideQuickTextText = "";
    private DefaultSkinTonePrefTracker mDefaultSkinTonePrefTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        addDisposable(
                prefs().getBoolean(
                                R.string.settings_key_do_not_flip_quick_key_codes_functionality,
                                R.bool.settings_default_do_not_flip_quick_keys_functionality)
                        .asObservable()
                        .subscribe(
                                value -> mDoNotFlipQuickTextKeyAndPopupFunctionality = value,
                                GenericOnError.onError(
                                        "settings_key_do_not_flip_quick_key_codes_functionality")));

        addDisposable(
                prefs().getString(
                                R.string.settings_key_emoticon_default_text,
                                R.string.settings_default_empty)
                        .asObservable()
                        .subscribe(
                                value -> mOverrideQuickTextText = value,
                                GenericOnError.onError("settings_key_emoticon_default_text")));

        mDefaultSkinTonePrefTracker = new DefaultSkinTonePrefTracker(prefs());
        addDisposable(mDefaultSkinTonePrefTracker);
    }

    protected void onQuickTextRequested(Keyboard.Key key) {
        if (mDoNotFlipQuickTextKeyAndPopupFunctionality) {
            outputCurrentQuickTextKey(key);
        } else {
            switchToQuickTextKeyboard();
        }
    }

    protected void onQuickTextKeyboardRequested(Keyboard.Key key) {
        if (mDoNotFlipQuickTextKeyAndPopupFunctionality) {
            switchToQuickTextKeyboard();
        } else {
            outputCurrentQuickTextKey(key);
        }
    }

    private void outputCurrentQuickTextKey(Keyboard.Key key) {
        QuickTextKey quickTextKey = AnyApplication.getQuickTextKeyFactory(this).getEnabledAddOn();
        if (TextUtils.isEmpty(mOverrideQuickTextText)) {
            final CharSequence keyOutputText = quickTextKey.getKeyOutputText();
            onText(key, keyOutputText);
        } else {
            onText(key, mOverrideQuickTextText);
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (finishingInput) {
            cleanUpQuickTextKeyboard(true);
        }
    }

    private void switchToQuickTextKeyboard() {
        final KeyboardViewContainerView inputViewContainer = getInputViewContainer();
        abortCorrectionAndResetPredictionState(false);

        cleanUpQuickTextKeyboard(false);

        final AnyKeyboardView actualInputView = (AnyKeyboardView) getInputView();
        QuickTextPagerView quickTextsLayout =
                QuickTextViewFactory.createQuickTextView(
                        getApplicationContext(),
                        inputViewContainer,
                        getQuickKeyHistoryRecords(),
                        mDefaultSkinTonePrefTracker);
        actualInputView.resetInputView();
        quickTextsLayout.setThemeValues(
                mCurrentTheme,
                actualInputView.getLabelTextSize(),
                actualInputView.getCurrentResourcesHolder().getKeyTextColor(),
                actualInputView.getDrawableForKeyCode(KeyCodes.CANCEL),
                actualInputView.getDrawableForKeyCode(KeyCodes.DELETE),
                actualInputView.getDrawableForKeyCode(KeyCodes.SETTINGS),
                actualInputView.getBackground(),
                actualInputView.getDrawableForKeyCode(KeyCodes.IMAGE_MEDIA_POPUP),
                actualInputView.getDrawableForKeyCode(KeyCodes.DELETE_RECENT_USED_SMILEYS),
                actualInputView.getPaddingBottom(),
                getSupportedMediaTypesForInput());

        actualInputView.setVisibility(View.GONE);
        inputViewContainer.addView(quickTextsLayout);
    }

    private boolean cleanUpQuickTextKeyboard(boolean reshowStandardKeyboard) {
        final KeyboardViewContainerView inputViewContainer = getInputViewContainer();
        if (inputViewContainer == null) return false;

        if (reshowStandardKeyboard) {
            View standardKeyboardView = (View) getInputView();
            if (standardKeyboardView != null) {
                standardKeyboardView.setVisibility(View.VISIBLE);
            }
        }

        QuickTextPagerView quickTextsLayout =
                inputViewContainer.findViewById(R.id.quick_text_pager_root);
        if (quickTextsLayout != null) {
            inputViewContainer.removeView(quickTextsLayout);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean handleCloseRequest() {
        return super.handleCloseRequest() || cleanUpQuickTextKeyboard(true);
    }
}
