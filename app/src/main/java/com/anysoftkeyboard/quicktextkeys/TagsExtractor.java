package com.anysoftkeyboard.quicktextkeys;

import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;

import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TagsExtractor {

    private final ArrayMap<String, List<CharSequence>> mTagsForOutputs = new ArrayMap<>();

    public TagsExtractor(@NonNull List<Keyboard.Key> keys) {
        for (Keyboard.Key key : keys) {
            AnyKeyboard.AnyKey anyKey = (AnyKeyboard.AnyKey) key;
            for (String tag : anyKey.getKeyTags()) {
                if (!mTagsForOutputs.containsKey(tag))
                    mTagsForOutputs.put(tag, new ArrayList<CharSequence>());
                mTagsForOutputs.get(tag).add(anyKey.text);

            }
        }
    }

    public List<CharSequence> getOutputForTag(@NonNull String tag) {
        if (mTagsForOutputs.containsKey(tag))
            return mTagsForOutputs.get(tag);
        else
            return Collections.emptyList();
    }
}
