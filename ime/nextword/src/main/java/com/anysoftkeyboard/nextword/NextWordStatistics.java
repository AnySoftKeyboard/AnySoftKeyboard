package com.anysoftkeyboard.nextword;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
public class NextWordStatistics {
    public final int firstWordCount;
    public final int secondWordCount;

    NextWordStatistics(int firstWordCount, int secondWordCount) {
        this.firstWordCount = firstWordCount;
        this.secondWordCount = secondWordCount;
    }
}
