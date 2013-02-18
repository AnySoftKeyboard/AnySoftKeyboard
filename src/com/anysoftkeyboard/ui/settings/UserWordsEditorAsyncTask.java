package com.anysoftkeyboard.ui.settings;

import com.anysoftkeyboard.ui.AsyncTaskWithProgressWindow;

abstract class UserWordsEditorAsyncTask extends AsyncTaskWithProgressWindow<Void, Void, Void, UserDictionaryEditorActivity> {
	
	protected UserWordsEditorAsyncTask(
			UserDictionaryEditorActivity userDictionaryEditorActivity) {
		super(userDictionaryEditorActivity);
	}
}