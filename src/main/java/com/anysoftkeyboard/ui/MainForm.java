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

package com.anysoftkeyboard.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ViewFlipper;

import com.anysoftkeyboard.ui.settings.MainSettings;
import com.anysoftkeyboard.ui.tutorials.ChangeLogActivity;
import com.anysoftkeyboard.ui.tutorials.TipsActivity;
import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class MainForm extends Activity implements OnClickListener {

    private ViewFlipper mPager;
    private Drawable mSelectedTabBottomDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_fragment);

        mSelectedTabBottomDrawable = getResources().getDrawable(R.drawable.selected_tab);
        mSelectedTabBottomDrawable.setBounds(0, 0, getWindowManager().getDefaultDisplay().getWidth(), getResources().getDimensionPixelOffset(R.dimen.selected_tab_drawable_height));

        mPager = (ViewFlipper) findViewById(R.id.main_pager);

        findViewById(R.id.goto_tips_form).setOnClickListener(this);
        findViewById(R.id.goto_changelog_button).setOnClickListener(this);
        findViewById(R.id.goto_howto_form).setOnClickListener(this);

        CheckBox showTipsNotifications = (CheckBox) findViewById(R.id.show_tips_next_time);
        showTipsNotifications.setChecked(AnyApplication.getConfig().getShowTipsNotification());
        showTipsNotifications.setOnClickListener(this);

        CheckBox showVersionNotifications = (CheckBox) findViewById(R.id.show_notifications_next_time);
        showVersionNotifications.setChecked(AnyApplication.getConfig().getShowVersionNotification());
        showVersionNotifications.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goto_howto_form:
                Intent i = new Intent(getApplicationContext(), WelcomeHowToNoticeActivity.class);
                startActivity(i);
                break;
            case R.id.goto_tips_form:
                Intent tipActivity = new Intent(getApplicationContext(), TipsActivity.class);
                tipActivity.putExtra(TipsActivity.EXTRA_SHOW_ALL_TIPS, true);
                startActivity(tipActivity);
                break;
            case R.id.goto_changelog_button:
                Intent changelog = new Intent(this, ChangeLogActivity.class);
                changelog.putExtra(ChangeLogActivity.EXTRA_SHOW_ALL_LOGS, true);
                startActivity(changelog);
                break;
            case R.id.show_tips_next_time:
                AnyApplication.getConfig().setShowTipsNotification(!AnyApplication.getConfig().getShowTipsNotification());
                break;
            case R.id.show_notifications_next_time:
                AnyApplication.getConfig().setShowVersionNotification(!AnyApplication.getConfig().getShowVersionNotification());
                break;
        }
    }

    public static void searchMarketForAddons(Context applicationContext, String additionalQueryString) throws android.content.ActivityNotFoundException {
        Intent search = new Intent(Intent.ACTION_VIEW);
        search.setData(Uri.parse("market://search?q=AnySoftKeyboard" + additionalQueryString));
        search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        applicationContext.startActivity(search);
    }

    public static void startSettings(Context applicationContext) {
        Intent intent = new Intent(applicationContext, MainSettings.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        applicationContext.startActivity(intent);
    }
}
