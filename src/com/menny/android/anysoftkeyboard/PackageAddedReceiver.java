//package com.menny.android.anysoftkeyboard;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
//public class PackageAddedReceiver extends BroadcastReceiver {
//    private static final String TAG = "ASK Pkg";
//
//    
//    
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		if (AnySoftKeyboardConfiguration.getInstance().getDEBUG())
//		{
//			Log.d(TAG, context.getPackageName());
//        	Log.d(TAG, intent.getData().toString());
//		}
//	}
//
//}