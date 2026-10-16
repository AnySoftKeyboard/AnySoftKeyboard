package com.anysoftkeyboard.quicktextkeys.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.anysoftkeyboard.ime.InputViewActionsProvider;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.keyboards.views.ThemeableChild;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlayDataImpl;
import com.anysoftkeyboard.overlay.OverlayDataNormalizer;
import com.anysoftkeyboard.quicktextkeys.HistoryQuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.remote.MediaType;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.astuetz.PagerSlidingTabStrip;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.evendanan.pixel.ViewPagerWithDisable;

public class QuickTextPagerView extends LinearLayout
    implements InputViewActionsProvider, ThemeableChild {

  private KeyboardTheme mKeyboardTheme;
  @NonNull private OverlayData mOverlayData = new OverlayDataImpl();
  private QuickKeysKeyboardPagerAdapter mPagerAdapter;
  private float mTabTitleTextSize;
  private ColorStateList mTabTitleTextColor;
  private Drawable mCloseKeyboardIcon;
  private Drawable mBackspaceIcon;
  private Drawable mSettingsIcon;
  private Drawable mMediaInsertionDrawable;
  private Drawable mDeleteRecentlyUsedDrawable;
  private int mBottomPadding;
  private QuickKeyHistoryRecords mQuickKeyHistoryRecords;
  private DefaultSkinTonePrefTracker mDefaultSkinTonePrefTracker;
  private DefaultGenderPrefTracker mDefaultGenderPrefTracker;

  public QuickTextPagerView(Context context) {
    super(context);
  }

  public QuickTextPagerView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public QuickTextPagerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  private static void setupSlidingTab(
      View rootView,
      float tabTitleTextSize,
      ColorStateList tabTitleTextColor,
      ViewPager pager,
      PagerAdapter adapter,
      ViewPager.OnPageChangeListener onPageChangeListener,
      int startIndex) {
    PagerSlidingTabStrip pagerTabStrip = rootView.findViewById(R.id.pager_tabs);
    pagerTabStrip.setTextSize((int) tabTitleTextSize);
    pagerTabStrip.setTextColor(tabTitleTextColor.getDefaultColor());
    pagerTabStrip.setIndicatorColor(tabTitleTextColor.getDefaultColor());
    pager.setAdapter(adapter);
    pager.setCurrentItem(startIndex);
    pagerTabStrip.setViewPager(pager);
    pagerTabStrip.setOnPageChangeListener(onPageChangeListener);
  }

  public void setThemeValues(
      @NonNull KeyboardTheme keyboardTheme,
      float tabTextSize,
      ColorStateList tabTextColor,
      Drawable closeKeyboardIcon,
      Drawable backspaceIcon,
      Drawable settingsIcon,
      Drawable keyboardDrawable,
      Drawable mediaInsertionDrawable,
      Drawable deleteRecentlyUsedDrawable,
      int bottomPadding,
      Set<MediaType> supportedMediaTypes) {
    setThemeValues(
        keyboardTheme,
        tabTextSize,
        tabTextColor,
        tabTextColor.getDefaultColor(),
        closeKeyboardIcon,
        backspaceIcon,
        settingsIcon,
        keyboardDrawable,
        mediaInsertionDrawable,
        deleteRecentlyUsedDrawable,
        bottomPadding,
        supportedMediaTypes);
  }

  public void setThemeValues(
      @NonNull KeyboardTheme keyboardTheme,
      float tabTextSize,
      ColorStateList tabTextColor,
      @ColorInt int nameTextColor,
      Drawable closeKeyboardIcon,
      Drawable backspaceIcon,
      Drawable settingsIcon,
      Drawable keyboardDrawable,
      Drawable mediaInsertionDrawable,
      Drawable deleteRecentlyUsedDrawable,
      int bottomPadding,
      Set<MediaType> supportedMediaTypes) {
    mKeyboardTheme = keyboardTheme;
    mTabTitleTextSize = tabTextSize;
    mTabTitleTextColor = ensureContrastColor(tabTextColor, nameTextColor, keyboardDrawable);
    mCloseKeyboardIcon = closeKeyboardIcon;
    mBackspaceIcon = backspaceIcon;
    mSettingsIcon = settingsIcon;
    mMediaInsertionDrawable = mediaInsertionDrawable;
    mDeleteRecentlyUsedDrawable = deleteRecentlyUsedDrawable;
    mBottomPadding = bottomPadding;
    findViewById(R.id.quick_keys_popup_quick_keys_insert_media)
        .setVisibility(supportedMediaTypes.isEmpty() ? View.GONE : VISIBLE);
    setBackground(keyboardDrawable);
    if (mPagerAdapter != null) {
      mPagerAdapter.setKeyTextColor(mTabTitleTextColor);
    }
  }

  @NonNull
  @VisibleForTesting
  static ColorStateList ensureContrastColor(
      @NonNull ColorStateList keyTextColor,
      @ColorInt int nameTextColor,
      @Nullable Drawable keyboardDrawable) {
    final int bgColor = extractDrawableColor(keyboardDrawable);
    final int bgLum = OverlayDataNormalizer.luminance(bgColor);
    final int keyTextLum = OverlayDataNormalizer.luminance(keyTextColor.getDefaultColor());
    final int diff = Math.abs(bgLum - keyTextLum);
    if (diff >= 96) {
      return keyTextColor;
    }
    final int nameTextLum = OverlayDataNormalizer.luminance(nameTextColor);
    final int nameDiff = Math.abs(bgLum - nameTextLum);
    if (nameDiff >= 96) {
      return new ColorStateList(new int[][] {{0}}, new int[] {nameTextColor});
    }
    final int fallbackColor = bgLum > OverlayDataNormalizer.GRAY_LUM ? Color.BLACK : Color.WHITE;
    return new ColorStateList(new int[][] {{0}}, new int[] {fallbackColor});
  }

  @ColorInt
  @VisibleForTesting
  static int extractDrawableColor(@Nullable Drawable drawable) {
    if (drawable == null) {
      return Color.BLACK;
    }
    if (drawable instanceof ColorDrawable) {
      final int color = ((ColorDrawable) drawable).getColor();
      if (Color.alpha(color) > 20) {
        return color;
      }
      return Color.BLACK;
    }
    try {
      Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
      Canvas canvas = new Canvas(bitmap);
      Rect originalBounds = drawable.copyBounds();
      drawable.setBounds(0, 0, 1, 1);
      drawable.draw(canvas);
      drawable.setBounds(originalBounds);
      int pixel = bitmap.getPixel(0, 0);
      bitmap.recycle();
      if (Color.alpha(pixel) > 20) {
        return pixel;
      }
    } catch (Exception e) {
      // ignore
    }
    return Color.BLACK;
  }

  @Override
  public void setOnKeyboardActionListener(OnKeyboardActionListener keyboardActionListener) {
    FrameKeyboardViewClickListener frameKeyboardViewClickListener =
        new FrameKeyboardViewClickListener(keyboardActionListener);
    frameKeyboardViewClickListener.registerOnViews(this);

    final Context context = getContext();
    final List<QuickTextKey> list = new ArrayList<>();
    // always starting with Recent
    final HistoryQuickTextKey historyQuickTextKey =
        new HistoryQuickTextKey(context, mQuickKeyHistoryRecords);
    list.add(historyQuickTextKey);
    // then all the rest
    list.addAll(AnyApplication.getQuickTextKeyFactory(context).getEnabledAddOns());

    final QuickTextUserPrefs quickTextUserPrefs = new QuickTextUserPrefs(context);

    final ViewPagerWithDisable pager = findViewById(R.id.quick_text_keyboards_pager);
    mPagerAdapter =
        new QuickKeysKeyboardPagerAdapter(
            context,
            pager,
            list,
            new RecordHistoryKeyboardActionListener(historyQuickTextKey, keyboardActionListener),
            mDefaultSkinTonePrefTracker,
            mDefaultGenderPrefTracker,
            mKeyboardTheme,
            mTabTitleTextColor,
            mBottomPadding);
    if (mOverlayData.isValid()) {
      mPagerAdapter.setThemeOverlay(getNormalizedOverlayData(mOverlayData));
    }

    final ImageView clearEmojiHistoryIcon =
        findViewById(R.id.quick_keys_popup_delete_recently_used_smileys);
    ViewPager.SimpleOnPageChangeListener onPageChangeListener =
        new ViewPager.SimpleOnPageChangeListener() {
          @Override
          public void onPageSelected(int position) {
            super.onPageSelected(position);
            QuickTextKey selectedKey = list.get(position);
            quickTextUserPrefs.setLastSelectedAddOnId(selectedKey.getId());
            // if this is History, we need to show clear icon
            // else, hide the clear icon
            clearEmojiHistoryIcon.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
          }
        };
    int startPageIndex = quickTextUserPrefs.getStartPageIndex(list);
    setupSlidingTab(
        this,
        mTabTitleTextSize,
        mTabTitleTextColor,
        pager,
        mPagerAdapter,
        onPageChangeListener,
        startPageIndex);

    // setting up icons from theme
    ((ImageView) findViewById(R.id.quick_keys_popup_close)).setImageDrawable(mCloseKeyboardIcon);
    ((ImageView) findViewById(R.id.quick_keys_popup_backspace)).setImageDrawable(mBackspaceIcon);
    ((ImageView) findViewById(R.id.quick_keys_popup_quick_keys_insert_media))
        .setImageDrawable(mMediaInsertionDrawable);
    clearEmojiHistoryIcon.setImageDrawable(mDeleteRecentlyUsedDrawable);
    ((ImageView) findViewById(R.id.quick_keys_popup_quick_keys_settings))
        .setImageDrawable(mSettingsIcon);
    final View actionsLayout = findViewById(R.id.quick_text_actions_layout);
    actionsLayout.setPadding(
        actionsLayout.getPaddingLeft(),
        actionsLayout.getPaddingTop(),
        actionsLayout.getPaddingRight(),
        // this will support the case were we have navigation-bar offset
        actionsLayout.getPaddingBottom() + mBottomPadding);

    applyThemeOverlayToUi();
  }

  @Override
  public void setKeyboardTheme(@NonNull KeyboardTheme theme) {
    mKeyboardTheme = theme;
    if (mPagerAdapter != null) {
      mPagerAdapter.setKeyboardTheme(theme);
    }
  }

  @Override
  public void setThemeOverlay(OverlayData overlay) {
    mOverlayData = overlay;
    if (mPagerAdapter != null) {
      if (mOverlayData.isValid()) {
        mPagerAdapter.setThemeOverlay(getNormalizedOverlayData(mOverlayData));
      } else {
        mPagerAdapter.setThemeOverlay(mOverlayData);
      }
    }
    applyThemeOverlayToUi();
  }

  private void applyThemeOverlayToUi() {
    if (mOverlayData.isValid()) {
      OverlayData normalizedOverlay = getNormalizedOverlayData(mOverlayData);
      int iconColor = normalizedOverlay.getPrimaryTextColor();
      applyIconColorFilter(R.id.quick_keys_popup_close, iconColor);
      applyIconColorFilter(R.id.quick_keys_popup_backspace, iconColor);
      applyIconColorFilter(R.id.quick_keys_popup_quick_keys_insert_media, iconColor);
      applyIconColorFilter(R.id.quick_keys_popup_delete_recently_used_smileys, iconColor);
      applyIconColorFilter(R.id.quick_keys_popup_quick_keys_settings, iconColor);

      PagerSlidingTabStrip pagerTabStrip = findViewById(R.id.pager_tabs);
      if (pagerTabStrip != null) {
        pagerTabStrip.setTextColor(iconColor);
        pagerTabStrip.setIndicatorColor(iconColor);
      }
    }
  }

  private static OverlayData getNormalizedOverlayData(OverlayData overlay) {
    if (overlay.getPrimaryDarkColor() != android.graphics.Color.TRANSPARENT
        || overlay.getSecondaryTextColor() != android.graphics.Color.TRANSPARENT) {
      return com.anysoftkeyboard.overlay.OverlayDataNormalizer.normalize(
          overlay, 96, overlay.getPrimaryDarkColor(), overlay.getSecondaryTextColor());
    } else {
      return overlay;
    }
  }

  private void applyIconColorFilter(int viewId, int color) {
    ImageView imageView = findViewById(viewId);
    if (imageView != null && imageView.getDrawable() != null) {
      imageView.getDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
  }

  public void setQuickKeyHistoryRecords(QuickKeyHistoryRecords quickKeyHistoryRecords) {
    mQuickKeyHistoryRecords = quickKeyHistoryRecords;
  }

  public void setEmojiVariantsPrefTrackers(
      DefaultSkinTonePrefTracker defaultSkinTonePrefTracker,
      DefaultGenderPrefTracker defaultGenderPrefTracker) {
    mDefaultSkinTonePrefTracker = defaultSkinTonePrefTracker;
    mDefaultGenderPrefTracker = defaultGenderPrefTracker;
  }
}
