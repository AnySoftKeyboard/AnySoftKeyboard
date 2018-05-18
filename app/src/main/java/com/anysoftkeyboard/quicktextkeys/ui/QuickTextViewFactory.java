package com.anysoftkeyboard.quicktextkeys.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.menny.android.anysoftkeyboard.R;

public class QuickTextViewFactory {

    public static QuickTextPagerView createQuickTextView(Context context, ViewGroup parent, int height,
            QuickKeyHistoryRecords quickKeyHistoryRecords, DefaultSkinTonePrefTracker defaultSkinTonePrefTracker) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("InflateParams") QuickTextPagerView rootView = (QuickTextPagerView) inflater.inflate(R.layout.quick_text_popup_root_view, parent, false);
        //hard setting the height - this should be the same height as the standard keyboard
        ViewGroup.LayoutParams params = rootView.getLayoutParams();
        params.height = height;
        rootView.setLayoutParams(params);
        rootView.setQuickKeyHistoryRecords(quickKeyHistoryRecords);
        rootView.setDefaultSkinTonePrefTracker(defaultSkinTonePrefTracker);

        return rootView;
    }
}
