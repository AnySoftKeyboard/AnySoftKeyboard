package net.evendanan.pushingpixels;

import android.app.Activity;
import android.app.Dialog;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.functions.Function;

public class RxProgressDialog {

    @CheckReturnValue
    public static <T> Observable<T> create(T data, Activity activity) {
        Dialog dialog = new Dialog(activity, com.menny.android.anysoftkeyboard.R.style.ProgressDialog);
        dialog.setContentView(com.menny.android.anysoftkeyboard.R.layout.progress_window);
        dialog.setTitle(null);
        dialog.setCancelable(false);
        dialog.setOwnerActivity(activity);
        dialog.show();

        return Observable.using(() -> dialog,
                (Function<Dialog, ObservableSource<T>>) d1 -> Observable.just(data),
                Dialog::dismiss,
                true);
    }
}
