package com.anysoftkeyboard.prefs.backup;

public interface PrefsProvider {
    String providerId();

    PrefsRoot getPrefsRoot();

    void storePrefsRoot(PrefsRoot prefsRoot);
}
