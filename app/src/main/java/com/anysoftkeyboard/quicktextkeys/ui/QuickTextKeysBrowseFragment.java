package com.anysoftkeyboard.quicktextkeys.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.PopupListKeyboard;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.ui.settings.AbstractAddOnsBrowserFragment;
import com.anysoftkeyboard.ui.settings.QuickTextSettingsFragment;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

public class QuickTextKeysBrowseFragment extends AbstractAddOnsBrowserFragment<QuickTextKey> {

    private DefaultSkinTonePrefTracker mSkinToneTracker;

    public QuickTextKeysBrowseFragment() {
        super("QuickKey", R.string.quick_text_keys_order, false, false, true,
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSkinToneTracker = new DefaultSkinTonePrefTracker(AnyApplication.prefs(getContext()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSkinToneTracker.dispose();
    }

    @NonNull
    @Override
    protected AddOnsFactory<QuickTextKey> getAddOnFactory() {
        return AnyApplication.getQuickTextKeyFactory(getContext());
    }

    @Override
    protected void onTweaksOptionSelected() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof FragmentChauffeurActivity) {
            FragmentChauffeurActivity chauffeurActivity = (FragmentChauffeurActivity) activity;
            chauffeurActivity.addFragmentToUi(new QuickTextSettingsFragment(), TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
        }
    }

    @Override
    protected void applyAddOnToDemoKeyboardView(@NonNull QuickTextKey addOn, @NonNull DemoAnyKeyboardView demoKeyboardView) {
        AnyKeyboard keyboard;
        if (addOn.isPopupKeyboardUsed()) {
            keyboard = new AnyPopupKeyboard(addOn, getContext(), addOn.getPackageContext(), addOn.getPopupKeyboardResId(), demoKeyboardView.getThemedKeyboardDimens(), addOn.getName(), mSkinToneTracker.getDefaultSkinTone());
        } else {
            keyboard = new PopupListKeyboard(addOn, getContext(), demoKeyboardView.getThemedKeyboardDimens(), addOn.getPopupListNames(), addOn.getPopupListValues(), addOn.getName());
        }
        keyboard.loadKeyboard(demoKeyboardView.getThemedKeyboardDimens());
        demoKeyboardView.setKeyboard(keyboard, null, null);

        final int keyboardViewMaxWidth = demoKeyboardView.getThemedKeyboardDimens().getKeyboardMaxWidth();
        if (keyboard.getMinWidth() > keyboardViewMaxWidth) {
            //fixing up the keyboard, so it will fit nicely in the width
            int currentY = 0;
            int xSub = 0;
            int rowsShown = 0;
            final int maxRowsToShow = 2;
            for (Keyboard.Key key : keyboard.getKeys()) {
                key.y = currentY;
                key.x -= xSub;
                if (key.x + key.width > keyboardViewMaxWidth) {
                    if (rowsShown < maxRowsToShow) {
                        rowsShown++;
                        currentY += key.height;
                        xSub += key.x;
                        key.y = currentY;
                        key.x = 0;
                    } else {
                        break;//only showing maxRowsToShow rows
                    }
                }
            }
            keyboard.resetDimensions();
        }
    }

    @Nullable
    @Override
    protected String getMarketSearchKeyword() {
        return "quick key";
    }

    @Override
    protected int getMarketSearchTitle() {
        return R.string.search_market_for_quick_key_addons;
    }
}
