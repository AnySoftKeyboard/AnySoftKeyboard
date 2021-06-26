package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import com.anysoftkeyboard.android.PermissionRequestHelper;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.nextword.NextWordDictionary;
import com.anysoftkeyboard.nextword.NextWordPrefsProvider;
import com.anysoftkeyboard.nextword.NextWordStatistics;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.prefs.backup.PrefsXmlStorage;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.utils.Triple;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import net.evendanan.pixel.GeneralDialogController;
import net.evendanan.pixel.RxProgressDialog;
import pub.devrel.easypermissions.AfterPermissionGranted;

public class NextWordSettingsFragment extends PreferenceFragmentCompat {

    private static final String ASK_NEXT_WORDS_FILENAME = "NextWords.xml";
    private static final int DIALOG_SAVE_SUCCESS = 10;
    private static final int DIALOG_SAVE_FAILED = 11;
    private static final int DIALOG_LOAD_SUCCESS = 20;
    private static final int DIALOG_LOAD_FAILED = 21;

    private GeneralDialogController mGeneralDialogController;

    @NonNull private CompositeDisposable mDisposable = new CompositeDisposable();

    private final Preference.OnPreferenceClickListener mClearDataListener =
            preference -> {
                mDisposable.add(
                        createDictionaryAddOnFragment(this)
                                .subscribeOn(RxSchedulers.background())
                                .map(
                                        pair -> {
                                            Context appContext =
                                                    pair.second
                                                            .requireContext()
                                                            .getApplicationContext();

                                            NextWordDictionary nextWordDictionary =
                                                    new NextWordDictionary(
                                                            appContext, pair.first.getLanguage());
                                            nextWordDictionary.load();
                                            nextWordDictionary.clearData();
                                            nextWordDictionary.close();

                                            return pair.second;
                                        })
                                .observeOn(RxSchedulers.mainThread())
                                .last(NextWordSettingsFragment.this)
                                .subscribe(
                                        NextWordSettingsFragment::loadUsageStatistics,
                                        t -> loadUsageStatistics()));
                return true;
            };

    private static Observable<Pair<DictionaryAddOnAndBuilder, NextWordSettingsFragment>>
            createDictionaryAddOnFragment(NextWordSettingsFragment fragment) {
        return Observable.fromIterable(
                        AnyApplication.getExternalDictionaryFactory(fragment.requireContext())
                                .getAllAddOns())
                .filter(addOn -> !TextUtils.isEmpty(addOn.getLanguage()))
                .distinct(DictionaryAddOnAndBuilder::getLanguage)
                .map(addOn -> Pair.create(addOn, fragment));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGeneralDialogController =
                new GeneralDialogController(
                        getActivity(),
                        (builder, optionId, data) -> {
                            builder.setPositiveButton(android.R.string.ok, null);
                            switch (optionId) {
                                case DIALOG_SAVE_SUCCESS:
                                    builder.setTitle(R.string.user_dict_backup_success_title);
                                    builder.setMessage(R.string.user_dict_backup_success_text);
                                    break;
                                case DIALOG_SAVE_FAILED:
                                    builder.setTitle(R.string.user_dict_backup_fail_title);
                                    builder.setMessage(
                                            getString(
                                                    R.string.user_dict_backup_fail_text_with_error,
                                                    data));
                                    break;
                                case DIALOG_LOAD_SUCCESS:
                                    builder.setTitle(R.string.user_dict_restore_success_title);
                                    builder.setMessage(R.string.user_dict_restore_success_text);
                                    break;
                                case DIALOG_LOAD_FAILED:
                                    builder.setTitle(R.string.user_dict_restore_fail_title);
                                    builder.setMessage(
                                            getString(
                                                    R.string.user_dict_restore_fail_text_with_error,
                                                    data));
                                    break;
                                default:
                                    throw new IllegalArgumentException(
                                            "Failed to handle "
                                                    + optionId
                                                    + " in NextWordSettingsFragment#onCreateDialog");
                            }
                        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_next_word);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        findPreference("clear_next_word_data").setOnPreferenceClickListener(mClearDataListener);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.next_word_menu_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        MainSettingsActivity mainSettingsActivity = (MainSettingsActivity) getActivity();
        if (mainSettingsActivity == null) return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.backup_words:
                doNextWordBackup();
                return true;
            case R.id.restore_words:
                doNextWordRestore();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.next_word_dict_settings));
        loadUsageStatistics();
    }

    private void loadUsageStatistics() {
        findPreference("clear_next_word_data").setEnabled(false);
        ((PreferenceCategory) findPreference("next_word_stats")).removeAll();

        mDisposable.add(
                createDictionaryAddOnFragment(this)
                        .subscribeOn(RxSchedulers.background())
                        .map(
                                pair -> {
                                    NextWordDictionary nextWordDictionary =
                                            new NextWordDictionary(
                                                    pair.second.getContext(),
                                                    pair.first.getLanguage());
                                    nextWordDictionary.load();
                                    return Triple.create(
                                            pair.second,
                                            pair.first,
                                            nextWordDictionary.dumpDictionaryStatistics());
                                })
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(
                                triple -> {
                                    final FragmentActivity activity =
                                            triple.getFirst().requireActivity();
                                    Preference localeData = new Preference(activity);
                                    final DictionaryAddOnAndBuilder addOn = triple.getSecond();
                                    localeData.setKey(addOn.getLanguage() + "_stats");
                                    localeData.setTitle(
                                            addOn.getLanguage() + " - " + addOn.getName());
                                    final NextWordStatistics statistics = triple.getThird();
                                    if (statistics.firstWordCount == 0) {
                                        localeData.setSummary(
                                                R.string.next_words_statistics_no_usage);
                                    } else {
                                        localeData.setSummary(
                                                activity.getString(
                                                        R.string.next_words_statistics_count,
                                                        statistics.firstWordCount,
                                                        statistics.secondWordCount
                                                                / statistics.firstWordCount));
                                    }
                                    localeData.setPersistent(false);

                                    ((PreferenceCategory)
                                                    triple.getFirst()
                                                            .findPreference("next_word_stats"))
                                            .addPreference(localeData);
                                },
                                t -> {},
                                () -> findPreference("clear_next_word_data").setEnabled(true)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGeneralDialogController.dismiss();
        mDisposable.dispose();
    }

    @AfterPermissionGranted(PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE)
    public void doNextWordBackup() {
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();

        if (PermissionRequestHelper.check(
                this, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_WRITE_CODE)) {
            PrefsXmlStorage storage =
                    new PrefsXmlStorage(
                            AnyApplication.getBackupFile(
                                    requireContext(), ASK_NEXT_WORDS_FILENAME));
            NextWordPrefsProvider provider =
                    new NextWordPrefsProvider(
                            getContext(),
                            ExternalDictionaryFactory.getLocalesFromDictionaryAddOns(
                                    requireContext()));

            mDisposable.add(
                    RxProgressDialog.create(
                                    Pair.create(storage, provider),
                                    requireActivity(),
                                    getString(R.string.take_a_while_progress_message),
                                    R.layout.progress_window)
                            .subscribeOn(RxSchedulers.background())
                            .map(
                                    pair -> {
                                        final PrefsRoot prefsRoot = pair.second.getPrefsRoot();
                                        pair.first.store(prefsRoot);

                                        return Boolean.TRUE;
                                    })
                            .observeOn(RxSchedulers.mainThread())
                            .subscribe(
                                    o -> mGeneralDialogController.showDialog(DIALOG_SAVE_SUCCESS),
                                    throwable ->
                                            mGeneralDialogController.showDialog(
                                                    DIALOG_SAVE_FAILED, throwable.getMessage()),
                                    this::loadUsageStatistics));
        }
    }

    @AfterPermissionGranted(PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE)
    public void doNextWordRestore() {
        mDisposable.dispose();
        mDisposable = new CompositeDisposable();

        if (PermissionRequestHelper.check(
                this, PermissionRequestHelper.STORAGE_PERMISSION_REQUEST_READ_CODE)) {
            PrefsXmlStorage storage =
                    new PrefsXmlStorage(
                            AnyApplication.getBackupFile(
                                    requireContext(), ASK_NEXT_WORDS_FILENAME));
            NextWordPrefsProvider provider =
                    new NextWordPrefsProvider(
                            getContext(),
                            ExternalDictionaryFactory.getLocalesFromDictionaryAddOns(
                                    requireContext()));

            mDisposable.add(
                    RxProgressDialog.create(
                                    Pair.create(storage, provider),
                                    requireActivity(),
                                    getString(R.string.take_a_while_progress_message),
                                    R.layout.progress_window)
                            .subscribeOn(RxSchedulers.background())
                            .map(
                                    pair -> {
                                        final PrefsRoot prefsRoot = pair.first.load();
                                        pair.second.storePrefsRoot(prefsRoot);
                                        return Boolean.TRUE;
                                    })
                            .observeOn(RxSchedulers.mainThread())
                            .subscribe(
                                    o -> mGeneralDialogController.showDialog(DIALOG_LOAD_SUCCESS),
                                    throwable ->
                                            mGeneralDialogController.showDialog(
                                                    DIALOG_LOAD_FAILED, throwable.getMessage()),
                                    this::loadUsageStatistics));
        }
    }

    @SuppressWarnings("deprecation") // needed for permissions flow
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequestHelper.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }
}
