package com.anysoftkeyboard.ime;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ImageView;
import android.widget.TextView;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.dictionaries.WordComposer;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.powersave.PowerSaving;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.utils.ChewbaccaOnTheDrums;
import com.anysoftkeyboard.utils.Triple;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;

public abstract class AnySoftKeyboardSuggestions extends AnySoftKeyboardKeyboardSwitchedListener {

    private static final long CLOSE_DICTIONARIES_DELAY = 10 * ONE_FRAME_DELAY;

    private static final long MAX_TIME_TO_EXPECT_SELECTION_UPDATE = 1500;
    //a year ago.
    private static final long NEVER_TIME_STAMP = -1L * 365L * 24L * 60L * 60L * 1000L;


    private static final int UNDO_COMMIT_NONE = -1;
    private static final int UNDO_COMMIT_WAITING_TO_RECORD_POSITION = -2;

    private static final DictionaryBackgroundLoader.Listener NO_OP_DICTIONARY_LOADER_LISTENER = new DictionaryBackgroundLoader.Listener() {

        @Override
        public void onDictionaryLoadingStarted(Dictionary dictionary) {
        }

        @Override
        public void onDictionaryLoadingDone(Dictionary dictionary) {
        }

        @Override
        public void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception) {
        }
    };

    private final KeyboardUIStateHandler mKeyboardHandler = new KeyboardUIStateHandler(this);

    private Suggest mSuggest;

    @NonNull
    private final SparseBooleanArray mSentenceSeparators = new SparseBooleanArray();

    /*package*/ TextView mCandidateCloseText;
    private View mCandidatesParent;
    private CandidateView mCandidateView;
    private ImageView mCandidatesCloseIcon;
    private static final CompletionInfo[] EMPTY_COMPLETIONS = new CompletionInfo[0];
    @NonNull
    private CompletionInfo[] mCompletions = EMPTY_COMPLETIONS;
    @NonNull
    private CharSequence mCommittedWord = "";

    private long mLastSpaceTimeStamp = NEVER_TIME_STAMP;

    private boolean mLastCharacterWasShifted = false;
    private boolean mFrenchSpacePunctuationBehavior;

    /*
     * This will help us find out if UNDO_COMMIT is still possible to be done
     */
    private int mUndoCommitCursorPosition = UNDO_COMMIT_NONE;

    private long mExpectingSelectionUpdateBy = Long.MIN_VALUE;
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
    protected boolean mAdditionalCharacterForReverting;

    private boolean mJustAutoAddedWord = false;
    /*
     * Configuration flag. Should we support dictionary suggestions
     */
    private boolean mShowSuggestions = false;
    private boolean mAutoComplete;
    private int mOrientation;

    @Override
    public void onCreate() {
        super.onCreate();

        mOrientation = getResources().getConfiguration().orientation;

        mSuggest = createSuggest();

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mKeyboardHandler.removeAllMessages();
        mSuggest.destroy();
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
    }

    @Override
    public void onStartInputView(final EditorInfo attribute, final boolean restarting) {
        super.onStartInputView(attribute, restarting);

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
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        mPredictionOn = false;
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

        if (mUndoCommitCursorPosition == UNDO_COMMIT_WAITING_TO_RECORD_POSITION) {
            Logger.d(TAG, "onUpdateSelection: I am in ACCEPTED_DEFAULT state, time to store the position - I can only undo-commit from here.");
            mUndoCommitCursorPosition = newSelStart;
        }

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

    @Override
    @CallSuper
    public void onKey(int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        if (mCandidateView != null) mCandidateView.dismissAddToDictionaryHint();
    }

    @Override
    public void onRelease(int primaryCode) {
        //not allowing undo on-text in clipboard paste operations.
        if (primaryCode == KeyCodes.CLIPBOARD_PASTE) mCommittedWord = "";
        setSpaceTimeStamp(primaryCode == KeyCodes.SPACE);
    }
    
    private void postRestartWordSuggestion() {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS);
        mKeyboardHandler.sendEmptyMessageDelayed(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS, 10 * ONE_FRAME_DELAY);
    }

    @Override
    @CallSuper
    public void onMultiTapStarted() {
        final InputViewBinder inputView = getInputView();
        if (inputView != null) {
            inputView.setShifted(mLastCharacterWasShifted);
        }
    }

    protected void handleCharacter(final int primaryCode, final Keyboard.Key key, final int multiTapIndex, int[] nearByKeyCodes) {
        if (BuildConfig.DEBUG) {
            Logger.d(TAG, "handleCharacter: %d, isPredictionOn: %s, mPredicting: %s", primaryCode, isPredictionOn(), TextEntryState.isPredicting());
        }

        mExpectingSelectionUpdateBy = SystemClock.uptimeMillis() + MAX_TIME_TO_EXPECT_SELECTION_UPDATE;
        if (TextEntryState.isReadyToPredict() && isAlphabet(primaryCode) && !isCursorTouchingWord()) {
            TextEntryState.newSession(isPredictionOn());
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

    protected void handleSeparator(int primaryCode) {
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
        boolean isEndOfSentence = newLine || isSentenceSeparator(primaryCode);
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
    public void onText(Keyboard.Key key, CharSequence text) {
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

    protected void setDictionariesForCurrentKeyboard() {
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

    @NonNull
    protected DictionaryBackgroundLoader.Listener getDictionaryLoadedListener(@NonNull AnyKeyboard currentAlphabetKeyboard) {
        return NO_OP_DICTIONARY_LOADER_LISTENER;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mOrientation) {
            mOrientation = newConfig.orientation;

            abortCorrectionAndResetPredictionState(false);

            String sentenceSeparatorsForCurrentKeyboard = getKeyboardSwitcher().getCurrentKeyboardSentenceSeparators();
            if (sentenceSeparatorsForCurrentKeyboard == null) {
                mSentenceSeparators.clear();
            } else {
                fillSeparatorsSparseArray(mSentenceSeparators, sentenceSeparatorsForCurrentKeyboard.toCharArray());
            }
        }
    }

    private static void fillSeparatorsSparseArray(SparseBooleanArray sparseBooleanArray, char[] chars) {
        sparseBooleanArray.clear();
        for (char separator : chars) sparseBooleanArray.put(separator, true);
    }

    @CallSuper
    protected void abortCorrectionAndResetPredictionState(boolean forever) {
        mSuggest.resetNextWordSentence();

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

    protected boolean canRestartWordSuggestion() {
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

    protected void clearSuggestions() {
        mKeyboardHandler.removeAllSuggestionMessages();
        setSuggestions(Collections.emptyList(), false, false, false);
    }

    protected void setSuggestions(@NonNull List<? extends CharSequence> suggestions,
            boolean completions, boolean typedWordValid,
            boolean haveMinimalSuggestion) {
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions,
                    typedWordValid, haveMinimalSuggestion && isAutoCorrect());
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateCandidatesView() {
        return getLayoutInflater().inflate(R.layout.candidates, null);
    }

    @NonNull
    protected Suggest getSuggest() {
        return mSuggest;
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

    @NonNull
    protected Suggest createSuggest() {
        return new Suggest(this);
    }

    protected abstract boolean isAlphabet(int code);

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


    protected void postUpdateSuggestions() {
        postUpdateSuggestions(5 * ONE_FRAME_DELAY);
    }

    /**
     * posts an update suggestions request to the messages queue. Removes any previous request.
     *
     * @param delay negative value will cause the call to be done now, in this thread.
     */
    protected void postUpdateSuggestions(long delay) {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);
        if (delay > 0) {
            mKeyboardHandler.sendMessageDelayed(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS), delay);
        } else if (delay == 0) {
            mKeyboardHandler.sendMessage(mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS));
        } else {
            performUpdateSuggestions();
        }
    }

    protected boolean isPredictionOn() {
        return mPredictionOn;
    }

    protected boolean isAutoCorrect() {
        return mAutoCorrectOn && mInputFieldSupportsAutoPick && mPredictionOn;
    }

    private boolean shouldCandidatesStripBeShown() {
        return mShowSuggestions && onEvaluateInputViewShown();
    }

    public void performUpdateSuggestions() {
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

    @CallSuper
    public void pickSuggestionManually(int index, CharSequence suggestion, boolean withAutoSpaceEnabled) {
        final String typedWord = mWord.getTypedWord().toString();

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

    /**
     * Commits the chosen word to the text field and saves it for later
     * retrieval.
     *
     * @param wordToCommit the suggestion picked by the user to be committed to the text
     *                     field
     * @param correcting   this is a correction commit
     */
    @CallSuper
    protected void commitWordToInput(@NonNull CharSequence wordToCommit, boolean correcting) {
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

    private void setSpaceTimeStamp(boolean isSpace) {
        if (isSpace) {
            mLastSpaceTimeStamp = SystemClock.uptimeMillis();
        } else {
            mLastSpaceTimeStamp = NEVER_TIME_STAMP;
        }
    }

    @Override
    public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
        super.onAlphabetKeyboardSet(keyboard);

        final Locale locale = keyboard.getLocale();
        mFrenchSpacePunctuationBehavior = mSwapPunctuationAndSpace && locale != null && locale.toString().toLowerCase(Locale.US).startsWith("fr");
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
        } else {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        }
    }

    protected boolean isSentenceSeparator(int code) {
        return mSentenceSeparators.get(code, false);
    }

    protected boolean isWordSeparator(int code) {
        return (!isAlphabet(code));
    }

    public boolean preferCapitalization() {
        return mWord.isFirstCharCapitalized();
    }

    public void closeDictionaries() {
        mSuggest.closeDictionaries();
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

        mCandidatesCloseIcon.setOnClickListener(new View.OnClickListener() {
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

    protected void onThemeChanged(@NonNull KeyboardTheme theme) {
        if (mCandidateView == null) return;

        mCandidatesCloseIcon.setImageDrawable(mCandidateView.getCloseIcon());
        mCandidateCloseText.setTextColor(mCandidateView.getTextOthersColor());
        mCandidateCloseText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCandidateView.getTextSize());
    }

    @CallSuper
    protected boolean isSuggestionAffectingCharacter(int code) {
        return Character.isLetter((char) code);
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
}
