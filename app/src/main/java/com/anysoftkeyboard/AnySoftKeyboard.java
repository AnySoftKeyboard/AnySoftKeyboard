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
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.dictionaries.WordComposer;
import com.anysoftkeyboard.ime.AnySoftKeyboardIncognito;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.CondenseType;
import com.anysoftkeyboard.keyboards.Keyboard.Key;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher.NextKeyboardType;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.powersave.PowerSaving;
import com.anysoftkeyboard.prefs.AnimationsLevel;
import com.anysoftkeyboard.receivers.PackagesChangedReceiver;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.ui.VoiceInputNotInstalledActivity;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.anysoftkeyboard.utils.ChewbaccaOnTheDrums;
import com.anysoftkeyboard.utils.IMEUtil;
import com.anysoftkeyboard.utils.Triple;
import com.google.android.voiceime.VoiceRecognitionTrigger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;

/**
 * Input method implementation for QWERTY-ish keyboard.
 */
public abstract class AnySoftKeyboard extends AnySoftKeyboardIncognito {

    private static final long ONE_FRAME_DELAY = 1000L / 60L;
    private static final long CLOSE_DICTIONARIES_DELAY = 10 * ONE_FRAME_DELAY;
    private static final ExtractedTextRequest EXTRACTED_TEXT_REQUEST = new ExtractedTextRequest();
    private static final long MAX_TIME_TO_EXPECT_SELECTION_UPDATE = 1500;
    private static final int UNDO_COMMIT_NONE = -1;
    private static final int UNDO_COMMIT_WAITING_TO_RECORD_POSITION = -2;

    //a year ago.
    private static final long NEVER_TIME_STAMP = -1L * 365L * 24L * 60L * 60L * 1000L;
    private final KeyboardUIStateHandler mKeyboardHandler = new KeyboardUIStateHandler(this);

    private final PackagesChangedReceiver mPackagesChangedReceiver = new PackagesChangedReceiver(this);
    @NonNull
    private final SparseBooleanArray mSentenceSeparators = new SparseBooleanArray();

    /*package*/ TextView mCandidateCloseText;
    private View mCandidatesParent;
    private CandidateView mCandidateView;
    private static final CompletionInfo[] EMPTY_COMPLETIONS = new CompletionInfo[0];
    @NonNull
    private CompletionInfo[] mCompletions = EMPTY_COMPLETIONS;
    private long mExpectingSelectionUpdateBy = Long.MIN_VALUE;
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    @NonNull
    private CharSequence mCommittedWord = "";
    /*
     * is prediction needed for the current input connection
     */
    private boolean mPredictionOn;
    /*
     * is out-side completions needed
     */
    private boolean mCompletionOn;
    private boolean mAutoSpace;
    private boolean mInputFieldSupportsAutoPick;
    private boolean mAutoCorrectOn;
    private boolean mAllowSuggestionsRestart = true;
    private boolean mCurrentlyAllowSuggestionRestart = true;
    private boolean mJustAutoAddedWord = false;
    private boolean mAutoCap;
    /*
     * Configuration flag. Should we support dictionary suggestions
     */
    private boolean mShowSuggestions = false;
    private boolean mAutoComplete;
    private boolean mShowKeyboardIconInStatusBar;

    /*
     * This will help us find out if UNDO_COMMIT is still possible to be done
     */
    private int mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
    private CondenseType mPrefKeyboardInCondensedLandscapeMode = CondenseType.None;
    private CondenseType mPrefKeyboardInCondensedPortraitMode = CondenseType.None;
    private CondenseType mKeyboardInCondensedMode = CondenseType.None;
    private boolean mAdditionalCharacterForReverting;
    private boolean mLastCharacterWasShifted = false;
    private InputMethodManager mInputMethodManager;
    private VoiceRecognitionTrigger mVoiceRecognitionTrigger;
    private long mLastSpaceTimeStamp = NEVER_TIME_STAMP;
    private View mFullScreenExtractView;
    private EditText mFullScreenExtractTextView;
    private boolean mFrenchSpacePunctuationBehavior;

    private ImageView mCandidatesCloseIcon;

    public AnySoftKeyboard() {
        super();
    }

    //TODO SHOULD NOT USE THIS METHOD AT ALL!
    private static int getCursorPosition(@Nullable InputConnection connection) {
        if (connection == null) {
            return 0;
        }
        ExtractedText extracted = connection.getExtractedText(EXTRACTED_TEXT_REQUEST, 0);
        if (extracted == null) {
            return 0;
        }
        return extracted.startOffset + extracted.selectionStart;
    }

    private static boolean isBackWordDeleteChar(int c) {
        return Character.isLetter(c);
    }

    private static void fillSeparatorsSparseArray(SparseBooleanArray sparseBooleanArray, char[] chars) {
        sparseBooleanArray.clear();
        for (char separator : chars) sparseBooleanArray.put(separator, true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mOrientation = getResources().getConfiguration().orientation;
        if (!BuildConfig.DEBUG && DeveloperUtils.hasTracingRequested(getApplicationContext())) {
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
        if (!BuildConfig.DEBUG && BuildConfig.VERSION_NAME.endsWith("-SNAPSHOT")) {
            throw new RuntimeException("You can not run a 'RELEASE' build with a SNAPSHOT postfix!");
        }

        addDisposable(AnimationsLevel.createPrefsObservable(this).subscribe(animationsLevel -> {
            final int fancyAnimation = getResources().getIdentifier("Animation_InputMethodFancy", "style", "android");
            final Window window = getWindow().getWindow();
            if (window == null) return;

            if (fancyAnimation != 0) {
                Logger.i(TAG, "Found Animation_InputMethodFancy as %d, so I'll use this", fancyAnimation);
                window.setWindowAnimations(fancyAnimation);
            } else {
                Logger.w(TAG, "Could not find Animation_InputMethodFancy, using default animation");
                window.setWindowAnimations(android.R.style.Animation_InputMethod);
            }
        }, GenericOnError.onError("AnimationsLevel")));

        addDisposable(prefs().getString(R.string.settings_key_default_split_state_portrait, R.string.settings_default_default_split_state)
                .asObservable().map(AnySoftKeyboard::parseCondenseType).subscribe(type -> {
                    mPrefKeyboardInCondensedPortraitMode = type;
                    setInitialCondensedState(getResources().getConfiguration());
                }, GenericOnError.onError("settings_key_default_split_state_portrait")));
        addDisposable(prefs().getString(R.string.settings_key_default_split_state_landscape, R.string.settings_default_default_split_state)
                .asObservable().map(AnySoftKeyboard::parseCondenseType).subscribe(type -> {
                    mPrefKeyboardInCondensedLandscapeMode = type;
                    setInitialCondensedState(getResources().getConfiguration());
                }, GenericOnError.onError("settings_key_default_split_state_landscape")));

        setInitialCondensedState(getResources().getConfiguration());

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        // register to receive packages changes
        registerReceiver(mPackagesChangedReceiver, mPackagesChangedReceiver.createIntentFilter());

        addDisposable(prefs().getBoolean(R.string.settings_key_keyboard_icon_in_status_bar, R.bool.settings_default_keyboard_icon_in_status_bar)
                .asObservable().subscribe(aBoolean -> mShowKeyboardIconInStatusBar = aBoolean, GenericOnError.onError("settings_key_keyboard_icon_in_status_bar")));
        addDisposable(prefs().getBoolean(R.string.settings_key_auto_capitalization, R.bool.settings_default_auto_capitalization)
                .asObservable().subscribe(aBoolean -> mAutoCap = aBoolean, GenericOnError.onError("settings_key_auto_capitalization")));
        addDisposable(prefs().getBoolean(R.string.settings_key_allow_suggestions_restart, R.bool.settings_default_allow_suggestions_restart)
                .asObservable().subscribe(aBoolean -> mAllowSuggestionsRestart = aBoolean, GenericOnError.onError("settings_key_allow_suggestions_restart")));

        final Observable<Boolean> powerSavingShowSuggestionsObservable = Observable.combineLatest(
                prefs().getBoolean(R.string.settings_key_show_suggestions, R.bool.settings_default_show_suggestions).asObservable(),
                PowerSaving.observePowerSavingState(getApplicationContext(), R.string.settings_key_power_save_mode_suggestions_control),
                (prefsShowSuggestions, powerSavingState) -> {
                    if (powerSavingState) {
                        return false;
                    } else {
                        return prefsShowSuggestions;
                    }
                });

        addDisposable(
                Observable.combineLatest(
                        powerSavingShowSuggestionsObservable,
                        prefs().getString(R.string.settings_key_auto_pick_suggestion_aggressiveness, R.string.settings_default_auto_pick_suggestion_aggressiveness).asObservable(),
                        prefs().getInteger(R.string.settings_key_min_length_for_word_correction__, R.integer.settings_default_min_word_length_for_suggestion).asObservable(),
                        Triple::new)
                        .subscribe(triple -> {
                            final boolean showSuggestionsChanged = mShowSuggestions != triple.getFirst();
                            mShowSuggestions = triple.getFirst();
                            final String autoPickAggressiveness = triple.getSecond();

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
                            mSuggest.setCorrectionMode(mShowSuggestions,
                                    calculatedCommonalityMaxLengthDiff, calculatedCommonalityMaxDistance,
                                    triple.getThird());
                            //starting over
                            TextEntryState.newSession(mShowSuggestions && mPredictionOn);
                            if (showSuggestionsChanged) {
                                if (mShowSuggestions) {
                                    setDictionariesForCurrentKeyboard();
                                } else {
                                    closeDictionaries();
                                }
                            }
                        }, GenericOnError.onError("combineLatest settings_key_show_suggestions")));

        mVoiceRecognitionTrigger = new VoiceRecognitionTrigger(this);
    }

    private static CondenseType parseCondenseType(String prefCondenseType) {
        switch (prefCondenseType) {
            case "split":
                return CondenseType.Split;
            case "compact_right":
                return CondenseType.CompactToRight;
            case "compact_left":
                return CondenseType.CompactToLeft;
            default:
                return CondenseType.None;
        }
    }

    @Override
    public void onDestroy() {
        Logger.i(TAG, "AnySoftKeyboard has been destroyed! Cleaning resources..");
        mKeyboardHandler.removeAllMessages();

        unregisterReceiver(mPackagesChangedReceiver);

        final IBinder imeToken = getImeToken();
        if (imeToken != null) mInputMethodManager.hideStatusIcon(imeToken);

        hideWindow();

        mSuggest.destroy();

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

        getInputView().resetInputView();
        getInputView().setKeyboardActionType(attribute.imeOptions);

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = EMPTY_COMPLETIONS;
        mInputFieldSupportsAutoPick = false;

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
                final int textVariation = attribute.inputType & EditorInfo.TYPE_MASK_VARIATION;
                switch (textVariation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_PASSWORD:
                    case EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                        Logger.d(TAG, "A password TYPE_CLASS_TEXT input with no prediction");
                        mPredictionOn = false;
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case EditorInfo.TYPE_TEXT_VARIATION_URI:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        Logger.d(TAG, "An internet input with has prediction but no auto-pick");
                        mPredictionOn = true;
                        mInputFieldSupportsAutoPick = false;
                        break;
                    default:
                        mInputFieldSupportsAutoPick = true;
                        mPredictionOn = true;
                }

                switch (textVariation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case EditorInfo.TYPE_TEXT_VARIATION_URI:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        mAutoSpace = false;
                        break;
                    default:
                        mAutoSpace = mPrefsAutoSpace;
                }

                final int textFlag = attribute.inputType & EditorInfo.TYPE_MASK_FLAGS;
                if ((textFlag & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) == EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS ||
                        (textFlag & EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) == EditorInfo.TYPE_TEXT_FLAG_AUTO_COMPLETE) {
                    Logger.d(TAG, "Input requested NO_SUGGESTIONS, or it is AUTO_COMPLETE by itself.");
                    mPredictionOn = false;
                }

                switch (textVariation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        Logger.d(TAG, "Setting INPUT_MODE_EMAIL as keyboard due to a TYPE_TEXT_VARIATION_EMAIL_ADDRESS input.");
                        getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_EMAIL, attribute, restarting);
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_URI:
                        Logger.d(TAG, "Setting INPUT_MODE_URL as keyboard due to a TYPE_TEXT_VARIATION_URI input.");
                        getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_URL, attribute, restarting);
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
                mAutoSpace = mPrefsAutoSpace;
                getKeyboardSwitcher().setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, attribute, restarting);
        }

        mAdditionalCharacterForReverting = false;

        setCandidatesViewShown(false);

        mPredictionOn = mPredictionOn && mShowSuggestions;
        TextEntryState.newSession(mPredictionOn);

        clearSuggestions();

        updateShiftStateNow();
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        mPredictionOn = false;

        final IBinder imeToken = getImeToken();
        if (mShowKeyboardIconInStatusBar && imeToken != null) {
            mInputMethodManager.hideStatusIcon(imeToken);
        }
        mKeyboardHandler.sendEmptyMessageDelayed(KeyboardUIStateHandler.MSG_CLOSE_DICTIONARIES, CLOSE_DICTIONARIES_DELAY);

        final InputViewBinder inputView = getInputView();
        if (inputView != null) inputView.resetInputView();
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

        if (TextEntryState.willUndoCommitOnBackspace() && mUndoCommitCursorPosition == oldSelStart && mUndoCommitCursorPosition != newSelStart) {
            Logger.d(TAG, "onUpdateSelection: I am in a state that is position sensitive but the user moved the cursor, so it is not possible to undo_commit now.");
            abortCorrectionAndResetPredictionState(false);
        }

        if (!isPredictionOn()) {
            return;// not relevant if no prediction is needed.
        }

        final InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return;// well, I can't do anything without this connection
        }

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
                postRestartWordSuggestion();
            }
        }
    }

    private void postRestartWordSuggestion() {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS);
        mKeyboardHandler.sendEmptyMessageDelayed(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS, 10 * ONE_FRAME_DELAY);
    }

    private boolean canRestartWordSuggestion() {
        final InputViewBinder inputView = getInputView();
        if (TextEntryState.isPredicting() || !isPredictionOn() || !mAllowSuggestionsRestart
                || !mCurrentlyAllowSuggestionRestart || inputView == null
                || !inputView.isShown()) {
            // why?
            // mPredicting - if I'm predicting a word, I can not restart it..
            // right? I'm inside that word!
            // isPredictionOn() - this is obvious.
            // mAllowSuggestionsRestart - config settings
            // mCurrentlyAllowSuggestionRestart - workaround for
            // onInputStart(restarting == true)
            // mInputView == null - obvious, no?
            Logger.d(TAG, "performRestartWordSuggestion: no need to restart: mPredicting=%s, isPredictionOn=%s, mAllowSuggestionsRestart=%s, mCurrentlyAllowSuggestionRestart=%s",
                    TextEntryState.isPredicting(), isPredictionOn(), mAllowSuggestionsRestart, mCurrentlyAllowSuggestionRestart);
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
            ic.setComposingRegion(mGlobalCursorPosition - toLeft.length(), mGlobalCursorPosition + toRight.length());

            ic.endBatchEdit();
            postUpdateSuggestions();
        } else {
            Logger.d(TAG, "performRestartWordSuggestion canRestartWordSuggestion == false");
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
            mCompletions = copyCompletionsFromAndroid(completions);
            mCompletionOn = true;
            if (mCompletions.length == 0) {
                clearSuggestions();
            } else {
                List<CharSequence> stringList = new ArrayList<>();
                for (CompletionInfo ci : mCompletions) {
                    if (ci != null) stringList.add(ci.getText());
                }
                // CharSequence typedWord = mWord.getTypedWord();
                setSuggestions(stringList, true, true, true);
                mWord.setPreferredWord(null);
                // I mean, if I'm here, it must be shown...
                setCandidatesViewShown(true);
            }
        }
    }

    @NonNull
    private static CompletionInfo[] copyCompletionsFromAndroid(@Nullable CompletionInfo[] completions) {
        if (completions == null) {
            return new CompletionInfo[0];
        } else {
            return Arrays.copyOf(completions, completions.length);
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

        mCandidateCloseText = view.findViewById(R.id.close_suggestions_strip_text);
        mCandidatesCloseIcon = view.findViewById(R.id.close_suggestions_strip_icon);

        mCandidateView = view.findViewById(R.id.candidates);
        mCandidateView.setService(this);
        setCandidatesViewShown(false);

        mCandidatesCloseIcon.setOnClickListener(new OnClickListener() {
            // two seconds is enough.
            private static final long DOUBLE_TAP_TIMEOUT = 2 * 1000 - 50;

            @Override
            public void onClick(View v) {
                mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT);
                mCandidateCloseText.setVisibility(View.VISIBLE);
                mCandidateCloseText.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.close_candidates_hint_in));
                mKeyboardHandler.sendMessageDelayed(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT), DOUBLE_TAP_TIMEOUT);
            }
        });
        mCandidateCloseText.setOnClickListener(v -> {
            mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_REMOVE_CLOSE_SUGGESTIONS_HINT);
            mCandidateCloseText.setVisibility(View.GONE);
            abortCorrectionAndResetPredictionState(true);
        });
    }

    @Override
    protected void onThemeChanged(@NonNull KeyboardTheme theme) {
        super.onThemeChanged(theme);
        if (mCandidateView == null) return;

        mCandidatesCloseIcon.setImageDrawable(mCandidateView.getCloseIcon());
        mCandidateCloseText.setTextColor(mCandidateView.getTextOthersColor());
        mCandidateCloseText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandidateView.getTextSize());
    }

    private void clearSuggestions() {
        setSuggestions(Collections.emptyList(), false, false, false);
    }

    @Override
    public void setSuggestions(@NonNull List<? extends CharSequence> suggestions,
            boolean completions, boolean typedWordValid,
            boolean haveMinimalSuggestion) {
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions,
                    typedWordValid, haveMinimalSuggestion && isAutoCorrect());
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

        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                return mUseFullScreenInputInLandscape;
            default:
                return mUseFullScreenInputInPortrait;
        }
    }

    private void setKeyboardStatusIcon() {
        AnyKeyboard alphabetKeyboard = getCurrentAlphabetKeyboard();
        final IBinder imeToken = getImeToken();
        if (mShowKeyboardIconInStatusBar && alphabetKeyboard != null && imeToken != null) {
            mInputMethodManager.showStatusIcon(imeToken,
                    alphabetKeyboard.getKeyboardContext().getPackageName(),
                    alphabetKeyboard.getKeyboardIconResId());
        }
    }

    private void checkAddToDictionaryWithAutoDictionary(WordComposer suggestion, Suggest.AdditionType type) {
        mJustAutoAddedWord = false;
        if (suggestion == null || suggestion.length() < 1 || !mShowSuggestions) {
            return;
        }

        final String newWord = suggestion.getTypedWord().toString();

        mInputSessionDisposables.add(
                Observable.just(Pair.create(newWord, type))
                        .subscribeOn(RxSchedulers.background())
                        .map(pair -> Pair.create(mSuggest.tryToLearnNewWord(pair.first, pair.second), pair.first))
                        .filter(pair -> pair.first)
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(pair -> {
                                    addWordToDictionary(pair.second);
                                    TextEntryState.acceptedSuggestionAddedToDictionary();
                                    mJustAutoAddedWord = true;
                                },
                                e -> Logger.w(TAG, e, "Failed to try-lean word '%s'!", newWord)));
    }

    public void addWordToDictionary(String word) {
        mInputSessionDisposables.add(
                Observable.just(word)
                        .subscribeOn(RxSchedulers.background())
                        .map(mSuggest::addWordToUserDictionary)
                        .filter(added -> added)
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(added -> {
                                    if (mCandidateView != null) {
                                        mCandidateView.notifyAboutWordAdded(word);
                                    }
                                },
                                e -> Logger.w(TAG, e, "Failed to add word '%s' to user-dictionary!", word)));
    }

    public void removeFromUserDictionary(String wordToRemove) {
        mInputSessionDisposables.add(
                Observable.just(wordToRemove)
                        .subscribeOn(RxSchedulers.background())
                        .map(word -> {
                            mSuggest.removeWordFromUserDictionary(word);
                            return word;
                        })
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(word -> {
                                    if (mCandidateView != null) {
                                        mCandidateView.notifyAboutRemovedWord(word);
                                    }
                                },
                                e -> Logger.w(TAG, e, "Failed to remove word '%s' from user-dictionary!", wordToRemove)));
        mJustAutoAddedWord = false;
        abortCorrectionAndResetPredictionState(false);
    }

    /**
     * Helper to determine if a given character code is alphabetic.
     */
    @Override
    protected boolean isAlphabet(int code) {
        if (super.isAlphabet(code)) return true;
        // inner letters have more options: ' in English. " in Hebrew, and more.
        if (TextEntryState.isPredicting()) {
            return getCurrentAlphabetKeyboard().isInnerWordLetter((char) code);
        } else {
            return getCurrentAlphabetKeyboard().isStartOfWordLetter((char) code);
        }
    }

    @Override
    protected boolean isSuggestionAffectingCharacter(int code) {
        return super.isSuggestionAffectingCharacter(code) || Character.isLetter((char) code);
    }

    @Override
    public void onMultiTapStarted() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }
        handleDeleteLastCharacter(true);
        if (getInputView() != null) {
            getInputView().setShifted(mLastCharacterWasShifted);
        }
    }

    @Override
    public void onMultiTapEnded() {
        final InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.endBatchEdit();
        }
        updateShiftStateNow();
    }

    private void onFunctionKey(final int primaryCode, final Key key, final int multiTapIndex, final int[] nearByKeyCodes, final boolean fromUI) {
        if (BuildConfig.DEBUG) Logger.d(TAG, "onFunctionKey %d", primaryCode);

        final InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case KeyCodes.DELETE:
                if (ic != null) {
                    // we do backword if the shift is pressed while pressing
                    // backspace (like in a PC)
                    if (mUseBackWord && mShiftKeyState.isPressed() && !mShiftKeyState.isLocked()) {
                        handleBackWord(ic);
                    } else {
                        handleDeleteLastCharacter(false);
                    }
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
                if (ic != null) {
                    handleBackWord(ic);
                }
                break;
            case KeyCodes.FORWARD_DELETE:
                if (ic != null) {
                    handleForwardDelete(ic);
                }
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
                            if (newPosition < 0) {
                                newPosition = 0;
                            }
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
                            if (newPosition > textAfter.length()) {
                                newPosition = textAfter.length();
                            }
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
                if (!handleCloseRequest()) {
                    hideWindow();
                }
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
                final InputViewBinder inputViewForUtilityKeyboardRequest = getInputView();
                if (inputViewForUtilityKeyboardRequest instanceof AnyKeyboardView) {
                    ((AnyKeyboardView) inputViewForUtilityKeyboardRequest).openUtilityKeyboard();
                }
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
            case KeyCodes.UNDO:
            case KeyCodes.REDO:
                handleClipboardOperation(key, primaryCode, ic);
                //not allowing undo on-text in clipboard paste operations.
                if (primaryCode == KeyCodes.CLIPBOARD_PASTE) mCommittedWord = "";
                break;
            case KeyCodes.DISABLED:
                Logger.d(TAG, "Disabled key was pressed.");
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
                    if (mSwitchKeyboardOnSpace) {
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
                    mAdditionalCharacterForReverting = false;
                }
                break;
        }
    }

    @Override
    public void onKey(int primaryCode, Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);

        if (primaryCode > 0) {
            onNonFunctionKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
        } else {
            onFunctionKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
        }

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
        if (ic == null) {
            return;
        }
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
        if (ic == null) {
            return;
        }
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
        setKeyboardFinalStuff();
        mFrenchSpacePunctuationBehavior = mSwapPunctuationAndSpace && keyboard.getLocale().toString().toLowerCase(Locale.US).startsWith("fr");
    }

    @Override
    protected void setKeyboardForView(@NonNull AnyKeyboard currentKeyboard) {
        currentKeyboard.setCondensedKeys(mKeyboardInCondensedMode);
        super.setKeyboardForView(currentKeyboard);
    }

    private void showLanguageSelectionDialog() {
        List<KeyboardAddOnAndBuilder> builders = getKeyboardSwitcher().getEnabledKeyboardsBuilders();
        ArrayList<CharSequence> keyboardsIds = new ArrayList<>();
        ArrayList<CharSequence> keyboards = new ArrayList<>();
        // going over all enabled keyboards
        for (KeyboardAddOnAndBuilder keyboardBuilder : builders) {
            keyboardsIds.add(keyboardBuilder.getId());
            CharSequence name = keyboardBuilder.getName();

            keyboards.add(name);
        }

        // An extra item for the settings line
        final CharSequence[] ids = new CharSequence[keyboardsIds.size() + 1];
        final CharSequence[] items = new CharSequence[keyboards.size() + 1];
        keyboardsIds.toArray(ids);
        keyboards.toArray(items);
        final String SETTINGS_ID = "ASK_LANG_SETTINGS_ID";
        ids[ids.length - 1] = SETTINGS_ID;
        items[ids.length - 1] = getText(R.string.setup_wizard_step_three_action_languages);

        showOptionsDialogWithData(getText(R.string.select_keyboard_popup_title), R.drawable.ic_keyboard_globe_menu,
                items, (di, position) -> {
                    CharSequence id = ids[position];
                    Logger.d(TAG, "User selected '%s' with id %s", items[position], id);
                    EditorInfo currentEditorInfo = getCurrentInputEditorInfo();
                    if (SETTINGS_ID.equals(id.toString())) {
                        startActivity(new Intent(getApplicationContext(), MainSettingsActivity.class)
                                .putExtra(MainSettingsActivity.EXTRA_KEY_APP_SHORTCUT_ID, "keyboards")
                                .setAction(Intent.ACTION_VIEW)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } else {
                        getKeyboardSwitcher().nextAlphabetKeyboard(currentEditorInfo, id.toString());
                    }
                });
    }

    @Override
    public View onCreateExtractTextView() {
        mFullScreenExtractView = super.onCreateExtractTextView();
        if (mFullScreenExtractView != null) {
            mFullScreenExtractTextView = mFullScreenExtractView.findViewById(android.R.id.inputExtractEditText);
        }

        return mFullScreenExtractView;
    }

    @Override
    public void updateFullscreenMode() {
        super.updateFullscreenMode();
        InputViewBinder inputViewBinder = getInputView();
        if (mFullScreenExtractView != null && inputViewBinder != null) {
            final AnyKeyboardView anyKeyboardView = (AnyKeyboardView) inputViewBinder;
            ViewCompat.setBackground(mFullScreenExtractView, anyKeyboardView.getBackground());
            if (mFullScreenExtractTextView != null) {
                mFullScreenExtractTextView.setTextColor(anyKeyboardView.getCurrentResourcesHolder().getKeyTextColor());
            }
        }
    }

    @Override
    public void onText(Key key, CharSequence text) {
        Logger.d(TAG, "onText: '%s'", text);
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return;
        }
        ic.beginBatchEdit();

        abortCorrectionAndResetPredictionState(false);
        ic.commitText(text, 1);

        mAdditionalCharacterForReverting = false;
        mCommittedWord = text;
        mUndoCommitCursorPosition = UNDO_COMMIT_WAITING_TO_RECORD_POSITION;

        TextEntryState.acceptedDefault(text);
        ic.endBatchEdit();

        setSuggestions(mSuggest.getNextSuggestions(mCommittedWord, false), false, false, false);
    }

    @Override
    protected void handleBackWord(InputConnection ic) {
        if (ic == null) {
            return;
        }

        if (TextEntryState.isPredicting() && mWord.cursorPosition() > 0 && mWord.length() > 0) {
            //sp#ace -> ace
            //cursor == 2
            //length == 5
            //textAfterCursor = word.substring(2, 3) -> word.substring(cursor, length - cursor)
            final CharSequence textAfterCursor = mWord.getTypedWord().subSequence(mWord.cursorPosition(), mWord.length());
            mWord.reset();
            mSuggest.resetNextWordSentence();
            TextEntryState.newSession(mPredictionOn);
            ic.setComposingText(textAfterCursor, 0);
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
        int idx = inputLength - 1;// it's OK since we checked whether cs is empty after retrieving it.
        if (isBackWordDeleteChar((int) cs.charAt(idx))) {
            while (idx > 0 && isBackWordDeleteChar((int) cs.charAt(idx - 1))) {
                idx--;
            }
        }
        ic.deleteSurroundingText(inputLength - idx, 0);// it is always > 0 !
    }

    private void handleDeleteLastCharacter(boolean forMultiTap) {
        InputConnection ic = getCurrentInputConnection();
        final boolean isPredicting = TextEntryState.isPredicting();
        final TextEntryState.State newState = TextEntryState.backspace();

        if (isPredicting) {
            final boolean wordManipulation = mWord.length() > 0 && mWord.cursorPosition() > 0;
            if (wordManipulation) {
                mWord.deleteLast();
                final int cursorPosition;
                if (mWord.cursorPosition() != mWord.length()) {
                    cursorPosition = getCursorPosition(ic);
                } else {
                    cursorPosition = -1;
                }

                if (cursorPosition >= 0) {
                    ic.beginBatchEdit();
                }

                ic.setComposingText(mWord.getTypedWord(), 1);
                if (mWord.length() == 0) {
                    TextEntryState.newSession(mPredictionOn);
                } else if (cursorPosition >= 0) {
                    ic.setSelection(cursorPosition - 1, cursorPosition - 1);
                }

                if (cursorPosition >= 0) {
                    ic.endBatchEdit();
                }

                postUpdateSuggestions();
            } else {
                ic.deleteSurroundingText(1, 0);
            }
        } else if (newState == TextEntryState.State.UNDO_COMMIT) {
            revertLastWord();
        } else {
            //just making sure that
            if (mCandidateView != null) mCandidateView.dismissAddToDictionaryHint();

            if (!forMultiTap) {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
            } else {
                if (ic == null) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                } else {
                    // this code tries to delete the text in a different way,
                    // because of multi-tap stuff
                    // using "deleteSurroundingText" will actually get the input
                    // updated faster!
                    // but will not handle "delete all selected text" feature,
                    // hence the "if (!forMultiTap)" above
                    final CharSequence beforeText = ic.getTextBeforeCursor(1, 0);
                    final int textLengthBeforeDelete = TextUtils.isEmpty(beforeText) ? 0 : beforeText.length();
                    if (textLengthBeforeDelete > 0) {
                        ic.deleteSurroundingText(1, 0);
                    } else {
                        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
                    }
                }
            }
        }
    }


    private void handleForwardDelete(InputConnection ic) {
        final boolean isPredicting = TextEntryState.isPredicting();

        if (isPredicting) {
            final boolean wordManipulation = mWord.length() > 0 && mWord.cursorPosition() > 0;
            if (wordManipulation) {
                mWord.deleteForward();
                final int cursorPosition;
                if (mWord.cursorPosition() != mWord.length()) {
                    cursorPosition = getCursorPosition(ic);
                } else {
                    cursorPosition = -1;
                }

                if (cursorPosition >= 0) {
                    ic.beginBatchEdit();
                }

                ic.setComposingText(mWord.getTypedWord(), 1);
                if (mWord.length() == 0) {
                    TextEntryState.newSession(mPredictionOn);
                } else if (cursorPosition >= 0) {
                    ic.setSelection(cursorPosition, cursorPosition);
                }

                if (cursorPosition >= 0) {
                    ic.endBatchEdit();
                }

                postUpdateSuggestions();
            } else {
                ic.deleteSurroundingText(0, 1);
            }
        } else {
            //just making sure that
            if (mCandidateView != null) mCandidateView.dismissAddToDictionaryHint();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_FORWARD_DEL);
            } else {
                ic.deleteSurroundingText(0, 1);
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

    private void toggleCaseOfSelectedCharacters() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) return;

        ExtractedText et = ic.getExtractedText(EXTRACTED_TEXT_REQUEST, 0);
        if (et == null) return;
        int selectionStart = et.selectionStart;
        int selectionEnd = et.selectionEnd;

        if (et.text == null) return;
        CharSequence selectedText = et.text.subSequence(selectionStart, selectionEnd);
        if (selectedText == null) return;

        if (selectedText.length() > 0) {
            ic.beginBatchEdit();
            String selectedTextString = selectedText.toString();
            if (selectedTextString.compareTo(selectedTextString.toUpperCase(getCurrentAlphabetKeyboard().getLocale())) == 0) {
                // Convert to lower case
                ic.setComposingText(selectedTextString.toLowerCase(getCurrentAlphabetKeyboard().getLocale()), 0);
            } else {
                // Convert to upper case
                ic.setComposingText(selectedTextString.toUpperCase(getCurrentAlphabetKeyboard().getLocale()), 0);
            }
            ic.endBatchEdit();
            ic.setSelection(selectionStart, selectionEnd);
        }
    }

    @Override
    protected void abortCorrectionAndResetPredictionState(boolean forever) {
        super.abortCorrectionAndResetPredictionState(forever);
        mJustAutoAddedWord = false;
        mKeyboardHandler.removeAllSuggestionMessages();

        final InputConnection ic = getCurrentInputConnection();
        if (ic != null) ic.finishComposingText();

        clearSuggestions();

        mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
        mCommittedWord = "";
        mWord.reset();
        mAdditionalCharacterForReverting = false;
        mJustAutoAddedWord = false;
        if (forever) {
            Logger.d(TAG, "abortCorrection will abort correct forever");
            mPredictionOn = false;
            setCandidatesViewShown(false);
        }
        TextEntryState.newSession(mPredictionOn && !forever);
    }

    private void handleCharacter(final int primaryCode, final Key key, final int multiTapIndex, int[] nearByKeyCodes) {
        if (BuildConfig.DEBUG) {
            Logger.d(TAG, "handleCharacter: %d, isPredictionOn: %s, mPredicting: %s", primaryCode, isPredictionOn(), TextEntryState.isPredicting());
        }

        mExpectingSelectionUpdateBy = SystemClock.uptimeMillis() + MAX_TIME_TO_EXPECT_SELECTION_UPDATE;
        if (TextEntryState.isReadyToPredict() && isAlphabet(primaryCode) && !isCursorTouchingWord()) {
            TextEntryState.newSession(mPredictionOn);
            mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
            mWord.reset();
            mAutoCorrectOn = mAutoComplete && mInputFieldSupportsAutoPick;
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
                if (mCandidateView != null) {
                    mCandidateView.replaceTypedWord(mWord.getTypedWord());
                }
            }
        } else {
            sendKeyChar((char) primaryCode);
        }
        mJustAutoAddedWord = false;
    }

    private void handleSeparator(int primaryCode) {
        // Issue 146: Right to left languages require reversed parenthesis
        if (!getCurrentAlphabetKeyboard().isLeftToRightLanguage()) {
            if (primaryCode == (int) ')') {
                primaryCode = (int) '(';
            } else if (primaryCode == (int) '(') {
                primaryCode = (int) ')';
            }
        }
        mExpectingSelectionUpdateBy = SystemClock.uptimeMillis() + MAX_TIME_TO_EXPECT_SELECTION_UPDATE;
        //will not show next-word suggestion in case of a new line or if the separator is a sentence separator.
        boolean newLine = primaryCode == KeyCodes.ENTER;
        boolean isEndOfSentence = (newLine || mSentenceSeparators.get(primaryCode));
        boolean isSpace = primaryCode == KeyCodes.SPACE;

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
            pickDefaultSuggestion(isAutoCorrect() && !newLine);
            // Picked the suggestion by a space/punctuation character: we will treat it
            // as "added an auto space".
            mAdditionalCharacterForReverting = !newLine;
        } else if (separatorInsideWord) {
            // when putting a separator in the middle of a word, there is no
            // need to do correction, or keep knowledge
            abortCorrectionAndResetPredictionState(false);
        }

        boolean handledOutputToInputConnection = false;

        if (ic != null) {
            if (isSpace) {
                if (mIsDoubleSpaceChangesToPeriod && (SystemClock.uptimeMillis() - mLastSpaceTimeStamp) < mMultiTapTimeout) {
                    //current text in the input-box should be something like "word "
                    //the user pressed on space again. So we want to change the text in the input-box
                    //into "word "->"word. "
                    ic.deleteSurroundingText(1, 0);
                    ic.commitText(". ", 1);
                    mAdditionalCharacterForReverting = true;
                    isEndOfSentence = true;
                    handledOutputToInputConnection = true;
                }
            } else if (mAdditionalCharacterForReverting && mLastSpaceTimeStamp != NEVER_TIME_STAMP/*meaning last key was SPACE*/ &&
                    (mSwapPunctuationAndSpace || newLine) &&
                    isSpaceSwapCharacter(primaryCode)) {
                //current text in the input-box should be something like "word "
                //the user pressed a punctuation (say ","). So we want to change the text in the input-box
                //into "word "->"word, "
                ic.deleteSurroundingText(1, 0);
                ic.commitText(((char) primaryCode) + (newLine ? "" : " "), 1);
                mAdditionalCharacterForReverting = !newLine;
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

    private boolean isSpaceSwapCharacter(int primaryCode) {
        if (isSentenceSeparator(primaryCode)) {
            if (mFrenchSpacePunctuationBehavior) {
                switch (primaryCode) {
                    case '!':
                    case '?':
                    case ':':
                    case ';':
                        return false;
                    default:
                        return true;
                }
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    @Override
    protected boolean handleCloseRequest() {
        return super.handleCloseRequest() || (getInputView() != null && getInputView().resetInputView());
    }

    @Override
    public void onWindowHidden() {
        super.onWindowHidden();

        abortCorrectionAndResetPredictionState(true);
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
        if (delay > 0) {
            mKeyboardHandler.sendMessageDelayed(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS), delay);
        } else if (delay == 0) {
            mKeyboardHandler.sendMessage(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS));
        } else {
            performUpdateSuggestions();
        }
    }

    @VisibleForTesting
    protected boolean isPredictionOn() {
        return mPredictionOn;
    }

    @VisibleForTesting
    protected boolean isAutoCorrect() {
        return mAutoCorrectOn && mInputFieldSupportsAutoPick && mPredictionOn;
    }

    private boolean shouldCandidatesStripBeShown() {
        return mShowSuggestions && onEvaluateInputViewShown();
    }

    /*package*/ void performUpdateSuggestions() {
        //mCandidateCloseText could be null if setCandidatesView was not called yet
        if (mCandidateCloseText != null) mCandidateCloseText.setVisibility(View.GONE);

        if (!TextEntryState.isPredicting() || !mShowSuggestions) {
            clearSuggestions();
            return;
        }

        final CharSequence typedWord = mWord.getTypedWord();

        final List<CharSequence> suggestionsList = mSuggest.getSuggestions(mWord, false);
        boolean correctionAvailable = mSuggest.hasMinimalCorrection();
        final boolean typedWordValid = mSuggest.isValidWord(typedWord) && !mWord.isAtTagsSearchState();

        if (mShowSuggestions) {
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

        if (!TextUtils.isEmpty(actualWordToOutput)) {
            TextEntryState.acceptedDefault(typedWord);
            final boolean fixed = !TextUtils.equals(typedWord, actualWordToOutput);
            commitWordToInput(actualWordToOutput, fixed);
            if (!fixed) {//if the word typed was auto-replaced, we should not learn it.
                // Add the word to the auto dictionary if it's not a known word
                // this is "typed" if the auto-correction is off, or "picked" if it is on or momentarily off.
                checkAddToDictionaryWithAutoDictionary(mWord, mAutoComplete ? Suggest.AdditionType.Picked : Suggest.AdditionType.Typed);
            }
            return true;
        }
        return false;
    }

    public void pickSuggestionManually(int index, CharSequence suggestion) {
        pickSuggestionManually(index, suggestion, mAutoSpace);
    }

    @Override
    public void pickSuggestionManually(int index, CharSequence suggestion, boolean withAutoSpaceEnabled) {
        super.pickSuggestionManually(index, suggestion, withAutoSpaceEnabled);
        final String typedWord = mWord.getTypedWord().toString();

        if (mWord.isAtTagsSearchState()) {
            if (index == 0) {
                //this is a special case for tags-searcher
                //since we append a magnifying glass to the suggestions, the "suggestion"
                //value is not a valid output suggestion
                suggestion = typedWord;
            } else {
                //regular emoji. Storing in history.
                getQuickKeyHistoryRecords().store(suggestion.toString(), suggestion.toString());
            }
        }

        final InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }

        TextEntryState.acceptedSuggestion(typedWord, suggestion);

        try {
            if (mCompletionOn && index >= 0 && index < mCompletions.length) {
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
            if (withAutoSpaceEnabled && (index == 0 || !mWord.isAtTagsSearchState())) {
                sendKeyChar((char) KeyCodes.SPACE);
                mAdditionalCharacterForReverting = true;
                setSpaceTimeStamp(true);
                TextEntryState.typedCharacter(' ', true);
            }
            // Add the word to the auto dictionary if it's not a known word
            mJustAutoAddedWord = false;

            if (!mWord.isAtTagsSearchState()) {
                if (index == 0) {
                    checkAddToDictionaryWithAutoDictionary(mWord, Suggest.AdditionType.Picked);
                }

                final boolean showingAddToDictionaryHint =
                        !mJustAutoAddedWord
                                && index == 0
                                && mShowSuggestions
                                && !mSuggest.isValidWord(suggestion)// this is for the case that the word was auto-added upon picking
                                && !mSuggest.isValidWord(suggestion.toString().toLowerCase(getCurrentAlphabetKeyboard().getLocale()));

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

    @Override
    protected void commitWordToInput(@NonNull CharSequence wordToCommit, boolean correcting) {
        super.commitWordToInput(wordToCommit, correcting);
        mWord.setPreferredWord(wordToCommit);
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            if (correcting) {
                AnyApplication.getDeviceSpecific().commitCorrectionToInputConnection(ic, mGlobalCursorPosition - mWord.getTypedWord().length(), mWord.getTypedWord(), mWord.getPreferredWord());
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
        if (ic == null) {
            return false;
        }

        CharSequence toLeft = ic.getTextBeforeCursor(1, 0);
        // It is not exactly clear to me why, but sometimes, although I request
        // 1 character, I get
        // the entire text. This causes me to incorrectly detect restart
        // suggestions...
        if (!TextUtils.isEmpty(toLeft) && toLeft.length() == 1 && !isWordSeparator(toLeft.charAt(0))) {
            return true;
        }

        CharSequence toRight = ic.getTextAfterCursor(1, 0);
        return !TextUtils.isEmpty(toRight) &&
                toRight.length() == 1 &&
                !isWordSeparator(toRight.charAt(0));
    }

    public void revertLastWord() {
        final int length = mCommittedWord.length() + (mAdditionalCharacterForReverting ? 1 : 0);
        if (length > 0) {
            mAutoCorrectOn = false;
            //note: typedWord may be empty
            final InputConnection ic = getCurrentInputConnection();
            mUndoCommitCursorPosition = UNDO_COMMIT_NONE;
            ic.setComposingRegion(mGlobalCursorPosition - length, mGlobalCursorPosition);
            final CharSequence typedWord = mWord.getTypedWord();
            ic.setComposingText(typedWord/* mComposing */, 1);
            TextEntryState.backspace();
            performUpdateSuggestions();
            if (mJustAutoAddedWord) {
                removeFromUserDictionary(typedWord.toString());
            }
            revertLastPopText();
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

    private void setKeyboardFinalStuff() {
        mShiftKeyState.reset();
        mControlKeyState.reset();
        // changing dictionary
        setDictionariesForCurrentKeyboard();
        // Notifying if needed
        setKeyboardStatusIcon();
        clearSuggestions();
        mKeyboardHandler.removeAllSuggestionMessages();
        updateShiftStateNow();
    }

    private void sendKeyDown(InputConnection ic, int key) {
        if (ic != null) ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, key));
    }

    private void sendKeyUp(InputConnection ic, int key) {
        if (ic != null) ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, key));
    }

    @Override
    public void onPress(int primaryCode) {
        super.onPress(primaryCode);
        InputConnection ic = getCurrentInputConnection();

        if (primaryCode == KeyCodes.SHIFT) {
            mShiftKeyState.onPress();
            // Toggle case on selected characters
            toggleCaseOfSelectedCharacters();
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
    }

    @Override
    public void onRelease(int primaryCode) {
        InputConnection ic = getCurrentInputConnection();
        if (primaryCode == KeyCodes.SHIFT) {
            mShiftKeyState.onRelease(mMultiTapTimeout, mLongPressTimeout);
            handleShift();
        } else {
            if (mShiftKeyState.onOtherKeyReleased()) {
                updateShiftStateNow();
            }
        }

        if (primaryCode == KeyCodes.CTRL) {
            sendKeyUp(ic, 113); // KeyEvent.KEYCODE_CTRL_LEFT
            mControlKeyState.onRelease(mMultiTapTimeout, mLongPressTimeout);
            handleControl();
        } else {
            mControlKeyState.onOtherKeyReleased();
        }
    }

    private void setDictionariesForCurrentKeyboard() {
        mSuggest.resetNextWordSentence();

        if (mPredictionOn) {
            // It null at the creation of the application.
            final AnyKeyboard currentAlphabetKeyboard = getCurrentAlphabetKeyboard();
            if (currentAlphabetKeyboard != null && isInAlphabetKeyboardMode()) {
                fillSeparatorsSparseArray(mSentenceSeparators, currentAlphabetKeyboard.getSentenceSeparators());
                //ensure NEW-LINE is there
                mSentenceSeparators.put(KeyCodes.ENTER, true);

                List<DictionaryAddOnAndBuilder> buildersForKeyboard = AnyApplication.getExternalDictionaryFactory(this).getBuildersForKeyboard(currentAlphabetKeyboard);

                mSuggest.setupSuggestionsForKeyboard(buildersForKeyboard, getDictionaryLoadedListener(currentAlphabetKeyboard));
            }
        }
    }

    private void launchSettings() {
        hideWindow();
        Intent intent = new Intent();
        intent.setClass(AnySoftKeyboard.this, MainSettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void launchDictionaryOverriding() {
        final List<DictionaryAddOnAndBuilder> buildersForKeyboard =
                AnyApplication.getExternalDictionaryFactory(this).getBuildersForKeyboard(getCurrentAlphabetKeyboard());
        final List<DictionaryAddOnAndBuilder> allBuilders = AnyApplication.getExternalDictionaryFactory(this).getAllAddOns();

        final CharSequence[] items = new CharSequence[allBuilders.size()];
        final boolean[] checked = new boolean[items.length];

        for (int dictionaryIndex = 0; dictionaryIndex < allBuilders.size(); dictionaryIndex++) {
            DictionaryAddOnAndBuilder dictionaryBuilder = allBuilders.get(dictionaryIndex);
            String description = dictionaryBuilder.getName().toString();
            if (!TextUtils.isEmpty(dictionaryBuilder.getDescription())) {
                description += " (" + dictionaryBuilder.getDescription() + ")";
            }
            items[dictionaryIndex] = description;
            checked[dictionaryIndex] = buildersForKeyboard.contains(dictionaryBuilder);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_settings_language);
        builder.setTitle(getString(R.string.override_dictionary_title, getCurrentAlphabetKeyboard().getKeyboardName()));

        builder.setMultiChoiceItems(items, checked, (dialogInterface, i, b) -> checked[i] = b);

        builder.setNegativeButton(android.R.string.cancel, (di, position) -> di.cancel());
        builder.setPositiveButton(R.string.label_done_key, (di, position) -> {
            List<DictionaryAddOnAndBuilder> newBuildersForKeyboard = new ArrayList<>(buildersForKeyboard.size());
            for (int itemIndex = 0; itemIndex < allBuilders.size(); itemIndex++) {
                if (checked[itemIndex]) {
                    newBuildersForKeyboard.add(allBuilders.get(itemIndex));
                }
            }

            AnyApplication.getExternalDictionaryFactory(AnySoftKeyboard.this).setBuildersForKeyboard(getCurrentAlphabetKeyboard(), newBuildersForKeyboard);

            di.dismiss();
        });
        builder.setNeutralButton(R.string.clear_all_dictionary_override, (dialogInterface, i) ->
                AnyApplication.getExternalDictionaryFactory(AnySoftKeyboard.this)
                        .setBuildersForKeyboard(getCurrentAlphabetKeyboard(), Collections.emptyList()));

        showNewOptionDialog(builder.create());
    }

    private void showOptionsMenu() {
        showOptionsDialogWithData(getText(R.string.ime_name), R.mipmap.ic_launcher,
                new CharSequence[]{
                        getText(R.string.ime_settings),
                        getText(R.string.override_dictionary),
                        getText(R.string.change_ime),
                        getString(R.string.switch_incognito_template, getText(R.string.switch_incognito))},
                (di, position) -> {
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
                        case 3:
                            setIncognito(!mSuggest.isIncognitoMode(), true);
                            break;
                        default:
                            throw new IllegalArgumentException("Position " + position + " is not covered by the ASK settings dialog.");
                    }
                }
        );
    }

    @Override
    @NonNull
    protected List<Drawable> generateWatermark() {
        final List<Drawable> watermark = super.generateWatermark();
        if (mSuggest.isIncognitoMode()) {
            watermark.add(ContextCompat.getDrawable(this, R.drawable.ic_watermark_incognito));
        }
        return watermark;
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

    private void setInitialCondensedState(Configuration configuration) {
        final CondenseType previousCondenseType = mKeyboardInCondensedMode;
        mKeyboardInCondensedMode = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ?
                mPrefKeyboardInCondensedLandscapeMode : mPrefKeyboardInCondensedPortraitMode;

        if (previousCondenseType != mKeyboardInCondensedMode) {
            getKeyboardSwitcher().flushKeyboardsCache();
            hideWindow();
        }
    }

    @Override
    public void onSharedPreferenceChange(String key) {
        if (ExternalDictionaryFactory.isOverrideDictionaryPrefKey(key)) {
            setDictionariesForCurrentKeyboard();
        } else {
            super.onSharedPreferenceChange(key);
        }
    }

    @Override
    public void deleteLastCharactersFromInput(int countToDelete) {
        if (countToDelete == 0) {
            return;
        }

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
