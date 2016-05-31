package com.anysoftkeyboard;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

@RunWith(AskGradleTestRunner.class)
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
        verifyCreatedGenericKeyboard(R.xml.symbols, R.xml.symbols, "symbols_keyboard", KeyboardSwitcher.MODE_TEXT);
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
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.eng_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(), RuntimeEnvironment.application.getString(R.string.symbols_keyboard));
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testSubtypeReported() {
        ArgumentCaptor<InputMethodSubtype> subtypeArgumentCaptor = ArgumentCaptor.forClass(InputMethodSubtype.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setInputMethodAndSubtype(
                Mockito.notNull(IBinder.class),
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
                subtypeArgumentCaptor.capture());
        final InputMethodSubtype subtypeArgumentCaptorValue = subtypeArgumentCaptor.getValue();
        Assert.assertNotNull(subtypeArgumentCaptorValue);
        Assert.assertEquals("en", subtypeArgumentCaptorValue.getLocale());
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", subtypeArgumentCaptorValue.getExtraValue());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Test
    public void testAvailableSubtypesReported() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());
        //inputMethodManager.setAdditionalInputMethodSubtypes(imeId, subtypes.toArray(new InputMethodSubtype[subtypes.size()]));
        ArgumentCaptor<InputMethodSubtype[]> subtypesCaptor = ArgumentCaptor.forClass(InputMethodSubtype[].class);
        final List<KeyboardAddOnAndBuilder> keyboardBuilders = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application);
        mAnySoftKeyboardUnderTest.onAvailableKeyboardsChanged(keyboardBuilders);

        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setAdditionalInputMethodSubtypes(
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
                subtypesCaptor.capture());

        InputMethodSubtype[] reportedSubtypes = subtypesCaptor.getValue();
        Assert.assertNotNull(reportedSubtypes);
        Assert.assertTrue(reportedSubtypes.length > 0);
        Assert.assertEquals(keyboardBuilders.size(), reportedSubtypes.length);
        for (int builderIndex = 0; builderIndex < keyboardBuilders.size(); builderIndex++) {
            KeyboardAddOnAndBuilder builder = keyboardBuilders.get(builderIndex);
            InputMethodSubtype subtype = reportedSubtypes[builderIndex];
            Assert.assertEquals(builder.getKeyboardLocale(), subtype.getLocale());
            Assert.assertEquals(builder.getId(), subtype.getExtraValue());
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardSwitchedOnCurrentInputMethodSubtypeChanged() {
        //enabling ALL keyboards for this test
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).edit();
        for (KeyboardAddOnAndBuilder builder : KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application)) {
            editor.putBoolean(builder.getId(), true);
        }
        editor.commit();

        final KeyboardAddOnAndBuilder keyboardBuilder = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(1);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());
        InputMethodSubtype subtype = new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilder.getId())
                .setSubtypeLocale(keyboardBuilder.getKeyboardLocale())
                .build();
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(subtype);
        ArgumentCaptor<InputMethodSubtype> subtypeArgumentCaptor = ArgumentCaptor.forClass(InputMethodSubtype.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setInputMethodAndSubtype(
                Mockito.notNull(IBinder.class),
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
                subtypeArgumentCaptor.capture());
        final InputMethodSubtype subtypeArgumentCaptorValue = subtypeArgumentCaptor.getValue();
        Assert.assertNotNull(subtypeArgumentCaptorValue);
        Assert.assertEquals(keyboardBuilder.getKeyboardLocale(), subtypeArgumentCaptorValue.getLocale());
        Assert.assertEquals(keyboardBuilder.getId(), subtypeArgumentCaptorValue.getExtraValue());
    }

}