package com.anysoftkeyboard;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputConnection;
import com.menny.android.anysoftkeyboard.R;

import java.lang.ref.WeakReference;

/**
 * handles all kind of UI thread related operations.
*/
final class KeyboardUIStateHanlder extends Handler {
    public static final int MSG_UPDATE_SUGGESTIONS = 0;
    public static final int MSG_RESTART_NEW_WORD_SUGGESTIONS = 1;
    public static final int MSG_UPDATE_SHIFT_STATE = 3;
    public static final int MSG_REMOVE_CLOSE_SUGGESTIONS_HINT = 4;

    private static final class CloseTextAnimationListener implements Animation.AnimationListener {
        private View closeText;

        public void setCloseText(View c) {
            closeText = c;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            closeText.setVisibility(View.GONE);
            closeText = null;
        }
    }

    private final CloseTextAnimationListener mCloseTextAnimationListener = new CloseTextAnimationListener();
    private final WeakReference<AnySoftKeyboard> mKeyboard;

    public KeyboardUIStateHanlder(AnySoftKeyboard keyboard) {
        mKeyboard = new WeakReference<AnySoftKeyboard>(keyboard);
    }

    @Override
    public void handleMessage(Message msg) {
        AnySoftKeyboard ask = mKeyboard.get();
        if (ask == null)// delayed posts and such may result in the
            // reference gone
            return;

        switch (msg.what) {
            case MSG_UPDATE_SUGGESTIONS:
                ask.performUpdateSuggestions();
                break;
            case MSG_RESTART_NEW_WORD_SUGGESTIONS:
                final InputConnection ic = ask.getCurrentInputConnection();
                ask.performRestartWordSuggestion(ic);
                break;
            // case MSG_UPDATE_OLD_SUGGESTIONS:
            // setOldSuggestions();
            // break;
            case MSG_UPDATE_SHIFT_STATE:
                ask.updateShiftKeyState(ask.getCurrentInputEditorInfo());
                break;
            case MSG_REMOVE_CLOSE_SUGGESTIONS_HINT:
                final View closeText = ask.mCandidateCloseText;
                if (closeText != null) {// in API3, this variable is
                    // null
                    mCloseTextAnimationListener.setCloseText(closeText);
                    Animation gone = AnimationUtils.loadAnimation(ask.getApplicationContext(), R.anim.close_candidates_hint_out);
                    gone.setAnimationListener(mCloseTextAnimationListener);
                    closeText.startAnimation(gone);
                }
            default:
                super.handleMessage(msg);
        }
    }
}
