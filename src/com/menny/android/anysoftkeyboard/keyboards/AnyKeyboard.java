/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.menny.android.anysoftkeyboard.keyboards;

import java.util.ArrayList;

import com.menny.android.anysoftkeyboard.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.util.Log;
import android.view.inputmethod.EditorInfo;

public abstract class AnyKeyboard extends Keyboard 
{
	public interface HardKeyboardTranslator
	{
		char translatePhysicalCharacter(int primaryCode);
	}

    private Key mEnterKey;
    private final boolean mSupportsShift;
    private final String mKeyboardName;
    private final String mKeyboardEnabledPref;
    
    private boolean mEnabled = true;
    
    
    protected AnyKeyboard(Context context, int xmlLayoutResId, boolean supportsShift,
    		/*mapping XML id will be added here,*/
    		String keyboardName,
    		String keyboardEnabledPref) 
    {
        super(context, xmlLayoutResId);
        mSupportsShift = supportsShift;
        mKeyboardName = keyboardName;
        mEnabled = true;
        mKeyboardEnabledPref = keyboardEnabledPref;
        //TODO: parsing of the mapping xml:
        //XmlResourceParser p = getResources().getXml(id from the constructor parameter);
        //parse to a HashMap?
    }

//    @Override
//    protected Row createRowFromXml(Resources res, XmlResourceParser parser) 
//    {
//    	Row aRow = super.createRowFromXml(res, parser);
//    	Log.i("AnySoftKeyboard", "createRowFromXml: rowEdgeFlags: "+aRow.rowEdgeFlags+". EDGE_TOP:"+EDGE_TOP+". verticalGap:"+aRow.verticalGap);
//    	if ((aRow.rowEdgeFlags & EDGE_TOP) != 0)
//    	{//this is the top row, I would like to add the swipe hints
//    		aRow.verticalGap += 80;
//    	}
//    	
//    	return aRow;
//    }
    
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, 
            XmlResourceParser parser) {
        Key key = new Key(res, parent, x, y, parser);
        if (key.codes[0] == 10) 
        {
            mEnterKey = key;
        }
        return key;
    }
    
    public void reloadKeyboardConfiguration(SharedPreferences sp)
    {
    	if (mKeyboardEnabledPref == "")
    		mEnabled = true;
    	else
    		mEnabled = sp.getBoolean(mKeyboardEnabledPref, true);
    }
    
    /**
     * This looks at the ime options given by the current editor, to set the
     * appropriate label on the keyboard's enter key (if it has one).
     */
    public void setImeOptions(Resources res, int options) {
        if (mEnterKey == null) {
            return;
        }
        
        switch (options&(EditorInfo.IME_MASK_ACTION|EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_go_key);
                break;
            case EditorInfo.IME_ACTION_NEXT:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_next_key);
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                mEnterKey.icon = res.getDrawable(
                        R.drawable.sym_keyboard_search);
                mEnterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                mEnterKey.iconPreview = null;
                mEnterKey.icon = null;
                mEnterKey.label = res.getText(R.string.label_send_key);
                break;
            default:
                mEnterKey.icon = res.getDrawable(
                        R.drawable.sym_keyboard_return);
                mEnterKey.label = null;
                break;
        }
    }
    
    public boolean getSupportsShift()
    {
    	return mSupportsShift;
    }
    
    public String getKeyboardName()
    {
    	//TODO: this should be taken from the strings.xml, right?
    	return mKeyboardName;
    }
    
    /*
     * This function is overridden by other alphabet keyboards, for nifty icons
     */
    public int getKeyboardIcon()
    {
    	return R.drawable.sym_keyboard_notification_icon;
    }
    
    public boolean isEnabled()
    {
    	return mEnabled;
    }
    
    public boolean isLetter(char letterCode)
    {
    	if (Character.isLetter(letterCode)) 
    		return true;
        else
        	return false;
    }
	public void addSuggestions(String currentWord, ArrayList<String> list) 
	{
	}
}
