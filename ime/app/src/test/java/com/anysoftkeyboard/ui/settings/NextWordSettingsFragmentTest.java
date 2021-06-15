package com.anysoftkeyboard.ui.settings;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.Manifest;
import android.app.Application;
import android.os.Build;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.utils.GeneralDialogTestUtil;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M)
public class NextWordSettingsFragmentTest
        extends RobolectricFragmentTestCase<NextWordSettingsFragment> {

    @NonNull
    @Override
    protected NextWordSettingsFragment createFragment() {
        return new NextWordSettingsFragment();
    }

    @Test
    public void testShowLanguageStats() {
        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

        com.anysoftkeyboard.rx.TestRxSchedulers.backgroundFlushAllJobs();
        TestRxSchedulers.foregroundFlushAllJobs();

        final Preference enStats = nextWordSettingsFragment.findPreference("en_stats");
        Assert.assertNotNull(enStats);
        Assert.assertEquals("en - English", enStats.getTitle());
    }

    @Test
    public void testBackupRestore() {
        Shadows.shadowOf((Application) getApplicationContext())
                .grantPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE);

        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.backup_words).when(menuItem).getItemId();
        nextWordSettingsFragment.onOptionsItemSelected(menuItem);
        TestRxSchedulers.foregroundFlushAllJobs();

        Assert.assertEquals(
                getApplicationContext().getText(R.string.user_dict_backup_success_title),
                GeneralDialogTestUtil.getTitleFromDialog(
                        GeneralDialogTestUtil.getLatestShownDialog()));

        Mockito.doReturn(R.id.restore_words).when(menuItem).getItemId();
        nextWordSettingsFragment.onOptionsItemSelected(menuItem);
        TestRxSchedulers.foregroundFlushAllJobs();

        // we want a success dialog here
        Assert.assertEquals(
                getApplicationContext().getText(R.string.user_dict_restore_success_title),
                GeneralDialogTestUtil.getTitleFromDialog(
                        GeneralDialogTestUtil.getLatestShownDialog()));
    }

    @Test
    public void testRestoreFailsWhenNoFile() {
        Shadows.shadowOf((Application) getApplicationContext())
                .grantPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE);

        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.restore_words).when(menuItem).getItemId();
        nextWordSettingsFragment.onOptionsItemSelected(menuItem);
        TestRxSchedulers.foregroundFlushAllJobs();

        // we want a failure dialog here
        Assert.assertEquals(
                getApplicationContext().getText(R.string.user_dict_restore_fail_title),
                GeneralDialogTestUtil.getTitleFromDialog(
                        GeneralDialogTestUtil.getLatestShownDialog()));
    }

    @Test
    public void testBackupFailsWhenNoPermissions() {
        Shadows.shadowOf((Application) getApplicationContext())
                .denyPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();
        Assert.assertNull(ShadowDialog.getLatestDialog());

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.backup_words).when(menuItem).getItemId();
        nextWordSettingsFragment.onOptionsItemSelected(menuItem);
        TestRxSchedulers.foregroundFlushAllJobs();

        // nothing happens here
        Assert.assertNull(ShadowDialog.getLatestDialog());
    }
}
