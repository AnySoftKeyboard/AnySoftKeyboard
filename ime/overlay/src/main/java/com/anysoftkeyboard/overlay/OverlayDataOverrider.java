package com.anysoftkeyboard.overlay;

import android.content.ComponentName;
import java.util.HashMap;
import java.util.Map;

public class OverlayDataOverrider implements OverlyDataCreator {
    private final OverlyDataCreator mOriginal;
    private final Map<String, OverlayData> mOverrides;

    public OverlayDataOverrider(OverlyDataCreator original, Map<String, OverlayData> overrides) {
        mOriginal = original;
        mOverrides = new HashMap<>(overrides);
    }

    @Override
    public OverlayData createOverlayData(ComponentName remoteApp) {
        if (mOverrides.containsKey(remoteApp.getPackageName())) {
            return mOverrides.get(remoteApp.getPackageName());
        } else {
            return mOriginal.createOverlayData(remoteApp);
        }
    }
}
