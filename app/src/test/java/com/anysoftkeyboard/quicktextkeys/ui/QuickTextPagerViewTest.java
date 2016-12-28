package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.ImageView;

import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.ui.ViewPagerWithDisable;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowView;

@RunWith(RobolectricTestRunner.class)
public class QuickTextPagerViewTest {

    private QuickTextPagerView mUnderTest;

    @Before
    public void setup() {
        Context context = RuntimeEnvironment.application;
        mUnderTest = (QuickTextPagerView) LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.quick_text_popup_root_view, null, false);
        mUnderTest.setThemeValues(10f, new ColorStateList(new int[][]{{0}}, new int[]{Color.WHITE}), context.getDrawable(R.drawable.ic_cancel), context.getDrawable(R.drawable.sym_keyboard_delete_light), context.getDrawable(R.drawable.ic_action_settings));
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
    public void testIconsSetFromTheme() throws Exception {
        mUnderTest.setOnKeyboardActionListener(Mockito.mock(OnKeyboardActionListener.class));

        Assert.assertEquals(R.drawable.ic_cancel, Shadows.shadowOf(((ImageView)mUnderTest.findViewById(R.id.quick_keys_popup_close)).getDrawable()).getCreatedFromResId());
        Assert.assertEquals(R.drawable.sym_keyboard_delete_light, Shadows.shadowOf(((ImageView)mUnderTest.findViewById(R.id.quick_keys_popup_backspace)).getDrawable()).getCreatedFromResId());
        Assert.assertEquals(R.drawable.ic_action_settings, Shadows.shadowOf(((ImageView)mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_settings)).getDrawable()).getCreatedFromResId());
    }

}