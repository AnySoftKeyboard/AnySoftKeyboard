package com.anysoftkeyboard.dictionaries;

public interface GetWordsCallback {
    void onGetWordsFinished(char[][] words, int[] frequencies);
}
