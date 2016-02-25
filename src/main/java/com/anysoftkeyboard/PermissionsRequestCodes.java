package com.anysoftkeyboard;

public enum PermissionsRequestCodes {
    CONTACTS,
    STORAGE_READ,
    STORAGE_WRITE,
    LAST_ENTRY;

    public int getRequestCode() {
        return ordinal() + 1;
    }
}
