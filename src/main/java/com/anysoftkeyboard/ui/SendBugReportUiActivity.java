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
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;


public class SendBugReportUiActivity extends Activity {
    private static final String TAG = "ASK_BUG_SENDER";

    public static final String CRASH_REPORT_TEXT = "CRASH_REPORT_TEXT";
    public static final String CRASH_TYPE_STRING = "CRASH_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_crash_log_ui);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //this is a "singleInstance" activity, so we may get a "newIntent" call, with new crash data. I'll store the new intent.
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView crashType = (TextView) findViewById(R.id.ime_crash_type);
        Intent callingIntent = getIntent();
        String type = callingIntent.getStringExtra(CRASH_TYPE_STRING);
        if (TextUtils.isEmpty(type) || (!BuildConfig.DEBUG)/*not showing the type of crash in RELEASE mode*/) {
            crashType.setVisibility(View.GONE);
        } else {
            crashType.setText(type);
        }
    }

    public void onCancelCrashReport(View v) {
        finish();
    }

    public void onSendCrashReport(View v) {
        String[] recipients = new String[]{BuildConfig.CRASH_REPORT_EMAIL_ADDRESS};

        Intent callingIntent = getIntent();

        Intent sendMail = new Intent();
        sendMail.setAction(Intent.ACTION_SEND);
        sendMail.setType("plain/text");
        sendMail.putExtra(Intent.EXTRA_EMAIL, recipients);
        sendMail.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.ime_crashed_title));
        sendMail.putExtra(Intent.EXTRA_TEXT, callingIntent.getStringExtra(CRASH_REPORT_TEXT));

        try {
            Intent sender = Intent.createChooser(sendMail, getString(R.string.ime_crashed_intent_selector_title));
            sender.putExtra(Intent.EXTRA_EMAIL, sendMail.getStringExtra(Intent.EXTRA_EMAIL));
            sender.putExtra(Intent.EXTRA_SUBJECT, sendMail.getStringExtra(Intent.EXTRA_SUBJECT));
            sender.putExtra(Intent.EXTRA_TEXT, callingIntent.getStringExtra(CRASH_REPORT_TEXT));

            Log.i(TAG, "Will send crash report using " + sender);
            startActivity(sender);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "Unable to send bug report via e-mail!", Toast.LENGTH_LONG).show();
        }

        finish();
    }
}
