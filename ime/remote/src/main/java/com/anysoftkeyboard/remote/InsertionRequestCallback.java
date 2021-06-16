package com.anysoftkeyboard.remote;

import androidx.core.view.inputmethod.InputContentInfoCompat;

public interface InsertionRequestCallback {
    void onMediaRequestDone(int requestId, InputContentInfoCompat contentInputInfo);

    void onMediaRequestCancelled(int requestId);
}
