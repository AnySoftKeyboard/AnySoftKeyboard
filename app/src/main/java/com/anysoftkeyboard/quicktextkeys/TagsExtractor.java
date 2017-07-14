package com.anysoftkeyboard.quicktextkeys;

import android.support.annotation.NonNull;

import com.anysoftkeyboard.base.dictionaries.KeyCodesProvider;

import java.util.List;

public interface TagsExtractor {
    /**
     * Is this extractor actually do anything.
     */
    boolean isEnabled();

    /**
     * Returns a list of all quick-text outputs related to the given tag.
     */
    List<CharSequence> getOutputForTag(@NonNull CharSequence typedTagToSearch, KeyCodesProvider wordComposer);
}
