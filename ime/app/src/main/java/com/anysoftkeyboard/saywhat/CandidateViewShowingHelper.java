package com.anysoftkeyboard.saywhat;

import android.view.View;
import androidx.annotation.NonNull;

public class CandidateViewShowingHelper {

  public boolean shouldShow(@NonNull PublicNotices ime) {
    final View candidate = ime.getInputViewContainer().getCandidateView();
    return candidate != null && candidate.getVisibility() == View.VISIBLE;
  }
}
