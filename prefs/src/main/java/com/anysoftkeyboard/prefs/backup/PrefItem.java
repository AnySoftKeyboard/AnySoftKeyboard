package com.anysoftkeyboard.prefs.backup;

import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrefItem {
    private final List<Pair<String, String>> mValues = new ArrayList<>();
    private final List<PrefItem> mChildren = new ArrayList<>();

    PrefItem() {
        /*ensuring that all istances are created in this package*/
    }

    public void addValue(String key, String value) {
        mValues.add(Pair.create(validKey(key), value));
    }

    public Iterable<Pair<String, String>> getValues() {
        return Collections.unmodifiableCollection(mValues);
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
}
