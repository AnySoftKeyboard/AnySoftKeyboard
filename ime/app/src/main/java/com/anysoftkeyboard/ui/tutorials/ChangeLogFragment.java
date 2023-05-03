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

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.anysoftkeyboard.ui.settings.MainFragment;
import com.anysoftkeyboard.ui.settings.MainFragmentDirections;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;

public abstract class ChangeLogFragment extends Fragment {

    protected ChangeLogFragment() {}

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(getMainLayout(), container, false);
    }

    protected abstract int getMainLayout();

    protected static void fillViewForLogItem(
            Context context,
            StringBuilder stringBuilder,
            VersionChangeLogs.VersionChangeLog change,
            ChangeLogViewHolder holder,
            CharSequence title) {
        holder.titleView.setText(title);

        stringBuilder.setLength(0);
        for (String changeEntry : change.changes) {
            if (stringBuilder.length() != 0) stringBuilder.append('\n');
            stringBuilder.append(context.getString(R.string.change_log_bullet_point, changeEntry));
        }

        holder.bulletPointsView.setText(stringBuilder.toString());

        holder.webLinkChangeLogView.setText(
                context.getString(R.string.change_log_url, change.changesWebUrl.toString()));
    }

    public static class FullChangeLogFragment extends ChangeLogFragment {
        private final StringBuilder mBulletsBuilder = new StringBuilder();

        @Override
        public void onStart() {
            super.onStart();
            MainSettingsActivity.setActivityTitle(this, getString(R.string.changelog));
        }

        @Override
        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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

            @NonNull @Override
            public ChangeLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ChangeLogViewHolder(
                        getLayoutInflater().inflate(R.layout.changelogentry_item, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull ChangeLogViewHolder holder, int position) {
                final VersionChangeLogs.VersionChangeLog change = mChangeLog.get(position);
                fillViewForLogItem(
                        requireContext(),
                        mBulletsBuilder,
                        change,
                        holder,
                        getString(
                                R.string.change_log_entry_header_template_without_name,
                                change.versionName));
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
    }

    public static class LatestChangeLogViewFactory {

        @NonNull public static View createLatestChangeLogView(
                @NonNull MainFragment mainFragment, @NonNull ViewGroup changeLogViewParent) {
            final LayoutInflater layoutInflater = mainFragment.getLayoutInflater();
            final View rootView =
                    layoutInflater.inflate(
                            R.layout.card_with_more_container, changeLogViewParent, false);
            ViewGroup container = rootView.findViewById(R.id.card_with_read_more);
            MainFragment.setupLink(
                    container,
                    R.id.read_more_link,
                    new ClickableSpan() {
                        @Override
                        public void onClick(View v) {
                            Navigation.findNavController(mainFragment.requireView())
                                    .navigate(
                                            MainFragmentDirections
                                                    .actionMainFragmentToFullChangeLogFragment());
                        }
                    },
                    true);

            final ChangeLogViewHolder changeLogViewHolder =
                    new ChangeLogViewHolder(
                            layoutInflater.inflate(R.layout.changelogentry_item, container, false));
            final VersionChangeLogs.VersionChangeLog change =
                    VersionChangeLogs.createChangeLog().get(0);
            final Context context = mainFragment.requireContext();
            fillViewForLogItem(
                    context,
                    new StringBuilder(),
                    change,
                    changeLogViewHolder,
                    context.getString(
                            R.string.change_log_card_version_title_template, change.versionName));
            changeLogViewHolder.webLinkChangeLogView.setVisibility(View.GONE);

            container.addView(changeLogViewHolder.itemView, 0);

            return rootView;
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
