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

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.inputmethodservice.*;
import android.media.AudioManager;
import android.os.*;
import android.preference.PreferenceManager;
import android.text.AutoText;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.Dictionary.*;
import com.menny.android.anysoftkeyboard.Dictionary.Dictionary.Language;
import com.menny.android.anysoftkeyboard.KeyboardSwitcher.NextKeyboardType;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider;

/**
 * Input method implementation for Qwerty'ish keyboard.
 */
public class AnySoftKeyboard extends InputMethodService implements
		KeyboardView.OnKeyboardActionListener,
		OnSharedPreferenceChangeListener, AnyKeyboardContextProvider {
	
	//this is determined from the version. It includes "tester", the it will be true
	private static boolean DEBUG = true;
	public static boolean getDEBUG() {return DEBUG;}
	
	private static final boolean TRACE_SDCARD = false;

	private static final int MSG_UPDATE_SUGGESTIONS = 0;
	private static final int MSG_START_TUTORIAL = 1;
	
	
	private static final int KEYCODE_ENTER = 10;
	private static final int KEYCODE_SPACE = ' ';
	private static final int KEYBOARD_NOTIFICATION_ID = 1;
	private static final String PUNCTUATION_CHARACTERS = ".\n!?,:;@<>()[]{}";

	private AnyKeyboardView mInputView;
	private CandidateViewContainer mCandidateViewContainer;
	private CandidateView mCandidateView;
	private Suggest mSuggest;
	private CompletionInfo[] mCompletions;

	private AlertDialog mOptionsDialog;

	KeyboardSwitcher mKeyboardSwitcher;
	private HardKeyboardActionImpl mHardKeyboardAction;

	private UserDictionaryBase mUserDictionary;

	private StringBuilder mComposing = new StringBuilder();
	private WordComposer mWord = new WordComposer();

	private int mCommittedLength;
	private boolean mPredicting;
	private CharSequence mBestWord;
	private boolean mPredictionLandscape;
	private boolean mPredictionOn;
	private boolean mCompletionOn;
	private boolean mAutoSpace;
	private boolean mAutoCorrectOn;
	private boolean mCapsLock;
	// private boolean mVibrateOn;
	private int mVibrationDuration;
	private boolean mSoundOn;
	private boolean mAutoCap;
	private boolean mQuickFixes;
	private boolean mShowSuggestions;
	private boolean mAutoComplete;
	private int mCorrectionMode;
	private String mKeyboardChangeNotificationType;
	private static final String KEYBOARD_NOTIFICATION_ALWAYS = "1";
	private static final String KEYBOARD_NOTIFICATION_ON_PHYSICAL = "2";
	private static final String KEYBOARD_NOTIFICATION_NEVER = "3";

	public static String mChangeKeysMode;

	// Indicates whether the suggestion strip is to be on in landscape
	private boolean mJustAccepted;
	private CharSequence mJustRevertedSeparator;
	
	private AudioManager mAudioManager;
	private NotificationManager mNotificationManager;
	
	private final HardKeyboardTranslator mGenericKeyboardTranslator;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_SUGGESTIONS:
				updateSuggestions();
				break;
			case MSG_START_TUTORIAL:
				if (mInputView.isShown()) 
				{
					TutorialsProvider.ShowTutorialsIfNeeded(AnySoftKeyboard.this, mInputView);
				} else 
				{
					// Try again soon if the view is not yet showing
					sendMessageDelayed(obtainMessage(MSG_START_TUTORIAL), 100);
				}
				break;
			}
		}
	};

	public AnySoftKeyboard()
	{
		mGenericKeyboardTranslator = new GenericPhysicalKeyboardTranslator(this);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("AnySoftKeyboard", "****** Starting AnySoftKeyboard:");
		Log.i("AnySoftKeyboard", "** Locale:"+ getResources().getConfiguration().locale.toString());
		String version = "";
        try {
			PackageInfo info = super.getApplication().getPackageManager().getPackageInfo(getApplication().getPackageName(), 0);
			version = info.versionName;
			Log.i("AnySoftKeyboard", "** Version: "+version);
		} catch (NameNotFoundException e) {
			Log.e("AnySoftKeyboard", "Failed to locate package information! This is very weird... I'm installed.");
		}
		
		DEBUG = version.contains("tester");
		Log.i("AnySoftKeyboard", "** Debug: "+DEBUG);
		
		//showToastMessage(R.string.toast_lengthy_start_up_operation, true);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// setStatusIcon(R.drawable.ime_qwerty);
		loadSettings();
		mKeyboardSwitcher = new KeyboardSwitcher(this);
		if (mSuggest == null)
		{
			// should it be always on?
			if (mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS))
				notifyKeyboardChangeIfNeeded();
			initSuggest(/* getResources().getConfiguration().locale.toString() */);
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);
		
		mHardKeyboardAction = new HardKeyboardActionImpl();
	}

	private void initSuggest(/* String locale */) {
		// mLocale = locale;
		mSuggest = new Suggest(this/* , R.raw.main */);
		mSuggest.setCorrectionMode(mCorrectionMode);
		mUserDictionary = DictionaryFactory.createUserDictionary(this);
		mSuggest.setUserDictionary(mUserDictionary);
		setMainDictionaryForCurrentKeyboard();
		// mWordSeparators = getResources().getString(R.string.word_separators);
		// mSentenceSeparators =
		// getResources().getString(R.string.sentence_separators);
	}

	@Override
	public void onDestroy() {
		DictionaryFactory.close();

		// unregisterReceiver(mReceiver);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		sp.unregisterOnSharedPreferenceChangeListener(this);
		if (mSoundOn) {
			Log.i("AnySoftKeyboard", "Releasing sounds effects from AUDIO_SERVICE");
			mAudioManager.unloadSoundEffects();
		}

		mNotificationManager.cancel(KEYBOARD_NOTIFICATION_ID);

		super.onDestroy();
	}

	@Override
	public void onFinishInputView(boolean finishingInput) {
		if (DEBUG) Log.d("AnySoftKeyboard", "onFinishInputView(finishingInput:"+finishingInput+")");
		super.onFinishInputView(finishingInput);
		if (!mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS)) {
			mNotificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
		}
		
		if (finishingInput)
			resetComposing();//clearing any predications
	};

	@Override
	public View onCreateInputView() {
		mInputView = (AnyKeyboardView) getLayoutInflater().inflate(R.layout.input, null);
		
		mKeyboardSwitcher.setInputView(mInputView);
		mKeyboardSwitcher.makeKeyboards(false);
		mInputView.setOnKeyboardActionListener(this);
		mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, null);
		
		startTutorial();
		
		return mInputView;
	}

	@Override
	public View onCreateCandidatesView() {
		mKeyboardSwitcher.makeKeyboards(false);
		mCandidateViewContainer = (CandidateViewContainer) getLayoutInflater().inflate(R.layout.candidates, null);
		mCandidateViewContainer.initViews();
		mCandidateView = (CandidateView) mCandidateViewContainer.findViewById(R.id.candidates);
		mCandidateView.setService(this);
		setCandidatesViewShown(true);
		return mCandidateViewContainer;
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		if (DEBUG) Log.d("AnySoftKeyboard", "onStartInputView(EditorInfo:"+attribute.imeOptions+","+attribute.inputType+", restarting:"+restarting+")");
		super.onStartInputView(attribute, restarting);
		
		mKeyboardSwitcher.makeKeyboards(false);
		resetComposing();//clearing any predications
		TextEntryState.newSession(this);

		mPredictionOn = false;
		mCompletionOn = false;
		mCompletions = null;
		mCapsLock = false;
		switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
		case EditorInfo.TYPE_CLASS_NUMBER:
		case EditorInfo.TYPE_CLASS_DATETIME:
		case EditorInfo.TYPE_CLASS_PHONE:
			mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_PHONE, attribute);
			break;
		case EditorInfo.TYPE_CLASS_TEXT:
			mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, attribute);
			// startPrediction();
			mPredictionOn = true;
			// Make sure that passwords are not displayed in candidate view
			final int variation = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
			if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
					|| variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
				mPredictionOn = false;
			}
			if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
					|| variation == EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME) {
				mAutoSpace = false;
			} else {
				mAutoSpace = true;
			}
			if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
				mPredictionOn = false;
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_EMAIL, attribute);
			} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
				mPredictionOn = false;
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_URL, attribute);
			} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_IM, attribute);
			} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_FILTER) {
				mPredictionOn = false;
			}
			if ((attribute.inputType & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
				mPredictionOn = false;
				mCompletionOn = true && isFullscreenMode();
			}
			updateShiftKeyState(attribute);
			break;
		default:
			mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, attribute);
			updateShiftKeyState(attribute);
		}
		mComposing.setLength(0);
		mPredicting = false;
		// mDeleteCount = 0;
		setCandidatesViewShown(false);
		// loadSettings();
		if (AutoText.getSize(mInputView) < 1)
			mQuickFixes = true;
		
		if (mSuggest != null) {
			mSuggest.setCorrectionMode(mCorrectionMode);
		}
		
		mPredictionOn = mPredictionOn && mCorrectionMode > 0;
		
		if (mInputView != null)
		{
			mInputView.closing();
		
			if (mCandidateView != null)
				mCandidateView.setSuggestions(null, false, false, false);
		}
		
		if (TRACE_SDCARD)
			Debug.startMethodTracing("anysoftkeyboard_log.trace");
	}

	@Override
	public void onFinishInput() {
		if (DEBUG) Log.d("AnySoftKeyboard", "onFinishInput()");
		super.onFinishInput();

		if (mInputView != null) {
			mInputView.closing();
		}

		if (!mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS)) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
		}
		//clearing any predications
		resetComposing();
		// releasing some memory. Dictionaries, completions, etc.
		System.gc();
	}

	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,	candidatesStart, candidatesEnd);
		// If the current selection in the text view changes, we should
		// clear whatever candidate text we have.
		if (mComposing.length() > 0 && mPredicting
				&& (newSelStart != candidatesEnd || newSelEnd != candidatesEnd)) {
			resetComposing();
		} else if (!mPredicting
				&& !mJustAccepted
				&& TextEntryState.getState() == TextEntryState.STATE_ACCEPTED_DEFAULT) {
			TextEntryState.reset();
		}
		mJustAccepted = false;
	}

	private void resetComposing() {
		mComposing.setLength(0);
		mPredicting = false;
		updateSuggestions();
		TextEntryState.reset();
		InputConnection ic = getCurrentInputConnection();
		if (ic != null) {
			ic.finishComposingText();
		}
	}

	@Override
	public void hideWindow() {
		if (TRACE_SDCARD)
			Debug.stopMethodTracing();
		
		if (mOptionsDialog != null && mOptionsDialog.isShowing()) {
			mOptionsDialog.dismiss();
			mOptionsDialog = null;
		}
		// if (mTutorial != null) {
		// mTutorial.close();
		// mTutorial = null;
		// }
		super.hideWindow();
		TextEntryState.endSession();
	}

	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) 
	{
		if (DEBUG)
		{
			Log.i("AnySoftKeyboard", "Received completions:");
			for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
				Log.i("AnySoftKeyboard", "  #" + i + ": " + completions[i]);
			}
		}
		
		if (mCompletionOn) {
			mCompletions = completions;
			if (completions == null) {
				mCandidateView.setSuggestions(null, false, false, false);
				return;
			}

			List<CharSequence> stringList = new ArrayList<CharSequence>();
			for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
				CompletionInfo ci = completions[i];
				if (ci != null)
					stringList.add(ci.getText());
			}
			// CharSequence typedWord = mWord.getTypedWord();
			mCandidateView.setSuggestions(stringList, true, true, true);
			mBestWord = null;
			setCandidatesViewShown(isCandidateStripVisible() || mCompletionOn);
		}
	}
	/*
	@Override
	public void setCandidatesViewShown(boolean shown) {
		//we want to show candidates only when needed
		super.setCandidatesViewShown(shown && isPredictionOn());
	}
	*/
	@Override
	public void onComputeInsets(InputMethodService.Insets outInsets) {
		super.onComputeInsets(outInsets);
		if (!isFullscreenMode()) {
			outInsets.contentTopInsets = outInsets.visibleTopInsets;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// For all other keys, if we want to do transformations on
		// text being entered with a hard keyboard, we need to process
		// it and do the appropriate action.
		// using physical keyboard is more annoying with candidate view in
		// the way
		// so we disable it.
		InputConnection ic = getCurrentInputConnection();
		/*
		// to clear the underline.
		commitTyped(ic);// to clear the underline.

		mPredicting = false;
		*/
		if (DEBUG)
			Log.d("AnySoftKeyboard", "Event: Key:"+event.getKeyCode()+" Shift:"+((event.getMetaState()&KeyEvent.META_SHIFT_ON) != 0)+" ALT:"+((event.getMetaState()&KeyEvent.META_ALT_ON) != 0)+" Repeats:"+event.getRepeatCount());
		
		boolean handledByAction = mHardKeyboardAction.initializeAction(event);
		if (!handledByAction)
		{
			switch (keyCode) 
			{
			case KeyEvent.KEYCODE_BACK:
				if (event.getRepeatCount() == 0 && mInputView != null) {
					if (mInputView.handleBack()) 
					{
						// consuming the meta keys
						mHardKeyboardAction.resetMetaState();
						if (ic != null)
							ic.clearMetaKeyStates(Integer.MAX_VALUE);//translated, so we also take care of the metakeys.
	
						return true;
					} /*
					 * else if (mTutorial != null) { mTutorial.close(); mTutorial =
					 * null; }
					 */
				}
				break;
			// case KeyEvent.KEYCODE_DPAD_DOWN:
			// case KeyEvent.KEYCODE_DPAD_UP:
			// case KeyEvent.KEYCODE_DPAD_LEFT:
			// case KeyEvent.KEYCODE_DPAD_RIGHT:
			// // If tutorial is visible, don't allow dpad to work
			// if (mTutorial != null) {
			// return true;
			// }
			// break;
			case KeyEvent.KEYCODE_DEL:
				onKey(Keyboard.KEYCODE_DELETE, new int[]{Keyboard.KEYCODE_DELETE});
				return true;
			case KeyEvent.KEYCODE_SPACE:
				if (mHardKeyboardAction.isAltActive()) 
				{	
					Log.d("AnySoftKeyborad", "User pressed ALT+SPACE, moving to next physical keyboard.");
					// consuming the meta keys
					mHardKeyboardAction.resetMetaState();
					if (ic != null)
						ic.clearMetaKeyStates(Integer.MAX_VALUE);//translated, so we also take care of the metakeys.
					// only physical keyboard
					nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.AlphabetSupportsPhysical);
	
					return true;
				}
				else
				{
					//still handling it
					onKey(32, new int[]{32});
					return true;
				}
			default:
				// sometimes, the physical keyboard will delete input, and then add some.
				// we'll try to make it nice
				if (ic != null)
					ic.beginBatchEdit();
				try 
				{
					if (mKeyboardSwitcher.isCurrentKeyboardPhysical()) 
					{
						AnyKeyboard current = mKeyboardSwitcher.getCurrentKeyboard();
						String keyboardName = current.getKeyboardName();
						HardKeyboardTranslator keyTranslator = (HardKeyboardTranslator)current;
						boolean translated = askTranslatorToTranslateHardKeyboardAction(keyCode, ic, keyboardName, keyTranslator);
						if (translated)
							return true;
					}
					//if we reached here, it means that either the keyboard is has no physical translation
					//or the it does not have a translation for the pressed keys.
					boolean translated = askTranslatorToTranslateHardKeyboardAction(keyCode, ic, "GENERIC", mGenericKeyboardTranslator);
					if (translated)
						return true;
					
					return super.onKeyDown(keyCode, event);
				} finally {
					if (ic != null)
						ic.endBatchEdit();
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean askTranslatorToTranslateHardKeyboardAction(int keyCode,
			InputConnection ic, String keyboardName,
			HardKeyboardTranslator keyTranslator) 
	{
		if (AnySoftKeyboard.DEBUG) Log.d("AnySoftKeyborad", "Asking '"	+ keyboardName + "' to translate key: " + keyCode);
		if (DEBUG) Log.v("AnySoftKeyboard", "Hard Keyboard Action before translation: Shift: "+mHardKeyboardAction.isShiftActive()+", Alt: "+mHardKeyboardAction.isAltActive()+", Key code: "+mHardKeyboardAction.getKeyCode()+", changed: "+mHardKeyboardAction.getKeyCodeWasChanged());
		keyTranslator.translatePhysicalCharacter(mHardKeyboardAction);						
		if (DEBUG) Log.v("AnySoftKeyboard", "Hard Keyboard Action after translation: Shift: "+mHardKeyboardAction.isShiftActive()+", Alt: "+mHardKeyboardAction.isAltActive()+", Key code: "+mHardKeyboardAction.getKeyCode()+", changed: "+mHardKeyboardAction.getKeyCodeWasChanged());

		final char translatedChar = (char)mHardKeyboardAction.consumeKeyCode();
		if (mHardKeyboardAction.getKeyCodeWasChanged()) 
		{
			// consuming the meta keys
			//Since I'm handling the physical keys, I also need to clear the meta state
			if (ic != null)
			{
				//the clear should be done only if we are not in sticky mode
				int metaStateToClear = Integer.MAX_VALUE;
				if (mHardKeyboardAction.isShiftActive())
					metaStateToClear -= KeyEvent.META_SHIFT_ON;
				if (mHardKeyboardAction.isAltActive())
					metaStateToClear -= KeyEvent.META_ALT_ON;
				
				ic.clearMetaKeyStates(metaStateToClear);//translated, so we also take care of the metakeys.
			}
			
			if (AnySoftKeyboard.DEBUG)
				Log.d("AnySoftKeyborad", "'"+ keyboardName	+ "' translated key " + keyCode + " to "+ translatedChar);

			onKey(translatedChar, new int[] { translatedChar });
			return true;
		} else {
			if (AnySoftKeyboard.DEBUG)
				Log.d("AnySoftKeyborad", "'"+ keyboardName+ "' did not translated key " + keyCode+ ".");
			return false;
		}
	}
	
	private void notifyKeyboardChangeIfNeeded() {
		// Log.d("anySoftKeyboard","notifyKeyboardChangeIfNeeded");
		// Thread.dumpStack();
		if (mKeyboardSwitcher == null)// happens on first onCreate.
			return;

		if ((mKeyboardSwitcher.isAlphabetMode()) && 
				!mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_NEVER)) 
		{
			AnyKeyboard current = mKeyboardSwitcher.getCurrentKeyboard();
			// notifying the user about the keyboard.
			// creating the message
			Notification notification = new Notification(current.getKeyboardIcon(), current.getKeyboardName(), System.currentTimeMillis());

			Intent notificationIntent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

			notification.setLatestEventInfo(getApplicationContext(),
					"Any Soft Keyboard", current.getKeyboardName(),
					contentIntent);
			if (mKeyboardChangeNotificationType.equals("1")) {
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				notification.flags |= Notification.FLAG_NO_CLEAR;
			} else {
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
			}
			notification.defaults = 0;// no sound, vibrate, etc.
			// notifying
			mNotificationManager.notify(KEYBOARD_NOTIFICATION_ID, notification);
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			// // If tutorial is visible, don't allow dpad to work
			// if (mTutorial != null) {
			// return true;
			// }
			// Enable shift key and DPAD to do selections
			if (mInputView != null && mInputView.isShown()
					&& mInputView.isShifted()) {
				event = new KeyEvent(event.getDownTime(), event.getEventTime(),
						event.getAction(), event.getKeyCode(), event
								.getRepeatCount(), event.getDeviceId(), event
								.getScanCode(), KeyEvent.META_SHIFT_LEFT_ON
								| KeyEvent.META_SHIFT_ON);
				InputConnection ic = getCurrentInputConnection();
				if (ic != null)
					ic.sendKeyEvent(event);
				return true;
			}
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private void commitTyped(InputConnection inputConnection) {
		if (mPredicting) {
			mPredicting = false;
			if (mComposing.length() > 0) {
				if (inputConnection != null) {
					inputConnection.commitText(mComposing, 1);
				}
				mCommittedLength = mComposing.length();
				TextEntryState.acceptedTyped(mComposing);
			}
			updateSuggestions();
		}
	}

	public void updateShiftKeyState(EditorInfo attr) {
		InputConnection ic = getCurrentInputConnection();
		if (attr != null && mInputView != null
				&& mKeyboardSwitcher.isAlphabetMode() && ic != null) {
			int caps = 0;
			EditorInfo ei = getCurrentInputEditorInfo();
			if (mAutoCap && ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
				caps = ic.getCursorCapsMode(attr.inputType);
			}
			mInputView.setShifted(mCapsLock || caps != 0);
		}
	}

	private void swapPunctuationAndSpace() {
		final InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		CharSequence lastTwo = ic.getTextBeforeCursor(2, 0);
		if (lastTwo != null && lastTwo.length() == 2
				&& lastTwo.charAt(0) == KEYCODE_SPACE
				&& isPunctuationCharacter(lastTwo.charAt(1))) {
			ic.beginBatchEdit();
			ic.deleteSurroundingText(2, 0);
			ic.commitText(lastTwo.charAt(1) + " ", 1);
			ic.endBatchEdit();
			updateShiftKeyState(getCurrentInputEditorInfo());
		}
	}

	private void doubleSpace() {
		// if (!mAutoPunctuate) return;
		if (mCorrectionMode == Suggest.CORRECTION_NONE)
			return;
		final InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		CharSequence lastThree = ic.getTextBeforeCursor(3, 0);
		if (lastThree != null && lastThree.length() == 3
				&& Character.isLetterOrDigit(lastThree.charAt(0))
				&& lastThree.charAt(1) == KEYCODE_SPACE
				&& lastThree.charAt(2) == KEYCODE_SPACE) {
			ic.beginBatchEdit();
			ic.deleteSurroundingText(2, 0);
			ic.commitText(". ", 1);
			ic.endBatchEdit();
			updateShiftKeyState(getCurrentInputEditorInfo());
		}
	}

	public boolean addWordToDictionary(String word) {
		mUserDictionary.addWord(word, 128);
		return true;
	}

	/**
	 * Helper to determine if a given character code is alphabetic.
	 */
	private boolean isAlphabet(int code) {
		return mKeyboardSwitcher.getCurrentKeyboard().isLetter((char)code);
	}

	// Implementation of KeyboardViewListener

	public void onKey(int primaryCode, int[] keyCodes) {
		// long when = SystemClock.uptimeMillis();
		// if (primaryCode != Keyboard.KEYCODE_DELETE ||
		// when > mLastKeyTime + QUICK_PRESS) {
		// mDeleteCount = 0;
		// }
		// mLastKeyTime = when;
		switch (primaryCode) {
		case Keyboard.KEYCODE_DELETE:
			handleBackspace();
			// mDeleteCount++;
			break;
		case Keyboard.KEYCODE_SHIFT:
			handleShift();
			break;
		case Keyboard.KEYCODE_CANCEL:
			if (mOptionsDialog == null || !mOptionsDialog.isShowing()) {
				handleClose();
			}
			break;
		case AnyKeyboardView.KEYCODE_OPTIONS:
			showOptionsMenu();
			break;
		case AnyKeyboardView.KEYCODE_SHIFT_LONGPRESS:
			if (mCapsLock) {
				handleShift();
			} else {
				toggleCapsLock();
			}
			break;
		case Keyboard.KEYCODE_MODE_CHANGE:
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Symbols);
			break;
		case AnyKeyboard.KEYCODE_LANG_CHANGE:
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
			break;
		default:
			if (isWordSeparator(primaryCode)) {
				handleSeparator(primaryCode);
			} else {
				handleCharacter(primaryCode, keyCodes);
			}
			// Cancel the just reverted state
			mJustRevertedSeparator = null;
		}
	}

	public void onText(CharSequence text) {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		ic.beginBatchEdit();
		if (mPredicting) {
			commitTyped(ic);
		}
		ic.commitText(text, 1);
		ic.endBatchEdit();
		updateShiftKeyState(getCurrentInputEditorInfo());
		mJustRevertedSeparator = null;
	}

	private void handleBackspace() {
		boolean deleteChar = false;
		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		if (mPredicting) {
			final int length = mComposing.length();
			if (length > 0) {
				mComposing.delete(length - 1, length);
				mWord.deleteLast();
				ic.setComposingText(mComposing, 1);
				if (mComposing.length() == 0) {
					mPredicting = false;
				}
				postUpdateSuggestions();
			} else {
				ic.deleteSurroundingText(1, 0);
			}
		} else {
			deleteChar = true;
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
		TextEntryState.backspace();
		if (TextEntryState.getState() == TextEntryState.STATE_UNDO_COMMIT) {
			revertLastWord(deleteChar);
			return;
		} else if (deleteChar) {
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			// if (mDeleteCount > DELETE_ACCELERATE_AT) {
			// sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			// }
		}
		mJustRevertedSeparator = null;
	}

	private void handleShift() {
		// Keyboard currentKeyboard = mInputView.getKeyboard();
		if (mKeyboardSwitcher.isAlphabetMode()) {
			// Alphabet keyboard
			checkToggleCapsLock();
			mInputView.setShifted(mCapsLock || !mInputView.isShifted());
		} else {
			mKeyboardSwitcher.toggleShift();
		}
	}

	private void handleCharacter(int primaryCode, int[] keyCodes) {
		// Log.d("AnySoftKeyboard",
		// "handleCharacter: "+primaryCode+", isPredictionOn:"+isPredictionOn()+", mPredicting:"+mPredicting);
		if (isAlphabet(primaryCode) && isPredictionOn()
				&& !isCursorTouchingWord()) {
			if (!mPredicting) {
				mPredicting = true;
				mComposing.setLength(0);
				mWord.reset();
			}
		}
		primaryCode = translatePrimaryCodeFromCurrentKeyboard(primaryCode);
		// if (mInputView.isShifted()) {
		// primaryCode = Character.toUpperCase(primaryCode);
		// }
		if (mPredicting) {
			if (mInputView.isShifted() && mComposing.length() == 0) {
				mWord.setCapitalized(true);
			}
			mComposing.append((char) primaryCode);
			mWord.add(primaryCode, keyCodes);
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.setComposingText(mComposing, 1);
			}
			postUpdateSuggestions();
		} else {
			sendKeyChar((char) primaryCode);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
		// measureCps();
		TextEntryState.typedCharacter((char) primaryCode,
				isWordSeparator(primaryCode));
	}

	private int translatePrimaryCodeFromCurrentKeyboard(int primaryCode) {
		if (AnySoftKeyboard.DEBUG)
			Log.d("AnySoftKeyboard", "translatePrimaryCodeFromCurrentKeyboard: " + primaryCode);
		if (isInputViewShown()) {
			if (AnySoftKeyboard.DEBUG)
				Log.v("AnySoftKeyboard", "translatePrimaryCodeFromCurrentKeyboard: isInputViewShown");
			
			if (mInputView.isShifted()) {
				if (AnySoftKeyboard.DEBUG)
					Log.d("AnySoftKeyboard", "translatePrimaryCodeFromCurrentKeyboard: mInputView.isShifted()");
				return mKeyboardSwitcher.getCurrentKeyboard().getShiftedKeyValue(primaryCode);
			}
		}
		return primaryCode;
	}

	private void handleSeparator(int primaryCode) {
		boolean pickedDefault = false;
		// Handle separator
		InputConnection ic = getCurrentInputConnection();
		if (ic != null) {
			ic.beginBatchEdit();
		}
		if (mPredicting) {
			// In certain languages where single quote is a separator, it's
			// better
			// not to auto correct, but accept the typed word. For instance,
			// in Italian dov' should not be expanded to dove' because the
			// elision
			// requires the last vowel to be removed.
			if (mAutoCorrectOn
					&& primaryCode != '\''
					&& (mJustRevertedSeparator == null
							|| mJustRevertedSeparator.length() == 0
							|| mJustRevertedSeparator.charAt(0) != primaryCode)) 
			{
				pickDefaultSuggestion();
				pickedDefault = true;
			} else {
				commitTyped(ic);
			}
		}
		sendKeyChar((char) primaryCode);
		TextEntryState.typedCharacter((char) primaryCode, true);
		if (TextEntryState.getState() == TextEntryState.STATE_PUNCTUATION_AFTER_ACCEPTED
				&& primaryCode != KEYCODE_ENTER) {
			swapPunctuationAndSpace();
		} else if (isPredictionOn() && primaryCode == ' ') {
			// else if (TextEntryState.STATE_SPACE_AFTER_ACCEPTED) {
			doubleSpace();
		}
		if (pickedDefault && mBestWord != null) {
			TextEntryState.acceptedDefault(mWord.getTypedWord(), mBestWord);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
		if (ic != null) {
			ic.endBatchEdit();
		}
	}

	private void handleClose() {
		commitTyped(getCurrentInputConnection());
		requestHideSelf(0);
		mInputView.closing();
		TextEntryState.endSession();
	}

	private void checkToggleCapsLock() {
		if (mKeyboardSwitcher.getCurrentKeyboard().isShifted()) {
			toggleCapsLock();
		}
	}

	private void toggleCapsLock() {
		mCapsLock = !mCapsLock;
		if (mKeyboardSwitcher.isAlphabetMode()) {
			mKeyboardSwitcher.getCurrentKeyboard().setShiftLocked(mCapsLock);
		}
	}

	private void postUpdateSuggestions() {
		mHandler.removeMessages(MSG_UPDATE_SUGGESTIONS);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_SUGGESTIONS), 100);
	}

	private boolean isPredictionOn() {
		boolean predictionOn = mPredictionOn;
		//if (!onEvaluateInputViewShown()) predictionOn &= mPredictionLandscape;
		return predictionOn;
	}

	private boolean isCandidateStripVisible() {
		boolean shown = isPredictionOn() && mShowSuggestions;
		if (!onEvaluateInputViewShown()) shown &= mPredictionLandscape;
		return shown;
	}

	private void updateSuggestions() {
		if (AnySoftKeyboard.DEBUG)
			Log.d("AnySoftKeyboard", "updateSuggestions: has mSuggest:"
					+ (mSuggest != null) + ", isPredictionOn:"
					+ isPredictionOn() + ", mPredicting:" + mPredicting
					+ ", mCorrectionMode:" + mCorrectionMode);
		// Check if we have a suggestion engine attached.
		if (mSuggest == null) {
			return;
		}
		
		final boolean showSuggestions = (mPredicting  && isPredictionOn() && isCandidateStripVisible());

		if  (!showSuggestions)
		{
			if (mCandidateView != null)
				mCandidateView.setSuggestions(null, false, false, false);
			return;
		}

		List<CharSequence> stringList = mSuggest.getSuggestions(mInputView,	mWord, false);
		boolean correctionAvailable = mSuggest.hasMinimalCorrection();
		// || mCorrectionMode == mSuggest.CORRECTION_FULL;
		CharSequence typedWord = mWord.getTypedWord();
		// If we're in basic correct
		boolean typedWordValid = mSuggest.isValidWord(typedWord);
		if (mCorrectionMode == Suggest.CORRECTION_FULL) {
			correctionAvailable |= typedWordValid;
		}

		mCandidateView.setSuggestions(stringList, false, typedWordValid, correctionAvailable);
		if (stringList.size() > 0) {
			if (correctionAvailable && !typedWordValid && stringList.size() > 1) {
				mBestWord = stringList.get(1);
			} else {
				mBestWord = typedWord;
			}
		} else {
			mBestWord = null;
		}
		setCandidatesViewShown(isCandidateStripVisible() || mCompletionOn);
	}

	private void pickDefaultSuggestion() {
		// Complete any pending candidate query first
		if (mHandler.hasMessages(MSG_UPDATE_SUGGESTIONS)) {
			mHandler.removeMessages(MSG_UPDATE_SUGGESTIONS);
			updateSuggestions();
		}
		if (mBestWord != null) {
			TextEntryState.acceptedDefault(mWord.getTypedWord(), mBestWord);
			mJustAccepted = true;
			pickSuggestion(mBestWord);
		}
	}

	public void pickSuggestionManually(int index, CharSequence suggestion) {
		if (mCompletionOn && mCompletions != null && index >= 0
				&& index < mCompletions.length) {
			CompletionInfo ci = mCompletions[index];
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.commitCompletion(ci);
			}
			mCommittedLength = suggestion.length();
			if (mCandidateView != null) {
				mCandidateView.clear();
			}
			updateShiftKeyState(getCurrentInputEditorInfo());
			return;
		}
		pickSuggestion(suggestion);
		TextEntryState.acceptedSuggestion(mComposing.toString(), suggestion);
		// Follow it with a space
		if (mAutoSpace) {
			sendSpace();
		}
		// Fool the state watcher so that a subsequent backspace will not do a
		// revert
		TextEntryState.typedCharacter((char) KEYCODE_SPACE, true);
	}

	private void pickSuggestion(CharSequence suggestion) {
		if (mCapsLock) {
			suggestion = suggestion.toString().toUpperCase();
		} else if (preferCapitalization()
				|| (mKeyboardSwitcher.isAlphabetMode() && mInputView.isShifted())) 
		{
			suggestion = Character.toUpperCase(suggestion.charAt(0))
					+ suggestion.subSequence(1, suggestion.length()).toString();
		}
		InputConnection ic = getCurrentInputConnection();
		if (ic != null) {
			ic.commitText(suggestion, 1);
		}
		mPredicting = false;
		mCommittedLength = suggestion.length();
		if (mCandidateView != null) {
			mCandidateView.setSuggestions(null, false, false, false);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	private boolean isCursorTouchingWord() {
		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return false;
		CharSequence toLeft = ic.getTextBeforeCursor(1, 0);
		CharSequence toRight = ic.getTextAfterCursor(1, 0);
		if (!TextUtils.isEmpty(toLeft) && !isWordSeparator(toLeft.charAt(0))) {
			return true;
		}
		if (!TextUtils.isEmpty(toRight) && !isWordSeparator(toRight.charAt(0))) {
			return true;
		}
		return false;
	}

	public void revertLastWord(boolean deleteChar) {
		final int length = mComposing.length();
		if (!mPredicting && length > 0) {
			final InputConnection ic = getCurrentInputConnection();
			mPredicting = true;
			ic.beginBatchEdit();
			mJustRevertedSeparator = ic.getTextBeforeCursor(1, 0);
			if (deleteChar)
				ic.deleteSurroundingText(1, 0);
			int toDelete = mCommittedLength;
			CharSequence toTheLeft = ic
					.getTextBeforeCursor(mCommittedLength, 0);
			if (toTheLeft != null && toTheLeft.length() > 0
					&& isWordSeparator(toTheLeft.charAt(0))) {
				toDelete--;
			}
			ic.deleteSurroundingText(toDelete, 0);
			ic.setComposingText(mComposing, 1);
			TextEntryState.backspace();
			ic.endBatchEdit();
			postUpdateSuggestions();
		} else {
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			mJustRevertedSeparator = null;
		}
	}

	// protected String getWordSeparators() {
	// return mWordSeparators;
	// }

	public boolean isWordSeparator(int code) {
		// String separators = getWordSeparators();
		// return separators.contains(String.valueOf((char)code));
		return (!isAlphabet(code));
	}

	public boolean isPunctuationCharacter(int code) {
		return PUNCTUATION_CHARACTERS.contains(String.valueOf((char) code));
	}

	private void sendSpace() {
		sendKeyChar((char) KEYCODE_SPACE);
		updateShiftKeyState(getCurrentInputEditorInfo());
		// onKey(KEY_SPACE[0], KEY_SPACE);
	}

	public boolean preferCapitalization() {
		return mWord.isCapitalized();
	}

	public void swipeRight() {
		// this should be done only if swipe was enabled
		if (mChangeKeysMode.equals("3")) {
			// TODO: space/backspace (depends on direction of keyboard)
		} else {
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
		}
	}

	public void swipeLeft() {
		// this should be done only if swipe was enabled
		if (mChangeKeysMode.equals("3")) {
			// TODO: space/backspace (depends on direction of keyboard)
		} else {
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Symbols);
		}
	}

	private void nextKeyboard(EditorInfo currentEditorInfo,
			KeyboardSwitcher.NextKeyboardType type) {
		Log.d("AnySoftKeyboard", "nextKeyboard: currentEditorInfo.inputType="
				+ currentEditorInfo.inputType + " type:" + type);

		AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
		if (currentKeyboard == null) {
			if (AnySoftKeyboard.DEBUG)
				Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. No current keyboard.");
		} else {
			if (AnySoftKeyboard.DEBUG)
				Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. Current keyboard is:" + currentKeyboard.getKeyboardName());
		}
		// in numeric keyboards, the LANG key will go back to the original
		// alphabet keyboard-
		// so no need to look for the next keyboard, 'mLastSelectedKeyboard'
		// holds the last
		// keyboard used.
		currentKeyboard = mKeyboardSwitcher.nextKeyboard(currentEditorInfo,	type);

		Log.i("AnySoftKeyboard", "nextKeyboard: Setting next keyboard to: "
				+ currentKeyboard.getKeyboardName());
		updateShiftKeyState(currentEditorInfo);
		// changing dictionary
		setMainDictionaryForCurrentKeyboard();
		// Notifying if needed
		if ((mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS))
				|| (mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ON_PHYSICAL) && (type == NextKeyboardType.AlphabetSupportsPhysical))) 
		{
			notifyKeyboardChangeIfNeeded();
		}
	}

	public void swipeDown() {
		handleClose();
	}

	public void swipeUp() {
		handleShift();
	}

	public void onPress(int primaryCode) {
		if (mVibrationDuration > 0) {
			if (AnySoftKeyboard.DEBUG)
				Log.d("AnySoftKeyboard", "Vibrating on key-pressed");
			((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
					.vibrate(mVibrationDuration);
		}
		if (mSoundOn/* && (!mSilentMode) */) {
			AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			// Will use sound effects ONLY if the device is not muted.
			if (manager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				int keyFX = AudioManager.FX_KEY_CLICK;
				switch (primaryCode) {
				case 13:
					keyFX = AudioManager.FX_KEYPRESS_RETURN;
				case Keyboard.KEYCODE_DELETE:
					keyFX = AudioManager.FX_KEYPRESS_DELETE;
				case 32:
					keyFX = AudioManager.FX_KEYPRESS_SPACEBAR;
				}
				int volume = manager
						.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
				if (AnySoftKeyboard.DEBUG)
					Log.d("AnySoftKeyboard", "Sound on key-pressed. Sound ID:"
							+ keyFX + " with volume " + volume);

				manager.playSoundEffect(keyFX, volume);
			} else {
				if (AnySoftKeyboard.DEBUG)
					Log.v("AnySoftKeyboard",
							"Devices is muted. No sounds on key-pressed.");
			}
		}
	}

	public void onRelease(int primaryCode) {
		// vibrate();
	}

	// private void checkTutorial(String privateImeOptions) {
	// if (privateImeOptions == null) return;
	// if (privateImeOptions.equals("com.android.setupwizard:ShowTutorial")) {
	// if (mTutorial == null) startTutorial();
	// } else if
	// (privateImeOptions.equals("com.android.setupwizard:HideTutorial")) {
	// if (mTutorial != null) {
	// if (mTutorial.close()) {
	// mTutorial = null;
	// }
	// }
	// }
	// }
	//    
	
//	private boolean mTutorialsShown = false;
	
	private void startTutorial() {
//		if (!mTutorialsShown)
//		{
//			mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_TUTORIAL), 500);
//			mTutorialsShown = true;
//		}
	}

	// void tutorialDone() {
	// mTutorial = null;
	// }
	//    
	// private void launchSettings() {
	// handleClose();
	// Intent intent = new Intent();
	// intent.setClass(LatinIME.this, LatinIMESettings.class);
	// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// startActivity(intent);
	// }

	private boolean loadSettings() {
		//setting all values to default
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		boolean handled = false;
		// Get the settings preferences
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		int newVibrationDuration = Integer.parseInt(sp.getString("vibrate_on_key_press_duration", "0"));
		handled = handled || (newVibrationDuration != mVibrationDuration);
		mVibrationDuration = newVibrationDuration;

		boolean newSoundOn = sp.getBoolean("sound_on", false);
		boolean soundChanged = (newSoundOn != mSoundOn);
		if (soundChanged) 
		{
			if (newSoundOn) {
				Log.i("AnySoftKeyboard", "Loading sounds effects from AUDIO_SERVICE due to configuration change.");
				mAudioManager.loadSoundEffects();
			} else {
				Log.i("AnySoftKeyboard", "Releasing sounds effects from AUDIO_SERVICE due to configuration change.");
				mAudioManager.unloadSoundEffects();
			}
		}
		handled = handled || soundChanged;
		mSoundOn = newSoundOn;
		// in order to support the old type of configuration
		String newKeyboardChangeNotificationType = sp.getString("physical_keyboard_change_notification_type", KEYBOARD_NOTIFICATION_ON_PHYSICAL);
		boolean notificationChanged = (!newKeyboardChangeNotificationType.equalsIgnoreCase(mKeyboardChangeNotificationType));
		handled = handled || notificationChanged;
		mKeyboardChangeNotificationType = newKeyboardChangeNotificationType;

		if (notificationChanged) {
			// now clearing the notification, and it will be re-shown if needed
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// now clearing the notification, and it will be re-shown if needed
			notificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
			// should it be always on?
			if (mKeyboardChangeNotificationType.equals(KEYBOARD_NOTIFICATION_ALWAYS))
				notifyKeyboardChangeIfNeeded();
		}

		boolean newAutoCap = sp.getBoolean("auto_caps", true);
		handled = handled || (newAutoCap != mAutoCap);
		mAutoCap = newAutoCap;

		boolean newShowSuggestions = sp.getBoolean("candidates_on", true);
		boolean suggestionsChanged = (newShowSuggestions != mShowSuggestions);
		handled = handled || suggestionsChanged;
		mShowSuggestions = newShowSuggestions;
		setMainDictionaryForCurrentKeyboard();

		boolean newAutoComplete = sp.getBoolean("auto_complete", true) && mShowSuggestions;
		handled = handled || (newAutoComplete != mAutoComplete);
		mAutoComplete = newAutoComplete;

		boolean newQuickFixes = sp.getBoolean("quick_fix", true);
		handled = handled || (newQuickFixes != mQuickFixes);
		mQuickFixes = newQuickFixes;

		mAutoCorrectOn = /*mSuggest != null &&*//*Suggestion always exists, maybe not at the moment, but shortly*/
			(mAutoComplete || mQuickFixes);
		mCorrectionMode = mAutoComplete ? 2
				: (mShowSuggestions/* mQuickFixes */? 1 : 0);

		boolean newLandscapePredications= sp.getBoolean("physical_keyboard_suggestions", true);
		handled = handled || (newLandscapePredications != mPredictionLandscape);
		mPredictionLandscape = newLandscapePredications;
		
		// this change requires the recreation of the keyboards.
		// so we wont mark the 'handled' result.
		mChangeKeysMode = sp.getString("keyboard_layout_change_method", "1");

		
		return handled;
	}

	private void setMainDictionaryForCurrentKeyboard() {
		if (mSuggest != null) {
			if (!mShowSuggestions) {
				Log.d("AnySoftKeyboard", "No suggestion is required. I'll try to release memory from the dictionary.");
				DictionaryFactory.releaseAllDictionaries();
				mSuggest.setMainDictionary(null);
			} else {
				// It null at the creation of the application.
				if ((mKeyboardSwitcher != null) && mKeyboardSwitcher.isAlphabetMode()) {
					AnyKeyboard currentKeyobard = mKeyboardSwitcher.getCurrentKeyboard();
					Language dictionaryLanguage = getLanguageForKeyobard(currentKeyobard);
					Dictionary mainDictionary = DictionaryFactory.getDictionary(dictionaryLanguage, this);
					mSuggest.setMainDictionary(mainDictionary);
				}
			}
		}
	}

	private Language getLanguageForKeyobard(AnyKeyboard currentKeyobard) 
	{
		//if there is a mapping in the settings, we'll use that, else we'll return the default
		String mappingSettingsKey = getDictionaryOverrideKey(currentKeyobard);
		String defaultDictionary = currentKeyobard.getDefaultDictionaryLanguage().toString();
		String dictionaryValue = getSharedPreferences().getString(mappingSettingsKey, null);
		if ((dictionaryValue == null) || (defaultDictionary.equals(dictionaryValue)))
			return currentKeyobard.getDefaultDictionaryLanguage();
		else
		{
			Log.d("AnySoftKeyboard", "Default dictionary '"+defaultDictionary+"' for keyboard '"+currentKeyobard.getKeyboardPrefId()+"' has been overriden to '"+dictionaryValue+"'");
			//fixing the Slovene->Slovenian
			if (dictionaryValue.equalsIgnoreCase("Slovene"))
			{//Please remove this in some future version.
				dictionaryValue = "Slovenian";
				Editor editor = getSharedPreferences().edit();
				Log.d("AnySoftKeyboard", "Dictionary override fix: Slovene->Slovenian.");
				editor.putString(mappingSettingsKey, dictionaryValue);
				editor.commit();
			}
			Language overridingLanguage = Language.valueOf(dictionaryValue);
			return overridingLanguage;
		}
	}

	private String getDictionaryOverrideKey(AnyKeyboard currentKeyobard) {
		String mappingSettingsKey = currentKeyobard.getKeyboardPrefId()+"_override_dictionary";
		return mappingSettingsKey;
	}

	private void launchSettings() {
		handleClose();
		Intent intent = new Intent();
		intent.setClass(AnySoftKeyboard.this, SoftKeyboardSettings.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void launchDictioanryOverriding()
	{
		AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
		final String dictionaryOverridingKey = getDictionaryOverrideKey(currentKeyboard);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.icon_8_key);
		builder.setTitle(getResources().getString(R.string.override_dictionary_title, currentKeyboard.getKeyboardName()));
		builder.setNegativeButton(android.R.string.cancel, null);
		ArrayList<CharSequence> dictioanries = new ArrayList<CharSequence>();
		dictioanries.add(getString(R.string.override_dictionary_default));
		for(Language lang : Language.values())
		{
			dictioanries.add(lang.toString());
		}
		 
		final CharSequence[] items = new CharSequence[dictioanries.size()];
		dictioanries.toArray(items);
		
		builder.setItems(items, 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int position) {
					di.dismiss();
					Editor editor = getSharedPreferences().edit();
					switch(position)
					{
					case 0:
						Log.d("AnySoftKeyboard", "Dictionary overriden disabled. User selected default.");
						editor.remove(dictionaryOverridingKey);
						showToastMessage(R.string.override_disabled, true);
						break;
					default:
						if ((position < 0) || (position >= items.length))
						{
							Log.d("AnySoftKeyboard", "Dictioanry override dialog canceled.");
						}
						else
						{
							String selectedLanguageString = items[position].toString();
							Log.d("AnySoftKeyboard", "Dictionary override. User selected "+selectedLanguageString);
							editor.putString(dictionaryOverridingKey, selectedLanguageString);
							showToastMessage(getString(R.string.override_enabled, selectedLanguageString), true);
						}
						break;
					}
					editor.commit();
					setMainDictionaryForCurrentKeyboard();
				}
			});
		 
		mOptionsDialog = builder.create();
		Window window = mOptionsDialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.token = mInputView.getWindowToken();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		window.setAttributes(lp);
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		mOptionsDialog.show();
	}
	
	private void showOptionsMenu() {
		 AlertDialog.Builder builder = new AlertDialog.Builder(this);
		 builder.setCancelable(true);
		 builder.setIcon(R.drawable.icon_8_key);
		 builder.setNegativeButton(android.R.string.cancel, null);
		 CharSequence itemSettings = getString(R.string.ime_settings);
		 CharSequence itemOverrideDictionary = getString(R.string.override_dictionary);
		 CharSequence itemInputMethod = getString(R.string.change_ime);
		 builder.setItems(new CharSequence[] { itemSettings, itemOverrideDictionary, itemInputMethod}, 
			 new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface di, int position) {
					 di.dismiss();
					 switch (position) {
					 case 0:
						 launchSettings();
						 break;
					 case 1:
						 launchDictioanryOverriding();
						 break;
					 case 2:
						 ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
						 break;
					 }
				 }
	 		});
		 builder.setTitle(getResources().getString(R.string.ime_name));
		 mOptionsDialog = builder.create();
		 Window window = mOptionsDialog.getWindow();
		 WindowManager.LayoutParams lp = window.getAttributes();
		 lp.token = mInputView.getWindowToken();
		 lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		 window.setAttributes(lp);
		 window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		 mOptionsDialog.show();
	 }
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.i("AnySoftKeyboard", "**** onConfigurationChanged");
		Log.i("AnySoftKeyboard", "** Locale:"+ newConfig.locale.toString());
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("AnySoftKeyboard", "onSharedPreferenceChanged - key:" + key);

		boolean handled = loadSettings();
		if (!handled) {
			/* AnyKeyboard removedKeyboard = */mKeyboardSwitcher.makeKeyboards(true);// maybe a new keyboard
			/*
			 * if (removedKeyboard != null) {
			 * DictionaryFactory.releaseDictionary
			 * (removedKeyboard.getDefaultDictionaryLanguage()); }
			 */
		}
	}

	public void appendCharactersToInput(CharSequence textToCommit) {
		if (DEBUG) Log.d("AnySoftKeyboard", "appendCharactersToInput: "+textToCommit);
		mWord.append(textToCommit);
		mComposing.append(textToCommit);
		appendStringToInput(textToCommit);
	}

	private void appendStringToInput(CharSequence textToCommit) {
		// handleTextDirection();
		if (DEBUG) Log.d("AnySoftKeyboard", "appendStringToInput: "+textToCommit);
		if (mCompletionOn) {
			getCurrentInputConnection().setComposingText(mWord.getTypedWord(),
					textToCommit.length());
			// updateCandidates();
		} else
			commitTyped(getCurrentInputConnection());

		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	public void deleteLastCharactersFromInput(int countToDelete) {
		if (DEBUG) Log.d("AnySoftKeyboard", "deleteLastCharactersFromInput: "+countToDelete);
			
		final int currentLength = mComposing.length();
		boolean shouldDeleteUsingCompletion;
		if (currentLength > 0) {
			shouldDeleteUsingCompletion = true;
			if (currentLength > countToDelete) {
				mComposing.delete(currentLength - countToDelete, currentLength);

				mWord.deleteLast(countToDelete);
			} else {
				mComposing.setLength(0);
				mWord.reset();
			}
		} else {
			shouldDeleteUsingCompletion = false;
		}

		if (mCompletionOn && shouldDeleteUsingCompletion) {
			getCurrentInputConnection().setComposingText(mComposing, 1);
			// updateCandidates();
		} else {
			getCurrentInputConnection().deleteSurroundingText(countToDelete, 0);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	public SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	public void showToastMessage(int resId, boolean forShortTime)
	{
		CharSequence text = getResources().getText(resId);
		showToastMessage(text, forShortTime);
	}

	private void showToastMessage(CharSequence text, boolean forShortTime) {
		int duration = forShortTime? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
		if (DEBUG)
			Log.v("AnySoftKeyboard", "showToastMessage: '"+text+"'. For: "+duration);
		Toast.makeText(this.getApplication(), text, duration).show();
	}
	
	public void performLengthyOperation(int textResId, final Runnable thingToDo) {
		thingToDo.run();
//		final ProgressDialog spinner = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
//		
//		Thread t = new Thread() {
//            public void run() {
//            	thingToDo.run();
//            	spinner.dismiss();
//            }
//        };
//        t.start();
//        spinner.setTitle(R.string.please_wait);
//        spinner.setIcon(R.drawable.icon_8_key);
//        spinner.setMessage(getResources().getText(textResId));
//        spinner.setCancelable(false);
//        spinner.show();
	}
	
	@Override
	public void onLowMemory() {
		Log.w("AnySoftKeyboard", "The OS has reported that it is low on memory!. I'll try to clear some cache.");
		mKeyboardSwitcher.onLowMemory();
		DictionaryFactory.onLowMemory(getLanguageForKeyobard(mKeyboardSwitcher.getCurrentKeyboard()));
		super.onLowMemory();
	}	
}
