package com.anysoftkeyboard.chewbacca;

import android.os.Parcel;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class BugReportDetailsTest {

    @Test
    public void testHappyPath() {
        RuntimeException inner = new RuntimeException("Inner");
        IOException exception = new IOException("the top", inner);
        String crashReport = "a huge crash report";
        BugReportDetails details = new BugReportDetails(exception, crashReport);

        Assert.assertSame(exception, details.throwable);
        Assert.assertSame(crashReport, details.crashReportText);

        final Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        details.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        BugReportDetails read = new BugReportDetails(parcel);

        Assert.assertEquals(details.crashReportText, read.crashReportText);
        Assert.assertEquals(details.throwable.getClass(), read.throwable.getClass());
        Assert.assertArrayEquals(details.throwable.getStackTrace(), read.throwable.getStackTrace());
        Assert.assertEquals(details.throwable.getMessage(), read.throwable.getMessage());
        Assert.assertEquals(
                details.throwable.getCause().getClass(), read.throwable.getCause().getClass());
        Assert.assertEquals(
                details.throwable.getCause().getMessage(), read.throwable.getCause().getMessage());
    }
}
