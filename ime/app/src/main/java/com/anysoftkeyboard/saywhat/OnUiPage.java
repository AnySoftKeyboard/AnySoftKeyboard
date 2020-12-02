package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

public interface OnUiPage extends PublicNotice {

    /**
     * Returns the view to show in UI when showing the Public Notices UI in the settings app.
     *
     * @return may return null if do not need to show anything.
     */
    @Nullable
    View inflateContentView(@NonNull Context context);
}
