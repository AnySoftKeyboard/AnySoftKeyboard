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
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.R;

public class AddOnStoreSearchView extends FrameLayout implements OnClickListener {
    private static final String TAG = "AddOnStoreSearchView";

    private View mStoreNotFoundView;

    public AddOnStoreSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.addon_store_search_view, this);
        setOnClickListener(this);

        mStoreNotFoundView = findViewById(R.id.no_store_found_error);
        mStoreNotFoundView.setVisibility(View.GONE);
        if (attrs != null) {
            CharSequence title = attrs.getAttributeValue("android", "title");
            if (!TextUtils.isEmpty(title)) {
                TextView cta = (TextView) findViewById(R.id.cta_title);
                cta.setText(title);
            }
        }
    }

    public void onClick(View view) {
        if (!startMarketActivity(getContext(), (String) getTag())) {
            mStoreNotFoundView.setVisibility(View.VISIBLE);
        }
    }

    public static boolean startMarketActivity(@NonNull Context context, @NonNull String marketKeyword) {
        try {
            Intent search = new Intent(Intent.ACTION_VIEW);
            search.setData(Uri.parse("market://search?q=AnySoftKeyboard " + marketKeyword));
            search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(search);
        } catch (Exception ex) {
            Logger.e(TAG, "Could not launch Store search!", ex);
            return false;
        }
        return true;
    }

    public void setTitle(CharSequence title) {
        TextView cta = (TextView) findViewById(R.id.cta_title);
        cta.setText(title);
    }
}
