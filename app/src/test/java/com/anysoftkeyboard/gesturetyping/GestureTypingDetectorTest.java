package com.anysoftkeyboard.gesturetyping;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GestureTypingDetectorTest {
    private List<char[][]> mWords;
    private Set<Keyboard.Key> mKeys;

    @Before
    public void setUp() {
        mWords = Collections.singletonList(new char[][]{"hello".toCharArray()});
        mKeys = Collections.singleton(Mockito.mock(AnyKeyboard.AnyKey.class));
    }

    @Test
    public void testCalculatesCornersInBackground() {
        Robolectric.getBackgroundThreadScheduler().pause();
        GestureTypingDetector detector = new GestureTypingDetector(5);
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, detector.getLoadingState());
        detector.setWords(mWords);
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, detector.getLoadingState());
        detector.setKeys(mKeys, 100, 100);
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, detector.getLoadingState());

        Robolectric.getBackgroundThreadScheduler().unPause();
        Robolectric.flushBackgroundThreadScheduler();

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, detector.getLoadingState());
    }

    @Test
    public void testCalculatesCornersInBackground_OtherFlow() {
        Robolectric.getBackgroundThreadScheduler().pause();
        GestureTypingDetector detector = new GestureTypingDetector(5);
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, detector.getLoadingState());
        detector.setKeys(Collections.emptyList(), 100, 100);
        Assert.assertEquals(GestureTypingDetector.LoadingState.NOT_LOADED, detector.getLoadingState());
        detector.setWords(mWords);
        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADING, detector.getLoadingState());

        Robolectric.getBackgroundThreadScheduler().unPause();
        Robolectric.flushBackgroundThreadScheduler();

        Assert.assertEquals(GestureTypingDetector.LoadingState.LOADED, detector.getLoadingState());
    }
}