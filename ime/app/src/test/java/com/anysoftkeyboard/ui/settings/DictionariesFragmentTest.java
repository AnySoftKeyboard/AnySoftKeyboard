package com.anysoftkeyboard.ui.settings;

import android.net.Uri;
import android.os.Build;
import androidx.preference.Preference;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M)
public class DictionariesFragmentTest
    extends RobolectricFragmentTestCase<DictionariesFragment> {

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.dictionariesFragment;
  }

  @Test
  public void testOnFolderPickedNullDoesNothing() {
    final DictionariesFragment fragment = startFragment();
    ShadowToast.reset();

    fragment.onFolderPicked(null);

    TestRxSchedulers.backgroundFlushAllJobs();
    TestRxSchedulers.foregroundFlushAllJobs();

    Assert.assertEquals(0, ShadowToast.shownToastCount());
  }

  @Test
  public void testOnFolderPickedNoTablesShowsToast() {
    final DictionariesFragment fragment = startFragment();
    ShadowToast.reset();

    Uri dummyUri = Uri.parse("content://dummy.provider/tree/dummy_id");
    fragment.onFolderPicked(dummyUri);

    // Before flushing background jobs, no toast has been shown
    Assert.assertEquals(0, ShadowToast.shownToastCount());

    TestRxSchedulers.backgroundFlushAllJobs();
    TestRxSchedulers.foregroundFlushAllJobs();

    Assert.assertEquals(1, ShadowToast.shownToastCount());
    Assert.assertTrue(
        ShadowToast.getTextOfLatestToast().contains("No radical tables found in"));
  }

  @Test
  public void testResetAndRefreshOverlay() {
    final DictionariesFragment fragment = startFragment();
    ShadowToast.reset();

    Preference resetPref =
        fragment.findPreference(fragment.getString(R.string.settings_key_radical_overlay_reset));
    Assert.assertNotNull(resetPref);
    resetPref.getOnPreferenceClickListener().onPreferenceClick(resetPref);

    Assert.assertEquals(1, ShadowToast.shownToastCount());
    Assert.assertEquals(
        fragment.getString(R.string.radical_overlay_toast_reset),
        ShadowToast.getTextOfLatestToast());

    ShadowToast.reset();
    Preference refreshPref =
        fragment.findPreference(fragment.getString(R.string.settings_key_radical_overlay_refresh));
    Assert.assertNotNull(refreshPref);
    refreshPref.getOnPreferenceClickListener().onPreferenceClick(refreshPref);

    Assert.assertEquals(1, ShadowToast.shownToastCount());
    Assert.assertEquals(
        fragment.getString(R.string.radical_overlay_toast_refreshed),
        ShadowToast.getTextOfLatestToast());
  }

  @Test
  public void testDisposableDisposedOnDestroy() {
    final DictionariesFragment fragment = startFragment();
    Assert.assertFalse(fragment.isDisposed());

    getActivityController().destroy();

    Assert.assertTrue(fragment.isDisposed());
  }
}
