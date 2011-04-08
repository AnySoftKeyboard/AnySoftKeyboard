package com.anysoftkeyboard.ui;

import com.menny.android.anysoftkeyboard.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class SendBugReportUiActivity extends Activity {

	public static final String CRASH_REPORT_TEXT = "CRASH_REPORT_TEXT";
	private static final String TAG = "ASK BUGER";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final AlertDialog dialog = new AlertDialog.Builder(this)
			.setIcon(R.drawable.icon_8_key)
			.setTitle(R.string.ime_name)
			.setMessage("Oops, didn't see that coming...")
			.setPositiveButton("Send :)", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					sendCrashReportViaEmail();
					finish();
				}
			})
			.setCancelable(true)
			.setNegativeButton("No", new OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			})
			.create();
		
		dialog.show();
	}

	protected void sendCrashReportViaEmail() {
		Intent callingIntent = getIntent();
		
		Intent sendMail = new Intent();
		sendMail.setAction(Intent.ACTION_SEND);
		sendMail.setType("plain/text");
		sendMail.putExtra(Intent.EXTRA_EMAIL, "mennyed@gmail.com");
		sendMail.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.ime_name) + " crashed!");
		sendMail.putExtra(Intent.EXTRA_TEXT, callingIntent.getStringExtra(CRASH_REPORT_TEXT));
		
		try {
			Intent sender = Intent.createChooser(sendMail, "Send bug report");
			sender.putExtra(Intent.EXTRA_EMAIL, "mennyed@gmail.com");
			sender.putExtra(Intent.EXTRA_SUBJECT, getText(R.string.ime_name) + " crashed!");
			sender.putExtra(Intent.EXTRA_TEXT, callingIntent.getStringExtra(CRASH_REPORT_TEXT));
			
			Log.i(TAG, "Will send crash report using "+sender);
			startActivity(sender);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Unable to send bug report via e-mail!", Toast.LENGTH_LONG).show();
		}
		
		finish();
	}
}
