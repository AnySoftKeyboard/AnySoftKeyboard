package com.menny.android.anysoftkeyboard.tutorials;

import com.menny.android.anysoftkeyboard.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

public class TutorialsProvider 
{
	public static void ShowTutorialsIfNeeded(Context context)
	{
		ShowTutorial(context, 0/*R.string.tutorial_swipe_title*/, 0/*R.string.tutorial_swipe_text*/);
	}
	
	public static void ShowTutorial(Context context, int titleId, int textId)
	{
		BasicTextTutorialDialog dialog = new BasicTextTutorialDialog(context, titleId, textId);
		dialog.show();
	}	
}
