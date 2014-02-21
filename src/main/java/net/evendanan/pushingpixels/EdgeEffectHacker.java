package net.evendanan.pushingpixels;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;

public class EdgeEffectHacker {

	/**
	 * Will apply a ColorFilter on-top of the edge-effect drawables.
	 * Call this method after inflating a view (e.g., ListView, ScrollView) which you want to brand
	 * @param context The application's Context
	 * @param brandColor The color you wish to apply.
	 */
	public static void brandGlowEffect(Context context, int brandColor) {
		//glow
		int glowDrawableId = context.getResources().getIdentifier("overscroll_glow", "drawable", "android");
		Drawable androidGlow = context.getResources().getDrawable(glowDrawableId);
		androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
		//edge
		int edgeDrawableId = context.getResources().getIdentifier("overscroll_edge", "drawable", "android");
		Drawable androidEdge = context.getResources().getDrawable(edgeDrawableId);
		androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
	}
}
