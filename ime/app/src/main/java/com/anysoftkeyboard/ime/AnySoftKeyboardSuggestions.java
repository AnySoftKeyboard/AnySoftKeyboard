package com.anysoftkeyboard.ime;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ImageView;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.android.PowerSaving;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.dictionaries.SuggestImpl;
import com.anysoftkeyboard.dictionaries.WordComposer;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.utils.Triple;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class AnySoftKeyboardSuggestions extends AnySoftKeyboardKeyboardSwitchedListener {

    @VisibleForTesting public static final long MAX_TIME_TO_EXPECT_SELECTION_UPDATE = 1500;
    private static final long CLOSE_DICTIONARIES_DELAY = 10 * ONE_FRAME_DELAY;
    private static final long NEVER_TIME_STAMP =
            -1L * 365L * 24L * 60L * 60L * 1000L; // a year ago.
    private static final DictionaryBackgroundLoader.Listener NO_OP_DICTIONARY_LOADER_LISTENER =
            new DictionaryBackgroundLoader.Listener() {

                @Override
                public void onDictionaryLoadingStarted(Dictionary dictionary) {}

                @Override
                public void onDictionaryLoadingDone(Dictionary dictionary) {}

                @Override
                public void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception) {}
            };
    private static final CompletionInfo[] EMPTY_COMPLETIONS = new CompletionInfo[0];
    @VisibleForTesting public static final long GET_SUGGESTIONS_DELAY = 5 * ONE_FRAME_DELAY;

    @VisibleForTesting
    final KeyboardUIStateHandler mKeyboardHandler = new KeyboardUIStateHandler(this);

    @NonNull private final SparseBooleanArray mSentenceSeparators = new SparseBooleanArray();

    protected int mWordRevertLength = 0;
    private WordComposer mWord = new WordComposer();
    private WordComposer mPreviousWord = new WordComposer();
    private Suggest mSuggest;
    private CandidateView mCandidateView;
    @NonNull private CompletionInfo[] mCompletions = EMPTY_COMPLETIONS;
    private long mLastSpaceTimeStamp = NEVER_TIME_STAMP;
    private long mExpectingSelectionUpdateBy = NEVER_TIME_STAMP;
    private boolean mLastCharacterWasShifted = false;
    private boolean mFrenchSpacePunctuationBehavior;
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

    @VisibleForTesting
    final CancelSuggestionsAction mCancelSuggestionsAction =
            new CancelSuggestionsAction(() -> abortCorrectionAndResetPredictionState(true));
    /*
     * Configuration flag. Should we support dictionary suggestions
     */
    private boolean mShowSuggestions = false;
    private boolean mAutoComplete;
    private int mOrientation;

    private static void fillSeparatorsSparseArray(
            SparseBooleanArray sparseBooleanArray, char[] chars) {
        sparseBooleanArray.clear();
        for (char separator : chars) sparseBooleanArray.put(separator, true);
    }

    @NonNull
    private static CompletionInfo[] copyCompletionsFromAndroid(
            @Nullable CompletionInfo[] completions) {
        if (completions == null) {
            return new CompletionInfo[0];
        } else {
            return Arrays.copyOf(completions, completions.length);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mOrientation = getResources().getConfiguration().orientation;

        mSuggest = createSuggest();

        addDisposable(
                prefs().getBoolean(
                                R.string.settings_key_allow_suggestions_restart,
                                R.bool.settings_default_allow_suggestions_restart)
                        .asObservable()
                        .subscribe(
                                aBoolean -> mAllowSuggestionsRestart = aBoolean,
                                GenericOnError.onError("settings_key_allow_suggestions_restart")));

        final Observable<Boolean> powerSavingShowSuggestionsObservable =
                Observable.combineLatest(
                        prefs().getBoolean(
                                        R.string.settings_key_show_suggestions,
                                        R.bool.settings_default_show_suggestions)
                                .asObservable(),
                        PowerSaving.observePowerSavingState(
                                getApplicationContext(),
                                R.string.settings_key_power_save_mode_suggestions_control),
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
                                prefs().getString(
                                                R.string
                                                        .settings_key_auto_pick_suggestion_aggressiveness,
                                                R.string
                                                        .settings_default_auto_pick_suggestion_aggressiveness)
                                        .asObservable(),
                                prefs().getInteger(
                                                R.string
                                                        .settings_key_min_length_for_word_correction__,
                                                R.integer
                                                        .settings_default_min_word_length_for_suggestion)
                                        .asObservable(),
                                Triple::new)
                        .subscribe(
                                triple -> {
                                    final boolean showSuggestionsChanged =
                                            mShowSuggestions != triple.getFirst();
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
                                    mSuggest.setCorrectionMode(
                                            mShowSuggestions,
                                            calculatedCommonalityMaxLengthDiff,
                                            calculatedCommonalityMaxDistance,
                                            triple.getThird());
                                    // starting over
                                    if (showSuggestionsChanged) {
                                        if (mShowSuggestions) {
                                            setDictionariesForCurrentKeyboard();
                                        } else {
                                            closeDictionaries();
                                        }
                                    }
                                },
                                GenericOnError.onError(
                                        "combineLatest settings_key_show_suggestions")));
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
        // removing close request (if it was asked for a previous onFinishInput).
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
                Logger.d(
                        TAG,
                        "Setting INPUT_MODE_DATETIME as keyboard due to a TYPE_CLASS_DATETIME input.");
                getKeyboardSwitcher()
                        .setKeyboardMode(
                                KeyboardSwitcher.INPUT_MODE_DATETIME, attribute, restarting);
                break;
            case EditorInfo.TYPE_CLASS_NUMBER:
                Logger.d(
                        TAG,
                        "Setting INPUT_MODE_NUMBERS as keyboard due to a TYPE_CLASS_NUMBER input.");
                getKeyboardSwitcher()
                        .setKeyboardMode(
                                KeyboardSwitcher.INPUT_MODE_NUMBERS, attribute, restarting);
                break;
            case EditorInfo.TYPE_CLASS_PHONE:
                Logger.d(
                        TAG,
                        "Setting INPUT_MODE_PHONE as keyboard due to a TYPE_CLASS_PHONE input.");
                getKeyboardSwitcher()
                        .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_PHONE, attribute, restarting);
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
                if ((textFlag & EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                        == EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS) {
                    Logger.d(TAG, "Input requested NO_SUGGESTIONS.");
                    mPredictionOn = false;
                }

                switch (textVariation) {
                    case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                    case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        Logger.d(
                                TAG,
                                "Setting INPUT_MODE_EMAIL as keyboard due to a TYPE_TEXT_VARIATION_EMAIL_ADDRESS input.");
                        getKeyboardSwitcher()
                                .setKeyboardMode(
                                        KeyboardSwitcher.INPUT_MODE_EMAIL, attribute, restarting);
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_URI:
                        Logger.d(
                                TAG,
                                "Setting INPUT_MODE_URL as keyboard due to a TYPE_TEXT_VARIATION_URI input.");
                        getKeyboardSwitcher()
                                .setKeyboardMode(
                                        KeyboardSwitcher.INPUT_MODE_URL, attribute, restarting);
                        break;
                    case EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
                        Logger.d(
                                TAG,
                                "Setting INPUT_MODE_IM as keyboard due to a TYPE_TEXT_VARIATION_SHORT_MESSAGE input.");
                        getKeyboardSwitcher()
                                .setKeyboardMode(
                                        KeyboardSwitcher.INPUT_MODE_IM, attribute, restarting);
                        break;
                    default:
                        Logger.d(
                                TAG, "Setting INPUT_MODE_TEXT as keyboard due to a default input.");
                        getKeyboardSwitcher()
                                .setKeyboardMode(
                                        KeyboardSwitcher.INPUT_MODE_TEXT, attribute, restarting);
                }

                break;
            default:
                Logger.d(TAG, "Setting INPUT_MODE_TEXT as keyboard due to a default input.");
                // No class. Probably a console window, or no GUI input connection
                mPredictionOn = false;
                mAutoSpace = mPrefsAutoSpace;
                getKeyboardSwitcher()
                        .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, attribute, restarting);
        }

        mPredictionOn = mPredictionOn && mShowSuggestions;

        if (isPredictionOn()) {
            getInputViewContainer().setActionsStripVisibility(true);
            getInputViewContainer().addStripAction(mCancelSuggestionsAction);
        } else {
            getInputViewContainer().setActionsStripVisibility(false);
        }
        clearSuggestions();
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();
        mPredictionOn = false;
        mKeyboardHandler.sendEmptyMessageDelayed(
                KeyboardUIStateHandler.MSG_CLOSE_DICTIONARIES, CLOSE_DICTIONARIES_DELAY);
        mExpectingSelectionUpdateBy = NEVER_TIME_STAMP;
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        abortCorrectionAndResetPredictionState(true);
    }

    /*
     * this function is called EVERY TIME them selection is changed. This also
     * includes the underlined suggestions.
     */
    @Override
    public void onUpdateSelection(
            int oldSelStart,
            int oldSelEnd,
            int newSelStart,
            int newSelEnd,
            int candidatesStart,
            int candidatesEnd) {
        super.onUpdateSelection(
                oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        Logger.v(
                TAG,
                "onUpdateSelection: word '%s', position %d.",
                mWord.getTypedWord(),
                mWord.cursorPosition());
        final boolean isExpectedEvent = SystemClock.uptimeMillis() < mExpectingSelectionUpdateBy;
        mExpectingSelectionUpdateBy = NEVER_TIME_STAMP;

        if (isExpectedEvent) {
            Logger.v(TAG, "onUpdateSelection: Expected event. Discarding.");
            return;
        }

        final boolean cursorMovedUnexpectedly =
                (oldSelStart != newSelStart || oldSelEnd != newSelEnd);
        if (cursorMovedUnexpectedly) {
            mLastSpaceTimeStamp = NEVER_TIME_STAMP;
            if (shouldRevertOnDelete()) {
                Logger.d(
                        TAG,
                        "onUpdateSelection: user moved cursor from a undo-commit sensitive position. Will not be able to undo-commit.");
                mWordRevertLength = 0;
            }
        }

        if (!isPredictionOn()) {
            return; // not relevant if no prediction is needed.
        }

        final InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return; // well, I can't do anything without this connection
        }

        Logger.d(TAG, "onUpdateSelection: ok, let's see what can be done");

        if (newSelStart != newSelEnd) {
            // text selection. can't predict in this mode
            Logger.d(TAG, "onUpdateSelection: text selection.");
            abortCorrectionAndResetPredictionState(false);
        } else if (cursorMovedUnexpectedly) {
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
            if (isCurrentlyPredicting()) {
                if (newSelStart >= candidatesStart && newSelStart <= candidatesEnd) {
                    // 1) predicting and moved inside the word - just update the
                    // cursor position and shift state
                    // inside the currently typed word
                    Logger.d(
                            TAG,
                            "onUpdateSelection: inside the currently typed word to location %d.",
                            newSelEnd - candidatesStart);
                    mWord.setCursorPosition(newSelEnd - candidatesStart);
                } else {
                    Logger.d(
                            TAG,
                            "onUpdateSelection: cursor moving outside the currently predicting word");
                    abortCorrectionAndResetPredictionState(false);
                    postRestartWordSuggestion();
                }
            } else {
                Logger.d(
                        TAG,
                        "onUpdateSelection: not predicting at this moment, maybe the cursor is now at a new word?");
                postRestartWordSuggestion();
            }
        } else {
            Logger.v(TAG, "onUpdateSelection: cursor moved expectedly");
        }
    }

    @Override
    public View onCreateInputView() {
        final View view = super.onCreateInputView();
        mCandidateView = getInputViewContainer().getCandidateView();
        mCandidateView.setService(this);
        mCandidateView.setCloseIconChangedListener(mCancelSuggestionsAction);
        return view;
    }

    protected WordComposer getCurrentComposedWord() {
        return mWord;
    }

    @Override
    @CallSuper
    public void onKey(
            int primaryCode,
            Keyboard.Key key,
            int multiTapIndex,
            int[] nearByKeyCodes,
            boolean fromUI) {
        if (primaryCode != KeyCodes.DELETE) {
            mWordRevertLength = 0;
        }
        mCandidateView.dismissAddToDictionaryHint();
    }

    @Override
    public void onRelease(int primaryCode) {
        // not allowing undo on-text in clipboard paste operations.
        if (primaryCode == KeyCodes.CLIPBOARD_PASTE) mWordRevertLength = 0;
        setSpaceTimeStamp(primaryCode == KeyCodes.SPACE);
        if (!isCurrentlyPredicting()
                && (primaryCode == KeyCodes.DELETE
                        || primaryCode == KeyCodes.DELETE_WORD
                        || primaryCode == KeyCodes.FORWARD_DELETE)) {
            postRestartWordSuggestion();
        }
    }

    private void postRestartWordSuggestion() {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS);
        mKeyboardHandler.sendEmptyMessageDelayed(
                KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS, 10 * ONE_FRAME_DELAY);
    }

    @Override
    @CallSuper
    public void onMultiTapStarted() {
        final InputViewBinder inputView = getInputView();
        if (inputView != null) {
            inputView.setShifted(mLastCharacterWasShifted);
        }
    }

    @Override
    protected boolean isSelectionUpdateDelayed() {
        return mExpectingSelectionUpdateBy > 0;
    }

    protected boolean shouldRevertOnDelete() {
        return mWordRevertLength > 0;
    }

    protected void handleCharacter(
            final int primaryCode,
            final Keyboard.Key key,
            final int multiTapIndex,
            int[] nearByKeyCodes) {
        if (BuildConfig.DEBUG) {
            Logger.d(
                    TAG,
                    "handleCharacter: %d, isPredictionOn: %s, isCurrentlyPredicting: %s",
                    primaryCode,
                    isPredictionOn(),
                    isCurrentlyPredicting());
        }

        if (mWord.charCount() == 0 && isAlphabet(primaryCode)) {
            mWordRevertLength = 0;
            mWord.reset();
            mAutoCorrectOn = isPredictionOn() && mAutoComplete && mInputFieldSupportsAutoPick;
            if (mShiftKeyState.isActive()) {
                mWord.setFirstCharCapitalized(true);
            }
        }

        mLastCharacterWasShifted = (getInputView() != null) && getInputView().isShifted();

        final InputConnection ic = getCurrentInputConnection();
        mWord.add(primaryCode, nearByKeyCodes);
        if (isPredictionOn()) {
            if (ic != null) {
                int newCursorPosition;
                if (mWord.cursorPosition() != mWord.charCount()) {
                    /* Cursor is not at the end of the word. I'll need to reposition.
                    The code for tracking the current position is split among several files and difficult to debug.
                    This has been proven to work in every case: */
                    if (multiTapIndex > 0) {
                        final int previousKeyCode = key.getMultiTapCode(multiTapIndex - 1);
                        newCursorPosition =
                                Character.charCount(primaryCode)
                                        - Character.charCount(previousKeyCode);
                    } else {
                        newCursorPosition = Character.charCount(primaryCode);
                    }
                    newCursorPosition += getCursorPosition();
                    ic.beginBatchEdit();
                } else {
                    newCursorPosition = -1;
                }

                markExpectingSelectionUpdate();
                ic.setComposingText(mWord.getTypedWord(), 1);
                if (newCursorPosition > 0) {
                    ic.setSelection(newCursorPosition, newCursorPosition);
                    ic.endBatchEdit();
                }
            }
            // this should be done ONLY if the key is a letter, and not a inner
            // character (like ').
            if (isSuggestionAffectingCharacter(primaryCode)) {
                postUpdateSuggestions();
            } else {
                // just replace the typed word in the candidates view
                mCandidateView.replaceTypedWord(mWord.getTypedWord());
            }
        } else {
            if (ic != null) {
                ic.beginBatchEdit();
            }
            markExpectingSelectionUpdate();
            for (char c : Character.toChars(primaryCode)) {
                sendKeyChar(c);
            }
            if (ic != null) {
                ic.endBatchEdit();
            }
        }
        mJustAutoAddedWord = false;
    }

    // Make sure to call this BEFORE actually making changes, and not after.
    // the event might arrive immediately as changes occur.
    protected void markExpectingSelectionUpdate() {
        mExpectingSelectionUpdateBy =
                SystemClock.uptimeMillis() + MAX_TIME_TO_EXPECT_SELECTION_UPDATE;
    }

    protected void handleSeparator(int primaryCode) {
        performUpdateSuggestions();
        // Issue 146: Right to left languages require reversed parenthesis
        if (!getCurrentAlphabetKeyboard().isLeftToRightLanguage()) {
            if (primaryCode == (int) ')') {
                primaryCode = (int) '(';
            } else if (primaryCode == (int) '(') {
                primaryCode = (int) ')';
            }
        }
        // will not show next-word suggestion in case of a new line or if the separator is a
        // sentence separator.
        final boolean wasPredicting = isCurrentlyPredicting();
        final boolean newLine = primaryCode == KeyCodes.ENTER;
        boolean isEndOfSentence = newLine || isSentenceSeparator(primaryCode);
        final boolean isSpace = primaryCode == KeyCodes.SPACE;

        // Handle separator
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }
        final WordComposer typedWord = prepareWordComposerForNextWord();
        CharSequence wordToOutput = typedWord.getTypedWord();
        // ACTION does not invoke default picking. See
        // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/198
        if (isAutoCorrect() && !newLine /*we do not auto-pick on ENTER.*/) {
            if (!TextUtils.equals(wordToOutput, typedWord.getPreferredWord())) {
                wordToOutput = typedWord.getPreferredWord();
            }
        }
        // this is a special case, when the user presses a separator WHILE
        // inside the predicted word.
        // in this case, I will want to just dump the separator.
        final boolean separatorInsideWord = (typedWord.cursorPosition() < typedWord.charCount());
        if (wasPredicting && !separatorInsideWord) {
            commitWordToInput(wordToOutput, typedWord.getTypedWord());
            if (TextUtils.equals(typedWord.getTypedWord(), wordToOutput)) {
                // if the word typed was auto-replaced, we should not learn it.
                // Add the word to the auto dictionary if it's not a known word
                // this is "typed" if the auto-correction is off, or "picked" if it is on or
                // momentarily off.
                checkAddToDictionaryWithAutoDictionary(
                        wordToOutput, SuggestImpl.AdditionType.Typed);
            }
            // Picked the suggestion by a space/punctuation character: we will treat it
            // as "added an auto space".
            mWordRevertLength = wordToOutput.length() + 1;
        } else if (separatorInsideWord) {
            // when putting a separator in the middle of a word, there is no
            // need to do correction, or keep knowledge
            abortCorrectionAndResetPredictionState(false);
        }

        boolean handledOutputToInputConnection = false;

        if (ic != null) {
            if (isSpace) {
                if (mIsDoubleSpaceChangesToPeriod
                        && (SystemClock.uptimeMillis() - mLastSpaceTimeStamp) < mMultiTapTimeout) {
                    // current text in the input-box should be something like "word "
                    // the user pressed on space again. So we want to change the text in the
                    // input-box
                    // into "word "->"word. "
                    ic.deleteSurroundingText(1, 0);
                    ic.commitText(". ", 1);
                    isEndOfSentence = true;
                    handledOutputToInputConnection = true;
                }
            } else if (mLastSpaceTimeStamp
                            != NEVER_TIME_STAMP /*meaning the previous key was SPACE*/
                    && (mSwapPunctuationAndSpace || newLine)
                    && isSpaceSwapCharacter(primaryCode)) {
                // current text in the input-box should be something like "word "
                // the user pressed a punctuation (say ","). So we want to change the text in the
                // input-box
                // into "word "->"word, "
                ic.deleteSurroundingText(1, 0);
                ic.commitText(new String(new int[] {primaryCode}, 0, 1) + (newLine ? "" : " "), 1);
                handledOutputToInputConnection = true;
            }
        }

        if (!handledOutputToInputConnection) {
            for (char c : Character.toChars(primaryCode)) {
                sendKeyChar(c);
            }
        }

        markExpectingSelectionUpdate();

        if (ic != null) {
            ic.endBatchEdit();
        }

        if (isEndOfSentence) {
            mSuggest.resetNextWordSentence();
            clearSuggestions();
        } else {
            setSuggestions(
                    mSuggest.getNextSuggestions(wordToOutput, typedWord.isAllUpperCase()),
                    false,
                    false);
        }
    }

    private WordComposer prepareWordComposerForNextWord() {
        if (mWord.isEmpty()) return mWord;

        final WordComposer typedWord = mWord;
        mWord = mPreviousWord;
        mPreviousWord = typedWord;
        mWord.reset(); // re-using
        return typedWord;
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
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS);
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);
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
        if (canRestartWordSuggestion()) { // 2.1
            ic.beginBatchEdit(); // don't want any events till I finish handling
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
            mWord.reset();

            final int[] tempNearByKeys = new int[1];

            int index = 0;
            while (index < word.length()) {
                final int c =
                        Character.codePointAt(word, Character.offsetByCodePoints(word, 0, index));
                if (index == 0) mWord.setFirstCharCapitalized(Character.isUpperCase(c));

                tempNearByKeys[0] = c;
                mWord.add(c, tempNearByKeys);

                index += Character.charCount(c);
            }
            mWord.setCursorPosition(toLeft.length());
            final int globalCursorPosition = getCursorPosition();
            ic.setComposingRegion(
                    globalCursorPosition - toLeft.length(),
                    globalCursorPosition + toRight.length());

            markExpectingSelectionUpdate();
            ic.endBatchEdit();
            performUpdateSuggestions();
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

        // simulating multiple keys
        final WordComposer initialWordComposer = new WordComposer();
        mWord.cloneInto(initialWordComposer);
        abortCorrectionAndResetPredictionState(false);
        ic.commitText(text, 1);

        // this will be the revert
        mWordRevertLength = initialWordComposer.charCount() + text.length();
        mPreviousWord = initialWordComposer;
        markExpectingSelectionUpdate();
        ic.endBatchEdit();
    }

    @Override
    public void onTyping(Keyboard.Key key, CharSequence text) {
        Logger.d(TAG, "onTyping: '%s'", text);
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return;
        }
        ic.beginBatchEdit();

        // simulating multiple keys
        final WordComposer initialWordComposer = new WordComposer();
        mWord.cloneInto(initialWordComposer);
        final boolean originalAutoCorrect = mAutoCorrectOn;
        mAutoCorrectOn = false;
        for (int pointCodeIndex = 0; pointCodeIndex < text.length(); ) {
            int pointCode = Character.codePointAt(text, pointCodeIndex);
            pointCodeIndex += Character.charCount(pointCode);
            // this will ensure that double-spaces will not count.
            mLastSpaceTimeStamp = NEVER_TIME_STAMP;
            // simulating key press
            onKey(pointCode, key, 0, new int[] {pointCode}, true);
        }
        mAutoCorrectOn = originalAutoCorrect;

        ic.endBatchEdit();
    }

    protected void setDictionariesForCurrentKeyboard() {
        mSuggest.resetNextWordSentence();

        if (mPredictionOn) {
            // It null at the creation of the application.
            final AnyKeyboard currentAlphabetKeyboard = getCurrentAlphabetKeyboard();
            if (currentAlphabetKeyboard != null && isInAlphabetKeyboardMode()) {
                fillSeparatorsSparseArray(
                        mSentenceSeparators, currentAlphabetKeyboard.getSentenceSeparators());
                // ensure NEW-LINE is there
                mSentenceSeparators.put(KeyCodes.ENTER, true);

                List<DictionaryAddOnAndBuilder> buildersForKeyboard =
                        AnyApplication.getExternalDictionaryFactory(this)
                                .getBuildersForKeyboard(currentAlphabetKeyboard);

                mSuggest.setupSuggestionsForKeyboard(
                        buildersForKeyboard, getDictionaryLoadedListener(currentAlphabetKeyboard));
            }
        }
    }

    @NonNull
    protected DictionaryBackgroundLoader.Listener getDictionaryLoadedListener(
            @NonNull AnyKeyboard currentAlphabetKeyboard) {
        return NO_OP_DICTIONARY_LOADER_LISTENER;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != mOrientation) {
            mOrientation = newConfig.orientation;

            abortCorrectionAndResetPredictionState(false);

            String sentenceSeparatorsForCurrentKeyboard =
                    getKeyboardSwitcher().getCurrentKeyboardSentenceSeparators();
            if (sentenceSeparatorsForCurrentKeyboard == null) {
                mSentenceSeparators.clear();
            } else {
                fillSeparatorsSparseArray(
                        mSentenceSeparators, sentenceSeparatorsForCurrentKeyboard.toCharArray());
            }
        }
    }

    @CallSuper
    protected void abortCorrectionAndResetPredictionState(boolean disabledUntilNextInputStart) {
        mSuggest.resetNextWordSentence();

        mLastSpaceTimeStamp = NEVER_TIME_STAMP;
        mJustAutoAddedWord = false;
        mKeyboardHandler.removeAllSuggestionMessages();

        final InputConnection ic = getCurrentInputConnection();
        markExpectingSelectionUpdate();
        if (ic != null) ic.finishComposingText();

        clearSuggestions();

        mWord.reset();
        mWordRevertLength = 0;
        mJustAutoAddedWord = false;
        if (disabledUntilNextInputStart) {
            Logger.d(TAG, "abortCorrection will abort correct forever");
            final KeyboardViewContainerView inputViewContainer = getInputViewContainer();
            if (inputViewContainer != null) {
                inputViewContainer.removeStripAction(mCancelSuggestionsAction);
            }
            mPredictionOn = false;
        }
    }

    protected boolean canRestartWordSuggestion() {
        final InputViewBinder inputView = getInputView();
        if (!isPredictionOn()
                || !mAllowSuggestionsRestart
                || !mCurrentlyAllowSuggestionRestart
                || inputView == null
                || !inputView.isShown()) {
            // why?
            // mPredicting - if I'm predicting a word, I can not restart it..
            // right? I'm inside that word!
            // isPredictionOn() - this is obvious.
            // mAllowSuggestionsRestart - config settings
            // mCurrentlyAllowSuggestionRestart - workaround for
            // onInputStart(restarting == true)
            // mInputView == null - obvious, no?
            Logger.d(
                    TAG,
                    "performRestartWordSuggestion: no need to restart: isPredictionOn=%s, mAllowSuggestionsRestart=%s, mCurrentlyAllowSuggestionRestart=%s",
                    isPredictionOn(),
                    mAllowSuggestionsRestart,
                    mCurrentlyAllowSuggestionRestart);
            return false;
        } else if (!isCursorTouchingWord()) {
            Logger.d(TAG, "User moved cursor to no-man land. Bye bye.");
            return false;
        }

        return true;
    }

    protected void clearSuggestions() {
        mKeyboardHandler.removeAllSuggestionMessages();
        setSuggestions(Collections.emptyList(), false, false);
    }

    protected void setSuggestions(
            @NonNull List<? extends CharSequence> suggestions,
            boolean typedWordValid,
            boolean haveMinimalSuggestion) {
        mCancelSuggestionsAction.setCancelIconVisible(!suggestions.isEmpty());
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(
                    suggestions, typedWordValid, haveMinimalSuggestion && isAutoCorrect());
        }
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
        return new SuggestImpl(this);
    }

    protected abstract boolean isAlphabet(int code);

    public void addWordToDictionary(String word) {
        mInputSessionDisposables.add(
                Observable.just(word)
                        .subscribeOn(RxSchedulers.background())
                        .map(mSuggest::addWordToUserDictionary)
                        .filter(added -> added)
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(
                                added -> {
                                    if (mCandidateView != null) {
                                        mCandidateView.notifyAboutWordAdded(word);
                                    }
                                },
                                e ->
                                        Logger.w(
                                                TAG,
                                                e,
                                                "Failed to add word '%s' to user-dictionary!",
                                                word)));
    }

    /** posts an update suggestions request to the messages queue. Removes any previous request. */
    protected void postUpdateSuggestions() {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);
        mKeyboardHandler.sendMessageDelayed(
                mKeyboardHandler.obtainMessage(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS),
                GET_SUGGESTIONS_DELAY);
    }

    protected boolean isPredictionOn() {
        return mPredictionOn;
    }

    protected boolean isCurrentlyPredicting() {
        return isPredictionOn() && !mWord.isEmpty();
    }

    protected boolean isAutoCorrect() {
        return mAutoCorrectOn && mInputFieldSupportsAutoPick && mPredictionOn;
    }

    public void performUpdateSuggestions() {
        mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);

        if (!isPredictionOn() || !mShowSuggestions) {
            clearSuggestions();
            return;
        }

        final CharSequence typedWord = mWord.getTypedWord();

        final List<CharSequence> suggestionsList = mSuggest.getSuggestions(mWord, false);
        boolean correctionAvailable = mSuggest.hasMinimalCorrection();
        final boolean typedWordValid =
                mSuggest.isValidWord(typedWord) && !mWord.isAtTagsSearchState();

        if (mShowSuggestions) {
            correctionAvailable |= typedWordValid;
        }

        // Don't auto-correct words with multiple capital letter
        correctionAvailable &= !mWord.isMostlyCaps();

        setSuggestions(suggestionsList, typedWordValid, correctionAvailable);
        if (suggestionsList.size() > 0) {
            if (correctionAvailable && !typedWordValid && suggestionsList.size() > 1) {
                mWord.setPreferredWord(suggestionsList.get(1));
            } else {
                mWord.setPreferredWord(typedWord);
            }
        } else {
            mWord.setPreferredWord(null);
        }
    }

    public void pickSuggestionManually(int index, CharSequence suggestion) {
        pickSuggestionManually(index, suggestion, mAutoSpace);
    }

    @CallSuper
    public void pickSuggestionManually(
            int index, CharSequence suggestion, boolean withAutoSpaceEnabled) {

        final InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.beginBatchEdit();
        }

        final WordComposer typedWord = prepareWordComposerForNextWord();

        try {
            if (mCompletionOn && index >= 0 && index < mCompletions.length) {
                CompletionInfo ci = mCompletions[index];
                if (ic != null) {
                    ic.commitCompletion(ci);
                }

                if (mCandidateView != null) {
                    mCandidateView.clear();
                }
                return;
            }
            commitWordToInput(
                    suggestion,
                    suggestion /*user physically picked a word from the suggestions strip. this is not a fix*/);

            // Follow it with a space
            if (withAutoSpaceEnabled && (index == 0 || !typedWord.isAtTagsSearchState())) {
                sendKeyChar((char) KeyCodes.SPACE);
                setSpaceTimeStamp(true);
            }
            // Add the word to the auto dictionary if it's not a known word
            mJustAutoAddedWord = false;

            if (!typedWord.isAtTagsSearchState()) {
                if (index == 0) {
                    checkAddToDictionaryWithAutoDictionary(
                            typedWord.getTypedWord(), SuggestImpl.AdditionType.Picked);
                }

                final boolean showingAddToDictionaryHint =
                        !mJustAutoAddedWord
                                && index == 0
                                && mShowSuggestions
                                && !mSuggest.isValidWord(
                                        suggestion) // this is for the case that the word was
                                // auto-added upon picking
                                && !mSuggest.isValidWord(
                                        suggestion
                                                .toString()
                                                .toLowerCase(
                                                        getCurrentAlphabetKeyboard().getLocale()));

                if (showingAddToDictionaryHint) {
                    if (mCandidateView != null) mCandidateView.showAddToDictionaryHint(suggestion);
                } else {
                    setSuggestions(
                            mSuggest.getNextSuggestions(suggestion, mWord.isAllUpperCase()),
                            false,
                            false);
                }
            }
        } finally {
            if (ic != null) {
                ic.endBatchEdit();
            }
        }
    }

    /**
     * Commits the chosen word to the text field and saves it for later retrieval.
     *
     * @param wordToCommit the suggestion picked by the user to be committed to the text field
     * @param typedWord the word the user typed.
     */
    @CallSuper
    protected void commitWordToInput(
            @NonNull CharSequence wordToCommit, @NonNull CharSequence typedWord) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            final boolean delayedUpdates = isSelectionUpdateDelayed();
            markExpectingSelectionUpdate();
            // we DO NOT want to use commitCorrection if we do not know
            // the exact position in the text-box.
            if (TextUtils.equals(wordToCommit, typedWord) || delayedUpdates) {
                ic.commitText(wordToCommit, 1);
            } else {
                AnyApplication.getDeviceSpecific()
                        .commitCorrectionToInputConnection(
                                ic,
                                getCursorPosition() - typedWord.length(),
                                typedWord,
                                wordToCommit);
            }
        }

        clearSuggestions();
    }

    private boolean isCursorTouchingWord() {
        InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return false;
        }

        CharSequence toLeft = ic.getTextBeforeCursor(1, 0);
        // It is not exactly clear to me why, but sometimes, although I request
        // 1 character, I get the entire text
        if (!TextUtils.isEmpty(toLeft) && !isWordSeparator(toLeft.charAt(0))) {
            return true;
        }

        CharSequence toRight = ic.getTextAfterCursor(1, 0);
        if (!TextUtils.isEmpty(toRight) && !isWordSeparator(toRight.charAt(0))) {
            return true;
        }

        return false;
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
        mFrenchSpacePunctuationBehavior =
                mSwapPunctuationAndSpace
                        && locale != null
                        && locale.toString().toLowerCase(Locale.US).startsWith("fr");
    }

    public void revertLastWord() {
        if (mWordRevertLength == 0) {
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
        } else {
            final int length = mWordRevertLength;
            mAutoCorrectOn = false;
            // note: typedWord may be empty
            final InputConnection ic = getCurrentInputConnection();
            final int globalCursorPosition = getCursorPosition();
            ic.setComposingRegion(globalCursorPosition - length, globalCursorPosition);
            WordComposer temp = mWord;
            mWord = mPreviousWord;
            mPreviousWord = temp;
            mWordRevertLength = 0;
            final CharSequence typedWord = mWord.getTypedWord();
            ic.setComposingText(typedWord /* mComposing */, 1);
            performUpdateSuggestions();
            if (mJustAutoAddedWord) {
                removeFromUserDictionary(typedWord.toString());
            }
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
                setSuggestions(stringList, true, true);
                mWord.setPreferredWord(null);
            }
        }
    }

    private void checkAddToDictionaryWithAutoDictionary(
            CharSequence newWord, Suggest.AdditionType type) {
        mJustAutoAddedWord = false;

        // unfortunately, has to do it on the main-thread (because we checking mJustAutoAddedWord)
        if (mSuggest.tryToLearnNewWord(newWord, type)) {
            addWordToDictionary(newWord.toString());
            mJustAutoAddedWord = true;
        }
    }

    @CallSuper
    protected boolean isSuggestionAffectingCharacter(int code) {
        return Character.isLetter(code);
    }

    public void removeFromUserDictionary(String wordToRemove) {
        mInputSessionDisposables.add(
                Observable.just(wordToRemove)
                        .subscribeOn(RxSchedulers.background())
                        .map(
                                word -> {
                                    mSuggest.removeWordFromUserDictionary(word);
                                    return word;
                                })
                        .observeOn(RxSchedulers.mainThread())
                        .subscribe(
                                word -> {
                                    if (mCandidateView != null) {
                                        mCandidateView.notifyAboutRemovedWord(word);
                                    }
                                },
                                e ->
                                        Logger.w(
                                                TAG,
                                                e,
                                                "Failed to remove word '%s' from user-dictionary!",
                                                wordToRemove)));
        mJustAutoAddedWord = false;
        abortCorrectionAndResetPredictionState(false);
    }

    @VisibleForTesting
    static class CancelSuggestionsAction
            implements KeyboardViewContainerView.StripActionProvider, CloseIconChangedListener {
        // two seconds is enough.
        private static final long DOUBLE_TAP_TIMEOUT = 2 * 1000 - 50;
        private final Runnable mCancelPrediction;
        private Animation mCancelToGoneAnimation;
        private Animation mCancelToVisibleAnimation;
        private Animation mCloseTextToGoneAnimation;
        private Animation mCloseTextToVisibleAnimation;
        private View mRootView;
        private View mCloseText;
        private Drawable mCloseIcon;
        private final Runnable mReHideTextAction =
                () -> {
                    mCloseTextToGoneAnimation.reset();
                    mCloseText.startAnimation(mCloseTextToGoneAnimation);
                };

        CancelSuggestionsAction(Runnable cancelPrediction) {
            mCancelPrediction = cancelPrediction;
        }

        @Override
        public View inflateActionView(ViewGroup parent) {
            final Context context = parent.getContext();
            mCancelToGoneAnimation =
                    AnimationUtils.loadAnimation(context, R.anim.suggestions_cancel_to_gone);
            mCancelToGoneAnimation.setAnimationListener(
                    new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mRootView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
            mCancelToVisibleAnimation =
                    AnimationUtils.loadAnimation(context, R.anim.suggestions_cancel_to_visible);
            mCloseTextToGoneAnimation =
                    AnimationUtils.loadAnimation(context, R.anim.suggestions_double_cancel_to_gone);
            mCloseTextToGoneAnimation.setAnimationListener(
                    new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            Logger.d("tsdt", "sdfsdfs");
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mCloseText.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
            mCloseTextToVisibleAnimation =
                    AnimationUtils.loadAnimation(
                            context, R.anim.suggestions_double_cancel_to_visible);

            mRootView =
                    LayoutInflater.from(context)
                            .inflate(R.layout.cancel_suggestions_action, parent, false);

            mCloseText = mRootView.findViewById(R.id.close_suggestions_strip_text);

            ImageView closeIcon = mRootView.findViewById(R.id.close_suggestions_strip_icon);
            closeIcon.setImageDrawable(mCloseIcon);

            mRootView.setOnClickListener(
                    view -> {
                        mRootView.removeCallbacks(mReHideTextAction);
                        if (mCloseText.getVisibility() == View.VISIBLE) {
                            // already shown, so just cancel suggestions.
                            mCancelPrediction.run();
                        } else {
                            mCloseText.setVisibility(View.VISIBLE);
                            mCloseTextToVisibleAnimation.reset();
                            mCloseText.startAnimation(mCloseTextToVisibleAnimation);
                            mRootView.postDelayed(mReHideTextAction, DOUBLE_TAP_TIMEOUT);
                        }
                    });

            return mRootView;
        }

        @Override
        public void onRemoved() {
            mRootView.removeCallbacks(mReHideTextAction);
        }

        void setCancelIconVisible(boolean visible) {
            if (mRootView != null) {
                final int visibility = visible ? View.VISIBLE : View.GONE;
                if (mRootView.getVisibility() != visibility) {
                    mRootView.setVisibility(View.VISIBLE);
                    mCancelToVisibleAnimation.reset();
                    mCancelToGoneAnimation.reset();
                    mRootView.startAnimation(
                            visible ? mCancelToVisibleAnimation : mCancelToGoneAnimation);
                }
            }
        }

        @Override
        public void onCloseIconChanged(Drawable icon) {
            mCloseIcon = icon;
        }
    }

    public interface CloseIconChangedListener {
        void onCloseIconChanged(Drawable icon);
    }
}
