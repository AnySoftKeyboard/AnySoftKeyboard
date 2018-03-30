package com.anysoftkeyboard.gesturetyping;

import android.support.annotation.NonNull;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GestureTypingDetectorTest {
    public static class TestableGestureTypingDetector extends GestureTypingDetector {
        public TestableGestureTypingDetector(@NonNull List<CharSequence> wordsToLoad) {
            setWords(wordsToLoad);
        }
    }

    @Test
    public void testCalculatesCornersInBackground() {
        Robolectric.getBackgroundThreadScheduler().pause();
        TestableGestureTypingDetector detector = new TestableGestureTypingDetector(Arrays.asList("hello", "welcome"));
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, detector.getLoadingState());
        detector.setWords(Arrays.asList(""));
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, detector.getLoadingState());
        detector.setKeys(Collections.emptyList(), 100, 100);
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, detector.getLoadingState());

        Robolectric.getBackgroundThreadScheduler().unPause();
        Robolectric.flushBackgroundThreadScheduler();

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, detector.getLoadingState());
    }
}