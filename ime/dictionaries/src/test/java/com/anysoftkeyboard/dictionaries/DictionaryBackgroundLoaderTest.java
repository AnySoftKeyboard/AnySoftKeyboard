package com.anysoftkeyboard.dictionaries;

import static org.mockito.ArgumentMatchers.same;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import io.reactivex.disposables.Disposable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class DictionaryBackgroundLoaderTest {

  @Test
  public void testHappyPath() {
    Dictionary dictionary = Mockito.mock(Dictionary.class);
    DictionaryBackgroundLoader.Listener listener =
        Mockito.mock(DictionaryBackgroundLoader.Listener.class);

    final Disposable disposable =
        DictionaryBackgroundLoader.loadDictionaryInBackground(listener, dictionary);
    TestRxSchedulers.drainAllTasks();

    final InOrder inOrder = Mockito.inOrder(listener, dictionary);
    inOrder.verify(dictionary).loadDictionary();
    inOrder.verify(listener).onDictionaryLoadingDone(same(dictionary));
    inOrder.verifyNoMoreInteractions();

    disposable.dispose();
    TestRxSchedulers.drainAllTasks();
    Mockito.verify(dictionary).close();
  }

  @Test
  public void testFailedToLoad() {
    Dictionary dictionary = Mockito.mock(Dictionary.class);
    final RuntimeException runtimeException = new RuntimeException();
    Mockito.doThrow(runtimeException).when(dictionary).loadDictionary();
    DictionaryBackgroundLoader.Listener listener =
        Mockito.mock(DictionaryBackgroundLoader.Listener.class);

    final Disposable disposable =
        DictionaryBackgroundLoader.loadDictionaryInBackground(listener, dictionary);

    TestRxSchedulers.drainAllTasks();

    final InOrder inOrder = Mockito.inOrder(listener, dictionary);
    inOrder.verify(dictionary).loadDictionary();
    inOrder.verify(dictionary).close();
    inOrder.verify(listener).onDictionaryLoadingFailed(same(dictionary), same(runtimeException));
    inOrder.verifyNoMoreInteractions();

    disposable.dispose();
    TestRxSchedulers.drainAllTasks();
    Mockito.verify(dictionary).close();
  }

  @Test
  public void testReloadHappyPath() {
    Dictionary dictionary = Mockito.mock(Dictionary.class);
    final Disposable disposable =
        DictionaryBackgroundLoader.reloadDictionaryInBackground(dictionary);

    TestRxSchedulers.drainAllTasks();

    final InOrder inOrder = Mockito.inOrder(dictionary);
    inOrder.verify(dictionary).loadDictionary();
    inOrder.verify(dictionary, Mockito.never()).close();
    inOrder.verifyNoMoreInteractions();

    disposable.dispose();
    TestRxSchedulers.drainAllTasks();
    Mockito.verify(dictionary, Mockito.never()).close();
  }

  @Test
  public void testReloadFailedToLoad() {
    Dictionary dictionary = Mockito.mock(Dictionary.class);
    final RuntimeException runtimeException = new RuntimeException();

    Mockito.doThrow(runtimeException).when(dictionary).loadDictionary();
    final Disposable disposable =
        DictionaryBackgroundLoader.reloadDictionaryInBackground(dictionary);

    TestRxSchedulers.drainAllTasks();

    final InOrder inOrder = Mockito.inOrder(dictionary);
    inOrder.verify(dictionary).loadDictionary();
    inOrder.verify(dictionary, Mockito.never()).close();
    inOrder.verifyNoMoreInteractions();

    disposable.dispose();
    TestRxSchedulers.drainAllTasks();
    Mockito.verify(dictionary, Mockito.never()).close();
  }
}
