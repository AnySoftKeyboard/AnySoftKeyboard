package com.anysoftkeyboard.dictionaries.content;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.dictionaries.BTreeDictionary;
import com.anysoftkeyboard.dictionaries.DictionaryBackgroundLoader;
import com.menny.android.anysoftkeyboard.AnyApplication;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public abstract class ContentObserverDictionary extends BTreeDictionary {

    @NonNull private Disposable mDictionaryChangedLoader = Disposables.empty();
    private ContentObserver mObserver = null;

    protected ContentObserverDictionary(String dictionaryName, Context context) {
        super(dictionaryName, context);
    }

    protected abstract void registerObserver(
            ContentObserver dictionaryContentObserver, ContentResolver contentResolver);

    @Override
    protected void loadAllResources() {
        super.loadAllResources();

        if (!isClosed() && mObserver == null) {
            mObserver = AnyApplication.getDeviceSpecific().createDictionaryContentObserver(this);
            registerObserver(mObserver, mContext.getContentResolver());
        }
    }

    void onStorageChanged() {
        if (isClosed()) return;
        resetDictionary();
        mDictionaryChangedLoader.dispose();
        mDictionaryChangedLoader = DictionaryBackgroundLoader.reloadDictionaryInBackground(this);
    }

    @Override
    protected void closeAllResources() {
        super.closeAllResources();
        mDictionaryChangedLoader.dispose();

        if (mObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
    }
}
