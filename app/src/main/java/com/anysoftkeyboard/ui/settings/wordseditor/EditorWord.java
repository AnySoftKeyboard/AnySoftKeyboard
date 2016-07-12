package com.anysoftkeyboard.ui.settings.wordseditor;

import android.support.annotation.NonNull;

/*package*/ class EditorWord {
    @NonNull
    public final String word;
    public final int frequency;

    public EditorWord(@NonNull String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    public static class Editing extends EditorWord {
        public Editing(@NonNull String word, int frequency) {
            super(word, frequency);
        }
    }

    public static class AddNew extends EditorWord {
        public AddNew() {
            super("", -1);
        }
    }
}
