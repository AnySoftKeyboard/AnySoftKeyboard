package com.anysoftkeyboard.remote;

import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.Set;

public class RemoteInsertionImpl implements RemoteInsertion {
    private final Context mContext;

    public RemoteInsertionImpl(Context context) {
        mContext = context;
    }

    @Override
    public void startMediaRequest(Set<MediaType> supportedMediaTypes, InputConnection inputConnection, EditorInfo editorInfo, int requestId, InsertionRequestCallback callback) {

    }
}
