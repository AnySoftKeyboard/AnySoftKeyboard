package com.anysoftkeyboard.ui.settings;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.palette.graphics.Palette;
import com.anysoftkeyboard.PermissionsRequestCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.prefs.backup.PrefsXmlStorage;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.FileExplorerCreate;
import com.anysoftkeyboard.ui.FileExplorerRestore;
import com.anysoftkeyboard.ui.settings.setup.SetUpKeyboardWizardFragment;
import com.anysoftkeyboard.ui.settings.setup.SetupSupport;
import com.anysoftkeyboard.ui.tutorials.ChangeLogFragment;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Function;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import net.evendanan.chauffeur.lib.FragmentChauffeurActivity;
import net.evendanan.chauffeur.lib.experiences.TransitionExperiences;
import net.evendanan.chauffeur.lib.permissions.PermissionsRequest;
import net.evendanan.pixel.GeneralDialogController;
import net.evendanan.pixel.RxProgressDialog;

public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    static final int DIALOG_SAVE_SUCCESS = 10;
    static final int DIALOG_SAVE_FAILED = 11;
    static final int DIALOG_LOAD_SUCCESS = 20;
    static final int DIALOG_LOAD_FAILED = 21;
    static int successDialog;
    static int failedDialog;
    public static List<GlobalPrefsBackup.ProviderDetails> supportedProviders;
    public static Boolean[] checked;
    static Function<
                    Pair<List<GlobalPrefsBackup.ProviderDetails>, Boolean[]>,
                    ObservableSource<GlobalPrefsBackup.ProviderDetails>>
            action;

    private final boolean mTestingBuild;
    private AnimationDrawable mNotConfiguredAnimation = null;
    @NonNull private Disposable mPaletteDisposable = Disposables.empty();
    private DemoAnyKeyboardView mDemoAnyKeyboardView;

    public int modeBackupRestore;
    private GeneralDialogController mDialogController;
    @NonNull private CompositeDisposable mDisposable = new CompositeDisposable();

    public static void setupLink(
            View root,
            int showMoreLinkId,
            ClickableSpan clickableSpan,
            boolean reorderLinkToLastChild) {
        TextView clickHere = root.findViewById(showMoreLinkId);
        if (reorderLinkToLastChild) {
            ViewGroup rootContainer = (ViewGroup) root;
            rootContainer.removeView(clickHere);
            rootContainer.addView(clickHere);
        }

        SpannableStringBuilder sb = new SpannableStringBuilder(clickHere.getText());
        sb.clearSpans(); // removing any previously (from instance-state) set click spans.
        sb.setSpan(
                clickableSpan, 0, clickHere.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        clickHere.setMovementMethod(LinkMovementMethod.getInstance());
        clickHere.setText(sb);
    }

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDialogController = new GeneralDialogController(getActivity(), this::onSetupDialogRequired);

        if (savedInstanceState == null) {
            // I to prevent leaks and duplicate ID errors, I must use the getChildFragmentManager
            // to add the inner fragments into the UI.
            // See: https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/285
            FragmentManager fragmentManager = getChildFragmentManager();
            fragmentManager
                    .beginTransaction()
                    .replace(
                            R.id.change_log_fragment,
                            new ChangeLogFragment.LatestChangeLogFragment())
                    .commit();
        }
        View testingView = view.findViewById(R.id.testing_build_message);
        testingView.setVisibility(mTestingBuild ? View.VISIBLE : View.GONE);
        View testerSignUp = view.findViewById(R.id.beta_sign_up);
        testerSignUp.setVisibility(mTestingBuild ? View.GONE : View.VISIBLE);
        mDemoAnyKeyboardView = view.findViewById(R.id.demo_keyboard_view);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentChauffeurActivity activity = (FragmentChauffeurActivity) getActivity();
        switch (item.getItemId()) {
            case R.id.about_menu_option:
                activity.addFragmentToUi(
                        new AboutAnySoftKeyboardFragment(),
                        TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                return true;
            case R.id.tweaks_menu_option:
                activity.addFragmentToUi(
                        new MainTweaksFragment(),
                        TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                return true;
            case R.id.backup_prefs:
            case R.id.restore_prefs:
                ((MainSettingsActivity) getActivity())
                        .startPermissionsRequest(
                                new MainFragment.StoragePermissionRequest(this, item.getItemId()));
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
                    public void onClick(View v) {
                        FragmentChauffeurActivity activity =
                                (FragmentChauffeurActivity) getActivity();
                        activity.addFragmentToUi(
                                new SetUpKeyboardWizardFragment(),
                                TransitionExperiences.DEEPER_EXPERIENCE_TRANSITION);
                    }
                };
        sb.setSpan(csp, start, start + length, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        clickHere.setMovementMethod(LinkMovementMethod.getInstance());
        clickHere.setText(sb);

        ClickableSpan socialLink =
                new ClickableSpan() {
                    @Override
                    public void onClick(View widget) {
                        Intent browserIntent =
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(
                                                getResources().getString(R.string.main_site_url)));
                        try {
                            startActivity(browserIntent);
                        } catch (ActivityNotFoundException weirdException) {
                            // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/516
                            // this means that there is nothing on the device
                            // that can handle Intent.ACTION_VIEW with "https" schema..
                            // silently swallowing it
                            Logger.w(
                                    TAG,
                                    "Can not open '%' since there is nothing on the device that can handle it.",
                                    browserIntent.getData());
                        }
                    }
                };
        setupLink(getView(), R.id.ask_social_link, socialLink, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.how_to_pointer_title));

        View notConfiguredBox = getView().findViewById(R.id.not_configured_click_here_root);
        // checking if the IME is configured
        final Context context = getActivity().getApplicationContext();

        if (SetupSupport.isThisKeyboardSetAsDefaultIME(context)) {
            notConfiguredBox.setVisibility(View.GONE);
        } else {
            notConfiguredBox.setVisibility(View.VISIBLE);
        }

        AnyKeyboard defaultKeyboard =
                AnyApplication.getKeyboardFactory(getContext())
                        .getEnabledAddOn()
                        .createKeyboard(Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        defaultKeyboard.loadKeyboard(mDemoAnyKeyboardView.getThemedKeyboardDimens());
        mDemoAnyKeyboardView.setKeyboard(defaultKeyboard, null, null);

        mDemoAnyKeyboardView.setOnViewBitmapReadyListener(this::onDemoViewBitmapReady);

        if (mNotConfiguredAnimation != null) {
            mNotConfiguredAnimation.start();
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
                                                || highestSwatch.getPopulation()
                                                        < swatch.getPopulation()) {
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
                                                        200 /*~80% alpha*/,
                                                        backgroundRed,
                                                        backgroundGreed,
                                                        backgroundBlue);
                                        TextView gplusLink =
                                                rootView.findViewById(R.id.ask_social_link);
                                        gplusLink.setTextColor(swatch.getTitleTextColor());
                                        gplusLink.setBackgroundColor(backgroundColor);
                                    }
                                },
                                throwable ->
                                        Logger.w(
                                                TAG,
                                                throwable,
                                                "Failed to parse palette from demo-keyboard."));
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

    private void onSetupDialogRequired(AlertDialog.Builder builder, int optionId, Object data) {
        switch (optionId) {
            case R.id.backup_prefs:
            case R.id.restore_prefs:
                onBackupRestoreDialogRequired(builder, optionId);
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
                throw new IllegalArgumentException(
                        "The option-id " + optionId + " is not supported here.");
        }
    }

    private void onBackupRestoreDialogRequired(AlertDialog.Builder builder, int optionId) {
        final int actionString;
        final int choosePathString = R.string.word_editor_action_choose_path;

        final String actionCustomPath;
        modeBackupRestore = optionId;
        switch (optionId) {
            case R.id.backup_prefs:
                action = GlobalPrefsBackup::backup;
                actionString = R.string.word_editor_action_backup_words;
                actionCustomPath = Intent.ACTION_CREATE_DOCUMENT;
                builder.setTitle(R.string.pick_prefs_providers_to_backup);
                successDialog = DIALOG_SAVE_SUCCESS;
                failedDialog = DIALOG_SAVE_FAILED;
                break;
            case R.id.restore_prefs:
                action = GlobalPrefsBackup::restore;
                actionString = R.string.word_editor_action_restore_words;
                actionCustomPath = Intent.ACTION_GET_CONTENT;
                builder.setTitle(R.string.pick_prefs_providers_to_restore);
                successDialog = DIALOG_LOAD_SUCCESS;
                failedDialog = DIALOG_LOAD_FAILED;
                break;
            default:
                throw new IllegalArgumentException(
                        "The option-id " + optionId + " is not supported here.");
        }

        supportedProviders = GlobalPrefsBackup.getAllPrefsProviders(getContext());
        final CharSequence[] providersTitles = new CharSequence[supportedProviders.size()];
        final boolean[] initialChecked = new boolean[supportedProviders.size()];
        checked = new Boolean[supportedProviders.size()];

        for (int providerIndex = 0; providerIndex < supportedProviders.size(); providerIndex++) {
            // starting with everything checked
            checked[providerIndex] = initialChecked[providerIndex] = true;
            providersTitles[providerIndex] =
                    getText(supportedProviders.get(providerIndex).providerTitle);
        }

        builder.setMultiChoiceItems(
                providersTitles, initialChecked, (dialogInterface, i, b) -> checked[i] = b);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setCancelable(true);
        builder.setPositiveButton(
                actionString,
                (dialog, which) -> {
                    mDisposable.dispose();
                    mDisposable = new CompositeDisposable();

                    mDisposable.add(launchBackupRestore(0, null));
                });
        builder.setNeutralButton(
                choosePathString,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Intent dataToFileChooser = new Intent();
                            dataToFileChooser.setType("text/xml");
                            if (modeBackupRestore == R.id.backup_prefs) {
                                // create backup file in selected directory
                                dataToFileChooser.putExtra(
                                        Intent.EXTRA_TITLE,
                                        GlobalPrefsBackup.GLOBAL_BACKUP_FILENAME);
                            }
                            dataToFileChooser.setAction(actionCustomPath);
                            dataToFileChooser.putExtra("checked", checked);
                            try {
                                startActivityForResult(dataToFileChooser, 1);
                            } catch (ActivityNotFoundException e) {
                                Logger.e(TAG, "Could not launch the custom path activity");
                                Toast.makeText(
                                                getActivity().getApplicationContext(),
                                                R.string.toast_error_custom_path_backup,
                                                Toast.LENGTH_LONG)
                                        .show();
                            }

                        } else {
                            Intent intent = null;
                            if (optionId == R.id.backup_prefs) {
                                intent = new Intent(getContext(), FileExplorerCreate.class);
                            } else if (optionId == R.id.restore_prefs) {
                                intent = new Intent(getContext(), FileExplorerRestore.class);
                            }
                            startActivity(intent);
                        }
                    }
                });
    }

    private Disposable launchBackupRestore(int custom, Uri customUri) {
        File filePath;
        if (custom == 1) {
            if (customUri.getPath() != null) {
                // Uri won't show an absolute path, so better show only file name
                List<String> path = Arrays.asList(customUri.getPath().split("/"));
                filePath = new File(path.get(path.size() - 1));
            } else {
                filePath = new File(customUri.getPath());
            }
        } else filePath = GlobalPrefsBackup.getBackupFile();

        return RxProgressDialog.create(
                        new Pair<>(supportedProviders, checked),
                        getActivity(),
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
                            Logger.w(
                                    "MainFragment",
                                    e,
                                    "Failed to do operation due to %s",
                                    e.getMessage());
                            mDialogController.showDialog(failedDialog, e.getMessage());
                        },
                        () -> mDialogController.showDialog(successDialog, filePath));
    }

    public static void launchRestoreCustomFileData(InputStream inputStream) {
        PrefsXmlStorage.prefsXmlStorageCustomPath(inputStream);
    }

    public static void launchBackupCustomFileData(OutputStream outputStream) {
        PrefsXmlStorage.prefsXmlBackupCustomPath(outputStream);
    }

    // This function is if launched when selecting neutral button of the main Fragment
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getDataString() != null) {

            ContentResolver resolver = getContext().getContentResolver();
            Logger.d(TAG, "Resolver " + resolver.getType(data.getData()));
            try {
                // Actually, it is not a good idea to convert URI into filepath.
                // For more informations, see:
                // https://commonsware.com/blog/2016/03/15/how-consume-content-uri.html
                if (modeBackupRestore == R.id.restore_prefs) {
                    Logger.d(TAG, "Launching Restore at uri " + data.getData());
                    launchRestoreCustomFileData(resolver.openInputStream(data.getData()));
                } else if (modeBackupRestore == R.id.backup_prefs) {
                    Logger.d(TAG, "Launching Backup at uri " + data.getData());
                    launchBackupCustomFileData(resolver.openOutputStream(data.getData()));
                }
                launchBackupRestore(1, data.getData());
            } catch (Exception e) {
                e.printStackTrace();
                Logger.d(TAG, "Error when getting inputStream on onActivityResult");
            }
        }
    }

    private static class StoragePermissionRequest
            extends PermissionsRequest.PermissionsRequestBase {

        private final WeakReference<MainFragment> mFragmentWeakReference;
        private final int mOptionId;

        StoragePermissionRequest(MainFragment fragment, int optionId) {
            super(PermissionsRequestCodes.STORAGE.getRequestCode(), getPermissionsForOsVersion());
            mOptionId = optionId;
            mFragmentWeakReference = new WeakReference<>(fragment);
        }

        @NonNull
        private static String[] getPermissionsForOsVersion() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                return new String[] {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                };
            } else {
                return new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            }
        }

        @Override
        public void onPermissionsGranted() {
            MainFragment fragment = mFragmentWeakReference.get();
            if (fragment == null) return;

            fragment.mDialogController.showDialog(mOptionId);
        }

        @Override
        public void onPermissionsDenied(
                @NonNull String[] grantedPermissions,
                @NonNull String[] deniedPermissions,
                @NonNull String[] declinedPermissions) {
            /*no-op - Main-Activity handles this case*/
        }
    }
}
