package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import java.util.Arrays;
import java.util.List;

public class Notices {
    public static List<PublicNotice> create(Context context) {
        return Arrays.asList(
                new CoronaVirusDetails(context),
                new BetaChannelPrePurgeNotice(context),
                new BetaChannelPostPurgeNotice(context));
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

    private abstract static class BetaChannelPurgeNoticeBase implements OnVisible {
        private final KeyboardViewContainerView.StripActionProvider mBetaPurgeInfo;
        private final TimedNoticeHelper mTimeHelper;
        private final CandidateViewShowingHelper mCandidateViewHelper;

        BetaChannelPurgeNoticeBase(
                Context context,
                @StringRes int prefKey,
                KeyboardViewContainerView.StripActionProvider betaPurgeInfo,
                TimedNoticeHelper.NextTimeProvider nextTimeProvider) {
            mBetaPurgeInfo = betaPurgeInfo;
            mCandidateViewHelper = new CandidateViewShowingHelper();
            mTimeHelper = new TimedNoticeHelper(context, prefKey, nextTimeProvider);
        }

        @Override
        public void onVisible(PublicNotices ime, AnyKeyboard keyboard, EditorInfo editorInfo) {
            if (shouldShowNotice(ime)) {
                showInfo(ime);
            }
        }

        @Override
        public void onHidden(PublicNotices ime, AnyKeyboard keyboard) {
            ime.getInputViewContainer().removeStripAction(mBetaPurgeInfo);
        }

        private void showInfo(PublicNotices ime) {
            mTimeHelper.markAsShown();
            ime.getInputViewContainer().addStripAction(mBetaPurgeInfo);
        }

        protected boolean shouldShowNotice(PublicNotices ime) {
            return BuildConfig.TESTING_BUILD
                    && mCandidateViewHelper.shouldShow(ime)
                    && mTimeHelper.shouldShow();
        }
    }

    private static class BetaChannelPrePurgeNotice extends BetaChannelPurgeNoticeBase {

        private static final int MAY_START = 136;
        private static final int MAY_END = 152;
        private static final int NOVEMBER_START = 320;
        private static final int NOVEMBER_END = 335;

        BetaChannelPrePurgeNotice(@NonNull Context context) {
            super(
                    context,
                    R.string.settings_key_public_notice_pre_purge_beta_channel,
                    new BetaPurgeStripAction(R.string.beta_pre_purge_action_text),
                    new PeriodsTimeProvider(MAY_START, MAY_END, NOVEMBER_START, NOVEMBER_END));
        }

        @Override
        @NonNull
        public String getName() {
            return "beta-channel-pre-purge-notice";
        }
    }

    private static class BetaChannelPostPurgeNotice extends BetaChannelPurgeNoticeBase {

        private static final int JUNE_START = BetaChannelPrePurgeNotice.MAY_END + 1;
        private static final int JUNE_END = JUNE_START + 15;
        private static final int DECEMBER_START = BetaChannelPrePurgeNotice.NOVEMBER_END + 1;
        private static final int DECEMBER_END = DECEMBER_START + 15;

        BetaChannelPostPurgeNotice(Context context) {
            super(
                    context,
                    R.string.settings_key_public_notice_post_purge_beta_channel,
                    new BetaPurgeStripAction(R.string.beta_post_purge_action_text),
                    new PeriodsTimeProvider(JUNE_START, JUNE_END, DECEMBER_START, DECEMBER_END));
        }

        @Override
        @NonNull
        public String getName() {
            return "beta-channel-post-purge-notice";
        }
    }

    private static class BetaPurgeStripAction
            implements KeyboardViewContainerView.StripActionProvider {

        @StringRes private final int mActionText;

        private BetaPurgeStripAction(int actionText) {
            mActionText = actionText;
        }

        @Override
        public View inflateActionView(ViewGroup parent) {
            final View rootView =
                    LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.beta_purge_action, parent, false);
            TextView message = rootView.findViewById(R.id.purge_message);
            message.setText(mActionText);
            rootView.setOnClickListener(
                    view -> {
                        String betaInfoWebPage =
                                view.getContext().getString(R.string.beta_purge_web_page_url);
                        final Intent intent =
                                new Intent(Intent.ACTION_VIEW, Uri.parse(betaInfoWebPage));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        view.getContext().startActivity(intent);
                        rootView.setVisibility(View.GONE);
                    });

            return rootView;
        }

        @Override
        public void onRemoved() {}
    }
}
