package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.view.Menu;
import android.view.MenuItem;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.ui.GeneralDialogControllerTest;
import com.anysoftkeyboard.ui.dev.DeveloperToolsFragment;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;

public class MainTweaksFragmentTest extends RobolectricFragmentTestCase<MainTweaksFragment> {

    @NonNull
    @Override
    protected MainTweaksFragment createFragment() {
        return new MainTweaksFragment();
    }

    @Test
    public void testNavigateToDevTools() {
        MainTweaksFragment fragment = startFragment();

        final Preference preferenceDevTools = fragment.findPreference(MainTweaksFragment.DEV_TOOLS_KEY);
        preferenceDevTools.getOnPreferenceClickListener().onPreferenceClick(preferenceDevTools);

        Robolectric.flushForegroundThreadScheduler();
        Fragment navigatedToFragment = fragment.getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertTrue(navigatedToFragment instanceof DeveloperToolsFragment);
    }

    @Test
    public void testBackupMenuItem() throws Exception {
        final MainTweaksFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.backup_prefs);
        Assert.assertNotNull(item);

        fragment.onOptionsItemSelected(item);

        final AlertDialog dialog = GeneralDialogControllerTest.getLatestShownDialog();
        Assert.assertNotNull(dialog);
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.word_editor_action_backup_words), GeneralDialogControllerTest.getTitleFromDialog(dialog));
        Assert.assertNotNull(dialog.getListView());
        Assert.assertEquals(GlobalPrefsBackup.getAllPrefsProviders(RuntimeEnvironment.application).size(), dialog.getListView().getCount());
/*
        Assert.assertTrue(AnyApplication.getBackupFile(MainTweaksFragment.GLOBAL_BACKUP_FILENAME).exists());
        Assert.assertTrue(AnyApplication.getBackupFile(MainTweaksFragment.GLOBAL_BACKUP_FILENAME).length() > 0);
*/
    }

    @Test
    public void testRestoreMenuItem() throws Exception {
        final MainTweaksFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.restore_prefs);
        Assert.assertNotNull(item);

        fragment.onOptionsItemSelected(item);

        final AlertDialog dialog = GeneralDialogControllerTest.getLatestShownDialog();
        Assert.assertNotNull(dialog);
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.word_editor_action_restore_words), GeneralDialogControllerTest.getTitleFromDialog(dialog));
        Assert.assertNotNull(dialog.getListView());
        Assert.assertEquals(GlobalPrefsBackup.getAllPrefsProviders(RuntimeEnvironment.application).size(), dialog.getListView().getCount());
/*
        Assert.assertTrue(AnyApplication.getBackupFile(MainTweaksFragment.GLOBAL_BACKUP_FILENAME).exists());
        Assert.assertTrue(AnyApplication.getBackupFile(MainTweaksFragment.GLOBAL_BACKUP_FILENAME).length() > 0);
*/
    }
}