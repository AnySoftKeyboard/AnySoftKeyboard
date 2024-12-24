package com.anysoftkeyboard.dictionaries.prefsprovider;

import android.content.Context;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionary;

class TappedAndroidUserDictionary extends AndroidUserDictionary {

  private final WordReadListener mWordsTapper;

  public TappedAndroidUserDictionary(Context context, String locale, WordReadListener wordsTapper) {
    super(context, locale, null /*DO NOT LISTEN TO CHANGES FROM THE OUTSIDE*/);
    mWordsTapper = wordsTapper;
  }

  @NonNull
  @Override
  protected WordReadListener createWordReadListener() {
    return mWordsTapper;
  }
}
