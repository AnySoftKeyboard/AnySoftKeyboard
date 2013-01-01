/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.anysoftkeyboard.spellcheck;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.textservice.SpellCheckerService;
import android.text.TextUtils;
import android.util.Log;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextInfo;

import com.menny.android.anysoftkeyboard.AnyApplication;

/**
 * Service for spell checking, using ASK dictionaries and mechanisms.
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class AnySpellCheckerService extends SpellCheckerService {
	static final String TAG = "ASK_SPELL";
	static final boolean DBG = AnyApplication.DEBUG;
	static final String[] EMPTY_STRING_ARRAY = new String[0];

	
	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public Session createSession() {
		return new AnySpellCheckerSession();
	}

	private static SuggestionsInfo getNotInDictEmptySuggestions() {
		return new SuggestionsInfo(0, EMPTY_STRING_ARRAY);
	}

	private static SuggestionsInfo getInDictEmptySuggestions() {
		return new SuggestionsInfo(
				SuggestionsInfo.RESULT_ATTR_IN_THE_DICTIONARY,
				EMPTY_STRING_ARRAY);
	}

	private static class AnySpellCheckerSession extends Session {

		AnySpellCheckerSession() {
		}

		/**
		 * Finds out whether a particular string should be filtered out of spell
		 * checking.
		 * 
		 * This will loosely match URLs, numbers, symbols.
		 * 
		 * @param text
		 *            the string to evaluate.
		 * @return true if we should filter this text out, false otherwise
		 */
		private static boolean shouldFilterOut(final String text) {
			if (TextUtils.isEmpty(text) || text.length() <= 1)
				return true;

			// TODO: check if an equivalent processing can't be done more
			// quickly with a
			// compiled regexp.
			// Filter by first letter
			final int firstCodePoint = text.codePointAt(0);
			// Filter out words that don't start with a letter or an apostrophe
			if (!Character.isLetter(firstCodePoint) && '\'' != firstCodePoint)
				return true;

			// Filter contents
			final int length = text.length();
			int letterCount = 0;
			for (int i = 0; i < length; ++i) {
				final int codePoint = text.codePointAt(i);
				// Any word containing a '@' is probably an e-mail address
				// Any word containing a '/' is probably either an ad-hoc
				// combination of two
				// words or a URI - in either case we don't want to spell check
				// that
				if ('@' == codePoint || '/' == codePoint)
					return true;
				if (Character.isLetter(codePoint))
					++letterCount;
			}
			// Guestimate heuristic: perform spell checking if at least 3/4 of
			// the characters
			// in this word are letters
			return (letterCount * 4 < length * 3);
		}

		// Note : this must be reentrant
		/**
		 * Gets a list of suggestions for a specific string. This returns a list
		 * of possible corrections for the text passed as an argument. It may
		 * split or group words, and even perform grammatical analysis.
		 */
		@Override
		public SuggestionsInfo onGetSuggestions(final TextInfo textInfo,
				final int suggestionsLimit) {

			try {
				final String text = textInfo.getText();
				final boolean shouldFilterOut = shouldFilterOut(text);

				Log.d(TAG, "onGetSuggestions for '" + text
						+ "'. Should filter out? " + shouldFilterOut);

				if (!shouldFilterOut) {
					//TODO: let's see if the word is in my dictionaries
				}
				return getNotInDictEmptySuggestions();
			} catch (RuntimeException e) {
				Log.e(TAG, "Exception caught in ASK SpellChecker!");
				e.printStackTrace();
				if (DBG)
					throw e;// in production crash the entire stack. Swallow it.

				
				return getNotInDictEmptySuggestions();
			}
		}

		@Override
		public void onCreate() {
		}
	}
}
