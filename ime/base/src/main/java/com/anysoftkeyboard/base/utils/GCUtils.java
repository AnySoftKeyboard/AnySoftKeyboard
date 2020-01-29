package com.anysoftkeyboard.base.utils;

import android.support.annotation.VisibleForTesting;
import android.text.format.DateUtils;
import android.util.Log;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class GCUtils {
    @VisibleForTesting static final int GC_TRY_LOOP_MAX = 5;
    private static final long GC_INTERVAL = DateUtils.SECOND_IN_MILLIS;
    private static final GCUtils sInstance = new GCUtils();

    public static GCUtils getInstance() {
        return sInstance;
    }

    @VisibleForTesting
    /*package*/ GCUtils() {}

    public void performOperationWithMemRetry(final String tag, MemRelatedOperation operation) {
        int retryCount = GC_TRY_LOOP_MAX;
        while (true) {
            try {
                operation.operation();
                return;
            } catch (OutOfMemoryError e) {
                if (retryCount == 0) throw e;

                retryCount--;
                Log.w(tag, "WOW! No memory for operation... I'll try to release some.");
                doGarbageCollection(tag);
            }
        }
    }

    @SuppressFBWarnings("DM_GC")
    @SuppressWarnings("PMD.DoNotCallGarbageCollectionExplicitly")
    @VisibleForTesting
    void doGarbageCollection(final String tag) {
        System.gc();
        try {
            Thread.sleep(GC_INTERVAL);
        } catch (InterruptedException e) {
            Log.e(tag, "Sleep was interrupted.");
        }
    }

    public interface MemRelatedOperation {
        void operation();
    }
}
