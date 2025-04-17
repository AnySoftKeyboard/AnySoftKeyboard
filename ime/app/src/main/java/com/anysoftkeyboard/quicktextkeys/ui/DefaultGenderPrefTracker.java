package com.anysoftkeyboard.quicktextkeys.ui;

import androidx.annotation.Nullable;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.menny.android.anysoftkeyboard.R;
import emoji.utils.JavaEmojiUtils;
import io.reactivex.disposables.Disposable;
import java.util.Random;

public class DefaultGenderPrefTracker implements Disposable {

  private final Disposable mDisposable;

  @Nullable private JavaEmojiUtils.Gender mDefaultGender = null;
  private boolean mRandom = false;

  public DefaultGenderPrefTracker(RxSharedPrefs prefs) {
    mDisposable =
        prefs
            .getString(
                R.string.settings_key_default_emoji_gender, R.string.settings_default_emoji_gender)
            .asObservable()
            .subscribe(
                value -> {
                  mRandom = false;
                  mDefaultGender = null;
                  switch (value) {
                    case "woman":
                      mDefaultGender = JavaEmojiUtils.Gender.Woman;
                      break;
                    case "man":
                      mDefaultGender = JavaEmojiUtils.Gender.Man;
                      break;
                    case "random":
                      mRandom = true;
                      break;
                    default:
                      break;
                  }
                });
  }

  @Nullable
  public JavaEmojiUtils.Gender getDefaultGender() {
    if (mRandom) {
      if (new Random().nextBoolean()) {
        return JavaEmojiUtils.Gender.Woman;
      } else {
        return JavaEmojiUtils.Gender.Man;
      }
    }
    return mDefaultGender;
  }

  @Override
  public void dispose() {
    mDisposable.dispose();
  }

  @Override
  public boolean isDisposed() {
    return mDisposable.isDisposed();
  }
}
