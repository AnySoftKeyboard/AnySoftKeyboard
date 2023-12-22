package com.anysoftkeyboard.releaseinfo;

import android.graphics.Paint;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ChangeLogFragmentShowAllLogsTest {

  @Test
  public void testRootViewHasAllLogs() {
    try (var scenario =
        FragmentScenario.launchInContainer(ChangeLogFragment.FullChangeLogFragment.class)) {
      scenario.moveToState(Lifecycle.State.RESUMED);
      scenario.onFragment(
          fragment -> {
            final RecyclerView rootView =
                fragment.getView().findViewById(R.id.change_logs_container);
            final var adapter = rootView.getAdapter();
            Assert.assertNotNull(adapter);
            final var changeLogItems = VersionChangeLogs.createChangeLog();
            Assert.assertEquals(changeLogItems.size(), adapter.getItemCount());

            final var viewHolder =
                (ChangeLogFragment.ChangeLogViewHolder) adapter.createViewHolder(rootView, 0);
            Assert.assertNotNull(viewHolder.titleView);
            Assert.assertEquals(
                Paint.UNDERLINE_TEXT_FLAG,
                viewHolder.titleView.getPaintFlags() & Paint.UNDERLINE_TEXT_FLAG);
            Assert.assertNotNull(viewHolder.bulletPointsView);
            Assert.assertNotNull(viewHolder.webLinkChangeLogView);

            for (int childViewIndex = 0;
                childViewIndex < adapter.getItemCount();
                childViewIndex++) {
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
          });
    }
  }
}
