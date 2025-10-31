package com.anysoftkeyboard.ime;

import android.content.ComponentCallbacks2;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.android.PowerSaving;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.GenericAutoClose;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.WordComposer;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

  public static final long MINIMUM_GESTURE_TIME_MS = 40;
  private static final int MAX_CACHED_DETECTORS = 4;

  private boolean mGestureTypingEnabled;
  protected final Map<String, GestureTypingDetector> mGestureTypingDetectors =
      new LinkedHashMap<String, GestureTypingDetector>(4, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, GestureTypingDetector> eldest) {
          if (size() > MAX_CACHED_DETECTORS) {
            Logger.d(TAG, "LRU evicting detector for key: %s", eldest.getKey());
            eldest.getValue().destroy();
            return true;
          }
          return false;
        }
      };
  @Nullable private GestureTypingDetector mCurrentGestureDetector;
  private boolean mDetectorReady = false;
  private boolean mJustPerformedGesture = false;
  private boolean mGestureShifted = false;

  @NonNull private Disposable mDetectorStateSubscription = Disposables.disposed();
  private long mGestureStartTime;
  private long mGestureLastTime;

  private long mMinimumGesturePathLength;
  private long mGesturePathLength;

  protected static String getKeyForDetector(@NonNull AnyKeyboard keyboard) {
    return String.format(
        Locale.US,
        "%s,%d,%d",
        keyboard.getKeyboardId(),
        keyboard.getMinWidth(),
        keyboard.getHeight());
  }

  @VisibleForTesting protected ClearGestureStripActionProvider mClearLastGestureAction;

  @Override
  public void onCreate() {
    super.onCreate();

    mClearLastGestureAction = new ClearGestureStripActionProvider(this);
    addDisposable(
        Observable.combineLatest(
                PowerSaving.observePowerSavingState(
                    getApplicationContext(), R.string.settings_key_power_save_mode_gesture_control),
                prefs()
                    .getBoolean(
                        R.string.settings_key_gesture_typing,
                        R.bool.settings_default_gesture_typing)
                    .asObservable(),
                (powerState, gestureTyping) -> {
                  if (powerState) return false;
                  return gestureTyping;
                })
            .subscribe(
                enabled -> {
                  mGestureTypingEnabled = enabled;
                  mDetectorStateSubscription.dispose();
                  if (!mGestureTypingEnabled) {
                    destroyAllDetectors();
                  } else {
                    final AnyKeyboard currentAlphabetKeyboard = getCurrentAlphabetKeyboard();
                    if (currentAlphabetKeyboard != null) {
                      setupGestureDetector(currentAlphabetKeyboard);
                    }
                  }
                },
                GenericOnError.onError("settings_key_gesture_typing")));
  }

  @Override
  public void onStartInputView(EditorInfo info, boolean restarting) {
    super.onStartInputView(info, restarting);

    getInputViewContainer().addStripAction(mClearLastGestureAction, true);
    mClearLastGestureAction.setVisibility(View.GONE);
    // the gesture path must be less than a key width, usually, 10%s.
    // but we need to square it, since we are dealing with distances. See addPoint method.
    final long width = (long) (getResources().getDisplayMetrics().widthPixels * 0.045f);
    mMinimumGesturePathLength = width * width;
  }

  @Override
  public void onFinishInputView(boolean finishInput) {
    getInputViewContainer().removeStripAction(mClearLastGestureAction);

    super.onFinishInputView(finishInput);
  }

  @Override
  public void onFinishInput() {
    mClearLastGestureAction.setVisibility(View.GONE);

    super.onFinishInput();
  }

  private void destroyAllDetectors() {
    for (GestureTypingDetector gestureTypingDetector : mGestureTypingDetectors.values()) {
      gestureTypingDetector.destroy();
    }
    mGestureTypingDetectors.clear();
    mCurrentGestureDetector = null;
    mDetectorReady = false;
    setupInputViewWatermark();
  }

  @Override
  public void onAddOnsCriticalChange() {
    super.onAddOnsCriticalChange();
    destroyAllDetectors();
  }

  private void setupGestureDetector(@NonNull AnyKeyboard keyboard) {
    mDetectorStateSubscription.dispose();
    if (mGestureTypingEnabled) {
      final String key = getKeyForDetector(keyboard);
      mCurrentGestureDetector = mGestureTypingDetectors.get(key);
      if (mCurrentGestureDetector == null) {
        mCurrentGestureDetector =
            new GestureTypingDetector(
                getResources().getDimension(R.dimen.gesture_typing_frequency_factor),
                15 /*max suggestions. For now it is static*/,
                getResources().getDimensionPixelSize(R.dimen.gesture_typing_min_point_distance),
                keyboard.getKeys());
        mGestureTypingDetectors.put(key, mCurrentGestureDetector);
      }

      mDetectorStateSubscription =
          mCurrentGestureDetector
              .state()
              .doOnDispose(
                  () -> {
                    Logger.d(TAG, "mCurrentGestureDetector state disposed");
                    mDetectorReady = false;
                    setupInputViewWatermark();
                  })
              .subscribe(
                  state -> {
                    Logger.d(TAG, "mCurrentGestureDetector state changed to %s", state);
                    mDetectorReady = state == GestureTypingDetector.LoadingState.LOADED;
                    setupInputViewWatermark();
                  },
                  e -> {
                    Logger.d(TAG, "mCurrentGestureDetector state ERROR %s", e.getMessage());
                    mDetectorReady = false;
                    setupInputViewWatermark();
                  });
    }
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    final GestureTypingDetector currentGestureDetector = mCurrentGestureDetector;
    if (currentGestureDetector != null) {
      // copying to a list so deleting detectors from the map will not change our iteration
      List<Map.Entry<String, GestureTypingDetector>> allDetectors =
          new ArrayList<>(mGestureTypingDetectors.entrySet());
      for (Map.Entry<String, GestureTypingDetector> pair : allDetectors) {
        if (pair.getValue() != currentGestureDetector) {
          pair.getValue().destroy();
          mGestureTypingDetectors.remove(pair.getKey());
        }
      }
    } else {
      destroyAllDetectors();
    }
  }

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);

    Logger.d(TAG, "onTrimMemory() called with level %d", level);

    // TRIM_MEMORY_RUNNING_MODERATE = 5 (Device running moderately low on memory)
    // TRIM_MEMORY_RUNNING_LOW = 10 (Device running significantly low on memory)
    // Clear non-active detectors to free memory before we hit critical levels
    if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE) {
      final GestureTypingDetector currentGestureDetector = mCurrentGestureDetector;
      if (currentGestureDetector != null) {
        // Keep current detector, destroy all others
        Logger.d(TAG, "Clearing non-active detectors, keeping current");
        List<Map.Entry<String, GestureTypingDetector>> allDetectors =
            new ArrayList<>(mGestureTypingDetectors.entrySet());
        int clearedCount = 0;
        for (Map.Entry<String, GestureTypingDetector> pair : allDetectors) {
          if (pair.getValue() != currentGestureDetector) {
            pair.getValue().destroy();
            mGestureTypingDetectors.remove(pair.getKey());
            clearedCount++;
          }
        }
        Logger.d(TAG, "Cleared %d non-active detectors", clearedCount);
      } else {
        Logger.d(TAG, "No current detector, clearing all");
        destroyAllDetectors();
      }
    }

    // At higher memory pressure, also trim the current detector
    if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
      if (mCurrentGestureDetector != null) {
        Logger.d(TAG, "Trimming current detector memory");
        mCurrentGestureDetector.trimMemory();
      }
    }
  }

  public static class WordListDictionaryListener implements DictionaryBackgroundLoader.Listener {

    private void onGetWordsFinished(char[][] words, int[] frequencies) {
      if (words.length > 0) {
        if (frequencies.length != words.length) {
          throw new IllegalArgumentException(
              "words and frequencies do not have the same length ("
                  + words.length
                  + ", "
                  + frequencies.length
                  + ")");
        }

        mWords.add(words);
        mWordFrequencies.add(frequencies);
      }
      Logger.d(
          "WordListDictionaryListener",
          "onDictionaryLoadingDone got words with length %d",
          words.length);
    }

    public interface Callback {
      void consumeWords(AnyKeyboard keyboard, List<char[][]> words, List<int[]> wordFrequencies);
    }

    private ArrayList<char[][]> mWords = new ArrayList<>();
    private final ArrayList<int[]> mWordFrequencies = new ArrayList<>();
    private final Callback mOnLoadedCallback;
    private final AtomicInteger mExpectedDictionaries = new AtomicInteger(0);
    private final AnyKeyboard mKeyboard;

    WordListDictionaryListener(AnyKeyboard keyboard, Callback wordsConsumer) {
      mKeyboard = keyboard;
      mOnLoadedCallback = wordsConsumer;
    }

    @Override
    public void onDictionaryLoadingStarted(Dictionary dictionary) {
      mExpectedDictionaries.incrementAndGet();
    }

    @Override
    public void onDictionaryLoadingDone(Dictionary dictionary) {
      final int expectedDictionaries = mExpectedDictionaries.decrementAndGet();
      Logger.d("WordListDictionaryListener", "onDictionaryLoadingDone for %s", dictionary);
      try {
        dictionary.getLoadedWords(this::onGetWordsFinished);
      } catch (Exception e) {
        Logger.w(
            "WordListDictionaryListener",
            e,
            "onDictionaryLoadingDone got exception from dictionary.");
      }

      if (expectedDictionaries == 0) doCallback();
    }

    private void doCallback() {
      mOnLoadedCallback.consumeWords(mKeyboard, mWords, mWordFrequencies);
      mWords = new ArrayList<>();
    }

    @Override
    public void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception) {
      final int expectedDictionaries = mExpectedDictionaries.decrementAndGet();
      Logger.e(
          "WordListDictionaryListener",
          exception,
          "onDictionaryLoadingFailed for %s with error %s",
          dictionary,
          exception.getMessage());
      if (expectedDictionaries == 0) doCallback();
    }
  }

  @NonNull
  @Override
  protected DictionaryBackgroundLoader.Listener getDictionaryLoadedListener(
      @NonNull AnyKeyboard currentAlphabetKeyboard) {
    if (mGestureTypingEnabled && !mDetectorReady) {
      return new WordListDictionaryListener(currentAlphabetKeyboard, this::onDictionariesLoaded);
    } else {
      return super.getDictionaryLoadedListener(currentAlphabetKeyboard);
    }
  }

  private void onDictionariesLoaded(
      AnyKeyboard keyboard, List<char[][]> newWords, List<int[]> wordFrequencies) {
    if (mGestureTypingEnabled && mCurrentGestureDetector != null) {
      // it might be null if the IME service started with enabled flag set to true. In that
      // case
      // the keyboard object will not be ready yet.
      final String key = getKeyForDetector(keyboard);
      if (mGestureTypingDetectors.containsKey(key)) {
        final GestureTypingDetector detector = mGestureTypingDetectors.get(key);
        detector.setWords(newWords, wordFrequencies);
      } else {
        Logger.wtf(TAG, "Could not find detector for key %s", key);
      }
    }
  }

  /**
   * When alphabet keyboard loaded, we start loading our gesture-typing word corners data. It is
   * earlier than the first time we click on the keyboard.
   */
  @Override
  public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
    super.onAlphabetKeyboardSet(keyboard);

    if (mGestureTypingEnabled) {
      setupGestureDetector(keyboard);
    }
  }

  @Override
  public void onSymbolsKeyboardSet(@NonNull AnyKeyboard keyboard) {
    super.onSymbolsKeyboardSet(keyboard);
    mDetectorStateSubscription.dispose();
    mCurrentGestureDetector = null;
    mDetectorReady = false;
    setupInputViewWatermark();
  }

  @Override
  public boolean onGestureTypingInputStart(int x, int y, AnyKeyboard.AnyKey key, long eventTime) {
    final GestureTypingDetector currentGestureDetector = mCurrentGestureDetector;
    if (mGestureTypingEnabled && currentGestureDetector != null && isValidGestureTypingStart(key)) {
      mGestureShifted = mShiftKeyState.isActive();
      // we can call this as many times as we want, it has a short-circuit check.
      confirmLastGesture(mPrefsAutoSpace);
      mGestureStartTime = eventTime;
      mGesturePathLength = 0;
      currentGestureDetector.clearGesture();
      onGestureTypingInput(x, y, eventTime);

      return true;
    }

    return false;
  }

  private static boolean isValidGestureTypingStart(AnyKeyboard.AnyKey key) {
    if (key.isFunctional()) {
      return false;
    } else {
      final int primaryCode = key.getPrimaryCode();
      if (primaryCode <= 0) {
        return false;
      } else {
        switch (primaryCode) {
          case KeyCodes.SPACE:
          case KeyCodes.ENTER:
            return false;
          default:
            return true;
        }
      }
    }
  }

  @Override
  public void onGestureTypingInput(int x, int y, long eventTime) {
    if (!mGestureTypingEnabled) return;
    final GestureTypingDetector currentGestureDetector = mCurrentGestureDetector;
    if (currentGestureDetector != null) {
      mGestureLastTime = eventTime;
      mGesturePathLength += currentGestureDetector.addPoint(x, y);
    }
  }

  @Override
  public boolean onGestureTypingInputDone() {
    if (!mGestureTypingEnabled) return false;
    if (mGestureLastTime - mGestureStartTime < MINIMUM_GESTURE_TIME_MS) return false;
    if (mGesturePathLength < mMinimumGesturePathLength) return false;

    InputConnection ic = getCurrentInputConnection();

    final GestureTypingDetector currentGestureDetector = mCurrentGestureDetector;
    if (ic != null && currentGestureDetector != null) {
      ArrayList<String> gestureTypingPossibilities = currentGestureDetector.getCandidates();

      if (!gestureTypingPossibilities.isEmpty()) {
        final boolean isShifted = mGestureShifted;
        final boolean isCapsLocked = mShiftKeyState.isLocked();

        final Locale locale = getCurrentAlphabetKeyboard().getLocale();
        if (isShifted || isCapsLocked) {

          StringBuilder builder = new StringBuilder();
          for (int i = 0; i < gestureTypingPossibilities.size(); ++i) {
            final CharSequence word = gestureTypingPossibilities.get(i);
            if (isCapsLocked) {
              gestureTypingPossibilities.set(i, word.toString().toUpperCase(locale));
            } else {
              builder.append(word.subSequence(0, 1).toString().toUpperCase(locale));
              builder.append(word.subSequence(1, word.length()));
              gestureTypingPossibilities.set(i, builder.toString());
              builder.setLength(0);
            }
          }
        }

        try (var closer = GenericAutoClose.batchEdit(ic)) {
          closer.noop();
          abortCorrectionAndResetPredictionState(false);

          CharSequence word = gestureTypingPossibilities.get(0);

          // This is used when correcting
          final WordComposer currentComposedWord = getCurrentComposedWord();
          currentComposedWord.reset();
          currentComposedWord.setAutoCapitalized(isShifted || isCapsLocked);
          currentComposedWord.simulateTypedWord(word);

          currentComposedWord.setPreferredWord(currentComposedWord.getTypedWord());
          // If there's any non-separator before the cursor, add a space:
          // TODO: Improve the detection of mid-word separations (not hardcode a hyphen and an
          // apostrophe),
          // and disable this check on URL tex fields.
          CharSequence toLeft = ic.getTextBeforeCursor(MAX_CHARS_PER_CODE_POINT, 0);
          if (toLeft == null) {
            Logger.w(
                TAG,
                "InputConnection was not null, but return null from getTextBeforeCursor. Assuming"
                    + " this means the ic is dead.");
            return false;
          } else if (toLeft.length() == 0) {
            Logger.v(TAG, "Beginning of text found, not adding a space.");
          } else {
            int lastCodePoint = Character.codePointBefore(toLeft, toLeft.length());
            if (Character.isWhitespace(lastCodePoint)
                || lastCodePoint == (int) '\''
                || lastCodePoint == (int) '-') {
              Logger.v(TAG, "Separator found, not adding a space.");
            } else {
              ic.commitText(new String(new int[] {KeyCodes.SPACE}, 0, 1), 1);
              Logger.v(TAG, "Non-separator found, adding a space.");
            }
          }
          ic.setComposingText(currentComposedWord.getTypedWord(), 1);

          mJustPerformedGesture = true;
          mClearLastGestureAction.setVisibility(View.VISIBLE);

          if (gestureTypingPossibilities.size() > 1) {
            setSuggestions(gestureTypingPossibilities, 0);
          } else {
            // clearing any suggestion shown
            setSuggestions(Collections.emptyList(), -1);
          }

          markExpectingSelectionUpdate();

          return true;
        }
      }

      currentGestureDetector.clearGesture();
    }
    return false;
  }

  @NonNull
  @Override
  protected List<Drawable> generateWatermark() {
    final List<Drawable> watermark = super.generateWatermark();
    if (mGestureTypingEnabled) {
      if (mDetectorReady) {
        watermark.add(ContextCompat.getDrawable(this, R.drawable.ic_watermark_gesture));
      } else if (mCurrentGestureDetector != null) {
        watermark.add(ContextCompat.getDrawable(this, R.drawable.ic_watermark_gesture_not_loaded));
      }
    }

    return watermark;
  }

  @Override
  public void onKey(
      int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
    if (mGestureTypingEnabled && mJustPerformedGesture && primaryCode > 0 /*printable character*/) {
      confirmLastGesture(primaryCode != KeyCodes.SPACE && mPrefsAutoSpace);
    } else if (primaryCode == KeyCodes.DELETE) {
      mClearLastGestureAction.setVisibility(View.GONE);
    }
    mJustPerformedGesture = false;

    super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
  }

  private void confirmLastGesture(boolean withAutoSpace) {
    if (mJustPerformedGesture) {
      pickSuggestionManually(0, getCurrentComposedWord().getTypedWord(), withAutoSpace);
      mClearLastGestureAction.setVisibility(View.GONE);
    }
  }

  @Override
  public void pickSuggestionManually(
      int index, CharSequence suggestion, boolean withAutoSpaceEnabled) {
    mJustPerformedGesture = false;
    super.pickSuggestionManually(index, suggestion, withAutoSpaceEnabled);
  }

  protected static class ClearGestureStripActionProvider
      implements KeyboardViewContainerView.StripActionProvider {
    private final AnySoftKeyboardWithGestureTyping mKeyboard;
    private View mRootView;

    ClearGestureStripActionProvider(@NonNull AnySoftKeyboardWithGestureTyping keyboard) {
      mKeyboard = keyboard;
    }

    @Override
    public @NonNull View inflateActionView(@NonNull ViewGroup parent) {
      mRootView =
          LayoutInflater.from(mKeyboard).inflate(R.layout.clear_gesture_action, parent, false);
      mRootView.setOnClickListener(
          view -> {
            InputConnection ic = mKeyboard.getCurrentInputConnection();
            mKeyboard.handleBackWord(ic);
            mKeyboard.mJustPerformedGesture = false;
            var prefs = AnyApplication.prefs(mKeyboard);
            var timesShown =
                prefs.getInteger(
                    R.string.settings_key_show_slide_for_gesture_back_word_counter,
                    R.integer.settings_default_zero_value);
            Integer counter = timesShown.get();
            if (counter < 3) {
              timesShown.set(counter + 1);
              Toast.makeText(
                      mKeyboard.getApplicationContext(),
                      R.string.tip_swipe_from_backspace_to_clear,
                      counter == 0 ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)
                  .show();
            }
            setVisibility(View.GONE);
          });

      return mRootView;
    }

    @Override
    public void onRemoved() {
      mRootView = null;
    }

    void setVisibility(int visibility) {
      if (mRootView != null) {
        mRootView.setVisibility(visibility);
      }
    }

    @VisibleForTesting
    int getVisibility() {
      return mRootView.getVisibility();
    }
  }
}
