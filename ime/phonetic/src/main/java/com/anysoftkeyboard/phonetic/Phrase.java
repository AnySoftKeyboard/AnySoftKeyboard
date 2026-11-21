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

/** A phrase placed over {@code span} syllables, with a natural-log score (higher is better). */
public final class Phrase {
  public final String text;
  public final int span;
  public final float score;

  public Phrase(String text, int span, float score) {
    this.text = text;
    this.span = span;
    this.score = score;
  }

  Phrase withScore(float newScore) {
    return new Phrase(text, span, newScore);
  }

  @Override
  public String toString() {
    return text + "(" + span + ", " + score + ")";
  }
}
