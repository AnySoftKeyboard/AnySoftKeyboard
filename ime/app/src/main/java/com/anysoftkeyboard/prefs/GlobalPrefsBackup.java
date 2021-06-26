package com.anysoftkeyboard.prefs;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.dictionaries.prefsprovider.UserDictionaryPrefsProvider;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.WordsSQLiteConnectionPrefsProvider;
import com.anysoftkeyboard.nextword.NextWordPrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.prefs.backup.PrefsXmlStorage;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GlobalPrefsBackup {
    public static final String GLOBAL_BACKUP_FILENAME = "AnySoftKeyboardPrefs.xml";

    private static File customFilename = null;

    public static List<ProviderDetails> getAllPrefsProviders(@NonNull Context context) {
        return Arrays.asList(
                new ProviderDetails(
                        new RxSharedPrefs.SharedPrefsProvider(
                                PreferenceManager.getDefaultSharedPreferences(context)),
                        R.string.shared_prefs_provider_name),
                new ProviderDetails(
                        new UserDictionaryPrefsProvider(context),
                        R.string.user_dict_prefs_provider),
                new ProviderDetails(
                        new NextWordPrefsProvider(
                                context,
                                ExternalDictionaryFactory.getLocalesFromDictionaryAddOns(context)),
                        R.string.next_word_dict_prefs_provider),
                new ProviderDetails(
                        new WordsSQLiteConnectionPrefsProvider(
                                context, AbbreviationsDictionary.ABBREVIATIONS_DB),
                        R.string.abbreviation_dict_prefs_provider));
    }

    private static Boolean backupProvider(PrefsProvider provider, PrefsRoot prefsRoot) {
        final PrefsRoot providerRoot = provider.getPrefsRoot();
        prefsRoot
                .createChild()
                .addValue("providerId", provider.providerId())
                .addValue("version", Integer.toString(providerRoot.getVersion()))
                .addChild(providerRoot);

        return Boolean.TRUE;
    }

    private static Boolean restoreProvider(PrefsProvider provider, PrefsRoot prefsRoot) {
        Observable.fromIterable(prefsRoot.getChildren())
                .filter(prefItem -> provider.providerId().equals(prefItem.getValue("providerId")))
                .blockingSubscribe(
                        providerPrefItem -> {
                            PrefsRoot prefsRootForProvider =
                                    new PrefsRoot(
                                            Integer.parseInt(providerPrefItem.getValue("version")));
                            final PrefItem actualPrefRoot =
                                    providerPrefItem.getChildren().iterator().next();
                            for (Map.Entry<String, String> attribute : actualPrefRoot.getValues()) {
                                prefsRootForProvider.addValue(
                                        attribute.getKey(), attribute.getValue());
                            }
                            for (PrefItem child : actualPrefRoot.getChildren()) {
                                prefsRootForProvider.addChild(child);
                            }

                            provider.storePrefsRoot(prefsRootForProvider);
                        });

        return Boolean.TRUE;
    }

    @NonNull
    public static Observable<ProviderDetails> backup(
            @NonNull Context context, Pair<List<ProviderDetails>, Boolean[]> enabledProviders) {
        return doIt(
                context,
                enabledProviders,
                s -> new PrefsRoot(1),
                GlobalPrefsBackup::backupProvider,
                PrefsXmlStorage::store);
    }

    @NonNull
    public static Observable<ProviderDetails> restore(
            @NonNull Context context, Pair<List<ProviderDetails>, Boolean[]> enabledProviders) {
        return doIt(
                context,
                enabledProviders,
                PrefsXmlStorage::load,
                GlobalPrefsBackup::restoreProvider,
                (s, p) -> {
                    /*no-op*/
                });
    }

    @NonNull
    public static Observable<ProviderDetails> doIt(
            @NonNull Context context,
            Pair<List<ProviderDetails>, Boolean[]> enabledProviders,
            Function<PrefsXmlStorage, PrefsRoot> prefsRootFactory,
            BiConsumer<PrefsProvider, PrefsRoot> providerAction,
            BiConsumer<PrefsXmlStorage, PrefsRoot> prefsRootFinalizer) {

        final Observable<ProviderDetails> providersObservable =
                Observable.zip(
                                Observable.fromIterable(enabledProviders.first),
                                Observable.fromArray(enabledProviders.second),
                                Pair::new)
                        .filter(pair -> pair.second)
                        .map(pair -> pair.first);

        final PrefsXmlStorage storage = new PrefsXmlStorage(getBackupFile(context));

        return Observable.using(
                () -> prefsRootFactory.apply(storage),
                prefsRoot ->
                        providersObservable.map(
                                providerDetails -> {
                                    providerAction.accept(providerDetails.provider, prefsRoot);
                                    return providerDetails;
                                }),
                prefsRoot -> prefsRootFinalizer.accept(storage, prefsRoot));
    }

    public static void updateCustomFilename(File filename) {
        customFilename = filename;
    }

    public static File getBackupFile(@NonNull Context context) {
        File tempFilename;

        if (customFilename == null) {
            return AnyApplication.getBackupFile(context, GLOBAL_BACKUP_FILENAME);
        } else {
            // We reset the customFilename
            tempFilename = customFilename;
            customFilename = null;
            return tempFilename;
        }
    }

    public static class ProviderDetails {
        public final PrefsProvider provider;
        @StringRes public final int providerTitle;

        @VisibleForTesting
        ProviderDetails(PrefsProvider provider, @StringRes int providerTitle) {
            this.provider = provider;
            this.providerTitle = providerTitle;
        }
    }
}
