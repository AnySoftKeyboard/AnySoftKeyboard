package com.anysoftkeyboard.spellcheck;

import java.util.ArrayList;
import java.util.Collections;

import com.anysoftkeyboard.dictionaries.Dictionary.WordCallback;
import com.anysoftkeyboard.utils.ArraysCompatUtils;
import com.anysoftkeyboard.utils.IMEUtil;

import android.util.Log;

class SuggestionsGatherer implements WordCallback {
    public static class Result {
        public final String[] mSuggestions;
        public final boolean mHasLikelySuggestions;
        public Result(final String[] gatheredSuggestions, final boolean hasLikelySuggestions) {
            mSuggestions = gatheredSuggestions;
            mHasLikelySuggestions = hasLikelySuggestions;
        }
    }

    private final ArrayList<CharSequence> mSuggestions;
    private final int[] mScores;
    private final String mOriginalText;
    private final double mSuggestionThreshold;
    private final double mLikelyThreshold;
    private final int mMaxLength;
    private int mLength = 0;

    // The two following attributes are only ever filled if the requested max length
    // is 0 (or less, which is treated the same).
    private String mBestSuggestion = null;
    private int mBestScore = Integer.MIN_VALUE; // As small as possible

    SuggestionsGatherer(final String originalText, final double suggestionThreshold,
            final double likelyThreshold, final int maxLength) {
        mOriginalText = originalText;
        mSuggestionThreshold = suggestionThreshold;
        mLikelyThreshold = likelyThreshold;
        mMaxLength = maxLength;
        mSuggestions = new ArrayList<CharSequence>(maxLength + 1);
        mScores = new int[mMaxLength];
    }

    @Override
    synchronized public boolean addWord(char[] word, int wordOffset, int wordLength, int frequency) {
        final int positionIndex = ArraysCompatUtils.binarySearch(mScores, 0, mLength, frequency);
        // binarySearch returns the index if the element exists, and -<insertion index> - 1
        // if it doesn't. See documentation for binarySearch.
        final int insertIndex = positionIndex >= 0 ? positionIndex : -positionIndex - 1;

        if (insertIndex == 0 && mLength >= mMaxLength) {
            // In the future, we may want to keep track of the best suggestion score even if
            // we are asked for 0 suggestions. In this case, we can use the following
            // (tested) code to keep it:
            // If the maxLength is 0 (should never be less, but if it is, it's treated as 0)
            // then we need to keep track of the best suggestion in mBestScore and
            // mBestSuggestion. This is so that we know whether the best suggestion makes
            // the score cutoff, since we need to know that to return a meaningful
            // looksLikeTypo.
            // if (0 >= mMaxLength) {
            //     if (score > mBestScore) {
            //         mBestScore = score;
            //         mBestSuggestion = new String(word, wordOffset, wordLength);
            //     }
            // }
            return true;
        }
        if (insertIndex >= mMaxLength) {
            // We found a suggestion, but its score is too weak to be kept considering
            // the suggestion limit.
            return true;
        }

        // Compute the normalized score and skip this word if it's normalized score does not
        // make the threshold.
        final String wordString = new String(word, wordOffset, wordLength);
        final double normalizedScore =
                IMEUtil.calcNormalizedScore(mOriginalText, wordString, frequency);
        if (normalizedScore < mSuggestionThreshold) {
            if (AnySpellCheckerService.DBG) Log.i(AnySpellCheckerService.TAG, wordString + " does not make the score threshold");
            return true;
        }

        if (mLength < mMaxLength) {
            final int copyLen = mLength - insertIndex;
            ++mLength;
            System.arraycopy(mScores, insertIndex, mScores, insertIndex + 1, copyLen);
            mSuggestions.add(insertIndex, wordString);
        } else {
            System.arraycopy(mScores, 1, mScores, 0, insertIndex);
            mSuggestions.add(insertIndex, wordString);
            mSuggestions.remove(0);
        }
        mScores[insertIndex] = frequency;

        return true;
    }

    public SuggestionsGatherer.Result getResults(final int capitalizeType) {
        final String[] gatheredSuggestions;
        final boolean hasLikelySuggestions;
        if (0 == mLength) {
            // Either we found no suggestions, or we found some BUT the max length was 0.
            // If we found some mBestSuggestion will not be null. If it is null, then
            // we found none, regardless of the max length.
            if (null == mBestSuggestion) {
                gatheredSuggestions = null;
                hasLikelySuggestions = false;
            } else {
                gatheredSuggestions = AnySpellCheckerService.EMPTY_STRING_ARRAY;
                final double normalizedScore =
                        IMEUtil.calcNormalizedScore(mOriginalText, mBestSuggestion, mBestScore);
                hasLikelySuggestions = (normalizedScore > mLikelyThreshold);
            }
        } else {
            if (AnySpellCheckerService.DBG) {
                if (mLength != mSuggestions.size()) {
                    Log.e(AnySpellCheckerService.TAG, "Suggestion size is not the same as stored mLength");
                }
                for (int i = mLength - 1; i >= 0; --i) {
                    Log.i(AnySpellCheckerService.TAG, "" + mScores[i] + " " + mSuggestions.get(i));
                }
            }
            Collections.reverse(mSuggestions);
            IMEUtil.removeDupes(mSuggestions);
            /*
            if (CAPITALIZE_ALL == capitalizeType) {
                for (int i = 0; i < mSuggestions.size(); ++i) {
                    // get(i) returns a CharSequence which is actually a String so .toString()
                    // should return the same object.
                    mSuggestions.set(i, mSuggestions.get(i).toString().toUpperCase(locale));
                }
            } else if (CAPITALIZE_FIRST == capitalizeType) {
                for (int i = 0; i < mSuggestions.size(); ++i) {
                    // Likewise
                    mSuggestions.set(i, Utils.toTitleCase(mSuggestions.get(i).toString(),
                            locale));
                }
            }
            */
            // This returns a String[], while toArray() returns an Object[] which cannot be cast
            // into a String[].
            gatheredSuggestions = mSuggestions.toArray(AnySpellCheckerService.EMPTY_STRING_ARRAY);

            final int bestScore = mScores[mLength - 1];
            final CharSequence bestSuggestion = mSuggestions.get(0);
            final double normalizedScore =
                    IMEUtil.calcNormalizedScore(mOriginalText, bestSuggestion, bestScore);
            hasLikelySuggestions = (normalizedScore > mLikelyThreshold);
            if (AnySpellCheckerService.DBG) {
                Log.i(AnySpellCheckerService.TAG, "Best suggestion : " + bestSuggestion + ", score " + bestScore);
                Log.i(AnySpellCheckerService.TAG, "Normalized score = " + normalizedScore
                        + " (threshold " + mLikelyThreshold
                        + ") => hasLikelySuggestions = " + hasLikelySuggestions);
            }
        }
        return new Result(gatheredSuggestions, hasLikelySuggestions);
    }
}