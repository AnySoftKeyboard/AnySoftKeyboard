package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.view.GestureDetector;

@TargetApi(8)
public class DeviceSpecific_V8 extends DeviceSpecific_V7 {
	@Override
	public String getApiLevel() {
		return "DeviceSpecific_V8";
	}

	@Override
	public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
		PackageManager pkg = appContext.getPackageManager();
		boolean hasDistintMultitouch = pkg
				.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT);
		boolean hasMultitouch = pkg
				.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);

		if (hasDistintMultitouch)
			return MultiTouchSupportLevel.Distinct;
		else if (hasMultitouch)
			return MultiTouchSupportLevel.Basic;
		else
			return MultiTouchSupportLevel.None;
	}

	@Override
	public GestureDetector createGestureDetector(Context appContext,
			AskOnGestureListener listener) {
		final boolean ignoreMultitouch = true;
		return new AskV8GestureDetector(appContext, listener, null,
				ignoreMultitouch);
	}
}