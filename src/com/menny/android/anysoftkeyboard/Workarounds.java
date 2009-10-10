package com.menny.android.anysoftkeyboard;

public class Workarounds 
{
	private static final boolean ms_requiresRtlWorkaround;
	
	static
	{
		String buildVersion = android.os.Build.VERSION.RELEASE.toUpperCase();
		boolean fixedGalaxy = android.os.Build.MODEL.toLowerCase().contains("galaxy")
				//Tomer's phone is the only one with the fix.
				&& buildVersion.contains("76XXCSDCBALUM6375");
		//Determine whether this device has the fix for RTL in the suggestions list
		ms_requiresRtlWorkaround = !fixedGalaxy;
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
