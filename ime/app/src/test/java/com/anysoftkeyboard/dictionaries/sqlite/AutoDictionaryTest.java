package com.anysoftkeyboard.dictionaries.sqlite;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

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
    Uri uri =
        Uri.parse(
            "content://"
                + getApplicationContext().getPackageName()
                + ".dictionary/Auto/en");
    io.reactivex.disposables.Disposable disposable =
        app.cash.copper.rx2.RxContentResolver.observeQuery(
                getApplicationContext().getContentResolver(),
                uri,
                null,
                null,
                null,
                null,
                true,
                com.anysoftkeyboard.rx.RxSchedulers.mainThread())
            .skip(1)
            .forEach(query -> changeDetected.set(true));

    try {
      mAutoDictionary.addWord("unfamiliar", 1);
      TestRxSchedulers.drainAllTasks();
      Assert.assertFalse(changeDetected.get());
    } finally {
      disposable.dispose();
    }
  }

  @Test
  public void testLearningThreshold() {
    Assert.assertFalse(mAutoDictionary.isValidWord("unknown"));
    Assert.assertFalse(mAutoDictionary.isValidWord("word"));
  }
}
