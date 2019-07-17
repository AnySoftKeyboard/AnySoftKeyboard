package com.anysoftkeyboard.quicktextkeys.ui;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.remote.MediaType;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.ui.ViewPagerWithDisable;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowView;

import java.util.Collections;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class QuickTextPagerViewTest {

    private QuickTextPagerView mUnderTest;
    private KeyboardTheme mKeyboardTheme;

    @Before
    public void setup() {
        Context context = getApplicationContext();
        mKeyboardTheme = AnyApplication.getKeyboardThemeFactory(context).getEnabledAddOn();
        mUnderTest = (QuickTextPagerView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.quick_text_popup_root_view, null, false);
        mUnderTest.setQuickKeyHistoryRecords(new QuickKeyHistoryRecords(AnyApplication.prefs(getApplicationContext())));
        mUnderTest.setDefaultSkinTonePrefTracker(Mockito.mock(DefaultSkinTonePrefTracker.class));
        mUnderTest.setThemeValues(mKeyboardTheme, 10f, new ColorStateList(new int[][]{{0}}, new int[]{Color.WHITE}),
                context.getDrawable(R.drawable.ic_cancel), context.getDrawable(R.drawable.sym_keyboard_delete_light), context.getDrawable(R.drawable.ic_action_settings),
                context.getDrawable(R.drawable.dark_background), context.getDrawable(R.drawable.ic_media_insertion), 10, Collections.singleton(MediaType.Image));
    }

    @Test
    public void testSetupBottomPadding() throws Exception {
        OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);
        mUnderTest.setOnKeyboardActionListener(listener);
        Assert.assertEquals(10, mUnderTest.findViewById(R.id.quick_text_actions_layout).getPaddingBottom());
    }

    @Test
    public void testShowMediaIcon() throws Exception {
        Context context = getApplicationContext();
        mUnderTest.setThemeValues(mKeyboardTheme, 10f, new ColorStateList(new int[][]{{0}}, new int[]{Color.WHITE}),
                context.getDrawable(R.drawable.ic_cancel), context.getDrawable(R.drawable.sym_keyboard_delete_light), context.getDrawable(R.drawable.ic_action_settings),
                context.getDrawable(R.drawable.dark_background), context.getDrawable(R.drawable.ic_media_insertion), 10, Collections.singleton(MediaType.Image));

        Assert.assertEquals(View.VISIBLE, mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_insert_media).getVisibility());

        mUnderTest.setThemeValues(mKeyboardTheme, 10f, new ColorStateList(new int[][]{{0}}, new int[]{Color.WHITE}),
                context.getDrawable(R.drawable.ic_cancel), context.getDrawable(R.drawable.sym_keyboard_delete_light), context.getDrawable(R.drawable.ic_action_settings),
                context.getDrawable(R.drawable.dark_background), context.getDrawable(R.drawable.ic_media_insertion), 10, Collections.emptySet());
        Assert.assertEquals(View.GONE, mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_insert_media).getVisibility());
    }

    @Test
    public void testSetOnKeyboardActionListener() throws Exception {
        OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

        ShadowView shadowView = Shadows.shadowOf((View) mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_settings));
        Assert.assertNull(shadowView.getOnClickListener());
        ViewPagerWithDisable pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);
        Assert.assertNull(pager.getAdapter());

        mUnderTest.setOnKeyboardActionListener(listener);

        Assert.assertNotNull(shadowView.getOnClickListener());
        Assert.assertNotNull(pager.getAdapter());
    }

    @Test
    public void testPassesOnlyEnabledAddOns() throws Exception {
        final QuickTextKeyFactory quickTextKeyFactory = AnyApplication.getQuickTextKeyFactory(getApplicationContext());

        Assert.assertEquals(17, quickTextKeyFactory.getAllAddOns().size());
        Assert.assertEquals(17, quickTextKeyFactory.getEnabledAddOns().size());
        quickTextKeyFactory.setAddOnEnabled(quickTextKeyFactory.getAllAddOns().get(0).getId(), false);
        Assert.assertEquals(17, quickTextKeyFactory.getAllAddOns().size());
        Assert.assertEquals(16, quickTextKeyFactory.getEnabledAddOns().size());

        OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

        mUnderTest.setOnKeyboardActionListener(listener);
        ViewPagerWithDisable pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);

        Assert.assertEquals(16 + 1/*history*/, pager.getAdapter().getCount());

        quickTextKeyFactory.setAddOnEnabled(quickTextKeyFactory.getAllAddOns().get(1).getId(), false);

        mUnderTest.setOnKeyboardActionListener(listener);
        pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);

        Assert.assertEquals(15 + 1/*history*/, pager.getAdapter().getCount());
    }

    @Test
    public void testSetThemeValues() throws Exception {
        mUnderTest.setOnKeyboardActionListener(Mockito.mock(OnKeyboardActionListener.class));

        Assert.assertEquals(R.drawable.ic_cancel, Shadows.shadowOf(((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_close)).getDrawable()).getCreatedFromResId());
        Assert.assertEquals(R.drawable.sym_keyboard_delete_light, Shadows.shadowOf(((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_backspace)).getDrawable()).getCreatedFromResId());
        Assert.assertEquals(R.drawable.ic_action_settings, Shadows.shadowOf(((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_settings)).getDrawable()).getCreatedFromResId());
        Assert.assertEquals(R.drawable.ic_media_insertion, Shadows.shadowOf(((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_insert_media)).getDrawable()).getCreatedFromResId());

        Assert.assertEquals(R.drawable.dark_background, Shadows.shadowOf(mUnderTest.getBackground()).getCreatedFromResId());
    }

}