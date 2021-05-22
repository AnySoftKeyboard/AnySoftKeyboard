package com.anysoftkeyboard.ui.tutorials;

import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ChangeLogFragmentShowAllLogsTest
        extends RobolectricFragmentTestCase<ChangeLogFragment.FullChangeLogFragment> {
    @NonNull
    @Override
    protected ChangeLogFragment.FullChangeLogFragment createFragment() {
        return new ChangeLogFragment.FullChangeLogFragment();
    }

    @Override
    @Ignore("hangs with OOM. Maybe the next Robolectric will be better")
    public void testEnsureLandscapeFragmentHandlesHappyPathLifecycle() {}

    @Override
    @Ignore("hangs with OOM. Maybe the next Robolectric will be better")
    public void testEnsureFragmentHandlesHappyPathLifecycleWithResume() {}

    @Override
    @Ignore("hangs with OOM. Maybe the next Robolectric will be better")
    public void testEnsureFragmentHandlesRecreateWithInstanceState() {}

    @Override
    @Ignore("hangs with OOM. Maybe the next Robolectric will be better")
    public void testEnsurePortraitFragmentHandlesHappyPathLifecycle() {}

    @Test
    public void testRootViewHasAllLogs() {
        RecyclerView rootView = startFragment().getView().findViewById(R.id.change_logs_container);

        final RecyclerView.Adapter adapter = rootView.getAdapter();
        final List<VersionChangeLogs.VersionChangeLog> changeLogItems =
                VersionChangeLogs.createChangeLog();
        Assert.assertEquals(changeLogItems.size(), adapter.getItemCount());

        final ChangeLogFragment.ChangeLogViewHolder viewHolder =
                (ChangeLogFragment.ChangeLogViewHolder) adapter.createViewHolder(rootView, 0);
        Assert.assertNotNull(viewHolder.titleView);
        Assert.assertEquals(
                Paint.UNDERLINE_TEXT_FLAG,
                viewHolder.titleView.getPaintFlags() & Paint.UNDERLINE_TEXT_FLAG);
        Assert.assertNotNull(viewHolder.bulletPointsView);
        Assert.assertNotNull(viewHolder.webLinkChangeLogView);

        for (int childViewIndex = 0; childViewIndex < adapter.getItemCount(); childViewIndex++) {
            final VersionChangeLogs.VersionChangeLog changeLogItem =
                    changeLogItems.get(childViewIndex);
            adapter.bindViewHolder(viewHolder, childViewIndex);

            Assert.assertTrue(
                    viewHolder.titleView.getText().toString().contains(changeLogItem.versionName));
            Assert.assertFalse(viewHolder.bulletPointsView.getText().toString().isEmpty());
            Assert.assertTrue(
                    viewHolder
                            .webLinkChangeLogView
                            .getText()
                            .toString()
                            .contains(changeLogItem.changesWebUrl.toString()));
        }
    }
}
