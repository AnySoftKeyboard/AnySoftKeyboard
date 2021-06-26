package com.anysoftkeyboard.ime;

import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class AnySoftKeyboardService extends AnySoftKeyboardBase {

    @Nullable private IBinder mImeToken = null;

    @Nullable
    protected IBinder getImeToken() {
        return mImeToken;
    }

    @NonNull
    @Override
    public AbstractInputMethodImpl onCreateInputMethodInterface() {
        return new InputMethod();
    }

    public class InputMethod extends InputMethodImpl {
        @Override
        public void attachToken(IBinder token) {
            super.attachToken(token);
            mImeToken = token;
        }
    }
}
