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

package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class KeyboardAddOnBrowserFragment extends AbstractAddOnsBrowserFragment<KeyboardAddOnAndBuilder> {

    public KeyboardAddOnBrowserFragment() {
        super("LanguageAddOnBrowserFragment", R.string.keyboards_group, false, false, false);
    }

    @NonNull
    @Override
    protected AddOnsFactory<KeyboardAddOnAndBuilder> getAddOnFactory() {
        return AnyApplication.getKeyboardFactory(getContext());
    }

    @Override
    protected int getItemDragDirectionFlags() {
        return ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    }

    @Nullable
    @Override
    protected String getMarketSearchKeyword() {
        return "language";
    }

    @Override
    protected int getMarketSearchTitle() {
        return R.string.search_market_for_keyboard_addons;
    }

    @Override
    protected void applyAddOnToDemoKeyboardView(@NonNull KeyboardAddOnAndBuilder addOn, @NonNull DemoAnyKeyboardView demoKeyboardView) {
        AnyKeyboard defaultKeyboard = addOn.createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        defaultKeyboard.loadKeyboard(demoKeyboardView.getThemedKeyboardDimens());
        demoKeyboardView.setKeyboard(defaultKeyboard, null, null);
    }
}
