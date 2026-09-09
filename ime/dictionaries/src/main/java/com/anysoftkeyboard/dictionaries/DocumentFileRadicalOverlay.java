/*
 * Copyright (c) 2026 AnySoftKeyboard contributors
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
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.anysoftkeyboard.base.utils.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * {@link RadicalOverlay} backed by a Storage Access Framework <em>tree URI</em> (the kind returned
 * by {@code Intent.ACTION_OPEN_DOCUMENT_TREE}).
 *
 * <p>This is the user-friendly counterpart of {@link FileBasedRadicalOverlay}: instead of requiring
 * {@code MANAGE_EXTERNAL_STORAGE} or restricting users to an addon-private external directory, the
 * user picks any folder via the system folder picker; the resulting tree URI grants persistable
 * read access without manifest permissions.
 *
 * <p>The per-keyboard subfolder convention is identical to {@link FileBasedRadicalOverlay}: the
 * user is expected to create a sub-folder whose name matches {@link
 * FileBasedRadicalOverlay#sanitizeKeyboardId(String)} of the addon ID. File names inside follow the
 * {@code FILE_*} constants on {@link FileBasedRadicalOverlay}.
 *
 * <p>Uses {@link DocumentsContract} directly (rather than the {@code
 * androidx.documentfile.provider.DocumentFile} wrapper) so the {@code dictionaries} module does not
 * need an extra dependency, and so the per-folder listing happens in a single cursor query instead
 * of an O(N) wrapper traversal.
 *
 * <p>If the picked folder is no longer accessible (permission revoked, provider gone), every
 * accessor returns {@code null} and a warning is logged, callers fall back to the bundled tables.
 * This matches the {@link RadicalOverlay#NONE} contract.
 */
public final class DocumentFileRadicalOverlay implements RadicalOverlay {

  private static final String TAG = "DocFileRadicalOverlay";

  /** MIME type returned by {@link DocumentsContract} for directories. */
  private static final String MIME_DIR = DocumentsContract.Document.MIME_TYPE_DIR;

  private static final String[] PROJECTION =
      new String[] {
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
      };

  @NonNull private final Context mContext;
  @NonNull private final Uri mTreeUri;
  @NonNull private final String mKeyboardId;

  /** Lazily-resolved child {@code Map<lowercase-name, Child>} for the per-keyboard folder. */
  @Nullable private Map<String, Child> mChildren;

  private boolean mResolved;

  /**
   * @param context any context, used only to fetch the {@link ContentResolver}; the application
   *     context is retained.
   * @param treeUri the tree URI returned by {@code ACTION_OPEN_DOCUMENT_TREE} (caller is
   *     responsible for {@code takePersistableUriPermission} on this URI before passing it in).
   * @param keyboardId the radical keyboard's stable ID (typically the addon's UUID).
   */
  public DocumentFileRadicalOverlay(
      @NonNull Context context, @NonNull Uri treeUri, @NonNull String keyboardId) {
    this.mContext = context.getApplicationContext();
    this.mTreeUri = treeUri;
    this.mKeyboardId = FileBasedRadicalOverlay.sanitizeKeyboardId(keyboardId);
  }

  /** A single child entry returned by listing the per-keyboard folder. */
  private static final class Child {
    @NonNull final String name;
    @NonNull final Uri uri;
    final boolean isFile;

    Child(@NonNull String name, @NonNull Uri uri, boolean isFile) {
      this.name = name;
      this.uri = uri;
      this.isFile = isFile;
    }
  }

  @Nullable
  private synchronized Map<String, Child> children() {
    if (mResolved) return mChildren;
    final ContentResolver resolver = mContext.getContentResolver();
    final String treeDocId;
    try {
      treeDocId = DocumentsContract.getTreeDocumentId(mTreeUri);
    } catch (Exception e) {
      Logger.w(TAG, "Bad tree URI %s: %s", mTreeUri, e.getMessage());
      // Permanent (URI is malformed), cache the failure so we don't retry every access.
      mResolved = true;
      return null;
    }
    // Find the per-keyboard sub-folder by display name in the tree root.
    final Uri rootChildrenUri =
        DocumentsContract.buildChildDocumentsUriUsingTree(mTreeUri, treeDocId);
    final String subDocId = findChildDocId(resolver, rootChildrenUri, mKeyboardId, true);
    // Fall back to the picked folder itself when there is no per-keyboard sub-folder.
    // Requiring one means knowing the keyboard's add-on UUID, which a user has no easy way
    // to discover, so the common "drop the tables in a folder and pick it" case must work.
    // The sub-folder layout is still honoured when present, which is what lets one picked
    // parent serve several radical keyboards with different tables.
    final String folderDocId = subDocId != null ? subDocId : treeDocId;

    // List children of the resolved folder.
    final Uri subChildrenUri =
        DocumentsContract.buildChildDocumentsUriUsingTree(mTreeUri, folderDocId);
    Map<String, Child> map = new LinkedHashMap<>();
    try (Cursor c = resolver.query(subChildrenUri, PROJECTION, null, null, null)) {
      if (c == null) {
        // Transient failure (DocumentsProvider not ready, etc), do NOT mark resolved, so the
        // next access retries. Otherwise a single cold-boot race could permanently poison the
        // overlay until "Refresh imported tables" is tapped.
        Logger.w(TAG, "Null cursor querying %s (transient, will retry)", subChildrenUri);
        return null;
      }
      final int idIdx = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
      final int nameIdx = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
      final int mimeIdx = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE);
      while (c.moveToNext()) {
        String docId = c.getString(idIdx);
        String name = c.getString(nameIdx);
        String mime = c.getString(mimeIdx);
        if (docId == null || name == null) continue;
        boolean isFile = mime != null && !MIME_DIR.equals(mime);
        Uri childUri = DocumentsContract.buildDocumentUriUsingTree(mTreeUri, docId);
        map.put(name.toLowerCase(Locale.ROOT), new Child(name, childUri, isFile));
      }
    } catch (SecurityException e) {
      Logger.w(TAG, "Permission lost for %s: %s", mTreeUri, e.getMessage());
      // Permission revoked by the system, permanent for this URI, cache the failure.
      mResolved = true;
      return null;
    } catch (Exception e) {
      Logger.w(
          TAG, "Failed listing %s: %s (transient, will retry)", subChildrenUri, e.getMessage());
      // Other exception (provider died, IO blip, ...), likely transient, retry on next access.
      return null;
    }
    mChildren = map;
    mResolved = true;
    return map;
  }

  /**
   * Returns the document ID of the first child of {@code parentChildrenUri} whose display name
   * equals {@code targetName} (case-sensitive, matches what the picker stored), and optionally is a
   * directory.
   */
  @Nullable
  private static String findChildDocId(
      @NonNull ContentResolver resolver,
      @NonNull Uri parentChildrenUri,
      @NonNull String targetName,
      boolean mustBeDirectory) {
    try (Cursor c = resolver.query(parentChildrenUri, PROJECTION, null, null, null)) {
      if (c == null) return null;
      final int idIdx = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
      final int nameIdx = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
      final int mimeIdx = c.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE);
      while (c.moveToNext()) {
        String name = c.getString(nameIdx);
        if (!targetName.equals(name)) continue;
        if (mustBeDirectory) {
          String mime = c.getString(mimeIdx);
          if (!MIME_DIR.equals(mime)) continue;
        }
        return c.getString(idIdx);
      }
    } catch (SecurityException e) {
      Logger.w(TAG, "Permission lost while searching for %s: %s", targetName, e.getMessage());
      return null;
    } catch (Exception e) {
      Logger.w(
          TAG, "Failed searching %s for %s: %s", parentChildrenUri, targetName, e.getMessage());
      return null;
    }
    return null;
  }

  @Nullable
  private List<StreamSupplier> singleFileIfExists(@NonNull String fileName) {
    Map<String, Child> kids = children();
    if (kids == null) return null;
    Child c = kids.get(fileName.toLowerCase(Locale.ROOT));
    if (c == null || !c.isFile) return null;
    List<StreamSupplier> out = new ArrayList<>(1);
    out.add(documentSupplier(c));
    return Collections.unmodifiableList(out);
  }

  /**
   * Recognises the BoshiamyTIP <em>character</em> tables, and only those.
   *
   * <p>A folder exported from a Boshiamy install also contains {@code liuext.tab} (extension word
   * list) and {@code liuphon.tab} (phonetic lookup). Those use a different internal layout, so
   * feeding them through the character-table decoder yields millions of junk entries that swamp the
   * radical map and make composing impossible. Match the {@code liu-uni*} family instead of every
   * {@code liu*.tab}, and let {@code liu.box} be handled separately as the plain-text
   * personal-additions file.
   */
  static boolean isLiuCharacterTable(@NonNull String fileName) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    return lower.startsWith("liu-uni") && lower.endsWith(".tab");
  }

  @Nullable
  private List<StreamSupplier> liuBinariesIfPresent() {
    Map<String, Child> kids = children();
    if (kids == null) return null;
    List<Child> matched = new ArrayList<>();
    for (Child c : kids.values()) {
      if (!c.isFile) continue;
      if (isLiuCharacterTable(c.name)) matched.add(c);
    }
    if (matched.isEmpty()) return null;
    // Collections.sort (not List#sort), the latter is API 24+ and minSdk is 23.
    Collections.sort(matched, (a, b) -> a.name.compareTo(b.name));
    List<StreamSupplier> out = new ArrayList<>(matched.size());
    for (Child c : matched) out.add(documentSupplier(c));
    return Collections.unmodifiableList(out);
  }

  @NonNull
  private StreamSupplier documentSupplier(@NonNull Child child) {
    final ContentResolver resolver = mContext.getContentResolver();
    final Uri uri = child.uri;
    final String name = child.name;
    return new StreamSupplier() {
      @NonNull
      @Override
      public InputStream open() throws IOException {
        InputStream raw;
        try {
          raw = resolver.openInputStream(uri);
        } catch (SecurityException e) {
          throw new IOException("Permission lost for " + uri, e);
        } catch (FileNotFoundException e) {
          throw new IOException("Document not found: " + uri, e);
        }
        if (raw == null) {
          throw new IOException("openInputStream returned null for " + uri);
        }
        return raw;
      }

      @NonNull
      @Override
      public String name() {
        return name;
      }
    };
  }

  @Nullable
  @Override
  public List<StreamSupplier> mainTableReplacement() {
    return singleFileIfExists(FileBasedRadicalOverlay.FILE_MAIN);
  }

  @Nullable
  @Override
  public List<StreamSupplier> mainTableOverlay() {
    List<StreamSupplier> overlay = new ArrayList<>();
    List<StreamSupplier> liu = liuBinariesIfPresent();
    if (liu != null) overlay.addAll(liu);
    List<StreamSupplier> liubox = singleFileIfExists(FileBasedRadicalOverlay.FILE_MAIN_OVERLAY);
    if (liubox != null) overlay.addAll(liubox);
    return overlay.isEmpty() ? null : Collections.unmodifiableList(overlay);
  }

  @Nullable
  @Override
  public List<StreamSupplier> phrasesReplacement() {
    return singleFileIfExists(FileBasedRadicalOverlay.FILE_PHRASES);
  }

  @Nullable
  @Override
  public List<StreamSupplier> phrasesOverlay() {
    return singleFileIfExists(FileBasedRadicalOverlay.FILE_PHRASES_OVERLAY);
  }

  @Nullable
  @Override
  public List<StreamSupplier> homophonesReplacement() {
    return singleFileIfExists(FileBasedRadicalOverlay.FILE_HOMOPHONES);
  }

  @Nullable
  @Override
  public List<StreamSupplier> charToZhuyinReplacement() {
    return singleFileIfExists(FileBasedRadicalOverlay.FILE_CHAR_TO_ZHUYIN);
  }

  @Nullable
  @Override
  public List<StreamSupplier> charToRadicalReplacement() {
    return singleFileIfExists(FileBasedRadicalOverlay.FILE_CHAR_TO_RADICAL);
  }

  @Nullable
  @Override
  public List<StreamSupplier> charFrequencyReplacement() {
    return singleFileIfExists(FileBasedRadicalOverlay.FILE_CHAR_FREQUENCY);
  }
}
