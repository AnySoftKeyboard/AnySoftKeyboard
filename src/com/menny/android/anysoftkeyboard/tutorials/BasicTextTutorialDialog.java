package com.menny.android.anysoftkeyboard.tutorials;

import com.menny.android.anysoftkeyboard.R;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class BasicTextTutorialDialog extends Dialog
{
	public BasicTextTutorialDialog(Context context, String title, String text) 
	{
		super(context);
		Log.d("AnySoftKeyboard", "Starting 'BasicTextTutorialDialog' with title '"+title+"'...");
		// I want it ABOVE the IME
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
	             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		//setting the stuff inside the dialog
		setContentView(R.layout.tutorial);
		//putting text
		
        TextView titleLabel = (TextView)findViewById(R.id.tutorial_title);
        titleLabel.setText(title);
        TextView textLabel = (TextView)findViewById(R.id.tutorial_text);
        textLabel.setText(text);
	}

}
