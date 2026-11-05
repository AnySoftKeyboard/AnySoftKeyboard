/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.dictionaries.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.content.ContentObserverDictionary;
import com.anysoftkeyboard.rx.GenericOnError;
import com.anysoftkeyboard.rx.RxSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.subjects.PublishSubject;

public abstract class SQLiteUserDictionaryBase extends ContentObserverDictionary {
  private static final String TAG = "SQLiteUserDictBase";

  private static final PublishSubject<StorageChangedEvent> msStorageChangedSubject =
      PublishSubject.create();

  private final String mLocale;
  private volatile WordsSQLiteConnection mStorage;
  @NonNull private Disposable mStorageChangedDisposable = Disposables.disposed();

  private static class StorageChangedEvent {
    @NonNull final Object source;
    @NonNull final CharSequence dictionaryName;
    @Nullable final String locale;

    StorageChangedEvent(
        @NonNull Object source, @NonNull CharSequence dictionaryName, @Nullable String locale) {
      this.source = source;
      this.dictionaryName = dictionaryName;
      this.locale = locale;
    }
  }

  protected SQLiteUserDictionaryBase(String dictionaryName, Context context, String locale) {
    super(dictionaryName, context, null /*internal storage, we know when it changes*/);
    mLocale = locale;
    Logger.d(TAG, "Created instance of %s for locale %s.", dictionaryName, locale);
  }

  public String getLocale() {
    return mLocale;
  }

  @Override
  protected void loadAllResources() {
    super.loadAllResources();

    if (mStorageChangedDisposable.isDisposed()) {
      mStorageChangedDisposable =
          msStorageChangedSubject
              .filter(
                  event ->
                      !isClosed()
                          && event.source != this
                          && TextUtils.equals(event.dictionaryName, getDictionaryName())
                          && TextUtils.equals(event.locale, mLocale))
              .observeOn(RxSchedulers.mainThread())
              .subscribe(
                  event -> {
                    if (!isClosed()) {
                      onStorageChanged();
                    }
                  },
                  GenericOnError.onError("SQLiteUserDictionaryBase.msStorageChangedSubject"));
    }
  }

  @Override
  protected void closeAllResources() {
    super.closeAllResources();
    mStorageChangedDisposable.dispose();
  }

  @Override
  protected void readWordsFromActualStorage(WordReadListener listener) {
    try {
      if (mStorage == null) mStorage = createStorage(mLocale);

      mStorage.loadWords(listener);
    } catch (SQLiteException e) {
      e.printStackTrace();
      final String dbFile = mStorage.getDbFilename();
      try {
        mStorage.close();
      } catch (SQLiteException swallow) {
        Logger.w(
            TAG,
            "Caught an SQL exception while closing database (message: '%s').",
            swallow.getMessage());
      }
      Logger.w(
          TAG,
          "Caught an SQL exception while read database (message: '%s'). I'll delete the"
              + " database '%s'...",
          e.getMessage(),
          dbFile);
      try {
        mContext.deleteDatabase(dbFile);
      } catch (Exception okToFailEx) {
        Logger.w(TAG, "Failed to delete database file " + dbFile + "!");
        okToFailEx.printStackTrace();
      }
      mStorage = null; // will re-create the storage.
      mStorage = createStorage(mLocale);
      // if this function will throw an exception again, well the hell with it.
      mStorage.loadWords(listener);
    }
  }

  protected WordsSQLiteConnection createStorage(String locale) {
    return new WordsSQLiteConnection(mContext, getDictionaryName() + ".db", locale);
  }

  @Override
  protected final void addWordToStorage(String word, int frequency) {
    if (mStorage != null) mStorage.addWord(word, frequency);
    notifyStorageChanged();
  }

  @Override
  protected final void deleteWordFromStorage(String word) {
    if (mStorage != null) mStorage.deleteWord(word);
    notifyStorageChanged();
  }

  protected void notifyStorageChanged() {
    msStorageChangedSubject.onNext(new StorageChangedEvent(this, getDictionaryName(), mLocale));
  }

  @Override
  protected void closeStorage() {
    if (mStorage != null) mStorage.close();
    mStorage = null;
  }
}
