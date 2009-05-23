package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;

import com.menny.android.anysoftkeyboard.R;

public class HebrewKeyboard extends AnyKeyboard 
{
	public HebrewKeyboard(Context context) 
	{
		super(context, R.xml.heb_qwerty, false, true, "עיברית");
		mPhysicalKeysMapping = new int[]{	1513, 1504, 1489, 1490,
											1511, 1499, 1506, 1497,
											1503, 1495, 1500, 1498,
											1510, 1502, 1501, 1508, 1509 /*Q*/,
											1512, 1491, 1488, 1493, 1492, 
											1507 /*W*/, 1505, 1496, 1494, 1514};
	}

}
