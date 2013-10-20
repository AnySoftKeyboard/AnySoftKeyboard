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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

public class MainSettingsFragment extends PreferenceActivity {

    private static final String TAG = "ASK_PREFS";

    private static final int DIALOG_WELCOME = 1;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs);
    }

    private void setAddOnGroupSummary(String preferenceGroupKey, AddOn addOn) {
        String summary = getResources().getString(R.string.selected_add_on_summary, addOn.getName());
        findPreference(preferenceGroupKey).setSummary(summary);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //I wont to help the user configure the keyboard
        if (WelcomeHowToNoticeActivity.shouldShowWelcomeActivity(getApplicationContext())) {
            //this is the first time the application is loaded.
            Log.i(TAG, "Welcome should be shown");
            showDialog(DIALOG_WELCOME);
        }

        setAddOnGroupSummary("keyboard_theme_group",
                KeyboardThemeFactory.getCurrentKeyboardTheme(getApplicationContext()));
        setAddOnGroupSummary("quick_text_keys_group",
                QuickTextKeyFactory.getCurrentQuickTextKey(getApplicationContext()));
        setAddOnGroupSummary("top_generic_row_group",
                KeyboardExtensionFactory.getCurrentKeyboardExtension(getApplicationContext(), KeyboardExtension.TYPE_TOP));
        setAddOnGroupSummary("bottom_generic_row_group",
                KeyboardExtensionFactory.getCurrentKeyboardExtension(getApplicationContext(), KeyboardExtension.TYPE_BOTTOM));
        setAddOnGroupSummary("extension_keyboard_group",
                KeyboardExtensionFactory.getCurrentKeyboardExtension(getApplicationContext(), KeyboardExtension.TYPE_EXTENSION));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_WELCOME) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_launcher)
                    .setTitle(R.string.how_to_enable_dialog_title)
                    .setMessage(R.string.how_to_enable_dialog_text)
                    .setPositiveButton(R.string.how_to_enable_dialog_show_me,
                            new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent i = new Intent(getApplicationContext(), WelcomeHowToNoticeActivity.class);
                                    startActivity(i);
                                }
                            })
                    .setNegativeButton(R.string.how_to_enable_dialog_dont_show_me, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();

            return dialog;
        } else {
            return super.onCreateDialog(id);
        }
    }


}
