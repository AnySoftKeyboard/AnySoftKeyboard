package com.anysoftkeyboard.nextword;

import java.util.Comparator;

public class NextWord {
    public final String nextWord;
    private int mUsedCount;

    public NextWord(CharSequence nextWord) {
        this.nextWord = nextWord.toString();
        mUsedCount = 1;
    }

    public NextWord(String nextWord, int usedCount) {
        this.nextWord = nextWord;
        mUsedCount = usedCount;
    }

    public void markAsUsed() {
        mUsedCount++;
    }

    public int getUsedCount() {
        return mUsedCount;
    }

    public static class NextWordComparator implements Comparator<NextWord> {
        @Override
        public int compare(NextWord lhs, NextWord rhs) {
            return lhs.mUsedCount - rhs.mUsedCount;
        }
    }

    @Override
    public String toString() {
        return "["+nextWord+":"+mUsedCount+"]";
    }
}
