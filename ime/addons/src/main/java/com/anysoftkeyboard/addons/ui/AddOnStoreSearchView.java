package com.anysoftkeyboard.addons.ui;

/*
 * Copyright (c) 2023 Menny Even-Danan
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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.anysoftkeyboard.addons.R;

public class AddOnStoreSearchView extends FrameLayout {
  private static final String TAG = "AddOnStoreSearchView";

  public AddOnStoreSearchView(Context context, AttributeSet attrs) {
    super(context, attrs);
    inflate(context, R.layout.addon_store_search_view, this);
  }

  public void setTitle(CharSequence title) {
    TextView cta = findViewById(R.id.cta_title);
    cta.setText(title);
  }
}
