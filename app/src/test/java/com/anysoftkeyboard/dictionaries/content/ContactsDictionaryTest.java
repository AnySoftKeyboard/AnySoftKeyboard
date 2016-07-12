package com.anysoftkeyboard.dictionaries.content;

import android.Manifest;
import android.content.ContentProvider;
import android.database.ContentObserver;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.ContactsContract;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowContentResolver;

import java.util.Collection;
import java.util.Iterator;

@RunWith(RobolectricGradleTestRunner.class)
public class ContactsDictionaryTest {
    private ContactsDictionary mDictionaryUnderTest;
    private ContentProvider mMockedContactsContentProvider;

    @Before
    public void setup() {
        mDictionaryUnderTest = new ContactsDictionary(RuntimeEnvironment.application);
        setAllowContactsRead(true);
        //setting up some dummy contacts
        mMockedContactsContentProvider = Mockito.mock(ContentProvider.class);
        MatrixCursor initialContacts = new MatrixCursor(new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.STARRED, ContactsContract.Contacts.TIMES_CONTACTED});
        Mockito.doReturn(initialContacts).when(mMockedContactsContentProvider).query(Mockito.any(Uri.class), Mockito.any(String[].class), Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString());
        initialContacts.addRow(new Object[]{1, "Menny Even-Danan", 1, 10});
        initialContacts.addRow(new Object[]{2, "Jonathan With'In", 0, 100});
        initialContacts.addRow(new Object[]{3, "Erela Portugaly", 1, 10});
        initialContacts.addRow(new Object[]{4, "John Smith", 0, 1});
        initialContacts.addRow(new Object[]{5, "John Lennon", 1, 126});
        initialContacts.addRow(new Object[]{6, "Mika Michael Michelle", 1, 10});
        ShadowContentResolver.registerProvider(ContactsContract.Contacts.CONTENT_URI.getAuthority(), mMockedContactsContentProvider);

        mDictionaryUnderTest.loadDictionary();
    }

    private void setAllowContactsRead(boolean enabled) {
        if (enabled)
            Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.READ_CONTACTS);
        else
            Shadows.shadowOf(RuntimeEnvironment.application).denyPermissions(Manifest.permission.READ_CONTACTS);
    }

    @Test(expected = RuntimeException.class)
    public void testFailsToLoadIfNoPermission() {
        setAllowContactsRead(false);
        ContactsDictionary dictionary = new ContactsDictionary(RuntimeEnvironment.application);
        dictionary.loadDictionary();
    }

    @Test
    public void testRegisterObserver() throws Exception {
        ShadowContentResolver shadowContentResolver = Shadows.shadowOf(RuntimeEnvironment.application.getContentResolver());
        final Collection<ContentObserver> contentObservers = shadowContentResolver.getContentObservers(ContactsContract.Contacts.CONTENT_URI);
        Assert.assertEquals(1, contentObservers.size());

        //now, simulating contacts update
        MatrixCursor initialContacts = new MatrixCursor(new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.STARRED, ContactsContract.Contacts.TIMES_CONTACTED});
        Mockito.doReturn(initialContacts).when(mMockedContactsContentProvider).query(Mockito.any(Uri.class), Mockito.any(String[].class), Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString());
        initialContacts.addRow(new Object[]{1, "Hagar Even-Danan", 1, 10});

        contentObservers.iterator().next().onChange(false);

        Iterator<String> nextWords = mDictionaryUnderTest.getNextWords("Hagar", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Even-Danan", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());

        nextWords = mDictionaryUnderTest.getNextWords("Menny", 2, 1).iterator();
        Assert.assertFalse(nextWords.hasNext());
    }

    @Test
    public void testCloseUnregisterObserver() {
        mDictionaryUnderTest.close();
        ShadowContentResolver shadowContentResolver = Shadows.shadowOf(RuntimeEnvironment.application.getContentResolver());
        Assert.assertEquals(0, shadowContentResolver.getContentObservers(ContactsContract.Contacts.CONTENT_URI).size());
    }

    @Test
    public void testDeleteWordFromStorageDoesNotHaveEffect() throws Exception {
        mDictionaryUnderTest.deleteWordFromStorage("Menny");
        Assert.assertTrue(mDictionaryUnderTest.isValidWord("Menny"));

    }

    @Test
    public void testAddWordToStorageDoesNotHaveEffect() throws Exception {
        mDictionaryUnderTest.addWordToStorage("aword", 126);
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("aword"));
    }

    @Test
    public void testGetNextWords() throws Exception {
        Iterator<String> nextWords = mDictionaryUnderTest.getNextWords("Menny", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Even-Danan", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());

        nextWords = mDictionaryUnderTest.getNextWords("Dummy", 2, 1).iterator();
        Assert.assertFalse(nextWords.hasNext());

        nextWords = mDictionaryUnderTest.getNextWords("Erela", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Portugaly", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());

        nextWords = mDictionaryUnderTest.getNextWords("John", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Lennon", nextWords.next());
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Smith", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());

        nextWords = mDictionaryUnderTest.getNextWords("Mika", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Michael", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());
        //next part of the name
        nextWords = mDictionaryUnderTest.getNextWords("Michael", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Michelle", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());

        nextWords = mDictionaryUnderTest.getNextWords("Jonathan", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("With'In", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());
    }
}