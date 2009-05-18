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

import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.R.drawable;
import com.menny.android.anysoftkeyboard.R.string;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.Keyboard.Row;
import android.view.inputmethod.EditorInfo;

public abstract class AnyKeyboard extends Keyboard {

    private Key mEnterKey;
    private final boolean mSupportsShift;
    private final String mKeyboardName;
    
    private boolean mEnabled = true;
    
    protected AnyKeyboard(Context context, int xmlLayoutResId, boolean supportsShift, String keyboardName) 
    {
        super(context, xmlLayoutResId);
        mSupportsShift = supportsShift;
        mKeyboardName = keyboardName;
        mEnabled = true;
    }
/*
    public AnyKeyboard(Context context, int layoutTemplateResId, 
            CharSequence characters, int columns, int horizontalPadding, boolean supportsShift, String keyboardName, boolean enabled) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
        mSupportsShift = supportsShift;
        mKeyboardName = keyboardName;
        mEnabled = enabled;
    }
*/
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
    	return mKeyboardName;
    }
    
    public boolean getEnabled()
    {
    	return mEnabled;
    }
    
    public void setEnabled(boolean enabled)
    {
    	mEnabled = enabled;
    }
    
    public boolean isLetter(char letterCode)
    {
    	if (Character.isLetter(letterCode)) 
    		return true;
        else
        	return false;
    }
    
//    static class LatinKey extends Keyboard.Key {
//        
//        public LatinKey(Resources res, Keyboard.Row parent, int x, int y, XmlResourceParser parser) {
//            super(res, parent, x, y, parser);
//        }
//        
//        /**
//         * Overriding this method so that we can reduce the target area for the key that
//         * closes the keyboard. 
//         */
//        @Override
//        public boolean isInside(int x, int y) {
//            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
//        }
//    }

}
