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

package com.anysoftkeyboard.addons;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface AddOn {
    interface AddOnResourceMapping {
        int[] getRemoteStyleableArrayFromLocal(int[] localStyleableArray);

        int getApiVersion();

        int getLocalAttrId(int remoteAttrId);
    }

    int INVALID_RES_ID = 0;

    String getId();

    String getName();

    CharSequence getDescription();

    String getPackageName();

    @Nullable
    Context getPackageContext();

    int getSortIndex();

    @NonNull
    AddOnResourceMapping getResourceMapping();

    int getApiVersion();
}
