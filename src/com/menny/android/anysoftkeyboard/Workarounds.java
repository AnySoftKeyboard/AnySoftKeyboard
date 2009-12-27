package com.menny.android.anysoftkeyboard;

import java.lang.reflect.Field;

public class Workarounds 
{
	//Determine whether this device has the fix for RTL in the suggestions list
	private static final boolean ms_requiresRtlWorkaround;
	
	private static final boolean ms_isDonut;
	private static final boolean ms_isEclair;
	
	static
	{
		boolean requiresRtlWorkaround = true;//all devices required this fix (in 1.6 it is still required)
		
		if (android.os.Build.MODEL.toLowerCase().contains("galaxy"))
		{
			//(see issue 132):
			try
			{
				final int buildInc = Integer.parseInt(android.os.Build.VERSION.INCREMENTAL);
				requiresRtlWorkaround = (buildInc < 20090831);
			}
			catch(Exception ex)
			{
				requiresRtlWorkaround = true;//if it is like that, then I do not know, and rather say WORKAROUND!
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
		if (!ms_requiresRtlWorkaround)
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
    	if (!ms_requiresRtlWorkaround)
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

	public static boolean isDonut() {
		return ms_isDonut;
	}
	
	public static boolean isEclair() {
		return ms_isEclair;
	}
}
