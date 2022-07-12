package com.anysoftkeyboard.chewbacca;

import android.os.Parcel;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class BugReportDetailsTest {

    @Test
    public void testHappyPath() {
        String crashReport = "a huge crash report";
        BugReportDetails details = new BugReportDetails(crashReport);

        Assert.assertSame(crashReport, details.crashReportText);

        final Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        details.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        BugReportDetails read = new BugReportDetails(parcel);

        Assert.assertEquals(details.crashReportText, read.crashReportText);
    }
}
