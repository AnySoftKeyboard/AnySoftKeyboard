package com.anysoftkeyboard.ui.settings.wordseditor;

import android.content.Context;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.IgnoredWordsDictionary;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;
import net.evendanan.pixel.UiUtils;

public class IgnoredWordsEditorFragment extends UserDictionaryEditorFragment {

  @Override
  public void onStart() {
    super.onStart();
    UiUtils.setActivityTitle(this, getString(R.string.ignored_words_dict_settings_titlebar));
  }

  @Override
  protected EditableDictionary createEditableDictionary(String locale) {
    return new MyIgnoredWordsDictionary(requireContext().getApplicationContext(), locale);
  }

  private static class MyIgnoredWordsDictionary extends IgnoredWordsDictionary
      implements MyEditableDictionary {

    @NonNull private final List<LoadedWord> mLoadedWords = new ArrayList<>();

    public MyIgnoredWordsDictionary(Context context, String locale) {
      super(context, locale);
    }

    @Override
    protected void readWordsFromActualStorage(final WordReadListener listener) {
      mLoadedWords.clear();
      WordReadListener myListener =
          (word, frequency) -> {
            mLoadedWords.add(new LoadedWord(word, frequency));
            return listener.onWordRead(word, frequency);
          };
      super.readWordsFromActualStorage(myListener);
    }

    @NonNull
    @Override
    public List<LoadedWord> getLoadedWords() {
      return mLoadedWords;
    }
  }
}
