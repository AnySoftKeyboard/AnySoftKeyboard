/*
 * Copyright (c) 2013 Menny Even-Danan
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

package com.anysoftkeyboard.ui.tutorials;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anysoftkeyboard.ui.settings.MainFragment;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;

import java.util.List;

public abstract class ChangeLogFragment extends Fragment {

    private final List<VersionChangeLogs.VersionChangeLog> mChangeLog;
    private final StringBuilder mBulletsBuilder = new StringBuilder();

    public ChangeLogFragment() {
        mChangeLog = VersionChangeLogs.createChangeLog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getMainLayout(), container, false);
    }

    protected abstract int getMainLayout();

    protected void fillViewForLogItem(int index, ChangeLogViewHolder holder) {
        final VersionChangeLogs.VersionChangeLog change = mChangeLog.get(index);

        setTitleText(holder.titleView, change.versionName);

        mBulletsBuilder.setLength(0);
        for (String changeEntry : change.changes) {
            mBulletsBuilder
                    .append(getString(R.string.change_log_bullet_point, changeEntry))
                    .append('\n');
        }
        holder.bulletPointsView.setText(mBulletsBuilder.toString());

        holder.webLinkChangeLogView.setText(getString(R.string.change_log_url, change.changesWebUrl.toString()));
    }

    protected abstract void setTitleText(TextView titleView, String versionName);


    public static class FullChangeLogFragment extends ChangeLogFragment {
        @Override
        public void onStart() {
            super.onStart();
            MainSettingsActivity.setActivityTitle(this, getString(R.string.changelog));
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            RecyclerView recyclerView = view.findViewById(R.id.change_logs_container);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new ChangeLogsAdapter(VersionChangeLogs.createChangeLog()));
            recyclerView.setHasFixedSize(false);
        }

        private class ChangeLogsAdapter extends RecyclerView.Adapter<ChangeLogViewHolder> {
            private final List<VersionChangeLogs.VersionChangeLog> mChangeLog;

            ChangeLogsAdapter(List<VersionChangeLogs.VersionChangeLog> changeLog) {
                mChangeLog = changeLog;
                setHasStableIds(true);
            }

            @Override
            public ChangeLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ChangeLogViewHolder(getLayoutInflater().inflate(R.layout.changelogentry_item, parent, false));
            }

            @Override
            public void onBindViewHolder(ChangeLogViewHolder holder, int position) {
                fillViewForLogItem(position, holder);
            }

            @Override
            public long getItemId(int position) {
                return mChangeLog.get(position).hashCode();
            }

            @Override
            public int getItemCount() {
                return mChangeLog.size();
            }
        }

        @Override
        protected int getMainLayout() {
            return R.layout.changelog;
        }

        @Override
        protected void setTitleText(TextView titleView, String versionName) {
            titleView.setText(getString(R.string.change_log_entry_header_template_without_name, versionName));
        }
    }

    public static class LatestChangeLogFragment extends ChangeLogFragment {

        @Override
        protected int getMainLayout() {
            return R.layout.card_with_more_container;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ViewGroup container = view.findViewById(R.id.card_with_read_more);
            MainFragment.setupLink(container, R.id.read_more_link, new ClickableSpan() {
                @Override
                public void onClick(View v) {
                    FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
                    if (activity == null) return;
                    activity.addFragmentToUi(new FullChangeLogFragment(),
                            TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                }
            }, true);

            final ChangeLogViewHolder changeLogViewHolder = new ChangeLogViewHolder(getLayoutInflater().inflate(R.layout.changelogentry_item, container, false));
            fillViewForLogItem(0, changeLogViewHolder);
            changeLogViewHolder.webLinkChangeLogView.setVisibility(View.GONE);

            container.addView(changeLogViewHolder.itemView, 0);
        }

        @Override
        protected void setTitleText(TextView titleView, String versionName) {
            titleView.setText(getString(R.string.change_log_card_version_title_template, versionName));
        }
    }

    @VisibleForTesting
    static class ChangeLogViewHolder extends RecyclerView.ViewHolder {

        public final TextView titleView;
        public final TextView bulletPointsView;
        public final TextView webLinkChangeLogView;

        ChangeLogViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.changelog_version_title);
            bulletPointsView = itemView.findViewById(R.id.chang_log_item);
            webLinkChangeLogView = itemView.findViewById(R.id.change_log__web_link_item);

            titleView.setPaintFlags(titleView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }
}
