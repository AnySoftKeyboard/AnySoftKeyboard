package com.anysoftkeyboard.quicktextkeys;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
@SuppressLint("CommitPrefEdits")
public class QuickKeyHistoryRecordsTest {

    private SharedPreferences mSharedPreferences;
    private QuickKeyHistoryRecords mUnderTest;

    @Before
    public void setUp() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
    }

    @Test
    public void testLoadHasDefaultValue() {
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        List<QuickKeyHistoryRecords.HistoryKey> keys = mUnderTest.getCurrentHistory();
        Assert.assertNotNull(keys);
        Assert.assertEquals(1, keys.size());
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, keys.get(0).name);
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, keys.get(0).value);
    }

    @Test
    public void testLoad() {
        mSharedPreferences.edit().putString(QuickKeyHistoryRecords.HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, "1,2,3,4,5,6").commit();
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        List<QuickKeyHistoryRecords.HistoryKey> keys = mUnderTest.getCurrentHistory();
        Assert.assertEquals(3, keys.size());
        Assert.assertEquals("1", keys.get(0).name);
        Assert.assertEquals("2", keys.get(0).value);
        Assert.assertEquals("3", keys.get(1).name);
        Assert.assertEquals("4", keys.get(1).value);
        Assert.assertEquals("5", keys.get(2).name);
        Assert.assertEquals("6", keys.get(2).value);
    }

    @Test
    public void testStoreAndLoadFromPrefs() {
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        mUnderTest.store("k", "v");
        List<QuickKeyHistoryRecords.HistoryKey> keys = mUnderTest.getCurrentHistory();
        Assert.assertEquals(2, keys.size());
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, keys.get(0).name);
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, keys.get(0).value);
        Assert.assertEquals("k", keys.get(1).name);
        Assert.assertEquals("v", keys.get(1).value);

        final QuickKeyHistoryRecords newRecord = new QuickKeyHistoryRecords(mSharedPreferences);
        final List<QuickKeyHistoryRecords.HistoryKey> newHistory = newRecord.getCurrentHistory();

        Assert.assertNotSame(keys, newHistory);
        Assert.assertEquals(keys.size(), newHistory.size());
        for (int historyIndex=0; historyIndex<keys.size(); historyIndex++) {
            final QuickKeyHistoryRecords.HistoryKey k1 = keys.get(historyIndex);
            final QuickKeyHistoryRecords.HistoryKey k2 = newHistory.get(historyIndex);
            Assert.assertEquals(k1.name, k2.name);
            Assert.assertEquals(k1.value, k2.value);
        }
    }

    @Test
    public void testLoadMoreThanLimit() {
        StringBuilder exceedString = new StringBuilder();
        for (int i = 0; i < QuickKeyHistoryRecords.MAX_LIST_SIZE * 2; i++) {
            exceedString.append(Integer.toString(2 * i)).append(QuickKeyHistoryRecords.HISTORY_TOKEN_SEPARATOR).append(Integer.toString(2 * i + 1)).append(QuickKeyHistoryRecords.HISTORY_TOKEN_SEPARATOR);
        }
        mSharedPreferences.edit().putString(QuickKeyHistoryRecords.HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, exceedString.toString()).commit();
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        List<QuickKeyHistoryRecords.HistoryKey> keys = mUnderTest.getCurrentHistory();
        Assert.assertEquals(QuickKeyHistoryRecords.MAX_LIST_SIZE, keys.size());
        Assert.assertEquals("0", keys.get(0).name);
        Assert.assertEquals("1", keys.get(0).value);
        Assert.assertEquals(Integer.toString(QuickKeyHistoryRecords.MAX_LIST_SIZE * 2 - 2), keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).name);
        Assert.assertEquals(Integer.toString(QuickKeyHistoryRecords.MAX_LIST_SIZE * 2 - 1), keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).value);
    }

    @Test
    public void testStore() {
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        mUnderTest.store("1", "2");
        mUnderTest.store("3", "4");
        mUnderTest.store("5", "6");

        final List<QuickKeyHistoryRecords.HistoryKey> currentHistory = mUnderTest.getCurrentHistory();
        Assert.assertEquals(3 + 1/*first default emoji*/, currentHistory.size());


        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, currentHistory.get(0).name);
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, currentHistory.get(0).value);
        Assert.assertEquals("1", currentHistory.get(1).name);
        Assert.assertEquals("2", currentHistory.get(1).value);
        Assert.assertEquals("3", currentHistory.get(2).name);
        Assert.assertEquals("4", currentHistory.get(2).value);
        Assert.assertEquals("5", currentHistory.get(3).name);
        Assert.assertEquals("6", currentHistory.get(3).value);
    }

    @Test
    public void testDoesNotLoadIfEmptyStrings() {
        mSharedPreferences.edit().putString(QuickKeyHistoryRecords.HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, "1,2,,4,5,").commit();
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        List<QuickKeyHistoryRecords.HistoryKey> keys = mUnderTest.getCurrentHistory();
        Assert.assertEquals(1, keys.size());
        Assert.assertEquals("1", keys.get(0).name);
        Assert.assertEquals("2", keys.get(0).value);
    }

    @Test
    public void testStoreDuplicateKey() {
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        mUnderTest.store("1", "2");
        mUnderTest.store("3", "4");
        mUnderTest.store("3", "6");

        List<QuickKeyHistoryRecords.HistoryKey> keys = mUnderTest.getCurrentHistory();

        Assert.assertEquals(2 + 1/*first default emoji*/, keys.size());

        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, keys.get(0).name);
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, keys.get(0).value);
        Assert.assertEquals("1", keys.get(1).name);
        Assert.assertEquals("2", keys.get(1).value);
        Assert.assertEquals("3", keys.get(2).name);
        Assert.assertEquals("6", keys.get(2).value);
    }

    @Test
    public void testStoreMoreThanLimit() {
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        for (int i = 0; i < QuickKeyHistoryRecords.MAX_LIST_SIZE * 4; i += 2) {
            mUnderTest.store("k" + Integer.toString(i), "v" + Integer.toString(i + 1));
        }

        mUnderTest.store("last", "last_last");

        List<QuickKeyHistoryRecords.HistoryKey> currentHistory = mUnderTest.getCurrentHistory();
        Assert.assertEquals(QuickKeyHistoryRecords.MAX_LIST_SIZE, currentHistory.size());

        Assert.assertEquals("last", currentHistory.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).name);
        Assert.assertEquals("last_last", currentHistory.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).value);
        Assert.assertEquals("k118", currentHistory.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 2).name);
        Assert.assertEquals("v119", currentHistory.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 2).value);
        Assert.assertEquals("k62", currentHistory.get(0).name);
        Assert.assertEquals("v63", currentHistory.get(0).value);

        mUnderTest.store("last_again", "last_again_last");

        currentHistory = mUnderTest.getCurrentHistory();
        Assert.assertEquals(QuickKeyHistoryRecords.MAX_LIST_SIZE, currentHistory.size());

        Assert.assertEquals(QuickKeyHistoryRecords.MAX_LIST_SIZE, currentHistory.size());

        Assert.assertEquals("last_again", currentHistory.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).name);
        Assert.assertEquals("last_again_last", currentHistory.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).value);
        Assert.assertEquals("last", currentHistory.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 2).name);
        Assert.assertEquals("last_last", currentHistory.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 2).value);
        Assert.assertEquals("k64", currentHistory.get(0).name);
        Assert.assertEquals("v65", currentHistory.get(0).value);
    }

    @Test
    public void testDoesNotStoreInIncognitoMode() {
        mUnderTest = new QuickKeyHistoryRecords(mSharedPreferences);
        final int initialItemsCount = 4;
        for (int i = 0; i < initialItemsCount*2; i += 2) {
            mUnderTest.store("k" + Integer.toString(i), "v" + Integer.toString(i + 1));
        }

        mUnderTest.setIncognitoMode(true);
        List<QuickKeyHistoryRecords.HistoryKey> currentHistory = mUnderTest.getCurrentHistory();
        Assert.assertEquals(initialItemsCount + 1/*initial emoji*/, currentHistory.size());

        for (int i = 10; i < 20; i += 2) {
            mUnderTest.store("k" + Integer.toString(i), "v" + Integer.toString(i + 1));
        }

        currentHistory = mUnderTest.getCurrentHistory();
        Assert.assertEquals(initialItemsCount + 1/*initial emoji*/, currentHistory.size());
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, currentHistory.get(0).name);
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, currentHistory.get(0).value);
        Assert.assertEquals("k0", currentHistory.get(1).name);
        Assert.assertEquals("v1", currentHistory.get(1).value);
        Assert.assertEquals("k2", currentHistory.get(2).name);
        Assert.assertEquals("v3", currentHistory.get(2).value);
        Assert.assertEquals("k4", currentHistory.get(3).name);
        Assert.assertEquals("v5", currentHistory.get(3).value);
        Assert.assertEquals("k6", currentHistory.get(4).name);
        Assert.assertEquals("v7", currentHistory.get(4).value);

        //turning incognito mode off
        mUnderTest.setIncognitoMode(false);
        mUnderTest.store("last_again", "last_again_last");

        currentHistory = mUnderTest.getCurrentHistory();
        Assert.assertEquals(initialItemsCount + 1 + 1/*the new record*/, currentHistory.size());

        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, currentHistory.get(0).name);
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, currentHistory.get(0).value);
        Assert.assertEquals("k0", currentHistory.get(1).name);
        Assert.assertEquals("v1", currentHistory.get(1).value);
        Assert.assertEquals("k2", currentHistory.get(2).name);
        Assert.assertEquals("v3", currentHistory.get(2).value);
        Assert.assertEquals("k4", currentHistory.get(3).name);
        Assert.assertEquals("v5", currentHistory.get(3).value);
        Assert.assertEquals("k6", currentHistory.get(4).name);
        Assert.assertEquals("v7", currentHistory.get(4).value);
        Assert.assertEquals("last_again", currentHistory.get(5).name);
        Assert.assertEquals("last_again_last", currentHistory.get(5).value);
    }

    @Test
    public void testHistoryKeyEqualsOnlyName() {
        QuickKeyHistoryRecords.HistoryKey key1 = new QuickKeyHistoryRecords.HistoryKey("1", "2");
        QuickKeyHistoryRecords.HistoryKey key2 = new QuickKeyHistoryRecords.HistoryKey("1", "3");
        QuickKeyHistoryRecords.HistoryKey key3 = new QuickKeyHistoryRecords.HistoryKey("2", "2");
        QuickKeyHistoryRecords.HistoryKey key4 = new QuickKeyHistoryRecords.HistoryKey("2", "2");

        Assert.assertEquals(key1, key1);
        Assert.assertEquals(key1, key2);
        Assert.assertNotEquals(key1, key3);
        Assert.assertEquals(key3, key4);
    }
}