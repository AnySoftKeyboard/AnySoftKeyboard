package com.anysoftkeyboard.remote;

import static com.anysoftkeyboard.remote.RemoteInsertionActivity.createPickingActivityIntent;

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
        mContext.registerReceiver(mMediaInsertionAvailableReceiver, MediaInsertionAvailableReceiver.createIntentFilter());
    }

    @Override
    public void startMediaRequest(@NonNull String[] mimeTypes, int requestId, @NonNull InsertionRequestCallback callback) {
        mCurrentRunningLocalProxy.dispose();

        mCurrentRequest = requestId;
        mCurrentCallback = callback;
        final Intent intent = createPickingActivityIntent(mimeTypes, requestId, mContext);
        mContext.startActivity(intent);
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
                mCurrentRunningLocalProxy = mLocalProxy.proxy(mContext, data)
                        .subscribe(
                                localUri -> mCurrentCallback.onMediaRequestDone(
                                        requestId,
                                        new InputContentInfoCompat(localUri, new ClipDescription("media", mimeTypes), null)),
                                GenericOnError.onError("mCurrentCallback.onMediaRequestDone"));
            }
        }

        mCurrentRequest = null;
    }

    static class MediaInsertionAvailableReceiver extends BroadcastReceiver {

        public static String MEDIA_INSERTION_AVAILABLE = "com.anysoftkeyboard.remote.MEDIA_INSERTION_AVAILABLE";
        public static String MEDIA_URI_BUNDLE_KEY = "com.anysoftkeyboard.remote.MEDIA_URI_BUNDLE_KEY";
        public static String MEDIA_MIMES_BUNDLE_KEY = "com.anysoftkeyboard.remote.MEDIA_MIMES_BUNDLE_KEY";
        public static String MEDIA_REQUEST_ID = "com.anysoftkeyboard.remote.MEDIA_REQUEST_ID";

        public static IntentFilter createIntentFilter() {
            IntentFilter filter = new IntentFilter();
            filter.addCategory(Intent.CATEGORY_DEFAULT);

            filter.addAction(MEDIA_INSERTION_AVAILABLE);

            return filter;
        }

        private final RemoteInsertionImpl mRemoteInsertion;

        public MediaInsertionAvailableReceiver(RemoteInsertionImpl remoteInsertion) {
            mRemoteInsertion = remoteInsertion;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            mRemoteInsertion.onReply(intent.getIntExtra(MEDIA_REQUEST_ID, 0),
                    intent.getParcelableExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_URI_BUNDLE_KEY),
                    intent.getStringArrayExtra(MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY));
        }
    }

    @VisibleForTesting
    interface LocalProxyFunction {
        Single<Uri> proxy(Context context, Uri remoteUri);
    }
}
