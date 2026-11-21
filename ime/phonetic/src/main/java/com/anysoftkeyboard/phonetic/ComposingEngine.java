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

import java.util.List;

/**
 * A composition-based input method (the IME keeps no state of its own while it is active).
 *
 * <p>After every call the IME syncs three things: it commits {@link #takeCommitText()}, shows
 * {@link #composingText()} with the cursor at {@link #cursorCharOffset()}, and fills the strip with
 * {@link #candidates()}.
 */
public interface ComposingEngine {
  /** Handles a typed code point. Returns false if the engine does not use it. */
  boolean onKey(int codePoint);

  /** Backspace. Returns false when there is nothing to delete (the IME then deletes normally). */
  boolean onDelete();

  /** Moves the cursor by {@code delta} typed symbols. Returns false at the edges. */
  boolean moveCursor(int delta);

  /** Picks a candidate from {@link #candidates()}. */
  void pick(Candidate candidate);

  /**
   * Commits the whole composition as its best conversion, the first candidate (Enter, punctuation,
   * keyboard switch, field change).
   */
  void commitAll();

  /** Drops the composition without committing anything. */
  void reset();

  boolean isComposing();

  /** Text committed by the last call(s); empty if none. Cleared by this call. */
  String takeCommitText();

  String composingText();

  int cursorCharOffset();

  List<Candidate> candidates();
}
