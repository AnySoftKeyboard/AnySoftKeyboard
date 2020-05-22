package com.anysoftkeyboard;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.anysoftkeyboard.dictionaries.GetWordsCallback;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.dictionaries.WordComposer;
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
import com.anysoftkeyboard.saywhat.PublicNotice;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

public class TestableAnySoftKeyboard extends SoftKeyboard {
    private Suggest mSpiedSuggest;
    private TestableKeyboardSwitcher mTestableKeyboardSwitcher;
    private AnyKeyboardView mSpiedKeyboardView;
    private EditorInfo mEditorInfo;
    private TestInputConnection mInputConnection;
    private CandidateView mMockCandidateView;
    private boolean mHidden = true;
    private boolean mCandidateShowsHint = false;
    private int mCandidateVisiblity = View.VISIBLE;
    private InputMethodManager mSpiedInputMethodManager;
    private int mLastOnKeyPrimaryCode;
    private AbstractInputMethodImpl mCreatedInputMethodInterface;
    private AbstractInputMethodSessionImpl mCreatedInputMethodSession;

    private OverlyDataCreator mOriginalOverlayDataCreator;
    private OverlyDataCreator mSpiedOverlayCreator;
    private PackageManager mSpiedPackageManager;

    private RemoteInsertion mRemoteInsertion;
    private InputContentInfoCompat mInputContentInfo;

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

    @Override
    protected List<PublicNotice> generatePublicNotices() {
        return Collections.emptyList();
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

        Mockito.doAnswer(invocation -> mCandidateVisiblity)
                .when(mMockCandidateView)
                .getVisibility();

        Mockito.doAnswer(invocation -> mCandidateVisiblity = invocation.getArgument(0))
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

    @NonNull
    @Override
    protected KeyboardSwitcher createKeyboardSwitcher() {
        return mTestableKeyboardSwitcher = new TestableKeyboardSwitcher(this);
    }

    @Override
    protected KeyboardViewContainerView createInputViewContainer() {
        final KeyboardViewContainerView originalInputContainer = super.createInputViewContainer();
        AnyKeyboardView inputView = (AnyKeyboardView) originalInputContainer.getChildAt(1);
        originalInputContainer.removeAllViews();
        mMockCandidateView = Mockito.mock(CandidateView.class);
        setupMockCandidateView();
        mSpiedKeyboardView = Mockito.spy(inputView);
        originalInputContainer.addView(mMockCandidateView);
        originalInputContainer.addView(mSpiedKeyboardView);

        return originalInputContainer;
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

    public void simulateTextTyping(
            final String text, final boolean advanceTime, final boolean asDiscreteKeys) {
        if (asDiscreteKeys) {
            for (char key : text.toCharArray()) {
                simulateKeyPress(key, advanceTime);
            }
        } else {
            onText(null, text);
            if (advanceTime) Robolectric.flushForegroundThreadScheduler();
            if (advanceTime) SystemClock.sleep(25);
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
        if (advanceTime) Robolectric.flushForegroundThreadScheduler();
        final AnyKeyboard keyboard = getCurrentKeyboard();
        Assert.assertNotNull(keyboard);
        if (key instanceof AnyKeyboard.AnyKey /*this will ensure this instance is not a mock*/) {
            final int keyCodeWithShiftState =
                    key.getCodeAtIndex(
                            0,
                            mSpiedKeyboardView != null
                                    && mSpiedKeyboardView.getKeyDetector().isKeyShifted(key));
            int[] nearByKeyCodes = new int[64];
            if (mSpiedKeyboardView != null) {
                mSpiedKeyboardView
                        .getKeyDetector()
                        .getKeyIndexAndNearbyCodes(key.centerX, key.centerY, nearByKeyCodes);
            }
            onKey(keyCodeWithShiftState, key, 0, nearByKeyCodes, true);
        } else {
            onKey(primaryCode, null, 0, new int[0], true);
        }
        if (advanceTime) Robolectric.flushForegroundThreadScheduler();
        if (advanceTime) SystemClock.sleep(25);
        onRelease(primaryCode);
        if (advanceTime) Robolectric.flushForegroundThreadScheduler();
    }

    @Nullable
    public static Keyboard.Key findKeyWithPrimaryKeyCode(int keyCode, AnyKeyboard keyboard) {
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

    @Override
    public AbstractInputMethodSessionImpl onCreateInputMethodSessionInterface() {
        return mCreatedInputMethodSession = super.onCreateInputMethodSessionInterface();
    }

    public AbstractInputMethodSessionImpl getCreatedInputMethodSessionInterface() {
        return mCreatedInputMethodSession;
    }

    @NonNull
    @Override
    public AbstractInputMethodImpl onCreateInputMethodInterface() {
        return mCreatedInputMethodInterface = super.onCreateInputMethodInterface();
    }

    public AbstractInputMethodImpl getCreatedInputMethodInterface() {
        return mCreatedInputMethodInterface;
    }

    public TestInputConnection getCurrentTestInputConnection() {
        return mInputConnection;
    }

    @NonNull
    @Override
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
                                                                "three".toCharArray()
                                                            },
                                                            new int[] {
                                                                180, 100, 253, 200, 120, 140, 100,
                                                                80, 60
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

    public static class TestableSuggest extends Suggest {

        private final Map<String, List<CharSequence>> mDefinedWords = new HashMap<>();
        private boolean mHasMinimalCorrection;
        private boolean mEnabledSuggestions;

        public TestableSuggest(Context context) {
            super(context);
        }

        public void setSuggestionsForWord(String word, CharSequence... suggestions) {
            mDefinedWords.put(word.toLowerCase(), Arrays.asList(suggestions));
        }

        @Override
        public void setCorrectionMode(
                boolean enabledSuggestions,
                int maxLengthDiff,
                int maxDistance,
                int minimumWorLength) {
            super.setCorrectionMode(
                    enabledSuggestions, maxLengthDiff, maxDistance, minimumWorLength);
            mEnabledSuggestions = enabledSuggestions;
        }

        @Override
        public List<CharSequence> getSuggestions(
                WordComposer wordComposer, boolean includeTypedWordIfValid) {
            if (!mEnabledSuggestions) return Collections.emptyList();

            if (wordComposer.isAtTagsSearchState()) {
                return super.getSuggestions(wordComposer, includeTypedWordIfValid);
            }

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
}
