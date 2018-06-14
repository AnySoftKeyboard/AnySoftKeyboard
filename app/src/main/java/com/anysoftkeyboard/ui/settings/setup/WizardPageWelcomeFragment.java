package com.anysoftkeyboard.ui.settings.setup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.SharedPreferencesCompat;
import android.view.View;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;
import java.util.Random;

public class WizardPageWelcomeFragment extends WizardPageBaseFragment implements View.OnClickListener {
    private static final String STARTED_PREF_KEY = "setup_wizard_STARTED_SETUP_PREF_KEY";
    public static final int DELAY_MILLIS_BEFORE_RESETTING_KEYBOARD = 1000;

    private DemoAnyKeyboardView mDemoAnyKeyboardView;

    private Runnable mPerformDemoKeyboardChange;

    @Override
    protected int getPageLayoutId() {
        return R.layout.keyboard_setup_wizard_page_welcome_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.go_to_start_setup).setOnClickListener(this);
        view.findViewById(R.id.setup_wizard_welcome_privacy_action).setOnClickListener(this);

        mDemoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view);
    }

    @Override
    protected boolean isStepCompleted(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(STARTED_PREF_KEY, false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_to_start_setup:
                final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putBoolean(STARTED_PREF_KEY, true);
                SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
                refreshWizardPager();
                break;
            case R.id.setup_wizard_welcome_privacy_action:
                String privacyUrl = getString(R.string.privacy_policy);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)));
                break;
            default:
                throw new IllegalArgumentException("Failed to handle " + v.getId() + " in WizardPageDoneAndMoreSettingsFragment");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mPerformDemoKeyboardChange = new ChangeDemoKeyboardRunnable(getContext(), mDemoAnyKeyboardView);
        mPerformDemoKeyboardChange.run();
        SetupSupport.popupViewAnimationWithIds(getView(), R.id.go_to_start_setup);
    }

    @Override
    public void onStop() {
        super.onStop();
        mDemoAnyKeyboardView.removeCallbacks(mPerformDemoKeyboardChange);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDemoAnyKeyboardView.onViewNotRequired();
    }

    private static class ChangeDemoKeyboardRunnable implements Runnable {

        private final Random mRandom = new Random();

        private final Context mContext;
        private final DemoAnyKeyboardView mDemoAnyKeyboardView;

        private KeyboardAddOnAndBuilder mKeyboardBuilder;

        public ChangeDemoKeyboardRunnable(Context context, DemoAnyKeyboardView demoAnyKeyboardView) {
            mContext = context;
            mDemoAnyKeyboardView = demoAnyKeyboardView;
            mKeyboardBuilder = AnyApplication.getKeyboardFactory(mContext).getEnabledAddOn();
        }

        @Override
        public void run() {
            mDemoAnyKeyboardView.setKeyboardTheme(getRandomAddOn(AnyApplication.getKeyboardThemeFactory(mContext)));

            KeyboardExtension bottomRow = getRandomAddOn(AnyApplication.getBottomRowFactory(mContext));
            KeyboardExtension topRow = getRandomAddOn(AnyApplication.getTopRowFactory(mContext));

            AnyKeyboard keyboard = mKeyboardBuilder.createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
            keyboard.loadKeyboard(mDemoAnyKeyboardView.getThemedKeyboardDimens(), topRow, bottomRow);
            mDemoAnyKeyboardView.setKeyboard(keyboard, null, null);

            mDemoAnyKeyboardView.postDelayed(this, DELAY_MILLIS_BEFORE_RESETTING_KEYBOARD);
        }

        private <T extends AddOn> T getRandomAddOn(AddOnsFactory<T> factory) {
            List<T> list = factory.getAllAddOns();
            return list.get(mRandom.nextInt(list.size()));
        }
    }
}
