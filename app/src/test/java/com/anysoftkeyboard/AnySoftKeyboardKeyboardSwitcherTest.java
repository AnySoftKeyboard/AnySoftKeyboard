package com.anysoftkeyboard;

import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_PASSWORD;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnySoftKeyboardKeyboardSwitcherTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testOnLowMemoryAlphabet() {
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(0, true);
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, true);
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(2, true);

        //creating all keyboards
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);

        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().size());
        for (AnyKeyboard keyboard : mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards()) {
            Assert.assertNotNull(keyboard);
        }

        Assert.assertEquals(6, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().size());
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(0));
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(1));
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(2));
        //special modes keyboards which were not created yet
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(3));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(4));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(5));

        mAnySoftKeyboardUnderTest.onLowMemory();

        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().size());
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().get(0));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().get(1));
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().get(2));

        Assert.assertEquals(6, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().size());
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(0));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(1));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(2));
        //special modes keyboards which were not created yet
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(3));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(4));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(5));
    }

    @Test
    public void testOnLowMemorySymbols() {
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(0, true);
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, true);
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(2, true);

        //creating all keyboards
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);

        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().size());
        for (AnyKeyboard keyboard : mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards()) {
            Assert.assertNotNull(keyboard);
        }

        Assert.assertEquals(6, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().size());
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(0));
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(1));
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(2));
        //special modes keyboards which were not created yet
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(3));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(4));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(5));

        mAnySoftKeyboardUnderTest.onLowMemory();

        Assert.assertEquals(3, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().size());
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().get(0));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().get(1));
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedAlphabetKeyboards().get(2));

        Assert.assertEquals(6, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().size());
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(0));
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(1));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(2));
        //special modes keyboards which were not created yet
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(3));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(4));
        Assert.assertNull(mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getCachedSymbolsKeyboards().get(5));
    }

    @Test
    public void testForceRecreateKeyboardOnSettingKeyboardView() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewSet();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
    }

    @Test
    public void testCreatedPhoneKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_PHONE);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals("phone_symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_PHONE, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getKeyboardModeSet());
    }

    @Test
    public void testCreatedDateTimeKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_DATETIME);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals("datetime_symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_DATETIME, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getKeyboardModeSet());
    }

    @Test
    public void testCreatedNumbersKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_NUMBER);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertEquals("numbers_symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_NUMBERS, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getKeyboardModeSet());
    }

    @Test
    public void testCreatedTextInputKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_TEXT, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getKeyboardModeSet());
    }

    @Test
    public void testCreatedEmailTextInputKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_EMAIL, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getKeyboardModeSet());
    }

    @Test
    public void testCreatedPasswordTextInputKeyboard() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        //just a normal text keyboard
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_TEXT, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getKeyboardModeSet());
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
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        //just a normal text keyboard
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_TEXT, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getKeyboardModeSet());
        //with NORMAL row mode, since the pref is false
        Assert.assertEquals(KEYBOARD_ROW_MODE_NORMAL, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());
    }

    @Test
    public void testKeyboardsRecycledOnPasswordRowSupportPrefChange() {
        Assert.assertTrue(AnyApplication.getConfig().supportPasswordKeyboardRowMode());
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, false);
        Assert.assertFalse(AnyApplication.getConfig().supportPasswordKeyboardRowMode());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, true);
        Assert.assertTrue(AnyApplication.getConfig().supportPasswordKeyboardRowMode());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();

        //same value
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_support_password_keyboard_type_state, true);
        Assert.assertTrue(AnyApplication.getConfig().supportPasswordKeyboardRowMode());
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsNotFlushed();
    }

    @Test
    public void testForceMakeKeyboardsOnOrientationChange() {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        final Configuration configuration = RuntimeEnvironment.application.getResources().getConfiguration();
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        //Assert.assertTrue(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        //sanity - not changing the orientation should not flush
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsNotFlushed();
    }

    @Test
    public void testForceMakeKeyboardsOnAddOnsPrefChange() {
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewSet();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        SharedPrefsHelper.setPrefsValue("keyboard_some-id_override_dictionary", "dictionary_id");
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsNotFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewNotSet();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).resetNextWordSentence();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setupSuggestionsForKeyboard(Mockito.anyListOf(DictionaryAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).setAddOnEnabled(AnyApplication.getQuickTextKeyFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId(), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewSet();
        AnyApplication.getTopRowFactory(RuntimeEnvironment.application).setAddOnEnabled(AnyApplication.getTopRowFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId(), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewSet();
        AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).setAddOnEnabled(AnyApplication.getBottomRowFactory(RuntimeEnvironment.application).getAllAddOns().get(1).getId(), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewSet();
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_always_hide_language_key), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewSet();

        //sanity
        SharedPrefsHelper.setPrefsValue("random", "dummy");
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsNotFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewNotSet();
    }
}