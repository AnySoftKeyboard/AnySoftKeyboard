package com.anysoftkeyboard;

import android.content.res.Configuration;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
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
public class AnySoftKeyboardKeyboardSwitcherTest {
    private ServiceController<TestableAnySoftKeyboard> mAnySoftKeyboardController;
    private TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

    @Before
    public void setUp() throws Exception {
        mAnySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = mAnySoftKeyboardController.attach().create().get();

        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
    }

    @After
    public void tearDown() throws Exception {
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

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.MODE_PHONE, editorInfo, false);
    }

    @Test
    public void testCreatedDateTimeKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_DATETIME);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.MODE_DATETIME, editorInfo, false);
    }

    @Test
    public void testCreatedNumbersKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_NUMBER);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.MODE_NUMBERS, editorInfo, true);
    }

    @Test
    public void testCreatedTextInputKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.MODE_TEXT, editorInfo, true);
    }

    @Test
    public void testCreatedEmailTextInputKeyboard() {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT + EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, true);
        mAnySoftKeyboardUnderTest.onCreateInputView();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, true);

        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setKeyboardMode(KeyboardSwitcher.MODE_EMAIL, editorInfo, true);
    }

    @Test
    public void testForceMakeKeyboardsOnOrientationChange() {
        final Configuration configuration = RuntimeEnvironment.application.getResources().getConfiguration();
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).flushKeyboardsCache();
        //sanity - not changing the orientation should not flush
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).flushKeyboardsCache();
    }

    @Test
    public void testForceMakeKeyboardsOnAddOnsPrefChange() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(KeyboardAddOnAndBuilder.KEYBOARD_PREF_PREFIX + "test", false);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue("dictionary_test", false);
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_active_quick_text_key), "dummy");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_ext_kbd_top_row_key), "dummy");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());
        SharedPrefsHelper.setPrefsValue(RuntimeEnvironment.application.getString(R.string.settings_key_ext_kbd_bottom_row_key), "dummy");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
        Mockito.reset(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher());

        //sanity
        SharedPrefsHelper.setPrefsValue("random", "dummy");
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).flushKeyboardsCache();
        Mockito.verify(mAnySoftKeyboardUnderTest.getSpiedKeyboardSwitcher(), Mockito.never()).setInputView(Mockito.isNotNull(AnyKeyboardView.class));
    }
}