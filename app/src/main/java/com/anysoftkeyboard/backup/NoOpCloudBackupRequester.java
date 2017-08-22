package com.anysoftkeyboard.backup;

public class NoOpCloudBackupRequester implements CloudBackupRequester {
    @Override
    public void notifyBackupManager() {
        /*no op*/
    }
}
