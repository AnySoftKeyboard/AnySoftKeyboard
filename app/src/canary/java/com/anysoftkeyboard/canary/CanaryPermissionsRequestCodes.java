package com.anysoftkeyboard.debug;

import com.anysoftkeyboard.PermissionsRequestCodes;

public enum CanaryPermissionsRequestCodes {
    INTERNET;

    public int getRequestCode() {
        return ordinal() + PermissionsRequestCodes.LAST_ENTRY.getRequestCode();
    }
}
