package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PreviewPopupTheme {
	public static final int ANIMATION_STYLE_NONE = 0;
	public static final int ANIMATION_STYLE_EXTEND = 1;
	public static final int ANIMATION_STYLE_APPEAR = 2;

	@Retention(RetentionPolicy.SOURCE)
	@IntDef({ANIMATION_STYLE_NONE, ANIMATION_STYLE_EXTEND, ANIMATION_STYLE_APPEAR})
	public @interface PreviewAnimationType {}

	private int mPreviewKeyTextSize;
	private int mPreviewLabelTextSize;
	private Drawable mPreviewKeyBackground;
	private int mPreviewKeyTextColor;
	private Typeface mKeyStyle = Typeface.DEFAULT;
	private int mVerticalOffset;
	@PreviewAnimationType
	private int mPreviewAnimationType = ANIMATION_STYLE_APPEAR;

	public int getPreviewKeyTextSize() {
		return mPreviewKeyTextSize;
	}

	public void setPreviewKeyTextSize(int previewKeyTextSize) {
		mPreviewKeyTextSize = previewKeyTextSize;
	}

	public int getPreviewLabelTextSize() {
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

	public int getPreviewKeyTextColor() {
		return mPreviewKeyTextColor;
	}

	public void setPreviewKeyTextColor(int previewKeyTextColor) {
		mPreviewKeyTextColor = previewKeyTextColor;
	}

	public void setKeyStyle(Typeface keyStyle) {
		mKeyStyle = keyStyle;
	}

	public Typeface getKeyStyle() {
		return mKeyStyle;
	}

	public void setVerticalOffset(int verticalOffset) {
		mVerticalOffset = verticalOffset;
	}

	public int getVerticalOffset() {
		return mVerticalOffset;
	}

	@PreviewAnimationType
	public int getPreviewAnimationType() {
		return mPreviewAnimationType;
	}

	public void setPreviewAnimationType(@PreviewAnimationType int mPreviewAnimationType) {
		this.mPreviewAnimationType = mPreviewAnimationType;
	}
}
