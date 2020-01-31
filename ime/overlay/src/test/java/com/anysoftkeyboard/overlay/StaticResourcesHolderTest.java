package com.anysoftkeyboard.overlay;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class StaticResourcesHolderTest {

    @Test
    public void testAllSet() {
        final Drawable keyBackground = Mockito.mock(Drawable.class);
        final Drawable keyboardBackground = Mockito.mock(Drawable.class);
        final ColorStateList textColor = Mockito.mock(ColorStateList.class);

        final StaticResourcesHolder staticResourcesHolder =
                new StaticResourcesHolder(
                        textColor, Color.BLUE, Color.RED, keyBackground, keyboardBackground);

        Assert.assertSame(keyBackground, staticResourcesHolder.getKeyBackground());
        Assert.assertSame(keyboardBackground, staticResourcesHolder.getKeyboardBackground());
        Assert.assertSame(textColor, staticResourcesHolder.getKeyTextColor());
        Assert.assertEquals(Color.RED, staticResourcesHolder.getNameTextColor());
        Assert.assertEquals(Color.BLUE, staticResourcesHolder.getHintTextColor());
    }
}
