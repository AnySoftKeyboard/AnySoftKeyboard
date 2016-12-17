package com.anysoftkeyboard;

import android.content.Context;
import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public class AnySoftKeyboardKeyboardSwitchingTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testSwitchToSymbols() {
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_alt_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_numbers_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
    }

    @Test
    public void testCreateOrUseCacheKeyboard() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.symbols, R.xml.symbols, "symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.symbols_alt, R.xml.symbols_alt, "alt_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.simple_alt_numbers, R.xml.simple_alt_numbers, "alt_numbers_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        //already created
        verifyNotCreatedGenericKeyboard();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        verifyNotCreatedGenericKeyboard();
        //not creating alphabet keyboard, because it is already created
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyNotCreatedGenericKeyboard();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
    }

    /**
     * Solves https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/719
     */
    @Test
    public void testInvalidateCachedLayoutsWhenInputModeChanges() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_EMAIL, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);

        Assert.assertEquals("symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_EMAIL, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);

        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_EMAIL, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

        //switching input types
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_URI);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_URL, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);

        Assert.assertEquals("symbols_keyboard", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_URL, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

    }

    @Test
    public void testCreateOrUseCacheKeyboardWhen16KeysEnabled() {
        SharedPrefsHelper.setPrefsValue("settings_key_use_16_keys_symbols_keyboards", true);
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.symbols_16keys, R.xml.symbols, "symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.symbols_alt_16keys, R.xml.symbols_alt, "alt_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.simple_alt_numbers, R.xml.simple_alt_numbers, "alt_numbers_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
    }

    private void verifyCreatedGenericKeyboard(int layoutResId, int landscapeLayoutResId, String keyboardId, int mode) {
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).createGenericKeyboard(Mockito.isNotNull(DefaultAddOn.class), Mockito.isNotNull(Context.class),
                Mockito.eq(layoutResId), Mockito.eq(landscapeLayoutResId), Mockito.isNotNull(String.class), Mockito.eq(keyboardId), Mockito.eq(mode), Mockito.anyBoolean());
    }

    private void verifyNotCreatedGenericKeyboard() {
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createGenericKeyboard(
                Mockito.any(AddOn.class), Mockito.any(Context.class), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean());
    }

    @Test
    public void testModeSwitch() {
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
    }

    @Test
    public void testModeSwitchLoadsDictionary() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setMainDictionary(Mockito.any(Context.class), Mockito.any(DictionaryAddOnAndBuilder.class));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setMainDictionary(Mockito.any(Context.class), Mockito.any(DictionaryAddOnAndBuilder.class));
    }

    @Test
    public void testOnKeyboardSetLoadsDictionary() {
        AnyKeyboard alphabetKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        AnyKeyboard symbolsKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedSuggest());
        mAnySoftKeyboardUnderTest.onSymbolsKeyboardSet(symbolsKeyboard);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest(), Mockito.never()).setMainDictionary(Mockito.any(Context.class), Mockito.any(DictionaryAddOnAndBuilder.class));

        mAnySoftKeyboardUnderTest.onAlphabetKeyboardSet(alphabetKeyboard);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedSuggest()).setMainDictionary(Mockito.any(Context.class), Mockito.any(DictionaryAddOnAndBuilder.class));
    }

    @Test
    public void testModeStaysOnConfigurationChange() {
        Configuration configuration = mAnySoftKeyboardUnderTest.getResources().getConfiguration();
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));

        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
    }

    @Test
    public void testCanNotSwitchWhenInLockedMode() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_PHONE);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        final AnyKeyboard phoneKeyboardInstance = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.symbols_phone_keyboard), phoneKeyboardInstance.getKeyboardName());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertSame(phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertSame(phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertSame(phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());

        //and making sure it is unlocked when restarting the input connection
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();
        editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertNotSame(phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
    }
}