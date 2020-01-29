package com.anysoftkeyboard;

public enum PermissionsRequestCodes {
    CONTACTS,
    STORAGE,
    LAST_ENTRY;

    public int getRequestCode() {
        return ordinal() + 1;
    }
}
