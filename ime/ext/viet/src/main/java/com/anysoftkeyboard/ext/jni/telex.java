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

package com.anysoftkeyboard.ext.jni;

public class telex {
    static {
        System.loadLibrary("anysoftkey_telex");
    }
    private static native String getTelex(String word, String c);

    public static String doTheThing(String word, char c) {
        String c_string = Character.toString(c);
        String return_s = getTelex(word, c_string);
        return return_s;
    }
}
