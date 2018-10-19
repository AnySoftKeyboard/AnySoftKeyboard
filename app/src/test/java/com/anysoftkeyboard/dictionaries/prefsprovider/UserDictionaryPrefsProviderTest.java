package com.anysoftkeyboard.dictionaries.prefsprovider;

import static java.util.Arrays.asList;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.test.TestUtils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class UserDictionaryPrefsProviderTest {

    @Test
    public void testHappyPath() throws Exception {
        UserDictionary enUserDictionary = new UserDictionary(getApplicationContext(), "en");
        enUserDictionary.loadDictionary();
        enUserDictionary.addWord("hello", 1);
        enUserDictionary.addWord("yo", 2);
        enUserDictionary.addWord("shalom", 3);
        Assert.assertTrue(enUserDictionary.isValidWord("hello"));
        enUserDictionary.close();

        UserDictionary frUserDictionary = new UserDictionary(getApplicationContext(), "fr");
        frUserDictionary.loadDictionary();
        frUserDictionary.addWord("Avoir", 1);
        frUserDictionary.addWord("Faire", 2);
        frUserDictionary.addWord("Demander", 3);
        Assert.assertTrue(frUserDictionary.isValidWord("Demander"));
        frUserDictionary.close();

        UserDictionary nullUserDictionary = new UserDictionary(getApplicationContext(), null);
        nullUserDictionary.loadDictionary();
        nullUserDictionary.addWord("WHAT", 1);
        nullUserDictionary.addWord("IS", 2);
        nullUserDictionary.addWord("NULL", 3);
        Assert.assertTrue(nullUserDictionary.isValidWord("NULL"));
        nullUserDictionary.close();

        UserDictionaryPrefsProvider underTest = new UserDictionaryPrefsProvider(getApplicationContext(), asList("en", "fr", null));
        final PrefsRoot prefsRoot = underTest.getPrefsRoot();

        Assert.assertEquals(1, prefsRoot.getVersion());
        final List<PrefItem> localeItems = TestUtils.convertToList(prefsRoot.getChildren());
        Assert.assertEquals(3, localeItems.size());

        //deleting storage
        enUserDictionary = new UserDictionary(getApplicationContext(), "en");
        enUserDictionary.loadDictionary();
        enUserDictionary.deleteWord("hello");
        enUserDictionary.deleteWord("yo");
        enUserDictionary.deleteWord("shalom");
        Assert.assertFalse(enUserDictionary.isValidWord("hello"));
        enUserDictionary.close();

        frUserDictionary = new UserDictionary(getApplicationContext(), "fr");
        frUserDictionary.loadDictionary();
        frUserDictionary.deleteWord("Avoir");
        frUserDictionary.deleteWord("Faire");
        frUserDictionary.deleteWord("Demander");
        Assert.assertFalse(frUserDictionary.isValidWord("Demander"));
        frUserDictionary.close();

        nullUserDictionary = new UserDictionary(getApplicationContext(), null);
        nullUserDictionary.loadDictionary();
        nullUserDictionary.deleteWord("WHAT");
        nullUserDictionary.deleteWord("IS");
        nullUserDictionary.deleteWord("NULL");
        Assert.assertFalse(nullUserDictionary.isValidWord("NULL"));
        nullUserDictionary.close();

        underTest.storePrefsRoot(prefsRoot);

        enUserDictionary = new UserDictionary(getApplicationContext(), "en");
        enUserDictionary.loadDictionary();
        Assert.assertTrue(enUserDictionary.isValidWord("hello"));
        Assert.assertTrue(enUserDictionary.isValidWord("yo"));
        Assert.assertTrue(enUserDictionary.isValidWord("shalom"));
        enUserDictionary.close();

        verifyLocale("en", "hello", "Avoir");
        verifyLocale("fr", "Avoir", "shalom");
    }

    private void verifyLocale(String locale, String validWord, String invalidWord) {
        UserDictionary userDictionary = new UserDictionary(getApplicationContext(), locale);
        userDictionary.loadDictionary();
        Assert.assertTrue(userDictionary.isValidWord(validWord));
        Assert.assertFalse(userDictionary.isValidWord(invalidWord));
        userDictionary.close();
    }
}