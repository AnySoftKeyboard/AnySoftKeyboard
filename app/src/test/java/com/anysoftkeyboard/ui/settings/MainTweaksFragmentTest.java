package com.anysoftkeyboard.ui.settings;

import android.Manifest;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

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
import org.robolectric.shadows.ShadowDialog;

import java.util.List;

import io.reactivex.Observable;

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
    public void testDoesNotStartFlowIfHasNoPermission() throws Exception {
        Shadows.shadowOf(RuntimeEnvironment.application).denyPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        final MainTweaksFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.backup_prefs);
        Assert.assertNotNull(item);

        fragment.onOptionsItemSelected(item);

        final AlertDialog dialog = GeneralDialogControllerTest.getLatestShownDialog();
        Assert.assertSame(GeneralDialogControllerTest.NO_DIALOG, dialog);
    }

    @Test
    public void testBackupMenuItem() throws Exception {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        final MainTweaksFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.backup_prefs);
        Assert.assertNotNull(item);

        fragment.onOptionsItemSelected(item);

        final AlertDialog dialog = GeneralDialogControllerTest.getLatestShownDialog();
        Assert.assertNotSame(GeneralDialogControllerTest.NO_DIALOG, dialog);
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.pick_prefs_providers_to_backup), GeneralDialogControllerTest.getTitleFromDialog(dialog));
        final ListView dialogListView = dialog.getListView();
        Assert.assertNotNull(dialogListView);
        Assert.assertEquals(View.VISIBLE, dialogListView.getVisibility());
        final List<GlobalPrefsBackup.ProviderDetails> allPrefsProviders = GlobalPrefsBackup.getAllPrefsProviders(RuntimeEnvironment.application);
        Assert.assertEquals(allPrefsProviders.size(), dialogListView.getCount());
        //everything is checked at first
        for (int providerIndex = 0; providerIndex < allPrefsProviders.size(); providerIndex++) {
            Assert.assertEquals(activity.getText(allPrefsProviders.get(providerIndex).providerTitle), dialogListView.getItemAtPosition(providerIndex));
        }

        Assert.assertTrue(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick());
        //no dialog here
        Assert.assertSame(GeneralDialogControllerTest.NO_DIALOG, GeneralDialogControllerTest.getLatestShownDialog());
    }

    @Test
    public void testCompleteOperation() throws Exception {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        final MainTweaksFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        fragment.onOptionsItemSelected(Shadows.shadowOf(activity).getOptionsMenu().findItem(R.id.backup_prefs));

        Assert.assertNotSame(GeneralDialogControllerTest.NO_DIALOG, GeneralDialogControllerTest.getLatestShownDialog());

        Assert.assertTrue(GeneralDialogControllerTest.getLatestShownDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick());
        //back up was done
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.prefs_providers_operation_success),
                GeneralDialogControllerTest.getTitleFromDialog(GeneralDialogControllerTest.getLatestShownDialog()));
        //verifying that progress-dialog was shown
        Assert.assertNotNull(Observable.fromIterable(ShadowDialog.getShownDialogs())
                .filter(dialog -> !dialog.isShowing())
                .filter(dialog -> dialog.findViewById(R.id.progress_dialog_message_text_view) != null)
                .lastOrError()
                .blockingGet());
        //closing dialog
        Assert.assertTrue(GeneralDialogControllerTest.getLatestShownDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick());
        Assert.assertSame(GeneralDialogControllerTest.NO_DIALOG, GeneralDialogControllerTest.getLatestShownDialog());

        //good!
        ShadowDialog.getShownDialogs().clear();

        //now, restoring
        fragment.onOptionsItemSelected(Shadows.shadowOf(activity).getOptionsMenu().findItem(R.id.restore_prefs));
        Assert.assertTrue(GeneralDialogControllerTest.getLatestShownDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick());
        //back up was done
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.prefs_providers_operation_success),
                GeneralDialogControllerTest.getTitleFromDialog(GeneralDialogControllerTest.getLatestShownDialog()));
        //verifying that progress-dialog was shown
        Assert.assertNotNull(Observable.fromIterable(ShadowDialog.getShownDialogs())
                .filter(dialog -> !dialog.isShowing())
                .filter(dialog -> dialog.findViewById(R.id.progress_dialog_message_text_view) != null)
                .lastOrError()
                .blockingGet());
        //closing dialog
        Assert.assertTrue(GeneralDialogControllerTest.getLatestShownDialog().getButton(DialogInterface.BUTTON_POSITIVE).callOnClick());
        Assert.assertSame(GeneralDialogControllerTest.NO_DIALOG, GeneralDialogControllerTest.getLatestShownDialog());
    }

    @Test
    public void testRestoreMenuItem() throws Exception {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        final MainTweaksFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.restore_prefs);
        Assert.assertNotNull(item);

        fragment.onOptionsItemSelected(item);

        final AlertDialog dialog = GeneralDialogControllerTest.getLatestShownDialog();
        Assert.assertNotNull(dialog);
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.pick_prefs_providers_to_restore), GeneralDialogControllerTest.getTitleFromDialog(dialog));
        Assert.assertNotNull(dialog.getListView());
        Assert.assertEquals(GlobalPrefsBackup.getAllPrefsProviders(RuntimeEnvironment.application).size(), dialog.getListView().getCount());
    }
}