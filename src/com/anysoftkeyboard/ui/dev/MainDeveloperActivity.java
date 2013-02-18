package com.anysoftkeyboard.ui.dev;


import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.anysoftkeyboard.ui.AsyncTaskWithProgressWindow;
import com.menny.android.anysoftkeyboard.R;

public class MainDeveloperActivity extends Activity {

	private abstract static class DeveloperAsyncTask<Params, Progress, Result> extends AsyncTaskWithProgressWindow<Params, Progress, Result, MainDeveloperActivity> {

		public DeveloperAsyncTask(MainDeveloperActivity mainDeveloperActivity) {
			super(mainDeveloperActivity);
		}
		
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.developer_tools);
	}

	public void onUserClickedMemoryDump(View v) {
		DeveloperAsyncTask<Void, Void, File> task = new DeveloperAsyncTask<Void, Void, File>(this) {
			
			@Override
			protected File doAsyncTask(Void[] params) throws Exception {
				return DeveloperUtils.createMemoryDump();
			}
			
			@Override
			protected void applyResults(File result,
					Exception backgroundException) {
				if (backgroundException != null) {
					Toast.makeText(getApplicationContext(), getString(R.string.failed_to_create_mem_dump, backgroundException.getMessage()), Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), getString(R.string.created_mem_dump_file, result.getAbsolutePath()), Toast.LENGTH_LONG).show();
				}
			}
		};
		task.execute();
	}
}
