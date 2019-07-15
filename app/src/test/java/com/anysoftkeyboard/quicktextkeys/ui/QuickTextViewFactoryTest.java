package com.anysoftkeyboard.quicktextkeys.ui;

import static com.menny.android.anysoftkeyboard.AnyApplication.prefs;

import static org.mockito.Mockito.mock;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class QuickTextViewFactoryTest {
    @Test
    public void testCreateQuickTextView() throws Exception {
        LinearLayout linearLayout = new LinearLayout(getApplicationContext());
        QuickTextPagerView view = QuickTextViewFactory.createQuickTextView(getApplicationContext(), linearLayout, 25,
                new QuickKeyHistoryRecords(prefs(getApplicationContext())), mock(DefaultSkinTonePrefTracker.class));

        Assert.assertNotNull(view);

        Assert.assertEquals(25, view.getLayoutParams().height);
        Assert.assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, view.getLayoutParams().width);
    }
}