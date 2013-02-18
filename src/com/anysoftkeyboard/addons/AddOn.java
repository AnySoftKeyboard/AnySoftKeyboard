package com.anysoftkeyboard.addons;

import android.content.Context;

public interface AddOn {
	public static final int INVALID_RES_ID = 0;
	
	String getId();
	String getName();
    String getDescription();
    String getPackageName();
    Context getPackageContext();
    int getSortIndex();
}
