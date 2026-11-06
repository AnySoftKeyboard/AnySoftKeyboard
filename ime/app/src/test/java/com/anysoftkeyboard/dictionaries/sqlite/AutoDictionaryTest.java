package com.anysoftkeyboard.dictionaries.sqlite;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.database.ContentObserver;
import android.net.Uri;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AutoDictionaryTest {

  private AutoDictionary mAutoDictionary;

  @Before
  public void setUp() {
    getApplicationContext().deleteDatabase("auto_dict_2.db");
    mAutoDictionary = new AutoDictionary(getApplicationContext(), "en");
    mAutoDictionary.loadDictionary();
    TestRxSchedulers.drainAllTasks();
  }

  @After
  public void tearDown() {
    mAutoDictionary.close();
  }

  @Test
  public void testDoesNotEmitStorageChangeNotifications() {
    final AtomicBoolean changeDetected = new AtomicBoolean(false);
    ContentObserver observer =
        new ContentObserver(null) {
          @Override
          public void onChange(boolean selfChange, Uri uri) {
            changeDetected.set(true);
          }
        };

    Uri uri =
        Uri.parse(
            "content://"
                + getApplicationContext().getPackageName()
                + ".dictionary/Auto/en");
    getApplicationContext().getContentResolver().registerContentObserver(uri, true, observer);

    try {
      mAutoDictionary.addWord("unfamiliar", 1);
      TestRxSchedulers.drainAllTasks();
      Assert.assertFalse(changeDetected.get());
    } finally {
      getApplicationContext().getContentResolver().unregisterContentObserver(observer);
    }
  }

  @Test
  public void testLearningThreshold() {
    Assert.assertFalse(mAutoDictionary.isValidWord("unknown"));
    Assert.assertFalse(mAutoDictionary.isValidWord("word"));
  }
}
