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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ChewbaccaUncaughtExceptionHandlerTest {

    @Test
    public void testCallsPreviousHandler() {
        final AtomicReference<Pair<Thread, Throwable>> receiver = new AtomicReference<>();
        final Thread.UncaughtExceptionHandler handler = (t, e) -> receiver.set(Pair.create(t, e));

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
    public void testDoesNotCreateArchivedReportIfNotCrashed() {
        Context app = ApplicationProvider.getApplicationContext();
        TestableChewbaccaUncaughtExceptionHandler underTest =
                new TestableChewbaccaUncaughtExceptionHandler(app, null);
        Assert.assertFalse(underTest.performCrashDetectingFlow());
        Assert.assertFalse(new File(app.getFilesDir(), "crashes").exists());
        Assert.assertEquals(
                0,
                Shadows.shadowOf(app.getSystemService(NotificationManager.class))
                        .getActiveNotifications()
                        .length);
    }

    @Test
    public void testCallsDetectedIfPreviouslyCrashed() throws Exception {
        Context app = ApplicationProvider.getApplicationContext();
        TestableChewbaccaUncaughtExceptionHandler underTest =
                new TestableChewbaccaUncaughtExceptionHandler(app, null);
        File newReport =
                new File(app.getFilesDir(), ChewbaccaUncaughtExceptionHandler.NEW_CRASH_FILENAME);
        List<String> reportTextLines =
                Arrays.asList(
                        "header text",
                        "header 2",
                        ChewbaccaUncaughtExceptionHandler.HEADER_BREAK_LINE,
                        "report text 1",
                        "report text 2");
        Files.write(newReport.toPath(), reportTextLines);
        Assert.assertTrue(newReport.exists());
        Assert.assertTrue(underTest.performCrashDetectingFlow());
        Assert.assertFalse(newReport.exists());
        File[] ackFiles = app.getFilesDir().listFiles();
        Assert.assertEquals(1, ackFiles.length);
        Matcher matcher =
                Pattern.compile(
                                ChewbaccaUncaughtExceptionHandler.ACK_CRASH_FILENAME_TEMPLATE
                                        .replace("{TIME}", "\\d+"))
                        .matcher(ackFiles[0].getName());
        Assert.assertTrue(ackFiles[0].getName() + " did not match", matcher.find());
        List<String> text = Files.readAllLines(ackFiles[0].toPath());
        Assert.assertEquals(5, text.size());
        for (int lineIndex = 0; lineIndex < reportTextLines.size(); lineIndex++) {
            Assert.assertEquals(
                    "line " + lineIndex + " not equals",
                    reportTextLines.get(lineIndex),
                    text.get(lineIndex));
        }

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
        Intent savedIntent = Shadows.shadowOf(notification.contentIntent).getSavedIntent();
        Assert.assertEquals(
                Intent.FLAG_ACTIVITY_NEW_TASK,
                savedIntent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
        Assert.assertEquals(Intent.ACTION_VIEW, savedIntent.getAction());
        Assert.assertEquals(Uri.parse("https://example.com"), savedIntent.getData());
        BugReportDetails reportDetails =
                savedIntent.getParcelableExtra(BugReportDetails.EXTRA_KEY_BugReportDetails);
        Assert.assertNotNull(reportDetails);
        Assert.assertEquals(
                reportDetails.crashHeader.trim(), String.join("\n", reportTextLines.subList(0, 2)));
        Assert.assertEquals(
                reportDetails.crashReportText.trim(), String.join("\n", reportTextLines));
        Assert.assertEquals("file", reportDetails.fullReport.getScheme());
        Assert.assertEquals(Uri.fromFile(ackFiles[0]), reportDetails.fullReport);
    }

    @Test
    public void testCrashLogFileWasCreated() throws Exception {
        Application app = ApplicationProvider.getApplicationContext();
        TestableChewbaccaUncaughtExceptionHandler underTest =
                new TestableChewbaccaUncaughtExceptionHandler(app, null);

        underTest.uncaughtException(Thread.currentThread(), new IOException("an error"));

        Assert.assertEquals(
                0,
                Shadows.shadowOf(app.getSystemService(NotificationManager.class))
                        .getActiveNotifications()
                        .length);

        File newReport =
                new File(app.getFilesDir(), ChewbaccaUncaughtExceptionHandler.NEW_CRASH_FILENAME);
        Assert.assertTrue(newReport.isFile());
        List<String> text = Files.readAllLines(newReport.toPath());
        Assert.assertEquals(
                44 /*this is fragile, and can change when crash report is changed*/, text.size());
        Assert.assertEquals(
                "Hi. It seems that we have crashed.... Here are some details:", text.get(0));
        Assert.assertEquals(
                ChewbaccaUncaughtExceptionHandler.HEADER_BREAK_LINE,
                text.stream()
                        .filter(ChewbaccaUncaughtExceptionHandler.HEADER_BREAK_LINE::equals)
                        .findFirst()
                        .orElse(null));
    }

    private static class TestableChewbaccaUncaughtExceptionHandler
            extends ChewbaccaUncaughtExceptionHandler {

        public TestableChewbaccaUncaughtExceptionHandler(
                @NonNull Context app, @Nullable Thread.UncaughtExceptionHandler previous) {
            super(app, previous);
        }

        @NonNull @Override
        protected Intent createBugReportingActivityIntent() {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"));
        }

        @Override
        protected void setupNotification(@NonNull NotificationCompat.Builder builder) {
            builder.setChannelId("test-channel-id");
        }

        @NonNull @Override
        protected String getAppDetails() {
            return "This is the app details in a test";
        }
    }
}
