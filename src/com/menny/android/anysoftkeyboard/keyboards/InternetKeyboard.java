package com.menny.android.anysoftkeyboard.keyboards;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import com.menny.android.anysoftkeyboard.R;

public class InternetKeyboard extends AnyKeyboard 
{
	public InternetKeyboard(Context context) 
	{
		super(context, R.xml.internet_qwerty, false, "Internet", "internet_keyboard");
	}
	
	@Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, 
            XmlResourceParser parser) {
        Key key = new Key(res, parent, x, y, parser);
        if (key.codes[0] == -80) 
        {
            key.text = ".com";
        }
        return key;
    }
}
