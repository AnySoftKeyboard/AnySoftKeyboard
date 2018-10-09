package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.keyboards.AnyKeyboard;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class WordListDictionaryListenerTest {


    @Test
    @SuppressWarnings("unchecked")
    public void testWaitsTillAllDictionariesLoadedBeforeGetWords() throws Exception {
        AnySoftKeyboardWithGestureTyping.WordListDictionaryListener.Callback consumer = Mockito.mock(AnySoftKeyboardWithGestureTyping.WordListDictionaryListener.Callback.class);
        AnyKeyboard keyboard = Mockito.mock(AnyKeyboard.class);

        AnySoftKeyboardWithGestureTyping.WordListDictionaryListener underTest = new AnySoftKeyboardWithGestureTyping.WordListDictionaryListener(keyboard, consumer);
        final Dictionary dictionary1 = Mockito.mock(Dictionary.class);
        Mockito.doReturn(new char[1][1]).when(dictionary1).getWords();
        final Dictionary dictionary2 = Mockito.mock(Dictionary.class);
        Mockito.doReturn(new char[2][2]).when(dictionary2).getWords();
        underTest.onDictionaryLoadingStarted(dictionary1);
        underTest.onDictionaryLoadingStarted(dictionary2);
        underTest.onDictionaryLoadingDone(dictionary1);

        Mockito.verify(dictionary1).getWords();
        Mockito.verify(dictionary2, Mockito.never()).getWords();
        Mockito.verify(consumer, Mockito.never()).consumeWords(Mockito.any(), Mockito.anyList());

        underTest.onDictionaryLoadingDone(dictionary2);

        Mockito.verify(dictionary2).getWords();
        ArgumentCaptor<List<char[][]>> wordsListCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(consumer).consumeWords(Mockito.same(keyboard), wordsListCaptor.capture());

        Assert.assertEquals(2, wordsListCaptor.getValue().size());
        Assert.assertEquals(1, wordsListCaptor.getValue().get(0).length);
        Assert.assertEquals(2, wordsListCaptor.getValue().get(1).length);
    }
}
