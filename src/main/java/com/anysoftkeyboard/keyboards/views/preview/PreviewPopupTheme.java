package com.anysoftkeyboard.keyboards.views.preview;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public class PreviewPopupTheme {
	private int mPreviewKeyTextSize;
	private int mPreviewLabelTextSize;
	private Drawable mPreviewKeyBackground;
	private int mPreviewKeyTextColor;
	private Typeface mKeyStyle = Typeface.DEFAULT;
	private int mVerticalOffset;

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
}
