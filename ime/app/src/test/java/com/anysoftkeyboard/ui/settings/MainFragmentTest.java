package com.anysoftkeyboard.ui.settings;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.utils.GeneralDialogTestUtil;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowDialog;

public class MainFragmentTest extends RobolectricFragmentTestCase<MainFragment> {

    private AtomicReference<MainFragment> mFragment;

    @Before
    public void setup() {
        mFragment = new AtomicReference<>(new MainFragment());
    }

    @NonNull
    @Override
    protected MainFragment createFragment() {
        return mFragment.get();
    }

    @Test
    public void testTestersVisibilityInTestingBuild() {
        mFragment.set(new MainFragment(true));

        MainFragment fragment = startFragment();
        Assert.assertEquals(
                View.VISIBLE,
                fragment.getView().findViewById(R.id.testing_build_message).getVisibility());
        Assert.assertEquals(
                View.GONE, fragment.getView().findViewById(R.id.beta_sign_up).getVisibility());
    }

    @Test
    public void testTestersVisibilityInReleaseBuild() {
        mFragment.set(new MainFragment(false));

        MainFragment fragment = startFragment();
        Assert.assertEquals(
                View.GONE,
                fragment.getView().findViewById(R.id.testing_build_message).getVisibility());
        Assert.assertEquals(
                View.VISIBLE, fragment.getView().findViewById(R.id.beta_sign_up).getVisibility());
    }

    @Test
    public void testShowsChangelog() throws Exception {
        MainFragment fragment = startFragment();
        final Fragment changeLogFragment =
                fragment.getChildFragmentManager().findFragmentById(R.id.change_log_fragment);
        Assert.assertNotNull(changeLogFragment);
        Assert.assertTrue(changeLogFragment.isVisible());
        Assert.assertEquals(View.VISIBLE, changeLogFragment.getView().getVisibility());
    }

    @Test
    public void testAboutMenuCommand() throws Exception {
        final MainFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.about_menu_option);
        Assert.assertNotNull(item);
        Assert.assertTrue(item.isVisible());

        fragment.onOptionsItemSelected(item);
        Robolectric.flushForegroundThreadScheduler();

        Fragment aboutFragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertNotNull(aboutFragment);
        Assert.assertTrue(aboutFragment instanceof AboutAnySoftKeyboardFragment);
    }

    @Test
    public void testTweaksMenuCommand() throws Exception {
        final MainFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.tweaks_menu_option);
        Assert.assertNotNull(item);
        Assert.assertTrue(item.isVisible());

        fragment.onOptionsItemSelected(item);
        Robolectric.flushForegroundThreadScheduler();

        Fragment aboutFragment =
                activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertNotNull(aboutFragment);
        Assert.assertTrue(aboutFragment instanceof MainTweaksFragment);
    }

    @Test
    public void testDoesNotStartFlowIfHasNoPermission() throws Exception {
        Shadows.shadowOf((Application) getApplicationContext())
                .denyPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
        final MainFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.backup_prefs);
        Assert.assertNotNull(item);

        fragment.onOptionsItemSelected(item);

        final AlertDialog dialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertSame(GeneralDialogTestUtil.NO_DIALOG, dialog);
    }

    @Test
    public void testBackupMenuItem() throws Exception {
        Shadows.shadowOf((Application) getApplicationContext())
                .grantPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
        final MainFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.backup_prefs);
        Assert.assertNotNull(item);

        fragment.onOptionsItemSelected(item);

        final AlertDialog dialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotSame(GeneralDialogTestUtil.NO_DIALOG, dialog);
        Assert.assertEquals(
                getApplicationContext().getText(R.string.pick_prefs_providers_to_backup),
                GeneralDialogTestUtil.getTitleFromDialog(dialog));
        final ListView dialogListView = dialog.getListView();
        Assert.assertNotNull(dialogListView);
        Assert.assertEquals(View.VISIBLE, dialogListView.getVisibility());
        final List<GlobalPrefsBackup.ProviderDetails> allPrefsProviders =
                GlobalPrefsBackup.getAllPrefsProviders(getApplicationContext());
        Assert.assertEquals(allPrefsProviders.size(), dialogListView.getCount());
        // everything is checked at first
        for (int providerIndex = 0; providerIndex < allPrefsProviders.size(); providerIndex++) {
            Assert.assertEquals(
                    activity.getText(allPrefsProviders.get(providerIndex).providerTitle),
                    dialogListView.getItemAtPosition(providerIndex));
        }

        Assert.assertTrue(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).callOnClick());
        // no dialog here
        Assert.assertSame(
                GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());
    }

    @Test
    public void testCompleteOperation() throws Exception {
        Shadows.shadowOf((Application) getApplicationContext())
                .grantPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
        final MainFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        fragment.onOptionsItemSelected(
                Shadows.shadowOf(activity).getOptionsMenu().findItem(R.id.backup_prefs));

        Assert.assertNotSame(
                GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

        Assert.assertTrue(
                GeneralDialogTestUtil.getLatestShownDialog()
                        .getButton(DialogInterface.BUTTON_POSITIVE)
                        .callOnClick());
        // back up was done
        Assert.assertEquals(
                getApplicationContext().getText(R.string.prefs_providers_operation_success),
                GeneralDialogTestUtil.getTitleFromDialog(
                        GeneralDialogTestUtil.getLatestShownDialog()));
        // verifying that progress-dialog was shown
        Assert.assertNotNull(
                Observable.fromIterable(ShadowDialog.getShownDialogs())
                        .filter(dialog -> !dialog.isShowing())
                        .filter(
                                dialog ->
                                        dialog.findViewById(R.id.progress_dialog_message_text_view)
                                                != null)
                        .lastOrError()
                        .blockingGet());
        // closing dialog
        Assert.assertTrue(
                GeneralDialogTestUtil.getLatestShownDialog()
                        .getButton(DialogInterface.BUTTON_POSITIVE)
                        .callOnClick());
        Assert.assertSame(
                GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

        // good!
        ShadowDialog.getShownDialogs().clear();

        // now, restoring
        fragment.onOptionsItemSelected(
                Shadows.shadowOf(activity).getOptionsMenu().findItem(R.id.restore_prefs));
        Assert.assertTrue(
                GeneralDialogTestUtil.getLatestShownDialog()
                        .getButton(DialogInterface.BUTTON_POSITIVE)
                        .callOnClick());
        // back up was done
        Assert.assertEquals(
                getApplicationContext().getText(R.string.prefs_providers_operation_success),
                GeneralDialogTestUtil.getTitleFromDialog(
                        GeneralDialogTestUtil.getLatestShownDialog()));
        // verifying that progress-dialog was shown
        Assert.assertNotNull(
                Observable.fromIterable(ShadowDialog.getShownDialogs())
                        .filter(dialog -> !dialog.isShowing())
                        .filter(
                                dialog ->
                                        dialog.findViewById(R.id.progress_dialog_message_text_view)
                                                != null)
                        .lastOrError()
                        .blockingGet());
        // closing dialog
        Assert.assertTrue(
                GeneralDialogTestUtil.getLatestShownDialog()
                        .getButton(DialogInterface.BUTTON_POSITIVE)
                        .callOnClick());
        Assert.assertSame(
                GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());
    }

    @Test
    public void testRestoreMenuItem() throws Exception {
        Shadows.shadowOf((Application) getApplicationContext())
                .grantPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
        final MainFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.restore_prefs);
        Assert.assertNotNull(item);

        fragment.onOptionsItemSelected(item);

        final AlertDialog dialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(dialog);
        Assert.assertEquals(
                getApplicationContext().getText(R.string.pick_prefs_providers_to_restore),
                GeneralDialogTestUtil.getTitleFromDialog(dialog));
        Assert.assertNotNull(dialog.getListView());
        Assert.assertEquals(
                GlobalPrefsBackup.getAllPrefsProviders(getApplicationContext()).size(),
                dialog.getListView().getCount());
    }
}
