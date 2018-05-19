package com.anysoftkeyboard;

import static android.os.Build.VERSION_CODES.N;

import android.graphics.Paint;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.ShadowPaint;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Paint.class)
public class MyShadowPaint extends ShadowPaint {
    public static final Set<String> textsWithoutGlyphs = new HashSet<>();

    @Implementation(minSdk = N)
    public boolean hasGlyph(String text) {
        return !textsWithoutGlyphs.contains(text);
    }

    @Resetter
    public void clearGlyphs() {
        textsWithoutGlyphs.clear();
    }
}
