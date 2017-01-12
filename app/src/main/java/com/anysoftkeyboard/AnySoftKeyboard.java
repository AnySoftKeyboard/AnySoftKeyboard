/*
 * Copyright (c) 2015 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.dictionaries.Dictionary;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.dictionaries.sqlite.AutoDictionary;
import com.anysoftkeyboard.ime.AnySoftKeyboardWithQuickText;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.AnyKeyboard.HardKeyboardTranslator;
import com.anysoftkeyboard.keyboards.CondenseType;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher.NextKeyboardType;
import com.anysoftkeyboard.keyboards.physical.HardKeyboardActionImpl;
import com.anysoftkeyboard.keyboards.physical.MyMetaKeyKeyListener;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.anysoftkeyboard.receivers.PackagesChangedReceiver;
import com.anysoftkeyboard.receivers.SoundPreferencesChangedReceiver;
import com.anysoftkeyboard.receivers.SoundPreferencesChangedReceiver.SoundPreferencesChangedListener;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.ui.VoiceInputNotInstalledActivity;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.utils.ChewbaccaOnTheDrums;
import com.anysoftkeyboard.utils.IMEUtil;
import com.anysoftkeyboard.utils.Logger;
import com.anysoftkeyboard.utils.ModifierKeyState;
import com.anysoftkeyboard.utils.Workarounds;
import com.google.android.voiceime.VoiceRecognitionTrigger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Input method implementation for QWERTY-ish keyboard.
 */
public abstract class AnySoftKeyboard extends AnySoftKeyboardWithQuickText implements SoundPreferencesChangedListener {

    private static final long ONE_FRAME_DELAY = 1000L / 60L;
    private static final long CLOSE_DICTIONARIES_DELAY = 5 * ONE_FRAME_DELAY;
    private static final ExtractedTextRequest EXTRACTED_TEXT_REQUEST = new ExtractedTextRequest();
    public static final String PREFS_KEY_POSTFIX_OVERRIDE_DICTIONARY = "_override_dictionary";

    private final ModifierKeyState mShiftKeyState = new ModifierKeyState(true/*supports locked state*/);
    private final ModifierKeyState mControlKeyState = new ModifierKeyState(false/*does not support locked state*/);
    private final HardKeyboardActionImpl mHardKeyboardAction = new HardKeyboardActionImpl();
    private final KeyboardUIStateHandler mKeyboardHandler = new KeyboardUIStateHandler(this);

    // receive ringer mode changes to detect silent mode
    private final SoundPreferencesChangedReceiver mSoundPreferencesChangedReceiver = new SoundPreferencesChangedReceiver(this);
    private final PackagesChangedReceiver mPackagesChangedReceiver = new PackagesChangedReceiver(this);
    protected IBinder mImeToken = null;

    @Nullable//this field is set at a undetermine point in service life-cycle
    /*package*/ TextView mCandidateCloseText;
    private View mCandidatesParent;
    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;
    private long mMetaState;
    @NonNull
    private final SparseBooleanArray mSentenceSeparators = new SparseBooleanArray();

    private AutoDictionary mAutoDictionary;
    private WordComposer mWord = new WordComposer();

    private static final long MAX_TIME_TO_EXPECT_SELECTION_UPDATE = 1500;
    private long mExpectingSelectionUpdateBy = Long.MIN_VALUE;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    @NonNull
    private CharSequence mCommittedWord = "";
    private int mGlobalCursorPosition = 0;
    private int mGlobalSelectionStartPosition = 0;

    private int mLastEditorIdPhysicalKeyboardWasUsed = 0;
    /*
     * Do we do prediction now
     */
    //private boolean mPredicting;
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
    private boolean mAllowSuggestionsRestart = true;
    private boolean mCurrentlyAllowSuggestionRestart = true;
    private boolean mJustAutoAddedWord = false;
    private boolean mAutoCap;
    private boolean mQuickFixes;
    /*
     * Configuration flag. Should we support dictionary suggestions
     */
    private boolean mShowSuggestions = false;
    private boolean mAutoComplete;

    private boolean mShowKeyboardIconInStatusBar;

    private static final int UNDO_COMMIT_NONE = -1;
    private static final int UNDO_COMMIT_WAITING_TO_RECORD_POSITION = -2;
    /*
     * This will help us find out if UNDO_COMMIT is still possible to be done
     */
    private int mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
    private AudioManager mAudioManager;
    private boolean mSilentMode;
    private boolean mSoundOn;
    // between 0..100. This is the custom volume
    private int mSoundVolume;
    private Vibrator mVibrator;
    private int mVibrationDuration;
    private CondenseType mKeyboardInCondensedMode = CondenseType.None;
    private boolean mJustAddedAutoSpace;
    private boolean mLastCharacterWasShifted = false;
    private InputMethodManager mInputMethodManager;
    private VoiceRecognitionTrigger mVoiceRecognitionTrigger;
    //a year ago.
    private static final long NEVER_TIME_STAMP = (-1L) * (365L * 24L * 60L * 60L * 1000L);
    private long mLastSpaceTimeStamp = NEVER_TIME_STAMP;

    public AnySoftKeyboard() {
        super();
    }

    //TODO SHOULD NOT USE THIS METHOD AT ALL!
    private static int getCursorPosition(@Nullable InputConnection connection) {
        if (connection == null)
            return 0;
        ExtractedText extracted = connection.getExtractedText(EXTRACTED_TEXT_REQUEST, 0);
        if (extracted == null)
            return 0;
        return extracted.startOffset + extracted.selectionStart;
    }

    private static boolean isBackWordStopChar(int c) {
        return !Character.isLetter(c);
    }

    private static String getDictionaryOverrideKey(AnyKeyboard currentKeyboard) {
        return currentKeyboard.getKeyboardPrefId() + PREFS_KEY_POSTFIX_OVERRIDE_DICTIONARY;
    }

    @Override
    @NonNull
    public AbstractInputMethodImpl onCreateInputMethodInterface() {
        return new InputMethodImpl() {
            @Override
            public void attachToken(IBinder token) {
                super.attachToken(token);
                mImeToken = token;
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mOrientation = getResources().getConfiguration().orientation;
        if ((!BuildConfig.DEBUG) && DeveloperUtils.hasTracingRequested(getApplicationContext())) {
            try {
                DeveloperUtils.startTracing();
                Toast.makeText(getApplicationContext(),
                        R.string.debug_tracing_starting, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                //see issue https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/105
                //I might get a "Permission denied" error.
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        R.string.debug_tracing_starting_failed, Toast.LENGTH_LONG).show();
            }
        }
        Logger.i(TAG, "****** AnySoftKeyboard v%s (%d) service started.", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        if (!BuildConfig.DEBUG && BuildConfig.VERSION_NAME.endsWith("-SNAPSHOT"))
            throw new RuntimeException("You can not run a 'RELEASE' build with a SNAPSHOT postfix!");

        if (mAskPrefs.getAnimationsLevel() != AskPrefs.AnimationsLevel.None) {
            final int fancyAnimation = getResources().getIdentifier("Animation_InputMethodFancy", "style", "android");
            if (fancyAnimation != 0) {
                Logger.i(TAG, "Found Animation_InputMethodFancy as %d, so I'll use this", fancyAnimation);
                getWindow().getWindow().setWindowAnimations(fancyAnimation);
            } else {
                Logger.w(TAG, "Could not find Animation_InputMethodFancy, using default animation");
                getWindow().getWindow().setWindowAnimations(android.R.style.Animation_InputMethod);
            }
        }

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        updateRingerMode();
        // register to receive ringer mode changes for silent mode
        registerReceiver(mSoundPreferencesChangedReceiver, mSoundPreferencesChangedReceiver.createFilterToRegisterOn());
        // register to receive packages changes
        registerReceiver(mPackagesChangedReceiver, mPackagesChangedReceiver.createFilterToRegisterOn());
        mVibrator = ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE));

        onLoadSettingsRequired(PreferenceManager.getDefaultSharedPreferences(this));
        mAskPrefs.addChangedListener(this);

        mVoiceRecognitionTrigger = new VoiceRecognitionTrigger(this);
    }

    @NonNull
    protected KeyboardSwitcher createKeyboardSwitcher() {
        return new KeyboardSwitcher(this, getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Logger.i(TAG, "AnySoftKeyboard has been destroyed! Cleaning resources..");
        mKeyboardHandler.removeAllMessages();
        mAskPrefs.removeChangedListener(this);

        unregisterReceiver(mSoundPreferencesChangedReceiver);
        unregisterReceiver(mPackagesChangedReceiver);

        mInputMethodManager.hideStatusIcon(mImeToken);

        hideWindow();

        closeDictionaries();

        if (DeveloperUtils.hasTracingStarted()) {
            DeveloperUtils.stopTracing();
            Toast.makeText(
                    getApplicationContext(),
                    getString(R.string.debug_tracing_finished,
                            DeveloperUtils.getTraceFile()), Toast.LENGTH_SHORT)
                    .show();
        }

        super.onDestroy();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        // Remove pending messages related to update suggestions
        abortCorrectionAndResetPredictionState(false);
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateCandidatesView() {
        return getLayoutInflater().inflate(R.layout.candidates, null);
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        //removing close request (if it was asked for a previous onFinishInput).
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_CLOSE_DICTIONARIES);

        abortCorrectionAndResetPredictionState(false);

        if (!restarting) {
            TextEntryState.newSession(mPredictionOn);
            // Clear shift states.
            mMetaState = 0;
            mCurrentlyAllowSuggestionRestart = mAllowSuggestionsRestart;
        } else {
            // something very fishy happening here...
            // this is the only way I can get around it.
            // it seems that when a onStartInput is called with restarting ==
            // true
            // suggestions restart fails :(
            // see Browser when editing multiline textbox
            mCurrentlyAllowSuggestionRestart = false;
        }

        setKeyboardStatusIcon();
    }

    @Override
    public boolean onShowInputRequested(int flags, boolean configChange) {
        final EditorInfo editorInfo = getCurrentInputEditorInfo();
        //in case the user has used physical keyboard with this input-field,
        //we will not show the keyboard view (until completely finishing, or switching input fields)
        final boolean previouslyPhysicalKeyboardInput;
        if ((!configChange) && editorInfo != null && editorInfo.fieldId == mLastEditorIdPhysicalKeyboardWasUsed && editorInfo.fieldId != 0) {
            Logger.d(TAG, "Already used physical keyboard on this input-field. Will not show keyboard view.");
            previouslyPhysicalKeyboardInput = true;
        } else {
            previouslyPhysicalKeyboardInput = false;
            mLastEditorIdPhysicalKeyboardWasUsed = 0;
        }
        return (!previouslyPhysicalKeyboardInput) && super.onShowInputRequested(flags, configChange);
    }

    @Override
    public void onStartInputView(final EditorInfo attribute, final boolean restarting) {
        Logger.v(TAG, "onStartInputView(EditorInfo{imeOptions %d, inputType %d}, restarting %s",
                attribute.imeOptions, attribute.inputType, restarting);

        super.onStartInputView(attribute, restarting);

        if (mVoiceRecognitionTrigger != null) {
            mVoiceRecognitionTrigger.onStartInputView();
        }

        if (getInputView() == null) {
            return;
        }

        getInputView().dismissPopupKeyboard();
        getInputView().setKeyboardActionType(attribute.imeOptions);

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
            case EditorInfo.TYPE_CLASS_DATETIME:
                Logger.d(TAG, "Setting INPUT_MODE_DATETIME as keyboard due to a TYPE_CLASS_DATETIME input.");
                getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_DATETIME, attribute, restarting);
                break;
            case EditorInfo.TYPE_CLASS_NUMBER:
                Logger.d(TAG, "Setting INPUT_MODE_NUMBERS as keyboard due to a TYPE_CLASS_NUMBER input.");
                getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_NUMBERS, attribute, restarting);
                break;
            case EditorInfo.TYPE_CLASS_PHONE:
                Logger.d(TAG, "Setting INPUT_MODE_PHONE as keyboard due to a TYPE_CLASS_PHONE input.");
                getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_PHONE, attribute, restarting);
                break;
            case EditorInfo.TYPE_CLASS_TEXT:
                Logger.d(TAG, "A TYPE_CLASS_TEXT input.");
                final int variation = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
                switch (variation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                    case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                        Logger.d(TAG, "A password TYPE_CLASS_TEXT input with no prediction");
                        mPredictionOn = false;
                        break;
                    default:
                        mPredictionOn = true;
                }

                if (mAskPrefs.getInsertSpaceAfterCandidatePick()) {
                    switch (variation) {
                        case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                        case EditorInfo.TYPE_TEXT_VARIATION_URI:
                        case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                            mAutoSpace = false;
                            break;
                        default:
                            mAutoSpace = true;
                    }
                } else {
                    // some users don't want auto-space
                    mAutoSpace = false;
                }

                final int textFlag = attribute.inputType & EditorInfo.TYPE_MASK_FLAGS;
                if ((textFlag & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS ||
                        (textFlag & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) == EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) {
                    Logger.d(TAG, "Input requested NO_SUGGESTIONS, or it is AUTO_COMPLETE by itself.");
                    mPredictionOn = false;
                }

                switch (variation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        Logger.d(TAG, "Setting INPUT_MODE_EMAIL as keyboard due to a TYPE_TEXT_VARIATION_EMAIL_ADDRESS input.");
                        getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_EMAIL, attribute, restarting);
                        mPredictionOn = false;
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_URI:
                        Logger.d(TAG, "Setting INPUT_MODE_URL as keyboard due to a TYPE_TEXT_VARIATION_URI input.");
                        getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_URL, attribute, restarting);
                        mPredictionOn = false;
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
                        Logger.d(TAG, "Setting INPUT_MODE_IM as keyboard due to a TYPE_TEXT_VARIATION_SHORT_MESSAGE input.");
                        getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_IM, attribute, restarting);
                        break;
                    default:
                        Logger.d(TAG, "Setting INPUT_MODE_TEXT as keyboard due to a default input.");
                        getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, attribute, restarting);
                }

                break;
            default:
                Logger.d(TAG, "Setting INPUT_MODE_TEXT as keyboard due to a default input.");
                // No class. Probably a console window, or no GUI input connection
                mPredictionOn = false;
                mAutoSpace = true;
                getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, attribute, restarting);
        }

        mJustAddedAutoSpace = false;
        setCandidatesViewShown(false);

        mPredictionOn = mPredictionOn && (mShowSuggestions/* || mQuickFixes */);
        TextEntryState.newSession(mPredictionOn);

        clearSuggestions();

        updateShiftStateNow();
    }

    @Override
    public void hideWindow() {
        super.hideWindow();

        TextEntryState.restartSession();
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        //properly finished input. Next time we DO want to show the keyboard view
        mLastEditorIdPhysicalKeyboardWasUsed = 0;

        hideWindow();

        if (mShowKeyboardIconInStatusBar) {
            mInputMethodManager.hideStatusIcon(mImeToken);
        }
        mKeyboardHandler.sendEmptyMessageDelayed(KeyboardUIStateHandler.MSG_CLOSE_DICTIONARIES, CLOSE_DICTIONARIES_DELAY);
    }

    /*
     * this function is called EVERY TIME them selection is changed. This also
     * includes the underlined suggestions.
     */
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

        if (BuildConfig.DEBUG)
            Logger.d(TAG, "onUpdateSelection: oss=%d, ose=%d, nss=%d, nse=%d, cs=%d, ce=%d",
                    oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

        mGlobalCursorPosition = newSelEnd;
        mGlobalSelectionStartPosition = newSelStart;
        if (mUndoCommitCursorPosition == UNDO_COMMIT_WAITING_TO_RECORD_POSITION) {
            Logger.d(TAG, "onUpdateSelection: I am in ACCEPTED_DEFAULT state, time to store the position - I can only undo-commit from here.");
            mUndoCommitCursorPosition = newSelStart;
        }
        updateShiftStateNow();

        final boolean isExpectedEvent = SystemClock.uptimeMillis() < mExpectingSelectionUpdateBy;
        mExpectingSelectionUpdateBy = NEVER_TIME_STAMP;

        if (isExpectedEvent) {
            Logger.v(TAG, "onUpdateSelection: Expected event. Discarding.");
            return;
        }

        if (!isPredictionOn()) {
            return;// not relevant if no prediction is needed.
        }

        final InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;// well, I can't do anything without this connection

        Logger.d(TAG, "onUpdateSelection: ok, let's see what can be done");

        if (newSelStart != newSelEnd) {
            // text selection. can't predict in this mode
            Logger.d(TAG, "onUpdateSelection: text selection.");
            abortCorrectionAndResetPredictionState(false);
        } else {
            // we have the following options (we are in an input which requires
            // predicting (mPredictionOn == true):
            // 1) predicting and moved inside the word
            // 2) predicting and moved outside the word
            // 2.1) to a new word
            // 2.2) to no word land
            // 3) not predicting
            // 3.1) to a new word
            // 3.2) to no word land

            // so, 1 and 2 requires that predicting is currently done, and the
            // cursor moved
            if (TextEntryState.isPredicting()) {
                if (newSelStart >= candidatesStart && newSelStart <= candidatesEnd) {
                    // 1) predicting and moved inside the word - just update the
                    // cursor position and shift state
                    // inside the currently selected word
                    int cursorPosition = newSelEnd - candidatesStart;
                    if (mWord.setCursorPosition(cursorPosition)) {
                        Logger.d(TAG, "onUpdateSelection: cursor moving inside the predicting word");
                    }
                } else {
                    Logger.d(TAG, "onUpdateSelection: cursor moving outside the currently predicting word");
                    abortCorrectionAndResetPredictionState(false);
                    // ask user whether to restart
                    postRestartWordSuggestion();
                }
            } else {
                Logger.d(TAG, "onUpdateSelection: not predicting at this moment, maybe the cursor is now at a new word?");
                if (TextEntryState.willUndoCommitOnBackspace()) {
                    if (mUndoCommitCursorPosition == oldSelStart && mUndoCommitCursorPosition != newSelStart) {
                        Logger.d(TAG, "onUpdateSelection: I am in a state that is position sensitive but the user moved the cursor, so it is not possible to undo_commit now.");
                        abortCorrectionAndResetPredictionState(false);
                    }
                }
                postRestartWordSuggestion();
            }
        }
    }

    private void postRestartWordSuggestion() {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS);
        mKeyboardHandler.sendEmptyMessageDelayed(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS, 10 * ONE_FRAME_DELAY);
    }

    private boolean canRestartWordSuggestion() {
        if (TextEntryState.isPredicting() || !isPredictionOn() || !mAllowSuggestionsRestart
                || !mCurrentlyAllowSuggestionRestart || getInputView() == null
                || !getInputView().isShown()) {
            // why?
            // mPredicting - if I'm predicting a word, I can not restart it..
            // right? I'm inside that word!
            // isPredictionOn() - this is obvious.
            // mAllowSuggestionsRestart - config settings
            // mCurrentlyAllowSuggestionRestart - workaround for
            // onInputStart(restarting == true)
            // mInputView == null - obvious, no?
            Logger.d(TAG, "performRestartWordSuggestion: no need to restart: mPredicting=%s, isPredictionOn=%s, mAllowSuggestionsRestart=%s, mCurrentlyAllowSuggestionRestart=%s"
                    , TextEntryState.isPredicting(), isPredictionOn(), mAllowSuggestionsRestart, mCurrentlyAllowSuggestionRestart);
            return false;
        } else if (!isCursorTouchingWord()) {
            Logger.d(TAG, "User moved cursor to no-man land. Bye bye.");
            return false;
        }

        return true;
    }

    public void performRestartWordSuggestion(final InputConnection ic) {
        // I assume ASK DOES NOT predict at this moment!

        // 2) predicting and moved outside the word - abort predicting, update
        // shift state
        // 2.1) to a new word - restart predicting on the new word
        // 2.2) to no word land - nothing else

        // this means that the new cursor position is outside the candidates
        // underline
        // this can be either because the cursor is really outside the
        // previously underlined (suggested)
        // or nothing was suggested.
        // in this case, we would like to reset the prediction and restart
        // if the user clicked inside a different word
        // restart required?
        if (canRestartWordSuggestion()) {// 2.1
            ic.beginBatchEdit();// don't want any events till I finish handling
            // this touch
            abortCorrectionAndResetPredictionState(false);

            // locating the word
            CharSequence toLeft = "";
            CharSequence toRight = "";
            while (true) {
                CharSequence newToLeft = ic.getTextBeforeCursor(toLeft.length() + 1, 0);
                if (TextUtils.isEmpty(newToLeft)
                        || isWordSeparator(newToLeft.charAt(0))
                        || newToLeft.length() == toLeft.length()) {
                    break;
                }
                toLeft = newToLeft;
            }
            while (true) {
                CharSequence newToRight = ic.getTextAfterCursor(toRight.length() + 1, 0);
                if (TextUtils.isEmpty(newToRight)
                        || isWordSeparator(newToRight.charAt(newToRight.length() - 1))
                        || newToRight.length() == toRight.length()) {
                    break;
                }
                toRight = newToRight;
            }
            CharSequence word = toLeft.toString() + toRight.toString();
            Logger.d(TAG, "Starting new prediction on word '%s'.", word);
            mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
            mWord.reset();

            final int[] tempNearByKeys = new int[1];

            for (int index = 0; index < word.length(); index++) {
                final char c = word.charAt(index);
                if (index == 0) mWord.setFirstCharCapitalized(Character.isUpperCase(c));

                tempNearByKeys[0] = c;
                mWord.add(c, tempNearByKeys);

                TextEntryState.typedCharacter(c, false);
            }
            ic.deleteSurroundingText(toLeft.length(), toRight.length());
            ic.setComposingText(word, 1);
            // repositioning the cursor
            if (toRight.length() > 0) {
                final int cursorPosition = getCursorPosition(ic) - toRight.length();
                Logger.d(TAG, "Repositioning the cursor inside the word to position %d", cursorPosition);
                ic.setSelection(cursorPosition, cursorPosition);
            }

            mWord.setCursorPosition(toLeft.length());
            ic.endBatchEdit();
            postUpdateSuggestions();
        } else {
            Logger.d(TAG, "performRestartWordSuggestion canRestartWordSuggestion == false");
        }
    }

    private void onPhysicalKeyboardKeyPressed() {
        EditorInfo editorInfo = getCurrentInputEditorInfo();
        mLastEditorIdPhysicalKeyboardWasUsed = editorInfo == null ? 0 : editorInfo.fieldId;
        if (mAskPrefs.hideSoftKeyboardWhenPhysicalKeyPressed()) {
            hideWindow();
        }

        // For all other keys, if we want to do transformations on
        // text being entered with a hard keyboard, we need to process
        // it and do the appropriate action.
        // using physical keyboard is more annoying with candidate view in
        // the way
        // so we disable it.

        // stopping any soft-keyboard prediction
        abortCorrectionAndResetPredictionState(false);
    }

    @Override
    public void onComputeInsets(@NonNull InputMethodService.Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (!isFullscreenMode()) {
            outInsets.contentTopInsets = outInsets.visibleTopInsets;
        }
    }

    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        if (BuildConfig.DEBUG) {
            Logger.d(TAG, "Received completions:");
            for (int i = 0; i < (completions != null ? completions.length : 0); i++) {
                Logger.d(TAG, "  #" + i + ": " + completions[i]);
            }
        }

        // completions should be shown if dictionary requires, or if we are in
        // full-screen and have outside completions
        if (mCompletionOn || (isFullscreenMode() && (completions != null))) {
            mCompletions = completions;
            // we do completions :)

            mCompletionOn = true;
            if (completions == null) {
                clearSuggestions();
                return;
            }

            List<CharSequence> stringList = new ArrayList<>();
            for (CompletionInfo ci : completions) {
                if (ci != null) stringList.add(ci.getText());
            }
            // CharSequence typedWord = mWord.getTypedWord();
            setSuggestions(stringList, true, true, true);
            mWord.setPreferredWord(null);
            // I mean, if I'm here, it must be shown...
            setCandidatesViewShown(true);
        }
    }

    @Override
    public void setCandidatesViewShown(boolean shown) {
        // we show predication only in on-screen keyboard
        // (onEvaluateInputViewShown)
        // or if the physical keyboard supports candidates
        // (mPredictionLandscape)
        final boolean shouldShow = shouldCandidatesStripBeShown() && shown;
        final boolean currentlyShown = mCandidatesParent != null && mCandidatesParent.getVisibility() == View.VISIBLE;
        super.setCandidatesViewShown(shouldShow);
        if (shouldShow != currentlyShown) {
            // I believe (can't confirm it) that candidates animation is kinda rare,
            // and it is better to load it on demand, then to keep it in memory always..
            if (shouldShow) {
                mCandidatesParent.setAnimation(AnimationUtils.loadAnimation(this, R.anim.candidates_bottom_to_up_enter));
            } else {
                mCandidatesParent.setAnimation(AnimationUtils.loadAnimation(this, R.anim.candidates_up_to_bottom_exit));
            }
        }
    }

    @Override
    public void setCandidatesView(@NonNull View view) {
        super.setCandidatesView(view);
        mCandidatesParent = view.getParent() instanceof View ? (View) view.getParent() : null;

        mCandidateView = (CandidateView) view.findViewById(R.id.candidates);
        mCandidateView.setService(this);
        setCandidatesViewShown(false);

        final KeyboardTheme theme = KeyboardThemeFactory.getCurrentKeyboardTheme(getApplicationContext());
        final TypedArray a = theme.getPackageContext().obtainStyledAttributes(null, R.styleable.AnyKeyboardViewTheme, 0, theme.getThemeResId());
        int closeTextColor = ContextCompat.getColor(this, R.color.candidate_other);
        float fontSizePixel = getResources().getDimensionPixelSize(R.dimen.candidate_font_height);
        Drawable suggestionCloseDrawable = null;
        try {
            closeTextColor = a.getColor(R.styleable.AnyKeyboardViewTheme_suggestionOthersTextColor, closeTextColor);
            fontSizePixel = a.getDimension(R.styleable.AnyKeyboardViewTheme_suggestionTextSize, fontSizePixel);
            suggestionCloseDrawable = a.getDrawable(R.styleable.AnyKeyboardViewTheme_suggestionCloseImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        a.recycle();

        mCandidateCloseText = (TextView) view.findViewById(R.id.close_suggestions_strip_text);
        ImageView closeIcon = (ImageView) view.findViewById(R.id.close_suggestions_strip_icon);
        if (suggestionCloseDrawable != null) closeIcon.setImageDrawable(suggestionCloseDrawable);

        closeIcon.setOnClickListener(new OnClickListener() {
            // two seconds is enough.
            private final static long DOUBLE_TAP_TIMEOUT = 2 * 1000 - 50;

            public void onClick(View v) {
                mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT);
                mCandidateCloseText.setVisibility(View.VISIBLE);
                mCandidateCloseText.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.close_candidates_hint_in));
                mKeyboardHandler.sendMessageDelayed(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT), DOUBLE_TAP_TIMEOUT);
            }
        });

        mCandidateCloseText.setTextColor(closeTextColor);
        mCandidateCloseText.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSizePixel);
        mCandidateCloseText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT);
                mCandidateCloseText.setVisibility(View.GONE);
                abortCorrectionAndResetPredictionState(true);
            }
        });
    }

    private void clearSuggestions() {
        setSuggestions(null, false, false, false);
    }

    private void setSuggestions(List<CharSequence> suggestions,
                                boolean completions, boolean typedWordValid,
                                boolean haveMinimalSuggestion) {
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions,
                    typedWordValid, haveMinimalSuggestion && mAutoCorrectOn);
        }
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        if (getCurrentInputEditorInfo() != null) {
            final EditorInfo editorInfo = getCurrentInputEditorInfo();
            if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_FULLSCREEN) != 0) {
                //if the view DOES NOT want fullscreen, then do what it wants
                Logger.d(TAG, "Will not go to Fullscreen because input view requested IME_FLAG_NO_FULLSCREEN");
                return false;
            } else if ((editorInfo.imeOptions & EditorInfo.IME_FLAG_NO_EXTRACT_UI) != 0) {
                Logger.d(TAG, "Will not go to Fullscreen because input view requested IME_FLAG_NO_EXTRACT_UI");
                return false;

            }
        }

        switch (mOrientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return mAskPrefs.getUseFullScreenInputInLandscape();
            default:
                return mAskPrefs.getUseFullScreenInputInPortrait();
        }
    }

    @Override
    public boolean onKeyDown(final int keyEventKeyCode, @NonNull KeyEvent event) {
        InputConnection ic = getCurrentInputConnection();
        if (handleSelectionExpending(keyEventKeyCode, ic, mGlobalSelectionStartPosition, mGlobalCursorPosition))
            return true;
        final boolean shouldTranslateSpecialKeys = isInputViewShown();

        //greater than zero means it is a physical keyboard.
        //we also want to hide the view if it's a glyph (for example, not physical volume-up key)
        if (event.getDeviceId() > 0 && event.isPrintingKey()) onPhysicalKeyboardKeyPressed();

        mHardKeyboardAction.initializeAction(event, mMetaState);

        switch (keyEventKeyCode) {
            /****
             * SPECIAL translated HW keys If you add new keys here, do not forget
             * to add to the
             */
            case KeyEvent.KEYCODE_CAMERA:
                if (shouldTranslateSpecialKeys
                        && mAskPrefs.useCameraKeyForBackspaceBackword()) {
                    handleBackWord(getCurrentInputConnection());
                    return true;
                }
                // DO NOT DELAY CAMERA KEY with unneeded checks in default mark
                return super.onKeyDown(keyEventKeyCode, event);
            case KeyEvent.KEYCODE_FOCUS:
                if (shouldTranslateSpecialKeys
                        && mAskPrefs.useCameraKeyForBackspaceBackword()) {
                    handleDeleteLastCharacter(false);
                    return true;
                }
                // DO NOT DELAY FOCUS KEY with unneeded checks in default mark
                return super.onKeyDown(keyEventKeyCode, event);
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (shouldTranslateSpecialKeys
                        && mAskPrefs.useVolumeKeyForLeftRight()) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
                    return true;
                }
                // DO NOT DELAY VOLUME UP KEY with unneeded checks in default
                // mark
                return super.onKeyDown(keyEventKeyCode, event);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (shouldTranslateSpecialKeys
                        && mAskPrefs.useVolumeKeyForLeftRight()) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
                    return true;
                }
                // DO NOT DELAY VOLUME DOWN KEY with unneeded checks in default
                // mark
                return super.onKeyDown(keyEventKeyCode, event);
            /****
             * END of SPECIAL translated HW keys code section
             */
            case KeyEvent.KEYCODE_BACK:
                if (event.getRepeatCount() == 0 && getInputView() != null) {
                    if (getInputView().handleBack()) {
                        // consuming the meta keys
                        if (ic != null) {
                            // translated, so we also take care of the metakeys
                            ic.clearMetaKeyStates(Integer.MAX_VALUE);
                        }
                        mMetaState = 0;
                        return true;
                    }
                }
                break;
            case 0x000000cc:// API 14: KeyEvent.KEYCODE_LANGUAGE_SWITCH
                switchToNextPhysicalKeyboard(ic);
                return true;
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (event.isAltPressed()
                        && Workarounds.isAltSpaceLangSwitchNotPossible()) {
                    switchToNextPhysicalKeyboard(ic);
                    return true;
                }
                // NOTE: letting it fall-through to the other meta-keys
            case KeyEvent.KEYCODE_ALT_LEFT:
            case KeyEvent.KEYCODE_ALT_RIGHT:
            case KeyEvent.KEYCODE_SYM:
                Logger.d(TAG + "-meta-key",
                        getMetaKeysStates("onKeyDown before handle"));
                mMetaState = MyMetaKeyKeyListener.handleKeyDown(mMetaState,
                        keyEventKeyCode, event);
                Logger.d(TAG + "-meta-key",
                        getMetaKeysStates("onKeyDown after handle"));
                break;
            case KeyEvent.KEYCODE_SPACE:
                if ((event.isAltPressed() && !Workarounds
                        .isAltSpaceLangSwitchNotPossible())
                        || event.isShiftPressed()) {
                    switchToNextPhysicalKeyboard(ic);
                    return true;
                }
                // NOTE:
                // letting it fall through to the "default"
            default:

                // Fix issue 185, check if we should process key repeat
                if (!mAskPrefs.getUseRepeatingKeys() && event.getRepeatCount() > 0)
                    return true;

                HardKeyboardTranslator keyTranslator = (HardKeyboardTranslator) getCurrentAlphabetKeyboard();
                if (getKeyboardSwitcher().isCurrentKeyboardPhysical() && keyTranslator != null) {
                    // sometimes, the physical keyboard will delete input, and then add some.
                    // we'll try to make it nice.
                    if (ic != null)
                        ic.beginBatchEdit();
                    try {
                        // issue 393, backword on the hw keyboard!
                        if (mAskPrefs.useBackword()
                                && keyEventKeyCode == KeyEvent.KEYCODE_DEL
                                && event.isShiftPressed()) {
                            handleBackWord(ic);
                            return true;
                        } else {
                            // http://article.gmane.org/gmane.comp.handhelds.openmoko.android-freerunner/629
                            keyTranslator.translatePhysicalCharacter(mHardKeyboardAction, this);

                            if (mHardKeyboardAction.getKeyCodeWasChanged()) {
                                final int translatedChar = mHardKeyboardAction.getKeyCode();
                                // typing my own.
                                onKey(translatedChar, null, -1, new int[]{translatedChar}, true/*faking from UI*/);
                                // my handling we are at a regular key press, so we'll update
                                // our meta-state member
                                mMetaState = MyMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
                                Logger.d(TAG + "-meta-key", getMetaKeysStates("onKeyDown after adjust - translated"));
                                return true;
                            }
                        }
                    } finally {
                        if (ic != null)
                            ic.endBatchEdit();
                    }
                }
                if (event.isPrintingKey()) {
                    // we are at a regular key press, so we'll update our
                    // meta-state
                    // member
                    mMetaState = MyMetaKeyKeyListener.adjustMetaAfterKeypress(mMetaState);
                    Logger.d(TAG + "-meta-key", getMetaKeysStates("onKeyDown after adjust"));
                }
        }
        return super.onKeyDown(keyEventKeyCode, event);
    }

    private void switchToNextPhysicalKeyboard(InputConnection ic) {
        // consuming the meta keys
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
    }

    private void setKeyboardStatusIcon() {
        AnyKeyboard alphabetKeyboard = getCurrentAlphabetKeyboard();
        if (mShowKeyboardIconInStatusBar && alphabetKeyboard != null) {
            mInputMethodManager.showStatusIcon(mImeToken,
                    alphabetKeyboard.getKeyboardContext().getPackageName(),
                    alphabetKeyboard.getKeyboardIconResId());
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        Logger.d(TAG, "onKeyUp keycode=%d", keyCode);
        switch (keyCode) {
            // Issue 248
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (!isInputViewShown()) {
                    return super.onKeyUp(keyCode, event);
                }
                if (mAskPrefs.useVolumeKeyForLeftRight()) {
                    // no need of vol up/down sound
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (getInputView() != null && getInputView().isShown()
                        && getInputView().isShifted()) {
                    event = new KeyEvent(event.getDownTime(), event.getEventTime(),
                            event.getAction(), event.getKeyCode(),
                            event.getRepeatCount(), event.getDeviceId(),
                            event.getScanCode(), KeyEvent.META_SHIFT_LEFT_ON
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
                mMetaState = MyMetaKeyKeyListener.handleKeyUp(mMetaState, keyCode, event);
                Logger.d(TAG + "-meta-key", getMetaKeysStates("onKeyUp"));
                setInputConnectionMetaStateAsCurrentMetaKeyKeyListenerState();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    private String getMetaKeysStates(String place) {
        final int shiftState = MyMetaKeyKeyListener.getMetaState(mMetaState, MyMetaKeyKeyListener.META_SHIFT_ON);
        final int altState = MyMetaKeyKeyListener.getMetaState(mMetaState, MyMetaKeyKeyListener.META_ALT_ON);
        final int symState = MyMetaKeyKeyListener.getMetaState(mMetaState, MyMetaKeyKeyListener.META_SYM_ON);

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
            ic.clearMetaKeyStates(clearStatesFlags);
        }
    }

    private boolean checkAddToDictionaryWithAutoDictionary(WordComposer suggestion, AutoDictionary.AdditionType type) {
        if (suggestion == null || suggestion.length() < 1)
            return false;
        // Only auto-add to dictionary if auto-correct is ON. Otherwise we'll be
        // adding words in situations where the user or application really
        // didn't
        // want corrections enabled or learned.
        if (!mQuickFixes && !mShowSuggestions)
            return false;

        if (mAutoDictionary != null) {
            String suggestionToCheck = suggestion.getTypedWord().toString();
            if (!mSuggest.isValidWord(suggestionToCheck)) {

                final boolean added = mAutoDictionary.addWord(suggestion, type, this);
                if (added && mCandidateView != null) {
                    mCandidateView.notifyAboutWordAdded(suggestion.getTypedWord());
                }
                return added;
            }
        }
        return false;
    }

    private void removeTrailingSpace() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;

        CharSequence lastOne = ic.getTextBeforeCursor(1, 0);
        if (lastOne != null && lastOne.length() == 1
                && lastOne.charAt(0) == KeyCodes.SPACE) {
            ic.deleteSurroundingText(1, 0);
        }
    }

    public boolean addWordToDictionary(String word) {
        boolean added = mSuggest.addWordToUserDictionary(word);
        if (added && mCandidateView != null)
            mCandidateView.notifyAboutWordAdded(word);
        return added;
    }

    public void removeFromUserDictionary(String word) {
        mJustAutoAddedWord = false;
        mSuggest.removeWordFromUserDictionary(word);
        abortCorrectionAndResetPredictionState(false);
        if (mCandidateView != null)
            mCandidateView.notifyAboutRemovedWord(word);
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    @Override
    protected boolean isAlphabet(int code) {
        if (super.isAlphabet(code)) return true;
        // inner letters have more options: ' in English. " in Hebrew, and more.
        if (TextEntryState.isPredicting())
            return getCurrentAlphabetKeyboard().isInnerWordLetter((char) code);
        else
            return getCurrentAlphabetKeyboard().isStartOfWordLetter((char) code);
    }

    @Override
    protected boolean isSuggestionAffectingCharacter(int code) {
        return super.isSuggestionAffectingCharacter(code) || Character.isLetter((char) code);
    }

    public void onMultiTapStarted() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            ic.beginBatchEdit();
        handleDeleteLastCharacter(true);
        if (getInputView() != null)
            getInputView().setShifted(mLastCharacterWasShifted);
    }

    public void onMultiTapEnded() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic != null)
            ic.endBatchEdit();
        updateShiftStateNow();
    }

    private void onFunctionKey(final int primaryCode, final Key key, final int multiTapIndex, final int[] nearByKeyCodes, final boolean fromUI) {
        if (BuildConfig.DEBUG) Logger.d(TAG, "onFunctionKey %d", primaryCode);

        final InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case KeyCodes.DELETE:
                if (ic == null)// if we don't want to do anything, lets check null first.
                    break;
                // we do backword if the shift is pressed while pressing
                // backspace (like in a PC)
                if (mAskPrefs.useBackword() && mShiftKeyState.isPressed() && !mShiftKeyState.isLocked()) {
                    handleBackWord(ic);
                } else {
                    handleDeleteLastCharacter(false);
                }
                break;
            case KeyCodes.SHIFT:
                if (fromUI) {
                    handleShift();
                } else {
                    //not from UI (user not actually pressed that button)
                    onPress(primaryCode);
                    onRelease(primaryCode);
                }
                break;
            case KeyCodes.SHIFT_LOCK:
                mShiftKeyState.toggleLocked();
                handleShift();
                break;
            case KeyCodes.DELETE_WORD:
                if (ic == null)// if we don't want to do anything, lets check
                    // null first.
                    break;
                handleBackWord(ic);
                break;
            case KeyCodes.CLEAR_INPUT:
                if (ic != null) {
                    ic.beginBatchEdit();
                    abortCorrectionAndResetPredictionState(false);
                    ic.deleteSurroundingText(Integer.MAX_VALUE, Integer.MAX_VALUE);
                    ic.endBatchEdit();
                }
                break;
            case KeyCodes.CTRL:
                if (fromUI) {
                    handleControl();
                } else {
                    //not from UI (user not actually pressed that button)
                    onPress(primaryCode);
                    onRelease(primaryCode);
                }
                break;
            case KeyCodes.CTRL_LOCK:
                mControlKeyState.toggleLocked();
                handleControl();
                break;
            case KeyCodes.ARROW_LEFT:
            case KeyCodes.ARROW_RIGHT:
                final int keyEventKeyCode = primaryCode == KeyCodes.ARROW_LEFT ?
                        KeyEvent.KEYCODE_DPAD_LEFT : KeyEvent.KEYCODE_DPAD_RIGHT;
                if (!handleSelectionExpending(keyEventKeyCode, ic, mGlobalSelectionStartPosition, mGlobalCursorPosition)) {
                    sendDownUpKeyEvents(keyEventKeyCode);
                }
                break;
            case KeyCodes.ARROW_UP:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_UP);
                break;
            case KeyCodes.ARROW_DOWN:
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_DOWN);
                break;
            case KeyCodes.MOVE_HOME:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    sendDownUpKeyEvents(0x0000007a/*API 11:KeyEvent.KEYCODE_MOVE_HOME*/);
                } else {
                    if (ic != null) {
                        CharSequence textBefore = ic.getTextBeforeCursor(1024, 0);
                        if (!TextUtils.isEmpty(textBefore)) {
                            int newPosition = textBefore.length() - 1;
                            while (newPosition > 0) {
                                char chatAt = textBefore.charAt(newPosition - 1);
                                if (chatAt == '\n' || chatAt == '\r') {
                                    break;
                                }
                                newPosition--;
                            }
                            if (newPosition < 0)
                                newPosition = 0;
                            ic.setSelection(newPosition, newPosition);
                        }
                    }
                }
                break;
            case KeyCodes.MOVE_END:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    //API 11: KeyEvent.KEYCODE_MOVE_END
                    sendDownUpKeyEvents(0x0000007b);
                } else {
                    if (ic != null) {
                        CharSequence textAfter = ic.getTextAfterCursor(1024, 0);
                        if (!TextUtils.isEmpty(textAfter)) {
                            int newPosition = 1;
                            while (newPosition < textAfter.length()) {
                                char chatAt = textAfter.charAt(newPosition);
                                if (chatAt == '\n' || chatAt == '\r') {
                                    break;
                                }
                                newPosition++;
                            }
                            if (newPosition > textAfter.length())
                                newPosition = textAfter.length();
                            try {
                                CharSequence textBefore = ic.getTextBeforeCursor(Integer.MAX_VALUE, 0);
                                if (!TextUtils.isEmpty(textBefore)) {
                                    newPosition = newPosition + textBefore.length();
                                }
                                ic.setSelection(newPosition, newPosition);
                            } catch (Throwable e/*I'm using Integer.MAX_VALUE, it's scary.*/) {
                                Logger.w(TAG, "Failed to getTextBeforeCursor.", e);
                            }
                        }
                    }
                }
                break;
            case KeyCodes.VOICE_INPUT:
                if (mVoiceRecognitionTrigger.isInstalled()) {
                    mVoiceRecognitionTrigger.startVoiceRecognition(getCurrentAlphabetKeyboard().getDefaultDictionaryLocale());
                } else {
                    Intent voiceInputNotInstalledIntent = new Intent(getApplicationContext(), VoiceInputNotInstalledActivity.class);
                    voiceInputNotInstalledIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(voiceInputNotInstalledIntent);
                }
                break;
            case KeyCodes.CANCEL:
                handleClose();
                break;
            case KeyCodes.SETTINGS:
                showOptionsMenu();
                break;
            case KeyCodes.SPLIT_LAYOUT:
            case KeyCodes.MERGE_LAYOUT:
            case KeyCodes.COMPACT_LAYOUT_TO_RIGHT:
            case KeyCodes.COMPACT_LAYOUT_TO_LEFT:
                if (getInputView() != null) {
                    mKeyboardInCondensedMode = CondenseType.fromKeyCode(primaryCode);
                    setKeyboardForView(getCurrentKeyboard());
                }
                break;
            case KeyCodes.DOMAIN:
                onText(key, mAskPrefs.getDomainText());
                break;
            case KeyCodes.QUICK_TEXT:
                onQuickTextRequested(key);
                break;
            case KeyCodes.QUICK_TEXT_POPUP:
                onQuickTextKeyboardRequested(key);
                break;
            case KeyCodes.MODE_SYMOBLS:
                nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Symbols);
                break;
            case KeyCodes.MODE_ALPHABET:
                if (getKeyboardSwitcher().shouldPopupForLanguageSwitch()) {
                    showLanguageSelectionDialog();
                } else {
                    nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
                }
                break;
            case KeyCodes.UTILITY_KEYBOARD:
                getInputView().openUtilityKeyboard();
                break;
            case KeyCodes.MODE_ALPHABET_POPUP:
                showLanguageSelectionDialog();
                break;
            case KeyCodes.ALT:
                nextAlterKeyboard(getCurrentInputEditorInfo());
                break;
            case KeyCodes.KEYBOARD_CYCLE:
                nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Any);
                break;
            case KeyCodes.KEYBOARD_REVERSE_CYCLE:
                nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.PreviousAny);
                break;
            case KeyCodes.KEYBOARD_CYCLE_INSIDE_MODE:
                nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.AnyInsideMode);
                break;
            case KeyCodes.KEYBOARD_MODE_CHANGE:
                nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.OtherMode);
                break;
            case KeyCodes.CLIPBOARD_COPY:
            case KeyCodes.CLIPBOARD_PASTE:
            case KeyCodes.CLIPBOARD_CUT:
            case KeyCodes.CLIPBOARD_SELECT_ALL:
            case KeyCodes.CLIPBOARD_PASTE_POPUP:
            case KeyCodes.CLIPBOARD_SELECT:
                handleClipboardOperation(key, primaryCode, ic);
                //not allowing undo on-text in clipboard paste operations.
                if (primaryCode == KeyCodes.CLIPBOARD_PASTE) mCommittedWord = "";
                break;
            default:
                if (BuildConfig.DEBUG) {
                    //this should not happen! We should handle ALL function keys.
                    throw new RuntimeException("UNHANDLED FUNCTION KEY! primary code " + primaryCode);
                } else {
                    Logger.w(TAG, "UNHANDLED FUNCTION KEY! primary code %d. Ignoring.", primaryCode);
                }
        }
    }

    private void onNonFunctionKey(final int primaryCode, final Key key, final int multiTapIndex, final int[] nearByKeyCodes, final boolean fromUI) {
        if (BuildConfig.DEBUG) Logger.d(TAG, "onFunctionKey %d", primaryCode);

        final InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case KeyCodes.ENTER:
                if (mShiftKeyState.isPressed() && ic != null) {
                    //power-users feature ahead: Shift+Enter
                    //getting away from firing the default editor action, by forcing newline
                    ic.commitText("\n", 1);
                    break;
                }
                final EditorInfo editorInfo = getCurrentInputEditorInfo();
                final int imeOptionsActionId = IMEUtil.getImeOptionsActionIdFromEditorInfo(editorInfo);
                if (ic != null && IMEUtil.IME_ACTION_CUSTOM_LABEL == imeOptionsActionId) {
                    // Either we have an actionLabel and we should performEditorAction with
                    // actionId regardless of its value.
                    ic.performEditorAction(editorInfo.actionId);
                } else if (ic != null && EditorInfo.IME_ACTION_NONE != imeOptionsActionId) {
                    // We didn't have an actionLabel, but we had another action to execute.
                    // EditorInfo.IME_ACTION_NONE explicitly means no action. In contrast,
                    // EditorInfo.IME_ACTION_UNSPECIFIED is the default value for an action, so it
                    // means there should be an action and the app didn't bother to set a specific
                    // code for it - presumably it only handles one. It does not have to be treated
                    // in any specific way: anything that is not IME_ACTION_NONE should be sent to
                    // performEditorAction.
                    ic.performEditorAction(imeOptionsActionId);
                } else {
                    handleSeparator(primaryCode);
                }
                break;
            case KeyCodes.SPACE:
                //shortcut. Nothing more.
                handleSeparator(primaryCode);
                //should we switch to alphabet keyboard?
                if (!isInAlphabetKeyboardMode()) {
                    Logger.d(TAG, "SPACE/ENTER while in symbols mode");
                    if (mAskPrefs.getSwitchKeyboardOnSpace()) {
                        Logger.d(TAG, "Switching to Alphabet is required by the user");
                        getKeyboardSwitcher().nextKeyboard(getCurrentInputEditorInfo(), NextKeyboardType.Alphabet);
                    }
                }
                break;
            case KeyCodes.TAB:
                sendTab();
                break;
            case KeyCodes.ESCAPE:
                sendEscape();
                break;
            default:
                if (isWordSeparator(primaryCode)) {
                    handleSeparator(primaryCode);
                } else {
                    if (mControlKeyState.isActive() && primaryCode >= 32 && primaryCode < 127) {
                        // http://en.wikipedia.org/wiki/Control_character#How_control_characters_map_to_keyboards
                        int controlCode = primaryCode & 31;
                        Logger.d(TAG, "CONTROL state: Char was %d and now it is %d", primaryCode, controlCode);
                        if (controlCode == 9) {
                            sendTab();
                        } else {
                            ic.commitText(Character.toString((char) controlCode), 1);
                        }
                    } else {
                        handleCharacter(primaryCode, key, multiTapIndex, nearByKeyCodes);
                    }
                    mJustAddedAutoSpace = false;
                }
                break;
        }
    }

    @Override
    public void onKey(int primaryCode, Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        if (primaryCode > 0)
            onNonFunctionKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
        else
            onFunctionKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);

        setSpaceTimeStamp(primaryCode == KeyCodes.SPACE);
    }

    private void setSpaceTimeStamp(boolean isSpace) {
        if (isSpace) {
            mLastSpaceTimeStamp = SystemClock.uptimeMillis();
        } else {
            mLastSpaceTimeStamp = NEVER_TIME_STAMP;
        }
    }

    private boolean isTerminalEmulation() {
        EditorInfo ei = getCurrentInputEditorInfo();
        if (ei == null) return false;

        switch (ei.packageName) {
            case "org.connectbot":
            case "org.woltage.irssiconnectbot":
            case "com.pslib.connectbot":
            case "com.sonelli.juicessh":
                return ei.inputType == 0;
            default:
                return false;
        }
    }

    private void sendTab() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        boolean tabHack = isTerminalEmulation();

        // Note: tab and ^I don't work in ConnectBot, hackish workaround
        if (tabHack) {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_DPAD_CENTER));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_DPAD_CENTER));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_I));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_I));
        } else {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_TAB));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
                    KeyEvent.KEYCODE_TAB));
        }
    }

    private void sendEscape() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        if (isTerminalEmulation()) {
            sendKeyChar((char) 27);
        } else {
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, 111 /* KEYCODE_ESCAPE */));
            ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, 111 /* KEYCODE_ESCAPE */));
        }
    }

    @Override
    public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
        super.onAlphabetKeyboardSet(keyboard);
        setKeyboardForView(keyboard);
        setKeyboardFinalStuff();
    }

    @Override
    public void onSymbolsKeyboardSet(@NonNull AnyKeyboard keyboard) {
        super.onSymbolsKeyboardSet(keyboard);
        setKeyboardForView(keyboard);
    }

    private void setKeyboardForView(AnyKeyboard currentKeyboard) {
        currentKeyboard.setCondensedKeys(mKeyboardInCondensedMode);
        if (getInputView() != null) {
            getInputView().setKeyboard(currentKeyboard, getKeyboardSwitcher().peekNextAlphabetKeyboard(), getKeyboardSwitcher().peekNextSymbolsKeyboard());
        }
    }

    private void showLanguageSelectionDialog() {
        KeyboardAddOnAndBuilder[] builders = getKeyboardSwitcher().getEnabledKeyboardsBuilders();
        ArrayList<CharSequence> keyboardsIds = new ArrayList<>();
        ArrayList<CharSequence> keyboards = new ArrayList<>();
        // going over all enabled keyboards
        for (KeyboardAddOnAndBuilder keyboardBuilder : builders) {
            keyboardsIds.add(keyboardBuilder.getId());
            String name = keyboardBuilder.getName();

            keyboards.add(name);
        }

        final CharSequence[] ids = new CharSequence[keyboardsIds.size()];
        final CharSequence[] items = new CharSequence[keyboards.size()];
        keyboardsIds.toArray(ids);
        keyboards.toArray(items);

        showOptionsDialogWithData(getText(R.string.select_keyboard_popup_title), R.drawable.ic_keyboard_globe_menu,
                items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int position) {
                        CharSequence id = ids[position];
                        Logger.d(TAG, "User selected '%s' with id %s", items[position], id);
                        EditorInfo currentEditorInfo = getCurrentInputEditorInfo();
                        getKeyboardSwitcher().nextAlphabetKeyboard(currentEditorInfo, id.toString());
                    }
                });
    }

    public void onText(Key key, CharSequence text) {
        Logger.d(TAG, "onText: '%s'", text);
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return;
        ic.beginBatchEdit();

        abortCorrectionAndResetPredictionState(false);
        ic.commitText(text, 1);

        mJustAddedAutoSpace = false;
        mCommittedWord = text;
        mUndoCommitCursorPosition = UNDO_COMMIT_WAITING_TO_RECORD_POSITION;

        TextEntryState.acceptedDefault(text);
        ic.endBatchEdit();

        setSuggestions(mSuggest.getNextSuggestions(mCommittedWord, false), false, false, false);
    }

    private void handleBackWord(InputConnection ic) {
        if (ic == null) {
            return;
        }

        if (TextEntryState.isPredicting()) {
            mWord.reset();
            mSuggest.resetNextWordSentence();
            TextEntryState.newSession(mPredictionOn);
            ic.setComposingText("", 1);
            postUpdateSuggestions();
            return;
        }
        // I will not delete more than 128 characters. Just a safe-guard.
        // this will also allow me do just one call to getTextBeforeCursor!
        // Which is always good. This is a part of issue 951.
        CharSequence cs = ic.getTextBeforeCursor(128, 0);
        if (TextUtils.isEmpty(cs)) {
            return;// nothing to delete
        }
        // TWO OPTIONS
        // 1) Either we do like Linux and Windows (and probably ALL desktop
        // OSes):
        // Delete all the characters till a complete word was deleted:
        /*
         * What to do: We delete until we find a separator (the function
         * isBackWordStopChar). Note that we MUST delete a delete a whole word!
         * So if the back-word starts at separators, we'll delete those, and then
         * the word before: "test this,       ," -> "test "
         */
        // Pro: same as desktop
        // Con: when auto-caps is on (the default), this will delete the
        // previous word, which can be annoying..
        // E.g., Writing a sentence, then a period, then ASK will auto-caps,
        // then when the user press backspace (for some reason),
        // the entire previous word deletes.

        // 2) Or we delete all the characters till we encounter a separator, but
        // delete at least one character.
        /*
         * What to do: We delete until we find a separator (the function
         * isBackWordStopChar). Note that we MUST delete a delete at least one
         * character "test this, " -> "test this," -> "test this" -> "test "
         */
        // Pro: Supports auto-caps, and mostly similar to desktop OSes
        // Con: Not all desktop use-cases are here.

        // For now, I go with option 2, but I'm open for discussion.

        // 2b) "test this, " -> "test this"

        final int inputLength = cs.length();
        int idx = inputLength - 1;// it's OK since we checked whether cs is
        // empty after retrieving it.
        while (idx > 0 && !isBackWordStopChar((int) cs.charAt(idx))) {
            idx--;
        }
        ic.deleteSurroundingText(inputLength - idx, 0);// it is always > 0 !
    }

    private void handleDeleteLastCharacter(boolean forMultiTap) {
        InputConnection ic = getCurrentInputConnection();

        boolean deleteChar = false;
        if (TextEntryState.isPredicting()) {
            final boolean wordManipulation = mWord.length() > 0 && mWord.cursorPosition() > 0;
            if (wordManipulation) {
                mWord.deleteLast();
                final int cursorPosition;
                if (mWord.cursorPosition() != mWord.length())
                    cursorPosition = getCursorPosition(ic);
                else
                    cursorPosition = -1;

                if (cursorPosition >= 0)
                    ic.beginBatchEdit();

                ic.setComposingText(mWord.getTypedWord(), 1);
                if (mWord.length() == 0) {
                    TextEntryState.newSession(mPredictionOn);
                } else if (cursorPosition >= 0) {
                    ic.setSelection(cursorPosition - 1, cursorPosition - 1);
                }

                if (cursorPosition >= 0)
                    ic.endBatchEdit();

                postUpdateSuggestions();
            } else {
                ic.deleteSurroundingText(1, 0);
            }
        } else {
            deleteChar = true;
        }

        TextEntryState.backspace();
        if (TextEntryState.getState() == TextEntryState.State.UNDO_COMMIT) {
            revertLastWord();
        } else if (deleteChar) {
            //just making sure that
            if (mCandidateView != null) mCandidateView.dismissAddToDictionaryHint();

            if (!forMultiTap) {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            } else {
                // this code tries to delete the text in a different way,
                // because of multi-tap stuff
                // using "deleteSurroundingText" will actually get the input
                // updated faster!
                // but will not handle "delete all selected text" feature,
                // hence the "if (!forMultiTap)" above
                final CharSequence beforeText = ic == null ? null : ic.getTextBeforeCursor(1, 0);
                final int textLengthBeforeDelete = (TextUtils.isEmpty(beforeText)) ? 0 : beforeText.length();
                if (textLengthBeforeDelete > 0)
                    ic.deleteSurroundingText(1, 0);
                else
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            }
        }
    }

    private void handleControl() {
        if (getInputView() != null && isInAlphabetKeyboardMode()) {
            getInputView().setControl(mControlKeyState.isActive());
        }
    }

    private void handleShift() {
        if (getInputView() != null) {
            Logger.d(TAG, "shift Setting UI active:%s, locked: %s", mShiftKeyState.isActive(), mShiftKeyState.isLocked());
            getInputView().setShifted(mShiftKeyState.isActive());
            getInputView().setShiftLocked(mShiftKeyState.isLocked());
        }
    }

    @Override
    protected void abortCorrectionAndResetPredictionState(boolean forever) {
        super.abortCorrectionAndResetPredictionState(forever);
        mJustAutoAddedWord = false;
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS);

        final InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.finishComposingText();

        clearSuggestions();

        mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
        mCommittedWord = "";
        mWord.reset();
        TextEntryState.newSession(mPredictionOn);
        mJustAddedAutoSpace = false;
        mJustAutoAddedWord = false;
        if (forever) {
            Logger.d(TAG, "abortCorrection will abort correct forever");
            mPredictionOn = false;
            setCandidatesViewShown(false);
        }
        TextEntryState.newSession(mPredictionOn && (!forever));
    }

    private void handleCharacter(final int primaryCode, final Key key, final int multiTapIndex, int[] nearByKeyCodes) {
        if (BuildConfig.DEBUG)
            Logger.d(TAG, "handleCharacter: %d, isPredictionOn: %s, mPredicting: %s", primaryCode, isPredictionOn(), TextEntryState.isPredicting());

        mExpectingSelectionUpdateBy = SystemClock.uptimeMillis() + MAX_TIME_TO_EXPECT_SELECTION_UPDATE;
        if (TextEntryState.isReadyToPredict() && isAlphabet(primaryCode) && !isCursorTouchingWord()) {
            TextEntryState.newSession(mPredictionOn);
            mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
            mWord.reset();
            mAutoCorrectOn = mAutoComplete;
            TextEntryState.typedCharacter((char) primaryCode, false);
            if (mShiftKeyState.isActive()) {
                mWord.setFirstCharCapitalized(true);
            }
        } else if (TextEntryState.isPredicting()) {
            TextEntryState.typedCharacter((char) primaryCode, false);
        }

        mLastCharacterWasShifted = (getInputView() != null) && getInputView().isShifted();

        if (TextEntryState.isPredicting()) {
            final InputConnection ic = getCurrentInputConnection();
            mWord.add(primaryCode, nearByKeyCodes);
            ChewbaccaOnTheDrums.onKeyTyped(mWord, getApplicationContext());

            if (ic != null) {
                final int cursorPosition;
                if (mWord.cursorPosition() != mWord.length()) {
                    //Cursor is not at the end of the word. I'll need to reposition
                    cursorPosition = mGlobalCursorPosition + 1/*adding the new character*/;
                    ic.beginBatchEdit();
                } else {
                    cursorPosition = -1;
                }

                ic.setComposingText(mWord.getTypedWord(), 1);
                if (cursorPosition > 0) {
                    ic.setSelection(cursorPosition, cursorPosition);
                    ic.endBatchEdit();
                }
            }
            // this should be done ONLY if the key is a letter, and not a inner
            // character (like ').
            if (isSuggestionAffectingCharacter(primaryCode)) {
                postUpdateSuggestions();
            } else {
                // just replace the typed word in the candidates view
                if (mCandidateView != null)
                    mCandidateView.replaceTypedWord(mWord.getTypedWord());
            }
        } else {
            sendKeyChar((char) primaryCode);
        }
        mJustAutoAddedWord = false;
    }

    private void handleSeparator(int primaryCode) {
        // Issue 146: Right to left languages require reversed parenthesis
        if (!getCurrentAlphabetKeyboard().isLeftToRightLanguage()) {
            if (primaryCode == (int) ')')
                primaryCode = (int) '(';
            else if (primaryCode == (int) '(')
                primaryCode = (int) ')';
        }
        mExpectingSelectionUpdateBy = SystemClock.uptimeMillis() + MAX_TIME_TO_EXPECT_SELECTION_UPDATE;
        //will not show next-word suggestion in case of a new line or if the separator is a sentence separator.
        boolean isEndOfSentence = (primaryCode == KeyCodes.ENTER || mSentenceSeparators.get(primaryCode));

        // Should dismiss the "Touch again to save" message when handling
        // separator
        if (mCandidateView != null && mCandidateView.dismissAddToDictionaryHint()) {
            postUpdateSuggestions();
        }

        // Handle separator
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }
        // this is a special case, when the user presses a separator WHILE
        // inside the predicted word.
        // in this case, I will want to just dump the separator.
        final boolean separatorInsideWord = (mWord.cursorPosition() < mWord.length());
        if (TextEntryState.isPredicting() && !separatorInsideWord) {
            //ACTION does not invoke default picking. See https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/198
            pickDefaultSuggestion(mAutoCorrectOn && primaryCode != KeyCodes.ENTER);
            // Picked the suggestion by a space/punctuation character: we will treat it
            // as "added an auto space".
            mJustAddedAutoSpace = true;
        } else if (separatorInsideWord) {
            // when putting a separator in the middle of a word, there is no
            // need to do correction, or keep knowledge
            abortCorrectionAndResetPredictionState(false);
        }

        if (mJustAddedAutoSpace && primaryCode == KeyCodes.ENTER) {
            removeTrailingSpace();
            mJustAddedAutoSpace = false;
        }

        boolean handledOutputToInputConnection = false;

        if (ic != null) {
            if (primaryCode == KeyCodes.SPACE) {
                if (mAskPrefs.isDoubleSpaceChangesToPeriod()) {
                    if ((SystemClock.uptimeMillis() - mLastSpaceTimeStamp) < ((long) mAskPrefs.getMultiTapTimeout())) {
                        //current text in the input-box should be something like "word "
                        //the user pressed on space again. So we want to change the text in the input-box
                        //into "word "->"word. "
                        ic.deleteSurroundingText(1, 0);
                        ic.commitText(". ", 1);
                        mJustAddedAutoSpace = true;
                        isEndOfSentence = true;
                        handledOutputToInputConnection = true;
                    }
                }
            } else if (mJustAddedAutoSpace && mLastSpaceTimeStamp != NEVER_TIME_STAMP/*meaning last key was SPACE*/ &&
                    mAskPrefs.shouldSwapPunctuationAndSpace() &&
                    primaryCode != KeyCodes.ENTER &&
                    isSentenceSeparator(primaryCode)) {
                //current text in the input-box should be something like "word "
                //the user pressed a punctuation (say ","). So we want to change the text in the input-box
                //into "word "->"word, "
                ic.deleteSurroundingText(1, 0);
                ic.commitText(((char) primaryCode) + " ", 1);
                mJustAddedAutoSpace = true;
                handledOutputToInputConnection = true;
            }
        }

        if (!handledOutputToInputConnection) {
            sendKeyChar((char) primaryCode);
        }
        TextEntryState.typedCharacter((char) primaryCode, true);

        if (ic != null) {
            ic.endBatchEdit();
        }

        if (isEndOfSentence) {
            mSuggest.resetNextWordSentence();
            clearSuggestions();
        } else if (!TextUtils.isEmpty(mCommittedWord)) {
            setSuggestions(mSuggest.getNextSuggestions(mCommittedWord, mWord.isAllUpperCase()), false, false, false);
            mWord.setFirstCharCapitalized(false);
        }
    }

    protected void handleClose() {
        boolean closeSelf = true;

        if (getInputView() != null)
            closeSelf = getInputView().closing();

        if (closeSelf) {
            requestHideSelf(0);
            abortCorrectionAndResetPredictionState(true);
        }
    }

    private void postUpdateSuggestions() {
        postUpdateSuggestions(5 * ONE_FRAME_DELAY);
    }

    /**
     * posts an update suggestions request to the messages queue. Removes any previous request.
     *
     * @param delay negative value will cause the call to be done now, in this thread.
     */
    private void postUpdateSuggestions(long delay) {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);
        if (delay > 0)
            mKeyboardHandler.sendMessageDelayed(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS), delay);
        else if (delay == 0)
            mKeyboardHandler.sendMessage(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS));
        else
            performUpdateSuggestions();
    }

    protected boolean isPredictionOn() {
        return mPredictionOn;
    }

    private boolean shouldCandidatesStripBeShown() {
        return mShowSuggestions && onEvaluateInputViewShown();
    }

    /*package*/ void performUpdateSuggestions() {
        //mCandidateCloseText could be null if setCandidatesView was not called yet
        if (mCandidateCloseText != null) mCandidateCloseText.setVisibility(View.GONE);

        if (!TextEntryState.isPredicting()) {
            clearSuggestions();
            return;
        }

        final CharSequence typedWord = mWord.getTypedWord();

        final List<CharSequence> suggestionsList = mSuggest.getSuggestions(mWord, false);
        boolean correctionAvailable = mSuggest.hasMinimalCorrection();
        final boolean typedWordValid = mSuggest.isValidWord(typedWord) && !mWord.isAtTagsSearchState();

        if (mShowSuggestions || mQuickFixes) {
            correctionAvailable |= typedWordValid;
        }

        // Don't auto-correct words with multiple capital letter
        correctionAvailable &= !mWord.isMostlyCaps();

        setSuggestions(suggestionsList, false, typedWordValid, correctionAvailable);
        if (suggestionsList.size() > 0) {
            if (correctionAvailable && !typedWordValid && suggestionsList.size() > 1) {
                mWord.setPreferredWord(suggestionsList.get(1));
            } else {
                mWord.setPreferredWord(typedWord);
            }
        } else {
            mWord.setPreferredWord(null);
        }
        setCandidatesViewShown(shouldCandidatesStripBeShown() || mCompletionOn);
    }

    private boolean pickDefaultSuggestion(boolean autoCorrectToPreferred) {
        // Complete any pending candidate query first
        if (mKeyboardHandler.hasMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS)) {
            performUpdateSuggestions();
        }

        final CharSequence typedWord = mWord.getTypedWord();
        final CharSequence actualWordToOutput = autoCorrectToPreferred ? mWord.getPreferredWord() : typedWord;
        Logger.d(TAG, "pickDefaultSuggestion: actualWordToOutput: %s, since mAutoCorrectOn is %s", actualWordToOutput, mAutoCorrectOn);

        if (!TextUtils.isEmpty(actualWordToOutput)) {
            TextEntryState.acceptedDefault(typedWord);
            final boolean fixed = !typedWord.equals(actualWordToOutput);
            commitWordToInput(actualWordToOutput, fixed);
            if (!fixed) {//if the word typed was auto-replaced, we should not learn it.
                // Add the word to the auto dictionary if it's not a known word
                // this is "typed" if the auto-correction is off, or "picked" if it is on or momentarily off.
                checkAddToDictionaryWithAutoDictionary(mWord, mAutoComplete ? AutoDictionary.AdditionType.Picked : AutoDictionary.AdditionType.Typed);
            }
            return true;
        }
        return false;
    }

    public void pickSuggestionManually(int index, CharSequence suggestion) {
        final String typedWord = mWord.getTypedWord().toString();

        if (mWord.isAtTagsSearchState()) {
            if (index == 0) {
                //this is a special case for tags-searcher
                //since we append a magnifying glass to the suggestions, the "suggestion"
                //value is not a valid output suggestion
                suggestion = typedWord;
            } else {
                //regular emoji. Storing in history.
                List<QuickKeyHistoryRecords.HistoryKey> keys = QuickKeyHistoryRecords.load(getSharedPrefs());
                QuickKeyHistoryRecords.store(getSharedPrefs(), keys, new QuickKeyHistoryRecords.HistoryKey(suggestion.toString(), suggestion.toString()));
            }
        }

        final InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }

        TextEntryState.acceptedSuggestion(typedWord, suggestion);

        try {
            if (mCompletionOn && mCompletions != null && index >= 0 && index < mCompletions.length) {
                CompletionInfo ci = mCompletions[index];
                if (ic != null) {
                    ic.commitCompletion(ci);
                }
                mCommittedWord = suggestion;
                if (mCandidateView != null) {
                    mCandidateView.clear();
                }
                return;
            }
            commitWordToInput(suggestion, false/*user physically picked a word from the suggestions strip. this is not a fix*/);

            TextEntryState.acceptedSuggestion(mWord.getTypedWord(), suggestion);
            // Follow it with a space
            if (mAutoSpace && (index == 0 || !mWord.isAtTagsSearchState())) {
                sendKeyChar((char) KeyCodes.SPACE);
                mJustAddedAutoSpace = true;
                setSpaceTimeStamp(true);
                TextEntryState.typedCharacter(' ', true);
            }
            // Add the word to the auto dictionary if it's not a known word
            mJustAutoAddedWord = false;

            if (!mWord.isAtTagsSearchState()) {
                if (index == 0) {
                    mJustAutoAddedWord = checkAddToDictionaryWithAutoDictionary(mWord, AutoDictionary.AdditionType.Picked);
                    if (mJustAutoAddedWord) TextEntryState.acceptedSuggestionAddedToDictionary();
                }

                final boolean showingAddToDictionaryHint =
                        (!mJustAutoAddedWord)
                                && index == 0
                                && (mQuickFixes || mShowSuggestions)
                                && (!mSuggest.isValidWord(suggestion))// this is for the case that the word was auto-added upon picking
                                && (!mSuggest.isValidWord(suggestion.toString().toLowerCase(getCurrentAlphabetKeyboard().getLocale())));

                if (showingAddToDictionaryHint) {
                    if (mCandidateView != null) mCandidateView.showAddToDictionaryHint(suggestion);
                } else if (!TextUtils.isEmpty(mCommittedWord) && !mJustAutoAddedWord) {
                    //showing next-words if:
                    //showingAddToDictionaryHint == false, we most likely do not have a next-word suggestion! The committed word is not in the dictionary
                    //mJustAutoAddedWord == false, we most likely do not have a next-word suggestion for a newly added word.
                    setSuggestions(mSuggest.getNextSuggestions(mCommittedWord, mWord.isAllUpperCase()), false, false, false);
                    mWord.setFirstCharCapitalized(false);
                }
            }
        } finally {
            if (ic != null) {
                ic.endBatchEdit();
            }
        }
    }

    /**
     * Commits the chosen word to the text field and saves it for later
     * retrieval.
     *
     * @param wordToCommit the suggestion picked by the user to be committed to the text
     *                   field
     * @param correcting this is a correction commit
     */
    protected void commitWordToInput(@NonNull CharSequence wordToCommit, boolean correcting) {
        mWord.setPreferredWord(wordToCommit);
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            if (correcting) {
                AnyApplication.getDeviceSpecific().commitCorrectionToInputConnection(ic, mGlobalCursorPosition - mWord.getTypedWord().length(), mWord.getTypedWord(), mWord.getPreferredWord());
                // and drawing pop-out text
                getInputView().popTextOutOfKey(mWord.getPreferredWord());
            } else {
                ic.commitText(wordToCommit, 1);
            }
        }
        mCommittedWord = wordToCommit;
        mUndoCommitCursorPosition = UNDO_COMMIT_WAITING_TO_RECORD_POSITION;

        clearSuggestions();
    }

    private boolean isCursorTouchingWord() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null)
            return false;

        CharSequence toLeft = ic.getTextBeforeCursor(1, 0);
        // It is not exactly clear to me why, but sometimes, although I request
        // 1 character, I get
        // the entire text. This causes me to incorrectly detect restart
        // suggestions...
        if (!TextUtils.isEmpty(toLeft) && toLeft.length() == 1 && !isWordSeparator(toLeft.charAt(0))) {
            return true;
        }

        CharSequence toRight = ic.getTextAfterCursor(1, 0);
        return (!TextUtils.isEmpty(toRight)) &&
                (toRight.length() == 1) &&
                (!isWordSeparator(toRight.charAt(0)));
    }

    public void revertLastWord() {
        final int length = mCommittedWord.length() + (mJustAddedAutoSpace? 1 : 0);
        if (length > 0) {
            mAutoCorrectOn = false;
            //note: typedWord may be empty
            final InputConnection ic = getCurrentInputConnection();
            mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
            ic.beginBatchEdit();
            ic.deleteSurroundingText(length, 0);
            final CharSequence typedWord = mWord.getTypedWord();
            ic.setComposingText(typedWord/* mComposing */, 1);
            TextEntryState.backspace();
            ic.endBatchEdit();
            performUpdateSuggestions();
            if (mJustAutoAddedWord) {
                removeFromUserDictionary(typedWord.toString());
            }
            getInputView().revertPopTextOutOfKey();
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        }
    }

    private boolean isSentenceSeparator(int code) {
        return mSentenceSeparators.get(code, false);
    }

    private boolean isWordSeparator(int code) {
        return (!isAlphabet(code));
    }

    public boolean preferCapitalization() {
        return mWord.isFirstCharCapitalized();
    }

    private void nextAlterKeyboard(EditorInfo currentEditorInfo) {
        getKeyboardSwitcher().nextAlterKeyboard(currentEditorInfo);

        Logger.d(TAG, "nextAlterKeyboard: Setting next keyboard to: %s", getCurrentSymbolsKeyboard().getKeyboardName());
    }

    private void nextKeyboard(EditorInfo currentEditorInfo, KeyboardSwitcher.NextKeyboardType type) {
        getKeyboardSwitcher().nextKeyboard(currentEditorInfo, type);
    }

    private static void fillSeparatorsSparseArray(SparseBooleanArray sparseBooleanArray, char[] chars) {
        sparseBooleanArray.clear();
        for (char separator : chars) sparseBooleanArray.put(separator, true);
    }

    private void setKeyboardFinalStuff() {
        mShiftKeyState.reset();
        mControlKeyState.reset();
        // changing dictionary
        setDictionariesForCurrentKeyboard();
        // Notifying if needed
        setKeyboardStatusIcon();
        postUpdateSuggestions();
        updateShiftStateNow();
    }

    private void sendKeyDown(InputConnection ic, int key) {
        if (ic != null) ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key));
    }

    private void sendKeyUp(InputConnection ic, int key) {
        if (ic != null) ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key));
    }

    public void onPress(int primaryCode) {
        super.onPress(primaryCode);
        InputConnection ic = getCurrentInputConnection();
        if (mVibrationDuration > 0 && primaryCode != 0 && mVibrator != null) {
            try {
                mVibrator.vibrate(mVibrationDuration);
            } catch (Exception e) {
                Logger.w(TAG, "Failed to interact with vibrator! Disabling for now.");
                mVibrationDuration = 0;
            }
        }

        if (primaryCode == KeyCodes.SHIFT) {
            mShiftKeyState.onPress();
            handleShift();
        } else {
            mShiftKeyState.onOtherKeyPressed();
        }

        if (primaryCode == KeyCodes.CTRL) {
            mControlKeyState.onPress();
            handleControl();
            sendKeyDown(ic, 113); // KeyEvent.KEYCODE_CTRL_LEFT (API 11 and up)
        } else {
            mControlKeyState.onOtherKeyPressed();
        }

        if (mSoundOn && (!mSilentMode) && primaryCode != 0) {
            final int keyFX;
            switch (primaryCode) {
                case 13:
                case KeyCodes.ENTER:
                    keyFX = AudioManager.FX_KEYPRESS_RETURN;
                    break;
                case KeyCodes.DELETE:
                    keyFX = AudioManager.FX_KEYPRESS_DELETE;
                    break;
                case KeyCodes.SPACE:
                    keyFX = AudioManager.FX_KEYPRESS_SPACEBAR;
                    break;
                default:
                    keyFX = AudioManager.FX_KEY_CLICK;
            }
            final float fxVolume;
            // creating scoop to make sure volume and maxVolume
            // are not used
            {
                final int volume;
                final int maxVolume;
                if (mSoundVolume > 0) {
                    volume = mSoundVolume;
                    maxVolume = 100;
                    fxVolume = ((float) volume) / ((float) maxVolume);
                } else {
                    fxVolume = -1.0f;
                }

            }

            mAudioManager.playSoundEffect(keyFX, fxVolume);
        }
    }

    public void onRelease(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        if (primaryCode == KeyCodes.SHIFT) {
            mShiftKeyState.onRelease(mAskPrefs.getMultiTapTimeout());
            handleShift();
        } else {
            if (mShiftKeyState.onOtherKeyReleased()) {
                updateShiftStateNow();
            }
        }

        if (primaryCode == KeyCodes.CTRL) {
            sendKeyUp(ic, 113); // KeyEvent.KEYCODE_CTRL_LEFT
            mControlKeyState.onRelease(mAskPrefs.getMultiTapTimeout());
            handleControl();
        } else {
            mControlKeyState.onOtherKeyReleased();
        }
    }

    // update flags for silent mode
    public void updateRingerMode() {
        mSilentMode = (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL);
    }

    @Override
    protected void onLoadSettingsRequired(SharedPreferences sp) {
        super.onLoadSettingsRequired(sp);

        mVibrationDuration = Integer.parseInt(sp.getString(getString(R.string.settings_key_vibrate_on_key_press_duration), getString(R.string.settings_default_vibrate_on_key_press_duration)));

        mSoundOn = sp.getBoolean(getString(R.string.settings_key_sound_on),
                getResources().getBoolean(R.bool.settings_default_sound_on));
        if (mSoundOn) {
            Logger.i(TAG, "Loading sounds effects from AUDIO_SERVICE due to configuration change.");
            try {
                mAudioManager.loadSoundEffects();
            } catch (SecurityException e) {
                //for unknown reason loadSoundEffects may throw SecurityException (happened on a HuaweiG750-U10/4.2.2).
                Logger.w(TAG, "SecurityException swallowed. ", e);
                mSoundOn = false;
            }
        }
        // checking the volume
        boolean customVolume = sp.getBoolean("use_custom_sound_volume", false);
        int newVolume;
        if (customVolume) {
            newVolume = sp.getInt("custom_sound_volume", 0) + 1;
            Logger.i(TAG, "Custom volume checked: " + newVolume + " out of 100");
        } else {
            Logger.i(TAG, "Custom volume un-checked.");
            newVolume = -1;
        }
        mSoundVolume = newVolume;

        mShowKeyboardIconInStatusBar = sp.getBoolean(
                getString(R.string.settings_key_keyboard_icon_in_status_bar),
                getResources().getBoolean(R.bool.settings_default_keyboard_icon_in_status_bar));

        if (mShowKeyboardIconInStatusBar) {
            setKeyboardStatusIcon();
        } else {
            mInputMethodManager.hideStatusIcon(mImeToken);
        }

        mAutoCap = sp.getBoolean("auto_caps", true);

        mShowSuggestions = sp.getBoolean("candidates_on", true);
        if (!mShowSuggestions) {
            //no suggestions is needed, we'll release all dictionaries.
            closeDictionaries();
        }

        final String autoPickAggressiveness = sp.getString(
                getString(R.string.settings_key_auto_pick_suggestion_aggressiveness),
                getString(R.string.settings_default_auto_pick_suggestion_aggressiveness));

        final int calculatedCommonalityMaxLengthDiff;
        final int calculatedCommonalityMaxDistance;
        switch (autoPickAggressiveness) {
            case "none":
                calculatedCommonalityMaxLengthDiff = 0;
                calculatedCommonalityMaxDistance = 0;
                mAutoComplete = false;
                break;
            case "minimal_aggressiveness":
                calculatedCommonalityMaxLengthDiff = 1;
                calculatedCommonalityMaxDistance = 1;
                mAutoComplete = true;
                break;
            case "high_aggressiveness":
                calculatedCommonalityMaxLengthDiff = 3;
                calculatedCommonalityMaxDistance = 4;
                mAutoComplete = true;
                break;
            case "extreme_aggressiveness":
                calculatedCommonalityMaxLengthDiff = 5;
                calculatedCommonalityMaxDistance = 5;
                mAutoComplete = true;
                break;
            default:
                calculatedCommonalityMaxLengthDiff = 2;
                calculatedCommonalityMaxDistance = 3;
                mAutoComplete = true;
        }
        mAutoCorrectOn = mAutoComplete = mAutoComplete && mShowSuggestions;

        mQuickFixes = sp.getBoolean("quick_fix", true);

        mAllowSuggestionsRestart = sp.getBoolean(
                getString(R.string.settings_key_allow_suggestions_restart),
                getResources().getBoolean(R.bool.settings_default_allow_suggestions_restart));

        mSuggest.setCorrectionMode(mQuickFixes, mShowSuggestions,
                calculatedCommonalityMaxLengthDiff, calculatedCommonalityMaxDistance,
                sp.getInt(getString(R.string.settings_key_min_length_for_word_correction__), 2));

        setInitialCondensedState(getResources().getConfiguration());
    }

    private void setDictionariesForCurrentKeyboard() {
        mSuggest.resetNextWordSentence();

        if (mPredictionOn) {
            // It null at the creation of the application.
            final AnyKeyboard currentAlphabetKeyboard = getCurrentAlphabetKeyboard();
            if ((currentAlphabetKeyboard != null) && isInAlphabetKeyboardMode()) {
                fillSeparatorsSparseArray(mSentenceSeparators, currentAlphabetKeyboard.getSentenceSeparators());

                // if there is a mapping in the settings, we'll use that,
                // else we'll
                // return the default
                String mappingSettingsKey = getDictionaryOverrideKey(currentAlphabetKeyboard);
                String defaultDictionary = currentAlphabetKeyboard.getDefaultDictionaryLocale();
                String dictionaryValue = getSharedPrefs().getString(mappingSettingsKey, null);

                final DictionaryAddOnAndBuilder dictionaryBuilder;

                if (dictionaryValue == null) {
                    dictionaryBuilder = ExternalDictionaryFactory.getDictionaryBuilderByLocale(
                            currentAlphabetKeyboard.getDefaultDictionaryLocale(), getApplicationContext());
                } else {
                    Logger.d(TAG, "Default dictionary '%s' for keyboard '%s' has been overridden to '%s'",
                            defaultDictionary, currentAlphabetKeyboard.getKeyboardPrefId(), dictionaryValue);
                    dictionaryBuilder = ExternalDictionaryFactory.getDictionaryBuilderById(dictionaryValue, getApplicationContext());
                }

                mSuggest.setMainDictionary(getApplicationContext(), dictionaryBuilder);
                String localeForSupportingDictionaries = dictionaryBuilder != null ? dictionaryBuilder.getLanguage() : defaultDictionary;
                Dictionary userDictionary = mSuggest.getDictionaryFactory().createUserDictionary(getApplicationContext(), localeForSupportingDictionaries);
                mSuggest.setUserDictionary(userDictionary);

                mAutoDictionary = mSuggest.getDictionaryFactory().createAutoDictionary(getApplicationContext(), localeForSupportingDictionaries);
                mSuggest.setAutoDictionary(mAutoDictionary);
                mSuggest.setContactsDictionary(getApplicationContext(), mAskPrefs.useContactsDictionary());
            }
        }
    }

    private void launchSettings() {
        handleClose();
        Intent intent = new Intent();
        intent.setClass(AnySoftKeyboard.this, MainSettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void launchDictionaryOverriding() {
        final String dictionaryOverridingKey = getDictionaryOverrideKey(getCurrentAlphabetKeyboard());
        final String dictionaryOverrideValue = getSharedPrefs().getString(dictionaryOverridingKey, null);
        ArrayList<CharSequence> dictionaryIds = new ArrayList<>();
        ArrayList<CharSequence> dictionariesNamesWithSelectedMark = new ArrayList<>();
        final ArrayList<CharSequence> dictionariesNamesForToast = new ArrayList<>();
        // null dictionary is handled as the default for the keyboard
        dictionaryIds.add(null);
        final String SELECTED = "\u2714 ";
        final String NOT_SELECTED = "- ";
        if (dictionaryOverrideValue == null) {
            dictionariesNamesWithSelectedMark.add(SELECTED + getString(R.string.override_dictionary_default));
        } else {
            dictionariesNamesWithSelectedMark.add(NOT_SELECTED + getString(R.string.override_dictionary_default));
        }
        dictionariesNamesForToast.add(getString(R.string.override_dictionary_default));
        // going over all installed dictionaries
        for (DictionaryAddOnAndBuilder dictionaryBuilder : ExternalDictionaryFactory.getAllAvailableExternalDictionaries(getApplicationContext())) {
            dictionaryIds.add(dictionaryBuilder.getId());
            String description = dictionaryBuilder.getName();
            if (!TextUtils.isEmpty(dictionaryBuilder.getDescription())) {
                description += " (" + dictionaryBuilder.getDescription() + ")";
            }

            dictionariesNamesForToast.add(description);
            if (dictionaryOverrideValue != null && dictionaryBuilder.getId().equals(dictionaryOverrideValue))
                description = SELECTED + description;
            else
                description = NOT_SELECTED + description;
            dictionariesNamesWithSelectedMark.add(description);
        }

        final CharSequence[] ids = new CharSequence[dictionaryIds.size()];
        final CharSequence[] items = new CharSequence[dictionariesNamesWithSelectedMark.size()];
        dictionariesNamesWithSelectedMark.toArray(items);
        dictionaryIds.toArray(ids);

        showOptionsDialogWithData(getString(R.string.override_dictionary_title, getCurrentAlphabetKeyboard().getKeyboardName()), R.drawable.ic_settings_language,
                items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int position) {
                        di.dismiss();
                        Editor editor = getSharedPrefs().edit();
                        if (position == 0) {
                            editor.remove(dictionaryOverridingKey);
                            showToastMessage(R.string.override_disabled, true);
                        } else {
                            CharSequence id = ids[position];
                            String selectedDictionaryId = (id == null) ? null : id.toString();
                            editor.putString(dictionaryOverridingKey, selectedDictionaryId);
                            showToastMessage(getString(R.string.override_enabled, dictionariesNamesForToast.get(position)), true);
                        }
                        SharedPreferencesCompat.EditorCompat.getInstance().apply(editor);
                        //override will be automatically done in the prefs callback.
                    }
                });
    }

    private void showOptionsMenu() {
        showOptionsDialogWithData(getText(R.string.ime_name), R.mipmap.ic_ask_launcher,
                new CharSequence[]{
                        getText(R.string.ime_settings),
                        getText(R.string.override_dictionary),
                        getText(R.string.change_ime)},
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface di, int position) {
                        switch (position) {
                            case 0:
                                launchSettings();
                                break;
                            case 1:
                                launchDictionaryOverriding();
                                break;
                            case 2:
                                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
                                break;
                        }
                    }
                }
        );
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mOrientation) {
            mOrientation = newConfig.orientation;
            setInitialCondensedState(newConfig);

            abortCorrectionAndResetPredictionState(false);

            String sentenceSeparatorsForCurrentKeyboard = getKeyboardSwitcher().getCurrentKeyboardSentenceSeparators();
            if (sentenceSeparatorsForCurrentKeyboard == null) {
                mSentenceSeparators.clear();
            } else {
                fillSeparatorsSparseArray(mSentenceSeparators, sentenceSeparatorsForCurrentKeyboard.toCharArray());
            }
        }
    }

    private void setInitialCondensedState(Configuration newConfig) {
        final String defaultCondensed = mAskPrefs.getInitialKeyboardCondenseState();
        mKeyboardInCondensedMode = CondenseType.None;
        switch (defaultCondensed) {
            case "split_always":
                mKeyboardInCondensedMode = CondenseType.Split;
                break;
            case "split_in_landscape":
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
                    mKeyboardInCondensedMode = CondenseType.Split;
                else
                    mKeyboardInCondensedMode = CondenseType.None;
                break;
            case "compact_right_always":
                mKeyboardInCondensedMode = CondenseType.CompactToRight;
                break;
            case "compact_left_always":
                mKeyboardInCondensedMode = CondenseType.CompactToLeft;
                break;
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        AnyApplication.requestBackupToCloud();

        onLoadSettingsRequired(sharedPreferences);

        if (key.startsWith(KeyboardAddOnAndBuilder.KEYBOARD_PREF_PREFIX) && key.endsWith(PREFS_KEY_POSTFIX_OVERRIDE_DICTIONARY)) {
            setDictionariesForCurrentKeyboard();
        } else if (key.startsWith(KeyboardAddOnAndBuilder.KEYBOARD_PREF_PREFIX) ||
                key.startsWith("dictionary_") ||
                key.equals(getString(R.string.settings_key_active_quick_text_key)) ||
                key.equals(getString(R.string.settings_key_ext_kbd_bottom_row_key)) ||
                key.equals(getString(R.string.settings_key_ext_kbd_top_row_key)) ||
                key.equals(getString(R.string.settings_key_ext_kbd_ext_ketboard_key)) ||
                key.equals(getString(R.string.settings_key_ext_kbd_hidden_bottom_row_key)) ||
                key.equals(getString(R.string.settings_key_keyboard_theme_key)) ||
                key.equals("zoom_factor_keys_in_portrait") ||
                key.equals("zoom_factor_keys_in_landscape") ||
                key.equals(getString(R.string.settings_key_smiley_icon_on_smileys_key)) ||
                key.equals(getString(R.string.settings_key_long_press_timeout)) ||
                key.equals(getString(R.string.settings_key_multitap_timeout)) ||
                key.equals(getString(R.string.settings_key_always_hide_language_key)) ||
                key.equals(getString(R.string.settings_key_default_split_state)) ||
                key.equals(getString(R.string.settings_key_support_password_keyboard_type_state))) {
            //this will recreate the keyboard view AND flush the keyboards cache.
            resetKeyboardView(true);
        }
    }

    public void deleteLastCharactersFromInput(int countToDelete) {
        if (countToDelete == 0)
            return;

        final int currentLength = mWord.length();
        boolean shouldDeleteUsingCompletion;
        if (currentLength > 0) {
            shouldDeleteUsingCompletion = true;
            if (currentLength > countToDelete) {
                int deletesLeft = countToDelete;
                while (deletesLeft > 0) {
                    mWord.deleteLast();
                    deletesLeft--;
                }
            } else {
                mWord.reset();
            }
        } else {
            shouldDeleteUsingCompletion = false;
        }
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            if (mPredictionOn && shouldDeleteUsingCompletion) {
                ic.setComposingText(mWord.getTypedWord()/* mComposing */, 1);
            } else {
                ic.deleteSurroundingText(countToDelete, 0);
            }
        }
    }

    public WordComposer getCurrentWord() {
        return mWord;
    }

    /**
     * Override this to control when the soft input area should be shown to the
     * user. The default implementation only shows the input view when there is
     * no hard keyboard or the keyboard is hidden. If you change what this
     * returns, you will need to call {@link #updateInputViewShown()} yourself
     * whenever the returned value may have changed to have it re-evalauted and
     * applied. This needs to be re-coded for Issue 620
     */
    @SuppressLint("MissingSuperCall")
    @Override
    public boolean onEvaluateInputViewShown() {
        Configuration config = getResources().getConfiguration();
        return config.keyboard == Configuration.KEYBOARD_NOKEYS ||
                config.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES;
    }

    public void onCancel() {
        //the user released their finger outside of any key... okay. I have nothing to do about that.
    }

    public void resetKeyboardView(boolean recreateView) {
        handleClose();
        if (recreateView) {
            // also recreate keyboard view
            setInputView(onCreateInputView());
            setCandidatesView(onCreateCandidatesView());
            setCandidatesViewShown(false);
        }
    }

    private void updateShiftStateNow() {
        final InputConnection ic = getCurrentInputConnection();
        EditorInfo ei = getCurrentInputEditorInfo();
        final int caps;
        if (mAutoCap && ic != null && ei != null && ei.inputType != EditorInfo.TYPE_NULL) {
            caps = ic.getCursorCapsMode(ei.inputType);
        } else {
            caps = 0;
        }
        final boolean inputSaysCaps = caps != 0;
        Logger.d(TAG, "shift updateShiftStateNow inputSaysCaps=%s", inputSaysCaps);
        mShiftKeyState.setActiveState(inputSaysCaps);
        handleShift();
    }

    /*package*/ void closeDictionaries() {
        mSuggest.closeDictionaries();
    }
}
