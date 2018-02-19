package com.menny.android.anysoftkeyboard;

import android.app.Application;
import android.database.ContentObserver;
import android.os.Build;
import android.view.GestureDetector;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.devicespecific.AskOnGestureListener;
import com.anysoftkeyboard.devicespecific.AskV19GestureDetector;
import com.anysoftkeyboard.devicespecific.AskV8GestureDetector;
import com.anysoftkeyboard.devicespecific.Clipboard;
import com.anysoftkeyboard.devicespecific.ClipboardV11;
import com.anysoftkeyboard.devicespecific.ClipboardV3;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.devicespecific.DeviceSpecificLowest;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV11;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV14;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV16;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV19;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV24;
import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.dictionaries.DictionaryContentObserver;
import com.anysoftkeyboard.dictionaries.DictionaryContentObserverAPI16;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public abstract class AnyApplicationDeviceSpecificTest {

    private final List<Class> mExpectedDeviceSpecificClass = Arrays.asList(
            DeviceSpecificLowest.class,//0
            DeviceSpecificLowest.class,//1
            DeviceSpecificLowest.class,
            DeviceSpecificLowest.class,
            DeviceSpecificLowest.class,
            DeviceSpecificLowest.class,
            DeviceSpecificLowest.class,
            DeviceSpecificLowest.class,
            DeviceSpecificLowest.class,//8
            DeviceSpecificLowest.class,
            DeviceSpecificLowest.class,//10
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
            DeviceSpecificV24.class,
            DeviceSpecificV24.class,
            DeviceSpecificV24.class,
            DeviceSpecificV24.class);

    private final List<Class> mExpectedClipboardClass = Arrays.asList(
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
            ClipboardV11.class);

    private final List<Class> mExpectedDictionaryObserverClass = Arrays.asList(
            DictionaryContentObserver.class,//0
            DictionaryContentObserver.class,//1
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,//8
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,//10
            DictionaryContentObserver.class,//11
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,
            DictionaryContentObserver.class,//14
            DictionaryContentObserver.class,
            DictionaryContentObserverAPI16.class,//16
            DictionaryContentObserverAPI16.class,
            DictionaryContentObserverAPI16.class,
            DictionaryContentObserverAPI16.class,//19
            DictionaryContentObserverAPI16.class,//20
            DictionaryContentObserverAPI16.class,
            DictionaryContentObserverAPI16.class,
            DictionaryContentObserverAPI16.class,
            DictionaryContentObserverAPI16.class,
            DictionaryContentObserverAPI16.class,
            DictionaryContentObserverAPI16.class,
            DictionaryContentObserverAPI16.class);

    private final List<Class> mExpectedGestureDetectorClass = Arrays.asList(
            GestureDetector.class,//0
            GestureDetector.class,//1
            GestureDetector.class,
            GestureDetector.class,
            GestureDetector.class,
            GestureDetector.class,
            GestureDetector.class,
            GestureDetector.class,
            AskV8GestureDetector.class,//8
            AskV8GestureDetector.class,
            AskV8GestureDetector.class,//10
            AskV8GestureDetector.class,//11
            AskV8GestureDetector.class,
            AskV8GestureDetector.class,
            AskV8GestureDetector.class,//14
            AskV8GestureDetector.class,
            AskV8GestureDetector.class,//16
            AskV8GestureDetector.class,
            AskV8GestureDetector.class,
            AskV19GestureDetector.class,//19
            AskV19GestureDetector.class,//20
            AskV19GestureDetector.class,
            AskV19GestureDetector.class,
            AskV19GestureDetector.class,
            AskV19GestureDetector.class,
            AskV19GestureDetector.class,
            AskV19GestureDetector.class,
            AskV19GestureDetector.class);

    public static class AnyApplicationDeviceSpecificTest1 extends AnyApplicationDeviceSpecificTest {

        @Test
        @Config(minSdk = 16, maxSdk = 21)
        public void testCreateDeviceSpecificImplementation1() throws Exception {
            implTestCreateDeviceSpecificImplementation();
        }

    }

    public static class AnyApplicationDeviceSpecificTest2 extends AnyApplicationDeviceSpecificTest {

        @Test
        @Config(minSdk = 22, maxSdk = 26)
        public void testCreateDeviceSpecificImplementation2() throws Exception {
            implTestCreateDeviceSpecificImplementation();
        }

    }

    void implTestCreateDeviceSpecificImplementation() {
        if (Build.VERSION.SDK_INT > 100) return;//FUTURE?

        final Application application = RuntimeEnvironment.application;

        final DeviceSpecific deviceSpecific = AnyApplication.getDeviceSpecific();
        Assert.assertNotNull(deviceSpecific);
        Assert.assertSame(mExpectedDeviceSpecificClass.get(Build.VERSION.SDK_INT), deviceSpecific.getClass());

        Assert.assertEquals(deviceSpecific.getClass().getSimpleName(), deviceSpecific.getApiLevel());

        final Clipboard clipboard = deviceSpecific.createClipboard(application);
        Assert.assertNotNull(clipboard);
        Assert.assertSame(mExpectedClipboardClass.get(Build.VERSION.SDK_INT), clipboard.getClass());

        final ContentObserver dictionaryContentObserver = deviceSpecific.createDictionaryContentObserver(Mockito.mock(BTreeDictionary.class));
        Assert.assertNotNull(dictionaryContentObserver);
        Assert.assertSame(mExpectedDictionaryObserverClass.get(Build.VERSION.SDK_INT), dictionaryContentObserver.getClass());

        final GestureDetector gestureDetector = deviceSpecific.createGestureDetector(application, Mockito.mock(AskOnGestureListener.class));
        Assert.assertNotNull(gestureDetector);
        Assert.assertSame(mExpectedGestureDetectorClass.get(Build.VERSION.SDK_INT), gestureDetector.getClass());
    }
}