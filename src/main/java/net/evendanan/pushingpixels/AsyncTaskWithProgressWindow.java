/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.evendanan.pushingpixels;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;

import com.menny.android.anysoftkeyboard.R;

import java.lang.ref.WeakReference;

public abstract class AsyncTaskWithProgressWindow<Params, Progress, Result, A extends AsyncTaskWithProgressWindow.AsyncTaskOwner>
        extends AsyncTask<Params, Progress, Result> {

    public static interface AsyncTaskOwner {
        Activity getActivity();
    }

    private static final String TAG = "AsyncTaskWithProgressWindow";

    private final WeakReference<A> mActivity;

    protected AsyncTaskWithProgressWindow(A activity) {
        mActivity = new WeakReference<A>(activity);
    }

    private Dialog mProgressDialog;

    private Exception mBackgroundException;

    protected final A getOwner() {
        A a = mActivity.get();
        return a;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        A a = getOwner();
        if (a == null)
            return;

        mProgressDialog = new Dialog(a.getActivity(), R.style.ProgressDialog);
        mProgressDialog.setContentView(R.layout.progress_window);
        mProgressDialog.setTitle(null);
        mProgressDialog.setCancelable(false);

        mProgressDialog.setOwnerActivity(a.getActivity());

        mProgressDialog.show();
    }

    @Override
    protected final Result doInBackground(Params... params) {
        mBackgroundException = null;
        try {
            return doAsyncTask(params);
        } catch (Exception e) {
            mBackgroundException = e;
        }
        return null;
    }

    protected abstract Result doAsyncTask(Params[] params) throws Exception;

    @Override
    protected final void onPostExecute(Result result) {
        super.onPostExecute(result);
        try {
            if (mProgressDialog != null && mProgressDialog.isShowing())
                mProgressDialog.dismiss();
        } catch (IllegalArgumentException e) {
            // just swallowing it.
            Log.w(TAG,
                    "Caught an exception while trying to dismiss the progress dialog. Not important?");
        }
        applyResults(result, mBackgroundException);
    }

    protected abstract void applyResults(Result result,
                                         Exception backgroundException);
}