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

package com.anysoftkeyboard.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlyDataCreatorForAndroid;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.f2prateek.rx.preferences2.Preference;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class KeyboardThemeSelectorFragment extends AbstractAddOnsBrowserFragment<KeyboardTheme> {

    private TextView mApplySummaryText;
    private Preference<Boolean> mApplyPrefs;
    private DemoAnyKeyboardView mSelectedKeyboardView;
    private OverlayData mOverlayData = new OverlayData();

    public KeyboardThemeSelectorFragment() {
        super(
                "KeyboardThemeSelectorFragment",
                R.string.keyboard_theme_list_title,
                true,
                false,
                true);
    }

    @NonNull
    @Override
    protected AddOnsFactory<KeyboardTheme> getAddOnFactory() {
        return AnyApplication.getKeyboardThemeFactory(getContext());
    }

    @Override
    protected void onTweaksOptionSelected() {
        Activity activity = getActivity();
        if (activity instanceof FragmentChauffeurActivity) {
            FragmentChauffeurActivity chauffeurActivity = (FragmentChauffeurActivity) activity;
            chauffeurActivity.addFragmentToUi(
                    new KeyboardThemeTweaksFragment(),
                    TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSelectedKeyboardView = view.findViewById(R.id.demo_keyboard_view);

        mApplyPrefs =
                AnyApplication.prefs(getContext())
                        .getBoolean(
                                R.string.settings_key_apply_remote_app_colors,
                                R.bool.settings_default_apply_remote_app_colors);
        ViewGroup demoView = view.findViewById(R.id.demo_keyboard_view_background);
        final View applyOverlayView =
                getLayoutInflater()
                        .inflate(R.layout.prefs_adapt_theme_to_remote_app, demoView, false);
        demoView.addView(applyOverlayView);
        mApplySummaryText = applyOverlayView.findViewById(R.id.apply_overlay_summary);
        CheckBox checkBox = applyOverlayView.findViewById(R.id.apply_overlay);
        if (!OverlyDataCreatorForAndroid.OS_SUPPORT_FOR_ACCENT) {
            mApplySummaryText.setVisibility(View.GONE);
            checkBox.setVisibility(View.GONE);
        }
        View demoAppsRoot = applyOverlayView.findViewById(R.id.overlay_demo_apps_root);
        checkBox.setOnCheckedChangeListener(
                (v, isChecked) -> {
                    isChecked &= OverlyDataCreatorForAndroid.OS_SUPPORT_FOR_ACCENT;
                    mApplyPrefs.set(isChecked);
                    mApplySummaryText.setText(
                            isChecked
                                    ? R.string.apply_overlay_summary_on
                                    : R.string.apply_overlay_summary_off);
                    demoAppsRoot.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    if (!isChecked) {
                        mOverlayData = new OverlayData(); /*empty one, to clear overlay*/
                        mSelectedKeyboardView.setThemeOverlay(mOverlayData);
                    }
                });

        checkBox.setChecked(mApplyPrefs.get());

        demoAppsRoot
                .findViewById(R.id.theme_app_demo_phone)
                .setOnClickListener(this::onDemoAppClicked);
        demoAppsRoot
                .findViewById(R.id.theme_app_demo_twitter)
                .setOnClickListener(this::onDemoAppClicked);
        demoAppsRoot
                .findViewById(R.id.theme_app_demo_whatsapp)
                .setOnClickListener(this::onDemoAppClicked);
        demoAppsRoot
                .findViewById(R.id.theme_app_demo_gmail)
                .setOnClickListener(this::onDemoAppClicked);
    }

    private void onDemoAppClicked(View view) {
        final int primaryBackground;
        final int secondaryBackground;
        final int primaryText;
        final int secondaryText;
        switch (view.getId()) {
            case R.id.theme_app_demo_phone:
                primaryBackground = R.color.overlay_demo_app_phone_primary_background;
                secondaryBackground = R.color.overlay_demo_app_phone_secondary_background;
                primaryText = R.color.overlay_demo_app_phone_primary_text;
                secondaryText = R.color.overlay_demo_app_phone_primary_text;
                break;
            case R.id.theme_app_demo_twitter:
                primaryBackground = R.color.overlay_demo_app_twitter_primary_background;
                secondaryBackground = R.color.overlay_demo_app_twitter_secondary_background;
                primaryText = R.color.overlay_demo_app_twitter_primary_text;
                secondaryText = R.color.overlay_demo_app_twitter_primary_text;
                break;
            case R.id.theme_app_demo_whatsapp:
                primaryBackground = R.color.overlay_demo_app_whatsapp_primary_background;
                secondaryBackground = R.color.overlay_demo_app_whatsapp_secondary_background;
                primaryText = R.color.overlay_demo_app_whatsapp_primary_text;
                secondaryText = R.color.overlay_demo_app_whatsapp_primary_text;
                break;
            case R.id.theme_app_demo_gmail:
                primaryBackground = R.color.overlay_demo_app_gmail_primary_background;
                secondaryBackground = R.color.overlay_demo_app_gmail_secondary_background;
                primaryText = R.color.overlay_demo_app_gmail_primary_text;
                secondaryText = R.color.overlay_demo_app_gmail_primary_text;
                break;
            default:
                throw new IllegalArgumentException("Unknown demo app view ID " + view.getId());
        }

        Activity activity = getActivity();
        mOverlayData =
                new OverlayData(
                        ContextCompat.getColor(activity, primaryBackground),
                        ContextCompat.getColor(activity, secondaryBackground),
                        ContextCompat.getColor(activity, primaryText),
                        ContextCompat.getColor(activity, primaryText),
                        ContextCompat.getColor(activity, secondaryText));

        mSelectedKeyboardView.setThemeOverlay(mOverlayData);
    }

    @Override
    protected int getMarketSearchTitle() {
        return R.string.search_market_for_keyboard_addons;
    }

    @Nullable
    @Override
    protected String getMarketSearchKeyword() {
        return "theme";
    }

    @Override
    protected void applyAddOnToDemoKeyboardView(
            @NonNull KeyboardTheme addOn, @NonNull DemoAnyKeyboardView demoKeyboardView) {
        demoKeyboardView.setKeyboardTheme(addOn);
        mSelectedKeyboardView.setThemeOverlay(mOverlayData);
        AnyKeyboard defaultKeyboard =
                AnyApplication.getKeyboardFactory(getContext())
                        .getEnabledAddOn()
                        .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        defaultKeyboard.loadKeyboard(demoKeyboardView.getThemedKeyboardDimens());
        demoKeyboardView.setKeyboard(defaultKeyboard, null, null);
    }
}
