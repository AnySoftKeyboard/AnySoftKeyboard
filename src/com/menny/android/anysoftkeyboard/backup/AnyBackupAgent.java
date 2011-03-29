package com.menny.android.anysoftkeyboard.backup;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

public class AnyBackupAgent extends BackupAgentHelper {
	static final String DEFAULT_PREFS_FILE = "com.menny.android.anysoftkeyboard_preferences";
    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "prefs";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, DEFAULT_PREFS_FILE);
        
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
