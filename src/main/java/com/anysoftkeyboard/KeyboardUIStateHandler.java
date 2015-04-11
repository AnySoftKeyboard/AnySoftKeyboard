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
final class KeyboardUIStateHandler extends Handler {
    public static final int MSG_UPDATE_SUGGESTIONS = R.id.keyboard_ui_handler_MSG_UPDATE_SUGGESTIONS;
    public static final int MSG_RESTART_NEW_WORD_SUGGESTIONS = R.id.keyboard_ui_handler_MSG_RESTART_NEW_WORD_SUGGESTIONS;
    public static final int MSG_REMOVE_CLOSE_SUGGESTIONS_HINT = R.id.keyboard_ui_handler_MSG_REMOVE_CLOSE_SUGGESTIONS_HINT;

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

    public KeyboardUIStateHandler(AnySoftKeyboard keyboard) {
        mKeyboard = new WeakReference<>(keyboard);
    }

    @Override
    public void handleMessage(Message msg) {
        AnySoftKeyboard ask = mKeyboard.get();
        if (ask == null)// delayed posts and such may result in the reference gone
            return;
        final InputConnection ic = ask.getCurrentInputConnection();

        switch (msg.what) {
            case MSG_UPDATE_SUGGESTIONS:
                ask.performUpdateSuggestions();
                break;
            case MSG_RESTART_NEW_WORD_SUGGESTIONS:
                ask.performRestartWordSuggestion(ic);
                break;
            case MSG_REMOVE_CLOSE_SUGGESTIONS_HINT:
                final View closeText = ask.mCandidateCloseText;
                if (closeText != null) {// in API3, this variable is null
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
