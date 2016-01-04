package com.anysoftkeyboard;

import android.content.Context;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;
import com.menny.android.anysoftkeyboard.R;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ServiceController;

@RunWith(AskGradleTestRunner.class)
public class AnySoftKeyboardKeyboardSwitchingTest {
    private ServiceController<TestableAnySoftKeyboard> mAnySoftKeyboardController;
    private TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

    @Before
    public void setUp() throws Exception {
        mAnySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = mAnySoftKeyboardController.attach().create().get();

        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).createKeyboardFromCreator(Mockito.eq(KeyboardSwitcher.MODE_TEXT), Mockito.isNotNull(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSwitchToSymbols() {
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_alt_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_numbers_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
    }

    @Test
    public void testCreateOrUseCacheKeyboard() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.symbols, R.xml.symbols, "symbols_keyboard", KeyboardSwitcher.MODE_TEXT);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.symbols_alt, R.xml.symbols_alt, "alt_symbols_keyboard", KeyboardSwitcher.MODE_TEXT);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.simple_alt_numbers, R.xml.simple_alt_numbers, "alt_numbers_symbols_keyboard", KeyboardSwitcher.MODE_TEXT);
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

    @Test
    public void testCreateOrUseCacheKeyboardWhen16KeysEnabled() {
        SharedPrefsHelper.setPrefsValue("settings_key_use_16_keys_symbols_keyboards", true);
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.symbols_16keys, R.xml.symbols, "symbols_keyboard", KeyboardSwitcher.MODE_TEXT);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.symbols_alt_16keys, R.xml.symbols_alt, "alt_symbols_keyboard", KeyboardSwitcher.MODE_TEXT);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).createKeyboardFromCreator(Mockito.anyInt(), Mockito.any(KeyboardAddOnAndBuilder.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(R.xml.simple_alt_numbers, R.xml.simple_alt_numbers, "alt_numbers_symbols_keyboard", KeyboardSwitcher.MODE_TEXT);
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
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
    }

    @Test
    public void testCanNotSwitchWhenInLockedMode() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_PHONE);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        final AnyKeyboard phoneKeyboardInstance = mAnySoftKeyboardUnderTest.getCurrentKeyboard();
        Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.symbols_phone_keyboard), phoneKeyboardInstance.getKeyboardName());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertSame(phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboard());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertSame(phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboard());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertSame(phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboard());

        //and making sure it is unlocked when restarting the input connection
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();
        editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertNotSame(phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboard());
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboard().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
    }

}