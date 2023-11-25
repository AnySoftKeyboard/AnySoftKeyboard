package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface OnUiPage extends PublicNotice {

  /**
   * Returns the view to show in UI when showing the Public Notices UI in the settings app.
   *
   * @return may return null if do not need to show anything.
   */
  @Nullable View inflateContentView(@NonNull Context context);
}
