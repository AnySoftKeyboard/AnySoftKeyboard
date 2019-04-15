package com.anysoftkeyboard.remote;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v13.view.inputmethod.InputContentInfoCompat;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.MediaInsertion;

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

        final Intent mediaInsertionIntent = Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext()).getNextStartedActivity();

        Assert.assertEquals(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_ACTION, mediaInsertionIntent.getAction());
        Assert.assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, mediaInsertionIntent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK);
        Assert.assertEquals(Intent.FLAG_ACTIVITY_NO_HISTORY, mediaInsertionIntent.getFlags() & Intent.FLAG_ACTIVITY_NO_HISTORY);
        Assert.assertEquals(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS, mediaInsertionIntent.getFlags() & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

        Assert.assertArrayEquals(new String[]{"media/png"}, mediaInsertionIntent.getStringArrayExtra(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_MIMES_KEY));
        Assert.assertEquals(123, mediaInsertionIntent.getIntExtra(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_REQUEST_ID_KEY, 0));

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
            intent.putExtra(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_MEDIA_URI_KEY, data);
        }
        if (mimeTypes != null) {
            intent.putExtra(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_MEDIA_MIMES_KEY, mimeTypes);
        }
        if (requestId != 0) {
            intent.putExtra(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_REQUEST_ID_KEY, requestId);
        }
        return intent;
    }

    private static Intent createReceiverIntent(Uri data, String[] mimeTypes, int requestId) {
        return createReceiverIntent(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_AVAILABLE_ACTION, data, mimeTypes, requestId);
    }

    private Single<Uri> fakeProxy(Context context, Uri remoteUri) {
        mReceivedRemoteUri = remoteUri;
        Assert.assertSame(context, ApplicationProvider.getApplicationContext());
        return Single.just(mFakeUriResponse);
    }
}