package com.menny.android.anysoftkeyboard.addons;

import android.content.Context;

public interface AddOn {
	String getId();
	int getNameResId();
	String getName();
    String getDescription();
    Context getPackageContext();
    int getSortIndex();
}
