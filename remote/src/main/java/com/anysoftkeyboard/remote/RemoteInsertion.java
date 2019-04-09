package com.anysoftkeyboard.remote;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.Set;

public interface RemoteInsertion {
    void startMediaRequest(Set<MediaType> supportedMediaTypes, InputConnection inputConnection, EditorInfo editorInfo, int requestId, InsertionRequestCallback callback);
}
