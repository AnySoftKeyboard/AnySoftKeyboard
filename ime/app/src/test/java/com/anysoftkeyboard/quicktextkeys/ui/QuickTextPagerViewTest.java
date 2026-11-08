package com.anysoftkeyboard.quicktextkeys.ui;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.remote.MediaType;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.Collections;
import net.evendanan.pixel.ViewPagerWithDisable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowView;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class QuickTextPagerViewTest {

  private QuickTextPagerView mUnderTest;
  private KeyboardTheme mKeyboardTheme;

  @Before
  public void setup() {
    Context context = getApplicationContext();
    mKeyboardTheme = AnyApplication.getKeyboardThemeFactory(context).getEnabledAddOn();
    mUnderTest =
        (QuickTextPagerView)
            LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.quick_text_popup_root_view, null, false);
    mUnderTest.setQuickKeyHistoryRecords(
        new QuickKeyHistoryRecords(AnyApplication.prefs(getApplicationContext())));
    mUnderTest.setEmojiVariantsPrefTrackers(
        Mockito.mock(DefaultSkinTonePrefTracker.class),
        Mockito.mock(DefaultGenderPrefTracker.class));
    mUnderTest.setThemeValues(
        mKeyboardTheme,
        10f,
        ColorStateList.valueOf(Color.WHITE),
        context.getDrawable(R.drawable.ic_cancel),
        context.getDrawable(R.drawable.sym_keyboard_delete_light),
        context.getDrawable(R.drawable.ic_action_settings),
        context.getDrawable(R.drawable.dark_background),
        context.getDrawable(R.drawable.ic_media_insertion),
        context.getDrawable(R.drawable.ic_delete_forever_dark),
        10,
        Collections.singleton(MediaType.Image));
  }

  @Test
  public void testSetupBottomPadding() throws Exception {
    OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);
    mUnderTest.setOnKeyboardActionListener(listener);
    Assert.assertEquals(
        10, mUnderTest.findViewById(R.id.quick_text_actions_layout).getPaddingBottom());
  }

  @Test
  public void testShowMediaIcon() throws Exception {
    Context context = getApplicationContext();
    mUnderTest.setThemeValues(
        mKeyboardTheme,
        10f,
        ColorStateList.valueOf(Color.WHITE),
        context.getDrawable(R.drawable.ic_cancel),
        context.getDrawable(R.drawable.sym_keyboard_delete_light),
        context.getDrawable(R.drawable.ic_action_settings),
        context.getDrawable(R.drawable.dark_background),
        context.getDrawable(R.drawable.ic_media_insertion),
        context.getDrawable(R.drawable.ic_delete_forever_dark),
        10,
        Collections.singleton(MediaType.Image));

    Assert.assertEquals(
        View.VISIBLE,
        mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_insert_media).getVisibility());

    mUnderTest.setThemeValues(
        mKeyboardTheme,
        10f,
        ColorStateList.valueOf(Color.WHITE),
        context.getDrawable(R.drawable.ic_cancel),
        context.getDrawable(R.drawable.sym_keyboard_delete_light),
        context.getDrawable(R.drawable.ic_action_settings),
        context.getDrawable(R.drawable.dark_background),
        context.getDrawable(R.drawable.ic_media_insertion),
        context.getDrawable(R.drawable.ic_delete_forever_dark),
        10,
        Collections.emptySet());
    Assert.assertEquals(
        View.GONE,
        mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_insert_media).getVisibility());
  }

  @Test
  public void testShowClearEmojiOnlyOnHistory() throws Exception {
    // setting up the listener since that is what sets up the adapter
    OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);
    mUnderTest.setOnKeyboardActionListener(listener);

    ViewPagerWithDisable pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);
    Assert.assertNotNull(pager);
    pager.setCurrentItem(0, false);
    TestRxSchedulers.foregroundFlushAllJobs();
    Assert.assertEquals(0, pager.getCurrentItem());

    Assert.assertEquals(
        View.VISIBLE,
        mUnderTest
            .findViewById(R.id.quick_keys_popup_delete_recently_used_smileys)
            .getVisibility());

    pager.setCurrentItem(1, false);
    TestRxSchedulers.foregroundFlushAllJobs();
    Assert.assertEquals(1, pager.getCurrentItem());

    Assert.assertEquals(
        View.GONE,
        mUnderTest
            .findViewById(R.id.quick_keys_popup_delete_recently_used_smileys)
            .getVisibility());

    pager.setCurrentItem(0);
    TestRxSchedulers.foregroundFlushAllJobs();
    Assert.assertEquals(0, pager.getCurrentItem());

    Assert.assertEquals(
        View.VISIBLE,
        mUnderTest
            .findViewById(R.id.quick_keys_popup_delete_recently_used_smileys)
            .getVisibility());
  }

  @Test
  public void testSetOnKeyboardActionListener() throws Exception {
    OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

    ShadowView shadowView =
        Shadows.shadowOf((View) mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_settings));
    Assert.assertNull(shadowView.getOnClickListener());
    ViewPagerWithDisable pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);
    Assert.assertNull(pager.getAdapter());

    mUnderTest.setOnKeyboardActionListener(listener);

    Assert.assertNotNull(shadowView.getOnClickListener());
    Assert.assertNotNull(pager.getAdapter());
  }

  @Test
  public void testPassesOnlyEnabledAddOns() throws Exception {
    final QuickTextKeyFactory quickTextKeyFactory =
        AnyApplication.getQuickTextKeyFactory(getApplicationContext());

    Assert.assertEquals(19, quickTextKeyFactory.getAllAddOns().size());
    Assert.assertEquals(19, quickTextKeyFactory.getEnabledAddOns().size());
    quickTextKeyFactory.setAddOnEnabled(quickTextKeyFactory.getAllAddOns().get(0).getId(), false);
    Assert.assertEquals(19, quickTextKeyFactory.getAllAddOns().size());
    Assert.assertEquals(18, quickTextKeyFactory.getEnabledAddOns().size());

    OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

    mUnderTest.setOnKeyboardActionListener(listener);
    ViewPagerWithDisable pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);

    Assert.assertEquals(18 + 1 /* history */, pager.getAdapter().getCount());

    quickTextKeyFactory.setAddOnEnabled(quickTextKeyFactory.getAllAddOns().get(1).getId(), false);

    mUnderTest.setOnKeyboardActionListener(listener);
    pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);

    Assert.assertEquals(17 + 1 /* history */, pager.getAdapter().getCount());
  }

  @Test
  public void testSetThemeValues() throws Exception {
    mUnderTest.setOnKeyboardActionListener(Mockito.mock(OnKeyboardActionListener.class));

    Assert.assertEquals(
        R.drawable.ic_cancel,
        Shadows.shadowOf(
                ((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_close)).getDrawable())
            .getCreatedFromResId());
    Assert.assertEquals(
        R.drawable.sym_keyboard_delete_light,
        Shadows.shadowOf(
                ((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_backspace))
                    .getDrawable())
            .getCreatedFromResId());
    Assert.assertEquals(
        R.drawable.ic_action_settings,
        Shadows.shadowOf(
                ((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_settings))
                    .getDrawable())
            .getCreatedFromResId());
    Assert.assertEquals(
        R.drawable.ic_media_insertion,
        Shadows.shadowOf(
                ((ImageView) mUnderTest.findViewById(R.id.quick_keys_popup_quick_keys_insert_media))
                    .getDrawable())
            .getCreatedFromResId());

    Assert.assertEquals(
        R.drawable.dark_background,
        Shadows.shadowOf(mUnderTest.getBackground()).getCreatedFromResId());
  }

  @Test
  public void testSetThemeOverlay_forwardsOverlayToPagerAdapter() throws Exception {
    mUnderTest.setOnKeyboardActionListener(Mockito.mock(OnKeyboardActionListener.class));
    com.anysoftkeyboard.overlay.OverlayData overlayData =
        new com.anysoftkeyboard.overlay.OverlayDataImpl(
            Color.WHITE, Color.BLACK, Color.BLUE, Color.BLACK, Color.GRAY);
    mUnderTest.setThemeOverlay(overlayData);
    ViewPagerWithDisable pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);
    Assert.assertNotNull(pager.getAdapter());
  }

  @Test
  public void
      testSetThemeOverlay_normalizesAgainstPrimaryDarkColor_andAppliesToPagerSlidingTabStrip()
          throws Exception {
    mUnderTest.setOnKeyboardActionListener(Mockito.mock(OnKeyboardActionListener.class));
    com.anysoftkeyboard.overlay.OverlayData overlayData =
        new com.anysoftkeyboard.overlay.OverlayDataImpl(
            Color.WHITE, Color.BLACK, Color.BLUE, Color.BLACK, Color.BLACK);
    mUnderTest.setThemeOverlay(overlayData);

    com.astuetz.PagerSlidingTabStrip pagerTabStrip = mUnderTest.findViewById(R.id.pager_tabs);
    Assert.assertEquals(Color.WHITE, pagerTabStrip.getTextColor().getDefaultColor());
    Assert.assertEquals(Color.WHITE, pagerTabStrip.getIndicatorColor());
  }

  @Test
  public void testSetThemeOverlay_invalidOverlayData_doesNotApplyNormalizedColors()
      throws Exception {
    mUnderTest.setOnKeyboardActionListener(Mockito.mock(OnKeyboardActionListener.class));
    com.astuetz.PagerSlidingTabStrip pagerTabStrip = mUnderTest.findViewById(R.id.pager_tabs);
    int initialTextColor = pagerTabStrip.getTextColor().getDefaultColor();

    com.anysoftkeyboard.overlay.OverlayData invalidOverlay =
        new com.anysoftkeyboard.overlay.OverlayDataImpl();
    mUnderTest.setThemeOverlay(invalidOverlay);
    Assert.assertEquals(initialTextColor, pagerTabStrip.getTextColor().getDefaultColor());
  }

  @Test
  public void
      testSetThemeOverlay_lightPrimaryColor_darkPrimaryDarkColor_normalizesAgainstPrimaryDarkColor()
          throws Exception {
    mUnderTest.setOnKeyboardActionListener(Mockito.mock(OnKeyboardActionListener.class));
    // primaryColor is WHITE (light), primaryDarkColor is BLACK (dark), secondaryTextColor is BLACK
    com.anysoftkeyboard.overlay.OverlayData overlayData =
        new com.anysoftkeyboard.overlay.OverlayDataImpl(
            Color.WHITE, Color.BLACK, Color.BLUE, Color.BLACK, Color.BLACK);
    mUnderTest.setThemeOverlay(overlayData);

    com.astuetz.PagerSlidingTabStrip pagerTabStrip = mUnderTest.findViewById(R.id.pager_tabs);
    // Normalized against primaryDarkColor (BLACK), text color must be WHITE (not BLACK)
    Assert.assertEquals(Color.WHITE, pagerTabStrip.getTextColor().getDefaultColor());
  }

  @Test
  public void testEnsureContrastColor_keepsGoodContrast() {
    Drawable bg = new ColorDrawable(Color.BLACK);
    ColorStateList keyTextColor = ColorStateList.valueOf(Color.WHITE);
    ColorStateList result = QuickTextPagerView.ensureContrastColor(keyTextColor, Color.YELLOW, bg);
    Assert.assertEquals(Color.WHITE, result.getDefaultColor());
  }

  @Test
  public void testEnsureContrastColor_fallsBackToNameTextColor() {
    Drawable bg = new ColorDrawable(Color.BLACK);
    ColorStateList keyTextColor = ColorStateList.valueOf(Color.DKGRAY);
    ColorStateList result = QuickTextPagerView.ensureContrastColor(keyTextColor, Color.WHITE, bg);
    Assert.assertEquals(Color.WHITE, result.getDefaultColor());
  }

  @Test
  public void testEnsureContrastColor_fallsBackToAutoBlackOrWhite() {
    Drawable darkBg = new ColorDrawable(Color.BLACK);
    ColorStateList darkKeyText = ColorStateList.valueOf(Color.DKGRAY);
    ColorStateList darkResult =
        QuickTextPagerView.ensureContrastColor(darkKeyText, Color.DKGRAY, darkBg);
    Assert.assertEquals(Color.WHITE, darkResult.getDefaultColor());

    Drawable lightBg = new ColorDrawable(Color.WHITE);
    ColorStateList lightKeyText = ColorStateList.valueOf(Color.LTGRAY);
    ColorStateList lightResult =
        QuickTextPagerView.ensureContrastColor(lightKeyText, Color.LTGRAY, lightBg);
    Assert.assertEquals(Color.BLACK, lightResult.getDefaultColor());
  }

  @Test
  public void testExtractDrawableColor() {
    Assert.assertEquals(Color.BLACK, QuickTextPagerView.extractDrawableColor(null));
    Assert.assertEquals(
        Color.RED, QuickTextPagerView.extractDrawableColor(new ColorDrawable(Color.RED)));
    Assert.assertEquals(
        Color.BLACK,
        QuickTextPagerView.extractDrawableColor(new ColorDrawable(Color.argb(10, 255, 0, 0))));

    Rect nonColorBounds = new Rect(1, 2, 3, 4);
    Drawable nonColorDrawable =
        new Drawable() {
          @Override
          public void draw(@NonNull Canvas canvas) {}

          @Override
          public void setAlpha(int alpha) {}

          @Override
          public void setColorFilter(@Nullable ColorFilter colorFilter) {}

          @Override
          public int getOpacity() {
            return PixelFormat.OPAQUE;
          }
        };
    nonColorDrawable.setBounds(nonColorBounds);
    Assert.assertEquals(Color.BLACK, QuickTextPagerView.extractDrawableColor(nonColorDrawable));
    Assert.assertEquals(nonColorBounds, nonColorDrawable.getBounds());

    Rect originalBounds = new Rect(10, 20, 30, 40);
    Drawable throwingDrawable =
        new Drawable() {
          @Override
          public void draw(@NonNull Canvas canvas) {
            throw new RuntimeException("Simulated draw failure");
          }

          @Override
          public void setAlpha(int alpha) {}

          @Override
          public void setColorFilter(@Nullable ColorFilter colorFilter) {}

          @Override
          public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
          }
        };
    throwingDrawable.setBounds(originalBounds);
    Assert.assertEquals(Color.BLACK, QuickTextPagerView.extractDrawableColor(throwingDrawable));
    Assert.assertEquals(originalBounds, throwingDrawable.getBounds());
  }

  @Test
  public void testSetThemeValues_updatesAdapterKeyTextColorWhenAdapterNotNull() {
    OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);
    mUnderTest.setOnKeyboardActionListener(listener);
    Context context = getApplicationContext();
    mUnderTest.setThemeValues(
        mKeyboardTheme,
        12f,
        ColorStateList.valueOf(Color.GREEN),
        context.getDrawable(R.drawable.ic_cancel),
        context.getDrawable(R.drawable.sym_keyboard_delete_light),
        context.getDrawable(R.drawable.ic_action_settings),
        new ColorDrawable(Color.BLACK),
        context.getDrawable(R.drawable.ic_media_insertion),
        context.getDrawable(R.drawable.ic_delete_forever_dark),
        10,
        Collections.singleton(MediaType.Image));
    ViewPagerWithDisable pager = mUnderTest.findViewById(R.id.quick_text_keyboards_pager);
    Assert.assertNotNull(pager.getAdapter());
  }

  @Test
  public void testThemeOverlayNormalizedApplied() {
    OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);
    mUnderTest.setOnKeyboardActionListener(listener);

    OverlayData overlayData = Mockito.mock(OverlayData.class);
    Mockito.doReturn(true).when(overlayData).isValid();
    Mockito.doReturn(Color.BLACK).when(overlayData).getPrimaryDarkColor();
    Mockito.doReturn(Color.BLACK).when(overlayData).getSecondaryTextColor();
    Mockito.doReturn(Color.BLACK).when(overlayData).getPrimaryColor();
    Mockito.doReturn(Color.BLACK).when(overlayData).getPrimaryTextColor();

    mUnderTest.setThemeOverlay(overlayData);

    com.astuetz.PagerSlidingTabStrip pagerTabStrip = mUnderTest.findViewById(R.id.pager_tabs);
    Assert.assertNotNull(pagerTabStrip);
    // OverlayDataNormalizer will normalize black text on black background to WHITE
    Assert.assertEquals(Color.WHITE, pagerTabStrip.getTextColor().getDefaultColor());
  }
}
