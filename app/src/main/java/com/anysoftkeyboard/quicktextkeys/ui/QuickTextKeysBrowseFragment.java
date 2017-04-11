package com.anysoftkeyboard.quicktextkeys.ui;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyPopupKeyboard;
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

    public QuickTextKeysBrowseFragment() {
        super("QuickKey", R.string.quick_text_keys_order, false, false, true);
    }

    @NonNull
    @Override
    protected AddOnsFactory<QuickTextKey> getAddOnFactory() {
        return AnyApplication.getQuickTextKeyFactory(getContext());
    }

    @Override
    protected int getItemDragDirectionFlags() {
        return ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
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
            keyboard = new AnyPopupKeyboard(addOn, getContext(), addOn.getPackageContext(), addOn.getPopupKeyboardResId(), demoKeyboardView.getThemedKeyboardDimens(), addOn.getName());
        } else {
            keyboard = new PopupListKeyboard(addOn, getContext(), demoKeyboardView.getThemedKeyboardDimens(), addOn.getPopupListNames(), addOn.getPopupListValues(), addOn.getName());
        }
        keyboard.loadKeyboard(demoKeyboardView.getThemedKeyboardDimens());
        demoKeyboardView.setKeyboard(keyboard, null, null);
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
