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

/** One entry of the candidate strip. */
public final class Candidate {
  public enum Kind {
    /** The whole composition; picking it commits everything. */
    SENTENCE,
    /** A phrase starting at the anchor. */
    PHRASE,
    /** The anchor syllable's own Bopomofo, to type the symbols themselves. Not learned. */
    RAW,
  }

  public final String text;
  public final Kind kind;
  final Phrase phrase;

  Candidate(String text, Kind kind, Phrase phrase) {
    this.text = text;
    this.kind = kind;
    this.phrase = phrase;
  }

  @Override
  public String toString() {
    return text;
  }
}
