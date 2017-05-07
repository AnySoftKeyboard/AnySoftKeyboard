package com.anysoftkeyboard.dictionaries.content;

import android.content.ContentProvider;
import android.content.pm.ProviderInfo;
import android.database.ContentObserver;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.UserDictionary;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.util.ContentProviderController;

import java.util.Collection;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AndroidUserDictionaryTest {

    private ContentProvider mMockedContactsContentProvider;

    @Before
    public void setup() {
        //setting up some dummy contacts
        mMockedContactsContentProvider = Mockito.mock(ContentProvider.class);
        MatrixCursor initialContacts = new MatrixCursor(new String[]{UserDictionary.Words._ID, UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY, UserDictionary.Words.LOCALE});
        Mockito.doReturn(initialContacts).when(mMockedContactsContentProvider).query(Mockito.any(Uri.class), Mockito.any(String[].class), Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString());
        initialContacts.addRow(new Object[]{1, "Dude", 1, "en"});
        initialContacts.addRow(new Object[]{1, "Dudess", 2, "en"});
        initialContacts.addRow(new Object[]{1, "shalom", 10, "iw"});
        initialContacts.addRow(new Object[]{1, "telephone", 2, "iw"});
        initialContacts.addRow(new Object[]{1, "catchall", 5, null});

        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = UserDictionary.Words.CONTENT_URI.getAuthority();
        ContentProviderController.of(mMockedContactsContentProvider).create(providerInfo);
    }

    @Test
    public void testLoadedWordsEN() throws Exception {
        AndroidUserDictionary dictionary = new AndroidUserDictionary(RuntimeEnvironment.application, "en");
        dictionary.loadDictionary();
        Assert.assertTrue(dictionary.isValidWord("Dude"));
        Assert.assertFalse(dictionary.isValidWord("Dudes"));

        ArgumentCaptor<String> selectionArgument = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mMockedContactsContentProvider).query(Mockito.any(Uri.class), Mockito.any(String[].class), selectionArgument.capture(), Mockito.any(String[].class), Mockito.anyString());

        Assert.assertEquals("(" + UserDictionary.Words.LOCALE + " IS NULL) or (" + UserDictionary.Words.LOCALE + "=?)", selectionArgument.getValue());
    }

    @Test
    public void testLoadedWordsNULL() throws Exception {
        AndroidUserDictionary dictionary = new AndroidUserDictionary(RuntimeEnvironment.application, null);
        dictionary.loadDictionary();
        Assert.assertTrue(dictionary.isValidWord("Dude"));
        Assert.assertFalse(dictionary.isValidWord("Dudes"));

        ArgumentCaptor<String> selectionArgument = ArgumentCaptor.forClass(String.class);
        Mockito.verify(mMockedContactsContentProvider).query(Mockito.any(Uri.class), Mockito.any(String[].class), selectionArgument.capture(), Mockito.any(String[].class), Mockito.anyString());

        Assert.assertEquals("(" + UserDictionary.Words.LOCALE + " IS NULL)", selectionArgument.getValue());
    }

    @Test
    public void testLoadedWordsWhenEmpty() throws Exception {
        MatrixCursor initialContacts = new MatrixCursor(new String[]{UserDictionary.Words._ID, UserDictionary.Words.WORD, UserDictionary.Words.FREQUENCY, UserDictionary.Words.LOCALE});
        Mockito.doReturn(initialContacts).when(mMockedContactsContentProvider).query(Mockito.any(Uri.class), Mockito.any(String[].class), Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString());
        AndroidUserDictionary dictionary = new AndroidUserDictionary(RuntimeEnvironment.application, null);
        dictionary.loadDictionary();
        Assert.assertFalse(dictionary.isValidWord("Dude"));
        Assert.assertFalse(dictionary.isValidWord("Dudes"));
        Assert.assertFalse(dictionary.isValidWord("Dudess"));
        Assert.assertFalse(dictionary.isValidWord("shalom"));
        Assert.assertFalse(dictionary.isValidWord("catchall"));
    }

    @Test(expected = RuntimeException.class)
    public void testLoadedWordsWhenNoContentProvider() throws Exception {
        ShadowContentResolver.reset();
        AndroidUserDictionary dictionary = new AndroidUserDictionary(RuntimeEnvironment.application, "en");
        //this should throw an exception, since there is no system content provider
        dictionary.loadDictionary();
    }

    @Test
    public void testRegisterObserver() throws Exception {
        AndroidUserDictionary dictionary = new AndroidUserDictionary(RuntimeEnvironment.application, null);
        dictionary.loadDictionary();

        Collection<ContentObserver> observerList = Shadows.shadowOf(RuntimeEnvironment.application.getContentResolver()).getContentObservers(UserDictionary.Words.CONTENT_URI);
        Assert.assertEquals(1, observerList.size());
    }

}