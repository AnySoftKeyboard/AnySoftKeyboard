package com.anysoftkeyboard.backup;

import android.app.backup.BackupManager;

public class CloudBackupRequesterApi8 implements CloudBackupRequester {

	private final String mPackageName;
	public CloudBackupRequesterApi8(String packageName) {
		mPackageName = packageName;
	}

	public void notifyBackupManager()
	{
		BackupManager.dataChanged(mPackageName);
	}
}
