package com.anysoftkeyboard.prefs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.Pair;
import android.support.v7.preference.PreferenceManager;

import com.anysoftkeyboard.prefs.backup.PrefItem;
import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;
import com.anysoftkeyboard.prefs.backup.PrefsXmlStorage;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class GlobalPrefsBackup {
    @VisibleForTesting
    static final String GLOBAL_BACKUP_FILENAME = "AnySoftKeyboardPrefs.xml";

    public static List<ProviderDetails> getAllPrefsProviders(@NonNull Context context) {
        return Arrays.asList(
                new ProviderDetails(
                        new RxSharedPrefs.SharedPrefsProvider(PreferenceManager.getDefaultSharedPreferences(context)),
                        R.string.shared_prefs_provider_name));
    }


    private static Boolean backupProvider(PrefsProvider provider, PrefsRoot prefsRoot) {
        final PrefsRoot providerRoot = provider.getPrefsRoot();
        prefsRoot.createChild()
                .addValue("providerId", provider.providerId())
                .addValue("version", Integer.toString(providerRoot.getVersion()))
                .addChild(providerRoot);

        return Boolean.TRUE;
    }

    private static Boolean restoreProvider(PrefsProvider provider, PrefsRoot prefsRoot) {
        Observable.fromIterable(prefsRoot.getChildren())
                .filter(prefItem -> provider.providerId().equals(prefItem.getValue("providerId")))
                .blockingSubscribe(providerPrefItem -> {
                    PrefsRoot prefsRootForProvider = new PrefsRoot(Integer.parseInt(providerPrefItem.getValue("version")));
                    final PrefItem actualPrefRoot = providerPrefItem.getChildren().iterator().next();
                    for (Map.Entry<String, String> attribute : actualPrefRoot.getValues()) {
                        prefsRootForProvider.addValue(attribute.getKey(), attribute.getValue());
                    }
                    for (PrefItem child : actualPrefRoot.getChildren()) {
                        prefsRootForProvider.addChild(child);
                    }

                    provider.storePrefsRoot(prefsRootForProvider);
                });

        return Boolean.TRUE;
    }

    @NonNull
    public static Observable<Boolean> backup(Observable<Pair<List<ProviderDetails>, Boolean[]>> enabledProvidersObservable) {
        return doIt(enabledProvidersObservable, GlobalPrefsBackup::backupProvider, s -> new PrefsRoot(1), PrefsXmlStorage::store);
    }

    @NonNull
    public static Observable<Boolean> restore(Observable<Pair<List<ProviderDetails>, Boolean[]>> enabledProvidersObservable) {
        return doIt(enabledProvidersObservable, GlobalPrefsBackup::restoreProvider, PrefsXmlStorage::load, (s, p) -> { /*no-op*/ });
    }

    @NonNull
    private static Observable<Boolean> doIt(
            Observable<Pair<List<ProviderDetails>, Boolean[]>> enabledProvidersObservable,
            BiFunction<PrefsProvider, PrefsRoot, Boolean> providerAction,
            Function<PrefsXmlStorage, PrefsRoot> prefsRootFactory,
            BiConsumer<PrefsXmlStorage, PrefsRoot> prefsRootFinalizer) {

        final Observable<PrefsProvider> providersObservable = enabledProvidersObservable
                .flatMap((Function<Pair<List<ProviderDetails>, Boolean[]>, ObservableSource<Pair<ProviderDetails, Boolean>>>) pair -> Observable.zip(
                        Observable.fromIterable(pair.first),
                        Observable.fromArray(pair.second),
                        Pair::new
                ))
                .filter(pair -> pair.second)
                .map(pair -> pair.first.provider);

        final PrefsXmlStorage storage = new PrefsXmlStorage(AnyApplication.getBackupFile(GLOBAL_BACKUP_FILENAME));

        return Observable.using(() -> prefsRootFactory.apply(storage),
                prefsRoot -> providersObservable.map(provider -> providerAction.apply(provider, prefsRoot)),
                prefsRoot -> prefsRootFinalizer.accept(storage, prefsRoot));
    }

    public static class ProviderDetails {
        public final PrefsProvider provider;
        @StringRes
        public final int providerTitle;

        @VisibleForTesting
        ProviderDetails(PrefsProvider provider, @StringRes int providerTitle) {
            this.provider = provider;
            this.providerTitle = providerTitle;
        }
    }
}
