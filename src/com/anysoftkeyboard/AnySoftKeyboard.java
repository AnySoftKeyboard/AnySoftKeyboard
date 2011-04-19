/*
 * Copyright (C) 2011 AnySoftKeyboard.
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

package com.anysoftkeyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.AutoText;
import android.text.ClipboardManager;
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
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.anysoftkeyboard.backup.CloudBackupRequester;
import com.anysoftkeyboard.dictionaries.AddableDictionary;
import com.anysoftkeyboard.dictionaries.AutoDictionary;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher.NextKeyboardType;
import com.anysoftkeyboard.keyboards.physical.HardKeyboardActionImpl;
import com.anysoftkeyboard.keyboards.physical.MyMetaKeyKeyListener;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardBaseView.OnKeyboardActionListener;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.quicktextkeys.QuickTextKey;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.ui.settings.MainSettings;
import com.anysoftkeyboard.ui.tutorials.TutorialsProvider;
import com.anysoftkeyboard.utils.ModifierKeyState;
import com.anysoftkeyboard.utils.Workarounds;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

/**
 * Input method implementation for Qwerty'ish keyboard.
 */
public class AnySoftKeyboard extends InputMethodService implements
		OnKeyboardActionListener,
		OnSharedPreferenceChangeListener, AnyKeyboardContextProvider {
	private final static String TAG = "ASK";
	
	public final static String NOTIFY_LAYOUT_SWITCH  = "com.menny.android.anysoftkeyboard.api.NOTIFY_LAYOUT_SWITCH";
    //API
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_RESID = "current_layout_resid";
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_NAME = "current_layout_name";
    private static final String NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE = "current_layout_package";
    private static final String NOTIFY_LAYOUT_SWITCH_NOTIFICATION_FLAGS = "notification_flags";
    private static final String NOTIFY_LAYOUT_SWITCH_NOTIFICATION_TITLE = "notification_title";

	private final boolean TRACE_SDCARD = false;

	private static final int MSG_UPDATE_SUGGESTIONS = 0;
	//private static final int MSG_START_TUTORIAL = 1;
    private static final int MSG_UPDATE_SHIFT_STATE = 2;

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
	private static final boolean DEBUG = AnySoftKeyboardConfiguration.DEBUG;

	private ModifierKeyState mShiftKeyState = new ModifierKeyState();
	
	private AnyKeyboardView mInputView;
	private LinearLayout mCandidateViewContainer;
	private CandidateView mCandidateView;
	private Suggest mSuggest;
	private CompletionInfo[] mCompletions;

	private AlertDialog mOptionsDialog;
	private AlertDialog mQuickTextKeyDialog;
	
	KeyboardSwitcher mKeyboardSwitcher;
	private final HardKeyboardActionImpl mHardKeyboardAction;
	private long mMetaState;

	//private UserDictionaryBase mContactsDictionary;
	private AddableDictionary mUserDictionary;
	private AutoDictionary mAutoDictionary;
	

	private StringBuilder mComposing = new StringBuilder();
	private WordComposer mWord = new WordComposer();

	private int mOrientation = Configuration.ORIENTATION_PORTRAIT;

	private int mCommittedLength;
	/*
	 * Do we do prediction now
	 */
	private boolean mPredicting;
	private CharSequence mBestWord;
	private final boolean mPredictionLandscape = false;
	/*
	 * is prediction needed for the current input connection
	 */
	private boolean mPredictionOn;
	/*
	 * is out-side completions needed
	 */
	private boolean mCompletionOn;
	private boolean mAutoSpace;
	private boolean mAutoCorrectOn;
	private boolean mCapsLock;

	private static final String SMILEY_PLUGIN_ID = "0077b34d-770f-4083-83e4-081957e06c27";
	private boolean mSmileyOnShortPress;
	private boolean mAutoCap;
	private boolean mQuickFixes;
	/*
	 * Configuration flag. Should we support dictionary suggestions
	 */
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
    private boolean mSilentMode;
    private boolean mSoundOn;
	// between 0..100. This is the custom volume
	private int mSoundVolume;

	private Vibrator mVibrator;
	private int mVibrationDuration;
	
	private NotificationManager mNotificationManager;

	private static AnySoftKeyboard INSTANCE;

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_SUGGESTIONS:
				performUpdateSuggestions();
				break;
			case MSG_UPDATE_SHIFT_STATE:
                updateShiftKeyState(getCurrentInputEditorInfo());
                break;
			}
		}
	};

	private boolean mJustAddedAutoSpace;

//	private static final int LAST_CHAR_SHIFT_STATE_UNKNOWN = 0;
//	private static final int LAST_CHAR_SHIFT_STATE_UNSHIFTED = 1;
//	private static final int LAST_CHAR_SHIFT_STATE_SHIFTED = 2;
//	private int mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;

	public static AnySoftKeyboard getInstance() {
		return INSTANCE;
	}

	public AnySoftKeyboard() {
		// mGenericKeyboardTranslator = new
		// GenericPhysicalKeyboardTranslator(this);
		mConfig = AnyApplication.getConfig();
		mHardKeyboardAction = new HardKeyboardActionImpl();
		INSTANCE = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// super.showStatusIcon(R.drawable.icon_8_key);
		Log.i("AnySoftKeyboard", "****** AnySoftKeyboard service started.");
		Thread.setDefaultUncaughtExceptionHandler(new ChewbaccaUncaughtExceptionHandler(getApplication().getBaseContext(), null));
		
		// showToastMessage(R.string.toast_lengthy_start_up_operation, true);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        updateRingerMode();
		// register to receive ringer mode changes for silent mode
        IntentFilter filter = new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        
		mVibrator = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));
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
		setDictionariesForCurrentKeyboard();
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "AnySoftKeyboard has been destroyed! Cleaning resources..");
		//DictionaryFactory.getInstance().close();

		// unregisterReceiver(mReceiver);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		sp.unregisterOnSharedPreferenceChangeListener(this);
		if (mSoundOn) {
			Log.i(TAG,
					"Releasing sounds effects from AUDIO_SERVICE");
			mAudioManager.unloadSoundEffects();
		}
        unregisterReceiver(mReceiver);

		mNotificationManager.cancel(KEYBOARD_NOTIFICATION_ID);
		
		
		Intent i = new Intent(NOTIFY_LAYOUT_SWITCH);
		//dome summy package, so that everybody removes notification
        i.putExtra(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE, "NO_SUCH_PACKAGE");
        sendBroadcast(i);

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
//		mInputView = (AnyKeyboardView) getLayoutInflater().inflate(
//				//the new layout will solve the "invalidateAllKeys" problem.
//				Workarounds.isDonut()? R.layout.input_donut : R.layout.input_cupcake
//				, null);
		mInputView = (AnyKeyboardView) getLayoutInflater().inflate(R.layout.input_gingerbread, null);
		//reseting token users
		mOptionsDialog = null;
		mQuickTextKeyDialog = null;
		
		//mKeyboardSwitcher.resetKeyboardsCache();
		//saving the orientation now, since the GUI is correct (we created that a second ago)
		//and the keyboard are empty
		//mOrientation = getResources().getConfiguration().orientation;
		
		mKeyboardSwitcher.setInputView(mInputView);
		//mKeyboardSwitcher.makeKeyboards(false);
		mInputView.setOnKeyboardActionListener(this);
		//mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, null);

		return mInputView;
	}

	@Override
	public View onCreateCandidatesView() {
		mKeyboardSwitcher.makeKeyboards(false);
		mCandidateViewContainer = (LinearLayout) getLayoutInflater()
				.inflate(R.layout.candidates, null);
		mCandidateView = (CandidateView) mCandidateViewContainer
				.findViewById(R.id.candidates);
		mCandidateView.setService(this);
		setCandidatesViewShown(true);
		return mCandidateViewContainer;
	}
	
	@Override
	public void onStartInput(EditorInfo attribute, boolean restarting) {
		if (DEBUG)
			Log.d(TAG, "onStartInput(EditorInfo:"+attribute+" restarting:"+restarting);
		
		super.onStartInput(attribute, restarting);
	}

	@Override
	public void onStartInputView(EditorInfo attribute, boolean restarting) {
		if (DEBUG)
			Log.d(TAG, "onStartInputView(EditorInfo:"
					+ attribute.imeOptions + "," + attribute.inputType
					+ ", restarting:" + restarting + ")");
		super.onStartInputView(attribute, restarting);

		if (mInputView == null) {
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
			case EditorInfo.TYPE_CLASS_DATETIME:
				if (DEBUG) Log.d(TAG, "Setting MODE_DATETIME as keyboard due to a TYPE_CLASS_DATETIME input.");
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_DATETIME, attribute);
				break;
			case EditorInfo.TYPE_CLASS_NUMBER:
				if (DEBUG) Log.d(TAG, "Setting MODE_NUMBERS as keyboard due to a TYPE_CLASS_NUMBER input.");
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_NUMBERS, attribute);
				break;
			case EditorInfo.TYPE_CLASS_PHONE:
				if (DEBUG) Log.d(TAG, "Setting MODE_PHONE as keyboard due to a TYPE_CLASS_PHONE input.");
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_PHONE, attribute);
				break;
			case EditorInfo.TYPE_CLASS_TEXT:
				if (DEBUG) Log.d(TAG, "A TYPE_CLASS_TEXT input.");
				final int variation = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
				switch(variation)
				{
				case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
				case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
					if (DEBUG) Log.d(TAG, "A password TYPE_CLASS_TEXT input with no prediction");
					mPredictionOn = false;
					break;
				default:
					mPredictionOn = true;
				}
				
				if (mConfig.getInsertSpaceAfterCandidatePick())
				{
					switch(variation)
					{
					case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
					case EditorInfo.TYPE_TEXT_VARIATION_URI:
						mAutoSpace = false;
						break;
					default:
						mAutoSpace = true;
					}
				}
				else
				{
					//some users don't want auto-space
					mAutoSpace = false;
				}
				
				switch(variation)
				{
				case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
					if (DEBUG) Log.d(TAG, "Setting MODE_EMAIL as keyboard due to a TYPE_TEXT_VARIATION_EMAIL_ADDRESS input.");
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_EMAIL, attribute);
					break;
				case EditorInfo.TYPE_TEXT_VARIATION_URI:
					if (DEBUG) Log.d(TAG, "Setting MODE_URL as keyboard due to a TYPE_TEXT_VARIATION_URI input.");
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_URL, attribute);
					break;
				case EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
					if (DEBUG) Log.d(TAG, "Setting MODE_IM as keyboard due to a TYPE_TEXT_VARIATION_SHORT_MESSAGE input.");
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_IM, attribute);
					break;
				default:
					if (DEBUG) Log.d(TAG, "Setting MODE_TEXT as keyboard due to a default input.");
					mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, attribute);
				}
				
				final int textFlag = attribute.inputType & EditorInfo.TYPE_MASK_FLAGS;
				switch(textFlag)
				{
				case 0x00080000://FROM API 5: EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS:
				case EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE:
					if (DEBUG) Log.d(TAG, "Input requested NO_SUGGESTIONS, or it is AUTO_COMPLETE by itself.");
					mPredictionOn = false;
					break;
				default:
					//we'll keep the previous mPredictionOn value
				}
				
				updateShiftKeyState(attribute);
				break;
			default:
				if (DEBUG) Log.d(TAG, "Setting MODE_TEXT as keyboard due to a default input.");
				//No class. Probably a console window, or no GUI input connection
				mKeyboardSwitcher.setKeyboardMode(KeyboardSwitcher.MODE_TEXT, attribute);
				mPredictionOn = false;
				mAutoSpace = true;
				updateShiftKeyState(attribute);
			}
		}
		
		mInputView.closing();
		if (AutoText.getSize(mInputView) < 1)
			mQuickFixes = true;
		
		mComposing.setLength(0);
		mPredicting = false;
		// mDeleteCount = 0;
        mJustAddedAutoSpace = false;
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
			Intent i = new Intent(NOTIFY_LAYOUT_SWITCH);
			i.putExtra(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE, NOTIFY_LAYOUT_SWITCH);//dome summy package, so that everybody removes notification
		    sendBroadcast(i);
		}
		// clearing any predications
		//resetComposing();
		// releasing some memory. Dictionaries, completions, etc.
		if (mAutoDictionary != null) mAutoDictionary.flushPendingWrites();
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
				&& !mJustAccepted)
		{
			switch (TextEntryState.getState()) {
            case ACCEPTED_DEFAULT:
                TextEntryState.reset();
                // fall through
            case SPACE_AFTER_PICKED:
                mJustAddedAutoSpace = false;  // The user moved the cursor.
                break;
			}
		}
		mJustAccepted = false;
	}
	
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
	    if(DEBUG)Log.d(TAG, "onTrackballEvent");
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
		postUpdateSuggestionsNow();
		TextEntryState.reset();
		
		//mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;
	}

	private void onPhysicalKeyboardKeyPressed() {
		if (mConfig.hideSoftKeyboardWhenPhysicalKeyPressed()) hideWindow(); 
	}

	@Override
	public void hideWindow() {
		if (TRACE_SDCARD)
			Debug.stopMethodTracing();
		
		if (mOptionsDialog != null && mOptionsDialog.isShowing()) {
			mOptionsDialog.dismiss();
			mOptionsDialog = null;
		}
		if (mQuickTextKeyDialog != null && mQuickTextKeyDialog.isShowing()) {
			mQuickTextKeyDialog.dismiss();
			mQuickTextKeyDialog = null;
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
			//we do completions :)
			
			mCompletionOn = true;
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
			setSuggestions(stringList, true, true, true);
			mBestWord = null;
			//I mean, if I'm here, it must be shown...
			setCandidatesViewShown(true);
		}
		else if (DEBUG) Log.v(TAG, "Received completions: completions should not be shown.");
	}
/*
	@Override
	public void setCandidatesViewShown(boolean shown) {
		// we show predication only in on-screen keyboard
		// (onEvaluateInputViewShown)
		// or if the physical keyboard supports candidates
		// (mPredictionLandscape)
		super.setCandidatesViewShown(shouldCandidatesStripBeShown() && shown);
	}
*/


    private void clearSuggestions() {
        setSuggestions(null, false, false, false);
    }

    private void setSuggestions(
            List<CharSequence> suggestions,
            boolean completions,
            boolean typedWordValid,
            boolean haveMinimalSuggestion) {

//        if (mIsShowingHint) {
//             setCandidatesView(mCandidateViewContainer);
//             mIsShowingHint = false;
//        }

        if (mCandidateView != null) {
            mCandidateView.setSuggestions(
                    suggestions, completions, typedWordValid, haveMinimalSuggestion);
        }
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
			return mConfig.getUseFullScreenInputInLandscape();
		default:
			return mConfig.getUseFullScreenInputInPortrait();
		}
	}

	@Override
	public boolean onKeyDown(final int keyCode, KeyEvent event) {
//		if (DEBUG)
//		{
//			Log.d(TAG, "onKeyDown:"+keyCode+" flags:"+event.getFlags());
//			
//			if (mInputView == null)
//			{
//				Log.d(TAG, "No input view");
//			}
//			else
//			{
//				Log.d(TAG, "\n canInteractWithUi:"+mInputView.canInteractWithUi()+"\n"+
//						"getHeight:"+mInputView.getHeight()+"\n"+
//						"getVisibility:"+mInputView.getVisibility()+"\n"+
//						"getWindowVisibility:"+mInputView.getWindowVisibility()+"\n"+
//						"isFocused:"+mInputView.isFocused()+"\n"+
//						"isShown:"+mInputView.isShown()+"\n");
//			}
//		}
		final boolean shouldTranslateSpecialKeys;
		if(mInputView == null || !mInputView.isShown())
			shouldTranslateSpecialKeys = false;
		else
			shouldTranslateSpecialKeys = true;
		
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
		/**** SPEACIAL translated HW keys
		 * If you add new keys here, do not forget to add to the 
		 */
		case KeyEvent.KEYCODE_CAMERA:
		     if(shouldTranslateSpecialKeys && mConfig.useCameraKeyForBackspaceBackword()){
		        handleBackword(getCurrentInputConnection());
		        return true;
		     }
		     break;
		case KeyEvent.KEYCODE_FOCUS:
		     if(shouldTranslateSpecialKeys && mConfig.useCameraKeyForBackspaceBackword()){
		    	 handleBackspace();
		    	 return true;
		     }
		     break;
		case KeyEvent.KEYCODE_VOLUME_UP:
             if(shouldTranslateSpecialKeys && mConfig.useVolumeKeyForLeftRight()){
            	 sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
		     	return true;
             }
             break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
	        if(shouldTranslateSpecialKeys && mConfig.useVolumeKeyForLeftRight()){
				sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
				return true;
	        }
	        break;
        /**** END of SPEACIAL translated HW keys code section
		 * 
		 */
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
		case KeyEvent.KEYCODE_SHIFT_LEFT:
        case KeyEvent.KEYCODE_SHIFT_RIGHT:
            if (event.isAltPressed() && Workarounds.isAltSpaceLangSwitchNotPossible()) {
                if(DEBUG)    Log.d(TAG,
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
            //NOTE: letting it fallthru to the other meta-keys
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
			if ((event.isAltPressed() && !Workarounds.isAltSpaceLangSwitchNotPossible()) || event.isShiftPressed()) {
			    if(DEBUG)
			    	if(event.isAltPressed()){
				Log.d(TAG,
								"User pressed ALT+SPACE, moving to next physical keyboard.");
			    	} else {
						Log.d(TAG,
						"User pressed SHIFT+SPACE, moving to next physical keyboard.");
			    	}
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
		default:

			// Fix issue 185, check if we should process key repeat
			if (!mConfig.getUseRepeatingKeys() && event.getRepeatCount() > 0)
				return true;

			if (mKeyboardSwitcher.isCurrentKeyboardPhysical()) {
				// sometimes, the physical keyboard will delete input, and then
				// add some.
				// we'll try to make it nice
				if (ic != null)
					ic.beginBatchEdit();
				try {
				    //issue 393, backword on the hw keyboard!
				    if(mConfig.useBackword() && keyCode == KeyEvent.KEYCODE_DEL && event.isShiftPressed()){
                        handleBackword(ic);
                        return true;
				    } else if (event.isPrintingKey()) {
						onPhysicalKeyboardKeyPressed();
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
							onKey(translatedChar, new int[] { translatedChar }, 0, 0);
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
			//mNotificationManager.notify(KEYBOARD_NOTIFICATION_ID, notification);
			Intent i = new Intent(NOTIFY_LAYOUT_SWITCH);
			i.putExtra(NOTIFY_LAYOUT_SWITCH_NOTIFICATION_TITLE, getText(R.string.ime_name));
			i.putExtra(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_RESID,   mKeyboardSwitcher.getCurrentKeyboard().getKeyboardIconResId());
			i.putExtra(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_NAME,    mKeyboardSwitcher.getCurrentKeyboard().getKeyboardName());
			i.putExtra(NOTIFY_LAYOUT_SWITCH_CURRENT_LAYOUT_PACKAGE, mKeyboardSwitcher.getCurrentKeyboard().getKeyboardContext().getPackageName());
			i.putExtra(NOTIFY_LAYOUT_SWITCH_NOTIFICATION_FLAGS, notification.flags);
			sendBroadcast(i);
			
		}
	}

	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
        //Issue 248
		case KeyEvent.KEYCODE_VOLUME_DOWN:
	    case KeyEvent.KEYCODE_VOLUME_UP:
	        if(mInputView == null || !mInputView.isShown()){
	            return super.onKeyUp(keyCode, event);
	        }else if(mConfig.useVolumeKeyForLeftRight()){
	            //no need of vol up/down sound
	            return true;
	        }
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
	
	private void addToDictionaries(CharSequence suggestion, int frequencyDelta) {
        checkAddToDictionary(suggestion, frequencyDelta/*, false*/);
    }
	
	/**
     * Adds to the UserBigramDictionary and/or AutoDictionary
     * @param addToBigramDictionary true if it should be added to bigram dictionary if possible
     */
    private void checkAddToDictionary(CharSequence suggestion, int frequencyDelta/*,
            boolean addToBigramDictionary*/) {
        if (suggestion == null || suggestion.length() < 1) return;
        // Only auto-add to dictionary if auto-correct is ON. Otherwise we'll be
        // adding words in situations where the user or application really didn't
        // want corrections enabled or learned.
        if (!(mCorrectionMode == Suggest.CORRECTION_FULL/*
                || mCorrectionMode == Suggest.CORRECTION_FULL_BIGRAM*/)) {
            return;
        }
        if (suggestion != null && mAutoDictionary != null) {
            if (/*!addToBigramDictionary &&*/ mAutoDictionary.isValidWord(suggestion)
                    || (!mSuggest.isValidWord(suggestion.toString())
                    && !mSuggest.isValidWord(suggestion.toString().toLowerCase()))) {
                mAutoDictionary.addWord(suggestion.toString(), frequencyDelta);
            }
            /*
            if (mUserBigramDictionary != null) {
                CharSequence prevWord = EditingUtil.getPreviousWord(getCurrentInputConnection(),
                        mSentenceSeparators);
                if (!TextUtils.isEmpty(prevWord)) {
                    mUserBigramDictionary.addBigrams(prevWord.toString(), suggestion.toString());
                }
            }*/
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
				addToDictionaries(mComposing, AutoDictionary.FREQUENCY_FOR_TYPED);
			}
			postUpdateSuggestionsNow();
		}
	}

	private void postUpdateShiftKeyState() {
        mHandler.removeMessages(MSG_UPDATE_SHIFT_STATE);
        // TODO: Should remove this 300ms delay?
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_SHIFT_STATE), 300);
    }

    public void updateShiftKeyState(EditorInfo attr) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null && attr != null && mKeyboardSwitcher.isAlphabetMode()) {
            mInputView.setShifted(mShiftKeyState.isMomentary() || mCapsLock
                    || getCursorCapsMode(ic, attr) != 0);
        }
    }

    private int getCursorCapsMode(InputConnection ic, EditorInfo attr) {
        int caps = 0;
        EditorInfo ei = getCurrentInputEditorInfo();
        if (mAutoCap && ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
            caps = ic.getCursorCapsMode(attr.inputType);
        }
        return caps;
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
            mJustAddedAutoSpace = true;
		}
	}

	private void doubleSpace() {
		// if (!mAutoPunctuate) return;
		if (!mConfig.isDoubleSpaceChangesToPeriod())
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
            mJustAddedAutoSpace = true;
		}
	}

    private void removeTrailingSpace() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        CharSequence lastOne = ic.getTextBeforeCursor(1, 0);
        if (lastOne != null && lastOne.length() == 1
                && lastOne.charAt(0) == KEYCODE_SPACE) {
            ic.deleteSurroundingText(1, 0);
        }
    }

	public boolean addWordToDictionary(String word) {
		mUserDictionary.addWord(word, 32*1024);
		return true;
	}

	/**
	 * Helper to determine if a given character code is alphabetic.
	 */
	private boolean isAlphabet(int code) {
		//inner letters have more options: ' in English. " in Hebrew, and more.
		if (mPredicting)
			return mKeyboardSwitcher.getCurrentKeyboard().isInnerWordLetter((char) code);
		else
			return mKeyboardSwitcher.getCurrentKeyboard().isStartOfWordLetter((char) code);
	}


	public void onKey(int primaryCode, int[] keyCodes, int x, int y) {
		if (DEBUG)	Log.d(TAG, "onKey " + primaryCode);
		
		switch (primaryCode) {
		case Keyboard.KEYCODE_DELETE:
			handleBackspace();
			break;
		case Keyboard.KEYCODE_SHIFT:
			if (!mInputView.hasDistinctMultitouch())
				handleShift(false);
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
		case AnyKeyboard.KEYCODE_QUICK_TEXT:
			QuickTextKey quickTextKey = QuickTextKeyFactory.getCurrentQuickTextKey(this);

			boolean printDefaultText;
			if (quickTextKey.getId().equals(SMILEY_PLUGIN_ID)) {
				printDefaultText = mSmileyOnShortPress;
			} else {
				printDefaultText = quickTextKey.getKeyOutputText() != null;
			}

			if (printDefaultText) {
				onText(quickTextKey.getKeyOutputText());
			} else {
//				if (quickTextKey.isPopupKeyboardUsed()) {
//					showQuickTextKeyPopupKeyboard(quickTextKey);
//				} else {
					showQuickTextKeyPopupList(quickTextKey);
//				}
			}
			break;
		case AnyKeyboardView.KEYCODE_QUICK_TEXT_LONGPRESS:
			quickTextKey = QuickTextKeyFactory.getCurrentQuickTextKey(this);
			if (quickTextKey.getId().equals(SMILEY_PLUGIN_ID) && !mSmileyOnShortPress) {
				onText(quickTextKey.getKeyOutputText());
			} else {
				if (quickTextKey.isPopupKeyboardUsed()) {
					showQuickTextKeyPopupKeyboard(quickTextKey);
				} else {
					showQuickTextKeyPopupList(quickTextKey);
				}
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
		case AnyKeyboard.KEYCODE_KEYBOARD_CYCLE_INSIDE_MODE:
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.AnyInsideMode);
			break;
		case AnyKeyboard.KEYCODE_KEYBOARD_MODE_CHANGE:
			nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.OtherMode);
			break;
		case AnyKeyboard.KEYCODE_CLIPBOARD:
		    ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
		    if(cm.hasText()){
		        onText(cm.getText());
		    }
		    break;
		default:
			// Issue 146: Right to left langs require reversed parenthesis
			if (mKeyboardSwitcher.isRightToLeftMode())
			{
				if (primaryCode == (int)')')
					primaryCode = (int)'(';
				else if (primaryCode == (int)'(')
					primaryCode = (int)')';
			}
			if (isWordSeparator(primaryCode)) {
				handleSeparator(primaryCode);
			} else {
				handleCharacter(primaryCode, keyCodes);
				
				// reseting the mSpaceSent, which is set to true upon selecting
				// candidate
				mJustAddedAutoSpace = false;
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
		KeyboardAddOnAndBuilder[] builders = mKeyboardSwitcher.getEnabledKeyboardsBuilders();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setIcon(R.drawable.icon_8_key);
		builder.setTitle(getResources().getString(R.string.select_keyboard_popup_title));
		builder.setNegativeButton(android.R.string.cancel, null);
		ArrayList<CharSequence> keyboardsIds = new ArrayList<CharSequence>();
		ArrayList<CharSequence> keyboards = new ArrayList<CharSequence>();
		//going over all enabled keyboards
		for (KeyboardAddOnAndBuilder keyboardBuilder : builders) {
			keyboardsIds.add(keyboardBuilder.getId());
			String name = keyboardBuilder.getName();
			
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
				    if (DEBUG)Log.d(TAG, "Keyboard selection popup canceled");
				} else {
					CharSequence id = ids[position];
					if (DEBUG)Log.d(TAG, "User selected "+items[position]+" with id "+id);
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
        mJustAddedAutoSpace = false;
	}
	
	private static boolean isBackwordStopChar(int c) {
		return c == 32 || PUNCTUATION_CHARACTERS.contains(c);
	}

	private void handleBackword(InputConnection ic) {
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
			//int csl = cs.length();//check if there is no input
			if (TextUtils.isEmpty(cs)) {
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
			int csl = 0;
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
		
		if (mConfig.useBackword() && mInputView != null && mInputView.isShifted())
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
		
		TextEntryState.backspace();
		if (TextEntryState.getState() == TextEntryState.State.UNDO_COMMIT) {
			revertLastWord(deleteChar);
			handleShiftStateAfterBackspace();
		} else if (deleteChar) {
			if (mCandidateView != null && mCandidateView.dismissAddToDictionaryHint()) {
                // Go back to the suggestion mode if the user canceled the
                // "Touch again to save".
                // NOTE: In gerenal, we don't revert the word when backspacing
                // from a manual suggestion pick.  We deliberately chose a
                // different behavior only in the case of picking the first
                // suggestion (typed word).  It's intentional to have made this
                // inconsistent with backspacing after selecting other suggestions.
                revertLastWord(deleteChar);
            }
			
			final CharSequence beforeText = ic.getTextBeforeCursor(1, 0);
			final int textLengthBeforeDelete = (TextUtils.isEmpty(beforeText))? 0 : beforeText.length();
			if (textLengthBeforeDelete > 0)
				ic.deleteSurroundingText(1, 0);
			else
				sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
			
			mJustRevertedSeparator = null;
		}
		handleShiftStateAfterBackspace();
	}
	
//	@Override
//	public void sendDownUpKeyEvents(int keyEventCode) {
//		super.sendDownUpKeyEvents(keyEventCode);
//		//since it happens in a different process (asynch)
//		//we'll let the system settle.
//		Thread.yield();//this is not a fix, but a bit relaxing..
//	}

	private void handleShiftStateAfterBackspace() {
//		switch(mLastCharacterShiftState)
//		{
//			//this code will help use in the case that
//			//a double/triple tap occur while first one was shifted
//		case LAST_CHAR_SHIFT_STATE_SHIFTED:
//			if (mInputView != null)
//				mInputView.setShifted(true);
//			mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;
//			break;
//		case LAST_CHAR_SHIFT_STATE_UNSHIFTED:
//			if (mInputView != null)
//				mInputView.setShifted(false);
//			mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;
//			break;
//		default:
//			updateShiftKeyState(getCurrentInputEditorInfo());
//			break;
//		}
	}

	private void handleShift(boolean reset) {
		mHandler.removeMessages(MSG_UPDATE_SHIFT_STATE);
		
		if (mKeyboardSwitcher.isAlphabetMode()) {
			//shift pressed and this is an alphabet keyboard
			//we want to do:
			//1)if keyboard is unshifted -> shift view and keyboard
			//2)if keyboard is shifted -> capslock keyboard
			//3)if keyboard is capslocked -> unshift view and keyboard
			final AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
			if (AnySoftKeyboardConfiguration.DEBUG)
			{
				final AnyKeyboard viewKeyboard = (AnyKeyboard)mInputView.getKeyboard();
				if (currentKeyboard != viewKeyboard)
				{
					Log.e(TAG, "NOTE: view keyboard and switcher keyboard are not the same!");
				}
			}
			
			final boolean caps;
			if (reset)
			{
				if (DEBUG) Log.d(TAG, "handleShift: reset");
				mInputView.setShifted(false);
				caps = false;
			}
			else
			{
				if (!mInputView.isShifted())
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
			}
			mCapsLock = caps;
			currentKeyboard.setShiftLocked(mCapsLock);
		}
	}

	private void handleCharacter(final int primaryCode, int[] keyCodes) {
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
		    //mLastCharacterShiftState = mInputView.isShifted()? LAST_CHAR_SHIFT_STATE_SHIFTED : LAST_CHAR_SHIFT_STATE_UNSHIFTED;
		}
		
		final int primaryCodeForShow;
		if (mInputView != null && mInputView.isShifted())
			primaryCodeForShow = Character.toUpperCase(primaryCode);
		else
			primaryCodeForShow = primaryCode;
		
		if (mPredicting) {
			if ((mInputView != null) && mInputView.isShifted()
					&& mComposing.length() == 0) {
				mWord.setCapitalized(true);
			}
			
			mComposing.append((char) primaryCodeForShow);
			if(keyCodes != null && keyCodes.length > 1 && primaryCode != keyCodes[0]){
				int swapedItem = keyCodes[0];
				keyCodes[0] = primaryCode;
				for(int i=1;i<keyCodes.length; i++)
				{
					if (keyCodes[i] == primaryCode)
					{
						keyCodes[i] = swapedItem;
						break;
					}
				}
//				int[] tmp = new int[keyCodes.length+1];
//			    tmp[0] = primaryCode;
//			    System.arraycopy(keyCodes, 0, tmp, 1, keyCodes.length);
//			    keyCodes = tmp;
			}
			if (mWord.add(primaryCodeForShow, keyCodes))
			{
				Toast note = Toast.makeText(this, "Check the logcat for a note from AnySoftKeyboard developers!", Toast.LENGTH_LONG);
				note.show();
				
				Log.i(TAG, "*******************"
						+"\nNICE!!! You found the our easter egg! http://www.dailymotion.com/video/x3zg90_gnarls-barkley-crazy-2006-mtv-star_music\n"
						+"\nAnySoftKeyboard R&D team would like to thank you for using our keyboard application."
						+"\nWe hope you enjoying it, we enjoyed making it."
						+"\nWhile developing this application, we heard Gnarls Barkley's Crazy quite a lot, and would like to share it with you."
						+"\n"
						+"\nThanks."
						+"\nMenny Even Danan, Hezi Cohen, Hugo Lopes, Henrik Andersson, Sami Salonen, and Lado Kumsiashvili."
						+"\n*******************");
				
				Intent easterEgg = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.dailymotion.com/video/x3zg90_gnarls-barkley-crazy-2006-mtv-star_music"));
				easterEgg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(easterEgg);
			}
			InputConnection ic = getCurrentInputConnection();
			if (ic != null) {
				ic.setComposingText(mComposing, 1);
			}
			postUpdateSuggestions();
		} else {
			sendKeyChar((char) primaryCodeForShow);
		}
		updateShiftKeyState(getCurrentInputEditorInfo());
		// measureCps();
		TextEntryState.typedCharacter((char) primaryCodeForShow,
				isWordSeparator(primaryCodeForShow));
	}

	private void handleSeparator(int primaryCode) {
		if(DEBUG) Log.d(TAG, "handleSeparator: "+primaryCode);
		
		// Should dismiss the "Touch again to save" message when handling separator
        if (mCandidateView != null && mCandidateView.dismissAddToDictionaryHint()) {
            postUpdateSuggestions();
        }
        
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
							|| mJustRevertedSeparator.charAt(0) != primaryCode)) {
				pickedDefault = pickDefaultSuggestion();
				// Picked the suggestion by the space key.  We consider this
                // as "added an auto space".
                if (primaryCode == KEYCODE_SPACE) {
                    mJustAddedAutoSpace = true;
                }
			} else {
				commitTyped(ic);
			}
		}
        if (mJustAddedAutoSpace && primaryCode == KEYCODE_ENTER) {
            removeTrailingSpace();
            mJustAddedAutoSpace = false;
        }

		sendKeyChar((char) primaryCode);

		TextEntryState.typedCharacter((char) primaryCode, true);
		if (TextEntryState.getState() == TextEntryState.State.PUNCTUATION_AFTER_ACCEPTED
				&& primaryCode != KEYCODE_ENTER) {
			swapPunctuationAndSpace();
		} else if (/*isPredictionOn() &&*/ primaryCode == ' ') {
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
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_SUGGESTIONS), 100);
	}
	
	private void postUpdateSuggestionsNow() {
		mHandler.removeMessages(MSG_UPDATE_SUGGESTIONS);
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_SUGGESTIONS));
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
//		return true;
//		return isPredictionOn() || isFullscreenMode();
		return true;
	}

	private void performUpdateSuggestions() {
		if (DEBUG)
			Log.d(TAG, "performUpdateSuggestions: has mSuggest:"
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

		List<CharSequence> stringList = mSuggest.getSuggestions(mInputView, mWord, false);
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
		setCandidatesViewShown(shouldCandidatesStripBeShown() || mCompletionOn);
	}

	private boolean pickDefaultSuggestion() {
		// Complete any pending candidate query first
		if (mHandler.hasMessages(MSG_UPDATE_SUGGESTIONS)) {
			mHandler.removeMessages(MSG_UPDATE_SUGGESTIONS);
			postUpdateSuggestionsNow();
		}

		if (mBestWord != null) {
			TextEntryState.acceptedDefault(mWord.getTypedWord(), mBestWord);
			mJustAccepted = true;
			pickSuggestion(mBestWord);
			  // Add the word to the auto dictionary if it's not a known word
            addToDictionaries(mBestWord, AutoDictionary.FREQUENCY_FOR_TYPED);
            return true;
		}
		return false;
	}
	
	private CharSequence pickSuggestion(CharSequence suggestion) {
        if (mCapsLock) {
                suggestion = suggestion.toString().toUpperCase();
        } else if (preferCapitalization()
                        || (mKeyboardSwitcher.isAlphabetMode() && (mInputView != null) && mInputView .isShifted())) {
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

	public void pickSuggestionManually(int index, CharSequence suggestion) {
		
		final boolean correcting = TextEntryState.isCorrecting();
		final InputConnection ic = getCurrentInputConnection();
		if (ic != null) {
			ic.beginBatchEdit();
		}
		try
		{
			if (mCompletionOn && mCompletions != null && index >= 0
					&& index < mCompletions.length) {
				CompletionInfo ci = mCompletions[index];
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
			pickSuggestion(suggestion, correcting);
			// Add the word to the auto dictionary if it's not a known word
	        if (index == 0) {
	            addToDictionaries(suggestion, AutoDictionary.FREQUENCY_FOR_PICKED);
	        }
	        
			TextEntryState.acceptedSuggestion(mComposing.toString(), suggestion);
			// Follow it with a space
			if (mAutoSpace && !correcting) {
				sendSpace();
				mJustAddedAutoSpace = true;
			}
			
			final boolean showingAddToDictionaryHint = index == 0 && mCorrectionMode > 0
			        && !mSuggest.isValidWord(suggestion)
			        && !mSuggest.isValidWord(suggestion.toString().toLowerCase());
		
			if (!correcting) {
	            // Fool the state watcher so that a subsequent backspace will not do a revert, unless
	            // we just did a correction, in which case we need to stay in
	            // TextEntryState.State.PICKED_SUGGESTION state.
	            TextEntryState.typedCharacter((char) KEYCODE_SPACE, true);
	            setNextSuggestions();
	        } else if (!showingAddToDictionaryHint) {
	            // If we're not showing the "Touch again to save", then show corrections again.
	            // In case the cursor position doesn't change, make sure we show the suggestions again.
	            clearSuggestions();
	            //postUpdateOldSuggestions();
	        }
				
			if (showingAddToDictionaryHint)	{
				mCandidateView.showAddToDictionaryHint(suggestion);
			}
		}
		finally
		{
			if (ic != null)
			{
				ic.endBatchEdit();
			}			
		}
	}

	/**
     * Commits the chosen word to the text field and saves it for later
     * retrieval.
     * @param suggestion the suggestion picked by the user to be committed to
     *            the text field
     * @param correcting whether this is due to a correction of an existing
     *            word.
     */
    private void pickSuggestion(CharSequence suggestion, boolean correcting) {
		if (mCapsLock) {
			suggestion = suggestion.toString().toUpperCase();
		} else if (preferCapitalization()
				|| (mKeyboardSwitcher.isAlphabetMode() && (mInputView != null) && mInputView .isShifted())) {
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
		// If we just corrected a word, then don't show punctuations
        if (!correcting) {
            setNextSuggestions();
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

    private void setNextSuggestions() {
        setSuggestions(new ArrayList<CharSequence>(), false, false, false);
    }

	public boolean isWordSeparator(int code) {
		// String separators = getWordSeparators();
		// return separators.contains(String.valueOf((char)code));
		return (!isAlphabet(code));
	}

	private void sendSpace() {
		sendKeyChar(' ');
		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	public boolean preferCapitalization() {
		return mWord.isCapitalized();
	}

	public void swipeRight() {
		//nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
		final int keyCode = mConfig.getSwipeRightKeyCode();
		if (keyCode != 0)
			onKey(keyCode, new int[]{keyCode}, -1, -1);
	}

	public void swipeLeft() {
		//nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Symbols);
		final int keyCode = mConfig.getSwipeLeftKeyCode();
		if (keyCode != 0)
			onKey(keyCode, new int[]{keyCode}, -1, -1);
	}

	private void nextAlterKeyboard(EditorInfo currentEditorInfo)
	{
		if(DEBUG)Log.d("AnySoftKeyboard", "nextAlterKeyboard: currentEditorInfo.inputType="
				+ currentEditorInfo.inputType);

		AnyKeyboard currentKeyboard = mKeyboardSwitcher.getCurrentKeyboard();
		if (currentKeyboard == null) {
			if (DEBUG) Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. No current keyboard.");
		} else {
			if (DEBUG) Log.d("AnySoftKeyboard", "nextKeyboard: Looking for next keyboard. Current keyboard is:"
								+ currentKeyboard.getKeyboardName());
		}

		currentKeyboard = mKeyboardSwitcher.nextAlterKeyboard(currentEditorInfo);

		Log.i(TAG, "nextAlterKeyboard: Setting next keyboard to: "
				+ currentKeyboard.getKeyboardName());
	}

	private void nextKeyboard(EditorInfo currentEditorInfo,
			KeyboardSwitcher.NextKeyboardType type) {
	    if (DEBUG) Log.d(TAG, "nextKeyboard: currentEditorInfo.inputType="
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
		Log.i(TAG, "nextKeyboard: Setting next keyboard to: "
				+ currentKeyboard.getKeyboardName());
		updateShiftKeyState(currentEditorInfo);
		mCapsLock = currentKeyboard.isShiftLocked();
		//mLastCharacterShiftState = LAST_CHAR_SHIFT_STATE_UNKNOWN;
		// changing dictionary
		setDictionariesForCurrentKeyboard();
		// Notifying if needed
		if ((mKeyboardChangeNotificationType
				.equals(KEYBOARD_NOTIFICATION_ALWAYS))
				|| (mKeyboardChangeNotificationType
						.equals(KEYBOARD_NOTIFICATION_ON_PHYSICAL) && (type == NextKeyboardType.AlphabetSupportsPhysical))) {
			notifyKeyboardChangeIfNeeded();
		}
	}

	public void swipeDown() {
		final int keyCode = mConfig.getSwipeDownKeyCode();
		if (keyCode != 0)
			onKey(keyCode, new int[]{keyCode}, -1, -1);
	}

	public void swipeUp() {
		final int keyCode = mConfig.getSwipeUpKeyCode();
		if (keyCode != 0)
			onKey(keyCode, new int[]{keyCode}, -1, -1);
	}

	public void onPress(int primaryCode) {
		if (DEBUG) Log.d(TAG, "onPress:"+primaryCode);
		if (mVibrationDuration > 0 && primaryCode!=0) {
			mVibrator.vibrate(mVibrationDuration);
		}
		
		final boolean distinctMultiTouch = mInputView.hasDistinctMultitouch();
        if (distinctMultiTouch && primaryCode == Keyboard.KEYCODE_SHIFT) {
            mShiftKeyState.onPress();
            handleShift(false);
        } else {
            mShiftKeyState.onOtherKeyPressed();
        }
        
		if (mSoundOn && (!mSilentMode) && primaryCode!=0) {
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
				if (mSoundVolume > 0)
				{
					volume = mSoundVolume;
					maxVolume = 100;
					if (DEBUG)
						Log.d("AnySoftKeyboard", "Sound on key-pressed. Taking custom volume: "+volume+" out of "+maxVolume);
					//pre-eclair
					// volume is between 0..8 (float)
					//eclair
					// volume is between 0..1 (float)
					if (Workarounds.getApiLevel() >= 5)
					{
						fxVolume = ((float)volume)/((float)maxVolume);
					}
					else
					{
						fxVolume = 8*((float)volume)/((float)maxVolume);
					}
				}
				else
				{
					fxVolume = -1.0f;
				}
				
			}

			if (DEBUG)
				Log.d("AnySoftKeyboard", "Sound on key-pressed. Sound ID:"
						+ keyFX + " with volume " + fxVolume);

			mAudioManager.playSoundEffect(keyFX, fxVolume);
		}
	}

	public void onRelease(int primaryCode) {
		if (DEBUG) Log.d(TAG, "onRelease:"+primaryCode);
		// vibrate();
		// Reset any drag flags in the keyboard
        //((AnyKeyboard) mInputView.getKeyboard()).keyReleased();
        //vibrate();
        final boolean distinctMultiTouch = mInputView.hasDistinctMultitouch();
        if (distinctMultiTouch && primaryCode == Keyboard.KEYCODE_SHIFT) {
            if (mShiftKeyState.isMomentary())
                handleShift(true);
            mShiftKeyState.onRelease();
        }
	}

	// receive ringer mode changes to detect silent mode
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRingerMode();
        }
    };
    
 // update flags for silent mode
    private void updateRingerMode() {
        mSilentMode = (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL);
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

		mSoundOn = sp.getBoolean(getString(R.string.settings_key_sound_on), getResources().getBoolean(R.bool.settings_default_sound_on));
		if (mSoundOn) {
			Log.i(TAG, "Loading sounds effects from AUDIO_SERVICE due to configuration change.");
			mAudioManager.loadSoundEffects();
		} else {
			Log.i(TAG, "Releasing sounds effects from AUDIO_SERVICE due to configuration change.");
			mAudioManager.unloadSoundEffects();
		}
		// checking the volume
		boolean customVolume = sp.getBoolean("use_custom_sound_volume", false);
		int newVolume;
		if (customVolume) {
			newVolume = sp.getInt("custom_sound_volume", 0) + 1;
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
			setDictionariesForCurrentKeyboard();

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

		mSmileyOnShortPress = sp.getBoolean(getString(R.string.settings_key_emoticon_long_press_opens_popup), getResources().getBoolean(R.bool.settings_default_emoticon_long_press_opens_popup));
//		mSmileyPopupType = sp.getString(getString(R.string.settings_key_smiley_popup_type), getString(R.string.settings_default_smiley_popup_type));

		((AnySoftKeyboardConfiguration.AnySoftKeyboardConfigurationImpl) mConfig).handleConfigurationChange(sp);
	}

	private void setDictionariesForCurrentKeyboard() {
		if (mSuggest != null) {
			if (!mShowSuggestions) {
			    if (DEBUG)Log.d(TAG, "No suggestion is required. I'll try to release memory from the dictionary.");
				//DictionaryFactory.getInstance().releaseAllDictionaries();
				mSuggest.setMainDictionary(null);
				mSuggest.setUserDictionary(null);
				mSuggest.setAutoDictionary(null);
			} else {
				// It null at the creation of the application.
				if ((mKeyboardSwitcher != null)
						&& mKeyboardSwitcher.isAlphabetMode()) {
					AnyKeyboard currentKeyobard = mKeyboardSwitcher.getCurrentKeyboard();
					
					// if there is a mapping in the settings, we'll use that, else we'll
					// return the default
					String mappingSettingsKey = getDictionaryOverrideKey(currentKeyobard);
					String defaultDictionary = currentKeyobard.getDefaultDictionaryLocale();
					String dictionaryValue = getSharedPreferences().getString(mappingSettingsKey, null);
					Dictionary dictionary = null;

					if (dictionaryValue == null){
						dictionary = DictionaryFactory.getInstance().getDictionaryByLanguage(currentKeyobard.getDefaultDictionaryLocale(), this);
					} else {
					        if (AnySoftKeyboardConfiguration.DEBUG)
					        {
					        	Log.d("AnySoftKeyboard", "Default dictionary '" + (defaultDictionary == null? "None" : defaultDictionary)
					        			+ "' for keyboard '" + currentKeyobard.getKeyboardPrefId()
					        			+ "' has been overriden to '" + dictionaryValue + "'");
					        }
							dictionary = DictionaryFactory.getInstance().getDictionaryById(dictionaryValue, this);
					}
					
					mSuggest.setMainDictionary(dictionary);
					
					mUserDictionary = DictionaryFactory.getInstance().createUserDictionary(this, defaultDictionary);
					mSuggest.setUserDictionary(mUserDictionary);
					
					mAutoDictionary = DictionaryFactory.getInstance().createAutoDictionary(this, this, defaultDictionary);
					mSuggest.setAutoDictionary(mAutoDictionary);
				}
			}
		}
	}

	private String getDictionaryOverrideKey(AnyKeyboard currentKeyboard) {
		String mappingSettingsKey = currentKeyboard.getKeyboardPrefId()
				+ "_override_dictionary";
		return mappingSettingsKey;
	}

	private void launchSettings() {
		handleClose();
		Intent intent = new Intent();
		intent.setClass(AnySoftKeyboard.this, MainSettings.class);
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
		//going over all installed dictionaries
		for (DictionaryAddOnAndBuilder dictionaryBuilder : ExternalDictionaryFactory.getAllAvailableExternalDictionaries(this)) {
			dictionaryIds.add(dictionaryBuilder.getId());
			String description = dictionaryBuilder.getDescription();
			if(description != null && description.length() != 0) {
				description = " (" + description + ")";
			}
			dictionaries.add(dictionaryBuilder.getName() + description);
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
				    if (AnySoftKeyboardConfiguration.DEBUG)
					Log
							.d("AnySoftKeyboard",
									"Dictionary overriden disabled. User selected default.");
					editor.remove(dictionaryOverridingKey);
					showToastMessage(R.string.override_disabled, true);
					break;
				default:
					if ((position < 0) || (position >= items.length)) {
					    if (DEBUG)
						Log.d("AnySoftKeyboard",
								"Dictionary override dialog canceled.");
					} else {
						CharSequence id = ids[position];
						String selectedDictionaryId = (id == null) ? null : id.toString();
						String selectedLanguageString = items[position]
								.toString();
						if (DEBUG)
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
				setDictionariesForCurrentKeyboard();
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

        // If orientation changed while predicting, commit the change
        if (newConfig.orientation != mOrientation) {
            commitTyped(getCurrentInputConnection());
            mOrientation = newConfig.orientation;
            
            mKeyboardSwitcher.makeKeyboards(true);
        }
        
		super.onConfigurationChanged(newConfig);
	}	
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	    if (DEBUG)Log.d("AnySoftKeyboard", "onSharedPreferenceChanged - key:" + key);
	    CloudBackupRequester.requestPrefsBackupToTheCloud();
	    
		boolean isKeyboardKey = key.startsWith(KeyboardAddOnAndBuilder.KEYBOARD_PREF_PREFIX);
		boolean isDictionaryKey = key.startsWith("dictionary_");
		boolean isQuickTextKey = key.equals(getString(R.string.settings_key_active_quick_text_key));
		if (isKeyboardKey || isDictionaryKey || isQuickTextKey) {
			mKeyboardSwitcher.makeKeyboards(true);
		} 
		
		loadSettings();

		if (	isDictionaryKey ||
				key.equals(getString(R.string.settings_key_use_contacts_dictionary)) ||
				key.equals(getString(R.string.settings_key_use_auto_dictionary)))
		{
		    setDictionariesForCurrentKeyboard();
		} 
		else  
		{
			//in some cases we do want to force keyboards recreations
			if (	key.equals(getString(R.string.settings_key_top_keyboard_row_id)) ||
					key.equals("zoom_factor_keys_in_portrait") ||
					key.equals("zoom_factor_keys_in_landscape") ||
					key.equals(getString(R.string.settings_key_smiley_icon_on_smileys_key)))
			{
				mKeyboardSwitcher.makeKeyboards(true);
			}
		}
	}

	public void appendCharactersToInput(CharSequence textToCommit) {
		if (DEBUG)
			Log.d(TAG, "appendCharactersToInput: "+ textToCommit);
		mWord.append(textToCommit);

		mComposing.append(textToCommit);
		
		if (mCompletionOn)
			getCurrentInputConnection().setComposingText(mWord.getTypedWord(), textToCommit.length());
		else
			commitTyped(getCurrentInputConnection());

		updateShiftKeyState(getCurrentInputEditorInfo());
	}

	public void deleteLastCharactersFromInput(int countToDelete) {
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
		Log.w(TAG, "The OS has reported that it is low on memory!. I'll try to clear some cache.");
		mKeyboardSwitcher.onLowMemory();
		//DictionaryFactory.getInstance().onLowMemory(mSuggest.getMainDictionary());
		super.onLowMemory();
	}
	
	private InputConnection mEditingInput = null;
	public void startInputConnectionEdit() {
		mEditingInput = getCurrentInputConnection();
		if (mEditingInput != null)
			mEditingInput.beginBatchEdit();
	}
	
	public void endInputConnectionEdit() {
		if (mEditingInput != null)
		{
			try
			{
				mEditingInput.endBatchEdit();
			}
			catch(Exception e)
			{
				//it could be dead already.
				e.printStackTrace();
			}
		}		
	}

	private void showQuickTextKeyPopupKeyboard(QuickTextKey quickTextKey) {
		if (mInputView != null) {
			if (quickTextKey.getPackageContext() == getApplicationContext()) {
				mInputView.simulateLongPress(AnyKeyboard.KEYCODE_QUICK_TEXT);
			} else {
				mInputView.showQuickTextPopupKeyboard(quickTextKey.getPackageContext(), quickTextKey);
			}
		}
	}
	
	private void showQuickTextKeyPopupList(final QuickTextKey key) {
        if (mQuickTextKeyDialog == null) {
            String[] names = key.getPopupListNames();
            final String[] texts = key.getPopupListValues();
			int[] icons = key.getPopupListIconResIds();

            final int N = names.length;

            List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
            for (int i = 0; i < N; i++) {
            	HashMap<String, Object> entry = new HashMap<String, Object>();

                entry.put("name", names[i]);
                entry.put("text", texts[i]);
				if (icons != null) entry.put("icons", icons[i]);

                entries.add(entry);
            }

			int layout;
			String[] from;
			int[] to;
			if (icons == null) {
				layout = R.layout.quick_text_key_menu_item_without_icon;
				from = new String[] {"name", "text"};
				to = new int[] {R.id.quick_text_name, R.id.quick_text_output};
			} else {
				layout = R.layout.quick_text_key_menu_item_with_icon;
				from = new String[] {"name", "text", "icons"};
				to = new int[]{R.id.quick_text_name, R.id.quick_text_output, R.id.quick_text_icon};
			}
            final SimpleAdapter a = new SimpleAdapter(this,	entries, layout, from, to);
            SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view instanceof ImageView) {
                        Drawable img = key.getPackageContext().getResources().getDrawable((Integer) data);
                        ((ImageView) view).setImageDrawable(img);
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

            mQuickTextKeyDialog = b.create();
            Window window = mQuickTextKeyDialog.getWindow();
    		WindowManager.LayoutParams lp = window.getAttributes();
    		lp.token = mInputView.getWindowToken();
    		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
    		window.setAttributes(lp);
    		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }

        mQuickTextKeyDialog.show();
    }

	public void promoteToUserDictionary(String word, int frequency) {
        if (mUserDictionary.isValidWord(word)) return;
        mUserDictionary.addWord(word, frequency);
    }

	public WordComposer getCurrentWord() {
        return mWord;
    }
	
	/**
     * Override this to control when the soft input area should be shown to
     * the user.  The default implementation only shows the input view when
     * there is no hard keyboard or the keyboard is hidden.  If you change what
     * this returns, you will need to call {@link #updateInputViewShown()}
     * yourself whenever the returned value may have changed to have it
     * re-evalauted and applied.
	 * This needs to be re-coded for Issue 620
     */
	@Override
    public boolean onEvaluateInputViewShown() {
        Configuration config = getResources().getConfiguration();
        return config.keyboard == Configuration.KEYBOARD_NOKEYS
                || config.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES;
    }
	
	public void onCancel() {
		//don't know what to do here.
	}
	
}

