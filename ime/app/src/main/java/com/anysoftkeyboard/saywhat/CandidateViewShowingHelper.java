package com.anysoftkeyboard.saywhat;

import android.support.annotation.NonNull;
import android.view.View;

public class CandidateViewShowingHelper {

    public boolean shouldShow(@NonNull PublicNotices ime) {
        final View candidate = ime.getInputViewContainer().getCandidateView();
        return candidate != null && candidate.getVisibility() == View.VISIBLE;
    }
}
