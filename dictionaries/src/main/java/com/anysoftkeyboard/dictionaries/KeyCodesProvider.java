package com.anysoftkeyboard.dictionaries;

public interface KeyCodesProvider {
    int length();

    int[] getCodesAt(int index);

    CharSequence getTypedWord();
}
