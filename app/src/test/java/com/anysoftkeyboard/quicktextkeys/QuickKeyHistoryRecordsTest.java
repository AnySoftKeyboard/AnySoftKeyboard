package com.anysoftkeyboard.quicktextkeys;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@SuppressLint("CommitPrefEdits")
public class QuickKeyHistoryRecordsTest {

    private SharedPreferences mSharedPreferences;

    @Before
    public void setUp() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
    }

    @Test
    public void testLoadHasDefaultValue() {
        List<QuickKeyHistoryRecords.HistoryKey> keys = QuickKeyHistoryRecords.load(mSharedPreferences);
        Assert.assertNotNull(keys);
        Assert.assertEquals(1, keys.size());
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, keys.get(0).name);
        Assert.assertEquals(QuickKeyHistoryRecords.DEFAULT_EMOJI, keys.get(0).value);
    }

    @Test
    public void testLoad() {
        mSharedPreferences.edit().putString(QuickKeyHistoryRecords.HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, "1,2,3,4,5,6").commit();
        List<QuickKeyHistoryRecords.HistoryKey> keys = QuickKeyHistoryRecords.load(mSharedPreferences);
        Assert.assertEquals(3, keys.size());
        Assert.assertEquals("1", keys.get(0).name);
        Assert.assertEquals("2", keys.get(0).value);
        Assert.assertEquals("3", keys.get(1).name);
        Assert.assertEquals("4", keys.get(1).value);
        Assert.assertEquals("5", keys.get(2).name);
        Assert.assertEquals("6", keys.get(2).value);
    }

    @Test
    public void testLoadMoreThanLimit() {
        StringBuilder exceedString = new StringBuilder();
        for (int i = 0; i < QuickKeyHistoryRecords.MAX_LIST_SIZE * 2; i++) {
            exceedString.append(Integer.toString(2 * i)).append(QuickKeyHistoryRecords.HISTORY_TOKEN_SEPARATOR).append(Integer.toString(2 * i + 1)).append(QuickKeyHistoryRecords.HISTORY_TOKEN_SEPARATOR);
        }
        mSharedPreferences.edit().putString(QuickKeyHistoryRecords.HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, exceedString.toString()).commit();
        List<QuickKeyHistoryRecords.HistoryKey> keys = QuickKeyHistoryRecords.load(mSharedPreferences);
        Assert.assertEquals(QuickKeyHistoryRecords.MAX_LIST_SIZE, keys.size());
        Assert.assertEquals("0", keys.get(0).name);
        Assert.assertEquals("1", keys.get(0).value);
        Assert.assertEquals(Integer.toString(QuickKeyHistoryRecords.MAX_LIST_SIZE * 2 - 2), keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).name);
        Assert.assertEquals(Integer.toString(QuickKeyHistoryRecords.MAX_LIST_SIZE * 2 - 1), keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).value);
    }

    @Test
    public void testStore() {
        List<QuickKeyHistoryRecords.HistoryKey> keys = new ArrayList<>();
        keys.add(new QuickKeyHistoryRecords.HistoryKey("1", "2"));
        keys.add(new QuickKeyHistoryRecords.HistoryKey("3", "4"));

        QuickKeyHistoryRecords.store(mSharedPreferences, keys, new QuickKeyHistoryRecords.HistoryKey("5", "6"));

        Assert.assertEquals(3, keys.size());

        Assert.assertEquals("1", keys.get(0).name);
        Assert.assertEquals("2", keys.get(0).value);
        Assert.assertEquals("3", keys.get(1).name);
        Assert.assertEquals("4", keys.get(1).value);
        Assert.assertEquals("5", keys.get(2).name);
        Assert.assertEquals("6", keys.get(2).value);

    }

    @Test
    public void testDoesNotLoadIfEmptyStrings() {
        mSharedPreferences.edit().putString(QuickKeyHistoryRecords.HISTORY_QUICK_TEXT_KEY_ENCODED_HISTORY_KEY, "1,2,,4,5,").commit();
        List<QuickKeyHistoryRecords.HistoryKey> keys = QuickKeyHistoryRecords.load(mSharedPreferences);
        Assert.assertEquals(1, keys.size());
        Assert.assertEquals("1", keys.get(0).name);
        Assert.assertEquals("2", keys.get(0).value);
    }

    @Test
    public void testStoreDuplicateKey() {
        List<QuickKeyHistoryRecords.HistoryKey> keys = new ArrayList<>();
        keys.add(new QuickKeyHistoryRecords.HistoryKey("1", "2"));
        keys.add(new QuickKeyHistoryRecords.HistoryKey("3", "4"));

        QuickKeyHistoryRecords.store(mSharedPreferences, keys, new QuickKeyHistoryRecords.HistoryKey("3", "6"));

        Assert.assertEquals(2, keys.size());

        Assert.assertEquals("1", keys.get(0).name);
        Assert.assertEquals("2", keys.get(0).value);
        Assert.assertEquals("3", keys.get(1).name);
        Assert.assertEquals("6", keys.get(1).value);
    }

    @Test
    public void testStoreMoreThanLimit() {
        List<QuickKeyHistoryRecords.HistoryKey> keys = new ArrayList<>();
        for (int i = 0; i < QuickKeyHistoryRecords.MAX_LIST_SIZE * 4; i += 2) {
            keys.add(new QuickKeyHistoryRecords.HistoryKey("k" + Integer.toString(i), "v" + Integer.toString(i + 1)));
        }

        QuickKeyHistoryRecords.store(mSharedPreferences, keys, new QuickKeyHistoryRecords.HistoryKey("last", "last_last"));

        Assert.assertEquals(QuickKeyHistoryRecords.MAX_LIST_SIZE, keys.size());

        Assert.assertEquals("last", keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).name);
        Assert.assertEquals("last_last", keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).value);
        Assert.assertEquals("k118", keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 2).name);
        Assert.assertEquals("v119", keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 2).value);
        Assert.assertEquals("k62", keys.get(0).name);
        Assert.assertEquals("v63", keys.get(0).value);

        QuickKeyHistoryRecords.store(mSharedPreferences, keys, new QuickKeyHistoryRecords.HistoryKey("last_again", "last_again_last"));

        Assert.assertEquals(QuickKeyHistoryRecords.MAX_LIST_SIZE, keys.size());

        Assert.assertEquals("last_again", keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).name);
        Assert.assertEquals("last_again_last", keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 1).value);
        Assert.assertEquals("last", keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 2).name);
        Assert.assertEquals("last_last", keys.get(QuickKeyHistoryRecords.MAX_LIST_SIZE - 2).value);
        Assert.assertEquals("k64", keys.get(0).name);
        Assert.assertEquals("v65", keys.get(0).value);
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