package com.menny.android.anysoftkeyboard.tutorials;

import android.app.Dialog;
import android.content.Context;

public class BasicTextTutorialDialog extends Dialog
{

	public BasicTextTutorialDialog(Context context, int titleId, int textId) 
	{
		super(context);
/*		// I want it ABOVE the IME
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
	             WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		//setting the stuff inside the dialog
		setContentView(R.layout.tutorial);
		//putting text
		
        TextView title = (TextView)findViewById(R.id.tutorial_title);
        title.setText(titleId);
        TextView text = (TextView)findViewById(R.id.tutorial_text);
		text.setText(textId);*/
	}

}
