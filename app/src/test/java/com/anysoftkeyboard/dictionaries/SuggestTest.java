package com.anysoftkeyboard.dictionaries;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class SuggestTest {

    private SuggestionsProvider mProvider;
    private Suggest mUnderTest;

    @Before
    public void setUp() throws Exception {
        mProvider = Mockito.mock(SuggestionsProvider.class);

        mUnderTest = new Suggest(mProvider);
    }

    @Test
    public void testDelegatesIncognito() {
        Mockito.verify(mProvider, Mockito.never()).setIncognitoMode(Mockito.anyBoolean());

        mUnderTest.setIncognitoMode(true);
        Mockito.doReturn(true).when(mProvider).isIncognitoMode();

        Mockito.verify(mProvider).setIncognitoMode(true);
        Mockito.verifyNoMoreInteractions(mProvider);

        Assert.assertTrue(mUnderTest.isIncognitoMode());
        //noinspection ResultOfMethodCallIgnored
        Mockito.verify(mProvider).isIncognitoMode();
        Mockito.verifyNoMoreInteractions(mProvider);
        Mockito.reset(mProvider);

        mUnderTest.setIncognitoMode(false);
        Mockito.doReturn(false).when(mProvider).isIncognitoMode();

        Mockito.verify(mProvider).setIncognitoMode(false);
        Mockito.verifyNoMoreInteractions(mProvider);

        Assert.assertFalse(mUnderTest.isIncognitoMode());
        //noinspection ResultOfMethodCallIgnored
        Mockito.verify(mProvider).isIncognitoMode();
        Mockito.verifyNoMoreInteractions(mProvider);
    }

}