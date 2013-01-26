package com.anysoftkeyboard.addons;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public abstract class AddOnImpl implements AddOn {

	private final String mId;
	private final String mName;
	private final String mDescription;
	private final String mPackageName;
	private final Context mAskAppContext;
	private WeakReference<Context> mPackageContext;
	private final int mSortIndex;

	protected AddOnImpl(Context askContext, Context packageContext, String id, int nameResId,
			String description, int sortIndex) {
		mId = id;
		mAskAppContext = askContext;
		mName = packageContext.getString(nameResId);
		mDescription = description;
		mPackageName = packageContext.getPackageName();
		mPackageContext = new WeakReference<Context>(packageContext);
		mSortIndex = sortIndex;
	}

	public final String getId() {
		return mId;
	}

	public final String getDescription() {
		return mDescription;
	}

	public final Context getPackageContext() {
		Context c = mPackageContext.get();
		if (c == null) {
			try {
				c = mAskAppContext.createPackageContext(mPackageName, PackageManager.GET_META_DATA);
				mPackageContext = new WeakReference<Context>(c);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return c;
	}

	public final int getSortIndex() {
		return mSortIndex;
	}

	public String getName() {
		return mName;
	}
}
