package com.menny.android.anysoftkeyboard.addons;

import android.content.Context;

public abstract class AddOnImpl implements AddOn {

	private final String mId;
    private final int mNameResId;
    private final String mDescription;
    private final Context mPackageContext;
    private final int mSortIndex;
    
    protected AddOnImpl(Context packageContext, String id, int nameResId, String description, int sortIndex)
    {
    	mId = id;
    	mNameResId = nameResId;
    	mDescription = description;
    	mPackageContext = packageContext;
    	mSortIndex = sortIndex;
    }
    
	public final String getId() {
		return mId;
	}

	public final int getNameResId() {
		return mNameResId;
	}

	public final String getDescription() {
		return mDescription;
	}

	public final Context getPackageContext() {
		return mPackageContext;
	}

	public final int getSortIndex() {
		return mSortIndex;
	}

	public String getName() {
		return mPackageContext.getString(mNameResId);
	}
}
