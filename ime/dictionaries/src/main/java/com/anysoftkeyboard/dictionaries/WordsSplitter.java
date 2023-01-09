package com.anysoftkeyboard.dictionaries;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.api.KeyCodes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class WordsSplitter {
    public static final int MAX_SPLITS = 5;
    private final Queue<WrappingKeysProvider> mPool;
    private final int[] mSplitIndices;

    public WordsSplitter() {
        mSplitIndices = new int[MAX_SPLITS];
        final int maxPermutations = 1 << MAX_SPLITS;
        mPool = new ArrayDeque<>(maxPermutations);
        for (int itemIndex = 0; itemIndex < maxPermutations; itemIndex++) {
            mPool.add(new WrappingKeysProvider());
        }
    }

    /**
     * Returns a list of possible splits that can be constructed from the typed word if a key was
     * SPACE rather and a letter.
     */
    public Iterable<Iterable<KeyCodesProvider>> split(KeyCodesProvider typedKeyCodes) {
        // optimization: splits can only happen if there are enough characters in the input
        if (typedKeyCodes.codePointCount() < 2) return Collections.emptyList();
        /*
         * What are we doing here:
         * 1) going over all the typed codes and marking indices of possible SPACE keys. O(N)
         *    count of permutation (Pc) is 2 in the port of the indices count (Ic).
         * 3) we iterate from 0 to count of permutations (Pc) as Pi
         *   3.1) for each Pi we iterate from 0 to Ic (indices count)
         *        note: each bit in Pi corresponds to a cell in possible indices array
         *   3.2) given a Pi, we pick a split index if the bit is 1.
         *   3.3) we create splits from the original word, as a list
         *
         * for example. Given the codes [a, b, c, d, e, f, g, h],
         * and given [c, f, g] are close to SPACE.
         * We create an indices array: [2, 5, 6].
         * This means we have 8 permutations (2 in the power of 3).
         * We iterate from 0..8: 000, 001, 010, 011, 100, 101, 110, 111
         * for each we pick indices from [2, 5, 6]:
         *    000 -> []8
         *    100 -> [2]8
         *    010 -> [5]8
         *    110 -> [2, 5]8
         *    001 -> [6]8
         *    101 -> [2, 6]8
         *    011 -> [5, 6]8
         *    111 -> [2, 5, 6]8
         * and then create splits from [a, b, c, d, e, f, g, h]:
         *    [0, 8] -> [[a, b, c, d, e, f, g, h]]
         *    [0, 2, 8] -> [[a, b], [d, e, f, g, h]]
         *    [0, 5, 8] -> [[a, b, c, d, e], [g, h]]
         *    [0, 2, 5, 8] -> [[a, b], [d, e], [g, h]]
         *    [0, 6, 8] -> [[a, b, c, d, e, f], [h]]
         *    [0, 2, 6, 8] -> [[a, b], [d, e, f] , [h]]
         *    [0, 5, 6, 8] -> [[a, b, c, d, e], [], [h]]
         *    [0, 2, 5, 6, 8] -> [[a, b] , [d, e], [], [h]]
         */

        int splitsCount = 0;
        for (int keyIndex = 0;
                keyIndex < typedKeyCodes.codePointCount() && splitsCount < MAX_SPLITS;
                keyIndex++) {
            final int[] nearByCodes = typedKeyCodes.getCodesAt(keyIndex);
            /*the first key is NEVER a possible space*/
            if (keyIndex != 0 && hasSpaceInCodes(nearByCodes)) {
                mSplitIndices[splitsCount] = keyIndex;
                splitsCount++;
            }
        }
        // optimization: no splits, we do not report anything
        if (splitsCount == 0) return Collections.emptyList();

        List<List<KeyCodesProvider>> possibilities = new ArrayList<>();
        // iterating over the permutations
        final int permutationsCount = 1 << splitsCount;
        for (int permutationIndex = 0; permutationIndex < permutationsCount; permutationIndex++) {
            // mapping to split indices
            List<KeyCodesProvider> splits = new ArrayList<>();
            possibilities.add(splits);
            int splitStart = 0;
            for (int pickIndex = 0; pickIndex < splitsCount; pickIndex++) {
                if (((1 << pickIndex) & permutationIndex) != 0) {
                    final int splitEnd = mSplitIndices[pickIndex];
                    addSplitToList(typedKeyCodes, splitStart, splitEnd, splits);
                    splitStart = splitEnd + 1;
                }
            }
            // and last split
            addSplitToList(typedKeyCodes, splitStart, typedKeyCodes.codePointCount(), splits);
        }

        return new Result(possibilities);
    }

    private void addSplitToList(
            KeyCodesProvider typedKeyCodes,
            int splitStart,
            int splitEnd,
            List<KeyCodesProvider> splits) {
        if (splitStart == splitEnd) return;

        // creating split
        WrappingKeysProvider provider = mPool.remove();
        provider.wrap(typedKeyCodes, splitStart, splitEnd);
        splits.add(provider);
        // back to the queue.
        mPool.add(provider);
    }

    private static boolean hasSpaceInCodes(int[] nearByCodes) {
        if (nearByCodes.length > 0) {
            // assuming the keycode at the end is SPACE.
            // see
            // com.anysoftkeyboard.keyboards.views.ProximityKeyDetector.getKeyIndexAndNearbyCodes
            return nearByCodes[nearByCodes.length - 1] == KeyCodes.SPACE;
        }
        return false;
    }

    private static class Result implements Iterable<Iterable<KeyCodesProvider>> {

        private final List<List<KeyCodesProvider>> mPossibilities;

        public Result(List<List<KeyCodesProvider>> possibilities) {
            mPossibilities = possibilities;
        }

        @NonNull
        @Override
        public Iterator<Iterable<KeyCodesProvider>> iterator() {
            final var actual = mPossibilities.iterator();
            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return actual.hasNext();
                }

                @Override
                public Iterable<KeyCodesProvider> next() {
                    return actual.next();
                }
            };
        }
    }

    @VisibleForTesting
    static class WrappingKeysProvider implements KeyCodesProvider {
        private KeyCodesProvider mOriginal;
        private int mOffset;
        private int mLength;

        @Override
        public int codePointCount() {
            return mLength;
        }

        @Override
        public int[] getCodesAt(int index) {
            return mOriginal.getCodesAt(mOffset + index);
        }

        @Override
        public CharSequence getTypedWord() {
            return mOriginal.getTypedWord().subSequence(mOffset, mLength);
        }

        public void wrap(KeyCodesProvider original, int offset, int length) {
            mOriginal = original;
            mOffset = offset;
            mLength = length;
        }
    }
}
