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

package com.anysoftkeyboard.backup;

import android.annotation.TargetApi;
import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInputStream;
import android.app.backup.SharedPreferencesBackupHelper;
import android.text.TextUtils;
import com.menny.android.anysoftkeyboard.R;

@TargetApi(8)
public class AnyBackupAgent extends BackupAgentHelper {
    static final String DEFAULT_PREFS_FILE = "com.menny.android.anysoftkeyboard_preferences";
    // A key to uniquely identify the set of backup data
    static final String PREFS_BACKUP_KEY = "prefs";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(getApplicationContext(), DEFAULT_PREFS_FILE) {
            @Override
            public void restoreEntity(BackupDataInputStream data) {
                final String key = data.getKey();
                if (!TextUtils.isEmpty(key)) {
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
                    //RTL is device specific
                    if (key.equalsIgnoreCase(getApplicationContext().getString(R.string.settings_key_workaround_disable_rtl_fix)))
                        return;
                }
                super.restoreEntity(data);
            }
        };

        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
