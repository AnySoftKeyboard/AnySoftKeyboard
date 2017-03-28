package com.menny.android.anysoftkeyboard;

import android.os.AsyncTask;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.concurrent.Executor;

@Implements(value = AsyncTask.class)
public class AsyncTaskShadow<I, P, R> extends org.robolectric.shadows.ShadowAsyncTask<I, P, R> {

    @RealObject
    private AsyncTask<I, P, R> mRealAsyncTask;

    @Implementation
    public AsyncTask<I, P, R> executeOnExecutor(Executor executor, I... params) {
        //due to some weird Robolectric issue with thread-safety. I'm routing the task to the single executor
        return super.execute(params);
    }
}
