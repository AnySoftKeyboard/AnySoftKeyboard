package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardDimens;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.HistoryQuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.astuetz.PagerSlidingTabStrip;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public class QuickTextPagerView extends LinearLayout implements InputViewBinder {

    public QuickTextPagerView(Context context) {
        super(context);
    }

    public QuickTextPagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public QuickTextPagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public QuickTextPagerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @NonNull
    @Override
    public KeyboardDimens getThemedKeyboardDimens() {
        return null;
    }

    @Override
    public void onViewNotRequired() {

    }

    @Override
    public boolean setControl(boolean active) {
        return false;
    }

    @Override
    public boolean setShifted(boolean active) {
        return false;
    }

    @Override
    public boolean isShifted() {
        return false;
    }

    @Override
    public boolean setShiftLocked(boolean locked) {
        return false;
    }

    @Override
    public boolean closing() {
        return false;
    }

    @Override
    public void setKeyboard(AnyKeyboard currentKeyboard, String nextAlphabetKeyboard, String nextSymbolsKeyboard) {

    }

    @Override
    public void setOnKeyboardActionListener(OnKeyboardActionListener keyboardActionListener) {
        FrameKeyboardViewClickListener frameKeyboardViewClickListener = new FrameKeyboardViewClickListener(keyboardActionListener);
        frameKeyboardViewClickListener.registerOnViews(this);

        final Context context = getContext();
        final List<QuickTextKey> list = new ArrayList<>();
        //always starting with Recent
        final HistoryQuickTextKey historyQuickTextKey = new HistoryQuickTextKey(context);
        list.add(historyQuickTextKey);
        //then all the rest
        list.addAll(QuickTextKeyFactory.getOrderedEnabledQuickKeys(context));

        final QuickTextUserPrefs quickTextUserPrefs = new QuickTextUserPrefs(context);

        PagerAdapter adapter = new QuickKeysKeyboardPagerAdapter(context, list, new RecordHistoryKeyboardActionListener(historyQuickTextKey, keyboardActionListener));

        ViewPager.SimpleOnPageChangeListener onPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                QuickTextKey selectedKey = list.get(position);
                quickTextUserPrefs.setLastSelectedAddOnId(selectedKey.getId());
            }
        };
        int startPageIndex = quickTextUserPrefs.getStartPageIndex(list);
        ViewPager pager = (ViewPager) findViewById(R.id.quick_text_keyboards_pager);
        Resources resources = context.getResources();
        final int tabTitleTextSize = resources.getDimensionPixelSize(R.dimen.key_label_text_size);
        final ColorStateList tabTitleTextColor = ResourcesCompat.getColorStateList(resources, R.color.blacktheme_popup_keytext_color, context.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            setupSlidingTab(this, tabTitleTextSize, tabTitleTextColor, pager, adapter, onPageChangeListener, startPageIndex);
        } else {
            setupSupportTab(tabTitleTextSize, tabTitleTextColor, pager, adapter, onPageChangeListener, startPageIndex);
        }
    }

    private static void setupSupportTab(int tabTitleTextSize, ColorStateList tabTitleTextColor, ViewPager pager, PagerAdapter adapter, ViewPager.OnPageChangeListener onPageChangeListener, int startIndex) {
        PagerTabStrip pagerTabStrip = (PagerTabStrip) pager.findViewById(R.id.pager_tabs);
        pagerTabStrip.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTitleTextSize);
        pagerTabStrip.setTextColor(tabTitleTextColor.getDefaultColor());
        pagerTabStrip.setTabIndicatorColor(tabTitleTextColor.getDefaultColor());
        pager.setAdapter(adapter);
        pager.setCurrentItem(startIndex);
        pager.setOnPageChangeListener(onPageChangeListener);
    }

    private static void setupSlidingTab(View rootView, int tabTitleTextSize, ColorStateList tabTitleTextColor, ViewPager pager, PagerAdapter adapter, ViewPager.OnPageChangeListener onPageChangeListener, int startIndex) {
        PagerSlidingTabStrip pagerTabStrip = (PagerSlidingTabStrip) rootView.findViewById(R.id.pager_tabs);
        pagerTabStrip.setTextSize(tabTitleTextSize);
        pagerTabStrip.setTextColor(tabTitleTextColor.getDefaultColor());
        pagerTabStrip.setIndicatorColor(tabTitleTextColor.getDefaultColor());
        pager.setAdapter(adapter);
        pager.setCurrentItem(startIndex);
        pagerTabStrip.setViewPager(pager);
        pagerTabStrip.setOnPageChangeListener(onPageChangeListener);
    }

    @Override
    public void setKeyboardActionType(int imeOptions) {

    }

    @Override
    public void popTextOutOfKey(CharSequence wordToAnimatePopping) {

    }

    @Override
    public void revertPopTextOutOfKey() {

    }

    @Override
    public boolean dismissPopupKeyboard() {
        return false;
    }

    @Override
    public boolean handleBack() {
        return false;
    }
    /* not implementing anything below */

    @Override
    public void openUtilityKeyboard() {
    }
}
