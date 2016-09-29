package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowToast;

import java.util.Arrays;

@RunWith(RobolectricTestRunner.class)
public class AnyKeyboardViewBaseTest {
    protected OnKeyboardActionListener mMockKeyboardListener;
    private AnyKeyboardBaseView mUnderTest;
    protected AnyKeyboard mEnglishKeyboard;

    @Before
    public void setUp() throws Exception {
        mMockKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
        AnyKeyboardBaseView view = createViewToTest(RuntimeEnvironment.application);
        setCreatedKeyboardView(view);
        mUnderTest.setOnKeyboardActionListener(mMockKeyboardListener);

        mEnglishKeyboard = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application)
                .get(0)
                .createKeyboard(RuntimeEnvironment.application, Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        mEnglishKeyboard.loadKeyboard(mUnderTest.getThemedKeyboardDimens());

        mUnderTest.setKeyboard(mEnglishKeyboard, 0);
    }

    @CallSuper
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardBaseView view) {
        mUnderTest = view;
    }

    protected AnyKeyboardBaseView createViewToTest(Context context) {
        return new AnyKeyboardBaseView(context, null);
    }

    @Test
    public void testKeyboardViewCreated() {
        Assert.assertNotNull(mUnderTest);
    }

    @Test
    public void testLongPressOutput() {
        AnyKeyboard.AnyKey key = (AnyKeyboard.AnyKey) mEnglishKeyboard.getKeys().get(5);
        key.longPressCode = 'z';
        mUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false);
        Mockito.verify(mMockKeyboardListener).onKey(Mockito.eq((int)'z'), Mockito.same(key), Mockito.eq(0), Mockito.any(int[].class), Mockito.eq(true));
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(Mockito.eq(key.getPrimaryCode()), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());
    }

    @Test
    public void testLongPressOutputTagsToast() {
        AnyKeyboard.AnyKey key = Mockito.mock(AnyKeyboard.AnyKey.class);
        Mockito.doReturn(Arrays.asList("tag", "tag2")).when(key).getKeyTags();

        mUnderTest.onLongPress(mEnglishKeyboard.getKeyboardAddOn(), key, false);
        Mockito.verify(mMockKeyboardListener, Mockito.never()).onKey(Mockito.anyInt(), Mockito.any(Keyboard.Key.class), Mockito.anyInt(), Mockito.any(int[].class), Mockito.anyBoolean());
        Assert.assertEquals(":tag, :tag2", ShadowToast.getTextOfLatestToast());
    }
}
