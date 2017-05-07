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

import java.lang.ref.WeakReference;

public abstract class AsyncTaskWithProgressWindow<I, P, R, A extends AsyncTaskWithProgressWindow.AsyncTaskOwner>
        extends AsyncTask<I, P, R> {

    public interface AsyncTaskOwner {
        Activity getActivity();
    }

    private static final String TAG = "ATaskProgressWindow";

    private final WeakReference<A> mActivity;
    private final boolean mShowProgressDialog;

    private Dialog mProgressDialog;
    private Exception mBackgroundException;

    protected AsyncTaskWithProgressWindow(A activity, boolean showProgressDialog) {
        mActivity = new WeakReference<>(activity);
        mShowProgressDialog = showProgressDialog;
    }

    protected AsyncTaskWithProgressWindow(A activity) {
        this(activity, true);
    }

    protected final A getOwner() {
        return mActivity.get();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        A a = getOwner();
        if (a == null)
            return;

        if (mShowProgressDialog) {
            mProgressDialog = new Dialog(a.getActivity(), com.menny.android.anysoftkeyboard.R.style.ProgressDialog);
            mProgressDialog.setContentView(com.menny.android.anysoftkeyboard.R.layout.progress_window);
            mProgressDialog.setTitle(null);
            mProgressDialog.setCancelable(false);

            mProgressDialog.setOwnerActivity(a.getActivity());

            mProgressDialog.show();
        }
    }

    @Override
    protected final R doInBackground(I... params) {
        mBackgroundException = null;
        try {
            return doAsyncTask(params);
        } catch (Exception e) {
            mBackgroundException = e;
        }
        return null;
    }

    protected abstract R doAsyncTask(I[] params) throws Exception;

    @Override
    protected final void onPostExecute(R result) {
        super.onPostExecute(result);
        try {
            if (mShowProgressDialog && mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            // just swallowing it.
            Log.w(TAG, "Caught an exception while trying to dismiss the progress dialog. Not important?");
        }
        applyResults(result, mBackgroundException);
    }

    protected abstract void applyResults(R result, Exception backgroundException);
}