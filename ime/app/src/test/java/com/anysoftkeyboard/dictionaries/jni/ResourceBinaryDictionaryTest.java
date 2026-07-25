package com.anysoftkeyboard.dictionaries.jni;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.dictionaries.GetWordsCallback;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ResourceBinaryDictionaryTest {

  private ResourceBinaryDictionary mDictionary;

  @Before
  public void setUp() {
    mDictionary =
        new ResourceBinaryDictionary(
            "test_dict",
            ApplicationProvider.getApplicationContext(),
            com.menny.android.anysoftkeyboard.R.array.english_words_dict_array);
  }

  @Test
  public void testGetLoadedWordsWhenUninitializedReturnsEmptyArrays() {
    GetWordsCallback callback = Mockito.mock(GetWordsCallback.class);

    // Dictionary has not called loadAllResources(), so mNativeDictPointer is 0L
    mDictionary.getLoadedWords(callback);

    Mockito.verify(callback).onGetWordsFinished(eq(new char[0][0]), eq(new int[0]));
  }

  @Test
  public void testGetLoadedWordsWhenClosedReturnsEmptyArrays() {
    GetWordsCallback callback = Mockito.mock(GetWordsCallback.class);

    mDictionary.close();
    Assert.assertTrue(mDictionary.isClosed());

    mDictionary.getLoadedWords(callback);

    Mockito.verify(callback).onGetWordsFinished(eq(new char[0][0]), eq(new int[0]));
  }

  @Test
  public void testGetLoadedWordsWhenLoadingReturnsEmptyArrays() {
    GetWordsCallback callback = Mockito.mock(GetWordsCallback.class);

    // Simulate loading state without native pointer set
    mDictionary.loadDictionary();
    mDictionary.close();

    mDictionary.getLoadedWords(callback);

    Mockito.verify(callback).onGetWordsFinished(any(char[][].class), any(int[].class));
  }
}
