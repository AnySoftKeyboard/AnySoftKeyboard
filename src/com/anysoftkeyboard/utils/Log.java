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

package com.anysoftkeyboard.utils;

import com.menny.android.anysoftkeyboard.AnyApplication;

public class Log {
    private static final boolean DEBUG = AnyApplication.DEBUG;

    private Log() {
        //no instances please.
    }

    public static void v(String TAG, String text) {
        if (DEBUG) android.util.Log.v(TAG, text);
    }

    public static void v(String TAG, String text, Throwable t) {
        if (DEBUG) android.util.Log.v(TAG, text, t);
    }

    public static void d(String TAG, String text) {
        if (DEBUG) android.util.Log.d(TAG, text);
    }

    public static void d(String TAG, String text, Throwable t) {
        if (DEBUG) android.util.Log.d(TAG, text, t);
    }

    public static void i(String TAG, String text) {
        android.util.Log.i(TAG, text);
    }

    public static void i(String TAG, String text, Throwable t) {
        android.util.Log.i(TAG, text, t);
    }

    public static void w(String TAG, String text) {
        android.util.Log.w(TAG, text);
    }

    public static void w(String TAG, String text, Throwable t) {
        android.util.Log.w(TAG, text, t);
    }

    public static void e(String TAG, String text) {
        android.util.Log.e(TAG, text);
    }

    public static void e(String TAG, String text, Throwable t) {
        android.util.Log.e(TAG, text, t);
    }

}
