package com.anysoftkeyboard.backup;

import android.content.Context;

import net.evendanan.frankenrobot.Diagram;

public class CloudBackupRequesterDiagram extends Diagram<CloudBackupRequester> {

	private final Context mContext;

	public CloudBackupRequesterDiagram(Context context) {
		mContext = context;
	}

	public Context getContext() {
		return mContext;
	}
}
