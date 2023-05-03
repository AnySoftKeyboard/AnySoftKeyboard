package net.evendanan.pixel;

import android.app.Dialog;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import io.reactivex.Observable;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowDialog;

public class GeneralDialogTestUtil {

  public static final AlertDialog NO_DIALOG = Mockito.mock(AlertDialog.class);

  public static AlertDialog getLatestShownDialog() {
    return (AlertDialog)
        TestRxSchedulers.blockingGet(
            Observable.fromIterable(ShadowDialog.getShownDialogs())
                .filter(dialog -> dialog instanceof AlertDialog)
                .filter(Dialog::isShowing)
                .filter(
                    dialog ->
                        GeneralDialogController.TAG_VALUE.equals(
                            dialog
                                .getWindow()
                                .getDecorView()
                                .getTag(GeneralDialogController.TAG_ID)))
                .last(NO_DIALOG));
  }

  public static CharSequence getTitleFromDialog(@NonNull Dialog dialog) {
    if (dialog instanceof AlertDialog) {
      return ((TextView) dialog.findViewById(R.id.alertTitle)).getText();
    } else {
      return Shadows.shadowOf(dialog).getTitle();
    }
  }
}
