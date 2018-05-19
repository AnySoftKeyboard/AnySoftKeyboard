package com.anysoftkeyboard;

import android.graphics.Paint;
import android.os.Build;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.ShadowPaint;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Paint.class)
public class MyShadowPaint extends ShadowPaint {
    private static final Set<String> msTextsWithoutGlyphs = new HashSet<>();

    @Implementation(minSdk = Build.VERSION_CODES.M)
    public boolean hasGlyph(String text) {
        return !msTextsWithoutGlyphs.contains(text);
    }

    public static void addStringWithoutGlyph(String string) {
        msTextsWithoutGlyphs.add(string);
    }

    @Resetter
    public void clearGlyphs() {
        msTextsWithoutGlyphs.clear();
    }
}
