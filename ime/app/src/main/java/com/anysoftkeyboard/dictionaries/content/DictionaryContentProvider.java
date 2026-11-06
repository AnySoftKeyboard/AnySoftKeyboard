package com.anysoftkeyboard.dictionaries.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A minimal stub {@link ContentProvider} that registers the {@code ${applicationId}.dictionary}
 * authority with Android's system {@code ContentService}.
 *
 * <p>Starting with Android 8.0 (API 26), observing or notifying changes on a URI with an
 * unregistered authority throws {@link SecurityException}. This provider satisfies that requirement
 * for SQLite-backed dictionary change notifications without exposing or querying external data.
 */
public class DictionaryContentProvider extends ContentProvider {

  @Override
  public boolean onCreate() {
    return true;
  }

  @Nullable
  @Override
  public Cursor query(
      @NonNull Uri uri,
      @Nullable String[] projection,
      @Nullable String selection,
      @Nullable String[] selectionArgs,
      @Nullable String sortOrder) {
    return null;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
    return null;
  }

  @Override
  public int delete(
      @NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(
      @NonNull Uri uri,
      @Nullable ContentValues values,
      @Nullable String selection,
      @Nullable String[] selectionArgs) {
    return 0;
  }
}
