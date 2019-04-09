package com.anysoftkeyboard.ime;

import android.content.ClipDescription;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.remote.InsertionRequestCallback;
import com.anysoftkeyboard.remote.MediaType;
import com.anysoftkeyboard.remote.RemoteInsertion;
import com.anysoftkeyboard.remote.RemoteInsertionImpl;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AnySoftKeyboardMediaInsertion extends AnySoftKeyboardHardware {

    private final Set<MediaType> mSupportedMediaTypes = new HashSet<>();
    private final Set<MediaType> mSupportedMediaTypesUnmodifiable = Collections.unmodifiableSet(mSupportedMediaTypes);

    private final InsertionRequestCallback mInsertionRequestCallback = new AskInsertionRequestCallback();
    private RemoteInsertion mKeyboardRemoteInsertion;

    @Override
    public void onCreate() {
        super.onCreate();
        mKeyboardRemoteInsertion = createRemoteInsertion();
    }

    protected RemoteInsertion createRemoteInsertion() {
        return new RemoteInsertionImpl(this);
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);

        mSupportedMediaTypes.clear();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            final String[] mimeTypes = EditorInfoCompat.getContentMimeTypes(info);

            for (String mimeType : mimeTypes) {
                if (ClipDescription.compareMimeTypes(mimeType, "image/*")) {
                    mSupportedMediaTypes.add(MediaType.Image);
                }
                if (ClipDescription.compareMimeTypes(mimeType, "image/gif")) {
                    mSupportedMediaTypes.add(MediaType.Gif);
                }
            }
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        mSupportedMediaTypes.clear();
    }

    protected void handleMediaInsertionKey() {
        final InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            mKeyboardRemoteInsertion.startMediaRequest(mSupportedMediaTypesUnmodifiable, inputConnection, getCurrentInputEditorInfo(), getIdForInsertionRequest(getCurrentInputEditorInfo()),
                    mInsertionRequestCallback);
        }
    }

    @VisibleForTesting
    static int getIdForInsertionRequest(EditorInfo info) {
        return Arrays.hashCode(new int[]{info.fieldId, info.packageName.hashCode(), info.imeOptions, info.inputType});
    }

    protected Set<MediaType> getSupportedMediaTypesForInput() {
        return mSupportedMediaTypesUnmodifiable;
    }

    @RequiresApi(android.os.Build.VERSION_CODES.HONEYCOMB_MR2)
    private void commitMedia(InputContentInfoCompat inputContentInfo) {
        InputConnection inputConnection = getCurrentInputConnection();
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        int flags = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            flags |= InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        InputConnectionCompat.commitContent(inputConnection, editorInfo, inputContentInfo, flags, null);
    }

    private class AskInsertionRequestCallback implements InsertionRequestCallback {
        @Override
        public void onMediaRequestDone(int requestId, InputContentInfoCompat contentInputInfo) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB_MR2) return;

            if (requestId == getIdForInsertionRequest(getCurrentInputEditorInfo())) {
                Logger.i("AskInsertionRequestCallback", "Received media insertion for ID %d with URI %s", requestId, contentInputInfo.getContentUri());
                commitMedia(contentInputInfo);
            } else {
                Logger.w("AskInsertionRequestCallback", "Received media insertion for ID %d, but currently at editor-info %d", requestId, getIdForInsertionRequest(getCurrentInputEditorInfo()));
            }
        }

        @Override
        public void onMediaRequestCancelled(int requestId) {

        }
    }
}
