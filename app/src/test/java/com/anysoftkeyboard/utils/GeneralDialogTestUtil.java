package com.anysoftkeyboard.utils;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import net.evendanan.pixel.GeneralDialogController;

import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowDialog;

import io.reactivex.Observable;

public class GeneralDialogTestUtil {

    public static final AlertDialog NO_DIALOG = Mockito.mock(AlertDialog.class);

    public static AlertDialog getLatestShownDialog() {
        return (AlertDialog) Observable.fromIterable(ShadowDialog.getShownDialogs())
                .filter(dialog -> dialog instanceof AlertDialog)
                .filter(Dialog::isShowing)
                .filter(dialog -> GeneralDialogController.TAG_VALUE.equals(dialog.getWindow().getDecorView().getTag(GeneralDialogController.TAG_ID)))
                .last(NO_DIALOG)
                .blockingGet();
    }

    public static CharSequence getTitleFromDialog(@NonNull Dialog dialog) {
        if (dialog instanceof AlertDialog) {
            return ((TextView) dialog.findViewById(net.evendanan.pixel.R.id.alertTitle)).getText();
        } else {
            return Shadows.shadowOf(dialog).getTitle();
        }
    }
}