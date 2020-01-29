package com.anysoftkeyboard.rx;

import android.os.Looper;
import android.support.annotation.VisibleForTesting;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Executor;

public class TestRxSchedulers {

    @VisibleForTesting
    public static void setSchedulers(Looper mainThreadLooper, Executor background) {
        RxSchedulers.msBackground = Schedulers.from(background);
        RxSchedulers.msMainThread = AndroidSchedulers.from(mainThreadLooper);
    }
}
