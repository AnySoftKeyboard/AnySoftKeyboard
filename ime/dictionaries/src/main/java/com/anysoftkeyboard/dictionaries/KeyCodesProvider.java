package com.anysoftkeyboard.dictionaries;

public interface KeyCodesProvider {
  int codePointCount();

  int[] getCodesAt(int index);

  CharSequence getTypedWord();
}
