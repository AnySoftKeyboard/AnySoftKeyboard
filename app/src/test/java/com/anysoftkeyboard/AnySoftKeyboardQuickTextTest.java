package com.anysoftkeyboard;

import android.view.View;

import com.anysoftkeyboard.api.KeyCodes;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AnySoftKeyboardQuickTextTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testOutputTextKeyOutputText() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals("\uD83D\uDE03", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(mAnySoftKeyboardUnderTest.getInputView(), mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(0));

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testOutputTextKeySwitchKeyboardWhenFlipped() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_do_not_flip_quick_key_codes_functionality, false);

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(mAnySoftKeyboardUnderTest.getInputView(), mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(0));

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testPopupTextKeyOutputTextWhenFlipped() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_do_not_flip_quick_key_codes_functionality, false);

        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals("\uD83D\uDE03", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(1, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(mAnySoftKeyboardUnderTest.getInputView(), mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(0));

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testPopupTextKeySwitchKeyboard() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());

        Assert.assertEquals(2, mAnySoftKeyboardUnderTest.getInputViewContainer().getChildCount());

        Assert.assertSame(mAnySoftKeyboardUnderTest.getInputView(), mAnySoftKeyboardUnderTest.getInputViewContainer().getChildAt(0));

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testSecondPressOnQuickTextKeyDoesNotCloseKeyboard() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());

        mAnySoftKeyboardUnderTest.hideWindow();

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.QUICK_TEXT_POPUP);

        Assert.assertEquals(View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.hideWindow();

        Assert.assertEquals(View.VISIBLE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.hideWindow();
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

}