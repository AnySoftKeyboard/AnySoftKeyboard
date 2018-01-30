package com.anysoftkeyboard.prefs.backup;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefItem {
    private final Map<String, String> mValues = new HashMap<>();
    private final List<PrefItem> mChildren = new ArrayList<>();

    PrefItem() {
        /*ensuring that all instances are created in this package*/
    }

    public PrefItem addValue(String key, String value) {
        mValues.put(validKey(key), value);
        return this;
    }

    public Iterable<Map.Entry<String, String>> getValues() {
        return Collections.unmodifiableCollection(mValues.entrySet());
    }

    @Nullable
    public String getValue(String key) {
        return mValues.get(key);
    }

    public PrefItem createChild() {
        PrefItem child = new PrefItem();
        mChildren.add(child);
        return child;
    }

    public Iterable<PrefItem> getChildren() {
        return Collections.unmodifiableCollection(mChildren);
    }

    private static String validKey(String text) {
        if (text.matches("\\A[\\p{Upper}|\\p{Lower}]+[\\p{Upper}|\\p{Lower}|\\p{Digit}]*\\z")) {
            return text;
        } else {
            throw new IllegalArgumentException("The key '" + text + "' has non ASCII or has whitespaces or is empty! This is not valid as an XML attribute");
        }
    }

    public void addChild(PrefItem prefsItem) {
        mChildren.add(prefsItem);
    }
}
