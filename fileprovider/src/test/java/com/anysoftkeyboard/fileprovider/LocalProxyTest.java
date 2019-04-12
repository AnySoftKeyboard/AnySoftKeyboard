package com.anysoftkeyboard.fileprovider;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.base.Charsets;
import com.google.common.io.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.test.core.app.ApplicationProvider;
import io.reactivex.Single;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class LocalProxyTest {

    private Uri mUri;
    private FileInputStream mInputStream;

    @Before
    public void setup() throws IOException {
        final File tempFile = File.createTempFile("LocalProxyTest", ".txt");
        Files.write("testing 123".getBytes(Charsets.UTF8), tempFile);
        mUri = Uri.parse("content://some.remote.app/file.png");
        mInputStream = new FileInputStream(tempFile);
        Shadows.shadowOf(ApplicationProvider.getApplicationContext().getContentResolver()).registerInputStream(mUri, mInputStream);
    }

    @After
    public void tearDown() throws IOException {
        mInputStream.close();
    }

    @Test
    @Config(shadows = ShadowFileProvider.class)
    public void testHappyPath() throws IOException {
        final Single<Uri> uriSingle = LocalProxy.proxy(ApplicationProvider.getApplicationContext(), mUri);
        final Uri localUri = uriSingle.blockingGet();

        Assert.assertNotNull(localUri);
        Assert.assertEquals("content", localUri.getScheme());
        Assert.assertEquals("com.anysoftkeyboard.fileprovider", localUri.getAuthority());
        Assert.assertTrue(localUri.getPath().endsWith("com.anysoftkeyboard.fileprovider-dataDir/files/media/file.png"));

        File actualFile = new File(localUri.getPath());
        Assert.assertTrue("File " + actualFile.getAbsolutePath() + " does not exist", actualFile.isFile());

        final List<String> copiedData = Files.readLines(actualFile, Charsets.UTF8);
        Assert.assertEquals(1, copiedData.size());
        Assert.assertEquals("testing 123", copiedData.get(0));
    }

    @Implements(FileProvider.class)
    public static class ShadowFileProvider {
        @Implementation
        public static Uri getUriForFile(Context context, String authority, File file) {
            return Uri.parse(String.format(Locale.ROOT, "content://%s%s", authority, file.getAbsolutePath()));
        }
    }
}