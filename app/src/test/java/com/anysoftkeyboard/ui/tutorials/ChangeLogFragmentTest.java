package com.anysoftkeyboard.ui.tutorials;

import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ChangeLogFragmentTest {

    @Test
    public void testCreate() {
        ChangeLogFragment fragmentWithFalse = ChangeLogFragment.createFragment(false);
        Assert.assertNotNull(fragmentWithFalse);
        Assert.assertEquals(1, fragmentWithFalse.getArguments().keySet().size());

        ChangeLogFragment fragmentWithTrue = ChangeLogFragment.createFragment(true);
        Assert.assertNotNull(fragmentWithTrue);
        Assert.assertEquals(1, fragmentWithTrue.getArguments().keySet().size());
    }

    public static class ChangeLogFragmentShowAllLogsTest extends RobolectricFragmentTestCase<ChangeLogFragment> {
        @NonNull
        @Override
        protected ChangeLogFragment createFragment() {
            return ChangeLogFragment.createFragment(false);
        }

        @Test
        public void testRootViewHasAllLogs() {
            LinearLayout rootView = startFragment().getView().findViewById(R.id.change_logs_container);

            int headersFound = 0;
            int changeLogItems = 0;
            int linksFound = 0;
            for (int childViewIndex = 0; childViewIndex < rootView.getChildCount(); childViewIndex++) {
                final int id = rootView.getChildAt(childViewIndex).getId();
                if (id == R.id.changelog_version_title) {
                    headersFound++;
                } else if (id == R.id.chang_log_item) {
                    changeLogItems++;
                } else if (id == R.id.change_log__web_link_item) {
                    linksFound++;
                }
            }

            List<VersionChangeLogs.VersionChangeLog> changeLogList = VersionChangeLogs.createChangeLog();
            Assert.assertEquals(changeLogList.size(), headersFound);

            for (VersionChangeLogs.VersionChangeLog changeLog : changeLogList) {
                changeLogItems -= changeLog.changes.length;
            }

            Assert.assertEquals(0, changeLogItems);

            Assert.assertEquals(changeLogList.size(), linksFound);
        }

        @Test
        public void testChangeLogHasLinkToOpenWebChangeLog() {
            List<VersionChangeLogs.VersionChangeLog> changeLogList = VersionChangeLogs.createChangeLog();

            LinearLayout rootView = startFragment().getView().findViewById(R.id.change_logs_container);
            TextView link = rootView.findViewById(R.id.change_log__web_link_item);
            Assert.assertNotNull(link);
            SpannableString spannableStringBuilder = (SpannableString) link.getText();
            URLSpan[] spans = spannableStringBuilder.getSpans(0, link.getText().length(), URLSpan.class);
            Assert.assertNotNull(spans);
            Assert.assertEquals(1, spans.length);
            URLSpan linkClickSpan = spans[0];
            Assert.assertNotNull(linkClickSpan);

            Assert.assertEquals(changeLogList.get(0).changesWebUrl.toString(), linkClickSpan.getURL());
            Assert.assertTrue(link.getText().toString().startsWith("More: "));
        }
    }

    public static class ChangeLogFragmentShowLatestTest extends RobolectricFragmentTestCase<ChangeLogFragment> {
        @NonNull
        @Override
        protected ChangeLogFragment createFragment() {
            return ChangeLogFragment.createFragment(true);
        }

        @Test
        public void testRootViewHasLatestLog() {
            LinearLayout rootView = startFragment().getView().findViewById(R.id.change_logs_container);

            int headersFound = 0;
            int changeLogItems = 0;
            for (int childViewIndex = 0; childViewIndex < rootView.getChildCount(); childViewIndex++) {
                final int id = rootView.getChildAt(childViewIndex).getId();
                if (id == R.id.changelog_version_title) {
                    headersFound++;
                } else if (id == R.id.chang_log_item) {
                    changeLogItems++;
                }
            }

            Assert.assertEquals(1, headersFound);

            Assert.assertEquals(VersionChangeLogs.createChangeLog().get(0).changes.length, changeLogItems);
        }

        @Test
        public void testChangeLogDoesNotHaveLinkToOpenWebChangeLog() {
            LinearLayout rootView = startFragment().getView().findViewById(R.id.change_logs_container);
            Assert.assertNull(rootView.findViewById(R.id.change_log__web_link_item));
        }
    }
}