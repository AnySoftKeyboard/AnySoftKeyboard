package com.anysoftkeyboard.dictionaries.content;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.Manifest;
import android.app.Application;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.provider.ContactsContract;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import de.triplet.simpleprovider.AbstractProvider;
import de.triplet.simpleprovider.Column;
import de.triplet.simpleprovider.Table;
import java.util.Collection;
import java.util.Iterator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.shadows.ShadowContentResolver;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ContactsDictionaryTest {
    private ContactsDictionary mDictionaryUnderTest;
    private ContactsContentProvider mProvider;

    @Before
    public void setup() {
        setAllowContactsRead(true);
        // setting up some dummy contacts
        mProvider = new ContactsContentProvider();
        ContentProviderController.of(mProvider).create(mProvider.getAuthority());
        mProvider.addRow(1, "Menny Even-Danan", true, 10);
        mProvider.addRow(2, "Jonathan With'In", false, 100);
        mProvider.addRow(3, "Erela Portugaly", true, 10);
        mProvider.addRow(4, "John Smith", false, 1);
        mProvider.addRow(5, "John Lennon", true, 126);
        mProvider.addRow(6, "Mika Michael Michelle", true, 10);
        mProvider.addRow(7, "Invisible Man", true, 99, false);

        mDictionaryUnderTest = new ContactsDictionary(getApplicationContext());
        mDictionaryUnderTest.loadDictionary();
    }

    private void setAllowContactsRead(boolean enabled) {
        if (enabled)
            Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                    .grantPermissions(Manifest.permission.READ_CONTACTS);
        else
            Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
                    .denyPermissions(Manifest.permission.READ_CONTACTS);
    }

    @Test(expected = RuntimeException.class)
    public void testFailsToLoadIfNoPermission() {
        setAllowContactsRead(false);
        ContactsDictionary dictionary = new ContactsDictionary(getApplicationContext());
        dictionary.loadDictionary();
    }

    @Test
    public void testRegisterObserver() throws Exception {
        ShadowContentResolver shadowContentResolver =
                Shadows.shadowOf(getApplicationContext().getContentResolver());
        final Collection<ContentObserver> contentObservers =
                shadowContentResolver.getContentObservers(ContactsContract.Contacts.CONTENT_URI);
        Assert.assertEquals(1, contentObservers.size());

        // now, simulating contacts update
        mProvider.addRow(10, "Hagar Even-Danan", true, 10);

        Iterator<String> nextWords = mDictionaryUnderTest.getNextWords("Hagar", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Even-Danan", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());
    }

    @Test
    public void testCloseUnregisterObserver() {
        mDictionaryUnderTest.close();
        ShadowContentResolver shadowContentResolver =
                Shadows.shadowOf(getApplicationContext().getContentResolver());
        Assert.assertEquals(
                0,
                shadowContentResolver
                        .getContentObservers(ContactsContract.Contacts.CONTENT_URI)
                        .size());
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
    public void testIsValid() {
        Assert.assertTrue(mDictionaryUnderTest.isValidWord("Menny"));
        Assert.assertFalse(mDictionaryUnderTest.isValidWord("Invisible"));
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
        // next part of the name
        nextWords = mDictionaryUnderTest.getNextWords("Michael", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("Michelle", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());

        nextWords = mDictionaryUnderTest.getNextWords("Jonathan", 2, 1).iterator();
        Assert.assertTrue(nextWords.hasNext());
        Assert.assertEquals("With'In", nextWords.next());
        Assert.assertFalse(nextWords.hasNext());
    }

    public static class ContactsContentProvider extends AbstractProvider {

        @Override
        protected String getAuthority() {
            return ContactsContract.Contacts.CONTENT_URI.getAuthority();
        }

        @Table
        public static class Contacts {
            @Column(value = Column.FieldType.INTEGER, primaryKey = true)
            public static final String _ID = ContactsContract.Contacts._ID;

            @Column(Column.FieldType.TEXT)
            public static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;

            @Column(Column.FieldType.INTEGER)
            public static final String STARRED = ContactsContract.Contacts.STARRED;

            @Column(Column.FieldType.INTEGER)
            public static final String TIMES_CONTACTED = ContactsContract.Contacts.TIMES_CONTACTED;

            @Column(Column.FieldType.INTEGER)
            public static final String IN_VISIBLE_GROUP =
                    ContactsContract.Contacts.IN_VISIBLE_GROUP;
        }

        public void addRow(int id, String name, boolean starred, int timesContacted) {
            addRow(id, name, starred, timesContacted, true);
        }

        public void addRow(
                int id, String name, boolean starred, int timesContacted, boolean visible) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contacts._ID, id);
            contentValues.put(Contacts.DISPLAY_NAME, name);
            contentValues.put(Contacts.STARRED, starred ? 1 : 0);
            contentValues.put(Contacts.TIMES_CONTACTED, timesContacted);
            contentValues.put(Contacts.IN_VISIBLE_GROUP, visible ? 1 : 0);
            insert(ContactsContract.Contacts.CONTENT_URI, contentValues);
        }
    }
}
