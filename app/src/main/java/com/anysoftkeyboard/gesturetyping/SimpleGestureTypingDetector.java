package com.anysoftkeyboard.gesturetyping;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.keyboards.Keyboard;

import java.lang.reflect.Array;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimpleGestureTypingDetector extends GestureTypingDetector {
    private ArrayList<Pruner> mPruners;

    public SimpleGestureTypingDetector(
            int maxSuggestions,
            int minPointDistance,
            @NonNull Iterable<Keyboard.Key> keys)
    {
        super(maxSuggestions, minPointDistance, keys);
    }

    private static class Pruner {
        private final HashMap<FirstKeyLastKey, ArrayList<char[]>> wordTree;
        private final int lengthThreshold;

        public Pruner(
                int lengthThreshold,
                char[][] words,
                SparseArray<Keyboard.Key> keysByCharacter)
        {
            this.lengthThreshold = lengthThreshold;
            wordTree = new HashMap<>();

            for (char[] word : words) {
                FirstKeyLastKey keyPair = getFirstKeyLastKey(word,keysByCharacter);
                ArrayList<char[]> wordsForPair = wordTree.get(keyPair);
                if (wordsForPair == null) {
                    wordsForPair = new ArrayList<char[]>();
                    wordTree.put(keyPair, wordsForPair);
                }
                wordsForPair.add(word);
            }
        }

        private static FirstKeyLastKey getFirstKeyLastKey(char[] word, SparseArray<Keyboard.Key> keysByCharacter) {
            char firstLetter = word[0];
            char lastLetter = word[word.length-1];

            char baseCharacter;
            baseCharacter = Dictionary.toLowerCase(firstLetter);
            Keyboard.Key firstKey = keysByCharacter.get(baseCharacter);
            baseCharacter = Dictionary.toLowerCase(lastLetter);
            Keyboard.Key lastKey = keysByCharacter.get(baseCharacter);

            return new FirstKeyLastKey(firstKey, lastKey);
        }

        private static Iterable<Keyboard.Key> findNClosestKeys(int x, int y, int n, Iterable<Keyboard.Key> keys) {
            HashMap<Keyboard.Key, Double> keyDistances = new HashMap<>();
            for (Keyboard.Key key : keys) {
                double distance = euclideanDistance(key.centerX, key.centerY, x, y);
                keyDistances.put(key, distance);
            }

            List<Map.Entry<Keyboard.Key, Double>> nClosestEntries = keyDistances.entrySet()
                    .stream()
                    .sorted(Comparator.comparing(Map.Entry::getValue))
                    .limit(n)
                    .collect(Collectors.toList());

            ArrayList<Keyboard.Key> closestKeys = new ArrayList<>();
            for (Map.Entry entry : nClosestEntries) {
                Keyboard.Key key = (Keyboard.Key) entry.getKey();
                closestKeys.add(key);
            }

            return closestKeys;
        }

        public ArrayList<char[]> pruneByExtremities(
                Gesture userGesture,
                Iterable<Keyboard.Key> keys)
        {
            ArrayList<char[]> remainingWords = new ArrayList<>();

            int startX, startY, endX, endY;
            startX = userGesture.getFirstX();
            startY = userGesture.getFirstY();
            endX = userGesture.getLastX();
            endY = userGesture.getLastY();
            Iterable<Keyboard.Key> startKeys = findNClosestKeys(startX, startY, 2, keys);
            Iterable<Keyboard.Key> endKeys = findNClosestKeys(endX, endY, 2, keys);

            for (Keyboard.Key startKey : startKeys) {
                for (Keyboard.Key endKey : endKeys) {
                    FirstKeyLastKey keyPair = new FirstKeyLastKey(startKey, endKey);
                    ArrayList<char[]> wordsForKeys = wordTree.get(keyPair);
                    if (wordsForKeys != null) {
                        remainingWords.addAll(wordsForKeys);
                    }
                }
            }

            return remainingWords;
        }

        /**
         *
         * @param userGesture
         * @param words
         * @return
         */
        public ArrayList<char[]> pruneByLength(
                Gesture userGesture,
                ArrayList<char[]> words,
                SparseArray<Keyboard.Key> keysByCharacter)
        {
            ArrayList<char[]> remainingWords = new ArrayList<>();

            double userLength = userGesture.getLength();
            Gesture idealGesture;
            double wordIdealLength;
            for (char[] word : words) {
                 idealGesture = Gesture.generateIdealGesture(word, keysByCharacter);
                 wordIdealLength  = idealGesture.getLength();
                 if (Math.abs(userLength - wordIdealLength) < lengthThreshold) {
                     remainingWords.add(word);
                 }
            }
            return remainingWords;
        }

        private static class FirstKeyLastKey {

            private final Keyboard.Key firstKey;
            private final Keyboard.Key lastKey;

            public FirstKeyLastKey(Keyboard.Key firstKey, Keyboard.Key lastKey){
                this.lastKey = lastKey;
                this.firstKey = firstKey;
            }

            public Keyboard.Key getFirstKey() {
                return firstKey;
            }

            public Keyboard.Key getLastKey() {
                return lastKey;
            }

            @Override
            public int hashCode() {
                return Objects.hash(firstKey, lastKey);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                FirstKeyLastKey other = (FirstKeyLastKey) obj;
                return (firstKey == other.getFirstKey() && lastKey == other.getLastKey());
            }
        }

    }
}
