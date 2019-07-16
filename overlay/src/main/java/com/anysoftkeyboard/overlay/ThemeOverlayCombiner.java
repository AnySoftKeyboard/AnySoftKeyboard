package com.anysoftkeyboard.overlay;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

public class ThemeOverlayCombiner {

    private static final int[][] NO_STATES = new int[][]{{0}};

    private OverlayData mOverlayData = new OverlayData();

    private final ThemeResourcesHolderImpl mThemeOriginalResources = new ThemeResourcesHolderImpl();
    private final ThemeResourcesHolderImpl mCalculatedResources = new ThemeResourcesHolderImpl();

    public void setOverlayData(@NonNull OverlayData data) {
        mOverlayData = data;
        recalculateResources();
    }

    private void recalculateResources() {
        if (mThemeOriginalResources.mKeyboardBackground != null) {
            mThemeOriginalResources.mKeyboardBackground.clearColorFilter();
        }

        if (mThemeOriginalResources.mKeyBackground != null) {
            mThemeOriginalResources.mKeyBackground.clearColorFilter();
        }

        if (mOverlayData.isValid()) {
            mCalculatedResources.mKeyBackground = overlayDrawable(mThemeOriginalResources.mKeyBackground, mOverlayData.getPrimaryColor());
            mCalculatedResources.mKeyboardBackground = overlayDrawable(mThemeOriginalResources.mKeyboardBackground, mOverlayData.getPrimaryDarkColor());
            mCalculatedResources.mKeyTextColor = new ColorStateList(NO_STATES, new int[]{mOverlayData.getPrimaryTextColor()});
            mCalculatedResources.mNameTextColor = mCalculatedResources.mHintTextColor = mOverlayData.getSecondaryTextColor();
        }
    }

    private static Drawable overlayDrawable(Drawable original, int color) {
        if (original == null) {
            return new ColorDrawable(color);
        } else {
            //BAD - DRAWS OVER TRANSPARENT
            //original.setColorFilter(color, PorterDuff.Mode.OVERLAY);
            //original.setColorFilter(color, PorterDuff.Mode.SCREEN);
            //original.setColorFilter(color, PorterDuff.Mode.LIGHTEN);

            //TOO WEAK. NOT DRAWING ON TRANSPARENT
            //original.setColorFilter(color, PorterDuff.Mode.MULTIPLY);

            //OKAY
            //original.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            //PRETTY GOOD
            //original.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            //original.setColorFilter(new LightingColorFilter(0xFF111111, color));
            original.setColorFilter(new LightingColorFilter(0xFF333333, color));
            //original.setColorFilter(new LightingColorFilter(0xFF444444, color));
            //original.setColorFilter(new LightingColorFilter(0xFF888888, color));
            return original;
        }
    }

    public void setThemeKeyBackground(Drawable drawable) {
        if (mThemeOriginalResources.mKeyBackground != null) {
            mThemeOriginalResources.mKeyBackground.clearColorFilter();
        }
        mThemeOriginalResources.mKeyBackground = drawable;
        recalculateResources();
    }

    public void setThemeKeyboardBackground(Drawable drawable) {
        if (mThemeOriginalResources.mKeyBackground != null) {
            mThemeOriginalResources.mKeyBackground.clearColorFilter();
        }
        mThemeOriginalResources.mKeyboardBackground = drawable;
        recalculateResources();
    }

    public void setThemeTextColor(ColorStateList color) {
        mThemeOriginalResources.mKeyTextColor = color;
    }

    public void setThemeNameTextColor(@ColorInt int color) {
        mThemeOriginalResources.mNameTextColor = color;
    }

    public void setThemeHintTextColor(@ColorInt int color) {
        mThemeOriginalResources.mHintTextColor = color;
    }

    public ThemeResourcesHolder getThemeResources() {
        if (mOverlayData.isValid()) {
            return mCalculatedResources;
        } else {
            return mThemeOriginalResources;
        }
    }

    public void applyOnIcon(Drawable icon) {
        //completely masking
        if (mOverlayData.isValid()) {
            icon.setColorFilter(mOverlayData.getPrimaryTextColor(), PorterDuff.Mode.SRC_IN);
        } else {
            icon.clearColorFilter();
        }
    }

    public void clearFromIcon(Drawable icon) {
        icon.clearColorFilter();
    }

    private static class ThemeResourcesHolderImpl implements ThemeResourcesHolder {

        private ColorStateList mKeyTextColor = new ColorStateList(new int[][]{{0}}, new int[]{Color.WHITE});
        @ColorInt
        private int mHintTextColor = Color.WHITE;
        @ColorInt
        private int mNameTextColor = Color.GRAY;

        private Drawable mKeyBackground;
        private Drawable mKeyboardBackground;

        @Override
        public ColorStateList getKeyTextColor() {
            return mKeyTextColor;
        }

        @Override
        public int getNameTextColor() {
            return mNameTextColor;
        }

        @Override
        public int getHintTextColor() {
            return mHintTextColor;
        }

        @Override
        public Drawable getKeyBackground() {
            return mKeyBackground;
        }

        @Override
        public Drawable getKeyboardBackground() {
            return mKeyboardBackground;
        }
    }
}
