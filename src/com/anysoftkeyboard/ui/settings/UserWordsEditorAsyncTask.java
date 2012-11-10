package com.anysoftkeyboard.ui.settings;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.menny.android.anysoftkeyboard.R;

abstract class UserWordsEditorAsyncTask extends AsyncTask<Void, Void, Void> {
	private static final String TAG = "ASK UserWordsEditorAsyncTask";
	/**
     * 
     */
	private final UserDictionaryEditorActivity mUserWordsEditorActivity;

	/**
	 * @param userDictionaryEditorActivity
	 */
	UserWordsEditorAsyncTask(
			UserDictionaryEditorActivity userDictionaryEditorActivity) {
		mUserWordsEditorActivity = userDictionaryEditorActivity;
	}

	private ProgressDialog progresDialog;

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progresDialog = new ProgressDialog(mUserWordsEditorActivity);
		progresDialog.setTitle("");
		progresDialog.setMessage(mUserWordsEditorActivity
				.getText(R.string.user_dictionary_read_please_wait));
		progresDialog.setCancelable(false);
		progresDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		progresDialog.setOwnerActivity(mUserWordsEditorActivity);

		progresDialog.show();
	}

	protected void onPostExecute(Void result) {
		try {
			if (progresDialog.isShowing())
				progresDialog.dismiss();
		} catch (IllegalArgumentException e) {
			// just swallowing it.
			Log.w(TAG, "Caught an excpetion while trying to dismiss the progress dialog. Not important?");
		}
		applyResults(result);
	}

	protected abstract void applyResults(Void result);
}