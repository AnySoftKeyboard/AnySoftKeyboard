package com.anysoftkeyboard.devicespecific;


import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;

public class DeviceSpecific_V7 extends DeviceSpecific_V5 {

	public DeviceSpecific_V7(Context context) {
		super(context);
	}

	public DeviceSpecific_V7(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public DeviceSpecific_V7(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }

	@Override
	public String getApiLevel() {
		return "DeviceSpecific_V7";
	}
	
	@Override
	public MultiTouchSupportLevel getMultiTouchSupportLevel() {
		PackageManager pkg = getContext().getPackageManager();
		boolean hasMultitouch = pkg.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
		
		if (hasMultitouch)
			return MultiTouchSupportLevel.Basic;
		else
			return MultiTouchSupportLevel.None;
	}
	
}
