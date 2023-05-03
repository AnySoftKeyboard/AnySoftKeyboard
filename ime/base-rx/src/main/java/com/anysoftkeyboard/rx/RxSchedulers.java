package com.anysoftkeyboard.rx;

import android.os.Looper;
import androidx.annotation.NonNull;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class RxSchedulers {

  private static Scheduler msBackground;
  private static Scheduler msMainThread;

  static {
    setSchedulers(Looper.getMainLooper(), Schedulers.io());
  }

  private RxSchedulers() {}

  static void setSchedulers(Looper mainLooper, Scheduler background) {
    msBackground = background;
    // https://medium.com/@sweers/rxandroids-new-async-api-4ab5b3ad3e93
    msMainThread = AndroidSchedulers.from(mainLooper, true);
    RxAndroidPlugins.setInitMainThreadSchedulerHandler(callable -> msMainThread);
  }

  @NonNull public static Scheduler mainThread() {
    return msMainThread;
  }

  @NonNull public static Scheduler background() {
    return msBackground;
  }
}
