package com.anysoftkeyboard.utils;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.base.utils.LogProvider;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.base.utils.NullLogProvider;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardTestRunner.class)
public class LoggerTest {

    @Test
    public void testSetLogProvider() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Mockito.verifyZeroInteractions(mockLog);
        Logger.d("mTag", "Text");
        Mockito.verify(mockLog).d("mTag", "Text");
        Mockito.verifyNoMoreInteractions(mockLog);

        Mockito.reset(mockLog);

        Logger.setLogProvider(new NullLogProvider());
        Logger.d("mTag", "Text2");
        Mockito.verifyZeroInteractions(mockLog);
    }

    @Test
    public void testGetAllLogLinesList() throws Exception {
        //filling up the log buffer
        for (int i=0; i<1024; i++) Logger.d("t", "t");

        final int initialListSize = Logger.getAllLogLinesList().size();

        //225 is the max lines count
        Assert.assertEquals(225, initialListSize);

        Logger.d("mTag", "Text1");
        Assert.assertEquals(initialListSize, Logger.getAllLogLinesList().size());

        Logger.i("TAG2", "Text2");
        Assert.assertEquals(initialListSize, Logger.getAllLogLinesList().size());

        final String expectedFirstLine = "-D-[mTag] Text1";
        final String expectedSecondLine = "-I-[TAG2] Text2";

        Assert.assertTrue(Logger.getAllLogLinesList().get(1).endsWith(expectedFirstLine));
        Assert.assertTrue(Logger.getAllLogLinesList().get(0).endsWith(expectedSecondLine));
    }

    @Test
    public void testGetAllLogLines() throws Exception {
        Logger.d("mTag", "Text1");

        final String expectedFirstLine = "-D-[mTag] Text1";

        Assert.assertTrue(Logger.getAllLogLines().endsWith(expectedFirstLine));
    }

    @Test
    public void testV() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.v("mTag", "Text with %d digits", 0);
        Mockito.verify(mockLog).v("mTag", "Text with 0 digits");

        Logger.v("mTag", "Text with no digits");
        Mockito.verify(mockLog).v("mTag", "Text with no digits");
    }

    @Test
    public void testD() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.d("mTag", "Text with %d digits", 1);
        Mockito.verify(mockLog).d("mTag", "Text with 1 digits");

        Logger.d("mTag", "Text with no digits");
        Mockito.verify(mockLog).d("mTag", "Text with no digits");
    }

    @Test
    public void testYell() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.yell("mTag", "Text with %d digits", 2);
        Mockito.verify(mockLog).yell("mTag", "Text with 2 digits");

        Logger.yell("mTag", "Text with no digits");
        Mockito.verify(mockLog).yell("mTag", "Text with no digits");
    }

    @Test
    public void testI() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.i("mTag", "Text with %d digits", 0);
        Mockito.verify(mockLog).i("mTag", "Text with 0 digits");

        Logger.i("mTag", "Text with no digits");
        Mockito.verify(mockLog).i("mTag", "Text with no digits");

    }

    @Test
    public void testW() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.w("mTag", "Text with %d digits", 0);
        Mockito.verify(mockLog).w("mTag", "Text with 0 digits");

        Logger.w("mTag", "Text with no digits");
        Mockito.verify(mockLog).w("mTag", "Text with no digits");
    }

    @Test
    public void testE1() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.e("mTag", "Text with %d digits", 0);
        Mockito.verify(mockLog).e("mTag", "Text with 0 digits");

        Logger.e("mTag", "Text with no digits");
        Mockito.verify(mockLog).e("mTag", "Text with no digits");
    }

    @Test
    public void testWtf() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.wtf("mTag", "Text with %d digits", 0);
        Mockito.verify(mockLog).wtf("mTag", "Text with 0 digits");

        Logger.wtf("mTag", "Text with no digits");
        Mockito.verify(mockLog).wtf("mTag", "Text with no digits");
    }
}