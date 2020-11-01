package com.anysoftkeyboard.dictionaries;

import androidx.annotation.Keep;

/** Interface used from JNI to javaland. Must never be removed or renamed with R8. */
@Keep
public interface GetWordsCallback {
    void onGetWordsFinished(char[][] words, int[] frequencies);
}
