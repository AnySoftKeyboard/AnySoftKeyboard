package com.anysoftkeyboard.ui.tutorials;

import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

public abstract class ChangeLogFragmentTest {

    @RunWith(AnySoftKeyboardRobolectricTestRunner.class)
    public static class ChangeLogFragmentShowAllLogsTest extends RobolectricFragmentTestCase<ChangeLogFragment> {
        @NonNull
        @Override
        protected ChangeLogFragment createFragment() {
            return new ChangeLogFragment.FullChangeLogFragment();
        }

        @Test
        public void testRootViewHasAllLogs() {
            RecyclerView rootView = startFragment().getView().findViewById(R.id.change_logs_container);

            final RecyclerView.Adapter adapter = rootView.getAdapter();
            final List<VersionChangeLogs.VersionChangeLog> changeLogItems = VersionChangeLogs.createChangeLog();
            Assert.assertEquals(changeLogItems.size(), adapter.getItemCount());

            final ChangeLogFragment.ChangeLogViewHolder viewHolder = (ChangeLogFragment.ChangeLogViewHolder) adapter.createViewHolder(rootView, 0);
            Assert.assertNotNull(viewHolder.titleView);
            Assert.assertEquals(Paint.UNDERLINE_TEXT_FLAG, viewHolder.titleView.getPaintFlags() & Paint.UNDERLINE_TEXT_FLAG);
            Assert.assertNotNull(viewHolder.bulletPointsView);
            Assert.assertNotNull(viewHolder.webLinkChangeLogView);

            for (int childViewIndex = 0; childViewIndex < adapter.getItemCount(); childViewIndex++) {
                final VersionChangeLogs.VersionChangeLog changeLogItem = changeLogItems.get(childViewIndex);
                adapter.bindViewHolder(viewHolder, childViewIndex);

                Assert.assertTrue(viewHolder.titleView.getText().toString().contains(changeLogItem.versionName));
                Assert.assertFalse(viewHolder.bulletPointsView.getText().toString().isEmpty());
                Assert.assertTrue(viewHolder.webLinkChangeLogView.getText().toString().contains(changeLogItem.changesWebUrl.toString()));
            }
        }
    }

    @RunWith(AnySoftKeyboardRobolectricTestRunner.class)
    public static class ChangeLogFragmentShowLatestTest extends RobolectricFragmentTestCase<ChangeLogFragment> {
        @NonNull
        @Override
        protected ChangeLogFragment createFragment() {
            return new ChangeLogFragment.LatestChangeLogFragment();
        }

        @Test
        public void testRootViewHasLatestLog() {
            ViewGroup rootView = startFragment().getView().findViewById(R.id.card_with_read_more);
            Assert.assertTrue(rootView.getChildAt(0) instanceof LinearLayout);
            LinearLayout container = (LinearLayout) rootView.getChildAt(0);

            int headersFound = 0;
            int changeLogItems = 0;
            int linkItems = 0;
            int visibleLinkItems = 0;
            for (int childViewIndex = 0; childViewIndex < container.getChildCount(); childViewIndex++) {
                final View childView = container.getChildAt(childViewIndex);
                final int id = childView.getId();
                if (id == R.id.changelog_version_title) {
                    headersFound++;
                } else if (id == R.id.chang_log_item) {
                    changeLogItems++;
                } else if (id == R.id.change_log__web_link_item) {
                    linkItems++;
                    if (childView.getVisibility() != View.GONE) visibleLinkItems++;
                }
            }

            Assert.assertEquals(1, headersFound);
            Assert.assertEquals(1, changeLogItems);
            Assert.assertEquals(1, linkItems);
            Assert.assertEquals(0, visibleLinkItems);
        }

        @Test
        public void testChangeLogDoesNotHaveLinkToOpenWebChangeLog() {
            LinearLayout rootView = startFragment().getView().findViewById(R.id.card_with_read_more);
            Assert.assertEquals(View.GONE, rootView.findViewById(R.id.change_log__web_link_item).getVisibility());
        }
    }
}