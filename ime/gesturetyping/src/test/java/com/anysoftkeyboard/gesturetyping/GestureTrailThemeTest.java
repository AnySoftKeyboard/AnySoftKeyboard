package com.anysoftkeyboard.gesturetyping;

import android.graphics.Color;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.AddOn;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GestureTrailThemeTest {

    @Test
    public void testFromThemeResource() {
        GestureTrailTheme underTest =
                GestureTrailTheme.fromThemeResource(
                        ApplicationProvider.getApplicationContext(),
                        ApplicationProvider.getApplicationContext(),
                        new AddOn.AddOnResourceMapping() {
                            @Override
                            public int[] getRemoteStyleableArrayFromLocal(
                                    int[] localStyleableArray) {
                                return localStyleableArray;
                            }

                            @Override
                            public int getApiVersion() {
                                return 10;
                            }

                            @Override
                            public int getLocalAttrId(int remoteAttrId) {
                                return remoteAttrId;
                            }
                        },
                        R.style.AnyKeyboardGestureTrailTheme);

        Assert.assertEquals(64, underTest.maxTrailLength);
        Assert.assertEquals(Color.parseColor("#5555ddff"), underTest.mTrailStartColor);
        Assert.assertEquals(Color.parseColor("#11116622"), underTest.mTrailEndColor);
        Assert.assertEquals(8f, underTest.mStartStrokeSize, 0.1f);
        Assert.assertEquals(2f, underTest.mEndStrokeSize, 0.1f);
        Assert.assertEquals(1 / 64f, underTest.mTrailFraction, 0.1f);
    }

    @Test
    public void testStrokeSizeFor() {
        GestureTrailTheme underTest =
                new GestureTrailTheme(Color.BLACK, Color.BLACK, 120f, 20f, 50);

        Assert.assertEquals(120f, underTest.strokeSizeFor(0), 0.1f);
        Assert.assertEquals(20f, underTest.strokeSizeFor(50), 0.1f);
        Assert.assertEquals(70f, underTest.strokeSizeFor(25), 0.1f);
    }

    @Test
    public void testStrokeColorFor() {
        GestureTrailTheme underTest =
                new GestureTrailTheme(
                        Color.argb(200, 60, 120, 240),
                        Color.argb(100, 30, 240, 200),
                        100f,
                        20f,
                        20);

        Assert.assertEquals(Color.argb(200, 60, 120, 240), underTest.strokeColorFor(0));
        Assert.assertEquals(Color.argb(100, 30, 240, 200), underTest.strokeColorFor(20));
        Assert.assertEquals(Color.argb(150, 45, 180, 220), underTest.strokeColorFor(10));
    }
}
