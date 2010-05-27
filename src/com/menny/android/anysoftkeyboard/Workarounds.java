package com.menny.android.anysoftkeyboard;

import java.lang.reflect.Field;

import android.util.Log;
import android.view.inputmethod.EditorInfo;

public class Workarounds 
{
	//Determine whether this device has the fix for RTL in the suggestions list
	private static final boolean ms_requiresRtlWorkaround;
	
	private static final boolean ms_isDonut;
	private static final boolean ms_isEclair;

	private static final String TAG = "ASK Workaround";
	
	static
	{
		boolean requiresRtlWorkaround = true;//all devices required this fix (in 2.1 it is still required)
		
		if (!android.os.Build.USER.toLowerCase().contains("root"))//there is no rooted ROM with a fix.
		{
			if (android.os.Build.MODEL.toLowerCase().contains("galaxy"))
			{
				//see issue 132
				//and issue 285
				//no fix: 1251851795000
				//fix: 1251970876000
				//no fix: 1251851795000
				//fix: 1251970876000
				//fix: 1261367883000
	//			//final int buildInc = Integer.parseInt(android.os.Build.VERSION.INCREMENTAL);
	//			//requiresRtlWorkaround = (buildInc < 20090831);
				requiresRtlWorkaround =  (android.os.Build.TIME <= 1251851795000l);
			}
			else if (android.os.Build.DEVICE.toLowerCase().contains("spica"))
			{
				//(see issue 285):
				//fixed: 1263807011000
				requiresRtlWorkaround =  (android.os.Build.TIME < 1263807011000l);//this is a lower "L" at the end
			}
		}
		ms_requiresRtlWorkaround = requiresRtlWorkaround;
		//checking f/w API is a bit tricky, we need to do it by reflection
		boolean isDonut = false;
		boolean isEclair = false;
		try
		{
			Field sdkInt = android.os.Build.VERSION.class.getField("SDK_INT");
			if (sdkInt != null)
			{
				//NOTE: I can not use the field here, since this code MAY run in cupcake, and therefore
				//fail in JIT compile. I need to perform this function with reflection...
				isDonut = (sdkInt.getInt(null) >= 4);
				isEclair = (sdkInt.getInt(null) >= 5);
			}
		}
		catch(Exception ex)
		{
		}
		ms_isDonut = isDonut;
		ms_isEclair = isEclair;
	}
	
	public static boolean isRightToLeftCharacter(final char key)
	{
    	final byte direction = Character.getDirectionality(key);

    	switch(direction)
		{
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
			return true;
		default:
			return false;
		}
	}
	
	public static int workaroundParenthesisDirectionFix(int primaryCode)
	{
		//Android does not support the correct direction of parenthesis in right-to-left langs.
		if (!getRtlWorkaround())
			return primaryCode;//I hope Galaxy has the fix...
		
		if (primaryCode == (int)')')
			return '(';
		else if (primaryCode == (int)'(')
			return ')';
		
		return primaryCode;
	}
	
	public static CharSequence workaroundCorrectStringDirection(CharSequence suggestion) 
    {
		//Hebrew letters are to be drawn in the other direction.
    	//Also, this is not valid for Galaxy (Israel's Cellcom Android)
    	if (!getRtlWorkaround())
			return suggestion;
		
    	//this function is a workaround! In the official 1.5 firmware, there is a RTL bug.
    	if (isRightToLeftCharacter(suggestion.charAt(0)))
    	{
    		String reveresed = "";
			for(int charIndex = suggestion.length() - 1; charIndex>=0; charIndex--)
			{
				reveresed = reveresed + suggestion.charAt(charIndex);
			}
			return reveresed;
    	}
    	else
    		return suggestion;
	}

	public static boolean getRtlWorkaround() {
		String configRtlWorkaround = AnySoftKeyboardConfiguration.getInstance().getRtlWorkaroundConfiguration();
		if (configRtlWorkaround.equals("auto"))
			return ms_requiresRtlWorkaround;
		else if (configRtlWorkaround.equals("workaround"))
			return true;
		else 
			return false;			
	}

	public static boolean isDonut() {
		return ms_isDonut;
	}
	
	public static boolean isEclair() {
		return ms_isEclair;
	}

	public static boolean doubleActionKeyDisableWorkAround(EditorInfo editor) {
		if (editor != null)
		{
			//package: com.android.mms, id:2131361817
			//in firmware 2, 2.1
			if (ms_isEclair && editor.packageName.contentEquals("com.android.mms")
					&& (editor.fieldId == 2131361817))
			{
				Log.d(TAG, "Android Ecliar Messaging MESSAGE field");
				return true;
			}
		}
		
		return false;
	}
}
