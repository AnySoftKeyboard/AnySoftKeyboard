package com.anysoftkeyboard;

import android.content.Context;
import android.content.res.Resources;

public class Constants {

	public static void initialize(Context appContext)
	{
		Resources res = appContext.getResources();
		mKEY_CODE_SPACE = res.getInteger(com.anysoftkeyboard.api.R.integer.key_code_space);
	}
	
	
	public static final boolean DEBUG = true;
	
	private static int mKEY_CODE_SPACE;
	public static int KEY_CODE_SPACE() {return mKEY_CODE_SPACE;}
}
