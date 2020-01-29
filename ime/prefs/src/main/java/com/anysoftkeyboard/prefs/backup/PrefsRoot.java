package com.anysoftkeyboard.prefs.backup;

public class PrefsRoot extends PrefItem {
    private final int mVersion;

    public PrefsRoot(int version) {
        mVersion = version;
    }

    public int getVersion() {
        return mVersion;
    }
}
