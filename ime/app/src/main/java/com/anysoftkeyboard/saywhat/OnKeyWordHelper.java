package com.anysoftkeyboard.saywhat;

import androidx.annotation.Nullable;
import com.anysoftkeyboard.keyboards.Keyboard;

public class OnKeyWordHelper {
  private final char[] mWord;
  private int mCurrentIndex = 0;

  public OnKeyWordHelper(String word) {
    this(word.toCharArray());
  }

  public OnKeyWordHelper(char[] word) {
    mWord = new char[word.length];
    System.arraycopy(word, 0, mWord, 0, mWord.length);
  }

  public boolean shouldShow(@Nullable Keyboard.Key pressedKey) {
    return pressedKey != null && shouldShow(pressedKey.getPrimaryCode());
  }

  public boolean shouldShow(int pressedKeyCode) {
    if (pressedKeyCode == mWord[mCurrentIndex]) {
      mCurrentIndex++;
      if (mCurrentIndex == mWord.length) {
        mCurrentIndex = 0;
        return true;
      }
    } else {
      mCurrentIndex = 0;
      if (pressedKeyCode == mWord[0]) {
        // special reset case where the reset character is actually the first in the array
        return shouldShow(pressedKeyCode);
      }
    }
    return false;
  }
}
