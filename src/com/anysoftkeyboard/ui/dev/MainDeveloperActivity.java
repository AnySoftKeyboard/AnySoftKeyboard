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

package com.anysoftkeyboard.ui.dev;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.anysoftkeyboard.ui.AsyncTaskWithProgressWindow;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import java.io.File;

public class MainDeveloperActivity extends Activity {

    private PopupWindow mPopupWindow;

    private abstract static class DeveloperAsyncTask<Params, Progress, Result>
            extends
            AsyncTaskWithProgressWindow<Params, Progress, Result, MainDeveloperActivity> {

        public DeveloperAsyncTask(MainDeveloperActivity mainDeveloperActivity) {
            super(mainDeveloperActivity);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.developer_tools);
        ((TextView) findViewById(R.id.dev_title)).setText(DeveloperUtils.getAppDetails(getApplicationContext()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateTracingState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPopupWindow != null)
            mPopupWindow.dismiss();
        mPopupWindow = null;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mPopupWindow != null && mPopupWindow.isShowing())
            return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            mPopupWindow = null;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void updateTracingState() {
        Button flipper = (Button) findViewById(R.id.dev_flip_trace_file);
        View progress = findViewById(R.id.dev_tracing_running_progress_bar);
        View share = findViewById(R.id.dev_share_trace_file);

        if (DeveloperUtils.hasTracingRequested(getApplicationContext())) {
            flipper.setText("Disable tracing");
        } else {
            flipper.setText("Enable tracing");
        }

        if (DeveloperUtils.hasTracingStarted()) {
            progress.setVisibility(View.VISIBLE);
        } else {
            progress.setVisibility(View.INVISIBLE);
        }

        if (!DeveloperUtils.hasTracingStarted()
                && DeveloperUtils.getTraceFile().exists()) {
            share.setEnabled(true);
        } else {
            share.setEnabled(false);
        }
    }

    public void onUserClickedMemoryDump(View v) {
        DeveloperAsyncTask<Void, Void, File> task = new DeveloperAsyncTask<Void, Void, File>(
                this) {

            @Override
            protected File doAsyncTask(Void[] params) throws Exception {
                return DeveloperUtils.createMemoryDump();
            }

            @Override
            protected void applyResults(File result,
                                        Exception backgroundException) {
                if (backgroundException != null) {
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.failed_to_create_mem_dump,
                                    backgroundException.getMessage()),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.created_mem_dump_file,
                                    result.getAbsolutePath()),
                            Toast.LENGTH_LONG).show();
                    View shareMemFile = findViewById(R.id.dev_share_mem_file);
                    shareMemFile.setTag(result);
                    shareMemFile.setEnabled(result.exists() && result.isFile());
                }
            }
        };
        task.execute();
    }

    public void onUserClickedShareMemoryDump(View v) {
        File memDump = (File) v.getTag();

        StringBuilder sb = new StringBuilder(
                "Hi! Here is a memory dump file for ");
        sb.append(DeveloperUtils.getAppDetails(getApplicationContext()));
        sb.append(DeveloperUtils.NEW_LINE);
        sb.append(DeveloperUtils.getSysInfo());

        shareFile(memDump, "AnySoftKeyboard Memory Dump File", sb.toString());
    }

    public void onUserClickedFlipTracing(View v) {
        final boolean enable = !DeveloperUtils
                .hasTracingRequested(getApplicationContext());
        DeveloperUtils.setTracingRequested(getApplicationContext(), enable);

        updateTracingState();

        if (enable) {
            // Just a few words to the user
            AlertDialog info = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.notification_icon_beta_version)
                    .setTitle("How to use Tracing")
                    .setMessage(
                            "Tracing is now enabled, but not started!"+DeveloperUtils.NEW_LINE+"To start tracing, you'll need to restart AnySoftKeyboard. How? Either reboot your phone, or switch to another keyboard app (like the stock)."+DeveloperUtils.NEW_LINE+"To stop tracing, first disable it, and then restart AnySoftkeyboard (as above)."+DeveloperUtils.NEW_LINE+"Thanks!!")
                    .setPositiveButton("Got it!", null).create();

            info.show();
        } else if (DeveloperUtils.hasTracingStarted()) {
            // the tracing is running now, so I'll explain how to stop it
            AlertDialog info = new AlertDialog.Builder(this)
                    .setIcon(R.drawable.notification_icon_beta_version)
                    .setTitle("How to stop Tracing")
                    .setMessage(
                            "Tracing is now disabled, but not ended!"+DeveloperUtils.NEW_LINE+"To end tracing (and to be able to send the file), you'll need to restart AnySoftKeyboard. How? Either reboot your phone (preferable), or switch to another keyboard app (like the stock)."+DeveloperUtils.NEW_LINE+"Thanks!!")
                    .setPositiveButton("Got it!", null).create();

            info.show();
        }
    }

    public void onUserClickedShareTracingFile(View v) {
        StringBuilder sb = new StringBuilder("Hi! Here is a tracing file for ");
        sb.append(DeveloperUtils.getAppDetails(getApplicationContext()));
        sb.append(DeveloperUtils.NEW_LINE);
        sb.append(DeveloperUtils.getSysInfo());

        shareFile(DeveloperUtils.getTraceFile(), "AnySoftKeyboard Trace File",
                sb.toString());
    }

    public void onUserClickedShowLogCat(View v) {
        View logRootView = getLayoutInflater().inflate(R.layout.developer_logcat_layout, null);
        TextView linesRoot = (TextView) logRootView.findViewById(R.id.lines_text_view);

        String log = Log.getAllLogLines();
        linesRoot.setText(log);

        mPopupWindow = new PopupWindow(logRootView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.showAtLocation(findViewById(R.id.root), Gravity.CENTER, 0, 0);
    }

    public void onUserClickedShareLogCat(View v) {
        StringBuilder sb = new StringBuilder("Hi! Here is a LogCat snippet for ");
        sb.append(DeveloperUtils.getAppDetails(getApplicationContext()));
        sb.append(DeveloperUtils.NEW_LINE);
        sb.append(DeveloperUtils.getSysInfo());
        sb.append(DeveloperUtils.NEW_LINE);
        sb.append(Log.getAllLogLines());

        shareFile(null, "AnySoftKeyboard LogCat",
                sb.toString());
    }

    private void shareFile(File fileToShare, String title, String message) {
        Intent sendMail = new Intent();
        sendMail.setAction(Intent.ACTION_SEND);
        sendMail.setType("plain/text");
        sendMail.putExtra(Intent.EXTRA_SUBJECT, title);
        sendMail.putExtra(Intent.EXTRA_TEXT, message);
        if (fileToShare != null){
            sendMail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileToShare));
        }

        try {
            Intent sender = Intent.createChooser(sendMail, "Share");
            sender.putExtra(Intent.EXTRA_SUBJECT, title);
            sender.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(sender);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(),
                    "Unable to send bug report via e-mail!", Toast.LENGTH_LONG)
                    .show();
        }
    }
}
