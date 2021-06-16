package com.anysoftkeyboard.rx;

import android.annotation.SuppressLint;
import android.os.Looper;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.robolectric.Shadows;
import org.robolectric.android.util.concurrent.PausedExecutorService;
import org.robolectric.shadows.ShadowLooper;

public class TestRxSchedulers {

    private static PausedExecutorService msBackgroundService;

    public static void setSchedulers(Looper mainThreadLooper, PausedExecutorService background) {
        msBackgroundService = background;
        RxSchedulers.setSchedulers(mainThreadLooper, Schedulers.from(background));
    }

    @SuppressLint("NewApi") // Duration can be used because it is part of the JVM.
    public static boolean foregroundHasMoreJobs() {
        return Shadows.shadowOf(Looper.getMainLooper()).getLastScheduledTaskTime().toMillis() != 0;
    }

    public static void foregroundFlushAllJobs() {
        int maxDrains = 20; // some tasks are re-inserted forever (like animation?)
        while (--maxDrains > 0 && foregroundHasMoreJobs()) {
            foregroundRunOneJob();
        }
        if (maxDrains == 0) {
            // we reached the max-drains.
            // so we'll advance the clock a bit
            foregroundAdvanceBy(1);
        }
    }

    @SuppressLint("NewApi") // Duration can be used because it is part of the JVM.
    public static void foregroundRunOneJob() {
        if (foregroundHasMoreJobs()) {
            final ShadowLooper shadowLooper = Shadows.shadowOf(Looper.getMainLooper());
            final long msToNextTask =
                    shadowLooper.getNextScheduledTaskTime().toMillis() - System.currentTimeMillis();
            if (msToNextTask > 0L) {
                shadowLooper.idleFor(msToNextTask, TimeUnit.MILLISECONDS);
            } else {
                shadowLooper.idle();
            }
        }
    }

    public static void foregroundAdvanceBy(long milliseconds) {
        if (milliseconds == 0) {
            Shadows.shadowOf(Looper.getMainLooper()).idle();
        } else {
            Shadows.shadowOf(Looper.getMainLooper()).idleFor(milliseconds, TimeUnit.MILLISECONDS);
        }
    }

    public static boolean backgroundHasQueuedJobs() {
        return msBackgroundService.hasQueuedTasks();
    }

    public static void backgroundFlushAllJobs() {
        int maxDrains = 20; // some tasks are re-inserted forever (like animation?)
        while (--maxDrains > 0 && backgroundHasQueuedJobs()) {
            msBackgroundService.runAll();
        }
    }

    public static void backgroundRunOneJob() {
        if (backgroundHasQueuedJobs()) msBackgroundService.runNext();
    }

    public static void drainAllTasks() {
        int maxDrains = 20; // some tasks are re-inserted forever (like animation?)
        while (--maxDrains > 0 && (foregroundHasMoreJobs() || backgroundHasQueuedJobs())) {
            foregroundRunOneJob();
            backgroundRunOneJob();
        }
        if (maxDrains == 0) {
            // we reached the max-drains.
            // so we'll advance the clock a bit
            foregroundAdvanceBy(1);
        }
    }

    public static void drainAllTasksUntilEnd() {
        while (foregroundHasMoreJobs() || backgroundHasQueuedJobs()) {
            int maxDrains = 20; // some tasks are re-inserted forever (like animation?)
            while (--maxDrains > 0 && (foregroundHasMoreJobs() || backgroundHasQueuedJobs())) {
                foregroundRunOneJob();
                backgroundRunOneJob();
            }
            if (maxDrains == 0) {
                // we reached the max-drains.
                // so we'll advance the clock a bit
                foregroundAdvanceBy(1);
            }
        }
    }

    public static <T> T blockingGet(Single<T> single) {
        final AtomicReference<T> holder = new AtomicReference<>();
        final Disposable disposable = single.subscribe(holder::set);
        while (holder.get() == null) {
            drainAllTasks();
        }
        disposable.dispose();
        return holder.get();
    }

    public static void destroySchedulers() {
        final PausedExecutorService background = msBackgroundService;
        msBackgroundService = null;
        if (background != null) background.shutdownNow();
        RxSchedulers.mainThread().shutdown();
        RxSchedulers.background().shutdown();
    }
}
