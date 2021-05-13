package com.anysoftkeyboard.dictionaries.content;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.provider.UserDictionary;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import de.triplet.simpleprovider.AbstractProvider;
import de.triplet.simpleprovider.Column;
import de.triplet.simpleprovider.Table;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.shadows.ShadowContentResolver;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AndroidUserDictionaryTest {

    private AUDContentProvider mProvider;

    @Before
    public void setup() {
        mProvider = new AUDContentProvider();
        ContentProviderController.of(mProvider).create(mProvider.getAuthority());
        // setting up some dummy words
        mProvider.addRow(1, "Dude", 1, "en");
        mProvider.addRow(2, "Dudess", 2, "en");
        mProvider.addRow(3, "shalom", 10, "iw");
        mProvider.addRow(4, "telephone", 2, "iw");
        mProvider.addRow(5, "catchall", 5, null);
    }

    @Test
    public void testLoadedWordsEN() throws Exception {
        AndroidUserDictionary dictionary = new AndroidUserDictionary(getApplicationContext(), "en");
        dictionary.loadDictionary();
        TestRxSchedulers.drainAllTasks();
        Assert.assertFalse(dictionary.isValidWord("Dudes"));
        Assert.assertTrue(dictionary.isValidWord("Dude"));
        Assert.assertFalse(dictionary.isValidWord("catchall"));
        Assert.assertFalse(dictionary.isValidWord("shalom"));
    }

    @Test
    public void testLoadedWordsNULL() throws Exception {
        AndroidUserDictionary dictionary = new AndroidUserDictionary(getApplicationContext(), null);
        dictionary.loadDictionary();
        TestRxSchedulers.drainAllTasks();
        Assert.assertTrue(dictionary.isValidWord("Dude"));
        Assert.assertFalse(dictionary.isValidWord("Dudes"));
        Assert.assertTrue(dictionary.isValidWord("catchall"));
        Assert.assertTrue(dictionary.isValidWord("shalom"));
    }

    @Test(expected = RuntimeException.class)
    public void testLoadedWordsWhenNoContentProvider() throws Exception {
        ShadowContentResolver.reset();
        AndroidUserDictionary dictionary = new AndroidUserDictionary(getApplicationContext(), "en");
        // this should throw an exception, since there is no system content provider
        dictionary.loadDictionary();
        TestRxSchedulers.drainAllTasks();
    }

    @Test
    public void testRegisterObserver() throws Exception {
        AndroidUserDictionary dictionary = new AndroidUserDictionary(getApplicationContext(), "en");
        dictionary.loadDictionary();
        TestRxSchedulers.drainAllTasks();

        Collection<ContentObserver> observerList =
                Shadows.shadowOf(getApplicationContext().getContentResolver())
                        .getContentObservers(UserDictionary.Words.CONTENT_URI);
        Assert.assertEquals(1, observerList.size());

        Assert.assertFalse(dictionary.isValidWord("Dudesss"));
        mProvider.addRow(15, "Dudesss", 1, "en");
        TestRxSchedulers.drainAllTasks();

        Assert.assertTrue(dictionary.isValidWord("Dudesss"));

        dictionary.close();
        Assert.assertTrue(
                Shadows.shadowOf(getApplicationContext().getContentResolver())
                        .getContentObservers(UserDictionary.Words.CONTENT_URI)
                        .isEmpty());
    }

    public static class AUDContentProvider extends AbstractProvider {

        @Override
        public String getAuthority() {
            return UserDictionary.Words.CONTENT_URI.getAuthority();
        }

        @Table
        public static class Words {
            @Column(value = Column.FieldType.INTEGER, primaryKey = true)
            public static final String KEY_ID = UserDictionary.Words._ID;

            @Column(Column.FieldType.TEXT)
            public static final String KEY_WORD = UserDictionary.Words.WORD;

            @Column(Column.FieldType.INTEGER)
            public static final String KEY_FREQ = UserDictionary.Words.FREQUENCY;

            @Column(Column.FieldType.TEXT)
            public static final String KEY_LOCALE = UserDictionary.Words.LOCALE;
        }

        public void addRow(int id, String word, int freq, String locale) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Words.KEY_ID, id);
            contentValues.put(Words.KEY_WORD, word);
            contentValues.put(Words.KEY_FREQ, freq);
            if (locale == null) {
                contentValues.putNull(Words.KEY_LOCALE);
            } else {
                contentValues.put(Words.KEY_LOCALE, locale);
            }
            insert(UserDictionary.Words.CONTENT_URI, contentValues);
        }
    }
}
