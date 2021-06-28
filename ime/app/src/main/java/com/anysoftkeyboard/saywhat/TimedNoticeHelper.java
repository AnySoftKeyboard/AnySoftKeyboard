package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.f2prateek.rx.preferences2.Preference;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class TimedNoticeHelper {
    public interface NextTimeProvider {
        long getNextTimeOffset(int timesShown);
    }

    private final Preference<String> mDataHolder;
    private final Preference<Integer> mTimesShownHolder;
    private final NextTimeProvider mNextTimeInMilliSecondsProvider;
    private long mNextTimeToShow;

    public TimedNoticeHelper(
            @NonNull Context context, @StringRes int prefKey, long timeBetweenShows) {
        this(context, prefKey, timesShown -> timeBetweenShows);
    }

    public TimedNoticeHelper(
            @NonNull Context context,
            @StringRes int prefKey,
            @NonNull NextTimeProvider timeProvider) {
        mNextTimeInMilliSecondsProvider = timeProvider;
        final String prefKeyTime = context.getString(prefKey);
        final String prefKeyShown = context.getString(prefKey) + "_SHOWN_TIMES";
        mDataHolder =
                AnyApplication.prefs(context)
                        .getString(
                                prefKeyTime,
                                R.string.settings_default_notice_never_before_seen_value);
        mTimesShownHolder =
                AnyApplication.prefs(context)
                        .getInteger(
                                prefKeyShown,
                                R.integer.settings_default_notice_never_before_seen_times_value);
        mNextTimeToShow = Long.parseLong(mDataHolder.get());
    }

    public boolean shouldShow() {
        return SystemClock.elapsedRealtime() >= mNextTimeToShow;
    }

    public void markAsShown() {
        final int timesShown = mTimesShownHolder.get();
        mTimesShownHolder.set(timesShown + 1);
        mNextTimeToShow =
                SystemClock.elapsedRealtime()
                        + mNextTimeInMilliSecondsProvider.getNextTimeOffset(timesShown);
        mDataHolder.set(Long.toString(mNextTimeToShow));
    }
}
