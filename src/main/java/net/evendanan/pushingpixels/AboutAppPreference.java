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

/* The following code was written by Matthew Wiggins
 * and is released under the APACHE 2.0 license 
 * 
 * additional code was written by Menny Even Danan, and is also released under APACHE 2.0 license 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package net.evendanan.pushingpixels;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.R;

import java.util.Calendar;


public class AboutAppPreference extends Preference {

    private int mAppIconResId = 0;
    private String mAppName = null;
    private String mAppOwner = null;

    public AboutAppPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.about_app_pref);
        setSelectable(false);
        setPersistent(false);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AboutAppPreferenceAttributes);

        mAppIconResId = array.getResourceId(R.styleable.AboutAppPreferenceAttributes_appIcon, 0);
        mAppName = getStringOrReference(array, R.styleable.AboutAppPreferenceAttributes_appName);
        mAppOwner = getStringOrReference(array, R.styleable.AboutAppPreferenceAttributes_appOwner);

        array.recycle();
    }

    private String getStringOrReference(TypedArray array, int index) {
        String value = array.getString(index);
        if (value == null) {
            int valueResId = array.getResourceId(index, 0);
            if (valueResId == 0)
                return null;
            else
                return getContext().getString(valueResId);
        } else {
            return value;
        }
    }


    @Override
    protected View onCreateView(ViewGroup parent) {
        View rootLayout = super.onCreateView(parent);

        ImageView appIcon = (ImageView) rootLayout.findViewById(R.id.app_icon);
        if (mAppIconResId == 0) {
            appIcon.setVisibility(View.GONE);
        } else {
            appIcon.setImageResource(mAppIconResId);
        }

        TextView name = (TextView) rootLayout.findViewById(R.id.app_name);
        name.setText(mAppName);

        String appVersionName = "";
        int appVersionNumber = 0;
        try {
            PackageInfo info = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            appVersionName = info.versionName;
            appVersionNumber = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView version = (TextView) rootLayout.findViewById(R.id.app_version);
        version.setText(getContext().getString(R.string.version_text, appVersionName, appVersionNumber));

        TextView appCopyright = (TextView) rootLayout.findViewById(R.id.app_copyright);
        if (mAppOwner == null) {
            appCopyright.setVisibility(View.GONE);
        } else {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            appCopyright.setText(getContext().getString(R.string.copyright_text, year, mAppOwner));
        }

        return rootLayout;
    }
}

