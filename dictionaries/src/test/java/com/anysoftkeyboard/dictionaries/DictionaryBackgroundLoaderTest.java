package com.anysoftkeyboard.dictionaries;

import static org.mockito.ArgumentMatchers.same;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

import io.reactivex.disposables.Disposable;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class DictionaryBackgroundLoaderTest {

    @Test
    public void testHappyPath() {
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        DictionaryBackgroundLoader.Listener listener = Mockito.mock(DictionaryBackgroundLoader.Listener.class);

        final Disposable disposable = DictionaryBackgroundLoader.loadDictionaryInBackground(listener, dictionary);

        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushBackgroundThreadScheduler();

        final InOrder inOrder = Mockito.inOrder(listener, dictionary);
        inOrder.verify(dictionary).loadDictionary();
        inOrder.verify(listener).onDictionaryLoadingDone(same(dictionary));
        inOrder.verifyNoMoreInteractions();

        disposable.dispose();
        Mockito.verify(dictionary).close();
    }

    @Test
    public void testFailedToLoad() {
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        final RuntimeException runtimeException = new RuntimeException();
        Mockito.doThrow(runtimeException).when(dictionary).loadDictionary();
        DictionaryBackgroundLoader.Listener listener = Mockito.mock(DictionaryBackgroundLoader.Listener.class);

        final Disposable disposable = DictionaryBackgroundLoader.loadDictionaryInBackground(listener, dictionary);

        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushBackgroundThreadScheduler();

        final InOrder inOrder = Mockito.inOrder(listener, dictionary);
        inOrder.verify(dictionary).loadDictionary();
        inOrder.verify(listener).onDictionaryLoadingFailed(same(dictionary), same(runtimeException));
        inOrder.verify(dictionary).close();
        inOrder.verifyNoMoreInteractions();

        disposable.dispose();

        Mockito.verify(dictionary).close();
    }

    @Test
    public void testReloadHappyPath() {
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        final Disposable disposable = DictionaryBackgroundLoader.reloadDictionaryInBackground(dictionary);

        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushBackgroundThreadScheduler();

        final InOrder inOrder = Mockito.inOrder(dictionary);
        inOrder.verify(dictionary).loadDictionary();
        inOrder.verify(dictionary, Mockito.never()).close();
        inOrder.verifyNoMoreInteractions();

        disposable.dispose();

        Mockito.verify(dictionary, Mockito.never()).close();
    }

    @Test
    public void testReloadFailedToLoad() {
        Dictionary dictionary = Mockito.mock(Dictionary.class);
        final RuntimeException runtimeException = new RuntimeException();

        Mockito.doThrow(runtimeException).when(dictionary).loadDictionary();
        final Disposable disposable = DictionaryBackgroundLoader.reloadDictionaryInBackground(dictionary);

        Robolectric.flushForegroundThreadScheduler();
        Robolectric.flushBackgroundThreadScheduler();

        final InOrder inOrder = Mockito.inOrder(dictionary);
        inOrder.verify(dictionary).loadDictionary();
        inOrder.verify(dictionary, Mockito.never()).close();
        inOrder.verifyNoMoreInteractions();

        disposable.dispose();

        Mockito.verify(dictionary, Mockito.never()).close();
    }

}