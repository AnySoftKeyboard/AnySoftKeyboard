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

import android.util.Log;
import android.view.inputmethod.EditorInfo;
import com.menny.android.anysoftkeyboard.AnyApplication;

import java.lang.reflect.Field;

public class Workarounds {
    //Determine whether this device has the fix for RTL in the suggestions list
    //private static final boolean ms_requiresRtlWorkaround;

    private static final int ms_ApiLevel;


    private static final String TAG = "ASK Workaround";

    static {
        //checking f/w API is a bit tricky, we need to do it by reflection
        int sdkVersion = 1;
        try {
            Field sdkInt = android.os.Build.VERSION.class.getField("SDK_INT");
            if (sdkInt != null) {
                //NOTE: I can not use the field here, since this code MAY run in cupcake, and therefore
                //fail in JIT compile. I need to perform this function using reflection...
                sdkVersion = sdkInt.getInt(null);
            }
        } catch (Exception ex) {
            sdkVersion = 3;
        }
        ms_ApiLevel = sdkVersion;
    }

    public static boolean isRightToLeftCharacter(final char key) {
        final byte direction = Character.getDirectionality(key);

        switch (direction) {
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
            case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isAltSpaceLangSwitchNotPossible() {
        String model = android.os.Build.MODEL.toLowerCase();
        if (model.equals("milestone") || model.equals("droid")) {
            return true;
        }
        return false;
    }

    public static int getApiLevel() {
        return ms_ApiLevel;
    }

    public static boolean doubleActionKeyDisableWorkAround(EditorInfo editor) {
        if (editor != null) {
            //package: com.android.mms, id:2131361817
            //in firmware 2, 2.1
            if (ms_ApiLevel <= 6 && ms_ApiLevel >= 5 && editor.packageName.contentEquals("com.android.mms")
                    && (editor.fieldId == 2131361817)) {
                if (AnyApplication.DEBUG) Log.d(TAG, "Android Ecliar Messaging MESSAGE field");
                return true;
            }
        }

        return false;
    }
}
