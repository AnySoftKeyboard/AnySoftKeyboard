package com.anysoftkeyboard.base.dictionaries;

public interface KeyCodesProvider {
    int length();

    int[] getCodesAt(int index);

    CharSequence getTypedWord();
}
