package com.anysoftkeyboard.base.utils;

import android.text.format.DateUtils;
import android.util.Log;

public class GCUtils {
    private static final int GC_TRY_COUNT = 2;
    // GC_TRY_LOOP_MAX is used for the hard limit of GC wait,
    // GC_TRY_LOOP_MAX should be greater than GC_TRY_COUNT.
    private static final int GC_TRY_LOOP_MAX = 5;
    private static final long GC_INTERVAL = DateUtils.SECOND_IN_MILLIS;
    private static final GCUtils sInstance = new GCUtils();
    private int mGCTryCount = 0;

    public static GCUtils getInstance() {
        return sInstance;
    }

    public boolean performOperationWithMemRetry(String TAG, MemRelatedOperation operation, boolean failWithException) {
        reset();

        boolean retry = true;
        while (retry) {
            try {
                operation.operation();
                return true;
            } catch (OutOfMemoryError e) {
                Log.w(TAG, "WOW! No memory for operation... I'll try to release some.");
                retry = tryGCOrWait(TAG, e);
                if (!retry && failWithException) throw e;
            }
        }
        return false;
    }

    private void reset() {
        mGCTryCount = 0;
    }

    private boolean tryGCOrWait(String metaData, Throwable t) {
        if (mGCTryCount % GC_TRY_COUNT == 0) {
            System.gc();
        }
        if (mGCTryCount > GC_TRY_LOOP_MAX) {
            return false;
        } else {
            mGCTryCount++;
            try {
                Thread.sleep(GC_INTERVAL);
                return true;
            } catch (InterruptedException e) {
                Log.e(metaData, "Sleep was interrupted.");
                //ImeLogger.logOnException(metaData, t);
                return false;
            }
        }
    }

    public interface MemRelatedOperation {
        void operation();
    }
}
