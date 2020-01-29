package com.anysoftkeyboard.remote;

import android.support.v13.view.inputmethod.InputContentInfoCompat;

public interface InsertionRequestCallback {
    void onMediaRequestDone(int requestId, InputContentInfoCompat contentInputInfo);

    void onMediaRequestCancelled(int requestId);
}
