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

package com.anysoftkeyboard.base.utils;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.PopupWindow;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.getkeepsafe.relinker.MissingLibraryException;
import com.getkeepsafe.relinker.ReLinker;

public class CompatUtils {
    private static String TAG = "ASK-CompatUtils";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public static void setPopupUnattachedToDecor(PopupWindow popupWindow) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            popupWindow.setAttachedInDecor(false);
        }
    }

    public static void unbindDrawable(Drawable d) {
        if (d != null) d.setCallback(null);
    }

    public static void loadNativeLibrary(
            @NonNull Context context, @NonNull String library, @NonNull String libraryVersion) {
        try {
            ReLinker.loadLibrary(context, library, libraryVersion);
        } catch (MissingLibraryException e) {
            Log.e(TAG, "******** Failed relink native library " + library + " ********");
            Log.e(TAG, "******** Failed relink native library " + library + " ********", e);
            Log.e(TAG, "******** Failed relink native library " + library + " ********");
            fallbackLoading(library);
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "******** Could not load native library " + library + " ********");
            Log.e(TAG, "******** Could not load native library " + library + " ********", ule);
            Log.e(TAG, "******** Could not load native library " + library + " ********");
            fallbackLoading(library);
        } catch (Throwable t) {
            Log.e(TAG, "******** Failed to load native library " + library + " ********");
            Log.e(TAG, "******** Failed to load native library " + library + " ********", t);
            Log.e(TAG, "******** Failed to load native library " + library + " ********");
            fallbackLoading(library);
        }
    }

    private static void fallbackLoading(String library) {
        try {
            Log.w(TAG, "Fallback loading native library " + library);
            System.loadLibrary(library);
        } catch (UnsatisfiedLinkError ule) {
            Log.e(TAG, "******** Could not load native library " + library + " ********");
            Log.e(TAG, "******** Could not load native library " + library + " ********", ule);
            Log.e(TAG, "******** Could not load native library " + library + " ********");
            // we are going to fail down the line anyway - better fail now
            throw ule;
        } catch (Throwable t) {
            Log.e(TAG, "******** Failed to load native library " + library + " ********");
            Log.e(TAG, "******** Failed to load native library " + library + " ********", t);
            Log.e(TAG, "******** Failed to load native library " + library + " ********");
            // we are going to fail down the line anyway - better fail now
            throw t;
        }
    }

    // this is needed since we do not have access to Objects.equals till API19
    public static boolean objectEquals(@Nullable Object first, @Nullable Object second) {
        //noinspection EqualsReplaceableByObjectsCall
        return (first == second) || (first != null && first.equals(second));
    }

    public static int appendImmutableFlag(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return flags | PendingIntent.FLAG_IMMUTABLE;
        } else {
            return flags;
        }
    }
}
