package com.anysoftkeyboard.ime;

import android.graphics.drawable.Drawable;
import android.view.inputmethod.InputConnection;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.android.PowerSaving;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.WordComposer;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;
    protected final Map<String, GestureTypingDetector> mGestureTypingDetectors = new HashMap<>();
    @Nullable private GestureTypingDetector mCurrentGestureDetector;
    private boolean mDetectorReady = false;
    private boolean mJustPerformedGesture = false;

    @NonNull private Disposable mDetectorStateSubscription = Disposables.disposed();

    protected static String getKeyForDetector(@NonNull AnyKeyboard keyboard) {
        return String.format(
                Locale.US,
                "%s,%d,%d",
                keyboard.getKeyboardId(),
                keyboard.getMinWidth(),
                keyboard.getHeight());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        addDisposable(
                Observable.combineLatest(
                                PowerSaving.observePowerSavingState(
                                        getApplicationContext(),
                                        R.string.settings_key_power_save_mode_gesture_control),
                                prefs().getBoolean(
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
                                        final AnyKeyboard currentAlphabetKeyboard =
                                                getCurrentAlphabetKeyboard();
                                        if (currentAlphabetKeyboard != null) {
                                            setupGestureDetector(currentAlphabetKeyboard);
                                        }
                                    }
                                },
                                GenericOnError.onError("settings_key_gesture_typing")));
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
            if (mGestureTypingDetectors.containsKey(key)) {
                mCurrentGestureDetector = mGestureTypingDetectors.get(key);
            } else {
                mCurrentGestureDetector =
                        new GestureTypingDetector(
                                getResources()
                                        .getDimension(R.dimen.gesture_typing_frequency_factor),
                                15 /*max suggestions. For now it is static*/,
                                getResources()
                                        .getDimensionPixelSize(
                                                R.dimen.gesture_typing_min_point_distance),
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
                                        Logger.d(
                                                TAG,
                                                "mCurrentGestureDetector state changed to %s",
                                                state);
                                        mDetectorReady =
                                                state == GestureTypingDetector.LoadingState.LOADED;
                                        setupInputViewWatermark();
                                    },
                                    e -> {
                                        Logger.d(
                                                TAG,
                                                "mCurrentGestureDetector state ERROR %s",
                                                e.getMessage());
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
            void consumeWords(
                    AnyKeyboard keyboard, List<char[][]> words, List<int[]> wordFrequencies);
        }

        private ArrayList<char[][]> mWords = new ArrayList<>();
        private ArrayList<int[]> mWordFrequencies = new ArrayList<>();
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
            return new WordListDictionaryListener(
                    currentAlphabetKeyboard, this::onDictionariesLoaded);
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
        if (mGestureTypingEnabled
                && currentGestureDetector != null
                && isValidGestureTypingStart(key)) {
            // we can call this as many times as we want, it has a short-circuit check.
            confirmLastGesture(mPrefsAutoSpace);

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
            currentGestureDetector.addPoint(x, y);
        }
    }

    @NonNull
    @Override
    protected List<Drawable> generateWatermark() {
        final List<Drawable> watermark = super.generateWatermark();
        if (mGestureTypingEnabled) {
            if (mDetectorReady) {
                watermark.add(ContextCompat.getDrawable(this, R.drawable.ic_watermark_gesture));
            } else if (mCurrentGestureDetector != null) {
                watermark.add(
                        ContextCompat.getDrawable(
                                this, R.drawable.ic_watermark_gesture_not_loaded));
            }
        }
        return watermark;
    }

    @Override
    public void onKey(
            int primaryCode,
            Keyboard.Key key,
            int multiTapIndex,
            int[] nearByKeyCodes,
            boolean fromUI) {
        if (mGestureTypingEnabled
                && mJustPerformedGesture
                && primaryCode > 0 /*printable character*/) {
            confirmLastGesture(primaryCode != KeyCodes.SPACE && mPrefsAutoSpace);
        }
        mJustPerformedGesture = false;

        super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
    }

    private void confirmLastGesture(boolean withAutoSpace) {
        if (mJustPerformedGesture) {
            pickSuggestionManually(0, getCurrentComposedWord().getTypedWord(), withAutoSpace);
        }
    }

    @Override
    public void onGestureTypingInputDone() {
        if (!mGestureTypingEnabled) return;

        InputConnection ic = getCurrentInputConnection();

        final GestureTypingDetector currentGestureDetector = mCurrentGestureDetector;
        if (ic != null && currentGestureDetector != null) {
            ArrayList<String> gestureTypingPossibilities = currentGestureDetector.getCandidates();

            if (!gestureTypingPossibilities.isEmpty()) {
                final boolean isShifted = mShiftKeyState.isActive();
                final boolean isCapsLocked = mShiftKeyState.isLocked();

                final Locale locale = getCurrentAlphabetKeyboard().getLocale();
                if (locale != null && (isShifted || isCapsLocked)) {

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

                ic.beginBatchEdit();
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
                if (toLeft.length() == 0) {
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

                if (gestureTypingPossibilities.size() > 1) {
                    setSuggestions(gestureTypingPossibilities, true, true);
                } else {
                    // clearing any suggestion shown
                    setSuggestions(Collections.emptyList(), false, false);
                }

                ic.endBatchEdit();
            }

            currentGestureDetector.clearGesture();
        }
    }

    @Override
    public void pickSuggestionManually(
            int index, CharSequence suggestion, boolean withAutoSpaceEnabled) {
        mJustPerformedGesture = false;
        super.pickSuggestionManually(index, suggestion, withAutoSpaceEnabled);
    }
}
