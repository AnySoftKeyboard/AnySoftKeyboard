package com.anysoftkeyboard.ime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.GetWordsCallback;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class WordListDictionaryListenerTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testWaitsTillAllDictionariesLoadedBeforeGetWords() throws Exception {
    AnySoftKeyboardWithGestureTyping.WordListDictionaryListener.Callback consumer =
        Mockito.mock(AnySoftKeyboardWithGestureTyping.WordListDictionaryListener.Callback.class);
    AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);

    AnySoftKeyboardWithGestureTyping.WordListDictionaryListener underTest =
        new AnySoftKeyboardWithGestureTyping.WordListDictionaryListener(keyboard, consumer);
    final Dictionary dictionary1 = Mockito.mock(Dictionary.class);
    Mockito.doAnswer(
            invocation -> {
              ((GetWordsCallback) invocation.getArgument(0))
                  .onGetWordsFinished(new char[1][1], new int[1]);
              return null;
            })
        .when(dictionary1)
        .getLoadedWords(any());
    final Dictionary dictionary2 = Mockito.mock(Dictionary.class);
    Mockito.doAnswer(
            invocation -> {
              ((GetWordsCallback) invocation.getArgument(0))
                  .onGetWordsFinished(new char[2][2], new int[2]);
              return null;
            })
        .when(dictionary2)
        .getLoadedWords(any());
    underTest.onDictionaryLoadingStarted(dictionary1);
    underTest.onDictionaryLoadingStarted(dictionary2);
    underTest.onDictionaryLoadingDone(dictionary1);

    Mockito.verify(dictionary1).getLoadedWords(any());
    Mockito.verify(dictionary2, Mockito.never()).getLoadedWords(any());
    Mockito.verify(consumer, Mockito.never()).consumeWords(any(), anyList(), any());

    underTest.onDictionaryLoadingDone(dictionary2);

    Mockito.verify(dictionary2).getLoadedWords(any());
    ArgumentCaptor<List<char[][]>> wordsListCaptor = ArgumentCaptor.forClass(List.class);
    Mockito.verify(consumer).consumeWords(Mockito.same(keyboard), wordsListCaptor.capture(), any());

    Assert.assertEquals(2, wordsListCaptor.getValue().size());
    Assert.assertEquals(1, wordsListCaptor.getValue().get(0).length);
    Assert.assertEquals(2, wordsListCaptor.getValue().get(1).length);
  }

  @Test
  public void testFailsWhenWordsAndFrequenciesDoNotHaveTheSameLength() {
    AnySoftKeyboardWithGestureTyping.WordListDictionaryListener.Callback consumer =
        Mockito.mock(AnySoftKeyboardWithGestureTyping.WordListDictionaryListener.Callback.class);
    AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);

    AnySoftKeyboardWithGestureTyping.WordListDictionaryListener underTest =
        new AnySoftKeyboardWithGestureTyping.WordListDictionaryListener(keyboard, consumer);
    final Dictionary dictionary1 = Mockito.mock(Dictionary.class);
    Mockito.doAnswer(
            invocation -> {
              ((GetWordsCallback) invocation.getArgument(0))
                  .onGetWordsFinished(new char[1][1], new int[2]);
              return null;
            })
        .when(dictionary1)
        .getLoadedWords(any());
    underTest.onDictionaryLoadingStarted(dictionary1);
    underTest.onDictionaryLoadingDone(dictionary1);

    Mockito.verify(dictionary1).getLoadedWords(any());

    ArgumentCaptor<List<char[][]>> wordsListCaptor = ArgumentCaptor.forClass(List.class);
    Mockito.verify(consumer).consumeWords(Mockito.same(keyboard), wordsListCaptor.capture(), any());

    Assert.assertEquals(0, wordsListCaptor.getValue().size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testReportsZeroWordsOnException() throws Exception {
    AnySoftKeyboardWithGestureTyping.WordListDictionaryListener.Callback consumer =
        Mockito.mock(AnySoftKeyboardWithGestureTyping.WordListDictionaryListener.Callback.class);
    AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);

    AnySoftKeyboardWithGestureTyping.WordListDictionaryListener underTest =
        new AnySoftKeyboardWithGestureTyping.WordListDictionaryListener(keyboard, consumer);
    final Dictionary dictionary1 = Mockito.mock(Dictionary.class);
    Mockito.doThrow(new UnsupportedOperationException()).when(dictionary1).getLoadedWords(any());
    underTest.onDictionaryLoadingStarted(dictionary1);
    underTest.onDictionaryLoadingDone(dictionary1);

    Mockito.verify(dictionary1).getLoadedWords(any());
    ArgumentCaptor<List<char[][]>> wordsListCaptor = ArgumentCaptor.forClass(List.class);
    Mockito.verify(consumer).consumeWords(Mockito.same(keyboard), wordsListCaptor.capture(), any());

    Assert.assertEquals(0, wordsListCaptor.getValue().size());
  }
}
