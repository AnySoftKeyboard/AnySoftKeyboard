package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface OnUiPage extends PublicNotice {

  /**
   * Returns the view to show in UI when showing the Public Notices UI in the settings app.
   *
   * @return may return null if do not need to show anything.
   */
  @Nullable
  View inflateContentView(@NonNull Context context);

  /**
   * Returns the view to show in UI when showing the Public Notices UI in the settings app. Default
   * implementation delegates to {@link #inflateContentView(Context)}.
   *
   * @param parent the parent ViewGroup that will host the notice view, supplying layout params.
   * @return may return null if do not need to show anything.
   */
  @Nullable
  default View inflateContentView(@NonNull ViewGroup parent) {
    return inflateContentView(parent.getContext());
  }
}
