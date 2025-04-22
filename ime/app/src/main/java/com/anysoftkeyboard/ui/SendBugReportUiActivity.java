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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.chewbacca.BugReportDetails;
import com.anysoftkeyboard.fileprovider.LocalProxy;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class SendBugReportUiActivity extends FragmentActivity {

  private static final String TAG = "ASKBugSender";

  private BugReportDetails mCrashReportDetails;
  private Disposable mDisposable = Disposables.empty();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.send_crash_log_ui);
  }

  @Override
  protected void onStart() {
    super.onStart();
    Intent callingIntent = getIntent();
    mCrashReportDetails =
        callingIntent.getParcelableExtra(BugReportDetails.EXTRA_KEY_BugReportDetails);
    if (mCrashReportDetails == null) {
      if (BuildConfig.DEBUG)
        throw new IllegalArgumentException(
            "Activity started without " + BugReportDetails.EXTRA_KEY_BugReportDetails + " extra!");
      finish();
    }

    ((TextView) findViewById(R.id.errorHeader)).setText(mCrashReportDetails.crashHeader);
  }

  public void onCancelCrashReport(View v) {
    finish();
  }

  public void onSendCrashReport(View v) {
    mDisposable.dispose();
    mDisposable =
        LocalProxy.proxy(this, mCrashReportDetails.fullReport).subscribe(this::sendReportViaSend);
  }

  private void sendReportViaSend(Uri fullReportUri) {
    String[] recipients = new String[] {BuildConfig.CRASH_REPORT_EMAIL_ADDRESS};

    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.setType("plain/text");
    sendIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.ime_crashed_title));
    sendIntent.putExtra(Intent.EXTRA_TEXT, mCrashReportDetails.crashHeader);
    sendIntent.putExtra(Intent.EXTRA_STREAM, fullReportUri);

    Intent sender =
        Intent.createChooser(sendIntent, getString(R.string.ime_crashed_intent_selector_title));
    Logger.i(TAG, "Sending crash report intent %s, with attachment %s", sender, fullReportUri);
    try {
      startActivity(sender);
    } catch (android.content.ActivityNotFoundException ex) {
      Toast.makeText(
              getApplicationContext(), "Unable to send bug report via e-mail!", Toast.LENGTH_LONG)
          .show();
    }
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mDisposable.dispose();
  }
}
