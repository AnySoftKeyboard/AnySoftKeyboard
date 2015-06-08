package com.anysoftkeyboard.nextword;

import android.content.Context;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.base.utils.Log;

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
        mNextWordsStorageFilename = "next_words.txt";
    }

    @NonNull
    public Iterable<NextWordsContainer> loadStoredNextWords() {
        FileInputStream inputStream = null;
        try {
            inputStream = mContext.openFileInput(mNextWordsStorageFilename);
            final int version = inputStream.read();
            if (version < 1) {
                Log.w(TAG, "Failed to read version from file.");
                return Collections.emptyList();
            }
            final NextWordsFileParser parser;
            switch (version) {
                case 1:
                    parser = new NextWordsFileParserV1();
                    break;
                default:
                    Log.w(TAG, "Version %d is not supported!", version);
                    return Collections.emptyList();
            }
            return parser.loadStoredNextWords(inputStream);
        } catch (FileNotFoundException e) {
            Log.w(TAG, e, "Failed to find %s. Maybe it's just the first time.", mNextWordsStorageFilename);
            return Collections.emptyList();
        } catch (IOException e) {
            Log.w(TAG, e, "Failed to open %s. Maybe it's just the first time.", mNextWordsStorageFilename);
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
            outputStream = mContext.openFileOutput(mNextWordsStorageFilename, Context.MODE_PRIVATE);
            parser.storeNextWords(nextWords, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            Log.w(TAG, e, "Failed to store to %s. Deleting", mNextWordsStorageFilename);
            mContext.deleteFile(mNextWordsStorageFilename);
        } finally {
            if (outputStream != null) try {
                outputStream.close();
            } catch (IOException e) {
            }
        }
    }
}
