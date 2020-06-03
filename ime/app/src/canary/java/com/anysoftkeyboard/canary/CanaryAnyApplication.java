/*
 * Copyright (c) 2016 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.canary;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.saywhat.OnVisible;
import com.anysoftkeyboard.saywhat.PublicNotice;
import com.anysoftkeyboard.saywhat.PublicNotices;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.List;

public class CanaryAnyApplication extends AnyApplication {

    @Override
    public List<Drawable> getInitialWatermarksList() {
        List<Drawable> watermarks = super.getInitialWatermarksList();
        watermarks.add(ContextCompat.getDrawable(this, R.drawable.ic_watermark_beta_build));

        return watermarks;
    }

    @Override
    public List<PublicNotice> createAppPublicNotices() {
        final List<PublicNotice> notices = super.createAppPublicNotices();
        notices.add(new BetaChannelPrePurgeNotice());
        notices.add(new BetaChannelPostPurgeNotice());
        return notices;
    }

    private abstract static class BetaChannelPurgeNoticeBase implements OnVisible {
        private final KeyboardViewContainerView.StripActionProvider mBetaPurgeInfo;
        // 1 day
        static final long MIN_TIME_BETWEEN_SHOWING = 24 * 60 * 60 * 1000;
        private long mLastTimeInfoWasShown = -MIN_TIME_BETWEEN_SHOWING;

        BetaChannelPurgeNoticeBase(KeyboardViewContainerView.StripActionProvider betaPurgeInfo) {
            mBetaPurgeInfo = betaPurgeInfo;
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
            mLastTimeInfoWasShown = SystemClock.elapsedRealtime();
            ime.getInputViewContainer().addStripAction(mBetaPurgeInfo);
        }

        protected boolean shouldShowNotice(PublicNotices ime) {
            final CandidateView candidateView = ime.getInputViewContainer().getCandidateView();
            return MIN_TIME_BETWEEN_SHOWING
                            < (SystemClock.elapsedRealtime() - mLastTimeInfoWasShown)
                    && candidateView != null
                    && candidateView.getVisibility() == View.VISIBLE;
        }
    }

    private static class BetaChannelPrePurgeNotice extends BetaChannelPurgeNoticeBase {

        BetaChannelPrePurgeNotice() {
            super(new BetaPurgeStripAction(R.string.beta_pre_purge_action_text));
        }

        @Override
        protected boolean shouldShowNotice(PublicNotices ime) {
            return super.shouldShowNotice(ime) && isCalendarPeriod();
        }

        private boolean isCalendarPeriod() {
            final Calendar calendar = Calendar.getInstance();
            return (calendar.get(Calendar.MONTH) == Calendar.MAY
                            && calendar.get(Calendar.DAY_OF_MONTH) > 15)
                    || (calendar.get(Calendar.MONTH) == Calendar.NOVEMBER
                            && calendar.get(Calendar.DAY_OF_MONTH) > 15);
        }

        @Override
        public String getName() {
            return "beta-channel-pre-purge-notice";
        }
    }

    private static class BetaChannelPostPurgeNotice extends BetaChannelPurgeNoticeBase {

        BetaChannelPostPurgeNotice() {
            super(new BetaPurgeStripAction(R.string.beta_post_purge_action_text));
        }

        @Override
        protected boolean shouldShowNotice(PublicNotices ime) {
            return super.shouldShowNotice(ime) && isCalendarPeriod();
        }

        private boolean isCalendarPeriod() {
            final Calendar calendar = Calendar.getInstance();
            return (calendar.get(Calendar.MONTH) == Calendar.JUNE
                            && calendar.get(Calendar.DAY_OF_MONTH) < 15)
                    || (calendar.get(Calendar.MONTH) == Calendar.DECEMBER
                            && calendar.get(Calendar.DAY_OF_MONTH) < 15);
        }

        @Override
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
