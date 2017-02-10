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

import android.app.Service;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import java.util.Calendar;


public class AboutAppView extends FrameLayout {

    private int mAppIconResId = 0;
    private String mAppName = null;
    private String mAppOwner = null;

    public AboutAppView(Context context) {
        this(context, null);
    }

    public AboutAppView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AboutAppView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AboutAppPreferenceAttributes);

        mAppIconResId = array.getResourceId(R.styleable.AboutAppPreferenceAttributes_appIcon, 0);
        mAppName = getStringOrReference(array, R.styleable.AboutAppPreferenceAttributes_appName);
        mAppOwner = getStringOrReference(array, R.styleable.AboutAppPreferenceAttributes_appOwner);

        array.recycle();

        initViewLayout();
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

    private void initViewLayout() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        ViewGroup rootLayout = (ViewGroup) inflater.inflate(R.layout.about_app_pref, this, false);

        ImageView appIcon = (ImageView) rootLayout.findViewById(R.id.app_icon);
        if (mAppIconResId == 0) {
            appIcon.setVisibility(View.GONE);
        } else {
            appIcon.setImageResource(mAppIconResId);
        }

        TextView name = (TextView) rootLayout.findViewById(R.id.app_name);
        name.setText(mAppName);

        final String appVersionName = BuildConfig.VERSION_NAME;
        final int appVersionNumber = BuildConfig.VERSION_CODE;

        TextView version = (TextView) rootLayout.findViewById(R.id.app_version);
        version.setText(getContext().getString(R.string.version_text_short, appVersionName, appVersionNumber));

        TextView appCopyright = (TextView) rootLayout.findViewById(R.id.app_copyright);
        if (mAppOwner == null) {
            appCopyright.setVisibility(View.GONE);
        } else {
            int year = Calendar.getInstance().get(Calendar.YEAR);
            appCopyright.setText(getContext().getString(R.string.copyright_text, year, mAppOwner));
        }

        removeAllViews();
        addView(rootLayout);
    }
}

