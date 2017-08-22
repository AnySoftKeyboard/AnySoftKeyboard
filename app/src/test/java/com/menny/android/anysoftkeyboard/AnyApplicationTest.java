package com.menny.android.anysoftkeyboard;

import android.app.Application;
import android.os.Build;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.backup.CloudBackupRequesterApi8;
import com.anysoftkeyboard.backup.NoOpCloudBackupRequester;
import com.anysoftkeyboard.devicespecific.Clipboard;
import com.anysoftkeyboard.devicespecific.ClipboardV11;
import com.anysoftkeyboard.devicespecific.ClipboardV3;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV11;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV14;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV16;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV19;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV3;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV8;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AnyApplicationTest {

    private final Class[] mExpectedDeviceSpecificClass = new Class[]{
            DeviceSpecificV3.class,//0
            DeviceSpecificV3.class,//1
            DeviceSpecificV3.class,
            DeviceSpecificV3.class,
            DeviceSpecificV3.class,
            DeviceSpecificV3.class,
            DeviceSpecificV3.class,
            DeviceSpecificV3.class,
            DeviceSpecificV8.class,//8
            DeviceSpecificV8.class,
            DeviceSpecificV8.class,//10
            DeviceSpecificV11.class,//11
            DeviceSpecificV11.class,
            DeviceSpecificV11.class,
            DeviceSpecificV14.class,//14
            DeviceSpecificV14.class,
            DeviceSpecificV16.class,//16
            DeviceSpecificV16.class,
            DeviceSpecificV16.class,
            DeviceSpecificV19.class,//19
            DeviceSpecificV19.class,//20
            DeviceSpecificV19.class,
            DeviceSpecificV19.class,
            DeviceSpecificV19.class,
            DeviceSpecificV19.class,
            DeviceSpecificV19.class,
            DeviceSpecificV19.class,
    };

    private final Class[] mExpectedClipboardClass = new Class[]{
            ClipboardV3.class,//0
            ClipboardV3.class,//1
            ClipboardV3.class,
            ClipboardV3.class,
            ClipboardV3.class,
            ClipboardV3.class,
            ClipboardV3.class,
            ClipboardV3.class,
            ClipboardV3.class,//8
            ClipboardV3.class,
            ClipboardV3.class,//10
            ClipboardV11.class,//11
            ClipboardV11.class,
            ClipboardV11.class,
            ClipboardV11.class,//14
            ClipboardV11.class,
            ClipboardV11.class,//16
            ClipboardV11.class,
            ClipboardV11.class,
            ClipboardV11.class,//19
            ClipboardV11.class,//20
            ClipboardV11.class,
            ClipboardV11.class,
            ClipboardV11.class,
            ClipboardV11.class,
            ClipboardV11.class,
            ClipboardV11.class,
    };

    private final Class[] mExpectedCloudBackupClass = new Class[]{
            NoOpCloudBackupRequester.class,//0
            NoOpCloudBackupRequester.class,//1
            NoOpCloudBackupRequester.class,
            NoOpCloudBackupRequester.class,
            NoOpCloudBackupRequester.class,
            NoOpCloudBackupRequester.class,
            NoOpCloudBackupRequester.class,
            NoOpCloudBackupRequester.class,
            CloudBackupRequesterApi8.class,//8
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,//10
            CloudBackupRequesterApi8.class,//11
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,//14
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,//16
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,//19
            CloudBackupRequesterApi8.class,//20
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,
            CloudBackupRequesterApi8.class,
    };

    @Test
    @Config(sdk = Config.ALL_SDKS)
    public void testCreateDeviceSpecificImplementation() throws Exception {
        if (Build.VERSION.SDK_INT > 100) return;//FUTURE?

        final Application application = RuntimeEnvironment.application;

        final DeviceSpecific deviceSpecific = AnyApplication.getDeviceSpecific();
        Assert.assertNotNull(deviceSpecific);
        Assert.assertSame(mExpectedDeviceSpecificClass[Build.VERSION.SDK_INT], deviceSpecific.getClass());

        final Clipboard clipboard = deviceSpecific.createClipboard(application);
        Assert.assertNotNull(clipboard);
        Assert.assertSame(mExpectedClipboardClass[Build.VERSION.SDK_INT], clipboard.getClass());

        final CloudBackupRequester cloudBackupRequester = deviceSpecific.createCloudBackupRequester(application);
        Assert.assertNotNull(cloudBackupRequester);
        Assert.assertSame(mExpectedCloudBackupClass[Build.VERSION.SDK_INT], cloudBackupRequester.getClass());
    }
}