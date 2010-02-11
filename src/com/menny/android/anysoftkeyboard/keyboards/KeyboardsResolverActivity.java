package com.menny.android.anysoftkeyboard.keyboards;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class KeyboardsResolverActivity extends Activity
{
	private String mKeyboardProviderUri;
	private final Object mMonitor = new Object();
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
		{
			mKeyboardProviderUri = data.getStringExtra("keyboardContentProviderUri");
			Log.i("ASK KeyboardsResolverActivity", "Got result: "+mKeyboardProviderUri);
		}
		else
		{
			Log.w("ASK KeyboardsResolverActivity", "Got ERROR result: "+resultCode);
		}
		mMonitor.notifyAll();
	}
	
	public String getKeyboardProviderUri(ResolveInfo info)
	{
		Intent intent = new Intent();
        intent.setClassName(info.activityInfo.applicationInfo.packageName, info.activityInfo.name);
        Log.d("ASK KeyboardsResolverActivity", "Located external activity "+intent.getComponent().toString());
		try {
			startActivityForResult(intent, 1);
			mMonitor.wait(5000);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("ASK KeyboardsResolverActivity", "Failed receiving keyboard provider URI from external activity.");
		}
		
		return mKeyboardProviderUri;
	}
}