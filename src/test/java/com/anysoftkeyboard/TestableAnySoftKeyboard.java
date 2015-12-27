package com.anysoftkeyboard;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.keyboards.KeyboardSwitcher;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;

import org.junit.Assert;
import org.mockito.Mockito;

public class TestableAnySoftKeyboard extends AnySoftKeyboard {

    private Suggest mSpiedSuggest;
    private KeyboardSwitcher mSpiedKeyboardSwitcher;
    private AnyKeyboardView mSpiedKeyboardView;

    public Suggest getSpiedSuggest() {
        return mSpiedSuggest;
    }

    @NonNull
    @Override
    protected Suggest createSuggest() {
        Assert.assertNull(mSpiedSuggest);
        return mSpiedSuggest = Mockito.spy(super.createSuggest());
    }

    public KeyboardSwitcher getSpiedKeyboardSwitcher() {
        return mSpiedKeyboardSwitcher;
    }

    @NonNull
    @Override
    protected KeyboardSwitcher createKeyboardSwitcher() {
        Assert.assertNull(mSpiedKeyboardSwitcher);
        return mSpiedKeyboardSwitcher = Mockito.spy(super.createKeyboardSwitcher());
    }

    @Override
    public View onCreateInputView() {
        Assert.assertNull(mSpiedKeyboardView);
        return mSpiedKeyboardView = Mockito.spy((AnyKeyboardView) super.onCreateInputView());
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
}
