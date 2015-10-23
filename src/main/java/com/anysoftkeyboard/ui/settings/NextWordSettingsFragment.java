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

import net.evendanan.pushingpixels.PassengerFragmentSupport;

import java.util.ArrayList;
import java.util.List;

public class NextWordSettingsFragment extends PreferenceFragment {

    private AsyncTask<Void, ProgressReport, Void> mNextWordStatsLoader;

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
    }

    @Override
    public void onStart() {
        super.onStart();
        PassengerFragmentSupport.setActivityTitle(this, getString(R.string.next_word_dict_settings));
        mNextWordStatsLoader = new AsyncTask<Void, ProgressReport, Void>() {

            private PreferenceCategory mStatsCategory;
            private Context mApplicationContext;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                mApplicationContext = getActivity().getApplicationContext();
                mStatsCategory = (PreferenceCategory) findPreference("next_word_stats");
            }

            @Override
            protected Void doInBackground(Void... params) {
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

                return null;
            }

            @Override
            protected void onProgressUpdate(ProgressReport... values) {
                super.onProgressUpdate(values);
                if (isCancelled()) return;
                for (ProgressReport progressReport : values) {
                    Preference localeData = new Preference(getActivity());
                    localeData.setKey("stats_" + progressReport.dictionaryBuilderByLocale.getLanguage());
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
        }.execute();
    }

    @Override
    public void onStop() {
        super.onStop();
        mNextWordStatsLoader.cancel(false);
    }
}
