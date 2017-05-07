package com.anysoftkeyboard;

import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import static com.anysoftkeyboard.AnySoftKeyboard.PREFS_KEY_POSTFIX_OVERRIDE_DICTIONARY;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_PASSWORD;

@RunWith(AnySoftKeyboardTestRunner.class)
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
        Assert.assertTrue(AnyApplication.getConfig().supportPasswordKeyboardRowMode());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, false);
        Assert.assertFalse(AnyApplication.getConfig().supportPasswordKeyboardRowMode());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, true);
        Assert.assertTrue(AnyApplication.getConfig().supportPasswordKeyboardRowMode());
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        //same value
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, true);
        Assert.assertTrue(AnyApplication.getConfig().supportPasswordKeyboardRowMode());
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
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, true);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), mAnySoftKeyboardUnderTest.getSpiedSuggest());
        SharedPrefsHelper.setPrefsValue("keyboard_id" + PREFS_KEY_POSTFIX_OVERRIDE_DICTIONARY, "dictionary_id");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).resetNextWordSentence();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setMainDictionary(Mockito.same(RuntimeEnvironment.application), Mockito.any(DictionaryAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), mAnySoftKeyboardUnderTest.getSpiedSuggest());
        AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).setAddOnEnabled(AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId(), true);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        AnyApplication.getTopRowFactory(RuntimeEnvironment.application).setAddOnEnabled(AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId(), true);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()/*multiple times since an event is fired for each key disabled/enabled*/).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).setAddOnEnabled(AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId(), true);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.atLeastOnce()/*multiple times since an event is fired for each key disabled/enabled*/).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
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