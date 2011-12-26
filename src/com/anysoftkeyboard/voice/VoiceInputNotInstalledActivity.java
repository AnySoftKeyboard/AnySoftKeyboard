package com.anysoftkeyboard.voice;

import com.menny.android.anysoftkeyboard.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class VoiceInputNotInstalledActivity extends Activity implements android.view.View.OnClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_input_not_installed);
		findViewById(R.id.install_button).setOnClickListener(this);
		findViewById(R.id.no_button).setOnClickListener(this);
	}
	
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.install_button:
			Intent search = new Intent(Intent.ACTION_VIEW);
			search.setData(Uri.parse("market://search?q=pname:com.google.android.voicesearch"));
			search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(search);
			break;
		}
		
		finish();
	}
}
