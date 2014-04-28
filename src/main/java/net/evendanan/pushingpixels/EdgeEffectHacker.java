package net.evendanan.pushingpixels;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class EdgeEffectHacker {

	/**
	 * Will apply a ColorFilter on-top of the edge-effect drawables.
	 * Call this method after inflating a view (e.g., ListView, ScrollView) which you want to brand
	 * @param context The application's Context
	 * @param brandColor The color you wish to apply.
	 */
	public static void brandGlowEffect(@NonNull Context context, int brandColor) {
		//glow
		int glowDrawableId = context.getResources().getIdentifier("overscroll_glow", "drawable", "android");
		if (glowDrawableId != 0) {
			Drawable androidGlow = context.getResources().getDrawable(glowDrawableId);
			assert androidGlow != null;//I know it can be null, since there is an Identifier with the type and name
			androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
		}
		//edge
		int edgeDrawableId = context.getResources().getIdentifier("overscroll_edge", "drawable", "android");
		if (edgeDrawableId != 0){
			Drawable androidEdge = context.getResources().getDrawable(edgeDrawableId);
			assert androidEdge != null;//I know it can be null, since there is an Identifier with the type and name
			androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
		}
	}
}
