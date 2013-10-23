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
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.ui.MainForm;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

public class AddOnStoreSearchPreference extends Preference implements OnClickListener {
    private static final String TAG = "AddOnStoreSearchPreference";

    private View mStoreNotFoundView;

    public AddOnStoreSearchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(R.layout.addon_store_search_pref, parent, false);
        layout.setOnClickListener(this);
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            TextView cta = (TextView) layout.findViewById(R.id.cta_title);
            cta.setText(title);
        }
        mStoreNotFoundView = layout.findViewById(R.id.no_store_found_error);
        mStoreNotFoundView.setVisibility(View.GONE);
        return layout;
    }

    public void onClick(View view) {
        try {
            String tag = getKey();
            MainForm.searchMarketForAddons(
                    getContext(),
                    " " + tag);
        } catch (Exception ex) {
            Log.e(TAG, "Could not launch Store search!", ex);
            mStoreNotFoundView.setVisibility(View.VISIBLE);
        }
    }
}
