package com.anysoftkeyboard.utils;

import com.menny.android.anysoftkeyboard.AskGradleTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AskGradleTestRunner.class)
public class LoggerTest {

    @Test
    public void testSetLogProvider() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Mockito.verifyZeroInteractions(mockLog);
        Logger.d("TAG", "Text");
        Mockito.verify(mockLog).d("TAG", "Text");
        Mockito.verifyNoMoreInteractions(mockLog);

        Mockito.reset(mockLog);

        Logger.setLogProvider(new NullLogProvider());
        Logger.d("TAG", "Text2");
        Mockito.verifyZeroInteractions(mockLog);
    }

    @Test
    public void testGetAllLogLinesList() throws Exception {
        final int initialListSize = Logger.getAllLogLinesList().size();

        //225 is the max lines count
        Assert.assertEquals(225, initialListSize);

        Logger.d("TAG", "Text1");
        Assert.assertEquals(initialListSize, Logger.getAllLogLinesList().size());

        Logger.i("TAG2", "Text2");
        Assert.assertEquals(initialListSize, Logger.getAllLogLinesList().size());

        final long currentTime = System.currentTimeMillis();
        final String expectedFirstLine = currentTime + "-D-[TAG] Text1";
        final String expectedSecondLine = currentTime + "-I-[TAG2] Text2";

        Assert.assertEquals(expectedFirstLine, Logger.getAllLogLinesList().get(1));
        Assert.assertEquals(expectedSecondLine, Logger.getAllLogLinesList().get(0));
    }

    @Test
    public void testGetAllLogLines() throws Exception {
        Logger.d("TAG", "Text1");
        Logger.i("TAG2", "Text2");

        final long currentTime = System.currentTimeMillis();
        final String expectedFirstLine = currentTime + "-D-[TAG] Text1";
        final String expectedSecondLine = currentTime + "-I-[TAG2] Text2";

        Assert.assertTrue(Logger.getAllLogLines().endsWith(Logger.NEW_LINE + expectedFirstLine + Logger.NEW_LINE + expectedSecondLine));
    }

    @Test
    public void testV() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.v("TAG", "Text with %d digits", 0);
        Mockito.verify(mockLog).v("TAG", "Text with 0 digits");

        Logger.v("TAG", "Text with no digits");
        Mockito.verify(mockLog).v("TAG", "Text with no digits");
    }

    @Test
    public void testD() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.d("TAG", "Text with %d digits", 1);
        Mockito.verify(mockLog).d("TAG", "Text with 1 digits");

        Logger.d("TAG", "Text with no digits");
        Mockito.verify(mockLog).d("TAG", "Text with no digits");
    }

    @Test
    public void testYell() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.yell("TAG", "Text with %d digits", 2);
        Mockito.verify(mockLog).yell("TAG", "Text with 2 digits");

        Logger.yell("TAG", "Text with no digits");
        Mockito.verify(mockLog).yell("TAG", "Text with no digits");
    }

    @Test
    public void testI() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.i("TAG", "Text with %d digits", 0);
        Mockito.verify(mockLog).i("TAG", "Text with 0 digits");

        Logger.i("TAG", "Text with no digits");
        Mockito.verify(mockLog).i("TAG", "Text with no digits");

    }

    @Test
    public void testW() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.w("TAG", "Text with %d digits", 0);
        Mockito.verify(mockLog).w("TAG", "Text with 0 digits");

        Logger.w("TAG", "Text with no digits");
        Mockito.verify(mockLog).w("TAG", "Text with no digits");
    }

    @Test
    public void testE1() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.e("TAG", "Text with %d digits", 0);
        Mockito.verify(mockLog).e("TAG", "Text with 0 digits");

        Logger.e("TAG", "Text with no digits");
        Mockito.verify(mockLog).e("TAG", "Text with no digits");
    }

    @Test
    public void testWtf() throws Exception {
        LogProvider mockLog = Mockito.mock(LogProvider.class);
        Logger.setLogProvider(mockLog);

        Logger.wtf("TAG", "Text with %d digits", 0);
        Mockito.verify(mockLog).wtf("TAG", "Text with 0 digits");

        Logger.wtf("TAG", "Text with no digits");
        Mockito.verify(mockLog).wtf("TAG", "Text with no digits");
    }
}