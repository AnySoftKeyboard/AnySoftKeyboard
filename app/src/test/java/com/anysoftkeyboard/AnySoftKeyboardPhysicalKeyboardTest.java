package com.anysoftkeyboard;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnySoftKeyboardPhysicalKeyboardTest extends AnySoftKeyboardBaseTest {

    private static final int FIELD_ID = 0x7234321;
    private static final String FIELD_PACKAGE_NAME = "com.example.app";

    @Override
    protected EditorInfo createEditorInfoTextWithSuggestionsForSetUp() {
        final EditorInfo editorInfo = super.createEditorInfoTextWithSuggestionsForSetUp();
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().keyboard = Configuration.KEYBOARD_NOKEYS;
        editorInfo.fieldId = FIELD_ID;
        editorInfo.packageName = FIELD_PACKAGE_NAME;
        return editorInfo;
    }

    @Test
    public void testDoesNotShowStatusBarIcon() {
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_keyboard_icon_in_status_bar), false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        //will call hide with a token
        Assert.assertNotNull(getShadowInputMethodManager().getLastStatusIconImeToken());
    }

    @Test
    public void testHidesStatusBarIconOnPrefsChange() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_keyboard_icon_in_status_bar), true);
        getShadowInputMethodManager().clearStatusIconDetails();
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_keyboard_icon_in_status_bar), false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        //will call hide with a token
        Assert.assertNotNull(getShadowInputMethodManager().getLastStatusIconImeToken());
    }

    @Test
    public void testShowsStatusBarIconOnPrefsChange() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_keyboard_icon_in_status_bar), false);
        getShadowInputMethodManager().clearStatusIconDetails();
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_keyboard_icon_in_status_bar), true);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        Assert.assertNotNull(getShadowInputMethodManager().getLastStatusIconPackageName());
        //will call hide with a token
        Assert.assertNotNull(getShadowInputMethodManager().getLastStatusIconImeToken());
    }

    @Test
    public void testStatusBarIconLifeCycle() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_keyboard_icon_in_status_bar), true);
        getShadowInputMethodManager().clearStatusIconDetails();
        EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        //starting with view shown (in setUp method)
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        //closing the keyboard
        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        mAnySoftKeyboardUnderTest.onFinishInput();
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());

        //and again
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        //closing the keyboard
        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        mAnySoftKeyboardUnderTest.onFinishInput();
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
    }

    @Test
    public void testNoStatusBarIconIfDisabled() {
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_keyboard_icon_in_status_bar), false);
        getShadowInputMethodManager().clearStatusIconDetails();
        EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        //closing the keyboard
        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());

        //and again
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        //closing the keyboard
        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
    }

    @Test
    public void testKeyboardViewHiddenWhenPhysicalKeyPressed() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardViewNotHiddenWhenVirtualKeyPressed() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c', -1));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c', -1));

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardViewNotHiddenWhenPhysicalNonPrintableKeyPressed() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown(KeyEvent.KEYCODE_VOLUME_DOWN, new TestKeyEvent(time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        mAnySoftKeyboardUnderTest.onKeyUp(KeyEvent.KEYCODE_VOLUME_DOWN, new TestKeyEvent(time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenOnNewInputConnectionField() {
        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = FIELD_ID + 1;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        simulateOnStartInputFlow(false, false, editorInfo);
        //this is a new input field, we should show the keyboard view
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenIfInputConnectionFieldIsZero() {
        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = 0;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        simulateOnStartInputFlow(false, false, editorInfo);
        //this is a new input field, we should show the keyboard view
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        //pressing a physical key
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onFinishInputView(false);

        simulateOnStartInputFlow(false, false, editorInfo);

        //since the input field id is ZERO, we will show the keyboard view again
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenOnPreviousInputConnectionFieldIfPhysicalKeyboardWasNotPressed() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();
        mAnySoftKeyboardUnderTest.hideWindow();

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = FIELD_ID;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        simulateOnStartInputFlow(false, false, editorInfo);
        //this is a new input field, we should show the keyboard view
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardStaysHiddenOnPreviousInputConnectionField() {
        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = FIELD_ID;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        //same input field, we should not show the keyboard view since it was canceled
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenOnPreviousInputConnectionFieldAfterProperClose() {
        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = FIELD_ID;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        //same input field, we should not show the keyboard view since it was canceled
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);
        }
        //this is the same input field, but it was previously finished completely.
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenOnInputConfigurationChange() {
        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = FIELD_ID;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        //same input field, we should not show the keyboard view since it was canceled
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onFinishInputView(true);

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, true/*configChange*/)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);
        }
        //this is the same input field, but it was previously finished completely.
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardStaysHiddenOnPreviousInputConnectionFieldAfterJustViewFinish() {
        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = FIELD_ID;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        //same input field, we should not show the keyboard view since it was canceled
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onFinishInputView(false);

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        //this is the same input field, but it was previously NOT finished completely.
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    public static class TestKeyEvent extends KeyEvent {

        public static final Parcelable.Creator<TestKeyEvent> CREATOR = new Parcelable.Creator<TestKeyEvent>() {
            @Override
            public TestKeyEvent createFromParcel(Parcel in) {
                return new TestKeyEvent(in.readLong(), in.readInt(), in.readInt());
            }

            @Override
            public TestKeyEvent[] newArray(int size) {
                return new TestKeyEvent[size];
            }
        };

        public TestKeyEvent(long downTime, int action, int code) {
            this(downTime, action, code, 99);
        }

        public TestKeyEvent(long downTime, int action, int code, int deviceId) {
            super(downTime, action == KeyEvent.ACTION_DOWN ? downTime : downTime + 1, action, code, 0, 0, deviceId, code);
        }

        @Override
        public boolean isPrintingKey() {
            return Character.isLetterOrDigit(getKeyCode()) || getKeyCode() == ' ' || getKeyCode() == '\n';
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(getDownTime());
            out.writeInt(getAction());
            out.writeInt(getKeyCode());
        }
    }
}