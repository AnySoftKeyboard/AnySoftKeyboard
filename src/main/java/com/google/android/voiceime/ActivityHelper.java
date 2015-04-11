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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import com.menny.android.anysoftkeyboard.BuildConfig;

import java.util.ArrayList;

/**
 * Helper activity used for triggering the Intent recognition, and for collecting the results.
 */
public class ActivityHelper extends Activity {

    @SuppressWarnings("unused")
    private static final String TAG = "ActivityHelper";

    private static final int RECOGNITION_REQUEST = 1;

    private ServiceBridge mServiceBridge;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mServiceBridge = new ServiceBridge();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra("calling_package"/*RecognizerIntent.EXTRA_CALLING_PACKAGE*/, BuildConfig.APPLICATION_ID);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        // Specify the recognition language if provided.
        if (bundle != null) {
            String languageLocale = bundle.getString(RecognizerIntent.EXTRA_LANGUAGE);
            if (languageLocale != null) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageLocale);
            }
        }
        startActivityForResult(intent, RECOGNITION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOGNITION_REQUEST && data != null
                && data.hasExtra(RecognizerIntent.EXTRA_RESULTS)) {
            ArrayList<String> results = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            createResultDialog(results.toArray(new String[results.size()])).show();
        } else {
            notifyResult(null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private Dialog createResultDialog(final String[] recognitionResults) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            builder = new AlertDialog.Builder(this);
        } else {
            builder = new AlertDialog.Builder(this,
                    android.R.style.Theme_Holo_Dialog_NoActionBar);
        }

        builder.setItems(recognitionResults, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyResult(recognitionResults[which]);
            }
        });

        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                notifyResult(null);
            }
        });

        builder.setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyResult(null);
            }
        });

        return builder.create();
    }

    private void notifyResult(String result) {
        mServiceBridge.notifyResult(this, result);
        finish();
    }
}
