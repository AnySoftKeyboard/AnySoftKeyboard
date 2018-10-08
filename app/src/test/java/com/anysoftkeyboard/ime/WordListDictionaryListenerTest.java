package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.dictionaries.Dictionary;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.List;

import io.reactivex.functions.Consumer;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class WordListDictionaryListenerTest {


    @Test
    @SuppressWarnings("unchecked")
    public void testWaitsTillAllDictionariesLoadedBeforeGetWords() throws Exception {
        Consumer<List<char[][]>> consumer = Mockito.mock(Consumer.class);

        AnySoftKeyboardWithGestureTyping.WordListDictionaryListener underTest = new AnySoftKeyboardWithGestureTyping.WordListDictionaryListener(consumer);
        final Dictionary dictionary1 = Mockito.mock(Dictionary.class);
        Mockito.doReturn(new char[0][0]).when(dictionary1).getWords();
        final Dictionary dictionary2 = Mockito.mock(Dictionary.class);
        Mockito.doReturn(new char[0][0]).when(dictionary2).getWords();
        underTest.onDictionaryLoadingStarted(dictionary1);
        underTest.onDictionaryLoadingStarted(dictionary2);
        underTest.onDictionaryLoadingDone(dictionary1);

        Mockito.verify(dictionary1).getWords();
        Mockito.verify(dictionary2, Mockito.never()).getWords();
        Mockito.verify(consumer, Mockito.never()).accept(Mockito.anyList());

        underTest.onDictionaryLoadingDone(dictionary2);

        Mockito.verify(dictionary2).getWords();
        Mockito.verify(consumer).accept(Mockito.anyList());
    }
}
