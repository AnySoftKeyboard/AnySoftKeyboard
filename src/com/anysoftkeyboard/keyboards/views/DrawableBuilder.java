package com.anysoftkeyboard.keyboards.views;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import com.anysoftkeyboard.theme.KeyboardTheme;

public class DrawableBuilder {
	private final int mDrawableResourceId;
	private final KeyboardTheme mTheme;
	
	private DrawableBuilder(KeyboardTheme theme, int drawableResId) {
		mTheme = theme;
		mDrawableResourceId = drawableResId;
	}
	
	public Drawable buildDrawable() {
		return mTheme.getPackageContext().getResources().getDrawable(mDrawableResourceId);
	}
	
	public static DrawableBuilder build(KeyboardTheme theme, TypedArray a, final int attr) {
		int resId = a.getResourceId(attr, 0);
		return new DrawableBuilder(theme, resId);
	}
}
