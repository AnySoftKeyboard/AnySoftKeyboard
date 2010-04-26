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
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.KeyboardSwitcher.NextKeyboardType;
import com.menny.android.anysoftkeyboard.dictionary.Dictionary;
import com.menny.android.anysoftkeyboard.dictionary.DictionaryFactory;
import com.menny.android.anysoftkeyboard.dictionary.ExternalDictionaryFactory;
import com.menny.android.anysoftkeyboard.dictionary.UserDictionaryBase;
import com.menny.android.anysoftkeyboard.dictionary.ExternalDictionaryFactory.DictionaryBuilder;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard;
import com.menny.android.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.menny.android.anysoftkeyboard.tutorials.TutorialsProvider;

/**
 * Input method implementation for Qwerty'ish keyboard.
 */
public class AnySoftKeyboard extends InputMethodService implements
		KeyboardView.OnKeyboardActionListener,
		OnSharedPreferenceChangeListener, AnyKeyboardContextProvider {
	private final static String TAG = "ASK";


	private final boolean TRACE_SDCARD = false;

	private static final int MSG_UPDATE_SUGGESTIONS = 0;
	private static final int MSG_START_TUTORIAL = 1;

	private static final int KEYCODE_ENTER = 10;
	private static final int KEYCODE_SPACE = ' ';
	private static final int KEYBOARD_NOTIFICATION_ID = 1;
	private static final String PUNCTUATION_CHARACTERS = ".\n!?,:;@<>()[]{}";

	private final AnySoftKeyboardConfiguration mConfig;
	private boolean DEBUG;

	private AnyKeyboardView mInputView;
	private CandidateViewContainer mCandidateViewContainer;
	private CandidateView mCandidateView;
	private Suggest mSuggest;
	private CompletionInfo[] mCompletions;

	private AlertDialog mOptionsDialog;

	KeyboardSwitcher mKeyboardSwitcher;
	private final HardKeyboardActionImpl mHardKeyboardAction;
	private long mMetaState;

	private UserDictionaryBase mUserDictionary;

	private StringBuilder mComposing = new StringBuilder();
	private WordComposer mWord = new WordComposer();

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

	private boolean mSwitchKeyboardOnSpace;
	private boolean mSmileyOnShortPress;
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

	// private final HardKeyboardTranslator mGenericKeyboardTranslator;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_SUGGESTIONS:
				updateSuggestions();
				break;
			case MSG_START_TUTORIAL:
				if ((mInputView != null) && mInputView.isShown()) {
					TutorialsProvider.ShowTutorialsIfNeeded(
							AnySoftKeyboard.this, mInputView);
				} else {
					// Try again soon if the view is not yet showing
					sendMessageDelayed(obtainMessage(MSG_START_TUTORIAL), 100);
				}
				break;
			}
		}
	};

	private boolean mSpaceSent;

	public AnySoftKeyboard() {
		// mGenericKeyboardTranslator = new
		// GenericPhysicalKeyboardTranslator(this);
		mConfig = AnySoftKeyboardConfiguration.getInstance();
		mHardKeyboardAction = new HardKeyboardActionImpl();
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

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.registerOnSharedPreferenceChangeListener(this);
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

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.unregisterOnSharedPreferenceChangeListener(this);
		if (mSoundOn) {
			Log.i(TAG,
					"Releasing sounds effects from AUDIO_SERVICE");
			mAudioManager.unloadSoundEffects();
		}

		mNotificationManager.cancel(KEYBOARD_NOTIFICATION_ID);

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

		if (finishingInput)
			resetComposing();// clearing any predications
	};

	@Override
	public View onCreateInputView() {
		mInputView = (AnyKeyboardView) getLayoutInflater().inflate(
				//the new layout will solve the "invalidateAllKeys" problem.
				Workarounds.isDonut()? R.layout.input_donut : R.layout.input_cupcake
				, null);

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
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		if (DEBUG)
			Log.d(TAG, "onStartInput(EditorInfo:"
					+ attribute.imeOptions + "," + attribute.inputType
					+ ", restarting:" + restarting + ")");
		super.onStartInput(attribute, restarting);

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
		switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
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
		if (AnySoftKeyboardConfiguration.getInstance().getUseFullScreenInput())
			return super.onEvaluateFullscreenMode();
		else
			return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d(TAG, "onKeyDown:"+keyCode);

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
		case KeyEvent.KEYCODE_ALT_LEFT:
		case KeyEvent.KEYCODE_ALT_RIGHT:
		case KeyEvent.KEYCODE_SHIFT_LEFT:
		case KeyEvent.KEYCODE_SHIFT_RIGHT:
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
			if (event.isAltPressed()) {
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
					if (event.isPrintingKey()) {
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

	// private boolean askTranslatorToTranslateHardKeyboardAction(int keyCode,
	// InputConnection ic, String keyboardName,
	// HardKeyboardTranslator keyTranslator)
	// {
	// if (AnySoftKeyboard.DEBUG) Log.d("AnySoftKeyborad", "Asking '" +
	// keyboardName + "' to translate key: " + keyCode);
	// if (DEBUG) Log.v("AnySoftKeyboard",
	// "Hard Keyboard Action before translation: Shift: "+mHardKeyboardAction.mPhysicalShiftState+", Alt: "+mHardKeyboardAction.mPhysicalAltState+", Key code: "+mHardKeyboardAction.getKeyCode()+", changed: "+mHardKeyboardAction.getKeyCodeWasChanged());
	// keyTranslator.translatePhysicalCharacter(mHardKeyboardAction);
	// if (DEBUG) Log.v("AnySoftKeyboard",
	// "Hard Keyboard Action after translation: Shift: "+mHardKeyboardAction.mPhysicalShiftState+", Alt: "+mHardKeyboardAction.mPhysicalAltState+", Key code: "+mHardKeyboardAction.getKeyCode()+", changed: "+mHardKeyboardAction.getKeyCodeWasChanged());
	//
	// final char translatedChar = (char)mHardKeyboardAction.consumeKeyCode();
	// if (DEBUG) Log.v("AnySoftKeyboard",
	// "Hard Keyboard Action after consumeKeyCode: Shift: "+mHardKeyboardAction.mPhysicalShiftState+", Alt: "+mHardKeyboardAction.mPhysicalAltState+", Key code: "+mHardKeyboardAction.getKeyCode());
	// if (mHardKeyboardAction.getKeyCodeWasChanged())
	// {
	// // consuming the meta keys
	// //Since I'm handling the physical keys, I also need to clear the meta
	// state
	// if (ic != null)
	// {
	// //the clear should be done only if we are not in sticky mode
	// int metaStateToClear = 0;
	// if (!mHardKeyboardAction.isShiftActive())
	// {
	// if (DEBUG) Log.v("AnySoftKeyboard",
	// "About to clear SHIFT state from input since shift state is:"+mHardKeyboardAction.mPhysicalShiftState);
	// metaStateToClear += KeyEvent.META_SHIFT_ON;
	// }
	// if (!mHardKeyboardAction.isAltActive())
	// {
	// if (DEBUG) Log.v("AnySoftKeyboard",
	// "About to clear ALT state from input since alt state is:"+mHardKeyboardAction.mPhysicalAltState);
	// metaStateToClear += KeyEvent.META_ALT_ON;
	// }
	//
	// ic.clearMetaKeyStates(metaStateToClear);//translated, so we also take
	// care of the metakeys.
	// }
	//
	// if (AnySoftKeyboard.DEBUG)
	// Log.d("AnySoftKeyborad", "'"+ keyboardName + "' translated key " +
	// keyCode + " to "+ translatedChar);
	//
	// onKey(translatedChar, new int[] { translatedChar });
	// return true;
	// } else {
	// if (AnySoftKeyboard.DEBUG)
	// Log.d("AnySoftKeyborad", "'"+ keyboardName+ "' did not translated key " +
	// keyCode+ ".");
	// return false;
	// }
	// }

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

			Notification notification = new Notification(current.getKeyboardIconResId(), keyboardName, System.currentTimeMillis());

			Intent notificationIntent = new Intent();
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);

			notification.setLatestEventInfo(getApplicationContext(),
					"Any Soft Keyboard", keyboardName,
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
		return mKeyboardSwitcher.getCurrentKeyboard().isLetter((char) code);
	}

	// Implementation of KeyboardViewListener

	public void onKey(int primaryCode, int[] keyCodes) {
		if (DEBUG)	Log.d("AnySoftKeyboard", "onKey " + primaryCode);
		//see issue 160, and "KEYCODE_SPACE" branch about this var.
		boolean switchToAlphabetAtTheEnd = false;

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
		case AnyKeyboard.KEYCODE_SMILEY:
			if (mSmileyOnShortPress) {
				// Log.d("AnySoftKeyboard", "SMILEY short: type smiley");
				onText(mConfig.getSmileyText());
			} else {
				// Log.d("AnySoftKeyboard", "SMILEY short: popup smileys");
				if (mInputView != null)
					mInputView.simulateLongPress(AnyKeyboard.KEYCODE_SMILEY);
			}
			break;
		case AnyKeyboardView.KEYCODE_SMILEY_LONGPRESS:
			if (mSmileyOnShortPress) {
				// Log.d("AnySoftKeyboard", "SMILEY long: popup smileys");
				if (mInputView != null)
					mInputView.simulateLongPress(AnyKeyboard.KEYCODE_SMILEY);
			} else {
				// Log.d("AnySoftKeyboard", "SMILEY long: type smiley");
				onText(mConfig.getSmileyText());
			}
			break;
		case Keyboard.KEYCODE_MODE_CHANGE:
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Symbols);
			break;
		case AnyKeyboard.KEYCODE_LANG_CHANGE:
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
			break;
		case AnyKeyboard.KEYCODE_ALTER_LAYOUT:
			nextAlterKeyboard(getCurrentInputEditorInfo());
			break;
		case AnyKeyboard.KEYCODE_KEYBOARD_CYCLE:
		    nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Any);
            break;
		case AnyKeyboard.KEYCODE_KEYBOARD_REVERSE_CYCLE:
		    nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.PreviousAny);
            break;
		case KEYCODE_SPACE:
			// Issue 160: Space in symbols keyboards should switch to
    		// alphabet keyboard
    		if (DEBUG)
    				Log.d(TAG, "SwitchKeyboardOnSpace: "
    						+ mSwitchKeyboardOnSpace);

			if (mSwitchKeyboardOnSpace
					&& !mKeyboardSwitcher.isAlphabetMode()) {
				switchToAlphabetAtTheEnd = true;
			}
    		//Note: letting the space fall to the DEFAULT
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
			if (switchToAlphabetAtTheEnd)
			{
				mKeyboardSwitcher.nextKeyboard(getCurrentInputEditorInfo(),
						NextKeyboardType.Alphabet);
			}
			break;
		}
	}

	public void onText(CharSequence text) {
		if (DEBUG)
			Log.d("AnySoftKeyboard", "onText: " + text);
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
				mInputView.requestRedraw();
			}
//			if (!mInputView.setShifted(mCapsLock || !mInputView.isShifted()))
//			{
//				//forcing redraw if view thinks it is still in the same state
//				mInputView.requestRedraw();
//			}
			mCapsLock = caps;
			currentKeyboard.setShiftLocked(mCapsLock);
		}
//		else {
//			mKeyboardSwitcher.toggleShift();
//		}
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
		// if (mInputView.isShifted()) {
		// primaryCode = Character.toUpperCase(primaryCode);
		// }
		if (mPredicting) {
			if ((mInputView != null) && mInputView.isShifted()
					&& mComposing.length() == 0) {
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
							|| mJustRevertedSeparator.length() == 0 || mJustRevertedSeparator
							.charAt(0) != primaryCode)) {
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
		boolean shown = isPredictionOn() && (mShowSuggestions || isFullscreenMode());
		if (!onEvaluateInputViewShown())
			shown &= mPredictionLandscape;
		return shown;
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

		final boolean showSuggestions = (mCandidateView != null && mPredicting
				&& isPredictionOn() && shouldCandidatesStripBeShown());

		if (!showSuggestions) {
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
			mSpaceSent = true;
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

		AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
		if (currentKeyboard == null) {
			if (DEBUG) Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. No current keyboard.");
		} else {
			if (DEBUG) Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. Current keyboard is:"
								+ currentKeyboard.getKeyboardName());
		}
		// in numeric keyboards, the LANG key will go back to the original
		// alphabet keyboard-
		// so no need to look for the next keyboard, 'mLastSelectedKeyboard'
		// holds the last
		// keyboard used.
		currentKeyboard = mKeyboardSwitcher.nextKeyboard(currentEditorInfo, type);

		Log.i("AnySoftKeyboard", "nextKeyboard: Setting next keyboard to: "
				+ currentKeyboard.getKeyboardName());
		updateShiftKeyState(currentEditorInfo);
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

	// private boolean mTutorialsShown = false;

	private void startTutorial() {
		// if (!mTutorialsShown)
		// {
		// mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_TUTORIAL),
		// 500);
		// mTutorialsShown = true;
		// }
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
		// setting all values to default
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		boolean handled = false;
		// Get the settings preferences
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		int newVibrationDuration = Integer.parseInt(sp.getString(
				"vibrate_on_key_press_duration", "0"));
		handled = handled || (newVibrationDuration != mVibrationDuration);
		mVibrationDuration = newVibrationDuration;

		boolean newSoundOn = sp.getBoolean("sound_on", false);
		boolean soundChanged = (newSoundOn != mSoundOn);
		if (soundChanged) {
			if (newSoundOn) {
				Log
						.i("AnySoftKeyboard",
								"Loading sounds effects from AUDIO_SERVICE due to configuration change.");
				mAudioManager.loadSoundEffects();
			} else {
				Log
						.i("AnySoftKeyboard",
								"Releasing sounds effects from AUDIO_SERVICE due to configuration change.");
				mAudioManager.unloadSoundEffects();
			}
		}
		handled = handled || soundChanged;
		mSoundOn = newSoundOn;
		// checking the volume
		boolean customVolume = sp.getBoolean("use_custom_sound_volume", false);
		int newVolume;
		if (customVolume) {
			newVolume = sp.getInt("custom_sound_volume", 0);
			Log.i("AnySoftKeyboard", "Custom volume checked: " + newVolume+" out of 100");
		} else {
			Log.i("AnySoftKeyboard", "Custom volume un-checked.");
			newVolume = -1;
		}
		handled = handled || (newVolume != mSoundVolume);
		mSoundVolume = newVolume;

		// in order to support the old type of configuration
		String newKeyboardChangeNotificationType = sp.getString(
				"physical_keyboard_change_notification_type",
				KEYBOARD_NOTIFICATION_ON_PHYSICAL);
		boolean notificationChanged = (!newKeyboardChangeNotificationType
				.equalsIgnoreCase(mKeyboardChangeNotificationType));
		handled = handled || notificationChanged;
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

		boolean newAutoCap = sp.getBoolean("auto_caps", true);
		handled = handled || (newAutoCap != mAutoCap);
		mAutoCap = newAutoCap;

		boolean newShowSuggestions = sp.getBoolean("candidates_on", true);
		boolean suggestionsChanged = (newShowSuggestions != mShowSuggestions);
		handled = handled || suggestionsChanged;
		mShowSuggestions = newShowSuggestions;
		// why check that it is "false"? Because it starts as "false", so it is
		// not 'changed'.
		if (suggestionsChanged || (!mShowSuggestions))
			setMainDictionaryForCurrentKeyboard();

		boolean newAutoComplete = sp.getBoolean("auto_complete", true)
				&& mShowSuggestions;
		handled = handled || (newAutoComplete != mAutoComplete);
		mAutoComplete = newAutoComplete;

		boolean newQuickFixes = sp.getBoolean("quick_fix", true);
		handled = handled || (newQuickFixes != mQuickFixes);
		mQuickFixes = newQuickFixes;

		mAutoCorrectOn = /* mSuggest != null && *//*
												 * Suggestion always exists,
												 * maybe not at the moment, but
												 * shortly
												 */
		(mAutoComplete || mQuickFixes);
		mCorrectionMode = mAutoComplete ? 2
				: (mShowSuggestions/* mQuickFixes */? 1 : 0);

		// boolean newLandscapePredications=
		// sp.getBoolean("physical_keyboard_suggestions", true);
		// handled = handled || (newLandscapePredications !=
		// mPredictionLandscape);
		// mPredictionLandscape = newLandscapePredications;

		boolean newSwitchKeyboardOnSpace = sp.getBoolean(
				"switch_keyboard_on_space", false);
		handled = handled
				|| (newSwitchKeyboardOnSpace != mSwitchKeyboardOnSpace);
		mSwitchKeyboardOnSpace = newSwitchKeyboardOnSpace;

		boolean newSmileyOnShort = sp.getBoolean(
				"emoticon_long_press_opens_popup", false);
		handled = handled || (newSmileyOnShort != mSmileyOnShortPress);
		mSmileyOnShortPress = newSmileyOnShort;

		// NOTE: a single "|" required here! We want the
		// 'handleConfigurationChange' function to be called ANYWAY!
		handled = handled
				| ((AnySoftKeyboardConfiguration.AnySoftKeyboardConfigurationImpl) mConfig)
						.handleConfigurationChange(sp);

		if (mInputView != null)
			mInputView.setPreviewEnabled(mConfig.getShowKeyPreview());

		return handled;
	}

	private void setMainDictionaryForCurrentKeyboard() {
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
		String defaultDictionary = currentKeyboard.getDefaultDictionaryLanguage();
		String dictionaryValue = getSharedPreferences().getString(mappingSettingsKey, null);
		Dictionary dictionary = null;

		if (dictionaryValue == null)
			dictionary = DictionaryFactory.getDictionaryByLanguage(currentKeyboard.getDefaultDictionaryLanguage(), this);
		else {
			if (dictionaryValue != null)
			{
				Log.d("AnySoftKeyboard", "Default dictionary '" + (defaultDictionary == null? "None" : defaultDictionary)
						+ "' for keyboard '" + currentKeyboard.getKeyboardPrefId()
						+ "' has been overriden to '" + dictionaryValue + "'");
				dictionary = DictionaryFactory.getDictionaryById(dictionaryValue, this);
			}
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

	private void launchDictioanryOverriding() {
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
		dictionaryIds.add(null);
		dictionaries.add(getString(R.string.override_dictionary_default));
		//making sure I know every installed dictionary
		ExternalDictionaryFactory.resetBuildersCache();
		//going over all installed dictionaries
		for (DictionaryBuilder dictionaryBuilder : ExternalDictionaryFactory.getAllBuilders(this)) {
			dictionaryIds.add(dictionaryBuilder.getId());
			dictionaries.add(dictionaryBuilder.getLanguage());
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
						String selectedDictionaryId  = ids[position].toString();
						String selectedLanguageString = items[position]
								.toString();
						Log.d("AnySoftKeyboard",
								"Dictionary override. User selected "
										+ selectedLanguageString + " which corresponds to id "
										+ selectedDictionaryId);
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
							launchDictioanryOverriding();
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
		Log.i("AnySoftKeyboard", "**** onConfigurationChanged");
		Log.i("AnySoftKeyboard", "** Locale:" + newConfig.locale.toString());
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

	public void performLengthyOperation(int textResId, final Runnable thingToDo) {
		thingToDo.run();
		// final ProgressDialog spinner = new ProgressDialog(this,
		// ProgressDialog.STYLE_SPINNER);
		//
		// Thread t = new Thread() {
		// public void run() {
		// thingToDo.run();
		// spinner.dismiss();
		// }
		// };
		// t.start();
		// spinner.setTitle(R.string.please_wait);
		// spinner.setIcon(R.drawable.icon_8_key);
		// spinner.setMessage(getResources().getText(textResId));
		// spinner.setCancelable(false);
		// spinner.show();
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
}
