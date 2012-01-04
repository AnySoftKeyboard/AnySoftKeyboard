package com.anysoftkeyboard.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

public class SoundPreferencesChangedReceiver extends BroadcastReceiver {

	public static interface SoundPreferencesChangedListener
	{
		void updateRingerMode();
	}
	
	private final SoundPreferencesChangedListener mListener;
	
	public SoundPreferencesChangedReceiver(SoundPreferencesChangedListener listener)
	{
		mListener = listener;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		mListener.updateRingerMode();
	}

	public IntentFilter createFilterToRegisterOn() {
		return new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
	}
}
