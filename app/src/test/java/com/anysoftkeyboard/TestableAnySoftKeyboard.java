package com.anysoftkeyboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;

import org.junit.Assert;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSystemClock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestableAnySoftKeyboard extends SoftKeyboard {
    private Suggest mSpiedSuggest;
    private TestableKeyboardSwitcher mTestableKeyboardSwitcher;
    private AnyKeyboardView mSpiedKeyboardView;
    private EditorInfo mEditorInfo;
    private TestInputConnection mInputConnection;
    private CandidateView mMockCandidateView;
    private boolean mHidden = true;
    private boolean mCandidateShowsHint = false;
    private InputMethodManager mSpiedInputMethodManager;
    private int mLastOnKeyPrimaryCode;

    public static EditorInfo createEditorInfoTextWithSuggestions() {
        return createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT);
    }

    public static EditorInfo createEditorInfo(final int imeOptions, final int inputType) {
        EditorInfo editorInfo = new EditorInfo();
        editorInfo.imeOptions = imeOptions;
        editorInfo.inputType = inputType;

        return editorInfo;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSpiedInputMethodManager = Mockito.spy(super.getInputMethodManager());
        mInputConnection = Mockito.spy(new TestInputConnection(this));
    }

    @Override
    public TagsExtractor getQuickTextTagsSearcher() {
        return super.getQuickTextTagsSearcher();
    }

    @Override
    public QuickKeyHistoryRecords getQuickKeyHistoryRecords() {
        return super.getQuickKeyHistoryRecords();
    }

    @Override
    protected InputMethodManager getInputMethodManager() {
        return mSpiedInputMethodManager;
    }

    public Suggest getSpiedSuggest() {
        return mSpiedSuggest;
    }

    public CandidateView getMockCandidateView() {
        return mMockCandidateView;
    }

    public boolean isAddToDictionaryHintShown() {
        return mCandidateShowsHint;
    }

    @NonNull
    @Override
    protected Suggest createSuggest() {
        Assert.assertNull(mSpiedSuggest);
        return mSpiedSuggest = Mockito.spy(new TestableSuggest(this));
    }

    public TestableKeyboardSwitcher getKeyboardSwitcherForTests() {
        return mTestableKeyboardSwitcher;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        mEditorInfo = attribute;
        super.onStartInput(attribute, restarting);
    }

    @Override
    public View onCreateCandidatesView() {
        View spiedRootView = Mockito.spy(super.onCreateCandidatesView());
        mMockCandidateView = Mockito.mock(CandidateView.class);
        resetMockCandidateView();
        Mockito.doReturn(mMockCandidateView).when(spiedRootView).findViewById(R.id.candidates);
        return spiedRootView;
    }

    public void resetMockCandidateView() {
        Mockito.reset(mMockCandidateView);
        Mockito.doAnswer(invocation -> {
            boolean previousState = mCandidateShowsHint;
            mCandidateShowsHint = false;
            return previousState;
        }).when(mMockCandidateView).dismissAddToDictionaryHint();
        Mockito.doAnswer(invocation -> {
            mCandidateShowsHint = true;
            return null;
        }).when(mMockCandidateView).showAddToDictionaryHint(Mockito.any(CharSequence.class));
        Mockito.doAnswer(invocation -> {
            mCandidateShowsHint = false;
            return null;
        }).when(mMockCandidateView).notifyAboutWordAdded(Mockito.any(CharSequence.class));

    }

    @Override
    public EditorInfo getCurrentInputEditorInfo() {
        return mEditorInfo;
    }

    @NonNull
    @Override
    protected KeyboardSwitcher createKeyboardSwitcher() {
        return mTestableKeyboardSwitcher = new TestableKeyboardSwitcher(this);
    }

    @Override
    protected KeyboardViewContainerView createInputViewContainer() {
        KeyboardViewContainerView containerView = super.createInputViewContainer();
        AnyKeyboardView inputView = (AnyKeyboardView) containerView.getChildAt(0);
        containerView.removeView(inputView);
        mSpiedKeyboardView = Mockito.spy(inputView);
        containerView.addView(mSpiedKeyboardView);

        return containerView;
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        mHidden = false;
        super.onStartInputView(attribute, restarting);
    }

    @Override
    public void requestHideSelf(int flags) {
        mHidden = true;
        super.requestHideSelf(flags);
    }

    @Override
    public void onWindowHidden() {
        mHidden = true;
        super.onWindowHidden();
    }

    @Override
    public void onWindowShown() {
        super.onWindowShown();
        mHidden = false;
    }

    @Override
    protected boolean handleCloseRequest() {
        if (!super.handleCloseRequest()) {
            mHidden = true;
            return false;
        } else {
            return true;
        }
    }

    public boolean isKeyboardViewHidden() {
        return mHidden;
    }

    public void simulateKeyPress(final int keyCode) {
        simulateKeyPress(keyCode, true);
    }

    public void simulateTextTyping(final String text) {
        simulateTextTyping(text, true, true);
    }

    public void simulateTextTyping(final String text, final boolean advanceTime, final boolean asDiscreteKeys) {
        if (asDiscreteKeys) {
            for (char key : text.toCharArray()) {
                simulateKeyPress(key, advanceTime);
            }
        } else {
            onText(null, text);
            Robolectric.flushForegroundThreadScheduler();
            if (advanceTime) ShadowSystemClock.sleep(25);
        }
    }

    public AnyKeyboard getCurrentKeyboardForTests() {
        return getCurrentKeyboard();
    }

    public void simulateKeyPress(final int keyCode, final boolean advanceTime) {
        Keyboard.Key key = findKeyWithPrimaryKeyCode(keyCode);
        if (key == null) {
            key = Mockito.mock(Keyboard.Key.class);
            Mockito.doReturn(keyCode).when(key).getPrimaryCode();
        }

        simulateKeyPress(key, advanceTime);
    }

    public void simulateKeyPress(final Keyboard.Key key, final boolean advanceTime) {
        final int primaryCode = key.getPrimaryCode();
        onPress(primaryCode);
        Robolectric.flushForegroundThreadScheduler();
        final AnyKeyboard keyboard = getCurrentKeyboard();
        Assert.assertNotNull(keyboard);
        if (key instanceof AnyKeyboard.AnyKey/*this will ensure this instance is not a mock*/) {
            final int keyCodeWithShiftState = key.getCodeAtIndex(0, mSpiedKeyboardView.getKeyDetector().isKeyShifted(key));
            onKey(keyCodeWithShiftState, key, 0, keyboard.getNearestKeys(key.x + 5, key.y + 5), true);
        } else {
            onKey(primaryCode, null, 0, new int[0], true);
        }
        Robolectric.flushForegroundThreadScheduler();
        if (advanceTime) ShadowSystemClock.sleep(25);
        onRelease(primaryCode);
        Robolectric.flushForegroundThreadScheduler();
    }

    @Nullable
    public Keyboard.Key findKeyWithPrimaryKeyCode(int keyCode, AnyKeyboard keyboard) {
        for (Keyboard.Key aKey : keyboard.getKeys()) {
            if (aKey.getPrimaryCode() == keyCode) {
                return aKey;
            }
        }

        return null;
    }

    @Nullable
    public Keyboard.Key findKeyWithPrimaryKeyCode(int keyCode) {
        return findKeyWithPrimaryKeyCode(keyCode, getCurrentKeyboard());
    }

    public void simulateCurrentSubtypeChanged(InputMethodSubtype subtype) {
        onCurrentInputMethodSubtypeChanged(subtype);
    }

    @Override
    public void onKey(int primaryCode, Keyboard.Key key, int multiTapIndex, int[] nearByKeyCodes, boolean fromUI) {
        mLastOnKeyPrimaryCode = primaryCode;
        super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
    }

    public int getLastOnKeyPrimaryCode() {
        return mLastOnKeyPrimaryCode;
    }

    public TestInputConnection getTestInputConnection() {
        return mInputConnection;
    }

    public String getCurrentInputConnectionText() {
        return mInputConnection.getCurrentTextInInputConnection();
    }

    public static class TestableSuggest extends Suggest {

        private final Map<String, List<CharSequence>> mDefinedWords = new HashMap<>();
        private boolean mHasMinimalCorrection;

        public TestableSuggest(Context context) {
            super(context);
        }

        public void setSuggestionsForWord(String word, CharSequence... suggestions) {
            mDefinedWords.put(word.toLowerCase(), Arrays.asList(suggestions));
        }

        @Override
        public List<CharSequence> getSuggestions(WordComposer wordComposer, boolean includeTypedWordIfValid) {
            if (wordComposer.isAtTagsSearchState())
                return super.getSuggestions(wordComposer, includeTypedWordIfValid);

            String word = wordComposer.getTypedWord().toString().toLowerCase();

            ArrayList<CharSequence> suggestions = new ArrayList<>();
            suggestions.add(wordComposer.getTypedWord());
            if (mDefinedWords.containsKey(word)) {
                suggestions.addAll(mDefinedWords.get(word));
                mHasMinimalCorrection = true;
            } else {
                mHasMinimalCorrection = false;
            }

            return suggestions;
        }

        @Override
        public boolean hasMinimalCorrection() {
            return mHasMinimalCorrection;
        }
    }

    public static class TestableKeyboardSwitcher extends KeyboardSwitcher {

        private boolean mKeyboardsFlushed;
        private boolean mViewSet;
        @InputModeId
        private int mInputModeId;

        public TestableKeyboardSwitcher(@NonNull AnySoftKeyboard ime) {
            super(ime, RuntimeEnvironment.application);
        }

        @Override
        public /*was protected, now public*/ AnyKeyboard createKeyboardFromCreator(int mode, KeyboardAddOnAndBuilder creator) {
            return super.createKeyboardFromCreator(mode, creator);
        }

        @Override
        public /*was protected, now public*/ GenericKeyboard createGenericKeyboard(AddOn addOn, Context context, int layoutResId, int landscapeLayoutResId, String name, String keyboardId, int mode) {
            return super.createGenericKeyboard(addOn, context, layoutResId, landscapeLayoutResId, name, keyboardId, mode);
        }

        public void verifyKeyboardsFlushed() {
            Assert.assertTrue(mKeyboardsFlushed);
            mKeyboardsFlushed = false;
        }

        @Override
        public void flushKeyboardsCache() {
            mKeyboardsFlushed = true;
            super.flushKeyboardsCache();
        }

        public void verifyKeyboardsNotFlushed() {
            Assert.assertFalse(mKeyboardsFlushed);
        }

        @Override
        public void setInputView(@NonNull InputViewBinder inputView) {
            mViewSet = true;
            super.setInputView(inputView);
        }

        public void verifyNewViewSet() {
            Assert.assertTrue(mViewSet);
            mViewSet = false;
        }

        @Override
        public void setKeyboardMode(@InputModeId int inputModeId, EditorInfo attr, boolean restarting) {
            mInputModeId = inputModeId;
            super.setKeyboardMode(inputModeId, attr, restarting);
        }

        public int getInputModeId() {
            return mInputModeId;
        }

        public void verifyNewViewNotSet() {
            Assert.assertFalse(mViewSet);
        }

        public List<AnyKeyboard> getCachedAlphabetKeyboards() {
            return Collections.unmodifiableList(Arrays.asList(mAlphabetKeyboards));
        }

        public List<AnyKeyboard> getCachedSymbolsKeyboards() {
            return Collections.unmodifiableList(Arrays.asList(mSymbolsKeyboardsArray));
        }
    }

}
