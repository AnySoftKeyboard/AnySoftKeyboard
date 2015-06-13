package com.anysoftkeyboard.nextword;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

public class NextWordsStorage {

    private static final String TAG = "NextWordsStorage";
    private final Context mContext;
    private final String mLocale;
    private final String mNextWordsStorageFilename;

    public NextWordsStorage(@NonNull Context context, @NonNull String locale) {
        mContext = context;
        mLocale = locale;
        mNextWordsStorageFilename = "next_words_"+mLocale+".txt";
    }

    @NonNull
    public Iterable<NextWordsContainer> loadStoredNextWords() {
        FileInputStream inputStream = null;
        try {
            if (Utils.DEBUG) Log.d(TAG, "Loading words from "+mNextWordsStorageFilename);
            inputStream = mContext.openFileInput(mNextWordsStorageFilename);
            final int version = inputStream.read();
            if (version < 1) {
                Log.w(TAG, "Failed to read version from file "+mNextWordsStorageFilename);
                return Collections.emptyList();
            }
            final NextWordsFileParser parser;
            switch (version) {
                case 1:
                    parser = new NextWordsFileParserV1();
                    break;
                default:
                    Log.w(TAG, String.format("Version %d is not supported!", version));
                    return Collections.emptyList();
            }
            return parser.loadStoredNextWords(inputStream);
        } catch (FileNotFoundException e) {
            Log.w(TAG, e);
            Log.w(TAG, String.format("Failed to find %s. Maybe it's just the first time.", mNextWordsStorageFilename));
            return Collections.emptyList();
        } catch (IOException e) {
            Log.w(TAG, e);
            Log.w(TAG, String.format("Failed to open %s. Maybe it's just the first time.", mNextWordsStorageFilename));
            return Collections.emptyList();
        } finally {
            if (inputStream != null) try {
                inputStream.close();
            } catch (IOException e) {
            }
        }
    }

    public void storeNextWords(@NonNull Iterable<NextWordsContainer> nextWords) {
        NextWordsFileParser parser = new NextWordsFileParserV1();
        FileOutputStream outputStream = null;
        try {
            Log.d(TAG, "Storing next-words into "+mNextWordsStorageFilename);
            outputStream = mContext.openFileOutput(mNextWordsStorageFilename, Context.MODE_PRIVATE);
            parser.storeNextWords(nextWords, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            Log.w(TAG, e);
            Log.w(TAG, String.format("Failed to store to %s. Deleting", mNextWordsStorageFilename));
            mContext.deleteFile(mNextWordsStorageFilename);
        } finally {
            if (outputStream != null) try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
    }
}
