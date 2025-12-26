package com.anysoftkeyboard.chewbacca;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.notification.NotificationDriver;
import com.anysoftkeyboard.notification.NotificationIds;
import com.anysoftkeyboard.notification.NotifyBuilder;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ChewbaccaUncaughtExceptionHandlerTest {

  @Test
  public void testCallsPreviousHandler() {
    final AtomicReference<Pair<Thread, Throwable>> receiver = new AtomicReference<>();
    final Thread.UncaughtExceptionHandler handler = (t, e) -> receiver.set(Pair.create(t, e));

    TestableChewbaccaUncaughtExceptionHandler underTest =
        new TestableChewbaccaUncaughtExceptionHandler(
            ApplicationProvider.getApplicationContext(),
            handler,
            Mockito.mock(NotificationDriver.class));

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
            ApplicationProvider.getApplicationContext(),
            null,
            Mockito.mock(NotificationDriver.class));

    underTest.uncaughtException(Thread.currentThread(), new IOException("an error"));
  }

  @Test
  public void testDoesNotCreateArchivedReportIfNotCrashed() {
    Context app = ApplicationProvider.getApplicationContext();
    var notificationDriver = Mockito.mock(NotificationDriver.class);
    TestableChewbaccaUncaughtExceptionHandler underTest =
        new TestableChewbaccaUncaughtExceptionHandler(app, null, notificationDriver);
    Assert.assertFalse(underTest.performCrashDetectingFlow());
    Assert.assertFalse(new File(app.getFilesDir(), "crashes").exists());
    Mockito.verify(notificationDriver, Mockito.never()).notify(Mockito.any(), Mockito.anyBoolean());
  }

  @Test
  public void testCallsDetectedIfPreviouslyCrashed() throws Exception {
    Context app = ApplicationProvider.getApplicationContext();
    var notificationDriver = Mockito.mock(NotificationDriver.class);
    var notificationBuilder = Mockito.mock(NotifyBuilder.class);
    Mockito.doReturn(notificationBuilder)
        .when(notificationDriver)
        .buildNotification(Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
    Mockito.doReturn(notificationBuilder).when(notificationBuilder).setContentText(Mockito.any());
    Mockito.doReturn(notificationBuilder).when(notificationBuilder).setColor(Mockito.anyInt());
    Mockito.doReturn(notificationBuilder).when(notificationBuilder).setDefaults(Mockito.anyInt());
    Mockito.doReturn(notificationBuilder).when(notificationBuilder).setContentIntent(Mockito.any());
    Mockito.doReturn(notificationBuilder)
        .when(notificationBuilder)
        .setAutoCancel(Mockito.anyBoolean());
    Mockito.doReturn(notificationBuilder)
        .when(notificationBuilder)
        .setOnlyAlertOnce(Mockito.anyBoolean());

    TestableChewbaccaUncaughtExceptionHandler underTest =
        new TestableChewbaccaUncaughtExceptionHandler(app, null, notificationDriver);
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
                ChewbaccaUncaughtExceptionHandler.ACK_CRASH_FILENAME_TEMPLATE.replace(
                    "{TIME}", "\\d+"))
            .matcher(ackFiles[0].getName());
    Assert.assertTrue(ackFiles[0].getName() + " did not match", matcher.find());
    List<String> text = Files.readAllLines(ackFiles[0].toPath());
    Assert.assertEquals(5, text.size());
    for (int lineIndex = 0; lineIndex < reportTextLines.size(); lineIndex++) {
      Assert.assertEquals(
          "line " + lineIndex + " not equals", reportTextLines.get(lineIndex), text.get(lineIndex));
    }

    Mockito.verify(notificationDriver)
        .buildNotification(
            NotificationIds.CrashDetected,
            R.drawable.ic_crash_detected,
            R.string.ime_crashed_title);
    Mockito.verify(notificationDriver).notify(Mockito.notNull(), Mockito.eq(true));

    ArgumentCaptor<PendingIntent> captor = ArgumentCaptor.forClass(PendingIntent.class);
    Mockito.verify(notificationBuilder).setContentIntent(captor.capture());

    Intent savedIntent = Shadows.shadowOf(captor.getValue()).getSavedIntent();
    Assert.assertEquals(
        Intent.FLAG_ACTIVITY_NEW_TASK, savedIntent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
    Assert.assertEquals(Intent.ACTION_VIEW, savedIntent.getAction());
    Assert.assertEquals(Uri.parse("https://example.com"), savedIntent.getData());
    BugReportDetails reportDetails =
        savedIntent.getParcelableExtra(BugReportDetails.EXTRA_KEY_BugReportDetails);
    Assert.assertNotNull(reportDetails);
    Assert.assertEquals(
        reportDetails.crashHeader.trim(), String.join("\n", reportTextLines.subList(0, 2)));
    Assert.assertEquals(reportDetails.crashReportText.trim(), String.join("\n", reportTextLines));
    Assert.assertEquals("file", reportDetails.fullReport.getScheme());
    Assert.assertEquals(Uri.fromFile(ackFiles[0]), reportDetails.fullReport);
  }

  @Test
  public void testCrashLogFileWasCreated() throws Exception {
    Application app = ApplicationProvider.getApplicationContext();
    NotificationDriver notificationDriver = Mockito.mock(NotificationDriver.class);
    TestableChewbaccaUncaughtExceptionHandler underTest =
        new TestableChewbaccaUncaughtExceptionHandler(app, null, notificationDriver);

    underTest.uncaughtException(Thread.currentThread(), new IOException("an error"));

    Mockito.verify(notificationDriver, Mockito.never()).notify(Mockito.any(), Mockito.anyBoolean());

    File newReport =
        new File(app.getFilesDir(), ChewbaccaUncaughtExceptionHandler.NEW_CRASH_FILENAME);
    Assert.assertTrue(newReport.isFile());
    List<String> text = Files.readAllLines(newReport.toPath());
    // Verify the crash report has meaningful content (not just empty or minimal)
    Assert.assertTrue(
        "Crash report should have substantial content (at least 20 lines), but was " + text.size(),
        text.size() >= 20);
    // Verify the header is present
    Assert.assertEquals(
        "Hi. It seems that we have crashed.... Here are some details:", text.get(0));
    // Verify the header break line is present
    Assert.assertEquals(
        ChewbaccaUncaughtExceptionHandler.HEADER_BREAK_LINE,
        text.stream()
            .filter(ChewbaccaUncaughtExceptionHandler.HEADER_BREAK_LINE::equals)
            .findFirst()
            .orElse(null));
    // Verify the exception is mentioned in the report
    Assert.assertTrue(
        "Report should contain exception information",
        text.stream().anyMatch(line -> line.contains("IOException") || line.contains("an error")));
  }

  private static class TestableChewbaccaUncaughtExceptionHandler
      extends ChewbaccaUncaughtExceptionHandler {

    public TestableChewbaccaUncaughtExceptionHandler(
        @NonNull Context app,
        @Nullable Thread.UncaughtExceptionHandler previous,
        @NonNull NotificationDriver driver) {
      super(app, previous, driver);
    }

    @NonNull
    @Override
    protected Intent createBugReportingActivityIntent() {
      return new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"));
    }

    @NonNull
    @Override
    protected String getAppDetails() {
      return "This is the app details in a test";
    }
  }
}
