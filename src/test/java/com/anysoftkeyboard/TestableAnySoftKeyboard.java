package com.anysoftkeyboard;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.anysoftkeyboard.addons.AddOn;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.GenericKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;

import org.junit.Assert;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowSystemClock;

public class TestableAnySoftKeyboard extends AnySoftKeyboard {

    private Suggest mSpiedSuggest;
    private TestableKeyboardSwitcher mSpiedKeyboardSwitcher;
    private AnyKeyboardView mSpiedKeyboardView;
    private EditorInfo mEditorInfo;
    private InputConnection mInputConnection;

    public Suggest getSpiedSuggest() {
        return mSpiedSuggest;
    }

    @NonNull
    @Override
    protected Suggest createSuggest() {
        Assert.assertNull(mSpiedSuggest);
        return mSpiedSuggest = Mockito.spy(super.createSuggest());
    }

    public TestableKeyboardSwitcher getSpiedKeyboardSwitcher() {
        return mSpiedKeyboardSwitcher;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        mEditorInfo = attribute;
        mInputConnection = Mockito.mock(InputConnection.class);
        super.onStartInput(attribute, restarting);
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

    public static class TestableKeyboardSwitcher extends KeyboardSwitcher {

        public TestableKeyboardSwitcher(@NonNull AnySoftKeyboard ime) {
            super(ime);
        }

        @Override
        public AnyKeyboard createKeyboardFromCreator(int mode, KeyboardAddOnAndBuilder creator) {
            return super.createKeyboardFromCreator(mode, creator);
        }

        @Override
        public GenericKeyboard createGenericKeyboard(AddOn addOn, Context context, int layoutResId, int landscapeLayoutResId, String name, String keyboardId, int mode, boolean disableKeyPreview) {
            return super.createGenericKeyboard(addOn, context, layoutResId, landscapeLayoutResId, name, keyboardId, mode, disableKeyPreview);
        }
    }
}
