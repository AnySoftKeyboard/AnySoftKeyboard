package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.app.AlertDialog;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AddOnTestUtils;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.utils.GeneralDialogTestUtil;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardKeyboardSwitchingTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testSwitchToSymbols() {
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.english_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.symbols_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.symbols_alt_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.symbols_numbers_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.symbols_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.english_keyboard));
    }

    @Test
    public void testCreateOrUseCacheKeyboard() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard("symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        final AnyKeyboard symbolsKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard("alt_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        final AnyKeyboard altSymbolsKeyboard =
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(
                "alt_numbers_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        final AnyKeyboard altNumbersSymbolsKeyboard =
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        Assert.assertNotSame(symbolsKeyboard, altSymbolsKeyboard);
        Assert.assertNotSame(altSymbolsKeyboard, altNumbersSymbolsKeyboard);
        Assert.assertNotSame(altNumbersSymbolsKeyboard, symbolsKeyboard);
        // already created
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertSame(symbolsKeyboard, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertSame(symbolsKeyboard, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
    }

    /** Solves https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/719 */
    @Test
    public void testInvalidateCachedLayoutsWhenInputModeChanges() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        Assert.assertEquals(
                Keyboard.KEYBOARD_ROW_MODE_EMAIL,
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);

        Assert.assertEquals(
                "symbols_keyboard",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        Assert.assertEquals(
                Keyboard.KEYBOARD_ROW_MODE_EMAIL,
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        Assert.assertEquals(
                Keyboard.KEYBOARD_ROW_MODE_EMAIL,
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

        // switching input types
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_URI);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        Assert.assertEquals(
                Keyboard.KEYBOARD_ROW_MODE_URL,
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);

        Assert.assertEquals(
                "symbols_keyboard",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        Assert.assertEquals(
                Keyboard.KEYBOARD_ROW_MODE_URL,
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());
    }

    @Test
    public void testCreateOrUseCacheKeyboardWhen16KeysEnabled() {
        SharedPrefsHelper.setPrefsValue("settings_key_use_16_keys_symbols_keyboards", true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard("symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        final AnyKeyboard symbolsKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard("alt_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        final AnyKeyboard altSymbolsKeyboard =
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(
                "alt_numbers_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        final AnyKeyboard altNumbersSymbolsKeyboard =
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        // all newly created
        Assert.assertNotSame(symbolsKeyboard, altSymbolsKeyboard);
        Assert.assertNotSame(altSymbolsKeyboard, altNumbersSymbolsKeyboard);
        Assert.assertNotSame(altNumbersSymbolsKeyboard, symbolsKeyboard);

        // now, cycling should use cached instances
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard("symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        Assert.assertSame(symbolsKeyboard, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard("alt_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        Assert.assertSame(
                altSymbolsKeyboard, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        verifyCreatedGenericKeyboard(
                "alt_numbers_symbols_keyboard", KeyboardSwitcher.INPUT_MODE_TEXT);
        Assert.assertSame(
                altNumbersSymbolsKeyboard, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
    }

    private void verifyCreatedGenericKeyboard(String keyboardId, int mode) {
        Assert.assertTrue(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests() instanceof GenericKeyboard);
        Assert.assertEquals(
                mode, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardMode());
        Assert.assertEquals(
                keyboardId, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
    }

    @Test
    public void testModeSwitch() {
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.english_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.symbols_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.english_keyboard));
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.symbols_keyboard));
    }

    @Test
    public void testModeSwitchLoadsDictionary() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
    }

    @Test
    public void testOnKeyboardSetLoadsDictionary() {
        AnyKeyboard alphabetKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        AnyKeyboard symbolsKeyboard = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();

        Mockito.reset(mAnySoftKeyboardUnderTest.getSuggest());
        mAnySoftKeyboardUnderTest.onSymbolsKeyboardSet(symbolsKeyboard);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest(), Mockito.never())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());

        mAnySoftKeyboardUnderTest.onAlphabetKeyboardSet(alphabetKeyboard);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSuggest())
                .setupSuggestionsForKeyboard(Mockito.anyList(), Mockito.any());
    }

    @Test
    public void testModeSwitchesOnConfigurationChange() {
        Configuration configuration = mAnySoftKeyboardUnderTest.getResources().getConfiguration();
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.english_keyboard));
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.english_keyboard));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.symbols_keyboard));

        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        // switches back to symbols since this is a non-restarting event.
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.english_keyboard));
    }

    @Test
    public void testCanNotSwitchWhenInLockedMode() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_PHONE);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        final AnyKeyboard phoneKeyboardInstance =
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests();
        Assert.assertEquals(
                getApplicationContext().getString(R.string.symbols_phone_keyboard),
                phoneKeyboardInstance.getKeyboardName());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertSame(
                phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertSame(
                phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertSame(
                phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());

        // and making sure it is unlocked when restarting the input connection
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();
        editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertNotSame(
                phoneKeyboardInstance, mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests());
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardName(),
                getApplicationContext().getString(R.string.english_keyboard));
    }

    @Test
    public void testShowSelectedKeyboardForURLField() {
        Resources resources = getApplicationContext().getResources();
        // default value should be first keyboard
        final KeyboardFactory keyboardFactory =
                AnyApplication.getKeyboardFactory(getApplicationContext());
        Assert.assertEquals(
                resources.getString(R.string.settings_default_keyboard_id),
                keyboardFactory.getEnabledIds().get(0));

        AddOnTestUtils.ensureKeyboardAtIndexEnabled(0, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(2, true);

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(0));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);

        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(1));

        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        mAnySoftKeyboardUnderTest.onFinishInput();
        editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(1));

        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        mAnySoftKeyboardUnderTest.onFinishInput();
        editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_URI);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        // automatically switched to the keyboard in the prefs
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(0));

        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        mAnySoftKeyboardUnderTest.onFinishInput();

        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_layout_for_internet_fields,
                keyboardFactory.getEnabledIds().get(2).toString());

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        // automatically switched to the keyboard in the prefs
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(2));
    }

    @Test
    public void testShowPreviousKeyboardIfInternetKeyboardPrefIdIsInvalid() {
        final KeyboardFactory keyboardFactory =
                AnyApplication.getKeyboardFactory(getApplicationContext());

        AddOnTestUtils.ensureKeyboardAtIndexEnabled(0, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(2, true);

        EditorInfo editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);

        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(1));

        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        mAnySoftKeyboardUnderTest.onFinishInput();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_layout_for_internet_fields, "none");

        editorInfo =
                TestableAnySoftKeyboard.createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_URI);

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(1));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);

        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(2));

        mAnySoftKeyboardUnderTest.onFinishInputView(false);
        mAnySoftKeyboardUnderTest.onFinishInput();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId(),
                keyboardFactory.getEnabledIds().get(2));
    }

    @Test
    public void testLanguageDialogShowLanguagesAndSettings() {
        Assert.assertSame(
                GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

        AddOnTestUtils.ensureKeyboardAtIndexEnabled(0, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(2, true);

        mAnySoftKeyboardUnderTest.onKey(KeyCodes.MODE_ALPHABET_POPUP, null, 0, null, true);

        final AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);

        Assert.assertEquals(
                "Select keyboard", GeneralDialogTestUtil.getTitleFromDialog(latestAlertDialog));
        Assert.assertEquals(4, latestAlertDialog.getListView().getCount());

        Assert.assertEquals(
                getResText(R.string.english_keyboard),
                latestAlertDialog.getListView().getAdapter().getItem(0));
        Assert.assertEquals(
                getResText(R.string.compact_keyboard_16keys),
                latestAlertDialog.getListView().getAdapter().getItem(1));
        Assert.assertEquals(
                getResText(R.string.english_keyboard),
                latestAlertDialog.getListView().getAdapter().getItem(2));
        Assert.assertEquals(
                "Setup languages…", latestAlertDialog.getListView().getAdapter().getItem(3));
    }

    @Test
    public void testLanguageDialogSwitchLanguage() {
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(0, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(2, true);

        mAnySoftKeyboardUnderTest.onKey(KeyCodes.MODE_ALPHABET_POPUP, null, 0, null, true);

        final AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());

        Shadows.shadowOf(latestAlertDialog.getListView()).performItemClick(1);

        Assert.assertEquals(
                "12335055-4aa6-49dc-8456-c7d38a1a5123",
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
    }

    @Test
    public void testLanguageDialogGoToSettings() {
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(0, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(2, true);

        mAnySoftKeyboardUnderTest.onKey(KeyCodes.MODE_ALPHABET_POPUP, null, 0, null, true);

        Assert.assertNull(
                Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                        .getNextStartedActivity());

        Shadows.shadowOf(GeneralDialogTestUtil.getLatestShownDialog().getListView())
                .performItemClick(3);
        Intent settingsIntent =
                Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                        .getNextStartedActivity();
        Assert.assertNotNull(settingsIntent);
        Assert.assertEquals(
                getApplicationContext().getPackageName(),
                settingsIntent.getComponent().getPackageName());
        Assert.assertEquals(
                MainSettingsActivity.class.getName(), settingsIntent.getComponent().getClassName());
        Assert.assertEquals(
                "keyboards",
                settingsIntent
                        .getExtras()
                        .getString(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID));
        Assert.assertEquals(Intent.ACTION_VIEW, settingsIntent.getAction());
        Assert.assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, settingsIntent.getFlags());
    }
}
