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
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import java.lang.ref.WeakReference;

import javax.annotation.Nullable;

public abstract class AddOnImpl implements AddOn {

    private final String mId;
    private final String mName;
    private final String mDescription;
    private final String mPackageName;
    private final Context mAskAppContext;
    private WeakReference<Context> mPackageContext;
    private final int mSortIndex;

    protected AddOnImpl(Context askContext, Context packageContext, String id, int nameResId,
                        String description, int sortIndex) {
        mId = id;
        mAskAppContext = askContext;
        mName = packageContext.getString(nameResId);
        mDescription = description;
        mPackageName = packageContext.getPackageName();
        mPackageContext = new WeakReference<Context>(packageContext);
        mSortIndex = sortIndex;
    }

    public final String getId() {
        return mId;
    }

    public final String getDescription() {
        return mDescription;
    }

    public String getPackageName() {
        return mPackageName;
    }

    @Nullable
    public final Context getPackageContext() {
        Context c = mPackageContext.get();
        if (c == null) {
            try {
                c = mAskAppContext.createPackageContext(mPackageName, PackageManager.GET_META_DATA);
                mPackageContext = new WeakReference<Context>(c);
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return c;
    }

    public final int getSortIndex() {
        return mSortIndex;
    }

    public String getName() {
        return mName;
    }
}
