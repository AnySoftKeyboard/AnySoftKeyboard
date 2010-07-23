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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.AutoText;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.KeyboardSwitcher.NextKeyboardType;
import com.menny.android.anysoftkeyboard.dictionary.Dictionary;
import com.menny.android.anysoftkeyboard.dictionary.DictionaryFactory;
import com.menny.android.anysoftkeyboard.dictionary.ExternalDictionaryFactory;
import com.menny.android.anysoftkeyboard.dictionary.UserDictionaryBase;
import com.menny.android.anysoftkeyboard.dictionary.ExternalDictionaryFactory.DictionaryBuilder;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.menny.android.anysoftkeyboard.keyboards.KeyboardBuildersFactory.KeyboardBuilder;
import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider;

/**
 * Input method implementation for Qwerty'ish keyboard.
 */
public class AnySoftKeyboard extends InputMethodService implements
		AnyKeyboardView.OnAnyKeyboardActionListener,
		OnSharedPreferenceChangeListener, AnyKeyboardContextProvider {
	private final static String TAG = "ASK";


	private final boolean TRACE_SDCARD = false;

	private static final int MSG_UPDATE_SUGGESTIONS = 0;
	//private static final int MSG_START_TUTORIAL = 1;

	public static final int KEYCODE_ENTER = 10;
	public static final int KEYCODE_SPACE = ' ';
	private static final int KEYBOARD_NOTIFICATION_ID = 1;
	
	private static final HashSet<Integer> SPACE_SWAP_CHARACTERS = new HashSet<Integer>(
			6);
	private static final HashSet<Integer> PUNCTUATION_CHARACTERS = new HashSet<Integer>(
			16);
	static {
		String src = ".\n!?,:;@<>()[]{}";
		for (int i = 0; i < src.length(); ++i)
			PUNCTUATION_CHARACTERS.add((int) src.charAt(i));
		
		src = ".!?,:;@";
		for (int i = 0; i < src.length(); ++i)
			SPACE_SWAP_CHARACTERS.add((int) src.charAt(i));
	}

	private final AnySoftKeyboardConfiguration mConfig;
	private boolean DEBUG;

	private AnyKeyboardView mInputView;
	private CandidateViewContainer mCandidateViewContainer;
	private CandidateView mCandidateView;
	private Suggest mSuggest;
	private CompletionInfo[] mCompletions;

	private AlertDialog mOptionsDialog;
	private AlertDialog mSmileyDialog;
	
	KeyboardSwitcher mKeyboardSwitcher;
	private final HardKeyboardActionImpl mHardKeyboardAction;
	private long mMetaState;

	private UserDictionaryBase mUserDictionary;

	private StringBuilder mComposing = new StringBuilder();
	private WordComposer mWord = new WordComposer();

	private int mOrientation = Configuration.ORIENTATION_PORTRAIT;

	private int mCommittedLength;
	private boolean mPredicting;
	private CharSequence mBestWord;
	private final boolean mPredictionLandscape = false;
	private boolean mPredictionOn;
	private boolean mCompletionOn;
	private boolean mAutoSpace;
	private boolean mAutoCorrectOn;
	private boolean mCapsLock;
	// private boolean mVibrateOn;
	private int mVibrationDuration;
	private boolean mSoundOn;
	// between 0..100. This is the custom volume
	private int mSoundVolume;

	private boolean mSmileyOnShortPress;
	private String mSmileyPopupType;
	private boolean mAutoCap;
	private boolean mQuickFixes;
	private boolean mShowSuggestions = false;
	private boolean mAutoComplete;
	private int mCorrectionMode;
	private String mKeyboardChangeNotificationType;
	private static final String KEYBOARD_NOTIFICATION_ALWAYS = "1";
	private static final String KEYBOARD_NOTIFICATION_ON_PHYSICAL = "2";
	private static final String KEYBOARD_NOTIFICATION_NEVER = "3";

	// Indicates whether the suggestion strip is to be on in landscape
	private boolean mJustAccepted;
	private CharSequence mJustRevertedSeparator;

	private AudioManager mAudioManager;
	private NotificationManager mNotificationManager;

	private static AnySoftKeyboard INSTANCE;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_SUGGESTIONS:
				updateSuggestions();
				break;
			}
		}
	};

	private boolean mSpaceSent;

	private static final int LAST_CHAR_SHIFT_STATE_UNKNOWN = 0;
	private static final int LAST_CHAR_SHIFT_STATE_UNSHIFTED = 1;
	private static final int LAST_CHAR_SHIFT_STATE_SHIFTED = 2;
	private int mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;

	public static AnySoftKeyboard getInstance() {
		return INSTANCE;
	}

	public AnySoftKeyboard() {
		// mGenericKeyboardTranslator = new
		// GenericPhysicalKeyboardTranslator(this);
		mConfig = AnySoftKeyboardConfiguration.getInstance();
		mHardKeyboardAction = new HardKeyboardActionImpl();
		INSTANCE = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// super.showStatusIcon(R.drawable.icon_8_key);
		Log.i("AnySoftKeyboard", "****** Starting AnySoftKeyboard:");
		((AnySoftKeyboardConfiguration.AnySoftKeyboardConfigurationImpl) mConfig)
				.initializeConfiguration(this);

		DEBUG = mConfig.getDEBUG();
		// showToastMessage(R.string.toast_lengthy_start_up_operation, true);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// setStatusIcon(R.drawable.ime_qwerty);
		loadSettings();
		mKeyboardSwitcher = new KeyboardSwitcher(this);
		
		if (mSuggest == null) {
			// should it be always on?
			if (mKeyboardChangeNotificationType
					.equals(KEYBOARD_NOTIFICATION_ALWAYS))
				notifyKeyboardChangeIfNeeded();
			initSuggest(/* getResources().getConfiguration().locale.toString() */);
		}

		mOrientation = getResources().getConfiguration().orientation;

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);

		TutorialsProvider.ShowTutorialsIfNeeded(AnySoftKeyboard.this);
	}
	
	@Override
	public void onUnbindInput() {
		Log.i(TAG, "onUnbindInput");
		super.onUnbindInput();
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
		Log.i(TAG, "AnySoftKeyboard has been destroyed! Cleaning resources..");
		DictionaryFactory.close();

		// unregisterReceiver(mReceiver);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.unregisterOnSharedPreferenceChangeListener(this);
		if (mSoundOn) {
			Log.i(TAG,
					"Releasing sounds effects from AUDIO_SERVICE");
			mAudioManager.unloadSoundEffects();
		}

		mNotificationManager.cancel(KEYBOARD_NOTIFICATION_ID);

		TutorialsProvider.onServiceDestroy();

		super.onDestroy();
	}

	@Override
	public void onFinishInputView(boolean finishingInput) {
		if (DEBUG)
			Log.d(TAG, "onFinishInputView(finishingInput:"
					+ finishingInput + ")");
		
		super.onFinishInputView(finishingInput);
		
		if (!mKeyboardChangeNotificationType
				.equals(KEYBOARD_NOTIFICATION_ALWAYS)) {
			mNotificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
		}

		resetComposing();// clearing any predications
	}

	@Override
	public View onCreateInputView() {
		if (DEBUG) Log.v(TAG, "Creating Input View");
		mInputView = (AnyKeyboardView) getLayoutInflater().inflate(
				//the new layout will solve the "invalidateAllKeys" problem.
				Workarounds.isDonut()? R.layout.input_donut : R.layout.input_cupcake
				, null);
		//reseting token users
		mOptionsDialog = null;
		mSmileyDialog = null;
		
		
		mKeyboardSwitcher.resetKeyboardsCache();
		mKeyboardSwitcher.setInputView(mInputView);
		//mKeyboardSwitcher.makeKeyboards(false);
		mInputView.setOnKeyboardActionListener(this);
		//mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, null);

		return mInputView;
	}

	@Override
	public View onCreateCandidatesView() {
		mKeyboardSwitcher.makeKeyboards(false);
		mCandidateViewContainer = (CandidateViewContainer) getLayoutInflater()
				.inflate(R.layout.candidates, null);
		mCandidateViewContainer.initViews();
		mCandidateView = (CandidateView) mCandidateViewContainer
				.findViewById(R.id.candidates);
		mCandidateView.setService(this);
		setCandidatesViewShown(true);
		return mCandidateViewContainer;
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		if (DEBUG)
			Log.d(TAG, "onStartInputView(EditorInfo:"
					+ attribute.imeOptions + "," + attribute.inputType
					+ ", restarting:" + restarting + ")");
		super.onStartInputView(attribute, restarting);

		if (mInputView != null) {
			mInputView.closing();
			if (AutoText.getSize(mInputView) < 1)
				mQuickFixes = true;
		}
		else
		{
			return;
		}

		mKeyboardSwitcher.makeKeyboards(false);
		resetComposing();// clearing any predications
		TextEntryState.newSession(this);

		if (!restarting) {
			// Clear shift states.
			mMetaState = 0;
		}

		mPredictionOn = false;
		mCompletionOn = false;
		mCompletions = null;
		mCapsLock = false;
		if (!restarting)
		{
			switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS)
			{
			case EditorInfo.TYPE_CLASS_NUMBER:
			case EditorInfo.TYPE_CLASS_DATETIME:
			case EditorInfo.TYPE_CLASS_PHONE:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_PHONE,
						attribute);
				break;
			case EditorInfo.TYPE_CLASS_TEXT:
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT,
						attribute);
				// startPrediction();
				mPredictionOn = true;
				// Make sure that passwords are not displayed in candidate view
				final int variation = attribute.inputType
						& EditorInfo.TYPE_MASK_VARIATION;
				if (variation == EditorInfo.TYPE_TEXT_VARIATION_PASSWORD
						|| variation == EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
					mPredictionOn = false;
				}

				if ((!AnySoftKeyboardConfiguration.getInstance().getInsertSpaceAfterCandidatePick()) ||//some users want to never get spaces added
						variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
						variation == EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME)
				{
					mAutoSpace = false;
				} else {
					mAutoSpace = true;
				}
				if (variation == EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
					mPredictionOn = false;
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_EMAIL,
							attribute);
				} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_URI) {
					mPredictionOn = false;
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_URL,
							attribute);
				} else if (variation == EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE) {
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_IM,
							attribute);
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
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT,
						attribute);
				updateShiftKeyState(attribute);
			}
		}
		mComposing.setLength(0);
		mPredicting = false;
		// mDeleteCount = 0;
		setCandidatesViewShown(false);
		// loadSettings();

		if (mSuggest != null) {
			mSuggest.setCorrectionMode(mCorrectionMode);
		}

		mPredictionOn = mPredictionOn && mCorrectionMode > 0;

		if (mCandidateView != null)
			mCandidateView.setSuggestions(null, false, false, false);

		if (TRACE_SDCARD)
			Debug.startMethodTracing("anysoftkeyboard_log.trace");
	}

	@Override
	public void onFinishInput() {
		if (DEBUG)
			Log.d(TAG, "onFinishInput()");
		super.onFinishInput();

		if (mInputView != null) {
			mInputView.closing();
		}

		if (!mKeyboardChangeNotificationType
				.equals(KEYBOARD_NOTIFICATION_ALWAYS)) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
		}
		// clearing any predications
		resetComposing();
		// releasing some memory. Dictionaries, completions, etc.
		System.gc();
	}
	
	@Override
	public void onUpdateSelection(int oldSelStart, int oldSelEnd,
			int newSelStart, int newSelEnd, int candidatesStart,
			int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
				candidatesStart, candidatesEnd);
		
		if (DEBUG)
		{
			Log.d(TAG, "mComposing.length():"+mComposing.length());
			Log.d(TAG, "oldSelStart:"+oldSelStart+" oldSelEnd:"+oldSelEnd);
			Log.d(TAG, "newSelStart:"+newSelStart+" newSelEnd:"+newSelEnd);
			Log.d(TAG, "candidatesStart:"+candidatesStart+" candidatesEnd:"+candidatesEnd);
		}
		// If the current selection in the text view changes, we should
		// clear whatever candidate text we have.
		if (mComposing.length() > 0 && mPredicting)//OK we are in predicting state
		{
			if ((candidatesEnd >= 0)//we have candidates underline
					&& (newSelEnd != candidatesEnd)) //the candidate underline does not end at the new cursor position! User changed the cursor.
			{
				resetComposing();
			}
			else if ((candidatesEnd < 0) || (candidatesStart <0))//the input cleared the underline
			{
				resetComposing();
			}
		}
		else if (!mPredicting
				&& !mJustAccepted
				&& TextEntryState.getState() == TextEntryState.STATE_ACCEPTED_DEFAULT) {
			TextEntryState.reset();
		}
		mJustAccepted = false;
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		Log.d(TAG, "onTrackballEvent");
		return super.onTrackballEvent(event);
	}
	@Override
	public Context getApplicationContext() {
		// TODO Auto-generated method stub
		return super.getApplicationContext();
	}
	
	private void resetComposing() {
		InputConnection ic = getCurrentInputConnection();
		if (ic != null) {
			ic.finishComposingText();
			// commitTyped(ic);
		}
		mComposing.setLength(0);
		mPredicting = false;
		updateSuggestions();
		TextEntryState.reset();
	}

	@Override
	public void hideWindow() {
		if (TRACE_SDCARD)
			Debug.stopMethodTracing();

		if (mOptionsDialog != null && mOptionsDialog.isShowing()) {
			mOptionsDialog.dismiss();
			mOptionsDialog = null;
		}
		if (mSmileyDialog != null && mSmileyDialog.isShowing()) {
			mSmileyDialog.dismiss();
			mSmileyDialog = null;
		}
		// if (mTutorial != null) {
		// mTutorial.close();
		// mTutorial = null;
		// }
		super.hideWindow();
		TextEntryState.endSession();
	}

	@Override
	public void onDisplayCompletions(CompletionInfo[] completions) {
		if (DEBUG) {
			Log.i(TAG, "Received completions:");
			for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
				Log.i("AnySoftKeyboard", "  #" + i + ": " + completions[i]);
			}
		}

		//completions should be shown if dictionary requires, or if we are in full-screen and have outside completeions
		if (mCompletionOn || (isFullscreenMode() && (completions != null))) {
			if (DEBUG) Log.v(TAG, "Received completions: completion should be shown: "+mCompletionOn+" fullscreen:"+isFullscreenMode());
			mCompletions = completions;
			if (completions == null) {
				if (DEBUG) Log.v(TAG, "Received completions: completion is NULL. Clearing suggestions.");
				mCandidateView.setSuggestions(null, false, false, false);
				return;
			}

			List<CharSequence> stringList = new ArrayList<CharSequence>();
			for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
				CompletionInfo ci = completions[i];
				if (ci != null)
					stringList.add(ci.getText());
			}
			if (DEBUG) Log.v(TAG, "Received completions: setting to suggestions view "+stringList.size()+ " completions.");
			// CharSequence typedWord = mWord.getTypedWord();
			mCandidateView.setSuggestions(stringList, true, true, true);
			mBestWord = null;
			//I mean, if I'm here, it must be shown...
			setCandidatesViewShown(true);
		}
		else if (DEBUG) Log.v(TAG, "Received completions: completions should not be shown.");
	}

	@Override
	public void setCandidatesViewShown(boolean shown) {
		// we show predication only in on-screen keyboard
		// (onEvaluateInputViewShown)
		// or if the physical keyboard supports candidates
		// (mPredictionLandscape)
		super.setCandidatesViewShown(shouldCandidatesStripBeShown() && shown);
	}

	@Override
	public void onComputeInsets(InputMethodService.Insets outInsets) {
		super.onComputeInsets(outInsets);
		if (!isFullscreenMode()) {
			outInsets.contentTopInsets = outInsets.visibleTopInsets;
		}
	}

	@Override
	public boolean onEvaluateFullscreenMode() {
		switch(mOrientation)
		{
		case Configuration.ORIENTATION_LANDSCAPE:
			return AnySoftKeyboardConfiguration.getInstance().getUseFullScreenInputInLandscape();
		default:
			return AnySoftKeyboardConfiguration.getInstance().getUseFullScreenInputInPortrait();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (DEBUG) Log.d(TAG, "onKeyDown:"+keyCode+" flags:"+event.getFlags());

		InputConnection ic = getCurrentInputConnection();
		if (!mPredictionLandscape) {
			// For all other keys, if we want to do transformations on
			// text being entered with a hard keyboard, we need to process
			// it and do the appropriate action.
			// using physical keyboard is more annoying with candidate view in
			// the way
			// so we disable it.

			// to clear the underline.
			commitTyped(ic);// to clear the underline.

			mPredicting = false;
		}
		if (DEBUG)
			Log.d(TAG, "Event: Key:" + event.getKeyCode()
					+ " Shift:"
					+ ((event.getMetaState() & KeyEvent.META_SHIFT_ON) != 0)
					+ " ALT:"
					+ ((event.getMetaState() & KeyEvent.META_ALT_ON) != 0)
					+ " Repeats:" + event.getRepeatCount());

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (event.getRepeatCount() == 0 && mInputView != null) {
				if (mInputView.handleBack()) {
					// consuming the meta keys
					if (ic != null) {
						ic.clearMetaKeyStates(Integer.MAX_VALUE);// translated,
						// so we
						// also take
						// care of
						// the
						// metakeys.
					}
					mMetaState = 0;
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
		// case KeyEvent.KEYCODE_DEL:
		// onKey(Keyboard.KEYCODE_DELETE, new int[]{Keyboard.KEYCODE_DELETE});
		// return true;
		// case KeyEvent.KEYCODE_ENTER:
		// // Let the underlying text editor always handle these.
		// return false;
		case KeyEvent.KEYCODE_SHIFT_LEFT:
        case KeyEvent.KEYCODE_SHIFT_RIGHT:
            if (event.isAltPressed() && Workarounds.isAltSpaceLangSwitchNotPossible()) {
                Log.d(TAG,
                                "User pressed ALT+SHIFT on motorola milestone, moving to next physical keyboard.");
                // consuming the meta keys
                // mHardKeyboardAction.resetMetaState();
                if (ic != null) {
                    ic.clearMetaKeyStates(Integer.MAX_VALUE);// translated, so
                    // we also take
                    // care of the
                    // metakeys.
                }
                mMetaState = 0;
                // only physical keyboard
                nextKeyboard(getCurrentInputEditorInfo(),
                        NextKeyboardType.AlphabetSupportsPhysical);
                return true;
            }
		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
		case KeyEvent.KEYCODE_SYM:
			if (DEBUG)
				Log.d(TAG+"-meta-key",
						getMetaKeysStates("onKeyDown before handle"));
			mMetaState = MyMetaKeyKeyListener.handleKeyDown(mMetaState,
					keyCode, event);
			if (DEBUG)
				Log.d(TAG+"-meta-key",
						getMetaKeysStates("onKeyDown after handle"));
			break;
		case KeyEvent.KEYCODE_SPACE:
			if (event.isAltPressed() && !Workarounds.isAltSpaceLangSwitchNotPossible()) {
				Log.d(TAG,
								"User pressed ALT+SPACE, moving to next physical keyboard.");
				// consuming the meta keys
				// mHardKeyboardAction.resetMetaState();
				if (ic != null) {
					ic.clearMetaKeyStates(Integer.MAX_VALUE);// translated, so
					// we also take
					// care of the
					// metakeys.
				}
				mMetaState = 0;
				// only physical keyboard
				nextKeyboard(getCurrentInputEditorInfo(),
						NextKeyboardType.AlphabetSupportsPhysical);

				return true;
			}
			//NOTE:
			// letting it fall through to the "default"
			// else
			// {
			// //still handling it
			// onKey(32, new int[]{32});
			// return true;
			// }
		default:

			// Fix issue 185, check if we should process key repeat
			if (!AnySoftKeyboardConfiguration.getInstance()
					.getUseRepeatingKeys()
					&& event.getRepeatCount() > 0)
				return true;

			if (mKeyboardSwitcher.isCurrentKeyboardPhysical()) {
				// sometimes, the physical keyboard will delete input, and then
				// add some.
				// we'll try to make it nice
				if (ic != null)
					ic.beginBatchEdit();
				try {
				    //issue 393, backword on the hw keyboard!
				    if(AnySoftKeyboardConfiguration.getInstance().useBackword() && keyCode == KeyEvent.KEYCODE_DEL && event.isShiftPressed()){
                        handleBackword(ic);
                        return true;
				    } else if (event.isPrintingKey()) {
						mHardKeyboardAction.initializeAction(event, mMetaState);
						// http://article.gmane.org/gmane.comp.handhelds.openmoko.android-freerunner/629
						AnyKeyboard current = mKeyboardSwitcher.getCurrentKeyboard();

						HardKeyboardTranslator keyTranslator = (HardKeyboardTranslator) current;

						if (DEBUG)
						{
							final String keyboardName = current.getKeyboardName();

							Log.d(TAG, "Asking '" + keyboardName
									+ "' to translate key: " + keyCode);
							Log.v(TAG,
									"Hard Keyboard Action before translation: Shift: "
											+ mHardKeyboardAction
													.isShiftActive()
											+ ", Alt: "
											+ mHardKeyboardAction.isAltActive()
											+ ", Key code: "
											+ mHardKeyboardAction.getKeyCode()
											+ ", changed: "
											+ mHardKeyboardAction.getKeyCodeWasChanged());
						}

						keyTranslator.translatePhysicalCharacter(mHardKeyboardAction);

						if (DEBUG)
							Log.v(TAG,
									"Hard Keyboard Action after translation: Key code: "
											+ mHardKeyboardAction.getKeyCode()
											+ ", changed: "
											+ mHardKeyboardAction
													.getKeyCodeWasChanged());
						if (mHardKeyboardAction.getKeyCodeWasChanged()) {
							final int translatedChar = mHardKeyboardAction
									.getKeyCode();
							// typing my own.
							onKey(translatedChar, new int[] { translatedChar });
							// my handling
							// we are at a regular key press, so we'll update
							// our meta-state member
							mMetaState = MyMetaKeyKeyListener
									.adjustMetaAfterKeypress(mMetaState);
							if (DEBUG)
								Log.d(TAG+"-meta-key",
												getMetaKeysStates("onKeyDown after adjust - translated"));
							return true;
						}
					}
				} finally {
					if (ic != null)
						ic.endBatchEdit();
				}
			}
			if (event.isPrintingKey()) {
				// we are at a regular key press, so we'll update our meta-state
				// member
				mMetaState = MyMetaKeyKeyListener
						.adjustMetaAfterKeypress(mMetaState);
				if (DEBUG)
					Log.d(TAG+"-meta-key",
							getMetaKeysStates("onKeyDown after adjust"));
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void notifyKeyboardChangeIfNeeded() {
		// Log.d("anySoftKeyboard","notifyKeyboardChangeIfNeeded");
		// Thread.dumpStack();
		if (mKeyboardSwitcher == null)// happens on first onCreate.
			return;

		if ((mKeyboardSwitcher.isAlphabetMode())
				&& !mKeyboardChangeNotificationType
						.equals(KEYBOARD_NOTIFICATION_NEVER)) {
			AnyKeyboard current = mKeyboardSwitcher.getCurrentKeyboard();
			// notifying the user about the keyboard.
			// creating the message
			final String keyboardName = current.getKeyboardName();

			Notification notification = new Notification();

			Intent notificationIntent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);

			notification.setLatestEventInfo(getApplicationContext(),
					getText(R.string.ime_name), keyboardName,
					contentIntent);
			//this will not work. Need to find a way to show notification as a different package.
			//notification.icon = current.getKeyboardIconResId();
			notification.icon = R.drawable.notification_icon;

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
		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
		case KeyEvent.KEYCODE_SYM:
			mMetaState = MyMetaKeyKeyListener.handleKeyUp(mMetaState, keyCode,
					event);
			if (DEBUG)
				Log.d("AnySoftKeyboard-meta-key", getMetaKeysStates("onKeyUp"));
			setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState();
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	private String getMetaKeysStates(String place) {
		final int shiftState = MyMetaKeyKeyListener.getMetaState(mMetaState,
				MyMetaKeyKeyListener.META_SHIFT_ON);
		final int altState = MyMetaKeyKeyListener.getMetaState(mMetaState,
				MyMetaKeyKeyListener.META_ALT_ON);
		final int symState = MyMetaKeyKeyListener.getMetaState(mMetaState,
				MyMetaKeyKeyListener.META_SYM_ON);

		return "Meta keys state at " + place + "- SHIFT:" + shiftState
				+ ", ALT:" + altState + " SYM:" + symState + " bits:"
				+ MyMetaKeyKeyListener.getMetaState(mMetaState) + " state:"
				+ mMetaState;
	}

	private void setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState() {
		InputConnection ic = getCurrentInputConnection();
		if (ic != null) {
			int clearStatesFlags = 0;
			if (MyMetaKeyKeyListener.getMetaState(mMetaState,
					MyMetaKeyKeyListener.META_ALT_ON) == 0)
				clearStatesFlags += KeyEvent.META_ALT_ON;
			if (MyMetaKeyKeyListener.getMetaState(mMetaState,
					MyMetaKeyKeyListener.META_SHIFT_ON) == 0)
				clearStatesFlags += KeyEvent.META_SHIFT_ON;
			if (MyMetaKeyKeyListener.getMetaState(mMetaState,
					MyMetaKeyKeyListener.META_SYM_ON) == 0)
				clearStatesFlags += KeyEvent.META_SYM_ON;
			if (DEBUG)
				Log
						.d(
								"AnySoftKeyboard-meta-key",
								getMetaKeysStates("setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState with flags: "
										+ clearStatesFlags));
			ic.clearMetaKeyStates(clearStatesFlags);
		}
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
			mInputView.requestShiftKeyRedraw();
		}
	}

	private void swapPunctuationAndSpace() {
		final InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		CharSequence lastTwo = ic.getTextBeforeCursor(2, 0);
		if (lastTwo != null && lastTwo.length() == 2
				&& lastTwo.charAt(0) == KEYCODE_SPACE
				&& SPACE_SWAP_CHARACTERS.contains((int)lastTwo.charAt(1))) {
			//ic.beginBatchEdit();
			ic.deleteSurroundingText(2, 0);
			ic.commitText(lastTwo.charAt(1) + " ", 1);
			//ic.endBatchEdit();
			updateShiftKeyState(getCurrentInputEditorInfo());
		}
	}

	private void doubleSpace() {
		// if (!mAutoPunctuate) return;
		if (!AnySoftKeyboardConfiguration.getInstance().isDoubleSpaceChangesToPeriod())
			return;
		final InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		CharSequence lastThree = ic.getTextBeforeCursor(3, 0);
		if (lastThree != null && lastThree.length() == 3
				&& Character.isLetterOrDigit(lastThree.charAt(0))
				&& lastThree.charAt(1) == KEYCODE_SPACE
				&& lastThree.charAt(2) == KEYCODE_SPACE) {
			//ic.beginBatchEdit();
			ic.deleteSurroundingText(2, 0);
			ic.commitText(". ", 1);
			//ic.endBatchEdit();
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
		return mKeyboardSwitcher.getCurrentKeyboard().isLetter((char) code);
	}

	// Implementation of KeyboardViewListener

	public void onKey(int primaryCode, int[] keyCodes) {
		
		if (DEBUG)	Log.d("AnySoftKeyboard", "onKey " + primaryCode);
		
		switch (primaryCode) {
		case Keyboard.KEYCODE_DELETE:
			handleBackspace();
			break;
		case Keyboard.KEYCODE_SHIFT:
			handleShift();
			break;
		case AnyKeyboard.KEYCODE_CTRL:
			//mCtrl = true;
			break;

		case AnyKeyboard.KEYCODE_LEFT:
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
			break;
		case AnyKeyboard.KEYCODE_RIGHT:
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
			break;
		case AnyKeyboard.KEYCODE_UP:
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
			break;
		case AnyKeyboard.KEYCODE_DOWN:
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
			break;

		case Keyboard.KEYCODE_CANCEL:
			if (mOptionsDialog == null || !mOptionsDialog.isShowing()) {
				handleClose();
			}
			break;
		case AnyKeyboardView.KEYCODE_OPTIONS:
			showOptionsMenu();
			break;
		case AnyKeyboard.KEYCODE_DOMAIN:
			onText(mConfig.getDomainText());
			break;
		case AnyKeyboard.KEYCODE_SMILEY:
			if (mSmileyOnShortPress) {
				// Log.d("AnySoftKeyboard", "SMILEY short: type smiley");
				onText(mConfig.getSmileyText());
			} else {
				// Log.d("AnySoftKeyboard", "SMILEY short: popup smileys");
				if (mSmileyPopupType.equalsIgnoreCase("popupKeyboard"))
				{
					if (mInputView != null)
						mInputView.simulateLongPress(AnyKeyboard.KEYCODE_SMILEY);
				}
				else
				{
					showSmileyDialog();
				}
			}
			break;
		case AnyKeyboardView.KEYCODE_SMILEY_LONGPRESS:
			if (mSmileyOnShortPress) {
				if (mSmileyPopupType.equalsIgnoreCase("popupKeyboard"))
				{
					if (mInputView != null)
						mInputView.simulateLongPress(AnyKeyboard.KEYCODE_SMILEY);
				}
				else
				{
					showSmileyDialog();
				}
			} else {
				// Log.d("AnySoftKeyboard", "SMILEY long: type smiley");
				onText(mConfig.getSmileyText());
			}
			break;
		case Keyboard.KEYCODE_MODE_CHANGE:
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Symbols);
			break;
		case AnyKeyboard.KEYCODE_LANG_CHANGE:
			if (mKeyboardSwitcher.shouldPopupForLanguageSwitch())
			{
				showLanguageSelectionDialog();
			}
			else
				nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
			break;
		case Keyboard.KEYCODE_ALT:
			nextAlterKeyboard(getCurrentInputEditorInfo());
			break;
		case AnyKeyboard.KEYCODE_KEYBOARD_CYCLE:
		    nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Any);
            break;
		case AnyKeyboard.KEYCODE_KEYBOARD_REVERSE_CYCLE:
		    nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.PreviousAny);
            break;
		default:
			primaryCode = translatePrimaryCodeFromCurrentKeyboard(primaryCode);
			// Issue 146: Right to left langs require reversed parenthesis
			if (mKeyboardSwitcher.isRightToLeftMode())
				primaryCode = Workarounds
						.workaroundParenthesisDirectionFix(primaryCode);

			if (isWordSeparator(primaryCode)) {
				handleSeparator(primaryCode);
			} else {
				handleCharacter(primaryCode, keyCodes);
				// reseting the mSpaceSent, which is set to true upon selecting
				// candidate
				mSpaceSent = false;
			}
			// Cancel the just reverted state
			mJustRevertedSeparator = null;
			if (mKeyboardSwitcher.isKeyRequireSwitchToAlphabet(primaryCode))
			{
				mKeyboardSwitcher.nextKeyboard(getCurrentInputEditorInfo(),
						NextKeyboardType.Alphabet);
			}
			break;
		}
	}

	private void showLanguageSelectionDialog() {
		KeyboardBuilder[] builders = mKeyboardSwitcher.getEnabledKeyboardsBuilders();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.icon_8_key);
		builder.setTitle(getResources().getString(R.string.select_keyboard_popup_title));
		builder.setNegativeButton(android.R.string.cancel, null);
		ArrayList<CharSequence> keyboardsIds = new ArrayList<CharSequence>();
		ArrayList<CharSequence> keyboards = new ArrayList<CharSequence>();
		//going over all enabled keyboards
		for (KeyboardBuilder keyboardBuilder : builders) {
			keyboardsIds.add(keyboardBuilder.getId());
			String name = keyboardBuilder.getPackageContext().getString(keyboardBuilder.getKeyboardNameResId());
			
			keyboards.add(name);
		}

		final CharSequence[] ids = new CharSequence[keyboardsIds.size()];
		final CharSequence[] items = new CharSequence[keyboards.size()];
		keyboardsIds.toArray(ids);
		keyboards.toArray(items);

		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface di, int position) {
				di.dismiss();
				
				if ((position < 0) || (position >= items.length)) {
					Log.d(TAG, "Keyboard selection popup canceled");
				} else {
					CharSequence id = ids[position];
					Log.d(TAG, "User selected "+items[position]+" with id "+id);
					EditorInfo currentEditorInfo = getCurrentInputEditorInfo();
					AnyKeyboard currentKeyboard = mKeyboardSwitcher.nextAlphabetKeyboard(currentEditorInfo, id.toString());
					setKeyboardStuff(currentEditorInfo, NextKeyboardType.Alphabet, currentKeyboard);
				}
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

	public void onText(CharSequence text) {
		if (DEBUG)
			Log.d("AnySoftKeyboard", "onText: " + text);
		InputConnection ic = getCurrentInputConnection();
		if (ic == null)
			return;
		//ic.beginBatchEdit();
		if (mPredicting) {
			commitTyped(ic);
		}
		ic.commitText(text, 1);
		//ic.endBatchEdit();
		updateShiftKeyState(getCurrentInputEditorInfo());
		mJustRevertedSeparator = null;
	}
	
	private static boolean isBackwordStopChar(int c) {
		return c == 32 || PUNCTUATION_CHARACTERS.contains(c);
	}

	private  void handleBackword(InputConnection ic) {
	    try{
	    if(ic == null){
	        return;
	    }
		if (mPredicting) {
			final int length = mComposing.length();
			if (length == 0) {
				return;
			}
			mComposing.delete(0, length);
			mWord.deleteLast();
			ic.setComposingText(mComposing, 1);
			if (mComposing.length() == 0) {
				mPredicting = false;
			}
			postUpdateSuggestions();
			return;
		}
		CharSequence cs = ic.getTextBeforeCursor(1, 0);
		int csl = cs.length();//check if there is no input
		if (csl == 0) {
			return;//nothing to delete
		}
		//TWO OPTIONS
		//1) Either we do like Linux and Windows (and probably ALL desktop OSes):
		//Delete all the characters till a complete word was deleted:
		/*
		 * What to do:
		 * We delete until we find a separator (the function isBackwordStopChar).
		 * Note that we MUST delete a delete a whole word! So if the backword starts
		 * at separators, we'll delete those, and then the word before:
		 * "test this,       ," -> "test "
		 */
		//Pro: same as desktop
		//Con: when auto-caps is on (the default), this will delete the previous word, which can be annoying..
		//E.g., Writing a sentence, then a period, then ASK will auto-caps, then when the user press backspace (for some reason),
		//the entire previous word deletes.
		
		//2) Or we delete all the characters till we encounter a separator, but delete at least one character.
		/*
		 * What to do:
		 * We delete until we find a separator (the function isBackwordStopChar).
		 * Note that we MUST delete a delete at least one character
		 * "test this, " -> "test this," -> "test this" -> "test "
		 */
		//Pro: Supports auto-caps, and mostly similar to desktop OSes
		//Con: Not all desktop use-cases are here.
		
		//For now, I go with option 2, but I'm open for discussion.
		
		//2b) "test this, " -> "test this"
		
		boolean stopCharAtTheEnd = isBackwordStopChar((int)cs.charAt(0)); 
		int idx = 1;
		while (true) {
			cs = ic.getTextBeforeCursor(idx, 0);
			csl = cs.length();
			if (csl < idx) {
				// read text is smaller than requested. We are at start
				break;
			}
			++idx;
			int cc = cs.charAt(0);
			boolean isBackwordStopChar = isBackwordStopChar(cc);
			if (stopCharAtTheEnd) {
				if (!isBackwordStopChar){
					--csl;
					break;
				} 
				continue;
			}
			if (isBackwordStopChar) {
				--csl;
				break;
			}
		}
		//we want to delete at least one character
		//ic.deleteSurroundingText(csl == 0 ? 1 : csl, 0);
		ic.deleteSurroundingText(csl, 0);//it is always > 0 !
		
	    }finally{
	        handleShiftStateAfterBackspace();
	    }
	}
	

	private void handleBackspace() {
		InputConnection ic = getCurrentInputConnection();	
		if (ic == null)//if we don't want to do anything, lets check null first.
            return;
		
		if (AnySoftKeyboardConfiguration.getInstance().useBackword() && mInputView != null && mInputView.isShifted())
		{
			handleBackword(ic);
			return;
		}
		
	    boolean deleteChar = false;
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
		
		handleShiftStateAfterBackspace();
		
		TextEntryState.backspace();
		if (TextEntryState.getState() == TextEntryState.STATE_UNDO_COMMIT) {
			revertLastWord(deleteChar);
			return;
		} else if (deleteChar) {
			//ensuring this is actually happens
			final int textLengthBeforeDelete = ic.getTextBeforeCursor(Integer.MAX_VALUE, 0).length();
			sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			
			int tries = 3;
			while(tries > 0)
			{
				final int textLengthAfterDelete = ic.getTextBeforeCursor(Integer.MAX_VALUE, 0).length();
				if (textLengthBeforeDelete != textLengthAfterDelete)
					break;
				else
					tries--;
			
				Log.v(TAG, "Delete did not happen. We'll wait some more for it.");
				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// if (mDeleteCount > DELETE_ACCELERATE_AT) {
			// sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			// }
		}
		mJustRevertedSeparator = null;
	}
	
	@Override
	public void sendDownUpKeyEvents(int keyEventCode) {
		super.sendDownUpKeyEvents(keyEventCode);
		//since it happens in a different process (asynch)
		//we'll let the system settle.
		try {
			Thread.sleep(10);//this is not a fix, but a bit relaxing..
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleShiftStateAfterBackspace() {
		switch(mLastCharacterShiftState)
		{
			//this code will help use in the case that
			//a double/triple tap occur while first one was shifted
		case LAST_CHAR_SHIFT_STATE_SHIFTED:
			if (mInputView != null)
				mInputView.setShifted(true);
			mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;
			break;
		case LAST_CHAR_SHIFT_STATE_UNSHIFTED:
			if (mInputView != null)
				mInputView.setShifted(false);
			mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;
			break;
		default:
			updateShiftKeyState(getCurrentInputEditorInfo());
			break;
		}
		if(mInputView != null){
		    mInputView.requestShiftKeyRedraw();
		}
	}

	private void handleShift() {
		if (mKeyboardSwitcher.isAlphabetMode()) {
			//shift pressed and this is an alphabet keyboard
			//we want to do:
			//1)if keyboard is unshifted -> shift view and keyboard
			//2)if keyboard is shifted -> capslock keyboard
			//3)if keyboard is capslocked -> unshift view and keyboard
			final AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
			final boolean caps;
			if (!currentKeyboard.isShifted())
			{
				if (DEBUG) Log.d(TAG, "handleShift: current keyboard is un-shifted");
				mInputView.setShifted(true);
				caps = false;
			}
			else
			{
				if (currentKeyboard.isShiftLocked())
				{
					if (DEBUG) Log.d(TAG, "handleShift: current keyboard is CAPSLOCKED");
					mInputView.setShifted(false);
					caps = false;
				}
				else
				{
					if (DEBUG) Log.d(TAG, "handleShift: current keyboard is shifted");
					mInputView.setShifted(true);
					caps = true;
				}
			}
			
			mCapsLock = caps;
			currentKeyboard.setShiftLocked(mCapsLock);
			
			mInputView.requestShiftKeyRedraw();
		}
	}

	private void handleCharacter(int primaryCode, int[] keyCodes) {
		if(DEBUG) Log.d("AnySoftKeyboard", "handleCharacter: "+primaryCode+", isPredictionOn:"+isPredictionOn()+", mPredicting:"+mPredicting);
		if (isAlphabet(primaryCode) && isPredictionOn()
				&& !isCursorTouchingWord()) {
			if (!mPredicting) {
				mPredicting = true;
				mComposing.setLength(0);
				mWord.reset();
			}
		}
		if(mInputView != null){
		    mLastCharacterShiftState = mInputView.isShifted()? LAST_CHAR_SHIFT_STATE_SHIFTED : LAST_CHAR_SHIFT_STATE_UNSHIFTED;
		}
		
		if (mPredicting) {
			if ((mInputView != null) && mInputView.isShifted()
					&& mComposing.length() == 0) {
				mWord.setCapitalized(true);
			}

			mComposing.append((char) primaryCode);
			if(keyCodes != null && keyCodes.length > 1){
			    if(primaryCode != keyCodes[0]){
			    int[] tmp = new int[keyCodes.length+1];
			    tmp[0] = primaryCode;
			    System.arraycopy(keyCodes, 0, tmp, 1, keyCodes.length);
			    keyCodes = tmp;
			   }
			}
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
		if (DEBUG)
			Log.d("AnySoftKeyboard",
					"translatePrimaryCodeFromCurrentKeyboard: " + primaryCode);
		if (isInputViewShown()) {
			if (DEBUG)
				Log
						.v("AnySoftKeyboard",
								"translatePrimaryCodeFromCurrentKeyboard: isInputViewShown");

			if ((mInputView != null) && mInputView.isShifted()) {
				if (DEBUG)
					Log
							.d("AnySoftKeyboard",
									"translatePrimaryCodeFromCurrentKeyboard: mInputView.isShifted()");
				return mKeyboardSwitcher.getCurrentKeyboard()
						.getShiftedKeyValue(primaryCode);
			}
		}
		return primaryCode;
	}

	private void handleSeparator(int primaryCode) {
		if(DEBUG) Log.d(TAG, "handleSeparator: "+primaryCode);
		boolean pickedDefault = false;
		// Handle separator
		InputConnection ic = getCurrentInputConnection();
//		if (ic != null) {
//			ic.beginBatchEdit();
//		}
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
							|| mJustRevertedSeparator.charAt(0) != primaryCode)) {
				pickDefaultSuggestion();
				pickedDefault = true;
			} else {
				commitTyped(ic);
			}
		}

		sendKeyChar((char) primaryCode);

		TextEntryState.typedCharacter((char) primaryCode, true);
		if (TextEntryState.getState() == TextEntryState.STATE_PUNCTUATION_AFTER_ACCEPTED
				&& primaryCode != KEYCODE_ENTER && mSpaceSent) {
			swapPunctuationAndSpace();
		} else if (/*isPredictionOn() &&*/ primaryCode == ' ') {
			// else if (TextEntryState.STATE_SPACE_AFTER_ACCEPTED) {
			doubleSpace();
		}
		if (pickedDefault && mBestWord != null) {
			TextEntryState.acceptedDefault(mWord.getTypedWord(), mBestWord);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
//		if (ic != null) {
//			ic.endBatchEdit();
//		}
	}

	private void handleClose() {
		commitTyped(getCurrentInputConnection());
		requestHideSelf(0);
		if (mInputView != null)
			mInputView.closing();
		TextEntryState.endSession();
	}

//	private void checkToggleCapsLock() {
//		if (mKeyboardSwitcher.getCurrentKeyboard().isShifted()) {
//			toggleCapsLock();
//		}
//	}

	private void postUpdateSuggestions() {
		mHandler.removeMessages(MSG_UPDATE_SUGGESTIONS);
		mHandler.sendMessageDelayed(mHandler
				.obtainMessage(MSG_UPDATE_SUGGESTIONS), 100);
	}

	private boolean isPredictionOn() {
		boolean predictionOn = mPredictionOn;
		// if (!onEvaluateInputViewShown()) predictionOn &=
		// mPredictionLandscape;
		return predictionOn;
	}

	private boolean shouldCandidatesStripBeShown() {
//		boolean shown = isPredictionOn() && (mShowSuggestions || isFullscreenMode());
//		if (!onEvaluateInputViewShown())
//			shown &= mPredictionLandscape;
//		return shown;
		return true;
	}

	private void updateSuggestions() {
		if (DEBUG)
			Log.d("AnySoftKeyboard", "updateSuggestions: has mSuggest:"
					+ (mSuggest != null) + ", isPredictionOn:"
					+ isPredictionOn() + ", mPredicting:" + mPredicting
					+ ", mCorrectionMode:" + mCorrectionMode);
		// Check if we have a suggestion engine attached.
		if (mSuggest == null) {
			return;
		}

//		final boolean showSuggestions = (mCandidateView != null && mPredicting
//				&& isPredictionOn() && shouldCandidatesStripBeShown());

		if (!mPredicting) {
			if (mCandidateView != null)
				mCandidateView.setSuggestions(null, false, false, false);
			return;
		}

		List<CharSequence> stringList = mSuggest.getSuggestions(mInputView,
				mWord, false);
		boolean correctionAvailable = mSuggest.hasMinimalCorrection();
		// || mCorrectionMode == mSuggest.CORRECTION_FULL;
		CharSequence typedWord = mWord.getTypedWord();
		// If we're in basic correct
		boolean typedWordValid = mSuggest.isValidWord(typedWord);
		if (mCorrectionMode == Suggest.CORRECTION_FULL) {
			correctionAvailable |= typedWordValid;
		}

		mCandidateView.setSuggestions(stringList, false, typedWordValid,
				correctionAvailable);
		if (stringList.size() > 0) {
			if (correctionAvailable && !typedWordValid && stringList.size() > 1) {
				mBestWord = stringList.get(1);
			} else {
				mBestWord = typedWord;
			}
		} else {
			mBestWord = null;
		}
		setCandidatesViewShown(shouldCandidatesStripBeShown() || mCompletionOn);
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

	public CharSequence pickSuggestionManually(int index, CharSequence suggestion) {
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
			return suggestion;
		}
		suggestion = pickSuggestion(suggestion);
		TextEntryState.acceptedSuggestion(mComposing.toString(), suggestion);
		// Follow it with a space
		if (mAutoSpace) {
			mSpaceSent = true;
			sendSpace();
		}
		// Fool the state watcher so that a subsequent backspace will not do a
		// revert
		TextEntryState.typedCharacter((char) KEYCODE_SPACE, true);
		return suggestion;
	}

	private CharSequence pickSuggestion(CharSequence suggestion) {
		if (mCapsLock) {
			suggestion = suggestion.toString().toUpperCase();
		} else if (preferCapitalization()
				|| (mKeyboardSwitcher.isAlphabetMode() && (mInputView != null) && mInputView
						.isShifted())) {
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

		return suggestion;
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
			//ic.beginBatchEdit();
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
			//ic.endBatchEdit();
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

	private void sendSpace() {
		sendKeyChar((char) KEYCODE_SPACE);
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	public boolean preferCapitalization() {
		return mWord.isCapitalized();
	}

	public void swipeRight() {
		//nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
		final int keyCode = AnySoftKeyboardConfiguration.getInstance().getSwipeRightKeyCode();
		if (keyCode != 0)
			onKey(keyCode, new int[]{keyCode});
	}

	public void swipeLeft() {
		//nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Symbols);
		final int keyCode = AnySoftKeyboardConfiguration.getInstance().getSwipeLeftKeyCode();
		if (keyCode != 0)
			onKey(keyCode, new int[]{keyCode});
	}

	private void nextAlterKeyboard(EditorInfo currentEditorInfo)
	{
		Log.d("AnySoftKeyboard", "nextAlterKeyboard: currentEditorInfo.inputType="
				+ currentEditorInfo.inputType);

		AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
		if (currentKeyboard == null) {
			if (DEBUG) Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. No current keyboard.");
		} else {
			if (DEBUG) Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. Current keyboard is:"
								+ currentKeyboard.getKeyboardName());
		}

		currentKeyboard = mKeyboardSwitcher.nextAlterKeyboard(currentEditorInfo);

		Log.i("AnySoftKeyboard", "nextAlterKeyboard: Setting next keyboard to: "
				+ currentKeyboard.getKeyboardName());
	}

	private void nextKeyboard(EditorInfo currentEditorInfo,
			KeyboardSwitcher.NextKeyboardType type) {
		Log.d("AnySoftKeyboard", "nextKeyboard: currentEditorInfo.inputType="
				+ currentEditorInfo.inputType + " type:" + type);

		// in numeric keyboards, the LANG key will go back to the original
		// alphabet keyboard-
		// so no need to look for the next keyboard, 'mLastSelectedKeyboard'
		// holds the last
		// keyboard used.
		AnyKeyboard currentKeyboard = mKeyboardSwitcher.nextKeyboard(currentEditorInfo, type);

		setKeyboardStuff(currentEditorInfo, type, currentKeyboard);
	}

	private void setKeyboardStuff(EditorInfo currentEditorInfo,
			KeyboardSwitcher.NextKeyboardType type, AnyKeyboard currentKeyboard) {
		Log.i("AnySoftKeyboard", "nextKeyboard: Setting next keyboard to: "
				+ currentKeyboard.getKeyboardName());
		updateShiftKeyState(currentEditorInfo);
		mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;
		// changing dictionary
		setMainDictionaryForCurrentKeyboard();
		// Notifying if needed
		if ((mKeyboardChangeNotificationType
				.equals(KEYBOARD_NOTIFICATION_ALWAYS))
				|| (mKeyboardChangeNotificationType
						.equals(KEYBOARD_NOTIFICATION_ON_PHYSICAL) && (type == NextKeyboardType.AlphabetSupportsPhysical))) {
			notifyKeyboardChangeIfNeeded();
		}
	}

	public void swipeDown() {
		final int keyCode = AnySoftKeyboardConfiguration.getInstance().getSwipeDownKeyCode();
		if (keyCode != 0)
			onKey(keyCode, new int[]{keyCode});
	}

	public void swipeUp() {
		final int keyCode = AnySoftKeyboardConfiguration.getInstance().getSwipeUpKeyCode();
		if (keyCode != 0)
			onKey(keyCode, new int[]{keyCode});
	}

	public void onPress(int primaryCode) {
		if (DEBUG) Log.d(TAG, "onPress:"+primaryCode);
		if (mVibrationDuration > 0) {
			if (DEBUG)
				Log.d(TAG, "Vibrating on key-pressed");
			((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
					.vibrate(mVibrationDuration);
		}
		if (mSoundOn/* && (!mSilentMode) */) {
			AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			// Will use sound effects ONLY if the device is not muted.
			if (manager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				final int keyFX;
				switch (primaryCode) {
				case 13:
				case 10:
					keyFX = AudioManager.FX_KEYPRESS_RETURN;
					break;
				case Keyboard.KEYCODE_DELETE:
					keyFX = AudioManager.FX_KEYPRESS_DELETE;
					break;
				case 32:
					keyFX = AudioManager.FX_KEYPRESS_SPACEBAR;
					break;
				default:
					keyFX = AudioManager.FX_KEY_CLICK;
				}
				final float fxVolume;
				//creating scoop to make sure volume and maxVolume
				//are not used
				{
					final int volume;
					final int maxVolume;
					if (mSoundVolume < 0)
					{
						//take system's volume
						volume = manager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
						maxVolume = manager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
						if (DEBUG)
							Log.d("AnySoftKeyboard", "Sound on key-pressed. Taking STREAM_NOTIFICATION: "+volume+" out of "+maxVolume);
					}
					else
					{
						volume = mSoundVolume;
						maxVolume = 100;
						if (DEBUG)
							Log.d("AnySoftKeyboard", "Sound on key-pressed. Taking custom volume: "+volume+" out of "+maxVolume);
					}
					//pre-eclair
					// volume is between 0..8 (float)
					//eclair
					// volume is between 0..1 (float)
					if (Workarounds.isEclair())
					{
						fxVolume = ((float)volume)/((float)maxVolume);
					}
					else
					{
						fxVolume = 8*((float)volume)/((float)maxVolume);
					}
				}

				if (DEBUG)
					Log.d("AnySoftKeyboard", "Sound on key-pressed. Sound ID:"
							+ keyFX + " with volume " + fxVolume);

				manager.playSoundEffect(keyFX, fxVolume);
			} else {
				if (DEBUG)
					Log.v("AnySoftKeyboard",
							"Devices is muted. No sounds on key-pressed.");
			}
		}
	}

	public void onRelease(int primaryCode) {
		if (DEBUG) Log.d(TAG, "onRelease:"+primaryCode);
		// vibrate();
	}

	private void loadSettings() {
		// setting all values to default
		PreferenceManager.setDefaultValues(this, R.layout.prefs, false);
		// Get the settings preferences
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		mVibrationDuration = Integer.parseInt(sp.getString(
				getString(R.string.settings_key_vibrate_on_key_press_duration),
				getString(R.string.settings_default_vibrate_on_key_press_duration)));

		boolean newSoundOn = sp.getBoolean(getString(R.string.settings_key_sound_on), getResources().getBoolean(R.bool.settings_default_sound_on));
		boolean soundChanged = (newSoundOn != mSoundOn);
		if (soundChanged) {
			if (newSoundOn) {
				Log
						.i(TAG,
								"Loading sounds effects from AUDIO_SERVICE due to configuration change.");
				mAudioManager.loadSoundEffects();
			} else {
				Log
						.i(TAG,
								"Releasing sounds effects from AUDIO_SERVICE due to configuration change.");
				mAudioManager.unloadSoundEffects();
			}
		}
		mSoundOn = newSoundOn;
		// checking the volume
		boolean customVolume = sp.getBoolean("use_custom_sound_volume", false);
		int newVolume;
		if (customVolume) {
			newVolume = sp.getInt("custom_sound_volume", 0);
			Log.i(TAG, "Custom volume checked: " + newVolume+" out of 100");
		} else {
			Log.i(TAG, "Custom volume un-checked.");
			newVolume = -1;
		}
		mSoundVolume = newVolume;

		// in order to support the old type of configuration
		String newKeyboardChangeNotificationType = sp.getString(
				getString(R.string.settings_key_physical_keyboard_change_notification_type),
				getString(R.string.settings_default_physical_keyboard_change_notification_type));
		boolean notificationChanged = (!newKeyboardChangeNotificationType
				.equalsIgnoreCase(mKeyboardChangeNotificationType));
		mKeyboardChangeNotificationType = newKeyboardChangeNotificationType;

		if (notificationChanged) {
			// now clearing the notification, and it will be re-shown if needed
			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			// now clearing the notification, and it will be re-shown if needed
			notificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
			// should it be always on?
			if (mKeyboardChangeNotificationType
					.equals(KEYBOARD_NOTIFICATION_ALWAYS))
				notifyKeyboardChangeIfNeeded();
		}

		mAutoCap = sp.getBoolean("auto_caps", true);

		boolean newShowSuggestions = sp.getBoolean("candidates_on", true);
		boolean suggestionsChanged = (newShowSuggestions != mShowSuggestions);
		mShowSuggestions = newShowSuggestions;
		// why check that it is "false"? Because it starts as "false", so it is
		// not 'changed'.
		if (suggestionsChanged || (!mShowSuggestions))
			setMainDictionaryForCurrentKeyboard();

		mAutoComplete = sp.getBoolean("auto_complete", true) && mShowSuggestions;

		mQuickFixes = sp.getBoolean("quick_fix", true);

		mAutoCorrectOn = /* mSuggest != null && *//*
												 * Suggestion always exists,
												 * maybe not at the moment, but
												 * shortly
												 */
		(mAutoComplete || mQuickFixes);
		mCorrectionMode = mAutoComplete ? 2
				: (mShowSuggestions/* mQuickFixes */? 1 : 0);

		mSmileyOnShortPress = sp.getBoolean("emoticon_long_press_opens_popup", false);
		mSmileyPopupType = sp.getString(getString(R.string.settings_key_smiley_popup_type), getString(R.string.settings_default_smiley_popup_type));

		((AnySoftKeyboardConfiguration.AnySoftKeyboardConfigurationImpl) mConfig).handleConfigurationChange(sp);

		if (mInputView != null)
			mInputView.setPreviewEnabled(mConfig.getShowKeyPreview());
	}

	/*package*/ void setMainDictionaryForCurrentKeyboard() {
		if (mSuggest != null) {
			if (!mShowSuggestions) {
				Log
						.d("AnySoftKeyboard",
								"No suggestion is required. I'll try to release memory from the dictionary.");
				DictionaryFactory.releaseAllDictionaries();
				mSuggest.setMainDictionary(null);
			} else {
				// It null at the creation of the application.
				if ((mKeyboardSwitcher != null)
						&& mKeyboardSwitcher.isAlphabetMode()) {
					AnyKeyboard currentKeyobard = mKeyboardSwitcher
							.getCurrentKeyboard();
					Dictionary dictionary = getDictionaryForKeyboard(currentKeyobard);

					mSuggest.setMainDictionary(dictionary);
				}
			}
		}
	}

	private Dictionary getDictionaryForKeyboard(AnyKeyboard currentKeyboard) {
		// if there is a mapping in the settings, we'll use that, else we'll
		// return the default
		String mappingSettingsKey = getDictionaryOverrideKey(currentKeyboard);
		String defaultDictionary = currentKeyboard.getDefaultDictionaryLocale();
		String dictionaryValue = getSharedPreferences().getString(mappingSettingsKey, null);
		Dictionary dictionary = null;

		if (dictionaryValue == null){
			dictionary = DictionaryFactory.getDictionaryByLanguage(currentKeyboard.getDefaultDictionaryLocale(), this);
		} else {
				Log.d("AnySoftKeyboard", "Default dictionary '" + (defaultDictionary == null? "None" : defaultDictionary)
						+ "' for keyboard '" + currentKeyboard.getKeyboardPrefId()
						+ "' has been overriden to '" + dictionaryValue + "'");
				dictionary = DictionaryFactory.getDictionaryById(dictionaryValue, this);
		}

		return dictionary;
	}

	private String getDictionaryOverrideKey(AnyKeyboard currentKeyboard) {
		String mappingSettingsKey = currentKeyboard.getKeyboardPrefId()
				+ "_override_dictionary";
		return mappingSettingsKey;
	}

	private void launchSettings() {
		handleClose();
		Intent intent = new Intent();
		intent.setClass(AnySoftKeyboard.this, SoftKeyboardSettings.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	private void launchDictionaryOverriding() {
		AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
		final String dictionaryOverridingKey = getDictionaryOverrideKey(currentKeyboard);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.icon_8_key);
		builder.setTitle(getResources().getString(
				R.string.override_dictionary_title,
				currentKeyboard.getKeyboardName()));
		builder.setNegativeButton(android.R.string.cancel, null);
		ArrayList<CharSequence> dictionaryIds = new ArrayList<CharSequence>();
		ArrayList<CharSequence> dictionaries = new ArrayList<CharSequence>();
		// null dictionary is handled as the default for the keyboard
		dictionaryIds.add(null);
		dictionaries.add(getString(R.string.override_dictionary_default));
		//making sure I know every installed dictionary
		ExternalDictionaryFactory.resetBuildersCache();
		//going over all installed dictionaries
		for (DictionaryBuilder dictionaryBuilder : ExternalDictionaryFactory.getAllBuilders(this)) {
			dictionaryIds.add(dictionaryBuilder.getId());
			String description = dictionaryBuilder.getDescription();
			if(description != null && description.length() != 0) {
				description = " (" + description + ")";
			}
			dictionaries.add(dictionaryBuilder.getDictionaryName() + description);
		}

		final CharSequence[] ids = new CharSequence[dictionaryIds.size()];
		final CharSequence[] items = new CharSequence[dictionaries.size()];
		dictionaries.toArray(items);
		dictionaryIds.toArray(ids);

		builder.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface di, int position) {
				di.dismiss();
				Editor editor = getSharedPreferences().edit();
				switch (position) {
				case 0:
					Log
							.d("AnySoftKeyboard",
									"Dictionary overriden disabled. User selected default.");
					editor.remove(dictionaryOverridingKey);
					showToastMessage(R.string.override_disabled, true);
					break;
				default:
					if ((position < 0) || (position >= items.length)) {
						Log.d("AnySoftKeyboard",
								"Dictionary override dialog canceled.");
					} else {
						CharSequence id = ids[position];
						String selectedDictionaryId = (id == null) ? null : id.toString();
						String selectedLanguageString = items[position]
								.toString();
						Log.d("AnySoftKeyboard",
								"Dictionary override. User selected "
										+ selectedLanguageString + " which corresponds to id "
										+ ((selectedDictionaryId == null) ? "(null)" : selectedDictionaryId));
						editor.putString(dictionaryOverridingKey,
								selectedDictionaryId);
						showToastMessage(getString(R.string.override_enabled,
								selectedLanguageString), true);
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
		builder.setItems(new CharSequence[] { itemSettings,
				itemOverrideDictionary, itemInputMethod },
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface di, int position) {
						di.dismiss();
						switch (position) {
						case 0:
							launchSettings();
							break;
						case 1:
							launchDictionaryOverriding();
							break;
						case 2:
							((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
									.showInputMethodPicker();
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
//		Log.i("AnySoftKeyboard", "**** onConfigurationChanged");
//		Log.i("AnySoftKeyboard", "** Locale:" + newConfig.locale.toString());
//		Log.i("AnySoftKeyboard", "** Orientation:" + newConfig.orientation + " previous:"+mOrientation);
		if (mOrientation != newConfig.orientation)
		{
			mOrientation = newConfig.orientation;
//			if (mInputView != null)
//			{
//				mInputView.closing();
//			}
			//resetting cache
			//this will force recreating of keyboards upon showing them up.
			mKeyboardSwitcher.resetKeyboardsCache();
			updateFullscreenMode();
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.d("AnySoftKeyboard", "onSharedPreferenceChanged - key:" + key);

		boolean isKeyboardKey = key.startsWith("keyboard_");
		boolean isDictionaryKey = key.startsWith("dictionary_");
		if (isKeyboardKey || isDictionaryKey) {
			mKeyboardSwitcher.makeKeyboards(true);
		}
		else
		{
			loadSettings();
			//in some cases we do want to force keyboards recreations
			if (	key.equals(getString(R.string.settings_key_top_keyboard_row_id)) ||
					key.equals("zoom_factor_keys_in_portrait") ||
					key.equals("zoom_factor_keys_in_landscape"))
			{
				mKeyboardSwitcher.makeKeyboards(true);
			}
		}
	}

	public void appendCharactersToInput(CharSequence textToCommit) {
		if (DEBUG)
			Log
					.d("AnySoftKeyboard", "appendCharactersToInput: "
							+ textToCommit);
		mWord.append(textToCommit);

		mComposing.append(textToCommit);
		appendStringToInput(textToCommit);
	}

	private void appendStringToInput(CharSequence textToCommit) {
		// handleTextDirection();
		if (DEBUG)
			Log.d("AnySoftKeyboard", "appendStringToInput: " + textToCommit);
		if (mCompletionOn) {
			getCurrentInputConnection().setComposingText(mWord.getTypedWord(),
					textToCommit.length());
			// updateCandidates();
		} else
			commitTyped(getCurrentInputConnection());

		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	public void deleteLastCharactersFromInput(int countToDelete) {
		if (DEBUG)
			Log.d("AnySoftKeyboard", "deleteLastCharactersFromInput: "
					+ countToDelete);
		if (countToDelete == 0)
			return;

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
		InputConnection ic = getCurrentInputConnection();
		if(ic != null){
		    if (mCompletionOn && shouldDeleteUsingCompletion) {
			    ic.setComposingText(mComposing, 1);
			    // updateCandidates();
		    } else {
		        ic.deleteSurroundingText(countToDelete, 0);
		    }
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	public SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(this);
	}

	public void showToastMessage(int resId, boolean forShortTime) {
		CharSequence text = getResources().getText(resId);
		showToastMessage(text, forShortTime);
	}

	private void showToastMessage(CharSequence text, boolean forShortTime) {
		int duration = forShortTime ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
		if (DEBUG)
			Log.v("AnySoftKeyboard", "showToastMessage: '" + text + "'. For: "
					+ duration);
		Toast.makeText(this.getApplication(), text, duration).show();
	}

	@Override
	public void onLowMemory() {
		Log
				.w("AnySoftKeyboard",
						"The OS has reported that it is low on memory!. I'll try to clear some cache.");
		mKeyboardSwitcher.onLowMemory();
		DictionaryFactory.onLowMemory(getDictionaryForKeyboard(mKeyboardSwitcher
				.getCurrentKeyboard()));
		super.onLowMemory();
	}

	public void endInputConnectionEdit() {
		InputConnection ic = getCurrentInputConnection();
		if (ic != null)
			ic.endBatchEdit();
	}

	public void startInputConnectionEdit() {
		InputConnection ic = getCurrentInputConnection();
		if (ic != null)
			ic.beginBatchEdit();
	}
	
	private void showSmileyDialog() {
        if (mSmileyDialog == null) {
            String[] names = getResources().getStringArray(R.array.smiley_names);
            final String[] texts = getResources().getStringArray(R.array.smiley_texts);

            final int N = names.length;

            List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
            for (int i = 0; i < N; i++) {
            	HashMap<String, Object> entry = new HashMap<String, Object>();

                entry.put("name", names[i]);
                entry.put("text", texts[i]);

                entries.add(entry);
            }

            final SimpleAdapter a = new SimpleAdapter(
                    this,
                    entries,
                    R.layout.smiley_menu_item,
                    new String[] {"name", "text"},
                    new int[] {R.id.smiley_name, R.id.smiley_text});
            SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView) {
                        Drawable img = getResources().getDrawable((Integer)data);
                        ((ImageView)view).setImageDrawable(img);
                        return true;
                    }
                    return false;
                }
            };
            a.setViewBinder(viewBinder);

            AlertDialog.Builder b = new AlertDialog.Builder(this);

            b.setTitle(getString(R.string.menu_insert_smiley));

            b.setCancelable(true);
            b.setAdapter(a, new DialogInterface.OnClickListener() {
                @SuppressWarnings("unchecked")
                public final void onClick(DialogInterface dialog, int which) {
                    HashMap<String, Object> item = (HashMap<String, Object>) a.getItem(which);
                    onText((String)item.get("text"));

                    dialog.dismiss();
                }
            });

            mSmileyDialog = b.create();
            Window window = mSmileyDialog.getWindow();
    		WindowManager.LayoutParams lp = window.getAttributes();
    		lp.token = mInputView.getWindowToken();
    		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
    		window.setAttributes(lp);
    		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }

        mSmileyDialog.show();
    }
}
