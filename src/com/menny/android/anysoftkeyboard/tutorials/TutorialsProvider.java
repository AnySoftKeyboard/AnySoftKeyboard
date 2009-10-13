package com.menny.android.anysoftkeyboard.tutorials;

import com.menny.android.anysoftkeyboard.AnySoftKeyboard;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class TutorialsProvider 
{
	private static boolean msDEBUG_TUTORIAL_SHOWN = false;
	
	public static void ShowTutorialsIfNeeded(Context context, View view)
	{
		Log.i("AnySoftKeyboard", "TutorialsProvider::ShowTutorialsIfNeeded called");
		if ((!msDEBUG_TUTORIAL_SHOWN) && (AnySoftKeyboard.DEBUG))
		{
			Log.i("AnySoftKeyboard", "TutorialsProvider::ShowTutorialsIfNeeded starting 'TESTERS VERSION'");
			msDEBUG_TUTORIAL_SHOWN = true;
			ShowBasicTutorial(view.getContext(), "TESTERS VERSION", "This is an ALPHA/BETA version, and should be used by testers only. It probably contains a lot of bugs, and half-baked features, so do not expect a stable version.\n"
					+"This version includes the following changes:\n*Georgian, Thai and Canadian keyboards\n*candidates in physical keyboard\n*Fixes for issues: 132, 129, 112, 114, 109, 141, 139.\n\nThanks for testing.");
		}
	}
	
	public static void ShowBasicTutorial(Context context, int titleId, int textId)
	{
		ShowBasicTutorial(context, context.getResources().getString(titleId), context.getResources().getString(textId));
	}
	
	public static void ShowBasicTutorial(Context context, String title, String text)
	{
		BasicTextTutorialDialog dialog = new BasicTextTutorialDialog(context, title, text);
		dialog.show();
	}	
}
