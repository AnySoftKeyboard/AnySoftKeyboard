package com.anysoftkeyboard.dictionaries.content;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ContentProviderController;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class DictionaryContentProviderTest {

  private DictionaryContentProvider mProvider;

  @Before
  public void setup() {
    ContentProviderController<DictionaryContentProvider> controller =
        Robolectric.buildContentProvider(DictionaryContentProvider.class);
    mProvider = controller.create().get();
  }

  @Test
  public void testLifecycleAndStubMethods() {
    Assert.assertNotNull(mProvider);
    Assert.assertTrue(mProvider.onCreate());

    Context context = getApplicationContext();
    Uri uri =
        Uri.parse(
            "content://" + context.getPackageName() + ".dictionary/IgnoredWordsDictionary/en");

    Assert.assertNull(mProvider.query(uri, null, null, null, null));
    Assert.assertNull(mProvider.getType(uri));
    Assert.assertNull(mProvider.insert(uri, new ContentValues()));
    Assert.assertEquals(0, mProvider.delete(uri, null, null));
    Assert.assertEquals(0, mProvider.update(uri, new ContentValues(), null, null));
  }
}
