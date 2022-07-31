package com.anysoftkeyboard.chewbacca;

import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ChewbaccaUtilsTest {

    @Test
    public void testGetSysInfo() {
        String info = ChewbaccaUtils.getSysInfo(ApplicationProvider.getApplicationContext());
        Assert.assertTrue(info.contains("BRAND:" + Build.BRAND));
        Assert.assertTrue(info.contains("VERSION.SDK_INT:" + Build.VERSION.SDK_INT));
        Assert.assertTrue(
                info.contains(
                        "Locale:"
                                + ApplicationProvider.getApplicationContext()
                                        .getResources()
                                        .getConfiguration()
                                        .locale));
    }
}
