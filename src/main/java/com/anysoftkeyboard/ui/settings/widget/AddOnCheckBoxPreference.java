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
package com.anysoftkeyboard.ui.settings.widget;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.IconHolder;
import com.anysoftkeyboard.addons.ScreenshotHolder;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.Banner;

public class AddOnCheckBoxPreference extends Preference implements
        OnCheckedChangeListener, OnClickListener {
    private static final String TAG = "AddOnCheckBoxPreference";

    private CheckBox mCheckBox;
    private TextView mName, mDescription;
    private ImageView mAddOnIcon;
    private View mIconOverlay;
    private AddOn mAddOn;

    public AddOnCheckBoxPreference(Context context) {
        this(context, null);
    }

    public AddOnCheckBoxPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AddOnCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setPersistent(true);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater inflator = LayoutInflater.from(getContext());
        ViewGroup layout = (ViewGroup) inflator.inflate(R.layout.addon_checkbox_pref, parent, false);

        mCheckBox = (CheckBox) layout.findViewById(R.id.addon_checkbox);
        mCheckBox.setOnCheckedChangeListener(this);
        mName = (TextView) layout.findViewById(R.id.addon_title);
        mDescription = (TextView) layout.findViewById(R.id.addon_description);
        mAddOnIcon = (ImageView) layout.findViewById(R.id.addon_image);
        mIconOverlay = layout.findViewById(R.id.addon_image_more_overlay);
        populateViews();
        return layout;
    }

    private void populateViews() {
        if (mAddOn == null || mCheckBox == null)
            return;// view is not ready yet.
        setKey(mAddOn.getId());
        mName.setText(mAddOn.getName());
        mDescription.setText(mAddOn.getDescription());
        Drawable icon = null;
        if (mAddOn instanceof IconHolder) {
            IconHolder addOn = (IconHolder) mAddOn;
            icon = addOn.getIcon();
        }

        if (icon == null) {
            try {
                PackageManager packageManager = getContext().getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(mAddOn.getPackageName(), 0);
                if (packageInfo != null) {
                    Log.w(TAG, "Failed to locate add-on package (which is weird, we DID load the add-on object from it).");
                    icon = packageInfo.applicationInfo.loadIcon(packageManager);
                }
            } catch (PackageManager.NameNotFoundException e) {
                icon = null;
                Log.w(TAG, "Failed to locate add-on package (which is weird, we DID load the add-on object from it).");
            }
        }

        mAddOnIcon.setImageDrawable(icon);

        if (mAddOn instanceof ScreenshotHolder) {
            if (((ScreenshotHolder) mAddOn).hasScreenshot()) {
                mAddOnIcon.setOnClickListener(this);
                mIconOverlay.setVisibility(View.VISIBLE);
            } else {
                mIconOverlay.setVisibility(View.GONE);
            }
        }
        boolean defaultChecked = false;
        if (mAddOn instanceof KeyboardAddOnAndBuilder) {
            defaultChecked = ((KeyboardAddOnAndBuilder) mAddOn).getKeyboardDefaultEnabled();
        }
        mCheckBox.setChecked(getPersistedBoolean(defaultChecked));
    }

    public void setAddOn(AddOn addOn) {
        mAddOn = addOn;
        populateViews();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mCheckBox.setChecked(isChecked);
        persistBoolean(isChecked);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.addon_image) {
            // showing a screenshot (if available)
            Drawable screenshot = null;
            if (mAddOn instanceof ScreenshotHolder) {
                ScreenshotHolder holder = (ScreenshotHolder) mAddOn;
                screenshot = holder.getScreenshot();
            }
            if (screenshot == null) {
                screenshot = mAddOnIcon.getDrawable();
            }
            //
            if (screenshot == null) return;
            //inflating the screenshot view
            LayoutInflater inflator = LayoutInflater.from(getContext());
            ViewGroup layout = (ViewGroup) inflator.inflate(
                    R.layout.addon_screenshot, null);
            final PopupWindow popup = new PopupWindow(getContext());
            popup.setContentView(layout);
            DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
            popup.setWidth(dm.widthPixels);
            popup.setHeight(dm.heightPixels);
            popup.setAnimationStyle(R.style.AddonScreenshotPopupAnimation);
            layout.findViewById(R.id.addon_screenshot_close).setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    popup.dismiss();
                }
            });
            ((Banner)layout.findViewById(R.id.addon_screenshot)).setImageDrawable(screenshot);
            popup.showAtLocation(view, Gravity.CENTER, 0, 0);
        }
    }
}
