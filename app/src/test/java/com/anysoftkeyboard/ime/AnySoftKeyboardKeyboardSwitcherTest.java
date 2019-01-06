package com.anysoftkeyboard.ime;

import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_EMAIL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_IM;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_NORMAL;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_PASSWORD;
import static com.anysoftkeyboard.keyboards.Keyboard.KEYBOARD_ROW_MODE_URL;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.addons.SupportTest;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardKeyboardSwitcherTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testOnLowMemoryAlphabet() {
        SupportTest.ensureKeyboardAtIndexEnabled(0, true);
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        SupportTest.ensureKeyboardAtIndexEnabled(2, true);

        simulateOnStartInputFlow();

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
        SupportTest.ensureKeyboardAtIndexEnabled(0, true);
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        SupportTest.ensureKeyboardAtIndexEnabled(2, true);

        simulateOnStartInputFlow();

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

        Assert.assertEquals("phone_symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_PHONE, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getInputModeId());
    }

    @Test
    public void testCreatedDateTimeKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_DATETIME);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals("datetime_symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_DATETIME, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getInputModeId());
    }

    @Test
    public void testCreatedNumbersKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_NUMBER);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertEquals("numbers_symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_NUMBERS, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getInputModeId());
    }

    @Test
    public void testCreatedTextInputKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_TEXT, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getInputModeId());
    }

    @Test
    public void testCreatedEmailTextInputKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_EMAIL, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getInputModeId());
    }

    @Test
    public void testCreatedPasswordTextInputKeyboard() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        //just a normal text keyboard
        Assert.assertEquals(KeyboardSwitcher.INPUT_MODE_TEXT, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getInputModeId());
        //with password row mode
        Assert.assertEquals(KEYBOARD_ROW_MODE_PASSWORD, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());
    }

    private void verifyMaskedKeyboardRow(@Keyboard.KeyboardRowModeId int modeId, int inputModeId, int variant) {
        SharedPrefsHelper.setPrefsValue(Keyboard.getPrefKeyForEnabledRowMode(modeId), false);

        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + variant);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        //just a normal text keyboard
        Assert.assertEquals(inputModeId, mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().getInputModeId());
        //with NORMAL row mode, since the pref is false
        Assert.assertEquals(KEYBOARD_ROW_MODE_NORMAL, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenPasswordFieldButOptionDisabled() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_PASSWORD, KeyboardSwitcher.INPUT_MODE_TEXT, EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenPasswordFieldButOptionDisabledVisiblePassword() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_PASSWORD, KeyboardSwitcher.INPUT_MODE_TEXT, EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenPasswordFieldButOptionDisabledWeb() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_PASSWORD, KeyboardSwitcher.INPUT_MODE_TEXT, EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD);
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenUrlFieldButOptionDisabled() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_URL, KeyboardSwitcher.INPUT_MODE_URL, EditorInfo.TYPE_TEXT_VARIATION_URI);
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenEmailAddressFieldButOptionDisabled() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_EMAIL, KeyboardSwitcher.INPUT_MODE_EMAIL, EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenWebEmailAddressFieldButOptionDisabled() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_EMAIL, KeyboardSwitcher.INPUT_MODE_EMAIL, EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenShortMessageFieldButOptionDisabled() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_IM, KeyboardSwitcher.INPUT_MODE_IM, EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenShortMessageFieldButOptionDisabledEmailSubject() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_IM, KeyboardSwitcher.INPUT_MODE_TEXT, EditorInfo.TYPE_TEXT_VARIATION_EMAIL_SUBJECT);
    }

    @Test
    public void testCreatedNormalTextInputKeyboardWhenShortMessageFieldButOptionDisabledLongMessage() {
        verifyMaskedKeyboardRow(KEYBOARD_ROW_MODE_IM, KeyboardSwitcher.INPUT_MODE_TEXT, EditorInfo.TYPE_TEXT_VARIATION_LONG_MESSAGE);
    }

    @Test
    public void testKeyboardsRecycledOnPasswordRowSupportPrefChange() {
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();//initial. It will reset flush state

        SharedPrefsHelper.setPrefsValue(Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL), false);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();

        SharedPrefsHelper.setPrefsValue(Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();

        //same value
        SharedPrefsHelper.setPrefsValue(Keyboard.getPrefKeyForEnabledRowMode(Keyboard.KEYBOARD_ROW_MODE_EMAIL), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsNotFlushed();
    }

    @Test
    public void testForceMakeKeyboardsOnOrientationChange() {
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();//initial. It will reset flush state

        Assert.assertFalse(mAnySoftKeyboardUnderTest.isKeyboardViewHidden());
        final Configuration configuration = getApplicationContext().getResources().getConfiguration();
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        //same orientation
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
    }

    @Test
    public void testForceMakeKeyboardsOnAddOnsPrefChange() {
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewSet();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        SharedPrefsHelper.setPrefsValue("keyboard_some-id_override_dictionary", "dictionary_id");
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsNotFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewNotSet();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).resetNextWordSentence();
        //no UI, no setup of suggestions dictionaries
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        AnyApplication.getQuickTextKeyFactory(getApplicationContext()).setAddOnEnabled(AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getAllAddOns().get(1).getId(),
                true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewNotSet();
        AnyApplication.getTopRowFactory(getApplicationContext()).setAddOnEnabled(AnyApplication.getTopRowFactory(getApplicationContext()).getAllAddOns().get(1).getId(), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewNotSet();
        AnyApplication.getBottomRowFactory(getApplicationContext()).setAddOnEnabled(AnyApplication.getBottomRowFactory(getApplicationContext()).getAllAddOns().get(1).getId(), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewNotSet();
        SharedPrefsHelper.setPrefsValue(getApplicationContext().getString(R.string.settings_key_always_hide_language_key), true);
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewNotSet();

        //sanity
        SharedPrefsHelper.setPrefsValue("random", "dummy");
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsNotFlushed();
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyNewViewNotSet();
    }
}