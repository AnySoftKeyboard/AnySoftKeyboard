package com.anysoftkeyboard.gesturetyping;

import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;
import android.util.SparseArray;
import com.anysoftkeyboard.dictionaries.Dictionary;
import com.anysoftkeyboard.keyboards.Keyboard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleGestureTypingDetector extends GestureTypingDetector {
    private ArrayList<Pruner> mPruners = new ArrayList<>();
    private final int mGestureLengthPruningThreshold;
    private static final int SAMPLING_POINTS = 300;
    private static final double SHAPE_STD = 22.9;
    private static final double LOCATION_STD = 63; // 0.525;
    private List<HashMap<String, Integer>> mWordFrequenciesMap;

    public SimpleGestureTypingDetector(
            int maxSuggestions,
            int minPointDistance,
            int gestureLengthPruningThreshold,
            @NonNull Iterable<Keyboard.Key> keys) {
        super(maxSuggestions, minPointDistance, keys);
        mGestureLengthPruningThreshold = gestureLengthPruningThreshold;
    }

    @Override
    public void setWords(@NonNull List<char[][]> words, @NonNull List<int[]> wordFrequencies) {
        super.setWords(words, wordFrequencies);

        mWordFrequenciesMap = new ArrayList<>();
        for (int dictIndex = 0; dictIndex < mWords.size(); dictIndex++) {
            final char[][] wordsForDict = mWords.get(dictIndex);
            final int[] frequencies = wordFrequencies.get(dictIndex);
            HashMap<String, Integer> frequenciesForDict = new HashMap<>();
            mWordFrequenciesMap.add(frequenciesForDict);

            for (int i = 0; i < wordsForDict.length; i++) {
                String word = new String(wordsForDict[i]);
                int freq = frequencies[i];
                frequenciesForDict.put(word, freq);
            }

            Pruner pruner =
                    new Pruner(mGestureLengthPruningThreshold, wordsForDict, mKeysByCharacter);
            mPruners.add(pruner);
        }

        mGenerateStateSubject.onNext(LoadingState.LOADED);
    }

    private double calcShapeDistance(Gesture gesture1, Gesture gesture2) {
        double x1;
        double x2;
        double y1;
        double y2;
        double distance;
        double totalDistance = 0;
        for (int i = 0; i < SAMPLING_POINTS; i++) {
            x1 = gesture1.getX(i);
            x2 = gesture2.getX(i);
            y1 = gesture1.getY(i);
            y2 = gesture2.getY(i);

            distance = sqrt(pow((x1 - x2), 2) + pow((y1 - y2), 2));
            totalDistance += distance;
        }
        return totalDistance;
    }

    private double calcLocationDistance(Gesture gesture1, Gesture gesture2) {
        double x1;
        double x2;
        double y1;
        double y2;
        double distance;
        double totalDistance = 0;
        for (int i = 0; i < SAMPLING_POINTS; i++) {
            x1 = gesture1.getX(i);
            x2 = gesture2.getX(i);
            y1 = gesture1.getY(i);
            y2 = gesture2.getY(i);

            distance = sqrt(pow((x1 - x2), 2) + pow((y1 - y2), 2));
            totalDistance += distance;
        }
        return totalDistance / SAMPLING_POINTS;
    }

    private double calcGaussianProbability(double value, double mean, double standardDeviation) {
        double factor = 1. / (standardDeviation * sqrt(2 * 3.14));
        double exponent = pow(((value - mean) / standardDeviation), 2);
        double probability = factor * exp((-1. / 2) * exponent);
        return probability;
    }

    /**
     * Gets an array of candidate words based on the user's gesture by calculating the distance
     * between the gesture and the gestures for the word set. The candidate words are weighed by
     * distance and frequency and sorted from best to worst.
     *
     * @return An array of candidate words sorted from best to worst.
     */
    @Override
    public ArrayList<String> getCandidates() {
        mCandidates.clear();
        mCandidateWeights.clear();

        if (mGenerateStateSubject.getValue() != LoadingState.LOADED) {
            return mCandidates;
        }

        Gesture userGesture = mUserGesture.resample(SAMPLING_POINTS);
        Gesture normalizedUserGesture = userGesture.normalizeByBoxSide();

        for (int dictIndex = 0; dictIndex < mWords.size(); dictIndex++) {
            Pruner pruner = mPruners.get(dictIndex);

            ArrayList<char[]> remainingWords;
            remainingWords = pruner.pruneByExtremities(mUserGesture, mKeys);
            remainingWords = pruner.pruneByLength(mUserGesture, remainingWords, mKeysByCharacter);

            HashMap<String, Integer> wordFrequencies = mWordFrequenciesMap.get(dictIndex);

            double shapeDistance;
            double locationDistance;
            double frequency;
            double locationProbability;
            double shapeProbability;
            double confidence;

            for (int i = 0; i < remainingWords.size(); i++) {
                char[] word = remainingWords.get(i);
                Gesture wordGesture = Gesture.generateIdealGesture(word, mKeysByCharacter);
                wordGesture = wordGesture.resample(SAMPLING_POINTS);

                Gesture normalizedGesture = wordGesture.normalizeByBoxSide();

                shapeDistance = calcShapeDistance(normalizedGesture, normalizedUserGesture);
                locationDistance = calcLocationDistance(wordGesture, userGesture);
                Log.d("GESTURETYPING0", new String(word));
                Log.d("GESTURETYPING1", Double.toString(shapeDistance));
                Log.d("GESTURETYPING2", Double.toString(locationDistance));

                shapeProbability = calcGaussianProbability(shapeDistance, 0, SHAPE_STD);
                locationProbability = calcGaussianProbability(locationDistance, 0, LOCATION_STD);
                Log.d("GESTURETYPING3", Double.toString(shapeProbability));
                Log.d("GESTURETYPING4", Double.toString(locationProbability));

                frequency = wordFrequencies.get(new String(word));

                confidence = 1. / (shapeProbability * locationProbability * frequency);
                Log.d("GESTURETYPING5", Double.toString(confidence));

                int candidateDistanceSortedIndex = 0;
                while (candidateDistanceSortedIndex < mCandidateWeights.size()
                        && mCandidateWeights.get(candidateDistanceSortedIndex) <= confidence) {
                    candidateDistanceSortedIndex++;
                }

                if (candidateDistanceSortedIndex < mMaxSuggestions) {
                    mCandidateWeights.add(candidateDistanceSortedIndex, confidence);
                    mCandidates.add(candidateDistanceSortedIndex, new String(word));
                    if (mCandidateWeights.size() > mMaxSuggestions) {
                        mCandidateWeights.remove(mMaxSuggestions);
                        mCandidates.remove(mMaxSuggestions);
                    }
                }
            }
        }

        Log.d("GESTUREWEIGHTS", mCandidateWeights.toString());

        return mCandidates;
    }

    protected static class Pruner {
        private final HashMap<Pair<Keyboard.Key, Keyboard.Key>, ArrayList<char[]>> mWordTree;
        private final int mLengthThreshold;

        Pruner(int lengthThreshold, char[][] words, SparseArray<Keyboard.Key> keysByCharacter) {
            mLengthThreshold = lengthThreshold;
            mWordTree = new HashMap<>();

            for (char[] word : words) {
                Pair<Keyboard.Key, Keyboard.Key> keyPair =
                        getFirstKeyLastKey(word, keysByCharacter);
                ArrayList<char[]> wordsForPair = mWordTree.get(keyPair);
                if (wordsForPair == null) {
                    wordsForPair = new ArrayList<>();
                    mWordTree.put(keyPair, wordsForPair);
                }
                wordsForPair.add(word);
            }
        }

        private static Pair<Keyboard.Key, Keyboard.Key> getFirstKeyLastKey(
                char[] word, SparseArray<Keyboard.Key> keysByCharacter) {
            char firstLetter = word[0];
            char lastLetter = word[word.length - 1];

            char baseCharacter;
            baseCharacter = Dictionary.toLowerCase(firstLetter);
            Keyboard.Key firstKey = keysByCharacter.get(baseCharacter);
            baseCharacter = Dictionary.toLowerCase(lastLetter);
            Keyboard.Key lastKey = keysByCharacter.get(baseCharacter);

            return new Pair<>(firstKey, lastKey);
        }

        private static Iterable<Keyboard.Key> findNClosestKeys(
                double x, double y, int n, Iterable<Keyboard.Key> keys) {
            HashMap<Keyboard.Key, Double> keyDistances = new HashMap<>();
            for (Keyboard.Key key : keys) {
                double distance = euclideanDistance(key.centerX, key.centerY, x, y);
                keyDistances.put(key, distance);
            }

            // I wish we could use Java8's streams, but we can't: Android does not support that on
            // all devices.
            List<Map.Entry<Keyboard.Key, Double>> nClosestEntries =
                    new ArrayList<>(keyDistances.entrySet());
            Collections.sort(nClosestEntries, (c1, c2) -> c1.getValue().compareTo(c2.getValue()));
            nClosestEntries = nClosestEntries.subList(0, n);

            ArrayList<Keyboard.Key> closestKeys = new ArrayList<>();
            for (Map.Entry<Keyboard.Key, Double> entry : nClosestEntries) {
                Keyboard.Key key = entry.getKey();
                closestKeys.add(key);
            }

            return closestKeys;
        }

        public ArrayList<char[]> pruneByExtremities(
                Gesture userGesture, Iterable<Keyboard.Key> keys) {
            ArrayList<char[]> remainingWords = new ArrayList<>();

            double startX;
            double startY;
            double endX;
            double endY;
            startX = userGesture.getFirstX();
            startY = userGesture.getFirstY();
            endX = userGesture.getLastX();
            endY = userGesture.getLastY();
            Iterable<Keyboard.Key> startKeys = findNClosestKeys(startX, startY, 2, keys);
            Iterable<Keyboard.Key> endKeys = findNClosestKeys(endX, endY, 2, keys);

            for (Keyboard.Key startKey : startKeys) {
                for (Keyboard.Key endKey : endKeys) {
                    Pair<Keyboard.Key, Keyboard.Key> keyPair = new Pair<>(startKey, endKey);
                    ArrayList<char[]> wordsForKeys = mWordTree.get(keyPair);
                    if (wordsForKeys != null) {
                        remainingWords.addAll(wordsForKeys);
                    }
                }
            }

            return remainingWords;
        }

        /**
         * @param userGesture
         * @param words
         * @return
         */
        public ArrayList<char[]> pruneByLength(
                Gesture userGesture,
                ArrayList<char[]> words,
                SparseArray<Keyboard.Key> keysByCharacter) {
            ArrayList<char[]> remainingWords = new ArrayList<>();

            double userLength = userGesture.getLength();
            Gesture idealGesture;
            double wordIdealLength;
            for (char[] word : words) {
                idealGesture = Gesture.generateIdealGesture(word, keysByCharacter);
                wordIdealLength = idealGesture.getLength();
                Log.d("GESTURETYPING21", Double.toString(Math.abs(userLength - wordIdealLength)));
                if (Math.abs(userLength - wordIdealLength) < mLengthThreshold) {
                    remainingWords.add(word);
                }
            }
            return remainingWords;
        }
    }
}
