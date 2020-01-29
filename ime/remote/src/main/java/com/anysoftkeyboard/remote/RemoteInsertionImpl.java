package com.anysoftkeyboard.remote;

import android.content.BroadcastReceiver;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import com.anysoftkeyboard.api.MediaInsertion;
import com.anysoftkeyboard.fileprovider.LocalProxy;
import com.anysoftkeyboard.rx.GenericOnError;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

@RequiresApi(Build.VERSION_CODES.HONEYCOMB_MR2)
public class RemoteInsertionImpl implements RemoteInsertion {
    private final Context mContext;
    private final BroadcastReceiver mMediaInsertionAvailableReceiver;
    private final LocalProxyFunction mLocalProxy;

    private Disposable mCurrentRunningLocalProxy = Disposables.empty();
    private Integer mCurrentRequest;
    private InsertionRequestCallback mCurrentCallback;

    public RemoteInsertionImpl(Context context) {
        this(context, LocalProxy::proxy);
    }

    @VisibleForTesting
    RemoteInsertionImpl(Context context, LocalProxyFunction localProxy) {
        mLocalProxy = localProxy;
        mContext = context;
        mMediaInsertionAvailableReceiver = new MediaInsertionAvailableReceiver(this);
        mContext.registerReceiver(
                mMediaInsertionAvailableReceiver,
                MediaInsertionAvailableReceiver.createIntentFilter());
    }

    @Override
    public void startMediaRequest(
            @NonNull String[] mimeTypes,
            int requestId,
            @NonNull InsertionRequestCallback callback) {
        mCurrentRunningLocalProxy.dispose();

        mCurrentRequest = requestId;
        mCurrentCallback = callback;

        final Intent pickingIntent = getMediaInsertRequestIntent(mimeTypes, requestId);

        mContext.startActivity(pickingIntent);
    }

    @NonNull
    @VisibleForTesting
    static Intent getMediaInsertRequestIntent(@NonNull String[] mimeTypes, int requestId) {
        final Intent pickingIntent =
                new Intent(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_ACTION);
        pickingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // pickingIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        pickingIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        pickingIntent.putExtra(
                MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_REQUEST_ID_KEY, requestId);
        pickingIntent.putExtra(
                MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_MIMES_KEY, mimeTypes);
        return pickingIntent;
    }

    @Override
    public void destroy() {
        mCurrentRunningLocalProxy.dispose();
        mContext.unregisterReceiver(mMediaInsertionAvailableReceiver);
    }

    private void onReply(int requestId, @Nullable Uri data, @NonNull String[] mimeTypes) {
        mCurrentRunningLocalProxy.dispose();

        if (mCurrentRequest == null) return;

        if (mCurrentRequest == requestId) {
            if (data == null) {
                mCurrentCallback.onMediaRequestCancelled(mCurrentRequest);
            } else {
                mCurrentRunningLocalProxy =
                        mLocalProxy
                                .proxy(mContext, data)
                                .subscribe(
                                        localUri ->
                                                mCurrentCallback.onMediaRequestDone(
                                                        requestId,
                                                        new InputContentInfoCompat(
                                                                localUri,
                                                                new ClipDescription(
                                                                        "media", mimeTypes),
                                                                null)),
                                        GenericOnError.onError(
                                                "mCurrentCallback.onMediaRequestDone"));
            }
        }

        mCurrentRequest = null;
    }

    static class MediaInsertionAvailableReceiver extends BroadcastReceiver {

        public static IntentFilter createIntentFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            filter.addAction(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_AVAILABLE_ACTION);

            return filter;
        }

        private final RemoteInsertionImpl mRemoteInsertion;

        public MediaInsertionAvailableReceiver(RemoteInsertionImpl remoteInsertion) {
            mRemoteInsertion = remoteInsertion;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mRemoteInsertion.onReply(
                    intent.getIntExtra(
                            MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_REQUEST_ID_KEY, 0),
                    intent.getParcelableExtra(
                            MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_MEDIA_URI_KEY),
                    intent.getStringArrayExtra(
                            MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_MEDIA_MIMES_KEY));
        }
    }

    @VisibleForTesting
    interface LocalProxyFunction {
        Single<Uri> proxy(Context context, Uri remoteUri);
    }
}
