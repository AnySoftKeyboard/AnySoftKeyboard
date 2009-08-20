/*
 * Copyright (C) 2009 Spiros Papadimitriou <spapadim@cs.cmu.edu>
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

package com.menny.android.anysoftkeyboard.Dictionary;

import java.io.IOException;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;

class EnglishBinaryDictionary extends BinaryDictionary {
    public EnglishBinaryDictionary (Context context) throws NameNotFoundException, IOException {
        super(context.getPackageManager().getResourcesForApplication("com.android.inputmethod.latin").getAssets().openNonAssetFd("res/raw-en/main.dict"));
    }
}
