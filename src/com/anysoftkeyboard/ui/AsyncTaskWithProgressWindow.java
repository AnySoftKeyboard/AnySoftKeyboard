package com.anysoftkeyboard.ui;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

public abstract class AsyncTaskWithProgressWindow<Params, Progress, Result, A extends Activity>
		extends AsyncTask<Params, Progress, Result> {
	private static final String TAG = "ASK AsyncTask";

	private final WeakReference<A> mActivity;

	protected AsyncTaskWithProgressWindow(A activity) {
		mActivity = new WeakReference<A>(activity);
	}

	private Dialog progresDialog;

	private Exception mBackgroundException;

	protected final A getOwningActivity() {
		A a = mActivity.get();
		return a;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		A a = getOwningActivity();
		if (a == null)
			return;

		progresDialog = new Dialog(mActivity.get(), R.style.ProgressDialog);
		progresDialog.setContentView(R.layout.progress_window);
		progresDialog.setTitle(null);
		progresDialog.setCancelable(false);

		progresDialog.setOwnerActivity(a);

		progresDialog.show();
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
			if (progresDialog != null && progresDialog.isShowing())
				progresDialog.dismiss();
		} catch (IllegalArgumentException e) {
			// just swallowing it.
			Log.w(TAG,
					"Caught an excpetion while trying to dismiss the progress dialog. Not important?");
		}
		applyResults(result, mBackgroundException);
	}

	protected abstract void applyResults(Result result,
			Exception backgroundException);
}