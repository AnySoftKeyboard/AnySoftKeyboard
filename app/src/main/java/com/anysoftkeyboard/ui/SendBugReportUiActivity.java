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

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.ui.dev.LogCatViewFragment;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

public class SendBugReportUiActivity extends FragmentActivity {

    public static class BugReportDetails implements Parcelable {
        public final Throwable throwable;
        public final String crashReportText;

        public BugReportDetails(Throwable throwable, String crashReportText) {

            this.throwable = throwable;
            this.crashReportText = crashReportText;
        }

        // Start of Parcel part
        public BugReportDetails(Parcel in) {
            throwable = (Throwable) in.readSerializable();
            crashReportText = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeSerializable(throwable);
            dest.writeString(crashReportText);
        }

        public static final Parcelable.Creator<BugReportDetails> CREATOR =
                new Parcelable.Creator<BugReportDetails>() {
                    @Override
                    public BugReportDetails createFromParcel(Parcel in) {
                        return new BugReportDetails(in);
                    }

                    @Override
                    public BugReportDetails[] newArray(int size) {
                        return new BugReportDetails[size];
                    }
                };

        @Override
        public int describeContents() {
            return 0;
        }
        // End of Parcel part
    }

    private static final String TAG = "ASK_BUG_SENDER";

    public static final String EXTRA_KEY_BugReportDetails = "EXTRA_KEY_BugReportDetails";

    private BugReportDetails mCrashReportDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_crash_log_ui);
    }

    @Override
    protected void onStart() {
        super.onStart();
        TextView crashTypeView = findViewById(R.id.ime_crash_type);
        Intent callingIntent = getIntent();
        mCrashReportDetails = callingIntent.getParcelableExtra(EXTRA_KEY_BugReportDetails);
        if (mCrashReportDetails == null) {
            if (BuildConfig.DEBUG)
                throw new IllegalArgumentException(
                        "Activity started without " + EXTRA_KEY_BugReportDetails + " extra!");
            finish();
        } else {
            if (mCrashReportDetails.throwable == null || !BuildConfig.DEBUG) {
                /*not showing the type of crash in RELEASE mode*/
                crashTypeView.setVisibility(View.GONE);
            } else {
                Throwable throwable = mCrashReportDetails.throwable;
                StringBuilder typeText = new StringBuilder(throwable.getClass().getName());
                if (!TextUtils.isEmpty(throwable.getMessage()))
                    typeText.append(": ").append(throwable.getMessage());

                StackTraceElement[] stackTrace = throwable.getStackTrace();
                if (stackTrace.length > 0) {
                    typeText.append("\n").append("Thrown at ").append(stackTrace[0]);
                    for (int i = 1; i < Math.min(3, stackTrace.length); i++) {
                        typeText.append("\n").append(stackTrace[i]);
                    }
                }

                crashTypeView.setText(typeText);
            }
        }
    }

    public void onClickOnType(View v) {
        findViewById(R.id.logcat_fragment_container).setVisibility(View.VISIBLE);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.logcat_fragment_container, new LogCatViewFragment())
                .commit();
    }

    public void onCancelCrashReport(View v) {
        finish();
    }

    public void onSendCrashReport(View v) {
        String[] recipients = new String[] {BuildConfig.CRASH_REPORT_EMAIL_ADDRESS};

        Intent sendMail = new Intent();
        sendMail.setAction(Intent.ACTION_SEND);
        sendMail.setType("plain/text");
        sendMail.putExtra(Intent.EXTRA_EMAIL, recipients);
        sendMail.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.ime_crashed_title));
        sendMail.putExtra(Intent.EXTRA_TEXT, mCrashReportDetails.crashReportText);

        try {
            Intent sender =
                    Intent.createChooser(
                            sendMail, getString(R.string.ime_crashed_intent_selector_title));
            sender.putExtra(Intent.EXTRA_EMAIL, sendMail.getStringArrayExtra(Intent.EXTRA_EMAIL));
            sender.putExtra(Intent.EXTRA_SUBJECT, sendMail.getStringExtra(Intent.EXTRA_SUBJECT));
            sender.putExtra(Intent.EXTRA_TEXT, mCrashReportDetails.crashReportText);

            Logger.i(TAG, "Will send crash report using " + sender);
            startActivity(sender);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(
                            getApplicationContext(),
                            "Unable to send bug report via e-mail!",
                            Toast.LENGTH_LONG)
                    .show();
        }

        finish();
    }
}
