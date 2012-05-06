package com.anysoftkeyboard.backup;

import com.anysoftkeyboard.ui.tutorials.WelcomeHowToNoticeActivity;
import com.menny.android.anysoftkeyboard.R;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInputStream;
import android.app.backup.SharedPreferencesBackupHelper;
import android.text.TextUtils;

@TargetApi(8)
public class AnyBackupAgent extends BackupAgentHelper {
	static final String DEFAULT_PREFS_FILE = "com.menny.android.anysoftkeyboard_preferences";
    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "prefs";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(getApplicationContext(), DEFAULT_PREFS_FILE)
        {
        	@Override
        	public void restoreEntity(BackupDataInputStream data) {
        		final String key = data.getKey();
        		if (!TextUtils.isEmpty(key))
        		{
	        		//there are some keys I do not want to restore, since they are device specific
	        		if (key.equalsIgnoreCase(getApplicationContext().getString(R.string.settings_key_default_split_state)))
	        			return;
	        		if (key.equalsIgnoreCase("zoom_factor_keys_in_portrait"))
	        			return;
	        		if (key.equalsIgnoreCase("zoom_factor_keys_in_landscape"))
	        			return;
	        		if (key.equalsIgnoreCase(getApplicationContext().getString(R.string.settings_key_portrait_fullscreen)))
	        			return;
	        		if (key.equalsIgnoreCase(getApplicationContext().getString(R.string.settings_key_landscape_fullscreen)))
	        			return;
	        		//every device/install should get this help thingy
	        		if (key.equalsIgnoreCase(WelcomeHowToNoticeActivity.ASK_HAS_BEEN_ENABLED_BEFORE))
	        			return;
        		}
        		super.restoreEntity(data);
        	}
        };
        
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
