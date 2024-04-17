package com.anysoftkeyboard.fileprovider;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.base.Charsets;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class LocalProxyTest {

  private Uri mUri;
  private FileInputStream mInputStream;
  private ContentProviderController<FileProvider> mFileProvider;

  @Before
  public void setup() throws IOException {
    final var appId = ApplicationProvider.getApplicationContext().getPackageName();
    final File tempFile = File.createTempFile("LocalProxyTest", ".png");
    Files.write("testing 123".getBytes(Charsets.UTF8), tempFile);
    mUri = Uri.parse("content://" + appId + "/file.png");
    mInputStream = new FileInputStream(tempFile);
    var shadowContentResolver =
        Shadows.shadowOf(ApplicationProvider.getApplicationContext().getContentResolver());
    shadowContentResolver.registerInputStream(mUri, mInputStream);
    var providerInfo = new ProviderInfo();
    providerInfo.authority = appId;
    providerInfo.grantUriPermissions = true;
    mFileProvider = Robolectric.buildContentProvider(FileProvider.class).create(providerInfo);
  }

  @After
  public void tearDown() throws IOException {
    mInputStream.close();
    mFileProvider.shutdown();
  }

  @Test
  @Config(shadows = ShadowFileProvider.class)
  public void testHappyPathKnownMime() throws IOException {
    var shadowMimeTypeMap = Shadows.shadowOf(MimeTypeMap.getSingleton());
    shadowMimeTypeMap.addExtensionMimeTypeMapping("png", "image/png");
    final var uriSingle = LocalProxy.proxy(ApplicationProvider.getApplicationContext(), mUri);
    final Uri localUri = TestRxSchedulers.blockingGet(uriSingle);

    Assert.assertNotNull(localUri);
    Assert.assertEquals("content", localUri.getScheme());
    Assert.assertEquals("com.anysoftkeyboard.fileprovider.test", localUri.getAuthority());
    Assert.assertTrue(
        localUri.getPath() + " should have a different value!",
        localUri
            .getPath()
            .endsWith("com.anysoftkeyboard.fileprovider.test-dataDir/files/media/file.png.png"));

    File actualFile = new File(localUri.getPath());
    Assert.assertTrue(
        "File " + actualFile.getAbsolutePath() + " does not exist", actualFile.isFile());

    final List<String> copiedData = Files.readLines(actualFile, Charsets.UTF8);
    Assert.assertEquals(1, copiedData.size());
    Assert.assertEquals("testing 123", copiedData.get(0));
  }

  @Test
  @Config(shadows = ShadowFileProvider.class)
  public void testHappyPathUnknownMime() throws IOException {
    final var uriSingle = LocalProxy.proxy(ApplicationProvider.getApplicationContext(), mUri);
    final Uri localUri = TestRxSchedulers.blockingGet(uriSingle);

    Assert.assertNotNull(localUri);
    Assert.assertEquals("content", localUri.getScheme());
    Assert.assertEquals("com.anysoftkeyboard.fileprovider.test", localUri.getAuthority());
    Assert.assertTrue(
        localUri
            .getPath()
            .endsWith("com.anysoftkeyboard.fileprovider.test-dataDir/files/media/file.png"));

    File actualFile = new File(localUri.getPath());
    Assert.assertTrue(
        "File " + actualFile.getAbsolutePath() + " does not exist", actualFile.isFile());

    final List<String> copiedData = Files.readLines(actualFile, Charsets.UTF8);
    Assert.assertEquals(1, copiedData.size());
    Assert.assertEquals("testing 123", copiedData.get(0));
  }

  @Implements(FileProvider.class)
  public static class ShadowFileProvider {
    @Nullable @Implementation
    public static String getType(@NonNull Uri uri) {
      String fileName = uri.getLastPathSegment();
      final int lastDot = fileName.lastIndexOf('.');
      if (lastDot >= 0) {
        final String extension = fileName.substring(lastDot + 1);
        final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mime != null) {
          return mime;
        }
      }

      return "application/octet-stream";
    }

    @Implementation
    public static Uri getUriForFile(Context context, String authority, File file) {
      return Uri.parse(
          String.format(Locale.ROOT, "content://%s%s", authority, file.getAbsolutePath()));
    }
  }
}
