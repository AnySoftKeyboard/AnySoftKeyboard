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

package com.anysoftkeyboard.dictionaries;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.io.File;

/**
 * Centralises all read/write/invalidate logic for the user-pickable radical-overlay folder.
 *
 * <p>One single source of truth for:
 *
 * <ul>
 *   <li>the {@link SharedPreferences} key used to persist the picked folder URI;
 *   <li>the default (file-based, app-private) fallback folder layout, subdir name {@code "radical"}
 *       under {@link Context#getExternalFilesDir(String)};
 *   <li>building the appropriate {@link RadicalOverlay} (SAF-backed or file-backed) wrapped in the
 *       host's process-wide {@link RadicalOverlayCache};
 *   <li>SAF persistable URI permission lifecycle (take on pick / release on clear);
 *   <li>cache invalidation, used by the settings "Refresh imported tables" button to pick up edits
 *       the user made outside the IME (e.g. via a text editor).
 * </ul>
 *
 * <p>Engine-side callers ({@link DictionaryAddOnAndBuilder}) and UI-side callers (Settings
 * fragment) both go through here so the contract stays consistent. Previously the engine referenced
 * the hard-coded subdir name {@code "boshiamy"} which was misleading for users importing
 * Cangjie/Zhuyin tables; the canonical name is now {@code "radical"}.
 */
public final class RadicalOverlayRepository {

  private static final String TAG = "ASKRadicalOverlayRepo";

  /** Subdir name used under {@link Context#getExternalFilesDir(String)} for the fallback path. */
  public static final String EXTERNAL_SUBDIR = "radical";

  /** Legacy subdir name kept for one-shot read-only fallback if the user had data under it. */
  private static final String LEGACY_EXTERNAL_SUBDIR = "boshiamy";

  private RadicalOverlayRepository() {
    // utility
  }

  /**
   * Returns the user-picked SAF tree URI, or {@code null} if none has been picked (in which case
   * callers should fall back to the file-based default).
   */
  @Nullable
  public static Uri getOverlayUri(@NonNull Context askContext) {
    final String raw = readOverlayUriString(askContext);
    if (raw == null || !raw.startsWith("content://")) return null;
    try {
      return Uri.parse(raw);
    } catch (Exception e) {
      Logger.w(TAG, "Failed to parse stored overlay URI '%s': %s", raw, e.getMessage());
      return null;
    }
  }

  /**
   * Stores the picked SAF tree URI and takes a persistable read permission so the URI remains
   * usable after process death.
   */
  /**
   * Returns true when {@code treeUri} looks like a usable overlay folder, that is it contains at
   * least one file this addon knows how to read, either directly or inside a per-keyboard
   * sub-folder.
   *
   * <p>Picking the wrong folder is easy and the failure is otherwise silent (the keyboard simply
   * keeps using the bundled tables), so the picker uses this to reject the selection up front
   * instead of leaving the user wondering why nothing changed.
   */
  public static boolean looksLikeOverlayFolder(@NonNull Context askContext, @NonNull Uri treeUri) {
    final ContentResolver resolver = askContext.getContentResolver();
    final String treeDocId;
    try {
      treeDocId = DocumentsContract.getTreeDocumentId(treeUri);
    } catch (Exception e) {
      Logger.w(TAG, "Bad tree URI %s: %s", treeUri, e.getMessage());
      return false;
    }
    if (hasKnownOverlayFile(resolver, treeUri, treeDocId, true)) return true;
    Logger.d(TAG, "No recognised overlay file under %s", treeUri);
    return false;
  }

  private static boolean hasKnownOverlayFile(
      @NonNull ContentResolver resolver,
      @NonNull Uri treeUri,
      @NonNull String docId,
      boolean descend) {
    final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId);
    final String[] projection = {
      DocumentsContract.Document.COLUMN_DOCUMENT_ID,
      DocumentsContract.Document.COLUMN_DISPLAY_NAME,
      DocumentsContract.Document.COLUMN_MIME_TYPE
    };
    java.util.List<String> subDirs = new java.util.ArrayList<>();
    try (android.database.Cursor c = resolver.query(childrenUri, projection, null, null, null)) {
      if (c == null) return false;
      while (c.moveToNext()) {
        final String childId = c.getString(0);
        final String name = c.getString(1);
        final String mime = c.getString(2);
        if (childId == null || name == null) continue;
        final boolean isDir = DocumentsContract.Document.MIME_TYPE_DIR.equals(mime);
        if (isDir) {
          if (descend) subDirs.add(childId);
        } else if (isKnownOverlayFileName(name)) {
          return true;
        }
      }
    } catch (Exception e) {
      Logger.w(TAG, "Failed listing %s: %s", childrenUri, e.getMessage());
      return false;
    }
    // Only one level down, matching the documented <picked>/<addon-uuid>/ layout.
    for (String sub : subDirs) {
      if (hasKnownOverlayFile(resolver, treeUri, sub, false)) return true;
    }
    return false;
  }

  private static boolean isKnownOverlayFileName(@NonNull String name) {
    if (DocumentFileRadicalOverlay.isLiuCharacterTable(name)) return true;
    final String lower = name.toLowerCase(java.util.Locale.ROOT);
    return lower.equals(FileBasedRadicalOverlay.FILE_MAIN)
        || lower.equals(FileBasedRadicalOverlay.FILE_MAIN_OVERLAY)
        || lower.equals(FileBasedRadicalOverlay.FILE_PHRASES)
        || lower.equals(FileBasedRadicalOverlay.FILE_PHRASES_OVERLAY)
        || lower.equals(FileBasedRadicalOverlay.FILE_HOMOPHONES)
        || lower.equals(FileBasedRadicalOverlay.FILE_CHAR_TO_ZHUYIN)
        || lower.equals(FileBasedRadicalOverlay.FILE_CHAR_TO_RADICAL)
        || lower.equals(FileBasedRadicalOverlay.FILE_CHAR_FREQUENCY);
  }

  public static void setOverlayUri(@NonNull Context askContext, @NonNull Uri treeUri) {
    final ContentResolver resolver = askContext.getContentResolver();
    final Uri previous = getOverlayUri(askContext);
    try {
      resolver.takePersistableUriPermission(
          treeUri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
    } catch (Exception e) {
      Logger.w(TAG, "takePersistableUriPermission failed for %s: %s", treeUri, e.getMessage());
      // Don't persist a URI we can't actually keep across reboot, the user would otherwise see
      // "picked" in the summary until the next time they enter the fragment, where
      // isUriStillAccessible flips it to "lost". Leave prefs untouched and let the caller
      // surface the failure.
      throw new SecurityException("Could not take persistable read permission for " + treeUri, e);
    }
    // Release the persistable read permission we previously held for any OLD picked URI,
    // otherwise repeated re-picks accumulate orphaned permissions and eventually hit Android's
    // per-app cap (takePersistableUriPermission would then start throwing).
    if (previous != null && !previous.equals(treeUri)) {
      try {
        resolver.releasePersistableUriPermission(
            previous, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
      } catch (Exception e) {
        Logger.w(
            TAG,
            "releasePersistableUriPermission failed for previous %s: %s",
            previous,
            e.getMessage());
      }
    }
    writeOverlayUriString(askContext, treeUri.toString());
    invalidateCache(askContext);
  }

  /**
   * Clears the stored URI and releases the persistable permission, returning the loader to the
   * file-based default.
   */
  public static void clearOverlayUri(@NonNull Context askContext) {
    final Uri previous = getOverlayUri(askContext);
    writeOverlayUriString(askContext, "");
    if (previous != null) {
      try {
        askContext
            .getContentResolver()
            .releasePersistableUriPermission(
                previous, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
      } catch (Exception e) {
        Logger.w(
            TAG, "releasePersistableUriPermission failed for %s: %s", previous, e.getMessage());
      }
    }
    invalidateCache(askContext);
  }

  /**
   * Drops the process-wide {@link RadicalOverlayCache} contents, forcing every subsequent keyboard
   * switch to re-materialise overlay bytes from disk / SAF. The "Refresh imported tables"
   * preference invokes this so users can pick up edits made outside the IME.
   */
  public static void invalidateCache(@NonNull Context askContext) {
    try {
      AnyApplication.getExternalDictionaryFactory(askContext)
          .getRadicalOverlayCache()
          .invalidateAll();
    } catch (Exception e) {
      Logger.w(TAG, "Failed to invalidate RadicalOverlayCache: %s", e.getMessage());
    }
  }

  /**
   * Builds the {@link RadicalOverlay} for a given keyboard addon, wrapped in the process-wide
   * cache. Returns {@link RadicalOverlay#NONE} if no suitable base directory can be located.
   */
  @NonNull
  public static RadicalOverlay buildOverlayForKeyboard(
      @NonNull Context askContext, @NonNull Context packageContext, @NonNull CharSequence addonId) {
    final String addonIdStr = addonId.toString();
    final Uri safUri = getOverlayUri(askContext);
    final RadicalOverlay base;
    final String cacheKey;
    final String cacheKeySuffix = "/" + addonIdStr;
    if (safUri != null) {
      base = new DocumentFileRadicalOverlay(askContext, safUri, addonIdStr);
      cacheKey = "saf:" + safUri + cacheKeySuffix;
    } else {
      final File externalDir = packageContext.getExternalFilesDir(null);
      if (externalDir == null) return RadicalOverlay.NONE;
      File baseDir = new File(externalDir, EXTERNAL_SUBDIR);
      // Fall back to the legacy subdir if the user already populated it under the old name and
      // hasn't migrated; this keeps existing alpha-cohort imported tables working without a
      // manual move. We require the new dir to be absent OR empty before falling back, so an
      // unrelated `mkdir radical/` doesn't silently hide the user's data.
      // TODO(post-1.x): drop LEGACY_EXTERNAL_SUBDIR once the alpha cohort has migrated.
      if (isMissingOrEmpty(baseDir)) {
        final File legacy = new File(externalDir, LEGACY_EXTERNAL_SUBDIR);
        if (legacy.isDirectory() && hasAnyChildren(legacy)) {
          Logger.w(
              TAG,
              "Using LEGACY overlay folder '%s' (please rename to '%s')",
              legacy.getAbsolutePath(),
              EXTERNAL_SUBDIR);
          baseDir = legacy;
        }
      }
      base = new FileBasedRadicalOverlay(baseDir, addonIdStr);
      cacheKey = "file:" + baseDir.getAbsolutePath() + cacheKeySuffix;
    }
    try {
      return AnyApplication.getExternalDictionaryFactory(askContext)
          .getRadicalOverlayCache()
          .wrap(cacheKey, base);
    } catch (Exception e) {
      Logger.w(TAG, "Failed to access RadicalOverlayCache: %s", e.getMessage());
      return base;
    }
  }

  private static boolean isMissingOrEmpty(@NonNull File dir) {
    if (!dir.isDirectory()) return true;
    final String[] entries = dir.list();
    return entries == null || entries.length == 0;
  }

  private static boolean hasAnyChildren(@NonNull File dir) {
    final String[] entries = dir.list();
    return entries != null && entries.length > 0;
  }

  // --- pref read/write helpers -------------------------------------------------------------

  @Nullable
  private static String readOverlayUriString(@NonNull Context askContext) {
    try {
      final RxSharedPrefs prefs = AnyApplication.prefs(askContext);
      return prefs
          .getString(R.string.settings_key_radical_overlay_dir, R.string.settings_default_empty)
          .get();
    } catch (Exception e) {
      // Fall back to the raw SharedPreferences if Rx wrapper is not ready (e.g. very early
      // startup or unit tests).
      try {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(askContext);
        return sp.getString(askContext.getString(R.string.settings_key_radical_overlay_dir), "");
      } catch (Exception inner) {
        Logger.w(TAG, "Failed to read overlay preference: %s", inner.getMessage());
        return null;
      }
    }
  }

  private static void writeOverlayUriString(@NonNull Context askContext, @NonNull String value) {
    try {
      final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(askContext);
      sp.edit()
          .putString(askContext.getString(R.string.settings_key_radical_overlay_dir), value)
          .apply();
    } catch (Exception e) {
      Logger.w(TAG, "Failed to persist overlay preference: %s", e.getMessage());
    }
  }
}
