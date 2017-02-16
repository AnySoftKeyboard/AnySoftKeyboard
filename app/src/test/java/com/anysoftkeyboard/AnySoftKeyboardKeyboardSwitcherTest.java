package com.anysoftkeyboard;

import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_PASSWORD;

@RunWith(RobolectricTestRunner.class)
public class AnySoftKeyboardKeyboardSwitcherTest extends AnySoftKeyboardBaseTest {

    @Override
    public void setUpForAnySoftKeyboardBase() throws Exception {
        super.setUpForAnySoftKeyboardBase();

        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
    }

    @Test
    public void testOnLowMemory() {
        mAnySoftKeyboardUnderTest.onLowMemory();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).onLowMemory();
    }

    @Test
    public void testForceRecreateKeyboardOnSettingKeyboardView() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).flushKeyboardsCache();
    }

    @Test
    public void testCreatedPhoneKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_PHONE);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.INPUT_MODE_PHONE, editorInfo, false);
    }

    @Test
    public void testCreatedDateTimeKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_DATETIME);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.INPUT_MODE_DATETIME, editorInfo, false);
    }

    @Test
    public void testCreatedNumbersKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_NUMBER);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.INPUT_MODE_NUMBERS, editorInfo, true);
    }

    @Test
    public void testCreatedTextInputKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, editorInfo, true);
    }

    @Test
    public void testCreatedEmailTextInputKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.INPUT_MODE_EMAIL, editorInfo, true);
    }

    @Test
    public void testCreatedPasswordTextInputKeyboard() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        //just a normal text keyboard
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, editorInfo, true);
        //with password row mode
        Assert.assertEquals(KEYBOARD_ROW_MODE_PASSWORD, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenPasswordFieldButOptionDisabled() {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, false);
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        //just a normal text keyboard
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, editorInfo, true);
        //with NORMAL row mode, since the pref is false
        Assert.assertEquals(KEYBOARD_ROW_MODE_NORMAL, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());
    }

    @Test
    public void testKeyboardsRecycledOnPasswordRowSupportPrefChange() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, false);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, true);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        //same value
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, true);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).flushKeyboardsCache();
    }

    @Test
    public void testForceMakeKeyboardsOnOrientationChange() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        final Configuration configuration = RuntimeEnvironment.application.getResources().getConfiguration();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        //Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        //sanity - not changing the orientation should not flush
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).flushKeyboardsCache();
    }

    @Test
    public void testForceMakeKeyboardsOnAddOnsPrefChange() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(KeyboardAddOnAndBuilder.KEYBOARD_PREF_PREFIX + "test", false);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue("dictionary_test", false);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_active_quick_text_key), "dummy");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_ext_kbd_top_row_key), "dummy");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_ext_kbd_bottom_row_key), "dummy");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_always_hide_language_key), true);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        //sanity
        SharedPrefsHelper.setPrefsValue("random", "dummy");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
    }
}