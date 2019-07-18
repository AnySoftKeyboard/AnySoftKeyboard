package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PreviewPopupTheme {
    static final int ANIMATION_STYLE_NONE = 0;
    static final int ANIMATION_STYLE_EXTEND = 1;
    static final int ANIMATION_STYLE_APPEAR = 2;
    private int mPreviewKeyTextSize;
    private int mPreviewLabelTextSize;
    private Drawable mPreviewKeyBackground;
    private int mPreviewKeyTextColor;
    private Typeface mKeyStyle = Typeface.DEFAULT;
    private int mVerticalOffset;
    @PreviewAnimationType private int mPreviewAnimationType = ANIMATION_STYLE_APPEAR;

    int getPreviewKeyTextSize() {
        return mPreviewKeyTextSize;
    }

    public void setPreviewKeyTextSize(int previewKeyTextSize) {
        mPreviewKeyTextSize = previewKeyTextSize;
    }

    int getPreviewLabelTextSize() {
        if (mPreviewLabelTextSize < 0) {
            return getPreviewKeyTextSize();
        } else {
            return mPreviewLabelTextSize;
        }
    }

    public void setPreviewLabelTextSize(int previewLabelTextSize) {
        mPreviewLabelTextSize = previewLabelTextSize;
    }

    public Drawable getPreviewKeyBackground() {
        return mPreviewKeyBackground;
    }

    public void setPreviewKeyBackground(Drawable previewKeyBackground) {
        mPreviewKeyBackground = previewKeyBackground;
    }

    int getPreviewKeyTextColor() {
        return mPreviewKeyTextColor;
    }

    public void setPreviewKeyTextColor(int previewKeyTextColor) {
        mPreviewKeyTextColor = previewKeyTextColor;
    }

    Typeface getKeyStyle() {
        return mKeyStyle;
    }

    public void setKeyStyle(Typeface keyStyle) {
        mKeyStyle = keyStyle;
    }

    public int getVerticalOffset() {
        return mVerticalOffset;
    }

    public void setVerticalOffset(int verticalOffset) {
        mVerticalOffset = verticalOffset;
    }

    @PreviewAnimationType
    public int getPreviewAnimationType() {
        return mPreviewAnimationType;
    }

    public void setPreviewAnimationType(@PreviewAnimationType int previewAnimationType) {
        mPreviewAnimationType = previewAnimationType;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ANIMATION_STYLE_NONE, ANIMATION_STYLE_EXTEND, ANIMATION_STYLE_APPEAR})
    public @interface PreviewAnimationType {}
}
