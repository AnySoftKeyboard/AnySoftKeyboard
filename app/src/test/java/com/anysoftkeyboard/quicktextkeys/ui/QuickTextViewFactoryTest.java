package com.anysoftkeyboard.quicktextkeys.ui;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class QuickTextViewFactoryTest {
    @Test
    public void testCreateQuickTextView() throws Exception {
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        QuickTextPagerView view = QuickTextViewFactory.createQuickTextView(RuntimeEnvironment.application, linearLayout, 25,
                new QuickKeyHistoryRecords(AnyApplication.prefs(RuntimeEnvironment.application)));

        Assert.assertNotNull(view);

        Assert.assertEquals(25, view.getLayoutParams().height);
        Assert.assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, view.getLayoutParams().width);
    }

}