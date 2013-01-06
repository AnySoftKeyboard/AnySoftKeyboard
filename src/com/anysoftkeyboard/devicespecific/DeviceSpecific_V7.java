package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;

@TargetApi(7)
public class DeviceSpecific_V7 extends DeviceSpecific_V3 {
	@Override
	public String getApiLevel() {
		return "DeviceSpecific_V7";
	}

	@Override
	public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
		PackageManager pkg = appContext.getPackageManager();
		boolean hasMultitouch = pkg
				.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);

		if (hasMultitouch)
			return MultiTouchSupportLevel.Basic;
		else
			return MultiTouchSupportLevel.None;
	}
}
