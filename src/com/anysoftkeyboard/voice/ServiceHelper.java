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

package com.anysoftkeyboard.voice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.util.Log;

/**
 * Service helper that connects the IME with the activity that triggers the recognition
 * and that will receive the recognition result.
 */
public class ServiceHelper extends Service {

    private static final String TAG = "ServiceHelper";

    private final IBinder mBinder = new ServiceHelperBinder();

    private Callback mCallback;

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "#onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "#onDestroy");
    }

    public void startRecognition(String languageLocale, Callback callback) {
        Log.i(TAG, "#startRecognition");
        mCallback = callback;
        Intent intent = new Intent(this, ActivityHelper.class);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageLocale);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void notifyResult(String recognitionResult) {
        if (mCallback != null) {
            mCallback.onResult(recognitionResult);
        }
    }

    public interface Callback {
        void onResult(String recognitionResult);
    }

    public class ServiceHelperBinder extends Binder {
        ServiceHelper getService() {
            return ServiceHelper.this;
        }
    }
}
