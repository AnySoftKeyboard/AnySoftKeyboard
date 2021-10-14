package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.menny.android.anysoftkeyboard.R;

public class QuickTextViewFactory {

    public static QuickTextPagerView createQuickTextView(
            Context context,
            ViewGroup parent,
            QuickKeyHistoryRecords quickKeyHistoryRecords,
            DefaultSkinTonePrefTracker defaultSkinTonePrefTracker,
            DefaultGenderPrefTracker defaultGenderPrefTracker) {
        LayoutInflater inflater = LayoutInflater.from(context);
        QuickTextPagerView rootView =
                (QuickTextPagerView)
                        inflater.inflate(R.layout.quick_text_popup_root_view, parent, false);
        // hard setting the height - this should be the same height as the standard keyboard
        ViewGroup.LayoutParams params = rootView.getLayoutParams();
        params.height = parent.getHeight();

        rootView.setLayoutParams(params);
        rootView.setQuickKeyHistoryRecords(quickKeyHistoryRecords);
        rootView.setEmojiVariantsPrefTrackers(defaultSkinTonePrefTracker, defaultGenderPrefTracker);

        return rootView;
    }
}
