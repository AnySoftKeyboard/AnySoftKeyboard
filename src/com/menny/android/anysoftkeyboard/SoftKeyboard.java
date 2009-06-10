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

package com.menny.android.anysoftkeyboard;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.Keyboard.Key;
import android.media.AudioManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard;
import com.menny.android.anysoftkeyboard.keyboards.GenericKeyboard;
import com.menny.android.anysoftkeyboard.keyboards.InternetKeyboard;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardFactory;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider;

/**
 * Example of writing an input method for a soft keyboard.  This code is
 * focused on simplicity over completeness, so it should in no way be considered
 * to be a complete soft keyboard implementation.  Its purpose is to provide
 * a basic example for how you would get started writing an input method, to
 * be fleshed out as appropriate.
 */
public class SoftKeyboard extends InputMethodService 
        implements KeyboardView.OnKeyboardActionListener, OnSharedPreferenceChangeListener {
	
	private enum NextKeyboardType 
	{
		Alphabet,
		SupportsPhysical,
		Any
	}
	
	public static SoftKeyboard msCurrentInstance;
	
	private static final int KEYBOARD_NOTIFICATION_ID = 1;
    
    private KeyboardView mInputView;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;
    
    private StringBuilder mComposing = new StringBuilder();
    //private boolean mPredictionOn;
    private boolean mCompletionOn;
    
    private int mLastDisplayWidth;
    private boolean mCapsLock;
    private long mLastShiftTime;
    
    private AnyKeyboard mSymbolsKeyboard;
    private AnyKeyboard mSymbolsShiftedKeyboard;
    private AnyKeyboard mInternetKeyboard;
    private AnyKeyboard mSimpleNumbersKeyboard;
    //my working keyboards
    private AnyKeyboard[] mKeyboards = null;
    private int mLastSelectedKeyboard = 0;
    
    private AnyKeyboard mCurKeyboard;
    
    private boolean mVibrateOnKeyPress = false;
    private boolean mSoundOnKeyPress = false;
    private boolean mAutoCaps = false;
    private boolean mShowCandidates = false;

	private boolean mKeyboardChangeNotification;

	public static String mChangeKeysMode;
    
	public SoftKeyboard()
	{
		super();
		msCurrentInstance = this;
	}
	
    /**
     * Main initialization of the input method component.  Be sure to call
     * to super class.
     */
    @Override public void onCreate() {
        super.onCreate();
        Log.i("AnySoftKeyboard", "onCreate");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);
    }
    
    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.unregisterOnSharedPreferenceChangeListener(this);
    }
    
    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override public void onInitializeInterface() 
    {
    	Log.i("AnySoftKeyboard", "onInitializeInterface: Have keyboards="+(mKeyboards != null)+". isFullScreen="+super.isFullscreenMode()+". isInputViewShown="+super.isInputViewShown());
    	reloadConfiguration();
        
        if (mKeyboards != null) 
        {
            // Configuration changes can happen after the keyboard gets recreated,
            // so we need to be able to re-build the keyboards if the available
            // space has changed.
            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
        }
        mLastDisplayWidth = getMaxWidth();
        
        //we'll create the keyboard only if needed
        createKeyboards();
    	ensureCurrentKeyboardIsOk();
    }

	private void createKeyboards() {
		mSymbolsKeyboard = new GenericKeyboard(this, R.xml.symbols, false, "Symbols", "");
        mSymbolsShiftedKeyboard = new GenericKeyboard(this, R.xml.symbols_shift, false, "Shift Symbols", "");
        mInternetKeyboard = new InternetKeyboard(this);
        mSimpleNumbersKeyboard = new GenericKeyboard(this, R.xml.simple_numbers, false, "Numbers", "");
        
        mKeyboards = KeyboardFactory.createAlphaBetKeyboards(this);
	}

    private void reloadConfiguration()
    {
    	SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mVibrateOnKeyPress = sp.getBoolean("vibrate_on", false);
        mSoundOnKeyPress = sp.getBoolean("sound_on", false);
        mKeyboardChangeNotification = sp.getBoolean("physical_keyboard_change_notification", true);
        
        if (mSoundOnKeyPress)
        	((AudioManager)getSystemService(Context.AUDIO_SERVICE)).loadSoundEffects();
        
        mAutoCaps = sp.getBoolean("auto_caps", true);
        mShowCandidates = sp.getBoolean("candidates_on", true);
        
        mChangeKeysMode = sp.getString("keyboard_layout_change_method", "1");
        
    	Log.d("AnySoftKeyboard", "Configuration loaded: " +
    			"mVibrateOnKeyPress: "+mVibrateOnKeyPress+
    			"mSoundOnKeyPress: "+mSoundOnKeyPress+
    			"mKeyboardChangeNotification: "+mKeyboardChangeNotification+
    			"mAutoCaps: "+mAutoCaps+
    			"mShowCandidates: "+mShowCandidates+
    			"mChangeKeysMode: "+mChangeKeysMode);
    }
    
	private void ensureCurrentKeyboardIsOk() 
	{        
        //need to check that current keyboard and mLastSelectedKeyboard are enabled.
        if (!mKeyboards[mLastSelectedKeyboard].isEnabled())
        {
        	//ALWAYS starting in Alphabet!
        	nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
        }
        //in the weird case (impossible?) that the  'mLastSelectedKeyboard' is enabled, 
        //but the mCurKeyboard is null.
        if ((mCurKeyboard == null) || (!mCurKeyboard.isEnabled()))
        	mCurKeyboard = mKeyboards[mLastSelectedKeyboard];
        
        if (mInputView != null)
        	mInputView.setKeyboard(mCurKeyboard);
	}
    
    /**
     * Called by the framework when your view for creating input needs to
     * be generated.  This will be called the first time your input method
     * is displayed, and every time it needs to be re-created such as due to
     * a configuration change.
     */
    @Override public View onCreateInputView() 
    {
    	Log.i("AnySoftKeyboard", "onCreateInputView");
    	
    	mInputView = (KeyboardView) getLayoutInflater().inflate(R.layout.input, null);
        mInputView.setOnKeyboardActionListener(this);
        mInputView.setKeyboard(mKeyboards[mLastSelectedKeyboard]);
        //reloadConfiguration();
        
        return mInputView;
    }

//    @Override
//    public void onWindowShown() 
//    {
//    	super.onWindowShown();
//    	TutorialsProvider.ShowTutorialsIfNeeded(this);
//    }
    
    /**
     * Called by the framework when your view for showing candidates needs to
     * be generated, like {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() 
    {
    	Log.i("AnySoftKeyboard", "onCreateCandidatesView. mCompletionOn:"+mCompletionOn);
    	if (mCompletionOn)
    	{
    		mCandidateView = new CandidateView(this);
    		mCandidateView.setService(this);
    		return mCandidateView;
    	}
    	else
    		return null;
    }

    /**
     * This is the main point where we do our initialization of the input method
     * to begin operating on an application.  At this point we have been
     * bound to the client, and are now receiving all of the detailed information
     * about the target of our edits.
     */
    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        Log.i("AnySoftKeyboard", "onStartInput. restarting:"+restarting);
        //reloadConfiguration();
        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);
        updateCandidates();
        
        if (!restarting) 
        {
            // Clear shift states.
            //mMetaState = 0;
        }
        
        mCompletions = null;
        
        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType&EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_NUMBER:
            case EditorInfo.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                //mCurKeyboard = mSymbolsKeyboard;
            	mCurKeyboard = mSimpleNumbersKeyboard;
                break;
                
            case EditorInfo.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                //mCurKeyboard = mSymbolsKeyboard;
            	mCurKeyboard = mSimpleNumbersKeyboard;
                break;
                
            case EditorInfo.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mKeyboards[mLastSelectedKeyboard];
                //mPredictionOn = mShowCandidates;
                mCompletionOn = mShowCandidates;
                
                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType &  EditorInfo.TYPE_MASK_VARIATION;
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    //mPredictionOn = false;
                	mCompletionOn = false;
                }
                
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_URI
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    //mPredictionOn = false;
                	mCompletionOn = false;
                }
                
                if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
                        || variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
                    //special keyboard
                	if (mInternetKeyboard.isEnabled())
                		mCurKeyboard = mInternetKeyboard;
                	else
                		mCurKeyboard = mKeyboards[mLastSelectedKeyboard];
                }
                
                if ((attribute.inputType&EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    //mPredictionOn = false;
                    mCompletionOn = isFullscreenMode() && mShowCandidates;
                }
                
                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;
                
            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mKeyboards[mLastSelectedKeyboard];
                updateShiftKeyState(attribute);
        }
        
        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }
    
    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();
        Log.i("AnySoftKeyboard", "onFinishInput");
        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();
        
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);
        
        mCurKeyboard = mKeyboards[mLastSelectedKeyboard];
        if (mInputView != null) {
            mInputView.closing();
        }
        
        if (mSoundOnKeyPress)
        	((AudioManager)getSystemService(Context.AUDIO_SERVICE)).unloadSoundEffects();
        
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
    }
    
    @Override public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        Log.i("AnySoftKeyboard", "onStartInputView. restarting:"+restarting);
        mInputView.setKeyboard(mCurKeyboard);
        //no need?
        //mInputView.closing();
    }
    
    /**
     * Deal with the editor reporting movement of its cursor.
     */
    @Override public void onUpdateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);
        
        // If the current selection in the text view changes, we should
        // clear whatever candidate text we have.
        if (mComposing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            mComposing.setLength(0);
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.finishComposingText();
            }
        }
    }

    /**
     * This tells us about completions that the editor has determined based
     * on the current text in it.  We want to use this in fullscreen mode
     * to show the completions ourself, since the editor can not be seen
     * in that situation.
     */
    @Override public void onDisplayCompletions(CompletionInfo[] completions) {
    	String paramDetails = "NULL";
    	if (completions != null)
    		paramDetails = "" + completions.length; 
    	Log.i("Completions", "onDisplayCompletions was called with "+paramDetails);
        if (mCompletionOn) 
        {
            mCompletions = completions;
            if (completions == null) {
                setSuggestions(null, false, false);
                return;
            }
            
            List<String> stringList = new ArrayList<String>();
            for (int i=0; i<(completions != null ? completions.length : 0); i++) {
                CompletionInfo ci = completions[i];
                if (ci != null) stringList.add(ci.getText().toString());
            }
            setSuggestions(stringList, true, true);
        }
    }
    
    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) 
        {
            case KeyEvent.KEYCODE_BACK:
                // The InputMethodService already takes care of the back
                // key for us, to dismiss the input method if it is shown.
                // However, our keyboard could be showing a pop-up window
                // that back should dismiss, so we first allow it to do that.
                if (event.getRepeatCount() == 0 && mInputView != null) {
                    if (mInputView.handleBack()) {
                        return true;
                    }
                }
                break;
                
            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;
                
            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;
                
            default:
                // For all other keys, if we want to do transformations on
                // text being entered with a hard keyboard, we need to process
                // it and do the appropriate action.
            	// using physical keyboard is more annoying with candidate view in the way
            	// so we disable it.
            	mCompletionOn = false;

            	if (keyCode == KeyEvent.KEYCODE_SPACE
                        && (event.getMetaState()&KeyEvent.META_ALT_ON) != 0) {
                    InputConnection ic = getCurrentInputConnection();
                    if (ic != null) 
                    {
                        // First, tell the editor that it is no longer in the
                        // shift state, since we are consuming this.
                        ic.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                        //only physical keyboard
                        nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.SupportsPhysical);
                        notifyKeyboardChangeIfNeeded();
                        
                        return true;
                    }
            	}
            	else if(keyCode >= KeyEvent.KEYCODE_A &&
            			keyCode <= KeyEvent.KEYCODE_COMMA &&
            			(mCurKeyboard != null) &&
            			(mCurKeyboard instanceof HardKeyboardTranslator)/* &&
            			((event.getMetaState()&KeyEvent.META_ALT_ON) == 0) &&
            			((event.getMetaState()&KeyEvent.META_SHIFT_ON) == 0)*/)
            	{
            		Log.d("AnySoftKeyborad", "Asking '"+mCurKeyboard.getKeyboardName()+"' to translate key: "+keyCode);
            		char translatedChar = ((HardKeyboardTranslator)mCurKeyboard).translatePhysicalCharacter(keyCode, event.getMetaState());
            		if (translatedChar != 0)
            		{
            			Log.d("AnySoftKeyborad", "'"+mCurKeyboard.getKeyboardName()+"' translated key "+keyCode+" to "+translatedChar);
                		sendKey(translatedChar);
            			return true;
            		}
            		else
            		{
            			Log.d("AnySoftKeyborad", "'"+mCurKeyboard.getKeyboardName()+"' did not translated key "+keyCode+".");
            		}
            	}
        }
        
        return super.onKeyDown(keyCode, event);
    }

    
    /**
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    /**
     * Helper to update the shift state of our keyboard based on the initial
     * editor state.
     */
    private void updateShiftKeyState(EditorInfo attr) 
    {
        if (mInputView != null) 
        {
            int caps = 0;
            
            if ((attr != null) && mAutoCaps)
            {
            	InputConnection ci = getCurrentInputConnection();
            	if (ci != null)
            		caps = ci.getCursorCapsMode(attr.inputType);
            }
            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }
    
    /**
     * Helper to determine if a given character code is alphabetic.
     */
    private boolean isAlphabet(int code) 
    {
        return mCurKeyboard.isLetter((char)code);
    }
    
    /**
     * Helper to send a key down / key up pair to the current editor.
     */
    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }
    
    /**
     * Helper to send a character to the editor as raw key events.
     */
    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    // Implementation of KeyboardViewListener

    public void onKey(int primaryCode, int[] keyCodes) 
    {
    	EditorInfo currentEditorInfo = getCurrentInputEditorInfo();
    	InputConnection currentInputConnection = getCurrentInputConnection();
        if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
            return;
        } else if (primaryCode == AnyKeyboardView.KEYCODE_OPTIONS) {
            // Show a menu or somethin'
        }
        else if (primaryCode == AnyKeyboard.KEYCODE_DOT_COM//my special .COM key
                && mInputView != null) {
        	commitTyped(currentInputConnection);
        	currentInputConnection.commitText(".com", 4);
        } else if (primaryCode == AnyKeyboardView.KEYCODE_DOMAINS_POP_UP//my special .COM key (long press)
                && mInputView != null) {
        	//should open up a popup with all domains
        	commitTyped(currentInputConnection);
        	currentInputConnection.commitText(".co.il", 4);
        } else if (primaryCode == AnyKeyboard.KEYCODE_LANG_CHANGE//my special lang key
                && mInputView != null) {
        	nextKeyboard(currentEditorInfo, NextKeyboardType.Any);//false - not just alphabet
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
                && mInputView != null) 
        {
        	nextSymbolsKeyboard();
        } 
        else if (!isAlphabet(primaryCode))
        {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(currentInputConnection);
            }
            sendKey(primaryCode);
            updateShiftKeyState(currentEditorInfo);
        }
        else
        {
            handleCharacter(primaryCode, keyCodes);
        }
    }

	private void nextSymbolsKeyboard() 
	{
		AnyKeyboard keyboardToSet = null;
		boolean shifted = false;
		if (!mCurKeyboard.getKeyboardName().equals(mSymbolsKeyboard.getKeyboardName()))
		{
			keyboardToSet = mSymbolsKeyboard;
			shifted = false;
		}
		else
		{
			keyboardToSet = mSymbolsShiftedKeyboard;
			shifted = true;
		}
		//setting
		mCurKeyboard = keyboardToSet;
		mInputView.setKeyboard(mCurKeyboard);
		mSymbolsKeyboard.setShifted(shifted);
	}

	private void nextKeyboard(EditorInfo currentEditorInfo, NextKeyboardType keyboardType) 
	{
		Log.d("AnySoftKeyboard", "nextKeyboard: keyboardType="+keyboardType+". currentEditorInfo.inputType="+currentEditorInfo.inputType);
		int variation = currentEditorInfo.inputType &  EditorInfo.TYPE_MASK_VARIATION;
		if ((keyboardType == NextKeyboardType.Any) && 
				mInternetKeyboard.isEnabled() &&
				(variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS 
		        || variation == EditorInfo.TYPE_TEXT_VARIATION_URI)) {
		    //special keyboard
			Log.d("AnySoftKeyboard", "nextKeyboard: Starting in internet textbox.");
			mCurKeyboard = mInternetKeyboard;
		}
		else
		{
			if (mCurKeyboard == null)
			{
				Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. No current keyboard. mLastSelectedKeyboard:"+mLastSelectedKeyboard+".");
			}
			else
			{
				Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. Current keyboard is:"+mCurKeyboard.getKeyboardName()+". mLastSelectedKeyboard:"+mLastSelectedKeyboard+". isEnabled:"+mCurKeyboard.isEnabled());
			}
			//in numeric keyboards, the LANG key will go back to the original alphabet keyboard-
			//so no need to look for the next keyboard, 'mLastSelectedKeyboard' holds the last
			//keyboard used.

			if ((mCurKeyboard == null) || isAlphaBetKeyboard(mCurKeyboard))
			{
				Log.d("AnySoftKeyboard", "nextKeyboard: Current keyboard is alphabet (or null), so i'll look for the next");
				int maxTries = mKeyboards.length;
				do
				{
					mLastSelectedKeyboard++;
					if (mLastSelectedKeyboard >= mKeyboards.length)
						mLastSelectedKeyboard = 0;
					
					Log.d("AnySoftKeyboard", "nextKeyboard: testing: "+mKeyboards[mLastSelectedKeyboard].getKeyboardName()+", which is "+mKeyboards[mLastSelectedKeyboard].isEnabled()+". index="+mLastSelectedKeyboard);
					AnyKeyboard aKeyboard = mKeyboards[mLastSelectedKeyboard];
					if (aKeyboard.isEnabled())
					{
						//we found an enabled keyboard - need to check that it OK
						//for the keyboardType parameter.
						if ((keyboardType == NextKeyboardType.SupportsPhysical) &&
								(!(aKeyboard instanceof HardKeyboardTranslator)))
						{
							//not a valid keyboard, since we wanted physical handling
							//and it is not
						}
						else
						{
							//HOHO! We found a valid keyboard!
							maxTries = 0;
						}
					}
					maxTries--;
				}while(maxTries > 0);
			}
			mCurKeyboard = mKeyboards[mLastSelectedKeyboard];
		}
		Log.i("AnySoftKeyboard", "nextKeyboard: Setting next keyboard to: "+mCurKeyboard.getKeyboardName());
		if (mInputView != null)
			mInputView.setKeyboard(mCurKeyboard);
		updateShiftKeyState(currentEditorInfo);
		mCurKeyboard.setImeOptions(getResources(), currentEditorInfo.imeOptions);
	}

	private void notifyKeyboardChangeIfNeeded() 
	{
		if (mKeyboardChangeNotification)
		{
			//notifying the user about the keyboard. This should be done in open keyboard only.
			//getting the manager
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			//removing last notification
			notificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
			//creating the message
			Notification notification = new Notification(mCurKeyboard.getKeyboardIcon(), mCurKeyboard.getKeyboardName(), System.currentTimeMillis());
	
			Intent notificationIntent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
			
			notification.setLatestEventInfo(getApplicationContext(), "Any Soft Keyboard", mCurKeyboard.getKeyboardName(), contentIntent);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			notification.flags |= Notification.FLAG_NO_CLEAR;
			notification.defaults = 0;//no sound, vibrate, etc.
			//notifying
			notificationManager.notify(KEYBOARD_NOTIFICATION_ID, notification);
		}
	}
    
    private boolean isAlphaBetKeyboard(AnyKeyboard viewedKeyboard)
    {
    	for(int i=0; i<mKeyboards.length; i++)
    	{
    		if (viewedKeyboard.getKeyboardName().equalsIgnoreCase(mKeyboards[i].getKeyboardName()))
    			return true;
    	}
    	
    	return false;
    }

    public void onText(CharSequence text) {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;
        ic.beginBatchEdit();
        if (mComposing.length() > 0) {
            commitTyped(ic);
        }
        ic.commitText(text, 0);
        ic.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() 
    {
        if (mCompletionOn) 
        {
            if (mComposing.length() > 0) 
            {
                ArrayList<String> list = new ArrayList<String>();
                String currentWord = mComposing.toString();
                list.add(currentWord);
                //asking current keyboard for suggestions
                mCurKeyboard.addSuggestions(currentWord, list);
                setSuggestions(list, true, true);
            } 
            else 
            {
                setSuggestions(null, false, false);
            }
        }
    }
    
    public void setSuggestions(List<String> suggestions, boolean completions, boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null /*&& mPredictionOn*/) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }
    
    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
            updateCandidates();
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (mInputView == null) {
            return;
        }
        
        AnyKeyboard currentKeyboard = (AnyKeyboard)mInputView.getKeyboard();
        if (isAlphaBetKeyboard(currentKeyboard))
        {
        	// Alphabet with shift support keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        }
        else
        {//not alpha-bet keyboard
        	nextSymbolsKeyboard();
        }
    }
    
    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) 
        {
            if (mInputView.isShifted()) 
            {
            	for(Key aKey : mCurKeyboard.getKeys())
            	{
            		final int[] aKeyCodes = aKey.codes;
            		if (aKeyCodes[0] == primaryCode)
            		{
            			if (aKeyCodes.length > 1)
                    		primaryCode = aKeyCodes[1];//keyboard specified the shift character
                    	else
                    		primaryCode = Character.toUpperCase(primaryCode);
            			
            			break;
            		}
            	}
            }
        }
        if (isAlphabet(primaryCode)) 
        {
            mComposing.append((char) primaryCode);
            
            if (mCompletionOn)
            	getCurrentInputConnection().setComposingText(mComposing, 1);
            else
            	getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
            
            updateShiftKeyState(getCurrentInputEditorInfo());
            updateCandidates();
        } 
        else 
        {
            getCurrentInputConnection().commitText(String.valueOf((char) primaryCode), 1);
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        if (mInputView != null)
        	mInputView.closing();
    }

    private void checkToggleCapsLock() 
    {
        long now = System.currentTimeMillis();
        if (mLastShiftTime + 400 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }
    
    
    public void pickSuggestionManually(String word) 
    {
//        if (mCompletionOn && mCompletions != null && index >= 0
//                && index < mCompletions.length) {
//            CompletionInfo ci = mCompletions[index];
//            getCurrentInputConnection().commitCompletion(ci);
//            if (mCandidateView != null) {
//                mCandidateView.clear();
//            }
//            updateShiftKeyState(getCurrentInputEditorInfo());
//        } else if (mComposing.length() > 0) {
//            // If we were generating candidate suggestions for the current
//            // text, we would commit one of them here.  But for this sample,
//            // we will just commit the current text.
//            commitTyped(getCurrentInputConnection());
//        }
    	getCurrentInputConnection().commitText(word, word.length());
		mComposing.setLength(0);
		//simulating space
		onKey((int)' ', null);
		if (mCandidateView != null) 
		{
		      mCandidateView.clear();
		}
    }
    
    public void swipeRight() 
    {
    	nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
    }
    
    public void swipeLeft() 
    {
    	nextSymbolsKeyboard();
    }

    public void swipeDown() 
    {
        handleClose();
    }
    
    public void swipeUp() 
    {
    	handleShift();
    }
    
    public void onPress(int primaryCode) {
    	if(mVibrateOnKeyPress)
    	{
    		((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(12);
    	}
    	if(mSoundOnKeyPress)
    	{
    		int keyFX = AudioManager.FX_KEY_CLICK;
    		switch(primaryCode)
    		{
    			case 13:
    				keyFX = AudioManager.FX_KEYPRESS_RETURN;
    			case Keyboard.KEYCODE_DELETE:
    				keyFX = AudioManager.FX_KEYPRESS_DELETE;
    			case 32:
    				keyFX = AudioManager.FX_KEYPRESS_SPACEBAR;
    		}
    		((AudioManager)getSystemService(Context.AUDIO_SERVICE)).playSoundEffect(keyFX);
    	}
    }    

	public void onRelease(int primaryCode) {
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("AnySoftKeyboard", "onSharedPreferenceChanged - key:"+key);
		mKeyboards = null;
		onInitializeInterface();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		Log.d("AnySoftKeyboard", "onConfigurationChanged - newConfig - newConfig.hardKeyboardHidden:"+newConfig.hardKeyboardHidden+" keyboard:"+newConfig.keyboard+" keyboardHidden:"+newConfig.keyboardHidden);
		super.onConfigurationChanged(newConfig);
	}
}
