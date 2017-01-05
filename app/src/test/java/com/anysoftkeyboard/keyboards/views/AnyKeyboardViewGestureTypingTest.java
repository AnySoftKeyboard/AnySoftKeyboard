package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.SharedPrefsHelper;
import com.anysoftkeyboard.ViewTestUtils;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AnyKeyboardViewGestureTypingTest extends AnyKeyboardViewBaseTest {

    private AnyKeyboardView mViewUnderTest;

    @Override
    protected AnyKeyboardViewBase createViewToTest(Context context) {
        return new AnyKeyboardView(context, null);
    }

    @Override
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardViewBase view) {
        super.setCreatedKeyboardView(view);
        mViewUnderTest = (AnyKeyboardView) view;
    }

    @Test
    public void testGestureTypingNotDoneWhenDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, false);

        final int keyAIndex = findKeyIndex('a');
        final int keyLIndex = findKeyIndex('l');
        AnyKeyboard.AnyKey key1 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(keyAIndex);
        AnyKeyboard.AnyKey key2 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(keyLIndex);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key1, key2, 400, true, true);

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onGestureTypingInput(Mockito.anyList(), Mockito.any(int[].class), Mockito.anyInt());
    }

    @Test
    public void testGestureTypingNotDoneStayingInTheSameKey() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);

        final int keyAIndex = findKeyIndex('a');
        AnyKeyboard.AnyKey key1 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(keyAIndex);
        Point point1 = ViewTestUtils.getKeyCenterPoint(key1);
        point1.offset(-3, -1);
        Point point2 = ViewTestUtils.getKeyCenterPoint(key1);
        point1.offset(3, 1);

        ViewTestUtils.navigateFromTo(mViewUnderTest, point1, point2, 400, true, true);

        Mockito.verify(mMockKeyboardListener, Mockito.never()).onGestureTypingInput(Mockito.anyList(), Mockito.any(int[].class), Mockito.anyInt());
    }

    @Test
    public void testGestureTypingHappyPath() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);

        final int keyAIndex = findKeyIndex('a');
        final int keyLIndex = findKeyIndex('l');
        AnyKeyboard.AnyKey key1 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(keyAIndex);
        AnyKeyboard.AnyKey key2 = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(keyLIndex);

        ViewTestUtils.navigateFromTo(mViewUnderTest, key1, key2, 400, true, false/*don't send up event*/);

        InOrder inOrder = Mockito.inOrder(mMockKeyboardListener);
        inOrder.verify(mMockKeyboardListener).onPress('a');
        inOrder.verify(mMockKeyboardListener).onFirstDownKey('a');
        inOrder.verify(mMockKeyboardListener).onRelease('a');
        //that's it, till the up event
        inOrder.verifyNoMoreInteractions();

        Mockito.reset(mMockKeyboardListener);
        //I want to verify the path too, so I'll capture its entries into another list
        final List<com.anysoftkeyboard.gesturetyping.Point> capturedPointValues = new ArrayList<>();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                List<com.anysoftkeyboard.gesturetyping.Point> providedList = (List<com.anysoftkeyboard.gesturetyping.Point>) invocation.getArguments()[0];
                capturedPointValues.addAll(providedList);
                return null;
            }
        }).when(mMockKeyboardListener).onGestureTypingInput(Mockito.anyList(), Mockito.any(int[].class), Mockito.anyInt());
        //now lifting the finger
        ViewTestUtils.navigateFromTo(mViewUnderTest, key2, key2, 20, false, true);
        //then a gesture detection
        //and releasing the last key
        inOrder = Mockito.inOrder(mMockKeyboardListener);
        ArgumentCaptor<int[]> arrayCodesArgumentCaptor = ArgumentCaptor.forClass(int[].class);
        ArgumentCaptor<List> pointsListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        final int keysInPath = 9;
        inOrder.verify(mMockKeyboardListener).onGestureTypingInput(pointsListArgumentCaptor.capture(), arrayCodesArgumentCaptor.capture(), Mockito.eq(keysInPath));
        inOrder.verify(mMockKeyboardListener).onRelease('l');
        inOrder.verifyNoMoreInteractions();

        Assert.assertEquals('a', arrayCodesArgumentCaptor.getValue()[0]);
        Assert.assertEquals('s', arrayCodesArgumentCaptor.getValue()[1]);
        Assert.assertEquals('d', arrayCodesArgumentCaptor.getValue()[2]);
        Assert.assertEquals('f', arrayCodesArgumentCaptor.getValue()[3]);
        Assert.assertEquals('g', arrayCodesArgumentCaptor.getValue()[4]);
        Assert.assertEquals('h', arrayCodesArgumentCaptor.getValue()[5]);
        Assert.assertEquals('j', arrayCodesArgumentCaptor.getValue()[6]);
        Assert.assertEquals('k', arrayCodesArgumentCaptor.getValue()[7]);
        Assert.assertEquals('l', arrayCodesArgumentCaptor.getValue()[keysInPath - 1]);

        //at least as many points as key codes (there could be several motion-events per key)
        Assert.assertTrue(capturedPointValues.size() >= keysInPath);

        //the original list should have been cleared.
        Assert.assertEquals(0, pointsListArgumentCaptor.getValue().size());
    }
}