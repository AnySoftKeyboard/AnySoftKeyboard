package com.anysoftkeyboard.ui.settings;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.palette.graphics.Palette;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.permissions.PermissionRequestHelper;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.releaseinfo.ChangeLogFragment;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.settings.setup.SetupSupport;
import com.anysoftkeyboard.ui.settings.setup.SetupWizardActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;
import java.util.List;
import net.evendanan.pixel.GeneralDialogController;
import net.evendanan.pixel.RxProgressDialog;
import net.evendanan.pixel.UiUtils;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class MainFragment extends Fragment {

  static final int DIALOG_SAVE_SUCCESS = 10;
  static final int DIALOG_SAVE_FAILED = 11;
  static final int DIALOG_LOAD_SUCCESS = 20;
  static final int DIALOG_LOAD_FAILED = 21;
  static final int BACKUP_REQUEST_ID = 1341;
  static final int RESTORE_REQUEST_ID = 1343;
  private static final String TAG = "MainFragment";
  public static List<GlobalPrefsBackup.ProviderDetails> supportedProviders;
  public static Boolean[] checked;

  private final boolean mTestingBuild;
  @NonNull private final CompositeDisposable mDisposable = new CompositeDisposable();
  private AnimationDrawable mNotConfiguredAnimation = null;

  private View mNoNotificationPermissionView;
  @NonNull private Disposable mPaletteDisposable = Disposables.empty();
  private DemoAnyKeyboardView mDemoAnyKeyboardView;
  private GeneralDialogController mDialogController;

  public MainFragment() {
    this(BuildConfig.TESTING_BUILD);
  }

  @SuppressWarnings("ValidFragment")
  @VisibleForTesting
  MainFragment(boolean testingBuild) {
    mTestingBuild = testingBuild;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.main_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mDialogController =
        new GeneralDialogController(
            getActivity(), R.style.Theme_AskAlertDialog, this::onSetupDialogRequired);
    final ViewGroup latestChangeLogCard = view.findViewById(R.id.latest_change_log_card);
    final View latestChangeLogCardContent =
        ChangeLogFragment.LatestChangeLogViewFactory.createLatestChangeLogView(
            this,
            latestChangeLogCard,
            () ->
                Navigation.findNavController(requireView())
                    .navigate(MainFragmentDirections.actionMainFragmentToFullChangeLogFragment()));
    latestChangeLogCard.addView(latestChangeLogCardContent);
    View testingView = view.findViewById(R.id.testing_build_message);
    testingView.setVisibility(mTestingBuild ? View.VISIBLE : View.GONE);
    View testerSignUp = view.findViewById(R.id.beta_sign_up);
    testerSignUp.setVisibility(mTestingBuild ? View.GONE : View.VISIBLE);
    mDemoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view);
    mNoNotificationPermissionView =
        view.findViewById(R.id.no_notifications_permission_click_here_root);
    mNoNotificationPermissionView.setOnClickListener(
        v -> AnyApplication.notifier(requireContext()).askForNotificationPostPermission(this));

    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.main_fragment_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.about_menu_option:
        Navigation.findNavController(requireView())
            .navigate(MainFragmentDirections.actionMainFragmentToAboutAnySoftKeyboardFragment());
        return true;
      case R.id.tweaks_menu_option:
        Navigation.findNavController(requireView())
            .navigate(MainFragmentDirections.actionMainFragmentToMainTweaksFragment());
        return true;
      case R.id.backup_prefs:
        onBackupRequested();
        return true;
      case R.id.restore_prefs:
        onRestoreRequested();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onViewStateRestored(Bundle savedInstanceState) {
    super.onViewStateRestored(savedInstanceState);
    // I'm doing the setup of the link in onViewStateRestored, since the links will be restored
    // too
    // and they will probably refer to a different scoop (Fragment).
    // setting up the underline and click handler in the keyboard_not_configured_box layout
    TextView clickHere = getView().findViewById(R.id.not_configured_click_here);
    mNotConfiguredAnimation =
        clickHere.getVisibility() == View.VISIBLE
            ? (AnimationDrawable) clickHere.getCompoundDrawables()[0]
            : null;

    String fullText = getString(R.string.not_configured_with_click_here);
    String justClickHereText = getString(R.string.not_configured_with_just_click_here);
    SpannableStringBuilder sb = new SpannableStringBuilder(fullText);
    // Get the index of "click here" string.
    int start = fullText.indexOf(justClickHereText);
    int length = justClickHereText.length();
    if (start == -1) {
      // this could happen when the localization is not correct
      start = 0;
      length = fullText.length();
    }
    ClickableSpan csp =
        new ClickableSpan() {
          @Override
          public void onClick(@NonNull View v) {
            startActivity(new Intent(requireContext(), SetupWizardActivity.class));
          }
        };
    sb.setSpan(csp, start, start + length, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    clickHere.setMovementMethod(LinkMovementMethod.getInstance());
    clickHere.setText(sb);

    ClickableSpan socialLink =
        new ClickableSpan() {
          @Override
          public void onClick(@NonNull View widget) {
            Intent browserIntent =
                new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(getResources().getString(R.string.main_site_url)));
            try {
              startActivity(browserIntent);
            } catch (ActivityNotFoundException weirdException) {
              // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/516
              // this means that there is nothing on the device
              // that can handle Intent.ACTION_VIEW with "https" schema..
              // silently swallowing it
              Logger.w(
                  TAG,
                  "Can not open '%' since there is nothing on the device that can" + " handle it.",
                  browserIntent.getData());
            }
          }
        };
    UiUtils.setupLink(getView(), R.id.ask_social_link, socialLink, false);
  }

  @Override
  public void onStart() {
    super.onStart();
    UiUtils.setActivityTitle(this, R.string.how_to_pointer_title);

    View notConfiguredBox = getView().findViewById(R.id.not_configured_click_here_root);
    // checking if the IME is configured
    final Context context = requireContext().getApplicationContext();

    if (SetupSupport.isThisKeyboardSetAsDefaultIME(context)) {
      notConfiguredBox.setVisibility(View.GONE);
    } else {
      notConfiguredBox.setVisibility(View.VISIBLE);
    }

    AnyKeyboard defaultKeyboard =
        AnyApplication.getKeyboardFactory(requireContext())
            .getEnabledAddOn()
            .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
    defaultKeyboard.loadKeyboard(mDemoAnyKeyboardView.getThemedKeyboardDimens());
    mDemoAnyKeyboardView.setKeyboard(defaultKeyboard, null, null);

    mDemoAnyKeyboardView.setOnViewBitmapReadyListener(this::onDemoViewBitmapReady);

    if (mNotConfiguredAnimation != null) {
      mNotConfiguredAnimation.start();
    }

    setNotificationPermissionCardVisibility();
  }

  @AfterPermissionGranted(PermissionRequestHelper.NOTIFICATION_PERMISSION_REQUEST_CODE)
  private void setNotificationPermissionCardVisibility() {
    mNoNotificationPermissionView.setVisibility(View.GONE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
              requireContext(), Manifest.permission.POST_NOTIFICATIONS)
          != PackageManager.PERMISSION_GRANTED) {
        mNoNotificationPermissionView.setVisibility(View.VISIBLE);
      }
    }
  }

  private void onDemoViewBitmapReady(Bitmap demoViewBitmap) {
    mPaletteDisposable =
        Observable.just(demoViewBitmap)
            .subscribeOn(RxSchedulers.background())
            .map(
                bitmap -> {
                  Palette p = Palette.from(bitmap).generate();
                  Palette.Swatch highestSwatch = null;
                  for (Palette.Swatch swatch : p.getSwatches()) {
                    if (highestSwatch == null
                        || highestSwatch.getPopulation() < swatch.getPopulation()) {
                      highestSwatch = swatch;
                    }
                  }
                  return highestSwatch;
                })
            .observeOn(RxSchedulers.mainThread())
            .subscribe(
                swatch -> {
                  final View rootView = getView();
                  if (swatch != null && rootView != null) {
                    final int backgroundRed = Color.red(swatch.getRgb());
                    final int backgroundGreed = Color.green(swatch.getRgb());
                    final int backgroundBlue = Color.blue(swatch.getRgb());
                    final int backgroundColor =
                        Color.argb(
                            200 /*~80% alpha*/, backgroundRed, backgroundGreed, backgroundBlue);
                    TextView gplusLink = rootView.findViewById(R.id.ask_social_link);
                    gplusLink.setTextColor(swatch.getTitleTextColor());
                    gplusLink.setBackgroundColor(backgroundColor);
                  }
                },
                throwable ->
                    Logger.w(TAG, throwable, "Failed to parse palette from demo-keyboard."));
  }

  @Override
  public void onStop() {
    super.onStop();
    mPaletteDisposable.dispose();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    mDemoAnyKeyboardView.onViewNotRequired();
    mDialogController.dismiss();
  }

  private void onSetupDialogRequired(
      Context context, AlertDialog.Builder builder, int optionId, Object data) {
    switch (optionId) {
      case R.id.backup_prefs:
      case R.id.restore_prefs:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          onBackupRestoreDialogRequired(builder, optionId);
        } else {
          builder.setTitle(R.string.backup_restore_not_support_before_kitkat);
          builder.setMessage(R.string.backup_restore_not_support_before_kitkat_message);
          builder.setPositiveButton(android.R.string.ok, null);
        }
        break;
      case DIALOG_SAVE_SUCCESS:
        builder.setTitle(R.string.prefs_providers_operation_success);
        builder.setMessage(getString(R.string.prefs_providers_backed_up_to, data));
        builder.setPositiveButton(android.R.string.ok, null);
        break;
      case DIALOG_SAVE_FAILED:
        builder.setTitle(R.string.prefs_providers_operation_failed);
        builder.setMessage(getString(R.string.prefs_providers_failed_backup_due_to, data));
        builder.setPositiveButton(android.R.string.ok, null);
        break;
      case DIALOG_LOAD_SUCCESS:
        builder.setTitle(R.string.prefs_providers_operation_success);
        builder.setMessage(getString(R.string.prefs_providers_restored_to, data));
        builder.setPositiveButton(android.R.string.ok, null);
        break;
      case DIALOG_LOAD_FAILED:
        builder.setTitle(R.string.prefs_providers_operation_failed);
        builder.setMessage(getString(R.string.prefs_providers_failed_restore_due_to, data));
        builder.setPositiveButton(android.R.string.ok, null);
        break;
      default:
        throw new IllegalArgumentException("The option-id " + optionId + " is not supported here.");
    }
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  private void onBackupRestoreDialogRequired(AlertDialog.Builder builder, int optionId) {
    @StringRes final int actionTitle;

    final String intentAction;
    switch (optionId) {
      case R.id.backup_prefs -> {
        actionTitle = R.string.word_editor_action_backup_words;
        intentAction = Intent.ACTION_CREATE_DOCUMENT;
        builder.setTitle(R.string.pick_prefs_providers_to_backup);
      }
      case R.id.restore_prefs -> {
        actionTitle = R.string.word_editor_action_restore_words;
        intentAction = Intent.ACTION_OPEN_DOCUMENT;
        builder.setTitle(R.string.pick_prefs_providers_to_restore);
      }
      default -> throw new IllegalArgumentException(
          "The option-id " + optionId + " is not supported here.");
    }

    supportedProviders = GlobalPrefsBackup.getAllPrefsProviders(requireContext());
    final CharSequence[] providersTitles = new CharSequence[supportedProviders.size()];
    final boolean[] initialChecked = new boolean[supportedProviders.size()];
    checked = new Boolean[supportedProviders.size()];

    for (int providerIndex = 0; providerIndex < supportedProviders.size(); providerIndex++) {
      // starting with everything checked
      checked[providerIndex] = initialChecked[providerIndex] = true;
      providersTitles[providerIndex] = getText(supportedProviders.get(providerIndex).providerTitle);
    }

    builder.setMultiChoiceItems(
        providersTitles, initialChecked, (dialogInterface, i, b) -> checked[i] = b);
    builder.setNegativeButton(android.R.string.cancel, null);
    builder.setCancelable(true);
    builder.setPositiveButton(
        actionTitle,
        (dialog, which) -> {
          // https://developer.android.com/training/data-storage/shared/documents-files#java
          Intent dataToFileChooser = new Intent();
          dataToFileChooser.setType("text/xml");
          dataToFileChooser.addCategory(Intent.CATEGORY_OPENABLE);
          dataToFileChooser.putExtra(Intent.EXTRA_TITLE, GlobalPrefsBackup.GLOBAL_BACKUP_FILENAME);
          dataToFileChooser.setAction(intentAction);
          dataToFileChooser.putExtra("checked", checked);
          try {
            startActivityForResult(
                dataToFileChooser,
                optionId == R.id.backup_prefs ? BACKUP_REQUEST_ID : RESTORE_REQUEST_ID);
          } catch (ActivityNotFoundException e) {
            Logger.e(TAG, "Could not launch the custom path activity");
            Toast.makeText(
                    requireContext().getApplicationContext(),
                    R.string.toast_error_custom_path_backup,
                    Toast.LENGTH_LONG)
                .show();
          }
        });
  }

  private Disposable launchBackupRestore(final boolean isBackup, Uri filePath) {
    final Function<
            Pair<List<GlobalPrefsBackup.ProviderDetails>, Boolean[]>,
            ObservableSource<GlobalPrefsBackup.ProviderDetails>>
        action;
    if (isBackup) {
      action =
          listPair ->
              GlobalPrefsBackup.backup(
                  listPair, getContext().getContentResolver().openOutputStream(filePath));
    } else {
      action =
          listPair ->
              GlobalPrefsBackup.restore(
                  listPair, getContext().getContentResolver().openInputStream(filePath));
    }

    return RxProgressDialog.create(
            new Pair<>(supportedProviders, checked),
            requireActivity(),
            getText(R.string.take_a_while_progress_message),
            R.layout.progress_window)
        .subscribeOn(RxSchedulers.background())
        .flatMap(action)
        .observeOn(RxSchedulers.mainThread())
        .subscribe(
            providerDetails ->
                Logger.i(
                    "MainFragment",
                    "Finished backing up %s",
                    providerDetails.provider.providerId()),
            e -> {
              Logger.w("MainFragment", e, "Failed to do operation due to %s", e.getMessage());
              mDialogController.showDialog(
                  isBackup ? DIALOG_SAVE_FAILED : DIALOG_LOAD_FAILED, e.getMessage());
            },
            () ->
                mDialogController.showDialog(
                    isBackup ? DIALOG_SAVE_SUCCESS : DIALOG_LOAD_SUCCESS, filePath));
  }

  // This function is if launched when selecting backup/restore button of the main Fragment
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if ((requestCode == RESTORE_REQUEST_ID || requestCode == BACKUP_REQUEST_ID)
        && resultCode == Activity.RESULT_OK) {

      ContentResolver resolver = requireContext().getContentResolver();
      Logger.d(TAG, "Resolver " + resolver.getType(data.getData()));
      try {
        // https://developer.android.com/training/data-storage/shared/documents-files#java
        mDisposable.add(launchBackupRestore(requestCode == BACKUP_REQUEST_ID, data.getData()));
      } catch (Exception e) {
        e.printStackTrace();
        Logger.d(TAG, "Error when getting filePath on onActivityResult");
      }
    }
  }

  @Override
  public void onDestroy() {
    mDisposable.dispose();
    super.onDestroy();
  }

  @AfterPermissionGranted(PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE)
  public void onRestoreRequested() {
    if (PermissionRequestHelper.check(
        this, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE)) {
      mDialogController.showDialog(R.id.restore_prefs);
    }
  }

  @AfterPermissionGranted(PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE)
  public void onBackupRequested() {
    if (PermissionRequestHelper.check(
        this, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE)) {
      mDialogController.showDialog(R.id.backup_prefs);
    }
  }

  @SuppressWarnings("deprecation") // required for permissions flow
  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    PermissionRequestHelper.onRequestPermissionsResult(
        requestCode, permissions, grantResults, this);
  }
}
