package com.anysoftkeyboard.quicktextkeys.ui;

import android.preference.PreferenceManager;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AnySoftKeyboardTestRunner.class)
public class QuickTextViewFactoryTest {
    @Test
    public void testCreateQuickTextView() throws Exception {
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        QuickTextPagerView view = QuickTextViewFactory.createQuickTextView(RuntimeEnvironment.application, linearLayout, 25,
                new QuickKeyHistoryRecords(PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)));

        Assert.assertNotNull(view);

        Assert.assertEquals(25, view.getLayoutParams().height);
        Assert.assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, view.getLayoutParams().width);
    }

}