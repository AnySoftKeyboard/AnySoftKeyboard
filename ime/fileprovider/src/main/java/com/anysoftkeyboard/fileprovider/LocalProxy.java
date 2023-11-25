package com.anysoftkeyboard.fileprovider;

import static androidx.core.content.FileProvider.getUriForFile;

import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.rx.RxSchedulers;
import io.reactivex.Single;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class LocalProxy {
  public static Single<Uri> proxy(@NonNull Context context, @NonNull Uri data) {
    return Single.just(data)
        .subscribeOn(RxSchedulers.background())
        .observeOn(RxSchedulers.mainThread())
        .map(remoteUri -> proxyContentUriToLocalFileUri(context, remoteUri));
  }

  private static Uri proxyContentUriToLocalFileUri(Context context, Uri remoteUri)
      throws IOException {
    try (InputStream remoteInputStream = context.getContentResolver().openInputStream(remoteUri)) {
      final var mimeType = context.getContentResolver().getType(remoteUri);
      final var ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
      Logger.d(
          "ASKLocalProxy",
          "Got mime-type '%s' and ext '%s' for url '%s'",
          mimeType,
          ext,
          remoteUri);

      final File localFilesFolder = new File(context.getFilesDir(), "media");

      if (localFilesFolder.isDirectory() || localFilesFolder.mkdirs()) {
        final File targetFile;
        if (ext == null) {
          targetFile = new File(localFilesFolder, remoteUri.getLastPathSegment());
        } else {
          targetFile =
              new File(
                  localFilesFolder,
                  String.format(Locale.ROOT, "%s.%s", remoteUri.getLastPathSegment(), ext));
        }

        Logger.d("ASKLocalProxy", "Starting to copy media from %s to %s", remoteUri, targetFile);
        byte[] buffer = new byte[4096];
        try (OutputStream outputStream =
            new BufferedOutputStream(new FileOutputStream(targetFile))) {
          int read;
          while ((read = remoteInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
          }
        }

        Logger.d(
            "ASKLocalProxy",
            "Done copying media from %s to %s. Size: %d",
            remoteUri,
            targetFile,
            targetFile.length());
        return getUriForFile(context, context.getPackageName(), targetFile);
      }
    }

    return remoteUri;
  }
}
