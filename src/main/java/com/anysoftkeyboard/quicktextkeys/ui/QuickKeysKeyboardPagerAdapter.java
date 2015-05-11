package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.PopupListKeyboard;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.keyboards.views.QuickKeysKeyboardView;
import com.anysoftkeyboard.quicktextkeys.HistoryQuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

public class QuickKeysKeyboardPagerAdapter extends PagerAdapter {

    @NonNull
    private final Context mContext;
    @NonNull
    private final OnKeyboardActionListener mKeyboardActionListener;
    @NonNull
    private final LayoutInflater mLayoutInflater;
    @NonNull
    private final AnyPopupKeyboard[] mPopupKeyboards;
    @NonNull
    private final boolean[] mIsAutoFitKeyboards;
    @NonNull
    private final QuickTextKey[] mAddOns;
    private final int mDecorationWidthSize;

    public QuickKeysKeyboardPagerAdapter(@NonNull Context context, @NonNull List<QuickTextKey> keyAddOns, @NonNull OnKeyboardActionListener keyboardActionListener, int decorationWidthSize) {
        mContext = context;
        mKeyboardActionListener = keyboardActionListener;
        mDecorationWidthSize = decorationWidthSize;
        mAddOns = keyAddOns.toArray(new QuickTextKey[keyAddOns.size()]);
        mPopupKeyboards = new AnyPopupKeyboard[mAddOns.length];
        mIsAutoFitKeyboards = new boolean[mAddOns.length];
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mPopupKeyboards.length;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View root = mLayoutInflater.inflate(R.layout.quick_text_popup_autorowkeyboard_view, container, false);
        container.addView(root);

        final QuickKeysKeyboardView keyboardView = (QuickKeysKeyboardView) root.findViewById(R.id.keys_container);
        keyboardView.setExternalDecorationHorizontalSize(mDecorationWidthSize);
        keyboardView.setOnKeyboardActionListener(mKeyboardActionListener);
        QuickTextKey addOn = mAddOns[position];
        AnyPopupKeyboard keyboard = mPopupKeyboards[position];
        if (keyboard == null) {
            if (addOn.isPopupKeyboardUsed()) {
                keyboard = new AnyPopupKeyboard(mContext, addOn.getPackageContext(), addOn.getPopupKeyboardResId(), keyboardView.getThemedKeyboardDimens(), addOn.getName());
            } else {
                keyboard = new PopupListKeyboard(mContext, keyboardView.getThemedKeyboardDimens(), addOn.getPopupListNames(), addOn.getPopupListValues(), addOn.getName());
            }
            mPopupKeyboards[position] = keyboard;
            final int keyboardViewMaxWidth = keyboardView.getThemedKeyboardDimens().getKeyboardMaxWidth();
            mIsAutoFitKeyboards[position] = keyboard.getMinWidth() > keyboardViewMaxWidth || addOn instanceof HistoryQuickTextKey;
            if (mIsAutoFitKeyboards[position]) {
                //fixing up the keyboard, so it will fit nicely in the width
                int currentY = 0;
                int xSub = 0;
                for (Keyboard.Key key : keyboard.getKeys()) {
                    key.y = currentY;
                    key.x -= xSub;
                    if (key.x + key.width > keyboardViewMaxWidth) {
                        currentY += key.height;
                        xSub += key.x;
                        key.y = currentY;
                        key.x = 0;
                    }
                }
                keyboard.resetDimensions();
            }
        }
        keyboardView.setKeyboard(keyboard, mIsAutoFitKeyboards[position]);
        return root;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        QuickTextKey key = mAddOns[position];
        return mContext.getResources().getString(R.string.quick_text_tab_title_template, key.getKeyOutputText(), key.getName());
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
