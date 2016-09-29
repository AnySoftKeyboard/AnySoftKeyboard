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

@RunWith(RobolectricTestRunner.class)
public class AnyKeyboardViewBaseTest {
    protected OnKeyboardActionListener mMockKeyboardListener;
    private AnyKeyboardBaseView mViewAnyKeyboardBaseViewTest;
    protected AnyKeyboard mEnglishKeyboard;

    @Before
    public void setUp() throws Exception {
        mMockKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
        AnyKeyboardBaseView view = createViewToTest(RuntimeEnvironment.application);
        setCreatedKeyboardView(view);
        mViewAnyKeyboardBaseViewTest.setOnKeyboardActionListener(mMockKeyboardListener);

        mEnglishKeyboard = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application)
                .get(0)
                .createKeyboard(RuntimeEnvironment.application, Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        mEnglishKeyboard.loadKeyboard(mViewAnyKeyboardBaseViewTest.getThemedKeyboardDimens());

        mViewAnyKeyboardBaseViewTest.setKeyboard(mEnglishKeyboard, 0);
    }

    @CallSuper
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardBaseView view) {
        mViewAnyKeyboardBaseViewTest = view;
    }

    protected AnyKeyboardBaseView createViewToTest(Context context) {
        return new AnyKeyboardBaseView(context, null);
    }

    @Test
    public void testKeyboardViewCreated() {
        Assert.assertNotNull(mViewAnyKeyboardBaseViewTest);
    }

    @Test
    public void testLongPressOutput() {
        //TODO
    }
}
