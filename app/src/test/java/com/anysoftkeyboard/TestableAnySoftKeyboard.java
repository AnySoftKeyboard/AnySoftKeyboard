package com.anysoftkeyboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.base.dictionaries.WordComposer;
import com.anysoftkeyboard.dictionaries.DictionaryFactory;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;

import org.junit.Assert;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowSystemClock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestableAnySoftKeyboard extends SoftKeyboard {

    private Suggest mSpiedSuggest;
    private TestableKeyboardSwitcher mSpiedKeyboardSwitcher;
    private AnyKeyboardView mSpiedKeyboardView;
    private EditorInfo mEditorInfo;
    private TestInputConnection mInputConnection;
    private CandidateView mMockCandidateView;
    private UserDictionary mSpiedUserDictionary;
    private boolean mHidden = true;
    private boolean mCandidateShowsHint = false;
    private InputMethodManager mSpiedInputMethodManager;
    private int mLastOnKeyPrimaryCode;

    @Override
    public void onCreate() {
        super.onCreate();
        mSpiedInputMethodManager = Mockito.spy(super.getInputMethodManager());
    }

    @Override
    public TagsExtractor getQuickTextTagsSearcher() {
        return super.getQuickTextTagsSearcher();
    }

    @Override
    protected InputMethodManager getInputMethodManager() {
        return mSpiedInputMethodManager;
    }

    public Suggest getSpiedSuggest() {
        return mSpiedSuggest;
    }

    public UserDictionary getSpiedUserDictionary() {
        return mSpiedUserDictionary;
    }

    public AnyKeyboardView getSpiedKeyboardView() {
        return mSpiedKeyboardView;
    }

    public CandidateView getMockCandidateView() {
        return mMockCandidateView;
    }

    public boolean isAddToDictionartHintShown(){
        return mCandidateShowsHint;
    }

    @NonNull
    @Override
    protected Suggest createSuggest() {
        Assert.assertNull(mSpiedSuggest);
        return mSpiedSuggest = Mockito.spy(new TestableSuggest(this));
    }

    public TestableKeyboardSwitcher getSpiedKeyboardSwitcher() {
        return mSpiedKeyboardSwitcher;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        mEditorInfo = attribute;
        if ((!restarting) || mInputConnection == null) mInputConnection = Mockito.spy(new TestInputConnection(this));
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
        Mockito.doAnswer(new Answer() {
                             @Override
                             public Object answer(InvocationOnMock invocation) throws Throwable {
                                 boolean previousState = mCandidateShowsHint;
                                 mCandidateShowsHint = false;
                                 return previousState;
                             }
                         }).when(mMockCandidateView).dismissAddToDictionaryHint();
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mCandidateShowsHint = true;
                return null;
            }
        }).when(mMockCandidateView).showAddToDictionaryHint(Mockito.any(CharSequence.class));
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                mCandidateShowsHint = false;
                return null;
            }
        }).when(mMockCandidateView).notifyAboutWordAdded(Mockito.any(CharSequence.class));

    }

    @Override
    public EditorInfo getCurrentInputEditorInfo() {
        return mEditorInfo;
    }

    @Override
    public InputConnection getCurrentInputConnection() {
        return mInputConnection;
    }

    @NonNull
    @Override
    protected KeyboardSwitcher createKeyboardSwitcher() {
        Assert.assertNull(mSpiedKeyboardSwitcher);
        return mSpiedKeyboardSwitcher = Mockito.spy(new TestableKeyboardSwitcher(this));
    }

    @Override
    public View onCreateInputView() {
        return mSpiedKeyboardView = Mockito.spy((AnyKeyboardView) super.onCreateInputView());
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
    public void hideWindow() {
        mHidden = true;
        super.hideWindow();
    }

    @Override
    protected void handleClose() {
        mHidden = true;
        super.handleClose();
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
        onPress(keyCode);
        Robolectric.flushForegroundThreadScheduler();
        final AnyKeyboard keyboard = getCurrentKeyboard();
        Assert.assertNotNull(keyboard);
        Keyboard.Key key = null;
        for (Keyboard.Key aKey : keyboard.getKeys()) {
            if (aKey.getPrimaryCode() == keyCode) {
                key = aKey;
                break;
            }
        }
        if (key == null) {
            onKey(keyCode, null, 0, new int[0], true);
        } else {
            final int keyCodeWithShiftState = key.getCodeAtIndex(0, mSpiedKeyboardView.getKeyDetector().isKeyShifted(key));
            onKey(keyCodeWithShiftState, key, 0, keyboard.getNearestKeys(key.x + 5, key.y + 5), true);
        }
        Robolectric.flushForegroundThreadScheduler();
        if (advanceTime) ShadowSystemClock.sleep(25);
        onRelease(keyCode);
        Robolectric.flushForegroundThreadScheduler();
    }

    public static EditorInfo createEditorInfoTextWithSuggestions() {
        return createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT);
    }

    public static EditorInfo createEditorInfo(final int imeOptions, final int inputType) {
        EditorInfo editorInfo = new EditorInfo();
        editorInfo.imeOptions = imeOptions;
        editorInfo.inputType = inputType;

        return editorInfo;
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

        @NonNull
        @Override
        protected DictionaryFactory createDictionaryFactory() {
            return Mockito.spy(super.createDictionaryFactory());
        }

        @Override
        public List<CharSequence> getSuggestions(WordComposer wordComposer, boolean includeTypedWordIfValid) {
            if (wordComposer.isAtTagsSearchState()) return super.getSuggestions(wordComposer, includeTypedWordIfValid);

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

        public TestableKeyboardSwitcher(@NonNull AnySoftKeyboard ime) {
            super(ime, RuntimeEnvironment.application);
        }

        @Override
        public /*was protected, now public*/ AnyKeyboard createKeyboardFromCreator(int mode, KeyboardAddOnAndBuilder creator) {
            return super.createKeyboardFromCreator(mode, creator);
        }

        @Override
        public /*was protected, now public*/ GenericKeyboard createGenericKeyboard(AddOn addOn, Context context, int layoutResId, int landscapeLayoutResId, String name, String keyboardId, int mode, boolean disableKeyPreview) {
            return super.createGenericKeyboard(addOn, context, layoutResId, landscapeLayoutResId, name, keyboardId, mode, disableKeyPreview);
        }
    }

}
