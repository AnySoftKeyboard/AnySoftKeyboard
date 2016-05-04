package com.anysoftkeyboard;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;
import com.menny.android.anysoftkeyboard.R;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ServiceController;

@RunWith(AskGradleTestRunner.class)
public class AnySoftKeyboardKeyboardPersistentLayoutTest {
    private TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

    @Before
    public void setUp() throws Exception {
        RuntimeEnvironment.application.getResources().getConfiguration().keyboard = Configuration.KEYBOARD_NOKEYS;
        //enabling the second english keyboard
        Assert.assertEquals(1, KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).size());
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        sharedPreferences.edit().putBoolean("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", true).commit();
        Assert.assertEquals(2, KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).size());
        //starting service
        ServiceController<TestableAnySoftKeyboard> anySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = anySoftKeyboardController.attach().create().get();

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
        if (mAnySoftKeyboardUnderTest.onShowInputRequested(0, configChange)) {
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
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_SYMOBLS);
        Assert.assertEquals("DEFAULT_ADD_ON", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
    }

    @Test
    public void testLayoutPersistentWithPackageId() {
        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }

    @Test
    public void testLayoutPersistentWithPackageIdOnConfigurationChanged() {
        Configuration configuration = mAnySoftKeyboardUnderTest.getResources().getConfiguration();
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        configuration.orientation = Configuration.ORIENTATION_LANDSCAPE;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);
        configuration.orientation = Configuration.ORIENTATION_PORTRAIT;
        mAnySoftKeyboardUnderTest.onConfigurationChanged(configuration);

        startInputFromPackage("com.app2", true/*restarting the same input*/, true/*this is a config change*/);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }

    @Test
    public void testLayoutResetPersistentWithPackageIdWhenLayoutDisabled() {
        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        sharedPreferences.edit().putBoolean("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", false).commit();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }

    @Test
    public void testLayoutNotPersistentWithPackageIdIfPrefIsDisabled() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
        sharedPreferences.edit().putBoolean(RuntimeEnvironment.application.getString(R.string.settings_key_persistent_layout_per_package_id), false).commit();

        AskPrefs askPrefs = new AskPrefsImpl(RuntimeEnvironment.application);
        Assert.assertFalse(askPrefs.getPersistLayoutForPackageId());

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app1");
        Assert.assertEquals("keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();

        startInputFromPackage("com.app2");
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardAddOn().getId());
        finishInput();
    }
}