package com.anysoftkeyboard.base.utils;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class LoggerTest {

    private LogProvider mMockLog;

    @Before
    public void setUp() {
        mMockLog = Mockito.mock(LogProvider.class);
        Mockito.when(mMockLog.supportsV()).thenReturn(true);
        Mockito.when(mMockLog.supportsD()).thenReturn(true);
        Mockito.when(mMockLog.supportsI()).thenReturn(true);
        Mockito.when(mMockLog.supportsW()).thenReturn(true);
        Mockito.when(mMockLog.supportsE()).thenReturn(true);
        Mockito.when(mMockLog.supportsWTF()).thenReturn(true);
        Mockito.when(mMockLog.supportsYell()).thenReturn(true);

        Logger.setLogProvider(mMockLog);
    }

    @Test
    public void testSetLogProvider() throws Exception {
        Mockito.verifyZeroInteractions(mMockLog);
        Logger.d("mTag", "Text");
        Mockito.verify(mMockLog).supportsD();
        Mockito.verify(mMockLog).d("mTag", "Text");
        Mockito.verifyNoMoreInteractions(mMockLog);

        Mockito.reset(mMockLog);

        Logger.setLogProvider(new NullLogProvider());
        Logger.d("mTag", "Text2");
        Mockito.verifyZeroInteractions(mMockLog);
    }

    @Test
    public void testSetLogProviderWhenDisabled() throws Exception {
        Mockito.when(mMockLog.supportsD()).thenReturn(false);
        Logger.setLogProvider(mMockLog);

        Mockito.verifyZeroInteractions(mMockLog);
        Logger.d("mTag", "Text");
        Mockito.verify(mMockLog).supportsD();
        Mockito.verifyNoMoreInteractions(mMockLog);

        Mockito.reset(mMockLog);

        Logger.setLogProvider(new NullLogProvider());
        Logger.d("mTag", "Text2");
        Mockito.verifyZeroInteractions(mMockLog);
    }

    @Test
    public void testGetAllLogLinesList() throws Exception {
        //filling up the log buffer
        for (int i = 0; i < 1024; i++) Logger.d("t", "t");

        final int initialListSize = Logger.getAllLogLinesList().size();

        //225 is the max lines count
        Assert.assertEquals(255, initialListSize);

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
        Logger.v("mTag", "Text with %d digits", 0);
        Mockito.verify(mMockLog).v("mTag", "Text with 0 digits");

        Logger.v("mTag", "Text with no digits");
        Mockito.verify(mMockLog).v("mTag", "Text with no digits");
    }

    @Test
    public void testVNotSupported() throws Exception {
        Mockito.when(mMockLog.supportsV()).thenReturn(false);
        Logger.v("mTag", "Text with %d digits", 0);
        Mockito.verify(mMockLog, Mockito.never()).v("mTag", "Text with 0 digits");

        Logger.v("mTag", "Text with no digits");
        Mockito.verify(mMockLog, Mockito.never()).v("mTag", "Text with no digits");
    }

    @Test
    public void testD() throws Exception {
        Logger.d("mTag", "Text with %d digits", 1);
        Mockito.verify(mMockLog).d("mTag", "Text with 1 digits");

        Logger.d("mTag", "Text with no digits");
        Mockito.verify(mMockLog).d("mTag", "Text with no digits");
    }

    @Test
    public void testDNotSupported() throws Exception {
        Mockito.when(mMockLog.supportsD()).thenReturn(false);
        Logger.d("mTag", "Text with %d digits", 1);
        Mockito.verify(mMockLog, Mockito.never()).d("mTag", "Text with 1 digits");

        Logger.d("mTag", "Text with no digits");
        Mockito.verify(mMockLog, Mockito.never()).d("mTag", "Text with no digits");
    }

    @Test
    public void testYell() throws Exception {
        Logger.yell("mTag", "Text with %d digits", 2);
        Mockito.verify(mMockLog).yell("mTag", "Text with 2 digits");

        Logger.yell("mTag", "Text with no digits");
        Mockito.verify(mMockLog).yell("mTag", "Text with no digits");
    }

    @Test
    public void testYellNotSupported() throws Exception {
        Mockito.when(mMockLog.supportsYell()).thenReturn(false);
        Logger.yell("mTag", "Text with %d digits", 2);
        Mockito.verify(mMockLog, Mockito.never()).yell("mTag", "Text with 2 digits");

        Logger.yell("mTag", "Text with no digits");
        Mockito.verify(mMockLog, Mockito.never()).yell("mTag", "Text with no digits");

        //yes, other levels
        Logger.d("mTag", "Text with no digits");
        Mockito.verify(mMockLog).d("mTag", "Text with no digits");
    }

    @Test
    public void testI() throws Exception {
        Logger.i("mTag", "Text with %d digits", 0);
        Mockito.verify(mMockLog).i("mTag", "Text with 0 digits");

        Logger.i("mTag", "Text with no digits");
        Mockito.verify(mMockLog).i("mTag", "Text with no digits");
    }

    @Test
    public void testINotSupported() throws Exception {
        Mockito.when(mMockLog.supportsI()).thenReturn(false);
        Logger.i("mTag", "Text with %d digits", 2);
        Mockito.verify(mMockLog, Mockito.never()).i("mTag", "Text with 2 digits");

        Logger.i("mTag", "Text with no digits");
        Mockito.verify(mMockLog, Mockito.never()).i("mTag", "Text with no digits");

        //yes, other levels
        Logger.d("mTag", "Text with no digits");
        Mockito.verify(mMockLog).d("mTag", "Text with no digits");
    }

    @Test
    public void testW() throws Exception {
        Logger.w("mTag", "Text with %d digits", 0);
        Mockito.verify(mMockLog).w("mTag", "Text with 0 digits");

        Logger.w("mTag", "Text with no digits");
        Mockito.verify(mMockLog).w("mTag", "Text with no digits");
    }

    @Test
    public void testWNotSupported() throws Exception {
        Mockito.when(mMockLog.supportsW()).thenReturn(false);
        Logger.w("mTag", "Text with %d digits", 2);
        Mockito.verify(mMockLog, Mockito.never()).w("mTag", "Text with 2 digits");

        Logger.w("mTag", "Text with no digits");
        Mockito.verify(mMockLog, Mockito.never()).w("mTag", "Text with no digits");

        //yes, other levels
        Logger.d("mTag", "Text with no digits");
        Mockito.verify(mMockLog).d("mTag", "Text with no digits");
    }

    @Test
    public void testE1() throws Exception {
        Logger.e("mTag", "Text with %d digits", 0);
        Mockito.verify(mMockLog).e("mTag", "Text with 0 digits");

        Logger.e("mTag", "Text with no digits");
        Mockito.verify(mMockLog).e("mTag", "Text with no digits");
    }

    @Test
    public void testENotSupported() throws Exception {
        Mockito.when(mMockLog.supportsE()).thenReturn(false);
        Logger.e("mTag", "Text with %d digits", 2);
        Mockito.verify(mMockLog, Mockito.never()).e("mTag", "Text with 2 digits");

        Logger.e("mTag", "Text with no digits");
        Mockito.verify(mMockLog, Mockito.never()).e("mTag", "Text with no digits");

        //yes, other levels
        Logger.d("mTag", "Text with no digits");
        Mockito.verify(mMockLog).d("mTag", "Text with no digits");
    }

    @Test
    public void testWtf() throws Exception {
        Logger.wtf("mTag", "Text with %d digits", 0);
        Mockito.verify(mMockLog).wtf("mTag", "Text with 0 digits");

        Logger.wtf("mTag", "Text with no digits");
        Mockito.verify(mMockLog).wtf("mTag", "Text with no digits");
    }

    @Test
    public void testWtfNotSupported() throws Exception {
        Mockito.when(mMockLog.supportsWTF()).thenReturn(false);
        Logger.wtf("mTag", "Text with %d digits", 2);
        Mockito.verify(mMockLog, Mockito.never()).wtf("mTag", "Text with 2 digits");

        Logger.wtf("mTag", "Text with no digits");
        Mockito.verify(mMockLog, Mockito.never()).wtf("mTag", "Text with no digits");

        //yes, other levels
        Logger.d("mTag", "Text with no digits");
        Mockito.verify(mMockLog).d("mTag", "Text with no digits");
    }
}