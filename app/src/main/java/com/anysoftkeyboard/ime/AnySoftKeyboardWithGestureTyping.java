package com.anysoftkeyboard.ime;

import android.support.annotation.NonNull;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.TextEntryState;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.functions.Consumer;

public abstract class AnySoftKeyboardWithGestureTyping extends AnySoftKeyboardWithQuickText {

    private boolean mGestureTypingEnabled;
    protected GestureTypingDetector mGestureTypingDetector;
    private final DictionaryBackgroundLoader.Listener mWordListDictionaryListener = new WordListDictionaryListener(this::onDictionariesLoaded);
    private final DictionaryBackgroundLoader.Listener mNoOpListener = new DictionaryBackgroundLoader.Listener() {

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

    @Override
    public void onCreate() {
        super.onCreate();

        addDisposable(prefs().getBoolean(R.string.settings_key_gesture_typing, R.bool.settings_default_gesture_typing)
                .asObservable().subscribe(enabled -> {
                    mGestureTypingEnabled = enabled;
                    if (mGestureTypingDetector != null && !mGestureTypingEnabled) {
                        mGestureTypingDetector.destroy();
                        mGestureTypingDetector = null;
                    } else if (mGestureTypingDetector == null && mGestureTypingEnabled) {
                        mGestureTypingDetector = new GestureTypingDetector(getResources().getDimensionPixelSize(R.dimen.gesture_typing_curvature));
                    }
                }, GenericOnError.onError("settings_key_gesture_typing")));
    }

    public static class WordListDictionaryListener implements DictionaryBackgroundLoader.Listener {

        private ArrayList<char[][]> mWords = new ArrayList<>();
        private final Consumer<List<char[][]>> mOnLoadedCallback;
        private AtomicInteger mExpectedDictionaries = new AtomicInteger(0);

        WordListDictionaryListener(Consumer<List<char[][]>> wordsProvider) {
            mOnLoadedCallback = wordsProvider;
        }

        @Override
        public void onDictionaryLoadingStarted(Dictionary dictionary) {
            mExpectedDictionaries.incrementAndGet();
        }

        @Override
        public void onDictionaryLoadingDone(Dictionary dictionary) {
            final int expectedDictionaries = mExpectedDictionaries.decrementAndGet();
            Logger.d("WordListDictionaryListener", "onDictionaryLoadingDone for %s", dictionary);
            char[][] words = dictionary.getWords();
            if (words != null && words.length > 0) {
                mWords.add(words);
            }
            Logger.d("WordListDictionaryListener", "onDictionaryLoadingDone got words with length %d", (words == null ? 0 : words.length));

            if (expectedDictionaries == 0) doCallback();
        }

        private void doCallback() {
            try {
                mOnLoadedCallback.accept(mWords);
            } catch (Exception e) {
                Logger.e("WordListDictionaryListener", e, "onDictionaryLoadingDone calling callback with error %s", e.getMessage());
            }
            mWords = new ArrayList<>();
        }

        @Override
        public void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception) {
            final int expectedDictionaries = mExpectedDictionaries.decrementAndGet();
            Logger.e("WordListDictionaryListener", exception, "onDictionaryLoadingFailed for %s with error %s", dictionary, exception.getMessage());
            if (expectedDictionaries == 0) doCallback();
        }
    }

    @NonNull
    protected DictionaryBackgroundLoader.Listener getDictionaryLoadedListener() {
        return mGestureTypingEnabled ? mWordListDictionaryListener : mNoOpListener;
    }

    private void onDictionariesLoaded(List<char[][]> newWords) {
        if (mGestureTypingDetector != null && mGestureTypingEnabled) {
            mGestureTypingDetector.setWords(newWords);

            final AnyKeyboard currentAlphabetKeyboard = getCurrentAlphabetKeyboard();
            //it might be null if the IME service started with enabled flag set to true. In that case
            //the keyboard object will not be ready yet.
            if (currentAlphabetKeyboard != null) {
                mGestureTypingDetector.setKeys(currentAlphabetKeyboard.getKeys(),
                        currentAlphabetKeyboard.getMinWidth(), currentAlphabetKeyboard.getHeight());
            }
        }
    }

    /**
     * FIXME:we only need gesture-typing enabled at alphabet mode.
     */
    private boolean getGestureTypingEnabled() {
        return mGestureTypingEnabled && isInAlphabetKeyboardMode();
    }


    /**
     * When alphabet keyboard loaded, we start loading our getsture-typing word corners data.
     * It is earlier than the first time we click on the keyboard.
     */
    @Override
    public void onAlphabetKeyboardSet(@NonNull AnyKeyboard keyboard) {
        super.onAlphabetKeyboardSet(keyboard);

        if (mGestureTypingEnabled && mGestureTypingDetector != null) {
            mGestureTypingDetector.setKeys(keyboard.getKeys(), keyboard.getMinWidth(), keyboard.getHeight());
        }
    }

    public abstract void setSuggestions(List<? extends CharSequence> suggestions,
            boolean completions, boolean typedWordValid,
            boolean haveMinimalSuggestion);

    @Override
    public boolean isValidGestureTypingStart(int x, int y) {
        if (!getGestureTypingEnabled()) return false;

        return mGestureTypingDetector.isValidStartTouch(x, y);
    }

    @Override
    public void onGestureTypingInputStart(int x, int y, long eventTime) {
        if (!getGestureTypingEnabled()) return;
        //we can call this as many times as we want, it has a short-circuit check.
        setCandidatesViewShown(true/*we need candidates-view to be shown, since we are going to show suggestions*/);

        confirmLastGesture(mPrefsAutoSpace);

        mGestureTypingDetector.clearGesture();
        mGestureTypingDetector.addPoint(x, y);
    }

    @Override
    public void onGestureTypingInput(int x, int y, long eventTime) {
        if (!getGestureTypingEnabled()) return;
        mGestureTypingDetector.addPoint(x, y);
    }

    @Override
    public void onKey(int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        if (getGestureTypingEnabled() && TextEntryState.getState() == TextEntryState.State.PERFORMED_GESTURE && primaryCode > 0 /*printable character*/) {
            confirmLastGesture(primaryCode != KeyCodes.SPACE && mPrefsAutoSpace);
        }

        super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
    }

    private void confirmLastGesture(boolean withAutoSpace) {
        if (TextEntryState.getState() == TextEntryState.State.PERFORMED_GESTURE) {
            pickSuggestionManually(0, mWord.getTypedWord(), withAutoSpace);
        }
    }

    @Override
    public void onGestureTypingInputDone() {
        if (!getGestureTypingEnabled()) return;

        InputConnection ic = getCurrentInputConnection();

        if (ic != null) {
            ArrayList<CharSequence> gestureTypingPossibilities = mGestureTypingDetector.getCandidates();

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
                mWord.reset();
                mWord.setAutoCapitalized(isShifted || isCapsLocked);
                mWord.simulateTypedWord(word);

                mWord.setPreferredWord(mWord.getTypedWord());
                ic.setComposingText(mWord.getTypedWord(), 1);

                TextEntryState.performedGesture();

                if (gestureTypingPossibilities.size() > 1) {
                    setCandidatesViewShown(true);
                    setSuggestions(gestureTypingPossibilities, false, true, true);
                } else {
                    //clearing any suggestion shown
                    setSuggestions(Collections.emptyList(), false, false, false);
                }

                ic.endBatchEdit();
            }

            mGestureTypingDetector.clearGesture();
        }
    }

    @Override
    public boolean isPerformingGesture() {
        return getGestureTypingEnabled() && mGestureTypingDetector.isPerformingGesture();

    }
}
