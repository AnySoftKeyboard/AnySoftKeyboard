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

package com.anysoftkeyboard.phonetic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Remembers which phrase the user picked for a (toneless) reading and boosts it in later walks.
 *
 * <p>A bounded LRU map, so it stays tiny. Serialized as {@code reading<TAB>text<TAB>count} lines.
 */
public final class UserModel {
  static final int CAPACITY = 1024;

  /** Large enough that one pick beats the dictionary's ranking in almost every case. */
  static final float PICK_BONUS = 12f;

  private static final int MAX_COUNT = 1000;

  private final Map<String, Integer> mPicks =
      new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
          return size() > CAPACITY;
        }
      };
  private boolean mDirty;

  /** Score bonus for {@code text} spanning {@code readings[from, from + span)}. */
  public float bonus(List<Syllable> readings, int from, int span, String text) {
    if (mPicks.isEmpty()) return 0f;
    final Integer count = mPicks.get(key(readings, from, span, text));
    return count == null ? 0f : PICK_BONUS + Math.min(count, 5);
  }

  public void learn(List<Syllable> readings, int from, int span, String text) {
    final String key = key(readings, from, span, text);
    final Integer count = mPicks.get(key);
    mPicks.put(key, count == null ? 1 : Math.min(MAX_COUNT, count + 1));
    mDirty = true;
  }

  /** Undoes a {@link #learn} (the user took a pick back). */
  public void forget(List<Syllable> readings, int from, int span, String text) {
    final String key = key(readings, from, span, text);
    final Integer count = mPicks.get(key);
    if (count == null) return;
    if (count <= 1) {
      mPicks.remove(key);
    } else {
      mPicks.put(key, count - 1);
    }
    mDirty = true;
  }

  public boolean isDirty() {
    return mDirty;
  }

  public void write(Writer out) throws IOException {
    for (Map.Entry<String, Integer> pick : mPicks.entrySet()) {
      out.write(pick.getKey());
      out.write('\t');
      out.write(Integer.toString(pick.getValue()));
      out.write('\n');
    }
    out.flush();
    mDirty = false;
  }

  /** Reads what {@link #write} wrote; malformed lines are skipped. */
  public static UserModel read(Reader in) throws IOException {
    final UserModel model = new UserModel();
    final BufferedReader reader = new BufferedReader(in);
    String line;
    while ((line = reader.readLine()) != null) {
      final int lastTab = line.lastIndexOf('\t');
      if (lastTab <= 0 || line.indexOf('\t') == lastTab) continue;
      try {
        model.mPicks.put(line.substring(0, lastTab), Integer.parseInt(line.substring(lastTab + 1)));
      } catch (NumberFormatException e) {
        // skip
      }
    }
    return model;
  }

  private static String key(List<Syllable> readings, int from, int span, String text) {
    final StringBuilder sb = new StringBuilder(span * 5 + text.length() + 1);
    for (int i = from; i < from + span; i++) {
      if (i > from) sb.append(',');
      sb.append(readings.get(i).base());
    }
    return sb.append('\t').append(text).toString();
  }
}
