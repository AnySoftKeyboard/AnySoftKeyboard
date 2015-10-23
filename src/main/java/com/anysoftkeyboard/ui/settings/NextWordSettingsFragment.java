package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.v4.preference.PreferenceFragment;
import android.text.TextUtils;

import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.nextword.NextWordDictionary;
import com.anysoftkeyboard.nextword.NextWordStatistics;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow;
import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow.AsyncTaskOwner;
import net.evendanan.pushingpixels.PassengerFragmentSupport;

import java.util.ArrayList;
import java.util.List;

public class NextWordSettingsFragment extends PreferenceFragment implements AsyncTaskOwner {

    private AsyncTask<Void, ProgressReport, List<String>> mNextWordStatsLoader;
    private final Preference.OnPreferenceClickListener mClearDataListener = new Preference.OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            new AsyncTaskWithProgressWindow<Void, Void, Void, NextWordSettingsFragment>(NextWordSettingsFragment.this, true) {

                @Override
                protected void applyResults(Void o, Exception backgroundException) {
                    loadUsageStatistics();
                }

                @Override
                protected Void doAsyncTask(Void[] params) throws Exception {
                    Context appContext = getActivity().getApplicationContext();
                    for (String locale : mDeviceLocales) {
                        NextWordDictionary nextWordDictionary = new NextWordDictionary(appContext, locale);
                        nextWordDictionary.clearData();
                    }
                    return null;
                }
            }.execute();
            return true;
        }
    };
    private List<String> mDeviceLocales;

    private static class ProgressReport {
        public final DictionaryAddOnAndBuilder dictionaryBuilderByLocale;
        public final NextWordStatistics nextWordStatistics;

        public ProgressReport(DictionaryAddOnAndBuilder dictionaryBuilderByLocale, NextWordStatistics nextWordStatistics) {
            this.dictionaryBuilderByLocale = dictionaryBuilderByLocale;
            this.nextWordStatistics = nextWordStatistics;
        }
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.prefs_next_word);
        findPreference("clear_next_word_data").setOnPreferenceClickListener(mClearDataListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        PassengerFragmentSupport.setActivityTitle(this, getString(R.string.next_word_dict_settings));
        loadUsageStatistics();
    }

    protected void loadUsageStatistics() {
        mNextWordStatsLoader = new AsyncTask<Void, ProgressReport, List<String>>() {

            private PreferenceCategory mStatsCategory;
            private Context mApplicationContext;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mApplicationContext = getActivity().getApplicationContext();
                mStatsCategory = (PreferenceCategory) findPreference("next_word_stats");
                mStatsCategory.removeAll();
                findPreference("clear_next_word_data").setEnabled(false);
            }

            @Override
            protected List<String> doInBackground(Void... params) {
                final List<DictionaryAddOnAndBuilder> dictionaries = ExternalDictionaryFactory.getAllAvailableExternalDictionaries(mApplicationContext);
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
                    final DictionaryAddOnAndBuilder dictionaryBuilderByLocale = ExternalDictionaryFactory.getDictionaryBuilderByLocale(locale, mApplicationContext);
                    NextWordDictionary nextWordDictionary = new NextWordDictionary(mApplicationContext, dictionaryBuilderByLocale.getLanguage());
                    nextWordDictionary.loadFromStorage();
                    if (isCancelled()) return null;
                    publishProgress(new ProgressReport(dictionaryBuilderByLocale, nextWordDictionary.dumpDictionaryStatistics()));
                }

                return deviceLocales;
            }

            @Override
            protected void onProgressUpdate(ProgressReport... values) {
                super.onProgressUpdate(values);
                if (isCancelled()) return;
                for (ProgressReport progressReport : values) {
                    Preference localeData = new Preference(getActivity());
                    localeData.setKey(progressReport.dictionaryBuilderByLocale.getLanguage());
                    localeData.setTitle(progressReport.dictionaryBuilderByLocale.getLanguage() + " - " + progressReport.dictionaryBuilderByLocale.getName());
                    if (progressReport.nextWordStatistics.firstWordCount == 0) {
                        localeData.setSummary(R.string.next_words_statistics_no_usage);
                    } else {
                        localeData.setSummary(getString(R.string.next_words_statistics_count,
                                progressReport.nextWordStatistics.firstWordCount,
                                progressReport.nextWordStatistics.secondWordCount / progressReport.nextWordStatistics.firstWordCount));
                    }
                    localeData.setPersistent(false);

                    mStatsCategory.addPreference(localeData);
                }
            }

            @Override
            protected void onPostExecute(List<String> deviceLocale) {
                super.onPostExecute(deviceLocale);
                findPreference("clear_next_word_data").setEnabled(true);
                mDeviceLocales = deviceLocale;
            }
        }.execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        mNextWordStatsLoader.cancel(false);
    }
}
