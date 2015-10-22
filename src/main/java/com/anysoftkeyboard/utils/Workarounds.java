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

import java.util.Locale;

public class Workarounds {

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
        String model = android.os.Build.MODEL.toLowerCase(Locale.US);
        if (model.equals("milestone") || model.equals("droid")) {
            return true;
        }
        return false;
    }
}
