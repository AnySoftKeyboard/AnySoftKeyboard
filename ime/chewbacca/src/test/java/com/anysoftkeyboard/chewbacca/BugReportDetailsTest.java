package com.anysoftkeyboard.chewbacca;

import android.net.Uri;
import android.os.Parcel;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class BugReportDetailsTest {

    @Test
    public void testHappyPath() {
        String header = "header";
        String crashReport = "a huge crash report";
        Uri someFile = Uri.fromFile(new File("/blah/blah.txt"));
        BugReportDetails details = new BugReportDetails(header, crashReport, someFile);

        Assert.assertSame(header, details.crashHeader);
        Assert.assertSame(crashReport, details.crashReportText);
        Assert.assertSame(someFile, details.fullReport);

        final Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        details.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        BugReportDetails read = new BugReportDetails(parcel);

        Assert.assertEquals(details.crashHeader, read.crashHeader);
        Assert.assertEquals(details.crashReportText, read.crashReportText);
        Assert.assertEquals(details.fullReport, read.fullReport);
    }
}
