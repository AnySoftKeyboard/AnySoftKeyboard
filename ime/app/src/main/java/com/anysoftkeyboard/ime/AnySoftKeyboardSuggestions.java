package com.anysoftkeyboard.ime;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.anysoftkeyboard.utils.IMEUtil;
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
  private static final long NEVER_TIME_STAMP = -1L * 365L * 24L * 60L * 60L * 1000L; // a year ago.
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
  @Nullable private Keyboard.Key mLastKey;
  private int mLastPrimaryKey = Integer.MIN_VALUE;
  private long mExpectingSelectionUpdateBy = NEVER_TIME_STAMP;
  private boolean mLastCharacterWasShifted = false;
  private boolean mFrenchSpacePunctuationBehavior;
  private boolean mHomophoneSearchMode = false;
  // Set while the candidate strip is showing homophone results. Picking any
  // suggestion in this state should be treated as a homophone replacement.
  private boolean mHomophoneSuggestionsActive = false;

  /**
   * Cursor position recorded at the moment homophone suggestions were shown. Used to validate that
   * a subsequent homophone pick still applies to the originally-committed character; if the user
   * moved the cursor in between, the pick falls back to a normal commit (no replacement).
   */
  private int mHomophoneAnchorCursorPos = -1;

  /** Length (in chars) of the previously-committed character when homophones were shown. */
  private int mHomophoneAnchorPrevCharLen = 0;

  /**
   * The picked candidate from the radical-candidate-selector feature: when the user types a
   * selector key (e.g. "v" after "fl") we promote the Nth original candidate to the front of the
   * suggestion strip. Cleared whenever the radical buffer becomes empty or when a non- selector
   * character is added.
   */
  @androidx.annotation.Nullable private String mPromotedRadicalCandidate;

  /**
   * The radical buffer (typed-radicals string) that was current when {@link
   * #mPromotedRadicalCandidate} was set. Used by {@link #performUpdateSuggestions} to detect when
   * the buffer has changed (e.g. user backspaced past the selector key) and the promotion is no
   * longer applicable.
   */
  @androidx.annotation.Nullable private String mPromotedRadicalBuffer;

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
  /*
   * True when the current input field is a password / visible-password / web-password
   * variation. Radical composition is disabled in these contexts so intermediate
   * radicals and candidate characters never leak into privacy-sensitive fields.
   * NOTE: This is intentionally narrower than {@code !mPredictionOn}: web browsers
   * (Edge / Chrome) routinely set TYPE_TEXT_FLAG_NO_SUGGESTIONS on ordinary search
   * boxes and address bars, which turns prediction off but must still allow radical
   * IMEs to compose Chinese characters.
   */
  private boolean mInputFieldIsPassword;
  private boolean mAutoCorrectOn;
  private boolean mAllowSuggestionsRestart = true;

  private boolean mJustAutoAddedWord = false;

  @VisibleForTesting
  final CancelSuggestionsAction mCancelSuggestionsAction =
      new CancelSuggestionsAction(() -> abortCorrectionAndResetPredictionState(true));

  /*
   * Configuration flag. Should we support dictionary suggestions
   */
  private boolean mShowSuggestions = false;
  private boolean mAutoComplete;

  private static void fillSeparatorsSparseArray(
      SparseBooleanArray sparseBooleanArray, char[] chars) {
    sparseBooleanArray.clear();
    for (char separator : chars) sparseBooleanArray.put(separator, true);
  }

  @Nullable
  protected Keyboard.Key getLastUsedKey() {
    return mLastKey;
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

    mSuggest = createSuggest();

    addDisposable(
        prefs()
            .getBoolean(
                R.string.settings_key_allow_suggestions_restart,
                R.bool.settings_default_allow_suggestions_restart)
            .asObservable()
            .subscribe(
                aBoolean -> mAllowSuggestionsRestart = aBoolean,
                GenericOnError.onError("settings_key_allow_suggestions_restart")));

    final Observable<Boolean> powerSavingShowSuggestionsObservable =
        Observable.combineLatest(
            prefs()
                .getBoolean(
                    R.string.settings_key_show_suggestions,
                    R.bool.settings_default_show_suggestions)
                .asObservable(),
            PowerSaving.observePowerSavingState(
                getApplicationContext(), R.string.settings_key_power_save_mode_suggestions_control),
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
                prefs()
                    .getString(
                        R.string.settings_key_auto_pick_suggestion_aggressiveness,
                        R.string.settings_default_auto_pick_suggestion_aggressiveness)
                    .asObservable(),
                prefs()
                    .getBoolean(
                        R.string.settings_key_try_splitting_words_for_correction,
                        R.bool.settings_default_try_splitting_words_for_correction)
                    .asObservable(),
                Triple::create)
            .subscribe(
                triple -> {
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
                      // Even with global suggestions off, a radical IME (Boshiamy / Cangjie /
                      // Zhuyin) must keep its dictionary loaded, those keyboards have no
                      // sensible "no suggestions" mode because every keystroke composes a
                      // radical. setupSuggestionsForKeyboard's radical-only branch will load
                      // just the radical pack; for non-radical keyboards we fall through to
                      // closeDictionaries() as before.
                      if (isCurrentKeyboardRadical()) {
                        setDictionariesForCurrentKeyboard();
                      } else {
                        closeDictionaries();
                        // Only abort correction if the user is currently typing/predicting
                        // to avoid clearing suggestions mock state/interactions prematurely.
                        if (isCurrentlyPredicting()) {
                          abortCorrectionAndResetPredictionState(false);
                        }
                      }
                    }
                  }
                },
                GenericOnError.onError("combineLatest settings_key_show_suggestions")));
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

    // Reset the password marker eagerly: between onStartInput and onStartInputView the new
    // field's variation hasn't been read yet, but any radical-mode predicate that fires in
    // that window (e.g. from a posted runnable resetting the keyboard view) must not see the
    // previous field's password flag.
    mInputFieldIsPassword = false;
    // Same race window for promoted-radical-candidate state: must not survive into a new
    // field, where the suggestion strip would briefly show a candidate that no longer
    // matches the (empty) buffer of the new composition.
    mPromotedRadicalCandidate = null;
    mPromotedRadicalBuffer = null;

    abortCorrectionAndResetPredictionState(false);
  }

  @Override
  public void onStartInputView(final EditorInfo attribute, final boolean restarting) {
    super.onStartInputView(attribute, restarting);

    mPredictionOn = false;
    mCompletionOn = false;
    mCompletions = EMPTY_COMPLETIONS;
    mInputFieldSupportsAutoPick = false;
    mInputFieldIsPassword = false;
    // prediction should be on by default, unless disabled by a specific variation
    mPredictionOn = true;

    switch (attribute.inputType & EditorInfo.TYPE_MASK_CLASS) {
      case EditorInfo.TYPE_CLASS_DATETIME:
        Logger.d(
            TAG,
            "Setting INPUT_MODE_DATETIME as keyboard due to a TYPE_CLASS_DATETIME" + " input.");
        getKeyboardSwitcher()
            .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_DATETIME, attribute, restarting);
        mPredictionOn = false;
        break;
      case EditorInfo.TYPE_CLASS_NUMBER:
        Logger.d(TAG, "Setting INPUT_MODE_NUMBERS as keyboard due to a TYPE_CLASS_NUMBER input.");
        getKeyboardSwitcher()
            .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_NUMBERS, attribute, restarting);
        mPredictionOn = false;
        if ((attribute.inputType & EditorInfo.TYPE_MASK_VARIATION)
            == EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD) {
          mInputFieldIsPassword = true;
        }
        break;
      case EditorInfo.TYPE_CLASS_PHONE:
        Logger.d(TAG, "Setting INPUT_MODE_PHONE as keyboard due to a TYPE_CLASS_PHONE input.");
        getKeyboardSwitcher()
            .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_PHONE, attribute, restarting);
        mPredictionOn = false;
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
            mInputFieldIsPassword = true;
            break;
          case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
          case EditorInfo.TYPE_TEXT_VARIATION_URI:
          case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
            Logger.d(TAG, "An internet input with has prediction but no auto-pick");
            mInputFieldSupportsAutoPick = false;
            break;
          default:
            mInputFieldSupportsAutoPick = true;
        }

        switch (textVariation) {
          case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
          case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
            mAutoSpace = false;
            break;
          default:
            mAutoSpace = mPrefsAutoSpace;
        }

        switch (textVariation) {
          case EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
          case EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
            Logger.d(
                TAG,
                "Setting INPUT_MODE_EMAIL as keyboard due to a"
                    + " TYPE_TEXT_VARIATION_EMAIL_ADDRESS input.");
            getKeyboardSwitcher()
                .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_EMAIL, attribute, restarting);
            break;
          case EditorInfo.TYPE_TEXT_VARIATION_URI:
            Logger.d(
                TAG,
                "Setting INPUT_MODE_URL as keyboard due to a" + " TYPE_TEXT_VARIATION_URI input.");
            getKeyboardSwitcher()
                .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_URL, attribute, restarting);
            break;
          case EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE:
            Logger.d(
                TAG,
                "Setting INPUT_MODE_IM as keyboard due to a"
                    + " TYPE_TEXT_VARIATION_SHORT_MESSAGE input.");
            getKeyboardSwitcher()
                .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_IM, attribute, restarting);
            break;
          default:
            Logger.d(TAG, "Setting INPUT_MODE_TEXT as keyboard due to a default input.");
            getKeyboardSwitcher()
                .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, attribute, restarting);
        }
        break;
      default:
        Logger.d(TAG, "Setting INPUT_MODE_TEXT as keyboard due to a default input.");
        // No class. Probably a console window, or no GUI input connection
        mAutoSpace = mPrefsAutoSpace;
        getKeyboardSwitcher()
            .setKeyboardMode(KeyboardSwitcher.INPUT_MODE_TEXT, attribute, restarting);
    }

    final int textFlag = attribute.inputType & EditorInfo.TYPE_MASK_FLAGS;
    if (IMEUtil.shouldHonorNoSuggestionsFlag(textFlag)) {
      Logger.d(TAG, "Input requested NO_SUGGESTIONS.");
      mPredictionOn = false;
    }

    mPredictionOn = mPredictionOn && mShowSuggestions;

    mCancelSuggestionsAction.setCancelIconVisible(false);
    getInputViewContainer().addStripAction(mCancelSuggestionsAction, false);
    getInputViewContainer()
        .setActionsStripVisibility(isPredictionOn() || isCurrentKeyboardRadical());
    clearSuggestions();
  }

  @Override
  public void onFinishInput() {
    super.onFinishInput();
    mCancelSuggestionsAction.setCancelIconVisible(false);
    mPredictionOn = false;
    // Cancel any pending suggestion messages to prevent them from executing after input ends
    mKeyboardHandler.removeAllSuggestionMessages();
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
    final int oldCandidateStart = mGlobalCandidateStartPositionDangerous;
    final int oldCandidateEnd = mGlobalCandidateEndPositionDangerous;
    super.onUpdateSelection(
        oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
    Logger.v(
        TAG,
        "onUpdateSelection: word '%s', position %d.",
        mWord.getTypedWord(),
        mWord.cursorPosition());
    final boolean noChange =
        newSelStart == oldSelStart
            && oldSelEnd == newSelEnd
            && oldCandidateStart == candidatesStart
            && oldCandidateEnd == candidatesEnd;
    final boolean isExpectedEvent = SystemClock.uptimeMillis() < mExpectingSelectionUpdateBy;
    if (noChange) {
      Logger.v(TAG, "onUpdateSelection: no-change. Discarding.");
      return;
    }
    mExpectingSelectionUpdateBy = NEVER_TIME_STAMP;

    if (isExpectedEvent) {
      Logger.v(TAG, "onUpdateSelection: Expected event. Discarding.");
      return;
    }

    final boolean cursorMovedUnexpectedly = (oldSelStart != newSelStart || oldSelEnd != newSelEnd);
    if (cursorMovedUnexpectedly) {
      mLastSpaceTimeStamp = NEVER_TIME_STAMP;
      if (shouldRevertOnDelete()) {
        Logger.d(
            TAG,
            "onUpdateSelection: user moved cursor from a undo-commit sensitive"
                + " position. Will not be able to undo-commit.");
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
        final var newPosition = newSelEnd - candidatesStart;
        if (newSelStart >= candidatesStart
            && newSelStart <= candidatesEnd
            && newPosition >= 0
            && newPosition <= mWord.charCount()) {
          // 1) predicting and moved inside the word - just update the
          // cursor position and shift state
          // inside the currently typed word
          Logger.d(
              TAG,
              "onUpdateSelection: inside the currently typed word to location %d.",
              newPosition);
          mWord.setCursorPosition(newPosition);
        } else {
          Logger.d(
              TAG, "onUpdateSelection: cursor moving outside the currently predicting" + " word");
          abortCorrectionAndResetPredictionState(false);
          postRestartWordSuggestion();
        }
      } else {
        Logger.d(
            TAG,
            "onUpdateSelection: not predicting at this moment, maybe the cursor is now"
                + " at a new word?");
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
    mCancelSuggestionsAction.setOwningCandidateView(mCandidateView);
    return view;
  }

  protected WordComposer getCurrentComposedWord() {
    return mWord;
  }

  @Override
  @CallSuper
  public void onKey(
      int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
    mLastKey = key;
    mLastPrimaryKey = primaryCode;
    super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
    if (primaryCode != KeyCodes.DELETE) {
      mWordRevertLength = 0;
    }
    mCandidateView.dismissAddToDictionaryHint();
  }

  protected void resetLastPressedKey() {
    mLastKey = null;
  }

  @Override
  public void onRelease(int primaryCode) {
    // not allowing undo on-text in clipboard paste operations.
    if (primaryCode == KeyCodes.CLIPBOARD_PASTE) mWordRevertLength = 0;
    if (mLastPrimaryKey == primaryCode && KeyCodes.isOutputKeyCode(primaryCode)) {
      // In radical mode, space is used to commit characters, not as a separator.
      // Don't record space timestamp to avoid swap-punctuation-and-space behavior.
      if (!(primaryCode == KeyCodes.SPACE && isRadicalInputMode())) {
        setSpaceTimeStamp(primaryCode == KeyCodes.SPACE);
      }
    }
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
    if (canRestartWordSuggestion()) {
      mKeyboardHandler.sendEmptyMessageDelayed(
          KeyboardUIStateHandler.MSG_RESTART_NEW_WORD_SUGGESTIONS, 10 * ONE_FRAME_DELAY);
    }
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
    // A committed radical-composed character must never be pulled back into the
    // composition buffer by the undo-commit feature (revertLastWord swaps mWord
    // with mPreviousWord, which on a radical keyboard still holds the raw radical
    // keystrokes -- e.g. "winest"). That made "commit a char, press space, press
    // backspace" re-insert the previous radicals. On radical keyboards we always
    // fall through to a plain delete instead.
    return mWordRevertLength > 0 && !isCurrentKeyboardRadical();
  }

  /**
   * Commits ("&#36865;&#20986;") any in-progress radical composition and clears all related
   * transient state. Called at the moment the user leaves a radical alphabet keyboard -- either to
   * the symbols/numbers view ({@link #onSymbolsKeyboardSet}) or to a different keyboard ({@link
   * #onAlphabetKeyboardSet}) -- so the typed radicals are flushed to the field instead of being
   * abandoned or carried over.
   *
   * <p>The RAW typed radical buffer is committed verbatim (e.g. "win"), NOT the highlighted mapped
   * candidate (e.g. "&#27442;"): the user never pressed space to confirm a candidate, so leaving
   * the keyboard must flush exactly what is in the buffer. Committing the raw buffer (passing the
   * same value as both {@code commitWordToInput} arguments) also keeps us on the plain {@code
   * commitText} path instead of {@code commitCorrection}; the latter leaves a correction span that
   * makes the framework re-select a previously committed word back into the buffer on the next
   * backspace.
   *
   * <p>No-op (returns {@code false}) when there is nothing pending, the field forbids radical
   * input, the current keyboard is not a radical keyboard, or no radical dictionaries are loaded.
   *
   * @return {@code true} if a composition was committed.
   */
  private boolean commitPendingRadical() {
    if (mWord.isEmpty()) return false;
    if (mInputFieldIsPassword
        || !isCurrentKeyboardRadical()
        || !mSuggest.hasRadicalDictionaries()) {
      return false;
    }
    final InputConnection ic = getCurrentInputConnection();
    if (ic != null) ic.beginBatchEdit();
    final WordComposer typedWord = prepareWordComposerForNextWord();
    final CharSequence rawTyped = typedWord.getTypedWord();
    commitWordToInput(rawTyped, rawTyped);
    mWordRevertLength = 0;
    mPreviousWord.reset();
    mPromotedRadicalCandidate = null;
    mPromotedRadicalBuffer = null;
    if (ic != null) ic.endBatchEdit();
    return true;
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

    if (mWord.charCount() == 0) {
      mWordRevertLength = 0;
      mWord.reset();
      mAutoCorrectOn = isPredictionOn() && mAutoComplete && mInputFieldSupportsAutoPick;
      if (isAlphabet(primaryCode) && mShiftKeyState.isActive()) {
        mWord.setFirstCharCapitalized(true);
      }
    }

    mLastCharacterWasShifted = (getInputView() != null) && getInputView().isShifted();

    final InputConnection ic = getCurrentInputConnection();
    final boolean radicalMode = isRadicalInputMode();
    // Detect candidate-selector key (Boshiamy-only feature). Must run BEFORE adding
    // the character to mWord because the selector index is based on the matches of
    // the radical sequence as it stood before the selector key was typed.
    //
    // The selector key is also appended to the radical buffer so its own matches still
    // appear after the promoted candidate (per spec: "再加上原有字根表中flv的字不重複出現在後面的候選字").
    if (radicalMode && mWord.charCount() > 0) {
      final String selectorKeys = currentRadicalCandidateSelectorKeys();
      if (selectorKeys != null && !selectorKeys.isEmpty()) {
        final int selectorPos = selectorKeys.indexOf((char) primaryCode);
        if (selectorPos >= 0) {
          // Per Boshiamy convention, selector keys (vrsfwlcbkj) promote the
          // (N+1)-th exact match of the radical sequence typed so far into the
          // highlighted slot of the candidate strip, but they do NOT commit.
          // The user must still press space to commit the highlighted candidate.
          //
          // The selector role is only taken when the extended sequence
          // (radicalsSoFar + selectorChar) does NOT itself resolve to any
          // characters. Otherwise the user is still typing a longer radical
          // code (e.g. "oo" + "v" -> "oov" -> 回), and we must defer to the
          // natural exact-match lookup that runs after mWord.add() below.
          final String typedRadicals = mWord.getTypedWord().toString();
          final String extendedRadicals = typedRadicals + (char) primaryCode;
          final boolean extendedHasMatches =
              !mSuggest.getRadicalExactMatches(extendedRadicals).isEmpty();
          if (extendedHasMatches) {
            mPromotedRadicalCandidate = null;
            mPromotedRadicalBuffer = null;
          } else {
            final List<String> matches = mSuggest.getRadicalExactMatches(typedRadicals);
            if (matches.size() > selectorPos + 1) {
              mPromotedRadicalCandidate = matches.get(selectorPos + 1);
              // Record the buffer as it will be AFTER the selector key is appended below
              // (so performUpdateSuggestions sees a match when running against the same buffer).
              mPromotedRadicalBuffer = extendedRadicals;
            } else {
              mPromotedRadicalCandidate = null;
              mPromotedRadicalBuffer = null;
            }
          }
        } else {
          mPromotedRadicalCandidate = null;
          mPromotedRadicalBuffer = null;
        }
      }
    } else if (mWord.charCount() == 0) {
      mPromotedRadicalCandidate = null;
      mPromotedRadicalBuffer = null;
    }
    // In radical mode, always use the prediction path (composing + suggestions)
    // even if normal prediction is off
    if (isPredictionOn() || radicalMode) {
      mWord.add(primaryCode, nearByKeyCodes);
      if (ic != null) {
        int newCursorPosition;
        if (mWord.cursorPosition() != mWord.charCount()) {
          /* Cursor is not at the end of the word. I'll need to reposition.
          The code for tracking the current position is split among several files and difficult to debug.
          This has been proven to work in every case: */
          if (multiTapIndex > 0) {
            final int previousKeyCode = key.getMultiTapCode(multiTapIndex - 1);
            newCursorPosition =
                Character.charCount(primaryCode) - Character.charCount(previousKeyCode);
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
      // character (like '). In radical mode, always update suggestions since
      // punctuation chars are part of radical sequences.
      if (isSuggestionAffectingCharacter(primaryCode) || radicalMode) {
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
    mExpectingSelectionUpdateBy = SystemClock.uptimeMillis() + MAX_TIME_TO_EXPECT_SELECTION_UPDATE;
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
    final boolean radicalMode = isRadicalInputMode();

    // Handle separator
    InputConnection ic = getCurrentInputConnection();
    if (ic != null) {
      ic.beginBatchEdit();
    }
    final WordComposer typedWord = prepareWordComposerForNextWord();
    CharSequence wordToOutput = typedWord.getTypedWord();
    boolean handledOutputToInputConnection = false;

    if (radicalMode && wasPredicting) {
      final boolean separatorInsideWord = (typedWord.cursorPosition() < typedWord.charCount());
      if (separatorInsideWord) {
        abortCorrectionAndResetPredictionState(false);
      } else {
        // In radical mode, always commit the preferred (radical-matched) candidate
        // independent of autocorrect settings
        final CharSequence preferredWord = typedWord.getPreferredWord();
        if (preferredWord != null && preferredWord.length() > 0) {
          wordToOutput = preferredWord;
        }

        if (mHomophoneSearchMode && isSpace) {
          // In homophone search mode, don't commit, show homophones instead.
          // NOTE: prepareWordComposerForNextWord() already swapped the composers
          // (mWord is now reset, mPreviousWord holds the typed radicals). Do NOT
          // re-alias them here, that would corrupt composer state for any
          // subsequent input/commit.
          commitWordToInput(wordToOutput, typedWord.getTypedWord());
          mWordRevertLength = 0;
          handledOutputToInputConnection = true;
          setSpaceTimeStamp(false);
          if (ic != null) ic.endBatchEdit();
          showHomophonesFor(wordToOutput.toString());
          return;
        }

        commitWordToInput(wordToOutput, typedWord.getTypedWord());
        mWordRevertLength = 0;
        // In radical mode, space commits the character but should not
        // insert a space. Mark as handled so the separator is not sent.
        // Also reset space timestamp to prevent swap-punctuation-and-space
        // from deleting the committed character on next separator.
        if (isSpace) {
          handledOutputToInputConnection = true;
          setSpaceTimeStamp(false);
        }
        // Don't mark as end-of-sentence so next-word suggestions are shown
        isEndOfSentence = false;
      }
    } else {
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
          checkAddToDictionaryWithAutoDictionary(wordToOutput, SuggestImpl.AdditionType.Typed);
        }
        // Picked the suggestion by a space/punctuation character: we will treat it
        // as "added an auto space".
        if (mAutoComplete) {
          mWordRevertLength = wordToOutput.length() + 1;
        }
      } else if (separatorInsideWord) {
        // when putting a separator in the middle of a word, there is no
        // need to do correction, or keep knowledge
        abortCorrectionAndResetPredictionState(false);
      }
    }

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
      } else if (mLastSpaceTimeStamp != NEVER_TIME_STAMP /*meaning the previous key was SPACE*/
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
      final List<CharSequence> nextSuggestions =
          mSuggest.getNextSuggestions(wordToOutput, typedWord.isAllUpperCase());
      Logger.d(
          TAG,
          "handleSeparator: next suggestions for '%s': %d items",
          wordToOutput,
          nextSuggestions.size());
      setSuggestions(nextSuggestions, -1);
    }
  }

  private WordComposer prepareWordComposerForNextWord() {
    if (mWord.isEmpty()) return mWord;

    final WordComposer typedWord = mWord;
    mWord = mPreviousWord;
    mPreviousWord = typedWord;
    mWord.reset(); // re-using
    // The promotion only applies to the in-flight composition; once we move on
    // (commit, separator, etc.) it must be cleared so the next composition starts fresh.
    mPromotedRadicalCandidate = null;
    mPromotedRadicalBuffer = null;
    return typedWord;
  }

  private boolean isSpaceSwapCharacter(int primaryCode) {
    if (isSentenceSeparator(primaryCode)) {
      if (mFrenchSpacePunctuationBehavior) {
        return switch (primaryCode) {
          case '!', '?', ':', ';' -> false;
          default -> true;
        };
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
        final int c = Character.codePointAt(word, Character.offsetByCodePoints(word, 0, index));
        if (index == 0) mWord.setFirstCharCapitalized(Character.isUpperCase(c));

        tempNearByKeys[0] = c;
        mWord.add(c, tempNearByKeys);

        index += Character.charCount(c);
      }
      mWord.setCursorPosition(toLeft.length());
      final int globalCursorPosition = getCursorPosition();
      ic.setComposingRegion(
          globalCursorPosition - toLeft.length(), globalCursorPosition + toRight.length());

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

    // Radical-mode popup-pick routing: long-press popups (e.g. ".", ",", "'", "/", "-",
    // ";", "[", "]" on the Boshiamy z/n/m keys) arrive here as onText events. In radical
    // mode, when the picked text is a single radical key code, route it through the
    // normal key dispatch so it appends to the composing radical buffer instead of
    // committing literal text to the field (which would break sequences like ".1" -> ㄅ).
    if (isRadicalInputMode() && text != null && text.length() > 0) {
      final int cp = Character.codePointAt(text, 0);
      if (Character.charCount(cp) == text.length()) {
        final AnyKeyboard kb = getCurrentAlphabetKeyboard();
        if (kb != null && kb.isRadicalKeyCode(cp)) {
          onKey(cp, key, 0, new int[] {cp}, true);
          return;
        }
      }
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

    final AnyKeyboard currentAlphabetKeyboard = getCurrentAlphabetKeyboard();
    if (currentAlphabetKeyboard == null || !isInAlphabetKeyboardMode()) return;

    // For radical keyboards, always load dictionaries (radical lookup doesn't depend on
    // prediction/suggestions settings). For normal keyboards, only load if prediction is on.
    if (mPredictionOn || isCurrentKeyboardRadical()) {
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

  @NonNull
  protected DictionaryBackgroundLoader.Listener getDictionaryLoadedListener(
      @NonNull AnyKeyboard currentAlphabetKeyboard) {
    return NO_OP_DICTIONARY_LOADER_LISTENER;
  }

  @Override
  protected void onOrientationChanged(int oldOrientation, int newOrientation) {
    super.onOrientationChanged(oldOrientation, newOrientation);
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
    mHomophoneSearchMode = false;
    mHomophoneSuggestionsActive = false;
    mHomophoneAnchorCursorPos = -1;
    mHomophoneAnchorPrevCharLen = 0;
    mPromotedRadicalCandidate = null;
    mPromotedRadicalBuffer = null;
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
    // In radical mode, never restart word suggestions on committed CJK text.
    // Once a radical-composed character is committed, it should not be pulled
    // back into the composition buffer.
    if (isRadicalInputMode()) {
      return false;
    }
    if (!isPredictionOn()
        || !mAllowSuggestionsRestart
        || !mAutoComplete
        || inputView == null
        || !inputView.isShown()) {
      // Conditions checked:
      // - isPredictionOn(): Global prediction must be enabled for the current input field
      // - mAllowSuggestionsRestart: User setting to enable/disable suggestion restart
      // - mAutoComplete: Auto-correct / word correction must be enabled
      // - inputView visibility: Input view must be shown
      //
      // Note: We don't check isCurrentlyPredicting() here because this method is called
      // AFTER abortCorrectionAndResetPredictionState() in onUpdateSelection(). Any previous
      // prediction has already been aborted before we reach this point.
      Logger.d(
          TAG,
          "performRestartWordSuggestion: no need to restart: isPredictionOn=%s,"
              + " mAllowSuggestionsRestart=%s, mAutoComplete=%s",
          isPredictionOn(),
          mAllowSuggestionsRestart,
          mAutoComplete);
      return false;
    } else if (!isCursorTouchingWord()) {
      Logger.d(TAG, "User moved cursor to no-man land. Bye bye.");
      return false;
    }

    return true;
  }

  protected void clearSuggestions() {
    mKeyboardHandler.removeAllSuggestionMessages();
    setSuggestions(Collections.emptyList(), -1);
  }

  /**
   * Toggles homophone search mode. When active, the next committed character's homophones are shown
   * as suggestions instead of next-word predictions.
   */
  public void toggleHomophoneSearch() {
    // Respect the password gate (don't surface candidate text in password fields), but allow
    // homophone search whenever the radical keyboard is active, browser inputs (Edge /
    // Chrome address bars, in-page search boxes) often set TYPE_TEXT_FLAG_NO_SUGGESTIONS,
    // which would otherwise disable this feature in those fields. Also no-op when the active
    // dictionary has no homophone data (e.g. the Japanese variant) so the key isn't a dead
    // toggle.
    if ((!mPredictionOn && !isRadicalInputMode()) || !mSuggest.hasHomophoneData()) {
      mHomophoneSearchMode = false;
      return;
    }
    mHomophoneSearchMode = !mHomophoneSearchMode;
    if (mHomophoneSearchMode) {
      // Show a hint in the candidate view, localized to the current keyboard's language.
      List<CharSequence> hint = new ArrayList<>();
      hint.add(getText(getHomophoneHintResIdForCurrentKeyboard()));
      setSuggestions(hint, -1);
    } else {
      clearSuggestions();
    }
  }

  private int getHomophoneHintResIdForCurrentKeyboard() {
    final AnyKeyboard kbd = getCurrentAlphabetKeyboard();
    String locale = kbd != null ? kbd.getDefaultDictionaryLocale() : null;
    if (locale == null) return R.string.homophone_search_hint;
    String l = locale.toLowerCase(java.util.Locale.ROOT);
    if (l.startsWith("ja")) return R.string.homophone_search_hint_ja;
    if (l.startsWith("zh-cn") || l.startsWith("zh_cn") || l.contains("simplified")) {
      return R.string.homophone_search_hint_zh_cn;
    }
    if (l.startsWith("zh")) return R.string.homophone_search_hint_zh_tw;
    return R.string.homophone_search_hint;
  }

  /**
   * Shows homophones for the given character. Each homophone is displayed with its radical code
   * prefix in the suggestion bar.
   */
  private void showHomophonesFor(@NonNull String character) {
    // Homophone lookup must work in any radical-mode field, including browser inputs
    // (Edge / Chrome address bar, in-page search boxes) that turn off normal prediction
    // via TYPE_TEXT_FLAG_NO_SUGGESTIONS. Gate on either prediction OR radical mode so
    // the user can still ask for homophones while composing radicals in those fields.
    if ((!mPredictionOn && !isRadicalInputMode()) || !mSuggest.hasHomophoneData()) {
      mHomophoneSearchMode = false;
      return;
    }

    List<CharSequence> homophones = mSuggest.getHomophones(character);
    if (homophones.isEmpty()) {
      mHomophoneSearchMode = false;
      setSuggestions(mSuggest.getNextSuggestions(character, false), -1);
      return;
    }

    // Show the bare characters only, keep the strip compact so more candidates
    // are visible. The radical code is revealed in the composing area when the
    // user picks one (see pickSuggestionManually).
    List<CharSequence> suggestions = new ArrayList<>();
    suggestions.add(character);
    for (CharSequence homophone : homophones) {
      String h = homophone.toString();
      if (h.equals(character)) continue; // skip the original
      suggestions.add(h);
      if (suggestions.size() >= 30) break;
    }

    setSuggestions(suggestions, 0);
    // setSuggestions clears mHomophoneSuggestionsActive (any non-homophone update should), so
    // assert the flag AFTER the call so picks following showHomophonesFor are routed through
    // the homophone-replace path instead of the default append path.
    mHomophoneSuggestionsActive = true;
    mHomophoneSearchMode = false;

    // Anchor the cursor position so that a subsequent homophone pick only
    // replaces the originally-committed character. If the user moves the cursor
    // (touches elsewhere) before picking, validation in pickSuggestionManually
    // will detect the mismatch and skip the in-place replacement.
    InputConnection anchorIc = getCurrentInputConnection();
    if (anchorIc != null) {
      // character may contain surrogate pairs or, in theory, multiple code points; use the
      // full UTF-16 length so we replace the entire previously-committed character.
      int prevLen = character.length();
      mHomophoneAnchorCursorPos = getCursorPosition();
      mHomophoneAnchorPrevCharLen = prevLen;
    } else {
      mHomophoneAnchorCursorPos = -1;
      mHomophoneAnchorPrevCharLen = 0;
    }
  }

  protected void setSuggestions(
      @NonNull List<? extends CharSequence> suggestions, int highlightedSuggestionIndex) {
    // Any call into setSuggestions other than showHomophonesFor clears the
    // homophone-active flag so picks are interpreted normally.
    mHomophoneSuggestionsActive = false;
    // Radical IMEs (Boshiamy / Cangjie / Zhuyin) are exempt from the global
    // suggestions gate: their candidate strip IS the input mechanism, and they
    // run with prediction off (see isCurrentlyPredicting), so applying the gate
    // would blank out every candidate.
    if ((!mShowSuggestions || !isPredictionOn()) && !isRadicalInputMode()) {
      suggestions = Collections.emptyList();
      highlightedSuggestionIndex = -1;
    }
    mCancelSuggestionsAction.setCancelIconVisible(!suggestions.isEmpty());
    if (mCandidateView != null) {
      mCandidateView.setSuggestions(suggestions, highlightedSuggestionIndex);
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
                e -> Logger.w(TAG, e, "Failed to add word '%s' to user-dictionary!", word)));
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

  /**
   * Checks if the current keyboard declares radical input support. This does not depend on whether
   * radical dictionaries are actually loaded yet.
   */
  protected boolean isCurrentKeyboardRadical() {
    final AnyKeyboard keyboard = getCurrentAlphabetKeyboard();
    return keyboard != null && keyboard.isRadicalKeyboard();
  }

  /**
   * Returns the per-keyboard candidate-selector key sequence (e.g. "vrsfwlcbkj" for Boshiamy) or
   * {@code null} if the current keyboard does not advertise this feature.
   */
  @androidx.annotation.Nullable
  protected String currentRadicalCandidateSelectorKeys() {
    final AnyKeyboard keyboard = getCurrentAlphabetKeyboard();
    return keyboard == null ? null : keyboard.getRadicalCandidateSelectorKeys();
  }

  /**
   * Checks if radical input mode is fully active: the keyboard declares radical support AND radical
   * dictionaries are loaded and ready.
   */
  protected boolean isRadicalInputMode() {
    // Radical composition must work in any non-password field, including those
    // that disable normal word prediction via TYPE_TEXT_FLAG_NO_SUGGESTIONS
    // (Edge / Chrome address bars, search boxes, many in-page <input> fields).
    // We therefore gate on the explicit password marker, NOT on mPredictionOn,
    // otherwise the browser's NO_SUGGESTIONS hint would silently break Chinese
    // input. Password / visible-password / web-password fields still get plain
    // QWERTY behavior so radicals can't leak into privacy-sensitive contexts.
    //
    // We also gate on isInAlphabetKeyboardMode(): radical input is only active
    // while the radical alphabet keyboard is the visible view. On the
    // symbols/numbers keyboard the same field is still radical-capable
    // (isCurrentKeyboardRadical() stays true), but digits and punctuation typed
    // there must be emitted literally, exactly like every other keyboard,
    // instead of being captured into the radical buffer. Any composition that was
    // open when the user switched to symbols has already been committed by
    // commitPendingRadical() (see onSymbolsKeyboardSet).
    return !mInputFieldIsPassword
        && isInAlphabetKeyboardMode()
        && isCurrentKeyboardRadical()
        && mSuggest.hasRadicalDictionaries();
  }

  protected boolean isCurrentlyPredicting() {
    return (isPredictionOn() || isRadicalInputMode()) && !mWord.isEmpty();
  }

  protected boolean isAutoCorrect() {
    return mAutoCorrectOn && mInputFieldSupportsAutoPick && mPredictionOn;
  }

  public void performUpdateSuggestions() {
    mKeyboardHandler.removeMessages(KeyboardUIStateHandler.MSG_UPDATE_SUGGESTIONS);

    final boolean radicalMode = isRadicalInputMode();

    if (!radicalMode && (!isPredictionOn() || !mShowSuggestions)) {
      clearSuggestions();
      return;
    }

    if (radicalMode) {
      // In radical mode, query radical dictionaries instead of normal suggestion pipeline
      List<CharSequence> radicalSuggestions = mSuggest.getRadicalSuggestions(mWord);
      // Apply candidate-selector promotion if the user typed a selector key
      // (e.g. "v" after "fl" promotes the 2nd candidate of "fl" to the front).
      //
      // The promotion only applies while the radical buffer still matches the buffer that
      // was current when the selector key was pressed. If the user backspaced past the
      // selector key, the promotion is stale and we drop it silently.
      final String currentBuffer = mWord.getTypedWord().toString();
      if (mPromotedRadicalCandidate != null && !currentBuffer.equals(mPromotedRadicalBuffer)) {
        mPromotedRadicalCandidate = null;
        mPromotedRadicalBuffer = null;
      }
      if (mPromotedRadicalCandidate != null && !radicalSuggestions.isEmpty()) {
        final List<CharSequence> reordered = new ArrayList<>(radicalSuggestions.size() + 1);
        // index 0 is always the typed-radicals echo
        reordered.add(radicalSuggestions.get(0));
        // Promoted candidate goes second (highlighted slot)
        reordered.add(mPromotedRadicalCandidate);
        // Then the rest, deduped against the promoted candidate
        for (int i = 1; i < radicalSuggestions.size(); i++) {
          final CharSequence c = radicalSuggestions.get(i);
          if (!mPromotedRadicalCandidate.contentEquals(c)) {
            reordered.add(c);
          }
        }
        radicalSuggestions = reordered;
      }
      // If we have radical candidates (beyond just the typed radicals), highlight the first one
      int highlightedIndex = radicalSuggestions.size() > 1 ? 1 : -1;
      setSuggestions(radicalSuggestions, highlightedIndex);
      if (highlightedIndex >= 0) {
        mWord.setPreferredWord(radicalSuggestions.get(highlightedIndex));
      } else {
        mWord.setPreferredWord(null);
      }
      return;
    }

    final List<CharSequence> suggestionsList = mSuggest.getSuggestions(mWord);
    int highlightedSuggestionIndex = isAutoCorrect() ? mSuggest.getLastValidSuggestionIndex() : -1;

    // Don't auto-correct words with multiple capital letter
    if (highlightedSuggestionIndex == 1 && mWord.isMostlyCaps()) highlightedSuggestionIndex = -1;

    setSuggestions(suggestionsList, highlightedSuggestionIndex);
    if (highlightedSuggestionIndex >= 0) {
      mWord.setPreferredWord(suggestionsList.get(highlightedSuggestionIndex));
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
    mWordRevertLength = 0; // no reverts
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
      // Handle homophone picks: when the candidate strip is showing homophone
      // results, any pick should replace the previously-committed character and
      // surface the picked character's radical code in the composing area.
      String actualChar = suggestion.toString();
      boolean isHomophonePick = mHomophoneSuggestionsActive && isRadicalInputMode();
      String radicalCodeHint = isHomophonePick ? mSuggest.getRadicalCode(actualChar) : null;

      if (isHomophonePick) {
        // Replace the previously-committed character with the picked homophone
        // ONLY IF the cursor is still where homophones were originally surfaced.
        // If the user moved the cursor in between, fall back to a normal commit
        // (no replacement) to avoid clobbering arbitrary text.
        InputConnection hpIc = getCurrentInputConnection();
        final int currentCursor = getCursorPosition();
        final boolean anchorValid =
            mHomophoneAnchorCursorPos >= mHomophoneAnchorPrevCharLen
                && currentCursor == mHomophoneAnchorCursorPos
                && mHomophoneAnchorPrevCharLen > 0;
        if (hpIc != null && anchorValid) {
          markExpectingSelectionUpdate();
          hpIc.setComposingRegion(
              mHomophoneAnchorCursorPos - mHomophoneAnchorPrevCharLen, mHomophoneAnchorCursorPos);
          hpIc.setComposingText(actualChar, 1);
          hpIc.finishComposingText();
        } else if (hpIc != null) {
          // Anchor stale, just append the picked character at the cursor.
          hpIc.commitText(actualChar, 1);
        }
        // Show radical code as composing hint + next-word suggestions
        List<CharSequence> nextSuggestions = new ArrayList<>();
        if (radicalCodeHint != null && !radicalCodeHint.isEmpty()) {
          nextSuggestions.add(radicalCodeHint);
        }
        nextSuggestions.addAll(mSuggest.getNextSuggestions(actualChar, false));
        setSuggestions(nextSuggestions, -1);
        // Ensure homophone search state is fully cleared so a subsequent toggle works.
        mHomophoneSearchMode = false;
        mHomophoneSuggestionsActive = false;
        mHomophoneAnchorCursorPos = -1;
        mHomophoneAnchorPrevCharLen = 0;
        mJustAutoAddedWord = false;
        return; // skip auto-correct/dictionary logic which would overwrite suggestions
      } else {
        commitWordToInput(
            suggestion,
            suggestion /*user physically picked a word from the suggestions strip. this is not a fix*/);
      }

      // Follow it with a space (not in radical mode, space is the commit key)
      if (withAutoSpaceEnabled
          && !isRadicalInputMode()
          && (index == 0 || !typedWord.isAtTagsSearchState())) {
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
            !isRadicalInputMode()
                && !mJustAutoAddedWord
                && index == 0
                && mShowSuggestions
                && !mSuggest.isValidWord(suggestion) // this is for the case that the word was
                // auto-added upon picking
                && !mSuggest.isValidWord(
                    suggestion.toString().toLowerCase(getCurrentAlphabetKeyboard().getLocale()));

        if (showingAddToDictionaryHint) {
          if (mCandidateView != null) mCandidateView.showAddToDictionaryHint(suggestion);
        } else {
          setSuggestions(mSuggest.getNextSuggestions(suggestion, mWord.isAllUpperCase()), -1);
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
                ic, getCursorPosition() - typedWord.length(), typedWord, wordToCommit);
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

  protected void setSpaceTimeStamp(boolean isSpace) {
    if (isSpace) {
      mLastSpaceTimeStamp = SystemClock.uptimeMillis();
    } else {
      mLastSpaceTimeStamp = NEVER_TIME_STAMP;
    }
  }

  @Override
  public void onSymbolsKeyboardSet(@NonNull AnyKeyboard keyboard) {
    // Leaving a radical alphabet keyboard for the symbols/numbers view: 送出 (commit) any
    // in-progress radical composition so it is flushed to the field instead of being held as a
    // fragile composing buffer. getCurrentAlphabetKeyboard() (used by commitPendingRadical via
    // isCurrentKeyboardRadical()) still points at the radical keyboard here, because super sets
    // mCurrentSymbolsKeyboard without touching the selected alphabet keyboard. No-op for
    // non-radical keyboards.
    commitPendingRadical();
    super.onSymbolsKeyboardSet(keyboard);
  }

  @Override
  public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
    // Per design, leaving a radical keyboard always 送出 (commits) the in-progress radical
    // composition rather than abandoning it. onAlphabetKeyboardSet fires for the NEW keyboard, so
    // getCurrentAlphabetKeyboard() still returns the OLD keyboard (super has not run yet).
    //
    // It also fires when the user merely returns from the symbols/numbers keyboard to the SAME
    // radical keyboard; in that case the id is unchanged and the buffer was already committed on
    // the way out (onSymbolsKeyboardSet), so there is nothing to do.
    final AnyKeyboard previousAlphabetKeyboard = getCurrentAlphabetKeyboard();
    final boolean alphabetKeyboardChanged =
        previousAlphabetKeyboard == null
            || !TextUtils.equals(
                previousAlphabetKeyboard.getKeyboardId(), keyboard.getKeyboardId());
    if (alphabetKeyboardChanged) {
      if (previousAlphabetKeyboard != null && previousAlphabetKeyboard.isRadicalKeyboard()) {
        // Switching out of a radical keyboard to a different keyboard: commit the radicals.
        commitPendingRadical();
      } else if (keyboard.isRadicalKeyboard() && mWord.charCount() > 0) {
        // Switching INTO a radical keyboard while a foreign (non-radical) composition is still
        // open (e.g. a half-typed English word): per design every keyboard switch 送出 (commits)
        // the buffer rather than discarding or carrying it over. Commit exactly what was typed
        // (no forced autocorrect) so it is flushed to the field, then reset the cross-keyboard
        // undo-commit state so the committed word can't be reverted back into the radical
        // keyboard's buffer. commitWordToInput finishes the composing region for us, so no
        // dangling composing text is left behind on WebView-backed fields.
        final WordComposer typedWord = prepareWordComposerForNextWord();
        commitWordToInput(typedWord.getTypedWord(), typedWord.getTypedWord());
        mWordRevertLength = 0;
        mPreviousWord.reset();
      }
    }

    super.onAlphabetKeyboardSet(keyboard);

    // Update candidate strip visibility for radical keyboards that need it
    // even when normal prediction is off
    final KeyboardViewContainerView inputViewContainer = getInputViewContainer();
    if (inputViewContainer != null) {
      inputViewContainer.setActionsStripVisibility(isPredictionOn() || isCurrentKeyboardRadical());
    }

    final Locale locale = keyboard.getLocale();
    mFrenchSpacePunctuationBehavior =
        mSwapPunctuationAndSpace && locale.toString().toLowerCase(Locale.ROOT).startsWith("fr");
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
      // Defensive: revertLastWord swaps the active composer, so any pending radical-selector
      // promotion that was attached to the previous composition is no longer applicable.
      mPromotedRadicalCandidate = null;
      mPromotedRadicalBuffer = null;
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
    return !isAlphabet(code);
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
        setSuggestions(stringList, -1);
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
                        TAG, e, "Failed to remove word '%s' from user-dictionary!", wordToRemove)));
    mJustAutoAddedWord = false;
    abortCorrectionAndResetPredictionState(false);
  }

  @VisibleForTesting
  static class CancelSuggestionsAction implements KeyboardViewContainerView.StripActionProvider {
    @NonNull private final Runnable mCancelPrediction;
    private Animator mCancelToGoneAnimation;
    private Animator mCancelToVisibleAnimation;
    private Animator mCloseTextToVisibleToGoneAnimation;
    private View mRootView;
    private View mCloseText;
    @Nullable private CandidateView mCandidateView;

    CancelSuggestionsAction(@NonNull Runnable cancelPrediction) {
      mCancelPrediction = cancelPrediction;
    }

    @Override
    public @NonNull View inflateActionView(@NonNull ViewGroup parent) {
      final Context context = parent.getContext();
      mCancelToGoneAnimation =
          AnimatorInflater.loadAnimator(context, R.animator.suggestions_cancel_to_gone);
      mCancelToGoneAnimation.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              mRootView.setVisibility(View.GONE);
            }
          });
      mCancelToVisibleAnimation =
          AnimatorInflater.loadAnimator(context, R.animator.suggestions_cancel_to_visible);
      mCloseTextToVisibleToGoneAnimation =
          AnimatorInflater.loadAnimator(
              context, R.animator.suggestions_cancel_text_to_visible_to_gone);
      mCloseTextToVisibleToGoneAnimation.addListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              super.onAnimationEnd(animation);
              mCloseText.setVisibility(View.GONE);
            }
          });
      mRootView =
          LayoutInflater.from(context).inflate(R.layout.cancel_suggestions_action, parent, false);

      mCloseText = mRootView.findViewById(R.id.close_suggestions_strip_text);
      ImageView closeIcon = mRootView.findViewById(R.id.close_suggestions_strip_icon);
      if (mCandidateView != null) {
        closeIcon.setImageDrawable(mCandidateView.getCloseIcon());
      }
      mRootView.setOnClickListener(
          view -> {
            if (mCloseText.getVisibility() == View.VISIBLE) {
              // already shown, so just cancel suggestions.
              mCancelPrediction.run();
            } else {
              mCloseText.setVisibility(View.VISIBLE);
              mCloseText.setPivotX(mCloseText.getWidth());
              mCloseText.setPivotY(mCloseText.getHeight() / 2f);
              mCloseTextToVisibleToGoneAnimation.setTarget(mCloseText);
              mCloseTextToVisibleToGoneAnimation.start();
            }
          });

      return mRootView;
    }

    @Override
    public void onRemoved() {
      mCloseTextToVisibleToGoneAnimation.cancel();
      mCancelToGoneAnimation.cancel();
      mCancelToVisibleAnimation.cancel();
    }

    void setOwningCandidateView(@NonNull CandidateView view) {
      mCandidateView = view;
    }

    void setCancelIconVisible(boolean visible) {
      if (mRootView != null) {
        final int visibility = visible ? View.VISIBLE : View.GONE;
        if (mRootView.getVisibility() != visibility) {
          mRootView.setVisibility(View.VISIBLE); // just to make sure
          Animator anim = visible ? mCancelToVisibleAnimation : mCancelToGoneAnimation;
          anim.setTarget(mRootView);
          anim.start();
        }
      }
    }
  }
}
