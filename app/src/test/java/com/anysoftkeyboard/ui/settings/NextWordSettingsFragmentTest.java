package com.anysoftkeyboard.ui.settings;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.view.MenuItem;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.ui.GeneralDialogControllerTest;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class NextWordSettingsFragmentTest extends RobolectricFragmentTestCase<NextWordSettingsFragment> {

    @NonNull
    @Override
    protected NextWordSettingsFragment createFragment() {
        return new NextWordSettingsFragment();
    }

    @Test
    public void testShowLanguageStats() {
        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        final Preference enStats = nextWordSettingsFragment.findPreference("en_stats");
        Assert.assertNotNull(enStats);
        Assert.assertEquals("en - English", enStats.getTitle());
    }

    @Test
    public void testBackupRestore() {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.backup_words).when(menuItem).getItemId();
        nextWordSettingsFragment.onOptionsItemSelected(menuItem);

        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.user_dict_backup_success_title), GeneralDialogControllerTest.getTitleFromDialog(GeneralDialogControllerTest.getLatestShownDialog()));

        Mockito.doReturn(R.id.restore_words).when(menuItem).getItemId();
        nextWordSettingsFragment.onOptionsItemSelected(menuItem);

        //we want a success dialog here
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.user_dict_restore_success_title), GeneralDialogControllerTest.getTitleFromDialog(GeneralDialogControllerTest.getLatestShownDialog()));
    }

    @Test
    public void testRestoreFailsWhenNoFile() {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.restore_words).when(menuItem).getItemId();
        nextWordSettingsFragment.onOptionsItemSelected(menuItem);

        //we want a failure dialog here
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.user_dict_restore_fail_title), GeneralDialogControllerTest.getTitleFromDialog(GeneralDialogControllerTest.getLatestShownDialog()));
    }

    @Test
    public void testBackupFailsWhenNoPermissions() {
        final NextWordSettingsFragment nextWordSettingsFragment = startFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.backup_words).when(menuItem).getItemId();
        nextWordSettingsFragment.onOptionsItemSelected(menuItem);

        //nothing happens here
        Assert.assertNull(ShadowApplication.getInstance().getLatestDialog());
    }
}