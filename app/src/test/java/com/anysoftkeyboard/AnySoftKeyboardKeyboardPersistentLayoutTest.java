package com.anysoftkeyboard;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.SharedPreferencesCompat;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;

import com.anysoftkeyboard.api.KeyCodes;
import com.menny.android.anysoftkeyboard.R;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ServiceController;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnySoftKeyboardKeyboardPersistentLayoutTest {
    private TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;
    private ServiceController<TestableAnySoftKeyboard> mAnySoftKeyboardController;

    @Before
    public void setUp() throws Exception {
        RuntimeEnvironment.application.getResources().getConfiguration().keyboard = Configuration.KEYBOARD_NOKEYS;
        //enabling the second english keyboard
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, true);
        //starting service
        mAnySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = mAnySoftKeyboardController.create().get();

        mAnySoftKeyboardUnderTest.onCreateInputView();
    }

    @After
    public void tearDown() throws Exception {
    }

    private void startInputFromPackage(@Nullable String packageId, boolean restarting, boolean configChange) {
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        editorInfo.packageName = packageId;
        editorInfo.fieldId = packageId == null ? 0 : packageId.hashCode();

        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, restarting);
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(InputMethod.SHOW_EXPLICIT, configChange)) {
            mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, restarting);
        }
    }

    private void startInputFromPackage(@Nullable String packageId) {
        startInputFromPackage(packageId, false, false);
    }

    private void finishInput() {
        mAnySoftKeyboardUnderTest.onFinishInputView(true);
        mAnySoftKeyboardUnderTest.onFinishInput();
    }

    @Test
    public void testDefaultPrefValueIsPersistentEnabled() {
        AskPrefs askPrefs = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertTrue(askPrefs.getPersistLayoutForPackageId());
    }

    @Test
    public void testSwitchLayouts() {
        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals("DEFAULT_ADD_ON", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
    }

    @Test
    public void testLayoutPersistentWithPackageId() {
        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }

    @Test
    public void testLayoutPersistentWithPackageIdOnConfigurationChanged() {
        Configuration configuration = mAnySoftKeyboardUnderTest.getResources().getConfiguration();
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);

        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);

        startInputFromPackage("com.app2", true/*restarting the same input*/, true/*this is a config change*/);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);

        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
    }

    @Test
    public void testLayoutResetPersistentWithPackageIdWhenLayoutDisabled() {
        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, false);

        startInputFromPackage("com.app2");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }

    @Test
    public void testLayoutNotPersistentWithPackageIdIfPrefIsDisabled() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        final SharedPreferences.Editor editor = sharedPreferences.edit().putBoolean(RuntimeEnvironment.application.getString(R.string.settings_key_persistent_layout_per_package_id), false);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);

        AskPrefs askPrefs = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(askPrefs.getPersistLayoutForPackageId());

        startInputFromPackage("com.app1");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }

    @Test
    public void testPersistentLastLayoutAcrossServiceRestarts() {
        finishInput();

        startInputFromPackage("com.app2");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        mAnySoftKeyboardController.destroy();


        mAnySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = mAnySoftKeyboardController.attach().create().get();

        mAnySoftKeyboardUnderTest.onCreateInputView();


        startInputFromPackage("com.app2");
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }

    @Test
    public void testDoesNotPersistentLastLayoutAcrossServiceRestartsWhenSettingIsDisabled() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        final SharedPreferences.Editor editor = sharedPreferences.edit().putBoolean(RuntimeEnvironment.application.getString(R.string.settings_key_persistent_layout_per_package_id), false);
        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);

        finishInput();

        startInputFromPackage("com.app2");
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        mAnySoftKeyboardController.destroy();


        mAnySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = mAnySoftKeyboardController.attach().create().get();

        mAnySoftKeyboardUnderTest.onCreateInputView();


        startInputFromPackage("com.app2");
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }
}