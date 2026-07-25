package com.anysoftkeyboard.dictionaries.jni;

import static org.mockito.ArgumentMatchers.eq;

import android.content.Context;
import androidx.annotation.NonNull;
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

  private TestableResourceBinaryDictionary mDictionary;

  @Before
  public void setUp() {
    mDictionary =
        new TestableResourceBinaryDictionary(
            ApplicationProvider.getApplicationContext(),
            com.menny.android.anysoftkeyboard.R.array.english_words_dict_array);
  }

  @Test
  public void testGetLoadedWordsWhenUninitializedReturnsEmptyArrays() {
    GetWordsCallback callback = Mockito.mock(GetWordsCallback.class);

    // Native pointer has not been initialized (ptr == 0L)
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

    // Verify behavior while isLoading() == true during resource loading
    mDictionary.mTestGetLoadedWordsDuringLoadCallback = callback;
    mDictionary.loadDictionary();

    Assert.assertNotNull(mDictionary.mCallbackExecutedDuringLoad);
    Mockito.verify(callback).onGetWordsFinished(eq(new char[0][0]), eq(new int[0]));
  }

  @Test
  public void testGetLoadedWordsWhenNativeFailsReturnsEmptyArrays() {
    GetWordsCallback callback = Mockito.mock(GetWordsCallback.class);

    mDictionary.loadDictionary();
    Assert.assertFalse(mDictionary.isTestLoading());

    mDictionary.setNativePointerForTest(12345L);
    mDictionary.mSimulateGetWordsNativeSuccess = false;

    mDictionary.getLoadedWords(callback);

    Mockito.verify(callback).onGetWordsFinished(eq(new char[0][0]), eq(new int[0]));
  }

  @Test
  public void testGetLoadedWordsWhenNativeSucceedsDoesNotInvokeEmptyFallback() {
    GetWordsCallback callback = Mockito.mock(GetWordsCallback.class);

    mDictionary.loadDictionary();
    Assert.assertFalse(mDictionary.isTestLoading());

    mDictionary.setNativePointerForTest(12345L);
    mDictionary.mSimulateGetWordsNativeSuccess = true;

    mDictionary.getLoadedWords(callback);

    Mockito.verify(callback, Mockito.never())
        .onGetWordsFinished(eq(new char[0][0]), eq(new int[0]));
  }

  private static class TestableResourceBinaryDictionary extends ResourceBinaryDictionary {

    boolean mSimulateGetWordsNativeSuccess = true;
    GetWordsCallback mTestGetLoadedWordsDuringLoadCallback = null;
    GetWordsCallback mCallbackExecutedDuringLoad = null;

    TestableResourceBinaryDictionary(Context context, int resId) {
      super("test_dict", context, resId);
    }

    @Override
    protected void loadNativeLibrary(@NonNull Context originPackageContext) {
      // No-op in host JVM unit tests
    }

    @Override
    protected void loadAllResources() {
      if (mTestGetLoadedWordsDuringLoadCallback != null) {
        Assert.assertTrue(isLoading());
        getLoadedWords(mTestGetLoadedWordsDuringLoadCallback);
        mCallbackExecutedDuringLoad = mTestGetLoadedWordsDuringLoadCallback;
      }
      super.loadAllResources();
    }

    @Override
    protected boolean getWordsNative(long dictPointer, GetWordsCallback callback) {
      return mSimulateGetWordsNativeSuccess;
    }

    void setNativePointerForTest(long ptr) {
      mNativeDictPointer.set(ptr);
    }

    boolean isTestLoading() {
      return isLoading();
    }
  }
}
