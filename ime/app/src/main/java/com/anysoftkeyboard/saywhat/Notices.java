package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.menny.android.anysoftkeyboard.R;
import java.util.Collections;
import java.util.List;

public class Notices {
    public static List<PublicNotice> create(Context context) {
        return Collections.singletonList(new CoronaVirusDetails(context));
    }

    private static class CoronaVirusDetails implements OnKey, OnVisible {

        static final char[] CORONAVIRUS = "coronavirus".toCharArray();
        // two days
        static final long MIN_TIME_BETWEEN_SHOWING = 2 * 24 * 60 * 60 * 1000;

        private final KeyboardViewContainerView.StripActionProvider mVirusInfo;
        private int mWaitingForIndex = 0;
        private long mLastTimeInfoWasShown = -MIN_TIME_BETWEEN_SHOWING;

        private CoronaVirusDetails(Context context) {
            mVirusInfo = new CovidInfo(context);
        }

        @Override
        public void onKey(PublicNotices ime, int primaryCode, Keyboard.Key key) {
            if (key != null && key.getPrimaryCode() == CORONAVIRUS[mWaitingForIndex]) {
                mWaitingForIndex++;
                if (mWaitingForIndex == CORONAVIRUS.length) {
                    mWaitingForIndex = 0;
                    showInfo(ime);
                }
            } else {
                mWaitingForIndex = 0;
            }
        }

        private void showInfo(PublicNotices ime) {
            mLastTimeInfoWasShown = SystemClock.elapsedRealtime();
            ime.getInputViewContainer().addStripAction(mVirusInfo);
        }

        @Override
        public void onVisible(PublicNotices ime, AnyKeyboard keyboard, EditorInfo editorInfo) {
            if (theRightTimeToShow(ime)) {
                showInfo(ime);
            }
        }

        private boolean theRightTimeToShow(PublicNotices ime) {
            return MIN_TIME_BETWEEN_SHOWING
                            < (SystemClock.elapsedRealtime() - mLastTimeInfoWasShown)
                    &&
                    // only till June 22nd
                    System.currentTimeMillis() < 1592789573000L
                    && ime.getInputViewContainer().getCandidateView() != null
                    && ime.getInputViewContainer().getCandidateView().getVisibility()
                            == View.VISIBLE;
        }

        @Override
        public void onHidden(PublicNotices ime, AnyKeyboard keyboard) {
            ime.getInputViewContainer().removeStripAction(mVirusInfo);
        }

        @Override
        public String getName() {
            return "covid-19";
        }

        private static class CovidInfo implements KeyboardViewContainerView.StripActionProvider {

            private View mRootView;
            private final Runnable mHideTextAction =
                    () -> mRootView.findViewById(R.id.covid_info_text).setVisibility(View.GONE);
            private final Intent mCoronaVirusInfoWebPage;

            private CovidInfo(Context context) {
                mCoronaVirusInfoWebPage =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(context.getString(R.string.codvid_info_url)));
                mCoronaVirusInfoWebPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            @Override
            public View inflateActionView(ViewGroup parent) {
                final Context context = parent.getContext();
                mRootView =
                        LayoutInflater.from(context)
                                .inflate(R.layout.covid_19_info_action, parent, false);

                mRootView.setOnClickListener(
                        view -> view.getContext().startActivity(mCoronaVirusInfoWebPage));
                mRootView.postDelayed(mHideTextAction, 2000);
                return mRootView;
            }

            @Override
            public void onRemoved() {
                mRootView.removeCallbacks(mHideTextAction);
            }
        }
    }
}
