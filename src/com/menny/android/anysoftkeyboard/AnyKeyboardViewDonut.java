/*
 * Copyright (C) 2010 AnySoftKeyoard.
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
//package com.menny.android.anysoftkeyboard;
//
//import android.content.Context;
//import android.inputmethodservice.Keyboard;
//import android.util.AttributeSet;
//
//public class AnyKeyboardViewDonut extends AnyKeyboardView
//{
//	public AnyKeyboardViewDonut(Context context, AttributeSet attrs) {
//		super(context, attrs);
//	}
//
//	@Override
//	protected void requestSpecialKeysRedraw() {
//		invalidateAllKeys();
//	}
//	
//	@Override
//	public void requestShiftKeyRedraw() {
//		if (canInteractWithUi())
//		{
//			//Log.d("FSGSDFGS", "canInteractWithUi");
//			Keyboard keyboard = getKeyboard();
//			if (keyboard != null)
//			{
//				final int shiftKeyIndex = keyboard.getShiftKeyIndex();
//				if (shiftKeyIndex >= 0)
//					invalidateKey(shiftKeyIndex);
//			}
//		}
//	}
//}