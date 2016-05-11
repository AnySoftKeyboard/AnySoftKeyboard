package com.menny.android.anysoftkeyboard;

import android.os.AsyncTask;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.concurrent.Executor;

@Implements(value = AsyncTask.class)
public class AsyncTaskShadow<Params, Progress, Result> extends org.robolectric.shadows.ShadowAsyncTask<Params, Progress, Result> {

    @RealObject
    private AsyncTask<Params, Progress, Result> realAsyncTask;

    @Implementation
    public AsyncTask<Params, Progress, Result> executeOnExecutor(Executor executor, Params... params) {
        //due to some weird Robolectric issue with thread-safety. I'm routing the task to the single executor
        return super.execute(params);
    }
}
