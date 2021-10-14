package com.anysoftkeyboard.quicktextkeys.ui;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.menny.android.anysoftkeyboard.AnyApplication.prefs;
import static org.mockito.Mockito.mock;

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
        QuickTextPagerView view =
                QuickTextViewFactory.createQuickTextView(
                        getApplicationContext(),
                        linearLayout,
                        new QuickKeyHistoryRecords(prefs(getApplicationContext())),
                        mock(DefaultSkinTonePrefTracker.class),
                        mock(DefaultGenderPrefTracker.class));

        Assert.assertNotNull(view);

        Assert.assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, view.getLayoutParams().width);
    }
}
