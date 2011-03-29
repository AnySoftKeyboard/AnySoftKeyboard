package com.menny.android.anysoftkeyboard.backup;

import android.app.backup.BackupManager;

public class CloudBackupRequesterApi8 extends CloudBackupRequester {

	public CloudBackupRequesterApi8(String packageName) {
		super(packageName);
	}

	@Override
	void notifyBackupManager()
	{
		BackupManager.dataChanged(getPackageName());
	}
}
