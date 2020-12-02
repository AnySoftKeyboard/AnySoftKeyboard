package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.f2prateek.rx.preferences2.Preference;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class TimedNoticeHelper {
    public interface NextTimeProvider {
        long getNextTimeOffset();
    }

    private final Preference<String> mDataHolder;
    private final NextTimeProvider mNextTimeInMilliSecondsProvider;
    private long mNextTimeToShow;

    public TimedNoticeHelper(
            @NonNull Context context, @StringRes int prefKey, long timeBetweenShows) {
        this(context, prefKey, () -> timeBetweenShows);
    }

    public TimedNoticeHelper(
            @NonNull Context context,
            @StringRes int prefKey,
            @NonNull NextTimeProvider timeProvider) {
        mNextTimeInMilliSecondsProvider = timeProvider;
        mDataHolder =
                AnyApplication.prefs(context)
                        .getString(
                                prefKey, R.string.settings_default_notice_never_before_seen_value);
        mNextTimeToShow = Long.parseLong(mDataHolder.get());
    }

    public boolean shouldShow() {
        return SystemClock.elapsedRealtime() >= mNextTimeToShow;
    }

    public void markAsShown() {
        mNextTimeToShow =
                SystemClock.elapsedRealtime() + mNextTimeInMilliSecondsProvider.getNextTimeOffset();
        mDataHolder.set(Long.toString(mNextTimeToShow));
    }
}
