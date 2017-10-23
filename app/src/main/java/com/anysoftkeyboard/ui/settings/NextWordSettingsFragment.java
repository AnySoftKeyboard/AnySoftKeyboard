package com.anysoftkeyboard.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.View;

import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.nextword.NextWordDictionary;
import com.anysoftkeyboard.nextword.NextWordStatistics;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow;
import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow.AsyncTaskOwner;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NextWordSettingsFragment extends PreferenceFragmentCompat implements AsyncTaskOwner {

    private AsyncTask<Void, ProgressReport, List<String>> mNextWordStatsLoader;
    private List<String> mDeviceLocales;
    private final Preference.OnPreferenceClickListener mClearDataListener = preference -> {
        new ClearDataAsyncTask(NextWordSettingsFragment.this).execute();
        return true;
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.prefs_next_word);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference("clear_next_word_data").setOnPreferenceClickListener(mClearDataListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        MainSettingsActivity.setActivityTitle(this, getString(R.string.next_word_dict_settings));
        loadUsageStatistics();
    }

    protected void loadUsageStatistics() {
        mNextWordStatsLoader = new LoadUsageStatisticsAsyncTask(this);
        AsyncTaskCompat.executeParallel(mNextWordStatsLoader);
    }

    @Override
    public void onStop() {
        super.onStop();
        mNextWordStatsLoader.cancel(false);
    }

    private static class ProgressReport {
        final DictionaryAddOnAndBuilder dictionaryBuilderByLocale;
        final NextWordStatistics nextWordStatistics;

        ProgressReport(DictionaryAddOnAndBuilder dictionaryBuilderByLocale, NextWordStatistics nextWordStatistics) {
            this.dictionaryBuilderByLocale = dictionaryBuilderByLocale;
            this.nextWordStatistics = nextWordStatistics;
        }
    }

    private static class LoadUsageStatisticsAsyncTask extends AsyncTask<Void, ProgressReport, List<String>> {

        private final WeakReference<NextWordSettingsFragment> mOwningFragment;
        private final WeakReference<Context> mApplicationContext;

        LoadUsageStatisticsAsyncTask(NextWordSettingsFragment fragment) {
            mOwningFragment = new WeakReference<>(fragment);
            mApplicationContext = new WeakReference<>(fragment.getContext().getApplicationContext());
            fragment.findPreference("clear_next_word_data").setEnabled(false);
            ((PreferenceCategory) fragment.findPreference("next_word_stats")).removeAll();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            final Context context = mApplicationContext.get();
            if (context == null) return Collections.emptyList();

            final List<DictionaryAddOnAndBuilder> dictionaries = AnyApplication.getExternalDictionaryFactory(context).getAllAddOns();
            final List<String> deviceLocales = new ArrayList<>();
            for (DictionaryAddOnAndBuilder builder : dictionaries) {
                if (isCancelled()) return null;
                if (TextUtils.isEmpty(builder.getLanguage()) || TextUtils.isEmpty(builder.getName()))
                    continue;
                if (deviceLocales.contains(builder.getLanguage())) continue;
                deviceLocales.add(builder.getLanguage());
            }

            for (String locale : deviceLocales) {
                if (isCancelled()) return null;
                final DictionaryAddOnAndBuilder dictionaryBuilderByLocale = AnyApplication.getExternalDictionaryFactory(context).getDictionaryBuilderByLocale(locale);
                NextWordDictionary nextWordDictionary = new NextWordDictionary(context, dictionaryBuilderByLocale.getLanguage());
                nextWordDictionary.load();
                if (isCancelled()) return null;
                publishProgress(new ProgressReport(dictionaryBuilderByLocale, nextWordDictionary.dumpDictionaryStatistics()));
            }

            return deviceLocales;
        }

        @Override
        protected void onProgressUpdate(ProgressReport... values) {
            super.onProgressUpdate(values);
            PreferenceFragmentCompat fragment = mOwningFragment.get();
            if (isCancelled() || fragment == null) return;
            Activity activity = fragment.getActivity();
            for (ProgressReport progressReport : values) {
                Preference localeData = new Preference(activity);
                localeData.setKey(progressReport.dictionaryBuilderByLocale.getLanguage());
                localeData.setTitle(progressReport.dictionaryBuilderByLocale.getLanguage() + " - " + progressReport.dictionaryBuilderByLocale.getName());
                if (progressReport.nextWordStatistics.firstWordCount == 0) {
                    localeData.setSummary(R.string.next_words_statistics_no_usage);
                } else {
                    localeData.setSummary(activity.getString(R.string.next_words_statistics_count,
                            progressReport.nextWordStatistics.firstWordCount,
                            progressReport.nextWordStatistics.secondWordCount / progressReport.nextWordStatistics.firstWordCount));
                }
                localeData.setPersistent(false);

                ((PreferenceCategory) fragment.findPreference("next_word_stats")).addPreference(localeData);
            }
        }

        @Override
        protected void onPostExecute(List<String> deviceLocale) {
            super.onPostExecute(deviceLocale);
            NextWordSettingsFragment fragment = mOwningFragment.get();
            if (isCancelled() || fragment == null) return;
            fragment.findPreference("clear_next_word_data").setEnabled(true);
            fragment.mDeviceLocales = deviceLocale;
        }
    }

    private static class ClearDataAsyncTask extends AsyncTaskWithProgressWindow<Void, Void, Void, NextWordSettingsFragment> {

        ClearDataAsyncTask(NextWordSettingsFragment fragment) {
            super(fragment, true);
        }

        @Override
        protected void applyResults(Void o, Exception backgroundException) {
            NextWordSettingsFragment fragment = getOwner();
            if (fragment == null) return;

            fragment.loadUsageStatistics();
        }

        @Override
        protected Void doAsyncTask(Void[] params) throws Exception {
            NextWordSettingsFragment fragment = getOwner();
            if (fragment == null) return null;

            Context appContext = fragment.getContext().getApplicationContext();
            for (String locale : fragment.mDeviceLocales) {
                NextWordDictionary nextWordDictionary = new NextWordDictionary(appContext, locale);
                nextWordDictionary.load();
                nextWordDictionary.clearData();
                nextWordDictionary.close();
            }
            return null;
        }
    }
}
