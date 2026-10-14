/*
 * Copyright (c) 2013 Menny Even-Danan
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

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A memory-efficient, packed trie structure that maps string keys to ordered candidate lists.
 *
 * <p>Inspired by {@link AutoTextImpl}, this structure packs trie nodes into a contiguous {@code
 * int[]} array and candidate text into a compact {@link String} buffer. This avoids thousands of
 * Java heap objects (e.g. {@code HashMap.Node}, {@code ArrayList}, {@code String}) and eliminates
 * the need for a separate {@code Set<String>} of prefixes, reducing memory footprint by over 90%.
 */
public class CompactTrie {

  private static final int TRIE_C = 0;
  private static final int TRIE_OFF = 1;
  private static final int TRIE_CHILD = 2;
  private static final int TRIE_NEXT = 3;

  private static final int TRIE_SIZEOF = 4;
  private static final int TRIE_NULL = -1;
  private static final int TRIE_ROOT = 0;

  @NonNull private final int[] mTrie;
  @NonNull private final String mText;
  private final int mSize;

  public static final CompactTrie EMPTY = new Builder().build();

  /** Constructs an immutable {@link CompactTrie} from the provided map of entries. */
  @NonNull
  public static CompactTrie fromMap(@NonNull Map<String, List<String>> map) {
    Builder b = new Builder();
    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
      String key = entry.getKey();
      List<String> candidates = entry.getValue();
      if (candidates == null) continue;
      for (String candidate : candidates) {
        b.addCandidate(key, candidate);
      }
    }
    return b.build();
  }

  private CompactTrie(@NonNull int[] trie, @NonNull String text, int size) {
    mTrie = trie;
    mText = text;
    mSize = size;
  }

  /** Returns the number of distinct keys stored in this trie. */
  public int size() {
    return mSize;
  }

  /** Returns true if this trie contains no entries. */
  public boolean isEmpty() {
    return mSize == 0;
  }

  /**
   * Looks up the candidates mapped to the given key.
   *
   * @param key the keystroke or prefix sequence to match
   * @return an unmodifiable list of matching candidates, or an empty list if none match
   */
  @NonNull
  public List<String> get(@NonNull CharSequence key) {
    if (mTrie.length == 0 || key.length() == 0) {
      return Collections.emptyList();
    }

    int here = mTrie[TRIE_ROOT];
    final int length = key.length();

    for (int i = 0; i < length; i++) {
      char c = key.charAt(i);
      boolean found = false;

      for (; here != TRIE_NULL; here = mTrie[here + TRIE_NEXT]) {
        if (c == (char) mTrie[here + TRIE_C]) {
          if (i == length - 1) {
            int off = mTrie[here + TRIE_OFF];
            if (off == TRIE_NULL) {
              return Collections.emptyList();
            }
            int count = mText.charAt(off++);
            List<String> list = new ArrayList<>(count);
            for (int k = 0; k < count; k++) {
              int len = mText.charAt(off++);
              list.add(mText.substring(off, off + len));
              off += len;
            }
            return Collections.unmodifiableList(list);
          }

          here = mTrie[here + TRIE_CHILD];
          found = true;
          break;
        }
      }

      if (!found) {
        return Collections.emptyList();
      }
    }

    return Collections.emptyList();
  }

  /**
   * Returns true if any key in this trie starts with the given prefix.
   *
   * @param prefix the sequence to test
   * @return true if at least one entry has a key starting with {@code prefix}
   */
  public boolean hasPrefix(@NonNull CharSequence prefix) {
    if (prefix.length() == 0) {
      return !isEmpty();
    }
    if (mTrie.length == 0) {
      return false;
    }

    int here = mTrie[TRIE_ROOT];
    final int length = prefix.length();

    for (int i = 0; i < length; i++) {
      char c = prefix.charAt(i);
      boolean found = false;

      for (; here != TRIE_NULL; here = mTrie[here + TRIE_NEXT]) {
        if (c == (char) mTrie[here + TRIE_C]) {
          if (i == length - 1) {
            return true;
          }
          here = mTrie[here + TRIE_CHILD];
          found = true;
          break;
        }
      }

      if (!found) {
        return false;
      }
    }

    return false;
  }

  /**
   * Exports all entries into a new {@link Builder} to permit incremental updates or test mutations.
   */
  @NonNull
  public Builder toBuilder() {
    Builder builder = new Builder();
    if (mTrie.length > 0 && mTrie[TRIE_ROOT] != TRIE_NULL) {
      traverseAndCollect(mTrie[TRIE_ROOT], new StringBuilder(), builder);
    }
    return builder;
  }

  private void traverseAndCollect(int here, StringBuilder key, Builder builder) {
    for (; here != TRIE_NULL; here = mTrie[here + TRIE_NEXT]) {
      char c = (char) mTrie[here + TRIE_C];
      key.append(c);
      int off = mTrie[here + TRIE_OFF];
      if (off != TRIE_NULL) {
        String k = key.toString();
        int count = mText.charAt(off++);
        for (int i = 0; i < count; i++) {
          int len = mText.charAt(off++);
          builder.addCandidate(k, mText.substring(off, off + len));
          off += len;
        }
      }
      int child = mTrie[here + TRIE_CHILD];
      if (child != TRIE_NULL) {
        traverseAndCollect(child, key, builder);
      }
      key.setLength(key.length() - 1);
    }
  }

  /** Builder for constructing an immutable, packed {@link CompactTrie}. */
  public static class Builder {
    private final Map<String, List<String>> mEntries = new HashMap<>();

    public Builder() {}

    /**
     * Appends a candidate for the specified key if it is not already present.
     *
     * @return true if the candidate was added, false if it was already listed
     */
    public boolean addCandidate(@NonNull String key, @NonNull String candidate) {
      List<String> list = mEntries.get(key);
      if (list == null) {
        list = new ArrayList<>(2);
        mEntries.put(key, list);
      }
      if (list.contains(candidate)) {
        return false;
      }
      list.add(candidate);
      return true;
    }

    /** Returns the current candidate list for {@code key}, or null if absent. */
    public List<String> getCandidates(@NonNull String key) {
      return mEntries.get(key);
    }

    /**
     * Sorts candidate lists in-place by frequency rank (lower rank = more common). Absent
     * characters maintain their original relative order at the end.
     */
    public void applyFrequencyOrdering(@NonNull Map<String, Integer> rank) {
      if (rank.isEmpty()) return;
      for (List<String> candidates : mEntries.values()) {
        if (candidates == null || candidates.size() < 2) continue;
        Collections.sort(
            candidates,
            (a, b) -> {
              int rankA = rank.containsKey(a) ? rank.get(a) : Integer.MAX_VALUE;
              int rankB = rank.containsKey(b) ? rank.get(b) : Integer.MAX_VALUE;
              return Integer.compare(rankA, rankB);
            });
      }
    }

    /** Returns the number of distinct keys in this builder. */
    public int size() {
      return mEntries.size();
    }

    /** Packs all entries into an immutable {@link CompactTrie}. */
    @NonNull
    public CompactTrie build() {
      if (mEntries.isEmpty()) {
        return new CompactTrie(new int[] {TRIE_NULL}, "", 0);
      }

      int estimatedNodes = mEntries.size() * 3 + 16;
      int[] trie = new int[estimatedNodes * TRIE_SIZEOF];
      trie[TRIE_ROOT] = TRIE_NULL;
      int trieUsed = TRIE_ROOT + 1;

      StringBuilder text = new StringBuilder(mEntries.size() * 8);

      for (Map.Entry<String, List<String>> entry : mEntries.entrySet()) {
        String key = entry.getKey();
        List<String> candidates = entry.getValue();
        if (candidates == null || candidates.isEmpty()) continue;

        int off = text.length();
        text.append((char) candidates.size());
        for (String c : candidates) {
          text.append((char) c.length());
          text.append(c);
        }

        // Add key and offset into trie
        int slen = key.length();
        int herep = TRIE_ROOT;

        for (int i = 0; i < slen; i++) {
          char c = key.charAt(i);
          boolean found = false;

          for (; trie[herep] != TRIE_NULL; herep = trie[herep] + TRIE_NEXT) {
            if (c == (char) trie[trie[herep] + TRIE_C]) {
              if (i == slen - 1) {
                trie[trie[herep] + TRIE_OFF] = off;
                found = true;
                break;
              }
              herep = trie[herep] + TRIE_CHILD;
              found = true;
              break;
            }
          }

          if (!found) {
            if (trieUsed + TRIE_SIZEOF > trie.length) {
              int[] copy = new int[trie.length * 2];
              System.arraycopy(trie, 0, copy, 0, trie.length);
              trie = copy;
            }

            int node = trieUsed;
            trieUsed += TRIE_SIZEOF;
            trie[herep] = node;

            trie[trie[herep] + TRIE_C] = c;
            trie[trie[herep] + TRIE_OFF] = TRIE_NULL;
            trie[trie[herep] + TRIE_NEXT] = TRIE_NULL;
            trie[trie[herep] + TRIE_CHILD] = TRIE_NULL;

            if (i == slen - 1) {
              trie[trie[herep] + TRIE_OFF] = off;
              break;
            }

            herep = trie[herep] + TRIE_CHILD;
          }
        }
      }

      int[] trimmed = new int[trieUsed];
      System.arraycopy(trie, 0, trimmed, 0, trieUsed);
      return new CompactTrie(trimmed, text.toString(), mEntries.size());
    }
  }
}
