package com.menny.android.anysoftkeyboard;

public class Workarounds 
{
	//Determine whether this device has the fix for RTL in the suggestions list
	private static final boolean ms_requiresRtlWorkaround;
	
	static
	{
		boolean requiresRtlWorkaround = true;//all devices required this fix (in 1.6 it is still required)
		if (android.os.Build.MODEL.toLowerCase().contains("galaxy"))
		{
			try
			{
				final int buildInc = Integer.parseInt(android.os.Build.VERSION.INCREMENTAL);
				requiresRtlWorkaround = (buildInc < 20090903);
			}
			catch(Exception ex)
			{
				requiresRtlWorkaround = true;//if it is like that, then I do not know, and rather say WORKAROUND!
			}
		}
		//Tomer's phone is the only one with the fix (see issue 132).
		ms_requiresRtlWorkaround = requiresRtlWorkaround;
	}
	
	public static CharSequence workaroundCorrectStringDirection(CharSequence suggestion) 
    {
		//Hebrew letters are to be drawn in the other direction. This will be probably be removed in Donut.
    	//Also, this is not valid for Galaxy (Israel's Cellcom Android)
    	if (!ms_requiresRtlWorkaround)
			return suggestion;
		
    	//this function is a workaround! In the official 1.5 firmware, there is a RTL bug.
    	final byte direction = Character.getDirectionality(suggestion.charAt(0));
    	//Log.d("AnySoftKeyboard", "CandidateView: correctStringDirection: direction:"+direction+" char:"+suggestion.charAt(0));
		switch(direction)
		{
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING:
		case Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE:
			String reveresed = "";
			for(int charIndex = suggestion.length() - 1; charIndex>=0; charIndex--)
			{
				reveresed = reveresed + suggestion.charAt(charIndex);
			}
			return reveresed;
		}
		return suggestion;
	}
}
