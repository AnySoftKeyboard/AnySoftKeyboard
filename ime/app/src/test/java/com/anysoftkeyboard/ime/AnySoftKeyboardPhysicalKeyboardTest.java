package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.addons.SupportTest;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardPhysicalKeyboardTest extends AnySoftKeyboardBaseTest {

    private static final int FIELD_ID = 0x7234321;
    private static final String FIELD_PACKAGE_NAME = "com.example.app";

    @Override
    protected EditorInfo createEditorInfoTextWithSuggestionsForSetUp() {
        final EditorInfo editorInfo = super.createEditorInfoTextWithSuggestionsForSetUp();
        mAnySoftKeyboardUnderTest.getResources().getConfiguration().keyboard =
                Configuration.KEYBOARD_NOKEYS;
        editorInfo.fieldId = FIELD_ID;
        editorInfo.packageName = FIELD_PACKAGE_NAME;
        return editorInfo;
    }

    @Before
    public void setUpAndHideInput() {
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        simulateFinishInputFlow();
    }

    @Test
    public void testDoesNotShowStatusBarIcon() {
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        SharedPrefsHelper.setPrefsValue(
                getApplicationContext()
                        .getString(R.string.settings_key_keyboard_icon_in_status_bar),
                false);
        simulateOnStartInputFlow();
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
    }

    @Test
    public void testHidesStatusBarIconOnPrefsChange() {
        SharedPrefsHelper.setPrefsValue(
                getApplicationContext()
                        .getString(R.string.settings_key_keyboard_icon_in_status_bar),
                true);
        getShadowInputMethodManager().clearStatusIconDetails();
        SharedPrefsHelper.setPrefsValue(
                getApplicationContext()
                        .getString(R.string.settings_key_keyboard_icon_in_status_bar),
                false);
        simulateOnStartInputFlow();
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
    }

    @Test
    public void testShowsStatusBarIconOnPrefsChange() {
        SharedPrefsHelper.setPrefsValue(
                getApplicationContext()
                        .getString(R.string.settings_key_keyboard_icon_in_status_bar),
                false);
        getShadowInputMethodManager().clearStatusIconDetails();
        SharedPrefsHelper.setPrefsValue(
                getApplicationContext()
                        .getString(R.string.settings_key_keyboard_icon_in_status_bar),
                true);
        simulateOnStartInputFlow();
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        Assert.assertNotNull(getShadowInputMethodManager().getLastStatusIconPackageName());
        // will call hide with a token
        Assert.assertNotNull(getShadowInputMethodManager().getLastStatusIconImeToken());
    }

    @Test
    public void testStatusBarIconLifeCycle() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_keyboard_icon_in_status_bar, true);
        getShadowInputMethodManager().clearStatusIconDetails();
        EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        // starting with view shown (in setUp method)
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        // closing the keyboard
        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        mAnySoftKeyboardUnderTest.onFinishInput();
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());

        // and again
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        // closing the keyboard
        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        Assert.assertTrue(getShadowInputMethodManager().isStatusIconShown());
        mAnySoftKeyboardUnderTest.onFinishInput();
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
    }

    @Test
    public void testNoStatusBarIconIfDisabled() {
        SharedPrefsHelper.setPrefsValue(
                getApplicationContext()
                        .getString(R.string.settings_key_keyboard_icon_in_status_bar),
                false);
        getShadowInputMethodManager().clearStatusIconDetails();
        EditorInfo editorInfo = createEditorInfoTextWithSuggestionsForSetUp();
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        // closing the keyboard
        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());

        // and again
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        // closing the keyboard
        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        Assert.assertFalse(getShadowInputMethodManager().isStatusIconShown());
    }

    @Test
    public void testKeyboardViewHiddenWhenPhysicalKeyPressed() {
        simulateOnStartInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardViewNotHiddenWhenVirtualKeyPressed() {
        simulateOnStartInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        long time = 0;
        int virtualKeyboardDeviceId = -1;
        mAnySoftKeyboardUnderTest.onKeyDown(
                'c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c', 0, virtualKeyboardDeviceId));
        mAnySoftKeyboardUnderTest.onKeyUp(
                'c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c', 0, virtualKeyboardDeviceId));

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardViewNotHiddenWhenPhysicalNonPrintableKeyPressed() {
        simulateOnStartInputFlow();

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_VOLUME_DOWN,
                new TestKeyEvent(time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_DOWN));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_VOLUME_DOWN,
                new TestKeyEvent(time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_VOLUME_DOWN));

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenOnNewInputConnectionField() {
        simulateOnStartInputFlow();

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = FIELD_ID + 1;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        simulateOnStartInputFlow(false, editorInfo);
        // this is a new input field, we should show the keyboard view
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenIfInputConnectionFieldIsZero() {
        simulateOnStartInputFlow();

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = 0;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        simulateOnStartInputFlow(false, editorInfo);
        // this is a new input field, we should show the keyboard view
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        // pressing a physical key
        mAnySoftKeyboardUnderTest.onKeyDown('c', new TestKeyEvent(time, KeyEvent.ACTION_DOWN, 'c'));
        mAnySoftKeyboardUnderTest.onKeyUp('c', new TestKeyEvent(time, KeyEvent.ACTION_UP, 'c'));

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onFinishInputView(false);

        simulateOnStartInputFlow(false, editorInfo);

        // since the input field id is ZERO, we will show the keyboard view again
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenOnPreviousInputConnectionFieldIfPhysicalKeyboardWasNotPressed() {
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.fieldId = FIELD_ID;
        editorInfo.packageName = FIELD_PACKAGE_NAME;

        simulateOnStartInputFlow(false, editorInfo);
        // this is a new input field, we should show the keyboard view
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardStaysHiddenOnPreviousInputConnectionField() {
        simulateOnStartInputFlow();

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
        // same input field, we should not show the keyboard view since it was canceled
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenOnPreviousInputConnectionFieldAfterProperClose() {
        simulateOnStartInputFlow();

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
        // same input field, we should not show the keyboard view since it was canceled
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);
        }
        // this is the same input field, but it was previously finished completely.
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardReOpenOnInputConfigurationChange() {
        simulateOnStartInputFlow();

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
        // same input field, we should not show the keyboard view since it was canceled
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onFinishInputView(true);

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, true /*configChange*/)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);
        }
        // this is the same input field, but it was previously finished completely.
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardStaysHiddenOnPreviousInputConnectionFieldAfterJustViewFinish() {
        simulateOnStartInputFlow();

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
        // same input field, we should not show the keyboard view since it was canceled
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        mAnySoftKeyboardUnderTest.onFinishInputView(false);

        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());

        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, false)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);
        }
        // this is the same input field, but it was previously NOT finished completely.
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardSwitchesLayoutOnAltSpace() {
        SupportTest.ensureKeyboardAtIndexEnabled(0, true);
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        SupportTest.ensureKeyboardAtIndexEnabled(2, true);

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE));

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(
                        time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, KeyEvent.META_ALT_ON));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(
                        time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE, KeyEvent.META_ALT_ON));

        Assert.assertEquals(
                "12335055-4aa6-49dc-8456-c7d38a1a5123",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }

    @Test
    public void testKeyboardNoLayoutSwitchOnAltSpace() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_enable_alt_space_language_shortcut, false);
        SupportTest.ensureKeyboardAtIndexEnabled(0, true);
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        SupportTest.ensureKeyboardAtIndexEnabled(2, true);

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE));

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(
                        time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE, KeyEvent.META_ALT_ON));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(
                        time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE, KeyEvent.META_ALT_ON));

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }

    @Test
    public void testKeyboardSwitchesLayoutOnShiftSpace() {
        SupportTest.ensureKeyboardAtIndexEnabled(0, true);
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        SupportTest.ensureKeyboardAtIndexEnabled(2, true);

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE));

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(
                        time,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_SPACE,
                        KeyEvent.META_SHIFT_ON));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(
                        time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE, KeyEvent.META_SHIFT_ON));

        Assert.assertEquals(
                "12335055-4aa6-49dc-8456-c7d38a1a5123",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }

    @Test
    public void testKeyboardNoLayoutSwitchOnShiftSpace() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_enable_shift_space_language_shortcut, false);
        SupportTest.ensureKeyboardAtIndexEnabled(0, true);
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        SupportTest.ensureKeyboardAtIndexEnabled(2, true);

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        long time = 0;
        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(time, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE));

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        mAnySoftKeyboardUnderTest.onKeyDown(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(
                        time,
                        KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_SPACE,
                        KeyEvent.META_SHIFT_ON));
        mAnySoftKeyboardUnderTest.onKeyUp(
                KeyEvent.KEYCODE_SPACE,
                new TestKeyEvent(
                        time, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_SPACE, KeyEvent.META_SHIFT_ON));

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }

    public static class TestKeyEvent extends KeyEvent {

        public static final Parcelable.Creator<TestKeyEvent> CREATOR =
                new Parcelable.Creator<TestKeyEvent>() {
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
            this(downTime, action, code, 0, 99);
        }

        public TestKeyEvent(long downTime, int action, int code, int metaState) {
            this(downTime, action, code, metaState, 99);
        }

        public TestKeyEvent(long downTime, int action, int code, int metaState, int deviceId) {
            super(
                    downTime,
                    action == KeyEvent.ACTION_DOWN ? downTime : downTime + 1,
                    action,
                    code,
                    0,
                    metaState,
                    deviceId,
                    code);
        }

        @Override
        public boolean isPrintingKey() {
            return Character.isLetterOrDigit(getKeyCode())
                    || getKeyCode() == ' '
                    || getKeyCode() == '\n';
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(getDownTime());
            out.writeInt(getAction());
            out.writeInt(getKeyCode());
        }
    }
}
