package com.anysoftkeyboard.nextword;

import com.anysoftkeyboard.prefs.backup.PrefsProvider;
import com.anysoftkeyboard.prefs.backup.PrefsRoot;

public class NextWordPrefsProvider implements PrefsProvider {
    @Override
    public String providerId() {
        return "NextWordPrefsProvider";
    }

    @Override
    public PrefsRoot getPrefsRoot() {
        return null;
    }

    @Override
    public void storePrefsRoot(PrefsRoot prefsRoot) throws Exception {

    }
}
