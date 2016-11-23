package com.anysoftkeyboard.quicktextkeys.ui;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class QuickTextViewFactoryTest {
    @Test
    public void testCreateQuickTextView() throws Exception {
        LinearLayout linearLayout = new LinearLayout(RuntimeEnvironment.application);
        QuickTextPagerView view = QuickTextViewFactory.createQuickTextView(RuntimeEnvironment.application, linearLayout, 25);

        Assert.assertNotNull(view);

        Assert.assertEquals(25, view.getLayoutParams().height);
        Assert.assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, view.getLayoutParams().width);
    }

}