package com.anysoftkeyboard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryAddOnAndBuilder;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.GetWordsCallback;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.dictionaries.SuggestImpl;
import com.anysoftkeyboard.dictionaries.SuggestionsProvider;
import com.anysoftkeyboard.dictionaries.WordComposer;
import com.anysoftkeyboard.dictionaries.content.ContactsDictionary;
import com.anysoftkeyboard.ime.AnySoftKeyboardClipboard;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.overlay.OverlyDataCreator;
import com.anysoftkeyboard.quicktextkeys.QuickKeyHistoryRecords;
import com.anysoftkeyboard.quicktextkeys.TagsExtractor;
import com.anysoftkeyboard.remote.RemoteInsertion;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.mockito.MockingDetails;
import org.mockito.Mockito;

public class TestableAnySoftKeyboard extends SoftKeyboard {
    // Same as suggestions delay, so we'll get them after typing for verification
    public static final long DELAY_BETWEEN_TYPING = GET_SUGGESTIONS_DELAY + 1;

    private TestableKeyboardSwitcher mTestableKeyboardSwitcher;
    private AnyKeyboardView mSpiedKeyboardView;
    private EditorInfo mEditorInfo;
    private TestInputConnection mInputConnection;
    private CandidateView mMockCandidateView;
    private boolean mHidden = true;
    private boolean mCandidateShowsHint = false;
    private int mCandidateVisibility = View.VISIBLE;
    private InputMethodManager mSpiedInputMethodManager;
    private int mLastOnKeyPrimaryCode;
    private AbstractInputMethodImpl mCreatedInputMethodInterface;
    private AbstractInputMethodSessionImpl mCreatedInputMethodSession;

    private OverlyDataCreator mOriginalOverlayDataCreator;
    private OverlyDataCreator mSpiedOverlayCreator;
    private PackageManager mSpiedPackageManager;

    private RemoteInsertion mRemoteInsertion;
    private InputContentInfoCompat mInputContentInfo;

    private long mDelayBetweenTyping = DELAY_BETWEEN_TYPING;

    public static EditorInfo createEditorInfoTextWithSuggestions() {
        return createEditorInfo(EditorInfo.IME_ACTION_NONE, EditorInfo.TYPE_CLASS_TEXT);
    }

    public static EditorInfo createEditorInfo(final int imeOptions, final int inputType) {
        EditorInfo editorInfo = new EditorInfo();
        editorInfo.packageName = "com.menny.android.anysoftkeyboard";
        editorInfo.imeOptions = imeOptions;
        editorInfo.inputType = inputType;

        return editorInfo;
    }

    @Nullable public static Keyboard.Key findKeyWithPrimaryKeyCode(int keyCode, AnyKeyboard keyboard) {
        for (Keyboard.Key aKey : keyboard.getKeys()) {
            if (aKey.getPrimaryCode() == keyCode) {
                return aKey;
            }
        }

        return null;
    }

    @Override
    protected boolean commitMediaToInputConnection(
            InputContentInfoCompat inputContentInfo,
            InputConnection inputConnection,
            EditorInfo editorInfo,
            int flags) {
        mInputContentInfo = inputContentInfo;
        return super.commitMediaToInputConnection(
                inputContentInfo, inputConnection, editorInfo, flags);
    }

    public InputContentInfoCompat getCommitedInputContentInfo() {
        return mInputContentInfo;
    }

    @Override
    public void onCreate() {
        mRemoteInsertion = Mockito.mock(RemoteInsertion.class);
        mSpiedPackageManager = Mockito.spy(super.getPackageManager());
        super.onCreate();
        mSpiedInputMethodManager = Mockito.spy(super.getInputMethodManager());
        mInputConnection = Mockito.spy(new TestInputConnection(this));
    }

    public void setUpdateSelectionDelay(long delay) {
        mInputConnection.setUpdateSelectionDelay(delay);
    }

    public void setDelayBetweenTyping(long delay) {
        mDelayBetweenTyping = delay;
    }

    @Override
    protected RemoteInsertion createRemoteInsertion() {
        return mRemoteInsertion;
    }

    @Override
    protected OverlyDataCreator createOverlayDataCreator() {
        mOriginalOverlayDataCreator = super.createOverlayDataCreator();
        Assert.assertNotNull(mOriginalOverlayDataCreator);

        mSpiedOverlayCreator = Mockito.spy(new OverlayCreatorForSpy(mOriginalOverlayDataCreator));

        return mSpiedOverlayCreator;
    }

    public AnySoftKeyboardClipboard.ClipboardActionOwner getClipboardActionOwnerImpl() {
        return mClipboardActionOwnerImpl;
    }

    public AnySoftKeyboardClipboard.ClipboardStripActionProvider getClipboardStripActionProvider() {
        return mSuggestionClipboardEntry;
    }

    public OverlyDataCreator getMockOverlayDataCreator() {
        return mSpiedOverlayCreator;
    }

    public OverlyDataCreator getOriginalOverlayDataCreator() {
        return mOriginalOverlayDataCreator;
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
    public InputMethodManager getInputMethodManager() {
        return mSpiedInputMethodManager;
    }

    @VisibleForTesting
    @NonNull @Override
    public Suggest getSuggest() {
        return super.getSuggest();
    }

    public CandidateView getMockCandidateView() {
        return mMockCandidateView;
    }

    public boolean isAddToDictionaryHintShown() {
        return mCandidateShowsHint;
    }

    @NonNull @Override
    protected Suggest createSuggest() {
        return Mockito.spy(
                new TestableSuggest(
                        new SuggestImpl(
                                new SuggestionsProvider(this) {
                                    @NonNull @Override
                                    protected ContactsDictionary createRealContactsDictionary() {
                                        return Mockito.mock(ContactsDictionary.class);
                                    }
                                })));
    }

    // MAGIC: now it is visible for tests
    @VisibleForTesting
    @Override
    public void setIncognito(boolean enable, boolean byUser) {
        super.setIncognito(enable, byUser);
    }

    public TestableKeyboardSwitcher getKeyboardSwitcherForTests() {
        return mTestableKeyboardSwitcher;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        mEditorInfo = attribute;
        super.onStartInput(attribute, restarting);
    }

    public void resetMockCandidateView() {
        Mockito.reset(mMockCandidateView);

        setupMockCandidateView();
    }

    private void setupMockCandidateView() {
        ViewGroup.LayoutParams lp =
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Mockito.doReturn(lp).when(mMockCandidateView).getLayoutParams();
        Mockito.doReturn(R.id.candidate_view).when(mMockCandidateView).getId();
        Mockito.doAnswer(
                        invocation -> {
                            boolean previousState = mCandidateShowsHint;
                            mCandidateShowsHint = false;
                            return previousState;
                        })
                .when(mMockCandidateView)
                .dismissAddToDictionaryHint();
        Mockito.doAnswer(
                        invocation -> {
                            mCandidateShowsHint = true;
                            return null;
                        })
                .when(mMockCandidateView)
                .showAddToDictionaryHint(any(CharSequence.class));
        Mockito.doAnswer(
                        invocation -> {
                            mCandidateShowsHint = false;
                            return null;
                        })
                .when(mMockCandidateView)
                .notifyAboutWordAdded(any(CharSequence.class));

        Mockito.doAnswer(invocation -> mCandidateVisibility)
                .when(mMockCandidateView)
                .getVisibility();

        Mockito.doReturn(
                        ApplicationProvider.getApplicationContext()
                                .getResources()
                                .getDrawable(R.drawable.close_suggestions_strip_icon))
                .when(mMockCandidateView)
                .getCloseIcon();

        Mockito.doAnswer(invocation -> mCandidateVisibility = invocation.getArgument(0))
                .when(mMockCandidateView)
                .setVisibility(anyInt());
    }

    @Override
    public boolean isPredictionOn() {
        return super.isPredictionOn();
    }

    @Override
    public boolean isAutoCorrect() {
        return super.isAutoCorrect();
    }

    @Override
    public EditorInfo getCurrentInputEditorInfo() {
        return mEditorInfo;
    }

    @NonNull @Override
    protected KeyboardSwitcher createKeyboardSwitcher() {
        return mTestableKeyboardSwitcher = new TestableKeyboardSwitcher(this);
    }

    @Override
    protected KeyboardViewContainerView createInputViewContainer() {
        final KeyboardViewContainerView originalInputContainer = super.createInputViewContainer();
        AnyKeyboardView inputView =
                (AnyKeyboardView) originalInputContainer.getStandardKeyboardView();

        originalInputContainer.removeAllViews();
        mMockCandidateView = Mockito.mock(CandidateView.class);
        setupMockCandidateView();
        mSpiedKeyboardView = Mockito.spy(inputView);

        originalInputContainer.addView(mMockCandidateView);
        originalInputContainer.addView(mSpiedKeyboardView);

        return originalInputContainer;
    }

    public AnyKeyboardView getSpiedKeyboardView() {
        return mSpiedKeyboardView;
    }

    @Override
    public PackageManager getPackageManager() {
        return mSpiedPackageManager;
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
    public void hideWindow() {
        super.hideWindow();
        mHidden = true;
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

    @SuppressWarnings("LoopOverCharArray")
    public void simulateTextTyping(
            final String text, final boolean advanceTime, final boolean asDiscreteKeys) {
        if (asDiscreteKeys) {
            for (char key : text.toCharArray()) {
                simulateKeyPress(key, advanceTime);
            }
        } else {
            onText(null, text);
            if (advanceTime) TestRxSchedulers.foregroundAdvanceBy(mDelayBetweenTyping);
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

    public void simulateKeyPress(Keyboard.Key key, final boolean advanceTime) {
        final int primaryCode;
        final int[] nearByKeyCodes;
        final AnyKeyboard keyboard = getCurrentKeyboard();
        Assert.assertNotNull(keyboard);
        if (key instanceof AnyKeyboard.AnyKey /*this will ensure this instance is not a mock*/) {
            primaryCode =
                    key.getCodeAtIndex(
                            0,
                            mSpiedKeyboardView != null
                                    && mSpiedKeyboardView.getKeyDetector().isKeyShifted(key));
            nearByKeyCodes = new int[64];
            if (mSpiedKeyboardView != null) {
                mSpiedKeyboardView
                        .getKeyDetector()
                        .getKeyIndexAndNearbyCodes(key.centerX, key.centerY, nearByKeyCodes);
            }

        } else {
            primaryCode = key.getPrimaryCode();
            key = null;
            nearByKeyCodes = new int[0];
        }
        onPress(primaryCode);
        onKey(primaryCode, key, 0, nearByKeyCodes, true);
        onRelease(primaryCode);
        if (advanceTime) {
            TestRxSchedulers.foregroundAdvanceBy(mDelayBetweenTyping);
            TestRxSchedulers.backgroundRunOneJob();
        }
    }

    @Nullable public Keyboard.Key findKeyWithPrimaryKeyCode(int keyCode) {
        return findKeyWithPrimaryKeyCode(keyCode, getCurrentKeyboard());
    }

    public void simulateCurrentSubtypeChanged(InputMethodSubtype subtype) {
        onCurrentInputMethodSubtypeChanged(subtype);
    }

    @Override
    public void onKey(
            int primaryCode,
            Keyboard.Key key,
            int multiTapIndex,
            int[] nearByKeyCodes,
            boolean fromUI) {
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

    public String getCurrentSelectedText() {
        return mInputConnection.getSelectedText(0).toString();
    }

    public void moveCursorToPosition(int position, boolean advanceTime) {
        setSelectedText(position, position, advanceTime);
    }

    public void setSelectedText(int begin, int end, boolean advanceTime) {
        mInputConnection.setSelection(begin, end);
        if (advanceTime) TestRxSchedulers.foregroundAdvanceBy(10 * ONE_FRAME_DELAY + 1);
    }

    public void onText(Keyboard.Key key, CharSequence text, boolean advanceTime) {
        super.onText(key, text);
        if (advanceTime) TestRxSchedulers.foregroundAdvanceBy(mDelayBetweenTyping);
    }

    @Override
    public void onText(Keyboard.Key key, CharSequence text) {
        onText(key, text, true);
    }

    @Override
    public AbstractInputMethodSessionImpl onCreateInputMethodSessionInterface() {
        return mCreatedInputMethodSession = super.onCreateInputMethodSessionInterface();
    }

    public AbstractInputMethodSessionImpl getCreatedInputMethodSessionInterface() {
        return mCreatedInputMethodSession;
    }

    @NonNull @Override
    public AbstractInputMethodImpl onCreateInputMethodInterface() {
        return mCreatedInputMethodInterface = super.onCreateInputMethodInterface();
    }

    public AbstractInputMethodImpl getCreatedInputMethodInterface() {
        return mCreatedInputMethodInterface;
    }

    public TestInputConnection getCurrentTestInputConnection() {
        return mInputConnection;
    }

    @NonNull @Override
    protected DictionaryBackgroundLoader.Listener getDictionaryLoadedListener(
            @NonNull AnyKeyboard currentAlphabetKeyboard) {
        final DictionaryBackgroundLoader.Listener dictionaryLoadedListener =
                super.getDictionaryLoadedListener(currentAlphabetKeyboard);
        if (dictionaryLoadedListener instanceof WordListDictionaryListener) {
            return new DictionaryBackgroundLoader.Listener() {

                @Override
                public void onDictionaryLoadingStarted(Dictionary dictionary) {
                    dictionaryLoadedListener.onDictionaryLoadingStarted(dictionary);
                }

                @Override
                public void onDictionaryLoadingDone(Dictionary dictionary) {
                    final MockingDetails mockingDetails = Mockito.mockingDetails(dictionary);
                    if (!mockingDetails.isMock() && !mockingDetails.isSpy()) {
                        dictionary = Mockito.spy(dictionary);
                        Mockito.doAnswer(
                                        invocation -> {
                                            ((GetWordsCallback) invocation.getArgument(0))
                                                    .onGetWordsFinished(
                                                            new char[][] {
                                                                "hello".toCharArray(),
                                                                "welcome".toCharArray(),
                                                                "is".toCharArray(),
                                                                "you".toCharArray(),
                                                                "good".toCharArray(),
                                                                "bye".toCharArray(),
                                                                "one".toCharArray(),
                                                                "two".toCharArray(),
                                                                "poo".toCharArray(),
                                                                "three".toCharArray()
                                                            },
                                                            new int[] {
                                                                180, 100, 253, 200, 120, 140, 100,
                                                                80, 40, 60
                                                            });
                                            return null;
                                        })
                                .when(dictionary)
                                .getLoadedWords(any());
                    }
                    dictionaryLoadedListener.onDictionaryLoadingDone(dictionary);
                }

                @Override
                public void onDictionaryLoadingFailed(Dictionary dictionary, Throwable exception) {
                    if (exception instanceof Resources.NotFoundException
                            && exception.getMessage().contains("resource ID #0x0")) {
                        // Due to a bug in Robolectric, typed-array is returning empty
                        onDictionaryLoadingDone(dictionary);
                    } else {
                        final Dictionary spy = Mockito.spy(dictionary);
                        Mockito.doAnswer(
                                        invocation -> {
                                            ((GetWordsCallback) invocation.getArgument(0))
                                                    .onGetWordsFinished(new char[0][0], new int[0]);
                                            return null;
                                        })
                                .when(spy)
                                .getLoadedWords(any());
                        dictionaryLoadedListener.onDictionaryLoadingFailed(spy, exception);
                    }
                }
            };
        } else {
            return dictionaryLoadedListener;
        }
    }

    // Needs this since we want to use Mockito.spy, which gets the class at runtime
    // and creates a stub for it, which will create an additional real instance
    // of super.createOverlayDataCreator(), and confuses everyone.
    private static class OverlayCreatorForSpy implements OverlyDataCreator {

        private final OverlyDataCreator mOriginalOverlayDataCreator;

        public OverlayCreatorForSpy(OverlyDataCreator originalOverlayDataCreator) {
            mOriginalOverlayDataCreator = originalOverlayDataCreator;
        }

        @Override
        public OverlayData createOverlayData(ComponentName remoteApp) {
            return mOriginalOverlayDataCreator.createOverlayData(remoteApp);
        }
    }

    public static class TestableKeyboardSwitcher extends KeyboardSwitcher {

        private boolean mKeyboardsFlushed;
        private boolean mViewSet;
        @InputModeId private int mInputModeId;

        public TestableKeyboardSwitcher(@NonNull AnySoftKeyboard ime) {
            super(ime, ApplicationProvider.getApplicationContext());
        }

        @Override
        public /*was protected, now public*/ AnyKeyboard createKeyboardFromCreator(
                int mode, KeyboardAddOnAndBuilder creator) {
            return super.createKeyboardFromCreator(mode, creator);
        }

        @Override
        public /*was protected, now public*/ GenericKeyboard createGenericKeyboard(
                AddOn addOn,
                Context context,
                int layoutResId,
                int landscapeLayoutResId,
                String name,
                String keyboardId,
                int mode) {
            return super.createGenericKeyboard(
                    addOn, context, layoutResId, landscapeLayoutResId, name, keyboardId, mode);
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
        public void setKeyboardMode(
                @InputModeId int inputModeId, EditorInfo attr, boolean restarting) {
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

        public AnyKeyboard[] getCachedAlphabetKeyboardsArray() {
            return mAlphabetKeyboards;
        }

        public List<AnyKeyboard> getCachedSymbolsKeyboards() {
            return Collections.unmodifiableList(Arrays.asList(mSymbolsKeyboardsArray));
        }
    }

    // I need this class, so the Mockito.spy will not mess with internal state of SuggestImpl
    private static class TestableSuggest implements Suggest {

        private final Suggest mDelegate;

        private TestableSuggest(Suggest delegate) {
            mDelegate = delegate;
        }

        @Override
        public void setCorrectionMode(
                boolean enabledSuggestions,
                int maxLengthDiff,
                int maxDistance,
                boolean splitWords) {
            mDelegate.setCorrectionMode(enabledSuggestions, maxLengthDiff, maxDistance, splitWords);
        }

        @Override
        public boolean isSuggestionsEnabled() {
            return mDelegate.isSuggestionsEnabled();
        }

        @Override
        public void closeDictionaries() {
            mDelegate.closeDictionaries();
        }

        @Override
        public void setupSuggestionsForKeyboard(
                @NonNull List<DictionaryAddOnAndBuilder> dictionaryBuilders,
                @NonNull DictionaryBackgroundLoader.Listener cb) {
            mDelegate.setupSuggestionsForKeyboard(dictionaryBuilders, cb);
        }

        @Override
        public void setMaxSuggestions(int maxSuggestions) {
            mDelegate.setMaxSuggestions(maxSuggestions);
        }

        @Override
        public void resetNextWordSentence() {
            mDelegate.resetNextWordSentence();
        }

        @Override
        public List<CharSequence> getNextSuggestions(
                CharSequence previousWord, boolean inAllUpperCaseState) {
            return mDelegate.getNextSuggestions(previousWord, inAllUpperCaseState);
        }

        @Override
        public List<CharSequence> getSuggestions(WordComposer wordComposer) {
            return mDelegate.getSuggestions(wordComposer);
        }

        @Override
        public int getLastValidSuggestionIndex() {
            return mDelegate.getLastValidSuggestionIndex();
        }

        @Override
        public boolean isValidWord(CharSequence word) {
            return mDelegate.isValidWord(word);
        }

        @Override
        public boolean addWordToUserDictionary(String word) {
            return mDelegate.addWordToUserDictionary(word);
        }

        @Override
        public void removeWordFromUserDictionary(String word) {
            mDelegate.removeWordFromUserDictionary(word);
        }

        @Override
        public void setTagsSearcher(@NonNull TagsExtractor extractor) {
            mDelegate.setTagsSearcher(extractor);
        }

        @Override
        public boolean tryToLearnNewWord(CharSequence newWord, AdditionType additionType) {
            return mDelegate.tryToLearnNewWord(newWord, additionType);
        }

        @Override
        public void setIncognitoMode(boolean incognitoMode) {
            mDelegate.setIncognitoMode(incognitoMode);
        }

        @Override
        public boolean isIncognitoMode() {
            return mDelegate.isIncognitoMode();
        }

        @Override
        public void destroy() {
            mDelegate.destroy();
        }
    }
}
