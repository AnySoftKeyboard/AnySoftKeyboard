package com.anysoftkeyboard.ui.settings.wordseditor;

import android.support.annotation.NonNull;
import com.anysoftkeyboard.RobolectricFragmentTestCase;

public class AbbreviationDictionaryEditorFragmentTest
        extends RobolectricFragmentTestCase<AbbreviationDictionaryEditorFragment> {

    @NonNull
    @Override
    protected AbbreviationDictionaryEditorFragment createFragment() {
        return new AbbreviationDictionaryEditorFragment();
    }
}
