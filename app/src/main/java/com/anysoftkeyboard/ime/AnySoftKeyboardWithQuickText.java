package com.anysoftkeyboard.ime;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextPagerView;
import com.anysoftkeyboard.quicktextkeys.ui.QuickTextViewFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardWithQuickText extends AnySoftKeyboardHardware {

    private boolean mDoNotFlipQuickTextKeyAndPopupFunctionality;
    private String mOverrideQuickTextText = "";

    @Override
    public void onCreate() {
        super.onCreate();
        addDisposable(prefs().getBoolean(R.string.settings_key_do_not_flip_quick_key_codes_functionality, R.bool.settings_default_do_not_flip_quick_keys_functionality)
                .asObservable().subscribe(value -> mDoNotFlipQuickTextKeyAndPopupFunctionality = value));

        addDisposable(prefs().getString(R.string.settings_key_emoticon_default_text, R.string.settings_default_empty)
                .asObservable().subscribe(value -> mOverrideQuickTextText = value));
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
        if (TextUtils.isEmpty(mOverrideQuickTextText))
            onText(key, quickTextKey.getKeyOutputText());
        else
            onText(key, mOverrideQuickTextText);
    }

    private void switchToQuickTextKeyboard() {
        abortCorrectionAndResetPredictionState(false);
        setCandidatesViewShown(false);

        cleanUpQuickTextKeyboard(false);

        final AnyKeyboardView actualInputView = (AnyKeyboardView) getInputView();
        final int height = actualInputView.getHeight();
        actualInputView.setVisibility(View.GONE);
        QuickTextPagerView quickTextsLayout = QuickTextViewFactory.createQuickTextView(getApplicationContext(), getInputViewContainer(), height, getQuickKeyHistoryRecords());
        actualInputView.closing();
        quickTextsLayout.setThemeValues(actualInputView.getLabelTextSize(), actualInputView.getKeyTextColor(),
                actualInputView.getDrawableForKeyCode(KeyCodes.CANCEL), actualInputView.getDrawableForKeyCode(KeyCodes.DELETE), actualInputView.getDrawableForKeyCode(KeyCodes.SETTINGS),
                actualInputView.getBackground());

        getInputViewContainer().addView(quickTextsLayout);
    }

    private boolean cleanUpQuickTextKeyboard(boolean reshowStandardKeyboard) {
        final ViewGroup inputViewContainer = getInputViewContainer();
        if (inputViewContainer == null) return false;

        QuickTextPagerView quickTextsLayout = inputViewContainer.findViewById(R.id.quick_text_pager_root);
        if (quickTextsLayout != null) {
            inputViewContainer.removeView(quickTextsLayout);
            if (reshowStandardKeyboard) {
                View standardKeyboardView = (View) getInputView();
                standardKeyboardView.setVisibility(View.VISIBLE);
            }
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
