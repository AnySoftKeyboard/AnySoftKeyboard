package com.anysoftkeyboard.devicespecific;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract class FactoryViewBase extends View {

	protected FactoryViewBase(Context context) {
		super(context);
	}
	
	protected FactoryViewBase(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	 
	protected FactoryViewBase(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
    }
	
	public abstract DeviceSpecific createDeviceSpecific();
}
