package com.menny.android.anysoftkeyboard.keyboards;

import android.util.Log;

import com.menny.android.anysoftkeyboard.AnyKeyboardContextProvider;

public class GenericKeyboard extends AnyKeyboard 
{
	//issue 146
	private boolean mRightToLeft = false;
	private Key mLeftClose;
	private Key mRightClose;
	
	public GenericKeyboard(AnyKeyboardContextProvider context, int xmlLayoutResId) 
	{
		super(context, xmlLayoutResId);
	}

	@Override
	protected Key createKeyFromXml(android.content.res.Resources res, Row parent, int x, int y, android.content.res.XmlResourceParser parser)
	{
		Key aKey = super.createKeyFromXml(res, parent, x, y, parser);
		
		if (aKey.codes != null && (aKey.codes.length == 1))
		{
			if (aKey.codes[0] == (int)'(')
				mLeftClose = aKey;
			else if (aKey.codes[0] == (int)')')
				mRightClose = aKey;
		}
		
		return aKey;
	}
	
	public void workaroundSetRightToLeftKeys(boolean rightToLeftLayoutFixRequired) 
	{
		if (rightToLeftLayoutFixRequired != mRightToLeft)
		{
			mRightToLeft = rightToLeftLayoutFixRequired;
			Log.d("AnySoftKeyboard", "workaround: changing () characters behavior. rightToLeftLayoutFixRequired:"+rightToLeftLayoutFixRequired);
			if (mRightToLeft)
			{
				mLeftClose.codes[0] = ')';
				mLeftClose.label = "(";
				mRightClose.codes[0] = '(';
				mRightClose.label = ")";
			}
			else
			{
				mLeftClose.codes[0] = '(';
				mLeftClose.label = "(";
				mRightClose.codes[0] = ')';
				mRightClose.label = ")";
			}
		}		
	}	
}
