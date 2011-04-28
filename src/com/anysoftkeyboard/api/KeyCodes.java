package com.anysoftkeyboard.api;

import android.content.Context;
import android.content.res.Resources;

public class KeyCodes {

	public final int SPACE;
	public final int PERIOD;
	public final int ENTER;
	public final int SHIFT;
	public final int DELETE;
	public final int SMILEY;
	public final int DOMAIN;
	public final int SETTINGS;
	
	public KeyCodes(Context appContext)
	{
		Resources res = appContext.getResources();
		SPACE = res.getInteger(com.anysoftkeyboard.api.R.integer.key_code_space);
		PERIOD = '.';
		ENTER = res.getInteger(com.anysoftkeyboard.api.R.integer.key_code_enter);
		SHIFT = res.getInteger(com.anysoftkeyboard.api.R.integer.key_code_shift);
		DELETE = res.getInteger(com.anysoftkeyboard.api.R.integer.key_code_delete);
		SMILEY = res.getInteger(com.anysoftkeyboard.api.R.integer.key_code_smiley);
		DOMAIN = res.getInteger(com.anysoftkeyboard.api.R.integer.key_code_domain);
		SETTINGS = res.getInteger(com.anysoftkeyboard.api.R.integer.key_code_settings);
	}
}
