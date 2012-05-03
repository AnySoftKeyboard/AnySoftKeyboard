package com.anysoftkeyboard.devicespecific;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.AttributeSet;

@TargetApi(7)
class FactoryView_V7 extends FactoryView_V5 {

	public FactoryView_V7(Context context) {
		super(context);
	}

	public FactoryView_V7(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	public FactoryView_V7(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }

	
	@Override
	public DeviceSpecific createDeviceSpecific() {
		return new DeviceSpecific_V7();
	}

	public static class DeviceSpecific_V7 extends DeviceSpecific_V5
	{		
		@Override
		public String getApiLevel() {
			return "DeviceSpecific_V7";
		}
		
		@Override
		public MultiTouchSupportLevel getMultiTouchSupportLevel(Context appContext) {
			PackageManager pkg = appContext.getPackageManager();
			boolean hasMultitouch = pkg.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
			
			if (hasMultitouch)
				return MultiTouchSupportLevel.Basic;
			else
				return MultiTouchSupportLevel.None;
		}
	}
	
}
