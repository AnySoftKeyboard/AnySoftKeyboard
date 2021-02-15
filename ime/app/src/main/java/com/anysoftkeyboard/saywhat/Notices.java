package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
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

        private final OnKeyWordHelper mTypedWordHelper;
        private final TimedNoticeHelper mTimeHelper;
        private final CandidateViewShowingHelper mCandidateVisibleHelper =
                new CandidateViewShowingHelper();
        private final KeyboardViewContainerView.StripActionProvider mVirusInfo;

        private CoronaVirusDetails(Context context) {
            mVirusInfo = new CovidInfo(context);
            mTypedWordHelper = new OnKeyWordHelper("coronavirus");
            mTimeHelper =
                    new TimedNoticeHelper(
                            context,
                            R.string.settings_key_public_notice_timed_covid,
                            // 7 days
                            7 * 24 * 60 * 60 * 1000);
        }

        @Override
        public void onKey(PublicNotices ime, int primaryCode, Keyboard.Key key) {
            if (mTypedWordHelper.shouldShow(primaryCode)) {
                showInfo(ime);
            }
        }

        private void showInfo(PublicNotices ime) {
            if (mCandidateVisibleHelper.shouldShow(ime)) {
                mTimeHelper.markAsShown();
                ime.getInputViewContainer().addStripAction(mVirusInfo);
            }
        }

        @Override
        public void onVisible(PublicNotices ime, AnyKeyboard keyboard, EditorInfo editorInfo) {
            if (mTimeHelper.shouldShow()) {
                showInfo(ime);
            }
        }

        @Override
        public void onHidden(PublicNotices ime, AnyKeyboard keyboard) {
            ime.getInputViewContainer().removeStripAction(mVirusInfo);
        }

        @Override
        @NonNull
        public String getName() {
            return "covid-19";
        }

        private static class CovidInfo implements KeyboardViewContainerView.StripActionProvider {

            private final Intent mCoronaVirusInfoWebPage;
            private View mRootView;
            private final Runnable mHideTextAction =
                    () -> mRootView.findViewById(R.id.covid_info_text).setVisibility(View.GONE);

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
