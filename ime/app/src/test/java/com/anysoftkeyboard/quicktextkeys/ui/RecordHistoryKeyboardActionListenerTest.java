package com.anysoftkeyboard.quicktextkeys.ui;

import static org.mockito.ArgumentMatchers.same;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.quicktextkeys.HistoryQuickTextKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RecordHistoryKeyboardActionListenerTest {

    private HistoryQuickTextKey mHistoryKey;
    private OnKeyboardActionListener mKeyboardListener;
    private RecordHistoryKeyboardActionListener mUnderTest;

    @Before
    public void setup() {
        mHistoryKey = Mockito.mock(HistoryQuickTextKey.class);
        mKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
        mUnderTest = new RecordHistoryKeyboardActionListener(mHistoryKey, mKeyboardListener);
    }

    @Test
    public void testDispatchToListener() throws Exception {
        Mockito.verifyZeroInteractions(mKeyboardListener);

        mUnderTest.onFirstDownKey(1);
        Mockito.verify(mKeyboardListener).onFirstDownKey(1);

        mUnderTest.onSeparate();
        Mockito.verify(mKeyboardListener).onSeparate();

        mUnderTest.onPinch();
        Mockito.verify(mKeyboardListener).onPinch();

        mUnderTest.onSwipeUp();
        Mockito.verify(mKeyboardListener).onSwipeUp();

        mUnderTest.onSwipeDown();
        Mockito.verify(mKeyboardListener).onSwipeDown();

        mUnderTest.onSwipeRight(false);
        Mockito.verify(mKeyboardListener).onSwipeRight(false);

        mUnderTest.onSwipeLeft(true);
        Mockito.verify(mKeyboardListener).onSwipeLeft(true);

        mUnderTest.onCancel();
        Mockito.verify(mKeyboardListener).onCancel();

        mUnderTest.onMultiTapStarted();
        Mockito.verify(mKeyboardListener).onMultiTapStarted();

        mUnderTest.onMultiTapEnded();
        Mockito.verify(mKeyboardListener).onMultiTapEnded();

        Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
        int[] codes = new int[] {1, 2, 3};
        mUnderTest.onKey(1, key, 2, codes, true);
        Mockito.verify(mKeyboardListener)
                .onKey(Mockito.eq(1), same(key), Mockito.eq(2), same(codes), Mockito.eq(true));

        mUnderTest.onPress(4);
        Mockito.verify(mKeyboardListener).onPress(4);

        mUnderTest.onRelease(2);
        Mockito.verify(mKeyboardListener).onRelease(2);

        mUnderTest.onLongPressDone(key);
        Mockito.verify(mKeyboardListener).onLongPressDone(same(key));
    }

    @Test
    public void onTextWithNoLabel() throws Exception {
        Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
        key.label = null;

        mUnderTest.onText(key, "test");

        Mockito.verify(mKeyboardListener).onText(key, "test");
        Mockito.verifyZeroInteractions(mHistoryKey);
    }

    @Test
    public void onTextWithNoText() throws Exception {
        Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
        key.label = "testing";

        mUnderTest.onText(key, null);

        Mockito.verify(mKeyboardListener).onText(key, null);
        Mockito.verifyZeroInteractions(mHistoryKey);
    }

    @Test
    public void onTextWithText() throws Exception {
        Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
        key.label = "testing";

        mUnderTest.onText(key, "testing_value");

        Mockito.verify(mKeyboardListener).onText(key, "testing_value");
        Mockito.verify(mHistoryKey).recordUsedKey("testing", "testing_value");
    }
}
