package net.evendanan.pushingpixels;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;

import com.anysoftkeyboard.base.utils.Logger;

public class EdgeEffectHacker {

    /**
     * Will apply a ColorFilter on-top of the edge-effect drawables.
     * Call this method after inflating a view (e.g., ListView, ScrollView) which you want to brand
     *
     * @param activity   The application's Context
     * @param brandColor The color you wish to apply.
     */
    public static void brandGlowEffect(@NonNull Activity activity, int brandColor) {
        try {
            //glow
            int glowDrawableId = activity.getResources().getIdentifier("overscroll_glow", "drawable", "android");
            if (glowDrawableId != 0) {
                Drawable androidGlow = ResourcesCompat.getDrawable(activity.getResources(), glowDrawableId, activity.getTheme());
                assert androidGlow != null;//I know it can be null, since there is an Identifier with the type and name
                androidGlow.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
            }
            //edge
            int edgeDrawableId = activity.getResources().getIdentifier("overscroll_edge", "drawable", "android");
            if (edgeDrawableId != 0) {
                Drawable androidEdge = ResourcesCompat.getDrawable(activity.getResources(), edgeDrawableId, activity.getTheme());
                assert androidEdge != null;//I know it can be null, since there is an Identifier with the type and name
                androidEdge.setColorFilter(brandColor, PorterDuff.Mode.SRC_IN);
            }
        } catch (Exception e) {
            Logger.w("EdgeEffectHacker", "Failed to set brandGlowEffect!", e);
        }
    }
}
