package com.anysoftkeyboard.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.view.View;

import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.nextword.NextWordDictionary;
import com.anysoftkeyboard.nextword.NextWordStatistics;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.utils.Triple;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

public class NextWordSettingsFragment extends PreferenceFragmentCompat {

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    private final Preference.OnPreferenceClickListener mClearDataListener = preference -> {
        mDisposable.add(createDictionaryAddOnFragment(this)
                .subscribeOn(RxSchedulers.background())
                .map(pair -> {
                    Context appContext = pair.second.getContext().getApplicationContext();

                    NextWordDictionary nextWordDictionary = new NextWordDictionary(appContext, pair.first.getLanguage());
                    nextWordDictionary.load();
                    nextWordDictionary.clearData();
                    nextWordDictionary.close();

                    return pair.second;
                })
                .observeOn(RxSchedulers.mainThread())
                .last(NextWordSettingsFragment.this)
                .subscribe(NextWordSettingsFragment::loadUsageStatistics, t -> loadUsageStatistics()));
        return true;
    };

    private static Observable<Pair<DictionaryAddOnAndBuilder, NextWordSettingsFragment>> createDictionaryAddOnFragment(NextWordSettingsFragment fragment) {
        return Observable.fromIterable(AnyApplication.getExternalDictionaryFactory(fragment.getContext()).getAllAddOns())
                .filter(addOn -> !TextUtils.isEmpty(addOn.getLanguage()))
                .distinct(DictionaryAddOnAndBuilder::getLanguage)
                .map(addOn -> Pair.create(addOn, fragment));
    }


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

    private void loadUsageStatistics() {
        findPreference("clear_next_word_data").setEnabled(false);
        ((PreferenceCategory) findPreference("next_word_stats")).removeAll();

        mDisposable.add(createDictionaryAddOnFragment(this)
                .subscribeOn(RxSchedulers.background())
                .map(pair -> {
                    NextWordDictionary nextWordDictionary = new NextWordDictionary(pair.second.getContext(), pair.first.getLanguage());
                    nextWordDictionary.load();
                    return Triple.create(pair.second, pair.first, nextWordDictionary.dumpDictionaryStatistics());
                })
                .observeOn(RxSchedulers.mainThread())
                .subscribe(triple -> {
                    final FragmentActivity activity = triple.getFirst().getActivity();
                    Preference localeData = new Preference(activity);
                    final DictionaryAddOnAndBuilder addOn = triple.getSecond();
                    localeData.setKey(addOn.getLanguage() + "_stats");
                    localeData.setTitle(addOn.getLanguage() + " - " + addOn.getName());
                    final NextWordStatistics statistics = triple.getThird();
                    if (statistics.firstWordCount == 0) {
                        localeData.setSummary(R.string.next_words_statistics_no_usage);
                    } else {
                        localeData.setSummary(activity.getString(R.string.next_words_statistics_count,
                                statistics.firstWordCount,
                                statistics.secondWordCount / statistics.firstWordCount));
                    }
                    localeData.setPersistent(false);

                    ((PreferenceCategory) triple.getFirst().findPreference("next_word_stats")).addPreference(localeData);
                }, t -> {
                }, () -> findPreference("clear_next_word_data").setEnabled(true)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
    }
}
