package com.anysoftkeyboard.ui.settings;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.GeneralDialogTestUtil;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowDialog;

@Config(sdk = Build.VERSION_CODES.M /*we are testing permissions here*/)
public class MainFragmentTest extends RobolectricFragmentTestCase<MainFragment> {

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.mainFragment;
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
    Assert.assertEquals(
        View.GONE, rootView.findViewById(R.id.change_log__web_link_item).getVisibility());
  }

  @Test
  public void testTestersVisibilityInTestingBuild() {
    startFragment();
    // replacing fragment
    MainFragment fragment = new MainFragment(true /*BuildConfig.DEBUG*/);
    getActivityController()
        .get()
        .getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.nav_host_fragment, fragment)
        .commitNow();
    ensureAllScheduledJobsAreDone();

    Assert.assertEquals(
        View.VISIBLE, fragment.getView().findViewById(R.id.testing_build_message).getVisibility());
    Assert.assertEquals(
        View.GONE, fragment.getView().findViewById(R.id.beta_sign_up).getVisibility());
  }

  @Test
  public void testTestersVisibilityInReleaseBuild() {
    startFragment();
    // replacing fragment
    MainFragment fragment = new MainFragment(false /*BuildConfig.DEBUG*/);
    getActivityController()
        .get()
        .getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.nav_host_fragment, fragment)
        .commitNow();
    ensureAllScheduledJobsAreDone();

    Assert.assertEquals(
        View.GONE, fragment.getView().findViewById(R.id.testing_build_message).getVisibility());
    Assert.assertEquals(
        View.VISIBLE, fragment.getView().findViewById(R.id.beta_sign_up).getVisibility());
  }

  @Test
  public void testShowsChangelog() throws Exception {
    MainFragment fragment = startFragment();
    final View changeLogCard = fragment.getView().findViewById(R.id.latest_change_log_card);
    Assert.assertNotNull(changeLogCard);
    final TextView title = changeLogCard.findViewById(R.id.changelog_version_title);
    Assert.assertNotNull(title);
    String actual = title.getText().toString().trim();
    var matchRegex =
        getApplicationContext()
            .getString(R.string.change_log_card_version_title_template, ".+")
            .trim();
    Assert.assertTrue(
        String.format(Locale.ROOT, "'%s' should match with '%s'", actual, matchRegex),
        Pattern.matches(matchRegex, actual));
    Assert.assertEquals(View.VISIBLE, title.getVisibility());
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
    TestRxSchedulers.foregroundFlushAllJobs();

    Fragment aboutFragment = getCurrentFragment();
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
    TestRxSchedulers.foregroundFlushAllJobs();

    Fragment aboutFragment = getCurrentFragment();
    Assert.assertNotNull(aboutFragment);
    Assert.assertTrue(aboutFragment instanceof MainTweaksFragment);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
  @Ignore("Robolectric does not support this API level")
  public void testBackupMenuItemNotSupportedPreKitKat() throws Exception {
    final MainFragment fragment = startFragment();
    final FragmentActivity activity = fragment.getActivity();

    Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
    Assert.assertNotNull(menu);
    final MenuItem item = menu.findItem(R.id.backup_prefs);

    fragment.onOptionsItemSelected(item);
    TestRxSchedulers.foregroundFlushAllJobs();

    final AlertDialog dialog = GeneralDialogTestUtil.getLatestShownDialog();
    Assert.assertNotSame(GeneralDialogTestUtil.NO_DIALOG, dialog);
    Assert.assertEquals(
        getApplicationContext().getText(R.string.backup_restore_not_support_before_kitkat),
        GeneralDialogTestUtil.getTitleFromDialog(dialog));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
  @Ignore("Robolectric does not support this API level")
  public void testRestoreMenuItemNotSupportedPreKitKat() throws Exception {
    final MainFragment fragment = startFragment();
    final FragmentActivity activity = fragment.getActivity();

    Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
    Assert.assertNotNull(menu);
    final MenuItem item = menu.findItem(R.id.restore_prefs);

    fragment.onOptionsItemSelected(item);
    TestRxSchedulers.foregroundFlushAllJobs();

    final AlertDialog dialog = GeneralDialogTestUtil.getLatestShownDialog();
    Assert.assertNotSame(GeneralDialogTestUtil.NO_DIALOG, dialog);
    Assert.assertEquals(
        getApplicationContext().getText(R.string.backup_restore_not_support_before_kitkat),
        GeneralDialogTestUtil.getTitleFromDialog(dialog));
  }

  @Test
  public void testBackupMenuItem() throws Exception {
    final MainFragment fragment = startFragment();
    final FragmentActivity activity = fragment.getActivity();

    Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
    Assert.assertNotNull(menu);
    final MenuItem item = menu.findItem(R.id.backup_prefs);
    Assert.assertNotNull(item);

    fragment.onOptionsItemSelected(item);
    TestRxSchedulers.foregroundFlushAllJobs();

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
  public void testRestorePickerCancel() throws Exception {
    final var shadowApplication = Shadows.shadowOf((Application) getApplicationContext());
    final MainFragment fragment = startFragment();
    final FragmentActivity activity = fragment.getActivity();

    fragment.onOptionsItemSelected(
        Shadows.shadowOf(activity).getOptionsMenu().findItem(R.id.restore_prefs));
    TestRxSchedulers.foregroundFlushAllJobs();

    Assert.assertNotSame(
        GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

    Assert.assertTrue(
        GeneralDialogTestUtil.getLatestShownDialog()
            .getButton(DialogInterface.BUTTON_POSITIVE)
            .callOnClick());
    TestRxSchedulers.foregroundAdvanceBy(1);
    // this will open the System's file chooser
    ShadowActivity.IntentForResult fileRequest =
        shadowApplication.getNextStartedActivityForResult();
    Assert.assertNotNull(fileRequest);
    Assert.assertEquals(Intent.ACTION_OPEN_DOCUMENT, fileRequest.intent.getAction());

    final var backupFile = Files.createTempFile("ask-backup", ".xml");
    Intent resultData = new Intent();
    resultData.setData(Uri.fromFile(backupFile.toFile()));
    Shadows.shadowOf(activity)
        .receiveResult(fileRequest.intent, Activity.RESULT_CANCELED, resultData);
    TestRxSchedulers.drainAllTasks();
    // pick cancel
    Assert.assertSame(
        GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());
  }

  @Test
  public void testCompleteOperation() throws Exception {
    final var shadowApplication = Shadows.shadowOf((Application) getApplicationContext());
    final MainFragment fragment = startFragment();
    final FragmentActivity activity = fragment.getActivity();

    fragment.onOptionsItemSelected(
        Shadows.shadowOf(activity).getOptionsMenu().findItem(R.id.backup_prefs));
    TestRxSchedulers.foregroundFlushAllJobs();

    Assert.assertNotSame(
        GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

    Assert.assertTrue(
        GeneralDialogTestUtil.getLatestShownDialog()
            .getButton(DialogInterface.BUTTON_POSITIVE)
            .callOnClick());
    TestRxSchedulers.foregroundAdvanceBy(1);
    // this will open the System's file chooser
    ShadowActivity.IntentForResult fileRequest =
        shadowApplication.getNextStartedActivityForResult();
    Assert.assertNotNull(fileRequest);
    Assert.assertEquals(Intent.ACTION_CREATE_DOCUMENT, fileRequest.intent.getAction());
    final var backupFile = Files.createTempFile("ask-backup", ".xml");
    Shadows.shadowOf(activity.getContentResolver())
        .registerOutputStream(
            Uri.fromFile(backupFile.toFile()), new FileOutputStream(backupFile.toFile()));
    Intent resultData = new Intent();
    resultData.setData(Uri.fromFile(backupFile.toFile()));
    Shadows.shadowOf(activity).receiveResult(fileRequest.intent, Activity.RESULT_OK, resultData);
    TestRxSchedulers.drainAllTasks();
    // back up was done
    Assert.assertEquals(
        getApplicationContext().getText(R.string.prefs_providers_operation_success),
        GeneralDialogTestUtil.getTitleFromDialog(GeneralDialogTestUtil.getLatestShownDialog()));
    // verifying that progress-dialog was shown
    Assert.assertNotNull(
        TestRxSchedulers.blockingGet(
            Observable.fromIterable(ShadowDialog.getShownDialogs())
                .filter(dialog -> !dialog.isShowing())
                .filter(
                    dialog -> dialog.findViewById(R.id.progress_dialog_message_text_view) != null)
                .lastOrError()));
    // closing dialog
    Assert.assertTrue(
        GeneralDialogTestUtil.getLatestShownDialog()
            .getButton(DialogInterface.BUTTON_POSITIVE)
            .callOnClick());
    Assert.assertSame(
        GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

    // good!
    ShadowDialog.getShownDialogs().clear();

    Shadows.shadowOf(activity.getContentResolver())
        .registerInputStream(
            Uri.fromFile(backupFile.toFile()), new FileInputStream(backupFile.toFile()));
    // now, restoring
    fragment.onOptionsItemSelected(
        Shadows.shadowOf(activity).getOptionsMenu().findItem(R.id.restore_prefs));
    TestRxSchedulers.foregroundFlushAllJobs();
    Assert.assertTrue(
        GeneralDialogTestUtil.getLatestShownDialog()
            .getButton(DialogInterface.BUTTON_POSITIVE)
            .callOnClick());
    TestRxSchedulers.foregroundAdvanceBy(1);
    // this will open the System's file chooser
    fileRequest = shadowApplication.getNextStartedActivityForResult();
    Assert.assertNotNull(fileRequest);
    Assert.assertEquals(Intent.ACTION_OPEN_DOCUMENT, fileRequest.intent.getAction());
    resultData = new Intent();
    resultData.setData(Uri.fromFile(backupFile.toFile()));
    Shadows.shadowOf(activity).receiveResult(fileRequest.intent, Activity.RESULT_OK, resultData);
    TestRxSchedulers.drainAllTasks();
    // back up was done
    Assert.assertEquals(
        getApplicationContext().getText(R.string.prefs_providers_operation_success),
        GeneralDialogTestUtil.getTitleFromDialog(GeneralDialogTestUtil.getLatestShownDialog()));
    // verifying that progress-dialog was shown
    Assert.assertNotNull(
        TestRxSchedulers.blockingGet(
            Observable.fromIterable(ShadowDialog.getShownDialogs())
                .filter(dialog -> !dialog.isShowing())
                .filter(
                    dialog -> dialog.findViewById(R.id.progress_dialog_message_text_view) != null)
                .lastOrError()));
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
    Shadows.shadowOf((Application) getApplicationContext());
    final MainFragment fragment = startFragment();
    final FragmentActivity activity = fragment.getActivity();

    Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
    Assert.assertNotNull(menu);
    final MenuItem item = menu.findItem(R.id.restore_prefs);
    Assert.assertNotNull(item);

    fragment.onOptionsItemSelected(item);
    TestRxSchedulers.foregroundFlushAllJobs();

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

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testShowNotificationPermissionCard() {
    var fragment = startFragment();
    var card = fragment.getView().findViewById(R.id.no_notifications_permission_click_here_root);
    Assert.assertEquals(View.VISIBLE, card.getVisibility());

    var viewShadow = Shadows.shadowOf(card);
    Assert.assertNotNull(viewShadow.getOnClickListener());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.S_V2)
  public void testDoNotShowNotificationPermissionCardBeforeT() {
    var fragment = startFragment();
    var card = fragment.getView().findViewById(R.id.no_notifications_permission_click_here_root);
    Assert.assertEquals(View.GONE, card.getVisibility());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testDoNotShowNotificationPermissionCardIfGranted() {
    var appShadow = Shadows.shadowOf(RuntimeEnvironment.getApplication());
    appShadow.grantPermissions(Manifest.permission.POST_NOTIFICATIONS);

    var fragment = startFragment();
    var card = fragment.getView().findViewById(R.id.no_notifications_permission_click_here_root);
    Assert.assertEquals(View.GONE, card.getVisibility());
  }
}
