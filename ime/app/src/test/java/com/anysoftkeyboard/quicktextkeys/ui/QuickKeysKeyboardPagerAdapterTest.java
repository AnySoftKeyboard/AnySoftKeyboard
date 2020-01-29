package com.anysoftkeyboard.quicktextkeys.ui;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewWithMiniKeyboard;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.keyboards.views.QuickKeysKeyboardView;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.ui.ScrollViewWithDisable;
import com.anysoftkeyboard.ui.ViewPagerWithDisable;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowView;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class QuickKeysKeyboardPagerAdapterTest {

    private ViewPagerWithDisable mViewPager;
    private QuickKeysKeyboardPagerAdapter mUnderTest;
    private List<QuickTextKey> mOrderedEnabledQuickKeys;
    private OnKeyboardActionListener mKeyboardListener;
    private DefaultSkinTonePrefTracker mSkinTonePrefTracker;

    @Before
    public void setup() {
        mViewPager = Mockito.mock(ViewPagerWithDisable.class);
        mOrderedEnabledQuickKeys =
                AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOns();
        mKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
        mSkinTonePrefTracker = Mockito.mock(DefaultSkinTonePrefTracker.class);
        mUnderTest =
                new QuickKeysKeyboardPagerAdapter(
                        getApplicationContext(),
                        mViewPager,
                        mOrderedEnabledQuickKeys,
                        mKeyboardListener,
                        mSkinTonePrefTracker,
                        AnyApplication.getKeyboardThemeFactory(getApplicationContext())
                                .getEnabledAddOn(),
                        11);
    }

    @Test
    public void testGetCount() throws Exception {
        Assert.assertEquals(mOrderedEnabledQuickKeys.size(), mUnderTest.getCount());
    }

    @Test
    public void testDestroyItem() {
        ViewGroup container = Mockito.mock(ViewGroup.class);
        View child = Mockito.mock(View.class);
        mUnderTest.destroyItem(container, 0, child);
        Mockito.verify(container).removeView(child);
    }

    @Test
    public void testInstantiateItem() throws Exception {
        ViewGroup container = new LinearLayout(getApplicationContext());
        Object instance0 = mUnderTest.instantiateItem(container, 0);
        Assert.assertNotNull(instance0);
        Assert.assertTrue(instance0 instanceof ScrollViewWithDisable);
        Assert.assertEquals(1, container.getChildCount());
        Assert.assertSame(instance0, container.getChildAt(0));
        //noinspection ResultOfMethodCallIgnored
        Mockito.verify(mSkinTonePrefTracker).getDefaultSkinTone();

        final QuickKeysKeyboardView keyboardView0 =
                ((View) instance0).findViewById(R.id.keys_container);
        Assert.assertNotNull(keyboardView0);

        Object instance1 = mUnderTest.instantiateItem(container, 1);
        Assert.assertNotNull(instance1);
        Assert.assertNotSame(instance0, instance1);
        final QuickKeysKeyboardView keyboardView1 =
                ((View) instance1).findViewById(R.id.keys_container);
        Assert.assertNotNull(keyboardView1);

        Assert.assertNotEquals(
                keyboardView0.getKeyboard().getKeyboardAddOn().getId(),
                keyboardView1.getKeyboard().getKeyboardAddOn().getId());

        Object instance0Again = mUnderTest.instantiateItem(container, 0);
        Assert.assertNotNull(instance0Again);
        Assert.assertNotSame(instance0, instance0Again);
        final QuickKeysKeyboardView keyboardView0Again =
                ((View) instance0Again).findViewById(R.id.keys_container);
        Assert.assertNotNull(keyboardView0Again);
        Assert.assertSame(keyboardView0.getKeyboard(), keyboardView0Again.getKeyboard());
        // making sure the keyboard DOES NOT have a background - this is because we want the
        // background to be used in the pager container.
        Assert.assertNull(keyboardView0.getBackground());
        Assert.assertNull(null, keyboardView1.getBackground());

        // adds padding
        Assert.assertEquals(11, ((View) instance0).getPaddingBottom());
    }

    @Test
    public void testKeyboardWillDraw() throws Exception {
        final ViewGroup container = new LinearLayout(getApplicationContext());

        for (int keyboardIndex = 0; keyboardIndex < mUnderTest.getCount(); keyboardIndex++) {
            final QuickKeysKeyboardView keyboardView =
                    ((View) mUnderTest.instantiateItem(container, keyboardIndex))
                            .findViewById(R.id.keys_container);
            Assert.assertNotNull(keyboardView);
            Assert.assertNotNull(keyboardView.getKeyboard());
            Assert.assertFalse(keyboardView.willNotDraw());
        }
    }

    @Test
    @Config(shadows = ShadowAnyKeyboardViewWithMiniKeyboard.class)
    public void testPopupListenerDisable() throws Exception {
        ViewGroup container = new LinearLayout(getApplicationContext());
        Object instance0 = mUnderTest.instantiateItem(container, 0);
        final QuickKeysKeyboardView keyboardView0 =
                ((View) instance0).findViewById(R.id.keys_container);

        ShadowAnyKeyboardViewWithMiniKeyboard shadow = Shadow.extract(keyboardView0);
        Assert.assertNotNull(shadow.mPopupShownListener);

        Mockito.verify(mViewPager, Mockito.never()).setEnabled(Mockito.anyBoolean());

        shadow.mPopupShownListener.onPopupKeyboardShowingChanged(true);

        Mockito.verify(mViewPager).setEnabled(false);
        Mockito.verifyNoMoreInteractions(mViewPager);

        Mockito.reset(mViewPager);

        shadow.mPopupShownListener.onPopupKeyboardShowingChanged(false);

        Mockito.verify(mViewPager).setEnabled(true);
        Mockito.verifyNoMoreInteractions(mViewPager);
    }

    @Implements(AnyKeyboardViewWithMiniKeyboard.class)
    public static class ShadowAnyKeyboardViewWithMiniKeyboard extends ShadowView {

        private AnyKeyboardViewWithMiniKeyboard.OnPopupShownListener mPopupShownListener;

        public ShadowAnyKeyboardViewWithMiniKeyboard() {}

        @Implementation
        public void setOnPopupShownListener(
                AnyKeyboardViewWithMiniKeyboard.OnPopupShownListener listener) {
            mPopupShownListener = listener;
        }
    }
}
