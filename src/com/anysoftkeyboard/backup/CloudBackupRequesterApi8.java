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
import android.app.backup.BackupManager;

@TargetApi(8)
public class CloudBackupRequesterApi8 implements CloudBackupRequester {

    private final BackupManager mBackuper;

    public CloudBackupRequesterApi8(CloudBackupRequesterDiagram diagram) {
        mBackuper = new BackupManager(diagram.getContext());
    }

    public void notifyBackupManager() {
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
