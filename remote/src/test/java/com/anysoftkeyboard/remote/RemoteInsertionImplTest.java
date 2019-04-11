package com.anysoftkeyboard.remote;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v13.view.inputmethod.InputContentInfoCompat;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

import androidx.test.core.app.ApplicationProvider;
import io.reactivex.Single;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class RemoteInsertionImplTest {

    private RemoteInsertionImpl mUnderTest;
    private Uri mFakeUriResponse;
    private Uri mReceivedRemoteUri;
    private InsertionRequestCallback mCallback;
    private ShadowApplication mShadowApplication;

    @Before
    public void setup() {
        mShadowApplication = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext());
        mCallback = Mockito.mock(InsertionRequestCallback.class);
        mUnderTest = new RemoteInsertionImpl(ApplicationProvider.getApplicationContext(), this::fakeProxy);
    }

    @Test
    public void testReceiverLifeCycle() {
        Assert.assertEquals(1,
                mShadowApplication.getRegisteredReceivers()
                        .stream()
                        .filter(wrapper -> wrapper.broadcastReceiver instanceof RemoteInsertionImpl.MediaInsertionAvailableReceiver)
                        .count());

        mUnderTest.destroy();
        Assert.assertEquals(0,
                mShadowApplication.getRegisteredReceivers()
                        .stream()
                        .filter(wrapper -> wrapper.broadcastReceiver instanceof RemoteInsertionImpl.MediaInsertionAvailableReceiver)
                        .count());
    }

    @Test
    public void testStartsPickActivityWithRequest() {
        mUnderTest.startMediaRequest(new String[]{"media/png"}, 123, mCallback);

        Mockito.verifyZeroInteractions(mCallback);

        final Intent nextStartedActivity = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getNextStartedActivity();

        Assert.assertNotNull(nextStartedActivity);
        Assert.assertEquals(RemoteInsertionActivity.class.getName(), nextStartedActivity.getComponent().getClassName());
        Assert.assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, nextStartedActivity.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK);

        Assert.assertArrayEquals(new String[]{"media/png"}, nextStartedActivity.getStringArrayExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY));
        Assert.assertEquals(123, nextStartedActivity.getIntExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_REQUEST_ID, 0));

        Mockito.verifyZeroInteractions(mCallback);
    }

    @Test
    public void testCorrectBroadcast() {
        mFakeUriResponse = Uri.parse("content://ask/image.png");
        mUnderTest.startMediaRequest(new String[]{"media/png"}, 123, mCallback);

        mShadowApplication.getRegisteredReceivers()
                .stream()
                .filter(wrapper -> wrapper.broadcastReceiver instanceof RemoteInsertionImpl.MediaInsertionAvailableReceiver)
                .map(ShadowApplication.Wrapper::getBroadcastReceiver)
                .findFirst().get().onReceive(ApplicationProvider.getApplicationContext(),
                createReceiverIntent(Uri.parse("content://example/image.png"), new String[]{"media/png"}, 123));

        Assert.assertEquals(Uri.parse("content://example/image.png"), mReceivedRemoteUri);

        ArgumentCaptor<InputContentInfoCompat> argumentCaptor = ArgumentCaptor.forClass(InputContentInfoCompat.class);
        Mockito.verify(mCallback).onMediaRequestDone(Mockito.eq(123), argumentCaptor.capture());
        Assert.assertEquals(mFakeUriResponse, argumentCaptor.getValue().getContentUri());
        Assert.assertEquals(1, argumentCaptor.getValue().getDescription().getMimeTypeCount());
        Assert.assertEquals("media/png", argumentCaptor.getValue().getDescription().getMimeType(0));
    }

    @Test
    public void testCorrectBroadcastWithoutRequestMade() {
        mShadowApplication.getRegisteredReceivers()
                .stream()
                .filter(wrapper -> wrapper.broadcastReceiver instanceof RemoteInsertionImpl.MediaInsertionAvailableReceiver)
                .map(ShadowApplication.Wrapper::getBroadcastReceiver)
                .findFirst().get().onReceive(ApplicationProvider.getApplicationContext(),
                createReceiverIntent(Uri.parse("content://example/image.png"), new String[]{"media/png"}, 0));

        Mockito.verifyZeroInteractions(mCallback);
    }

    @Test
    public void testIncorrectEmptyIntent() {
        mUnderTest.startMediaRequest(new String[]{"media/png"}, 123, mCallback);

        mShadowApplication.getRegisteredReceivers()
                .stream()
                .filter(wrapper -> wrapper.broadcastReceiver instanceof RemoteInsertionImpl.MediaInsertionAvailableReceiver)
                .map(ShadowApplication.Wrapper::getBroadcastReceiver)
                .findFirst().get().onReceive(ApplicationProvider.getApplicationContext(),
                createReceiverIntent(null, null, 0));

        Mockito.verifyZeroInteractions(mCallback);
    }

    @Test
    public void testIncorrectEmptyDataBroadcast() {
        mUnderTest.startMediaRequest(new String[]{"media/png"}, 123, mCallback);

        mShadowApplication.getRegisteredReceivers()
                .stream()
                .filter(wrapper -> wrapper.broadcastReceiver instanceof RemoteInsertionImpl.MediaInsertionAvailableReceiver)
                .map(ShadowApplication.Wrapper::getBroadcastReceiver)
                .findFirst().get().onReceive(ApplicationProvider.getApplicationContext(),
                createReceiverIntent(null, new String[]{"media/png"}, 123));

        Mockito.verify(mCallback).onMediaRequestCancelled(123);
    }

    @Test
    public void testIncorrectRequestBroadcast() {
        mUnderTest.startMediaRequest(new String[]{"media/png"}, 123, mCallback);

        mShadowApplication.getRegisteredReceivers()
                .stream()
                .filter(wrapper -> wrapper.broadcastReceiver instanceof RemoteInsertionImpl.MediaInsertionAvailableReceiver)
                .map(ShadowApplication.Wrapper::getBroadcastReceiver)
                .findFirst().get().onReceive(ApplicationProvider.getApplicationContext(),
                createReceiverIntent(null, new String[]{"media/png"}, 2));

        Mockito.verifyZeroInteractions(mCallback);
    }

    private static Intent createReceiverIntent(String action, Uri data, String[] mimeTypes, int requestId) {
        final Intent intent = new Intent(action);

        if (data != null) {
            intent.putExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_URI_BUNDLE_KEY, data);
        }
        if (mimeTypes != null) {
            intent.putExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY, mimeTypes);
        }
        if (requestId != 0) {
            intent.putExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_REQUEST_ID, requestId);
        }
        return intent;
    }

    private static Intent createReceiverIntent(Uri data, String[] mimeTypes, int requestId) {
        return createReceiverIntent(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_INSERTION_AVAILABLE, data, mimeTypes, requestId);
    }

    private Single<Uri> fakeProxy(Context context, Uri remoteUri) {
        mReceivedRemoteUri = remoteUri;
        Assert.assertSame(context, ApplicationProvider.getApplicationContext());
        return Single.just(mFakeUriResponse);
    }
}