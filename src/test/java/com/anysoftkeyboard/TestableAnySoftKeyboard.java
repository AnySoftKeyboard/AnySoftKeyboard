package com.anysoftkeyboard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.addons.AddOn;
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
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowSystemClock;

public class TestableAnySoftKeyboard extends AnySoftKeyboard {

    private Suggest mSpiedSuggest;
    private TestableKeyboardSwitcher mSpiedKeyboardSwitcher;
    private AnyKeyboardView mSpiedKeyboardView;
    private EditorInfo mEditorInfo;
    private TestInputConnection mInputConnection;
    private CandidateView mMockCandidateView;
    private UserDictionary mSpiedUserDictionary;

    public Suggest getSpiedSuggest() {
        return mSpiedSuggest;
    }

    public CandidateView getMockCandidateView() {
        return mMockCandidateView;
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
        if (restarting || mInputConnection == null) mInputConnection = Mockito.spy(new TestInputConnection(this));
        super.onStartInput(attribute, restarting);
    }

    @Override
    public View onCreateCandidatesView() {
        View spiedRootView = Mockito.spy(super.onCreateCandidatesView());
        mMockCandidateView = Mockito.mock(CandidateView.class);
        Mockito.doReturn(mMockCandidateView).when(spiedRootView).findViewById(R.id.candidates);
        return spiedRootView;
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
        Assert.assertNull(mSpiedKeyboardView);
        return mSpiedKeyboardView = Mockito.spy((AnyKeyboardView) super.onCreateInputView());
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
                updateInputConnection(key);
            }
        } else {
            onText(null, text);
            Robolectric.flushForegroundThreadScheduler();
            if (advanceTime) ShadowSystemClock.sleep(25);
        }
    }

    private void updateInputConnection(char key) {
        mInputConnection.appendToInput(key);
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
            onKey(keyCode, key, 0, keyboard.getNearestKeys(key.x + 5, key.y + 5), true);
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

    public static class TestableSuggest extends Suggest {

        public TestableSuggest(Context context) {
            super(context);
        }

        @NonNull
        @Override
        protected DictionaryFactory createDictionaryFactory() {
            return Mockito.spy(super.createDictionaryFactory());
        }
    }

    public static class TestableKeyboardSwitcher extends KeyboardSwitcher {

        public TestableKeyboardSwitcher(@NonNull AnySoftKeyboard ime) {
            super(ime);
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

    public static class TestInputConnection implements InputConnection {

        private int mCursorPosition = 0;
        private String mInputText = "";
        @NonNull
        private final AnySoftKeyboard mIme;

        public TestInputConnection(@NonNull AnySoftKeyboard ime) {
            mIme = ime;
        }

        @Override
        public CharSequence getTextBeforeCursor(int n, int flags) {
            return mInputText.substring(mCursorPosition - n, mCursorPosition);
        }

        @Override
        public CharSequence getTextAfterCursor(int n, int flags) {
            return mInputText.substring(mCursorPosition, mCursorPosition + n);
        }

        @Override
        public CharSequence getSelectedText(int flags) {
            return "";
        }

        @Override
        public int getCursorCapsMode(int reqModes) {
            return 0;
        }

        @Override
        public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
            return null;
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            String beforeText = mInputText.substring(0, mCursorPosition-beforeLength);
            String afterText = mInputText.substring(mCursorPosition+afterLength);
            mInputText = beforeText+afterText;
            notifyTextChange(-beforeLength);
            return true;
        }

        private void notifyTextChange(int cursorDelta) {
            final int oldPosition = mCursorPosition;
            mCursorPosition += cursorDelta;
            mIme.onUpdateSelection(oldPosition, oldPosition, mCursorPosition, mCursorPosition, mCursorPosition, mCursorPosition);
        }

        @Override
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            return false;
        }

        @Override
        public boolean setComposingRegion(int start, int end) {
            return false;
        }

        @Override
        public boolean finishComposingText() {
            return false;
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return true;
        }

        @Override
        public boolean commitCompletion(CompletionInfo text) {
            return false;
        }

        @Override
        public boolean commitCorrection(CorrectionInfo correctionInfo) {
            return true;
        }

        @Override
        public boolean setSelection(int start, int end) {
            return false;
        }

        @Override
        public boolean performEditorAction(int editorAction) {
            return false;
        }

        @Override
        public boolean performContextMenuAction(int id) {
            return false;
        }

        @Override
        public boolean beginBatchEdit() {
            return true;
        }

        @Override
        public boolean endBatchEdit() {
            return true;
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            return true;
        }

        @Override
        public boolean clearMetaKeyStates(int states) {
            return true;
        }

        @Override
        public boolean reportFullscreenMode(boolean enabled) {
            return false;
        }

        @Override
        public boolean performPrivateCommand(String action, Bundle data) {
            return false;
        }

        @Override
        public boolean requestCursorUpdates(int cursorUpdateMode) {
            return false;
        }

        public void appendToInput(char key) {
            mInputText += key;
            notifyTextChange(1);
        }
    }
}
