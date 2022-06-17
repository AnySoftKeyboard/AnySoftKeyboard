package com.anysoftkeyboard.chewbacca;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.service.notification.StatusBarNotification;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ChewbaccaUncaughtExceptionHandlerTest {

    @Test
    public void testCallsPreviousHandler() {
        final AtomicReference<Pair<Thread, Throwable>> receiver = new AtomicReference<>();
        final Thread.UncaughtExceptionHandler handler =
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                        receiver.set(Pair.create(t, e));
                    }
                };

        TestableChewbaccaUncaughtExceptionHandler underTest =
                new TestableChewbaccaUncaughtExceptionHandler(
                        ApplicationProvider.getApplicationContext(), handler);

        Thread thread = new Thread();
        IOException exception = new IOException("an error");
        underTest.uncaughtException(thread, exception);

        Assert.assertSame(thread, receiver.get().first);
        Assert.assertSame(exception, receiver.get().second);
    }

    @Test
    public void testDoesNotCrashOnNullPreviousHandler() {
        TestableChewbaccaUncaughtExceptionHandler underTest =
                new TestableChewbaccaUncaughtExceptionHandler(
                        ApplicationProvider.getApplicationContext(), null);

        underTest.uncaughtException(Thread.currentThread(), new IOException("an error"));
    }

    @Test
    public void testNotificationWasSent() {
        Application app = ApplicationProvider.getApplicationContext();
        TestableChewbaccaUncaughtExceptionHandler underTest =
                new TestableChewbaccaUncaughtExceptionHandler(app, null);

        underTest.uncaughtException(Thread.currentThread(), new IOException("an error"));

        StatusBarNotification[] activeNotifications =
                Shadows.shadowOf(app.getSystemService(NotificationManager.class))
                        .getActiveNotifications();
        Notification notification =
                Arrays.stream(activeNotifications)
                        .filter(n -> n.getId() == R.id.notification_icon_app_error)
                        .map(StatusBarNotification::getNotification)
                        .findFirst()
                        .orElse(null);
        Assert.assertNotNull(notification);
        Assert.assertEquals("test-channel-id", notification.getChannelId());
    }

    private static class TestableChewbaccaUncaughtExceptionHandler
            extends ChewbaccaUncaughtExceptionHandler {

        public TestableChewbaccaUncaughtExceptionHandler(
                @NonNull Context app, @Nullable Thread.UncaughtExceptionHandler previous) {
            super(app, previous);
        }

        @NonNull
        @Override
        protected Intent createBugReportingActivityIntent() {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"));
        }

        @Override
        protected void setupNotification(
                @NonNull NotificationCompat.Builder builder, @NonNull Throwable ex) {
            builder.setChannelId("test-channel-id");
        }

        @NonNull
        @Override
        protected String getAppDetails() {
            return "This is the app details in a test";
        }
    }
}
