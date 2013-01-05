package com.anysoftkeyboard.backup;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;

@TargetApi(8)
public class CloudBackupRequesterApi8 implements CloudBackupRequester {
	
	private final BackupManager mBackuper;
	
	public CloudBackupRequesterApi8(CloudBackupRequesterDiagram diagram) {
		mBackuper = new BackupManager(diagram.getContext());
	}

	public void notifyBackupManager()
	{
		mBackuper.dataChanged();
	}
	
/*
	public void requestRestore()
	{
		mBackuper.requestRestore(
				new RestoreObserver() {
					@Override
					public void restoreStarting(int numPackages) {
						Log.d(TAG, "Restore from cloud starting.");
						super.restoreStarting(numPackages);
					}
					
					@Override
					public void onUpdate(int nowBeingRestored, String currentPackage) {
						Log.d(TAG, "Restoring "+currentPackage);
						super.onUpdate(nowBeingRestored, currentPackage);
					}
					
					@Override
					public void restoreFinished(int error) {
						Log.d(TAG, "Restore from cloud finished.");
						super.restoreFinished(error);
					}
				});
	}
	*/
	
}
