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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.base.utils.Logger;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;
import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow;

import java.io.File;

@SuppressLint("SetTextI18n")
public class DeveloperToolsFragment extends Fragment implements AsyncTaskWithProgressWindow.AsyncTaskOwner, View.OnClickListener {

    private abstract static class DeveloperAsyncTask<I, P, R>
            extends
            AsyncTaskWithProgressWindow<I, P, R, DeveloperToolsFragment> {

        public DeveloperAsyncTask(DeveloperToolsFragment mainDeveloperActivity) {
            super(mainDeveloperActivity);
        }

    }

    private Button mFlipper;
    private View mProgressIndicator;
    private View mShareButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(com.menny.android.anysoftkeyboard.R.layout.developer_tools, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) view.findViewById(com.menny.android.anysoftkeyboard.R.id.dev_title)).setText(DeveloperUtils.getAppDetails(getActivity().getApplicationContext()));

        mFlipper = view.findViewById(com.menny.android.anysoftkeyboard.R.id.dev_flip_trace_file);
        mProgressIndicator = view.findViewById(com.menny.android.anysoftkeyboard.R.id.dev_tracing_running_progress_bar);
        mShareButton = view.findViewById(com.menny.android.anysoftkeyboard.R.id.dev_share_trace_file);

        view.findViewById(com.menny.android.anysoftkeyboard.R.id.memory_dump_button).setOnClickListener(this);
        view.findViewById(com.menny.android.anysoftkeyboard.R.id.dev_share_mem_file).setOnClickListener(this);
        view.findViewById(com.menny.android.anysoftkeyboard.R.id.dev_flip_trace_file).setOnClickListener(this);
        view.findViewById(com.menny.android.anysoftkeyboard.R.id.dev_share_trace_file).setOnClickListener(this);
        view.findViewById(com.menny.android.anysoftkeyboard.R.id.show_logcat_button).setOnClickListener(this);
        view.findViewById(com.menny.android.anysoftkeyboard.R.id.share_logcat_button).setOnClickListener(this);

        TextView textWithListener = (TextView) view.findViewById(com.menny.android.anysoftkeyboard.R.id.actionDoneWithListener);
        textWithListener.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                Toast.makeText(getContext().getApplicationContext(), "OnEditorActionListener i:" + i, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTracingState();
        MainSettingsActivity.setActivityTitle(this, getString(com.menny.android.anysoftkeyboard.R.string.developer_tools));
    }

    private void updateTracingState() {
        if (DeveloperUtils.hasTracingRequested(getActivity().getApplicationContext())) {
            mFlipper.setText("Disable tracing");
        } else {
            mFlipper.setText("Enable tracing");
        }

        if (DeveloperUtils.hasTracingStarted()) {
            mProgressIndicator.setVisibility(View.VISIBLE);
        } else {
            mProgressIndicator.setVisibility(View.INVISIBLE);
        }

        if (!DeveloperUtils.hasTracingStarted()
                && DeveloperUtils.getTraceFile().exists()) {
            mShareButton.setEnabled(true);
        } else {
            mShareButton.setEnabled(false);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case com.menny.android.anysoftkeyboard.R.id.memory_dump_button:
                onUserClickedMemoryDump();
                break;
            case com.menny.android.anysoftkeyboard.R.id.dev_share_mem_file:
                onUserClickedShareMemoryDump(v);
                break;
            case com.menny.android.anysoftkeyboard.R.id.dev_flip_trace_file:
                onUserClickedFlipTracing();
                break;
            case com.menny.android.anysoftkeyboard.R.id.dev_share_trace_file:
                onUserClickedShareTracingFile();
                break;
            case com.menny.android.anysoftkeyboard.R.id.show_logcat_button:
                onUserClickedShowLogCat();
                break;
            case com.menny.android.anysoftkeyboard.R.id.share_logcat_button:
                onUserClickedShareLogCat();
                break;
            default:
                throw new IllegalArgumentException("Failed to handle "+v.getId()+" in DeveloperToolsFragment");
        }
    }

    private void onUserClickedMemoryDump() {
        DeveloperAsyncTask<Void, Void, File> task = new DeveloperAsyncTask<Void, Void, File>(
                this) {

            @Override
            protected File doAsyncTask(Void[] params) throws Exception {
                return DeveloperUtils.createMemoryDump();
            }

            @Override
            protected void applyResults(File result,
                                        Exception backgroundException) {
                Activity activity = getActivity();
                if (activity == null)
                    return;
                View rootView = getView();
                if (rootView == null)
                    return;

                if (backgroundException != null) {
                    Toast.makeText(activity.getApplicationContext(),
                            getString(com.menny.android.anysoftkeyboard.R.string.failed_to_create_mem_dump,
                                    backgroundException.getMessage()),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(activity.getApplicationContext(),
                            getString(com.menny.android.anysoftkeyboard.R.string.created_mem_dump_file,
                                    result.getAbsolutePath()),
                            Toast.LENGTH_LONG).show();
                    View shareMemFile = rootView.findViewById(com.menny.android.anysoftkeyboard.R.id.dev_share_mem_file);
                    shareMemFile.setTag(result);
                    shareMemFile.setEnabled(result.exists() && result.isFile());
                }
            }
        };
        task.execute();
    }

    private void onUserClickedShareMemoryDump(View v) {
        File memDump = (File) v.getTag();

        shareFile(memDump, "AnySoftKeyboard Memory Dump File", "Hi! Here is a memory dump file for " + DeveloperUtils.getAppDetails(getActivity().getApplicationContext()) + DeveloperUtils.NEW_LINE + DeveloperUtils.getSysInfo(getActivity()));
    }

    private void onUserClickedFlipTracing() {
        final boolean enable = !DeveloperUtils
                .hasTracingRequested(getActivity().getApplicationContext());
        DeveloperUtils.setTracingRequested(getActivity().getApplicationContext(), enable);

        updateTracingState();

        if (enable) {
            // Just a few words to the user
            AlertDialog info = new AlertDialog.Builder(getActivity())
                    .setIcon(com.menny.android.anysoftkeyboard.R.drawable.notification_icon_beta_version)
                    .setTitle("How to use Tracing")
                    .setMessage(
                            "Tracing is now enabled, but not started!" + DeveloperUtils.NEW_LINE + "To start tracing, you'll need to restart AnySoftKeyboard. How? Either reboot your phone, or switch to another keyboard app (like the stock)." + DeveloperUtils.NEW_LINE + "To stop tracing, first disable it, and then restart AnySoftKeyboard (as above)." + DeveloperUtils.NEW_LINE + "Thanks!!")
                    .setPositiveButton("Got it!", null).create();

            info.show();
        } else if (DeveloperUtils.hasTracingStarted()) {
            // the tracing is running now, so I'll explain how to stop it
            AlertDialog info = new AlertDialog.Builder(getActivity())
                    .setIcon(com.menny.android.anysoftkeyboard.R.drawable.notification_icon_beta_version)
                    .setTitle("How to stop Tracing")
                    .setMessage(
                            "Tracing is now disabled, but not ended!" + DeveloperUtils.NEW_LINE + "To end tracing (and to be able to send the file), you'll need to restart AnySoftKeyboard. How? Either reboot your phone (preferable), or switch to another keyboard app (like the stock)." + DeveloperUtils.NEW_LINE + "Thanks!!")
                    .setPositiveButton("Got it!", null).create();

            info.show();
        }
    }

    private void onUserClickedShareTracingFile() {
        shareFile(DeveloperUtils.getTraceFile(), "AnySoftKeyboard Trace File",
                "Hi! Here is a tracing file for " + DeveloperUtils.getAppDetails(getActivity().getApplicationContext()) + DeveloperUtils.NEW_LINE + DeveloperUtils.getSysInfo(getActivity()));
    }

    private void onUserClickedShowLogCat() {
        ((FragmentChauffeurActivity) getActivity()).addFragmentToUi(new LogCatViewFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
    }

    private void onUserClickedShareLogCat() {
        shareFile(null, "AnySoftKeyboard LogCat",
                "Hi! Here is a LogCat snippet for " + DeveloperUtils.getAppDetails(getActivity().getApplicationContext()) + DeveloperUtils.NEW_LINE + DeveloperUtils.getSysInfo(getActivity()) + DeveloperUtils.NEW_LINE + Logger.getAllLogLines());
    }

    private void shareFile(File fileToShare, String title, String message) {
        Intent sendMail = new Intent();
        sendMail.setAction(Intent.ACTION_SEND);
        sendMail.setType("plain/text");
        sendMail.putExtra(Intent.EXTRA_SUBJECT, title);
        sendMail.putExtra(Intent.EXTRA_TEXT, message);
        if (fileToShare != null) {
            sendMail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fileToShare));
        }

        try {
            Intent sender = Intent.createChooser(sendMail, "Share");
            sender.putExtra(Intent.EXTRA_SUBJECT, title);
            sender.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(sender);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Unable to send bug report via e-mail!", Toast.LENGTH_LONG)
                    .show();
        }
    }
}
