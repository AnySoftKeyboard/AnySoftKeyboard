package com.anysoftkeyboard.utils;

import java.lang.reflect.Field;

import com.anysoftkeyboard.AnySoftKeyboardConfiguration;

import android.util.Log;
import android.view.inputmethod.EditorInfo;

public class Workarounds 
{
	//Determine whether this device has the fix for RTL in the suggestions list
	//private static final boolean ms_requiresRtlWorkaround;
	
	private static final int ms_ApiLevel;
	

	private static final String TAG = "ASK Workaround";
	
	static
	{
		//checking f/w API is a bit tricky, we need to do it by reflection
		int sdkVersion = 1;
		try
		{
			Field sdkInt = android.os.Build.VERSION.class.getField("SDK_INT");
			if (sdkInt != null)
			{
				//NOTE: I can not use the field here, since this code MAY run in cupcake, and therefore
				//fail in JIT compile. I need to perform this function with reflection...
				sdkVersion = sdkInt.getInt(null);
			}
		}
		catch(Exception ex)
		{
			sdkVersion = 3;
		}
		ms_ApiLevel = sdkVersion;
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

	public static boolean isAltSpaceLangSwitchNotPossible(){
		String model = android.os.Build.MODEL.toLowerCase();
		if(model.equals("milestone") || model.equals("droid")){
			return true;
		}
		return false;
	}

	public static int getApiLevel() {
		return ms_ApiLevel;
	}
	
	public static boolean doubleActionKeyDisableWorkAround(EditorInfo editor) {
		if (editor != null)
		{
			//package: com.android.mms, id:2131361817
			//in firmware 2, 2.1
			if (ms_ApiLevel <= 6 && ms_ApiLevel >=5 && editor.packageName.contentEquals("com.android.mms")
					&& (editor.fieldId == 2131361817))
			{
			    if (AnySoftKeyboardConfiguration.DEBUG)Log.d(TAG, "Android Ecliar Messaging MESSAGE field");
				return true;
			}
		}
		
		return false;
	}
}
