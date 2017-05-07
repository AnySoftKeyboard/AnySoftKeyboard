package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.ui.ViewPagerWithDisable;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowView;

@RunWith(AnySoftKeyboardTestRunner.class)
public class QuickTextPagerViewTest {

    private QuickTextPagerView mUnderTest;

    @Before
    public void setup() {
        Context context = RuntimeEnvironment.application;
        mUnderTest = (QuickTextPagerView) LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.quick_text_popup_root_view, null, false);
        mUnderTest.setThemeValues(10f, new ColorStateList(new int[][]{{0}}, new int[]{Color.WHITE}),
                context.getDrawable(R.drawable.ic_cancel), context.getDrawable(R.drawable.sym_keyboard_delete_light), context.getDrawable(R.drawable.ic_action_settings),
                context.getDrawable(R.drawable.dark_background));
    }

    @Test
    public void testSetOnKeyboardActionListener() throws Exception {
        OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

        ShadowView shadowView = Shadows.shadowOf(mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_settings));
        Assert.assertNull(shadowView.getOnClickListener());
        ViewPagerWithDisable pager = (ViewPagerWithDisable) mUnderTest.findViewById(R.id.quick_text_keyboards_pager);
        Assert.assertNull(pager.getAdapter());

        mUnderTest.setOnKeyboardActionListener(listener);

        Assert.assertNotNull(shadowView.getOnClickListener());
        Assert.assertNotNull(pager.getAdapter());
    }

    @Test
    public void testPassesOnlyEnabledAddOns() throws Exception {
        final QuickTextKeyFactory quickTextKeyFactory = AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application);

        Assert.assertEquals(16, quickTextKeyFactory.getAllAddOns().size());
        Assert.assertEquals(16, quickTextKeyFactory.getEnabledAddOns().size());
        quickTextKeyFactory.setAddOnEnabled(quickTextKeyFactory.getAllAddOns().get(0).getId(), false);
        Assert.assertEquals(16, quickTextKeyFactory.getAllAddOns().size());
        Assert.assertEquals(15, quickTextKeyFactory.getEnabledAddOns().size());

        OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

        mUnderTest.setOnKeyboardActionListener(listener);
        ViewPagerWithDisable pager = (ViewPagerWithDisable) mUnderTest.findViewById(R.id.quick_text_keyboards_pager);

        Assert.assertEquals(15 + 1/*history*/, pager.getAdapter().getCount());

        quickTextKeyFactory.setAddOnEnabled(quickTextKeyFactory.getAllAddOns().get(1).getId(), false);

        mUnderTest.setOnKeyboardActionListener(listener);
        pager = (ViewPagerWithDisable) mUnderTest.findViewById(R.id.quick_text_keyboards_pager);

        Assert.assertEquals(14 + 1/*history*/, pager.getAdapter().getCount());
    }

    @Test
    public void testSetThemeValues() throws Exception {
        mUnderTest.setOnKeyboardActionListener(Mockito.mock(OnKeyboardActionListener.class));

        Assert.assertEquals(R.drawable.ic_cancel, Shadows.shadowOf(((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_close)).getDrawable()).getCreatedFromResId());
        Assert.assertEquals(R.drawable.sym_keyboard_delete_light, Shadows.shadowOf(((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_backspace)).getDrawable()).getCreatedFromResId());
        Assert.assertEquals(R.drawable.ic_action_settings, Shadows.shadowOf(((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_settings)).getDrawable()).getCreatedFromResId());
        Assert.assertEquals(R.drawable.dark_background, Shadows.shadowOf(mUnderTest.getBackground()).getCreatedFromResId());
    }

}