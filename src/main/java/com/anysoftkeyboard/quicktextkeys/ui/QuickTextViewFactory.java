package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;

import com.anysoftkeyboard.keyboards.views.MiniKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.HistoryQuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.astuetz.PagerSlidingTabStrip;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public class QuickTextViewFactory {

    public static View createQuickTextView(final Context context, final MiniKeyboardActionListener keyboardActionListener, int tabTitleTextSize, ColorStateList tabTitleTextColor) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.quick_text_popup_root_view, null, false);
        FrameKeyboardViewClickListener frameKeyboardViewClickListener = new FrameKeyboardViewClickListener(keyboardActionListener);
        frameKeyboardViewClickListener.registerOnViews(rootView);
        final List<QuickTextKey> list = new ArrayList<>();
        //always starting with Recent
        final HistoryQuickTextKey historyQuickTextKey = new HistoryQuickTextKey(context);
        list.add(historyQuickTextKey);
        //then all the rest
        list.addAll(QuickTextKeyFactory.getOrderedEnabledQuickKeys(context));

        final QuickTextUserPrefs quickTextUserPrefs = new QuickTextUserPrefs(context);

        keyboardActionListener.setInOneShot(quickTextUserPrefs.isOneShotQuickTextPopup());

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
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.quick_text_keyboards_pager);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
            setupSlidingTab(rootView, tabTitleTextSize, tabTitleTextColor, pager, adapter, onPageChangeListener, startPageIndex);
        } else {
            setupSupportTab(tabTitleTextSize, tabTitleTextColor, pager, adapter, onPageChangeListener, startPageIndex);
        }

        return rootView;
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

    protected static void setupSlidingTab(View rootView, int tabTitleTextSize, ColorStateList tabTitleTextColor, ViewPager pager, PagerAdapter adapter, ViewPager.OnPageChangeListener onPageChangeListener, int startIndex) {
        PagerSlidingTabStrip pagerTabStrip = (PagerSlidingTabStrip) rootView.findViewById(R.id.pager_tabs);
        pagerTabStrip.setTextSize(tabTitleTextSize);
        pagerTabStrip.setTextColor(tabTitleTextColor.getDefaultColor());
        pagerTabStrip.setIndicatorColor(tabTitleTextColor.getDefaultColor());
        pager.setAdapter(adapter);
        pager.setCurrentItem(startIndex);
        pagerTabStrip.setViewPager(pager);
        pagerTabStrip.setOnPageChangeListener(onPageChangeListener);
    }
}
