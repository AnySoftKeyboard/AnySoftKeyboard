package com.menny.android.anysoftkeyboard;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.*;

public class SeekBarPreference extends DialogPreference{

    private Context context;
    private SeekBar volumeLevel;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    protected void onPrepareDialogBuilder(Builder builder) {

        LinearLayout layout = new LinearLayout(context);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.setMinimumWidth(400);
        layout.setPadding(20, 20, 20, 20);

        volumeLevel = new SeekBar(context);
        volumeLevel.setMax(8);
        volumeLevel.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        volumeLevel.setProgress(getPersistedInt(0));

        layout.addView(volumeLevel);

        builder.setView(layout);

        super.onPrepareDialogBuilder(builder);
    }

    protected void onDialogClosed(boolean positiveResult) {
        persistInt(volumeLevel.getProgress());
    }

	/*
SeekBarPreference sb = new SeekBarPreference(this, null);
sb.setTitle(R.string.volume);
sb.setSummary(R.string.volume_summary);
sb.setKey(VOLUME_LEVEL_PREF); 	
	*/
} 
