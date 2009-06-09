package com.menny.android.anysoftkeyboard.tutorials;

import android.content.Context;

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
