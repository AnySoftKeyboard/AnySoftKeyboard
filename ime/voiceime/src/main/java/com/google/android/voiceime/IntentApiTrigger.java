/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.android.voiceime;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.inputmethodservice.InputMethodService;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Triggers a voice recognition using the Intent api. */
class IntentApiTrigger implements Trigger {

    private static final String TAG = "VoiceIntentApiTrigger";

    private final InputMethodService mInputMethodService;

    private final ServiceBridge mServiceBridge;

    private String mLastRecognitionResult;

    private Set<Character> mUpperCaseChars;

    private final Handler mHandler;

    private IBinder mToken;

    public IntentApiTrigger(InputMethodService inputMethodService) {
        mInputMethodService = inputMethodService;

        mServiceBridge =
                new ServiceBridge(
                        new Callback() {

                            @Override
                            public void onRecognitionResult(String recognitionResult) {
                                postResult(recognitionResult);
                            }
                        });

        mUpperCaseChars = new HashSet<>();
        mUpperCaseChars.add('.');
        mUpperCaseChars.add('!');
        mUpperCaseChars.add('?');
        mUpperCaseChars.add('\n');

        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void startVoiceRecognition(String language) {
        mToken = mInputMethodService.getWindow().getWindow().getAttributes().token;

        mServiceBridge.startVoiceRecognition(mInputMethodService, language);
    }

    public static boolean isInstalled(InputMethodService inputMethodService) {
        PackageManager pm = inputMethodService.getPackageManager();
        List<ResolveInfo> activities =
                pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return activities.size() > 0;
    }

    private InputMethodManager getInputMethodManager() {
        return (InputMethodManager)
                mInputMethodService.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void postResult(String recognitionResult) {
        mLastRecognitionResult = recognitionResult;

        // Request the system to display the IME.
        getInputMethodManager()
                .showSoftInputFromInputMethod(mToken, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onStartInputView() {
        Log.i(TAG, "#onStartInputView");
        if (mLastRecognitionResult != null) {
            scheduleCommit();
        }
    }

    private void scheduleCommit() {
        mHandler.post(
                new Runnable() {

                    @Override
                    public void run() {
                        commitResult();
                    }
                });
    }

    private void commitResult() {
        if (mLastRecognitionResult == null) {
            return;
        }

        String result = mLastRecognitionResult;

        InputConnection conn = mInputMethodService.getCurrentInputConnection();

        if (conn == null) {
            Log.i(
                    TAG,
                    "Unable to commit recognition result, as the current input connection "
                            + "is null. Did someone kill the IME?");
            return;
        }

        if (!conn.beginBatchEdit()) {
            Log.i(TAG, "Unable to commit recognition result, as a batch edit cannot start");
            return;
        }

        try {
            ExtractedTextRequest etr = new ExtractedTextRequest();
            etr.flags = InputConnection.GET_TEXT_WITH_STYLES;

            ExtractedText et = conn.getExtractedText(etr, 0);

            if (et == null) {
                Log.i(TAG, "Unable to commit recognition result, as extracted text is null");
                return;
            }

            if (et.text != null) {

                if (et.selectionStart != et.selectionEnd) {
                    conn.deleteSurroundingText(et.selectionStart, et.selectionEnd);
                }

                result = format(et, result);
            }

            if (!conn.commitText(result, 0)) {
                Log.i(TAG, "Unable to commit recognition result");
                return;
            }

            mLastRecognitionResult = null;
        } finally {
            conn.endBatchEdit();
        }
    }

    /**
     * Formats the recognised text by adding white spaces at the beginning or at the end, and by
     * making the first char upper case if necessary.
     */
    private String format(ExtractedText et, String result) {
        int pos = et.selectionStart - 1;

        while (pos > 0 && Character.isWhitespace(et.text.charAt(pos))) {
            pos--;
        }

        if (pos == -1 || mUpperCaseChars.contains(et.text.charAt(pos))) {
            result = Character.toUpperCase(result.charAt(0)) + result.substring(1);
        }

        if (et.selectionStart - 1 > 0
                && !Character.isWhitespace(et.text.charAt(et.selectionStart - 1))) {
            result = " " + result;
        }

        if (et.selectionEnd < et.text.length()
                && !Character.isWhitespace(et.text.charAt(et.selectionEnd))) {
            result = result + " ";
        }
        return result;
    }

    interface Callback {
        void onRecognitionResult(String recognitionResult);
    }
}
