package net.evendanan.pixel;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;
import io.reactivex.Observable;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowDialog;

public class GeneralDialogTestUtil {

    public static final AlertDialog NO_DIALOG = Mockito.mock(AlertDialog.class);

    public static AlertDialog getLatestShownDialog() {
        return (AlertDialog)
                Observable.fromIterable(ShadowDialog.getShownDialogs())
                        .filter(dialog -> dialog instanceof AlertDialog)
                        .filter(Dialog::isShowing)
                        .filter(
                                dialog ->
                                        GeneralDialogController.TAG_VALUE.equals(
                                                dialog.getWindow()
                                                        .getDecorView()
                                                        .getTag(GeneralDialogController.TAG_ID)))
                        .last(NO_DIALOG)
                        .blockingGet();
    }

    public static CharSequence getTitleFromDialog(@NonNull Dialog dialog) {
        if (dialog instanceof AlertDialog) {
            return ((TextView) dialog.findViewById(R.id.alertTitle)).getText();
        } else {
            return Shadows.shadowOf(dialog).getTitle();
        }
    }
}
