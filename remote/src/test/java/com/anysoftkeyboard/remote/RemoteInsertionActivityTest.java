package com.anysoftkeyboard.remote;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;

import androidx.test.core.app.ApplicationProvider;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RemoteInsertionActivityTest {

    private ShadowApplication mShadowApplication;

    @Before
    public void setup() {
        mShadowApplication = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext());
    }

    @Test
    public void testHappyPath() {
        ActivityController<RemoteInsertionActivity> controller = Robolectric.buildActivity(RemoteInsertionActivity.class,
                RemoteInsertionActivity.createPickingActivityIntent(new String[]{"image/png"}, 123, ApplicationProvider.getApplicationContext()));
        controller.setup();

        final ShadowActivity shadowActivity = Shadows.shadowOf(controller.get());

        final ShadowActivity.IntentForResult activityForResult = shadowActivity.getNextStartedActivityForResult();
        final Intent chooserIntent = activityForResult.intent;
        Assert.assertNotNull(chooserIntent);
        Assert.assertEquals(Intent.ACTION_CHOOSER, chooserIntent.getAction());
        final Intent actualIntent = chooserIntent.getParcelableExtra(Intent.EXTRA_INTENT);
        Assert.assertNotNull(actualIntent);
        Assert.assertEquals(Intent.ACTION_PICK, actualIntent.getAction());
        Assert.assertEquals(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, actualIntent.getData());
        Assert.assertEquals("image/*", actualIntent.getType());

        Assert.assertEquals(0, mShadowApplication.getBroadcastIntents().size());
    }

    @Test
    public void testDoesNotRequestOnRecreate() {
        Bundle outState = new Bundle();
        outState.putInt(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_URI_BUNDLE_KEY, 234);
        outState.putStringArray(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY, new String[]{"image/png"});

        ActivityController<RemoteInsertionActivity> controller = Robolectric.buildActivity(RemoteInsertionActivity.class,
                RemoteInsertionActivity.createPickingActivityIntent(new String[]{"image/png"}, 123, ApplicationProvider.getApplicationContext()));
        controller.setup(outState);

        Assert.assertNull(Shadows.shadowOf(controller.get()).getNextStartedActivityForResult());
    }
}