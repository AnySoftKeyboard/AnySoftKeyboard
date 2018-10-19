package com.anysoftkeyboard.prefs;

import static com.anysoftkeyboard.prefs.RxSharedPrefs.CONFIGURATION_VERSION;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.content.SharedPreferences;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SharedPrefsProviderTest {

    private SharedPreferences mSharedPreferences;
    private RxSharedPrefs.SharedPrefsProvider mUnderTest;

    @Before
    public void setup() {
        mSharedPreferences = getApplicationContext().getSharedPreferences("for_test.xml", Context.MODE_PRIVATE);
        //ensuring it's empty
        clearAllPrefs();

        mUnderTest = new RxSharedPrefs.SharedPrefsProvider(mSharedPreferences);
    }

    private void clearAllPrefs() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (Map.Entry<String, ?> entry : mSharedPreferences.getAll().entrySet()) {
            editor.remove(entry.getKey());
        }
        editor.commit();

        Assert.assertEquals(0, mSharedPreferences.getAll().size());
    }

    @Test
    public void testHappyPath() throws Exception {
        mSharedPreferences.edit()
                .putString("stringKey", "String value")
                .putString("stringKeyNull", null)//this is empty! So it does not count
                .putInt("intKey", 222)
                .putBoolean("boolKey", true)
                .putBoolean("boolKey2", false).commit();


        final PrefsRoot prefsRoot = mUnderTest.getPrefsRoot();
        clearAllPrefs();

        mUnderTest.storePrefsRoot(prefsRoot);

        Assert.assertEquals(5 /*will have the PREFS version*/, mSharedPreferences.getAll().size());
        Assert.assertEquals("String value", mSharedPreferences.getString("stringKey", "empty"));
        Assert.assertEquals(222, mSharedPreferences.getInt("intKey", 2));
        Assert.assertEquals(true, mSharedPreferences.getBoolean("boolKey", false));
        Assert.assertEquals(false, mSharedPreferences.getBoolean("boolKey2", true));
        //this field is not stored, so the default value will be retrieved.
        Assert.assertEquals("empty", mSharedPreferences.getString("stringKeyNull", "empty"));
        //the upgrade process will store the configuration version
        Assert.assertEquals(RxSharedPrefs.CONFIGURATION_LEVEL_VALUE, mSharedPreferences.getInt(CONFIGURATION_VERSION, 0));
    }

    @Test
    public void testDoesNotKeepStuffFromBeforeRestore() throws Exception {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("stringKey", "String value").commit();

        final PrefsRoot prefsRoot = mUnderTest.getPrefsRoot();
        //adds something else
        editor.putString("stringKey2", "String value 2").commit();

        mUnderTest.storePrefsRoot(prefsRoot);
        Assert.assertEquals("String value", mSharedPreferences.getString("stringKey", "empty"));
        Assert.assertFalse(mSharedPreferences.contains("stringKey2"));
    }

    @Test
    public void testDoesNotStoreStringSet() throws Exception {
        mSharedPreferences.edit().putStringSet("stringKey", new HashSet<>(Arrays.asList("v1", "v2", "v3"))).commit();

        Assert.assertEquals(3, mSharedPreferences.getStringSet("stringKey", Collections.emptySet()).size());

        final PrefsRoot prefsRoot = mUnderTest.getPrefsRoot();

        mUnderTest.storePrefsRoot(prefsRoot);
        Assert.assertEquals(0, mSharedPreferences.getStringSet("stringKey", Collections.emptySet()).size());
    }
}