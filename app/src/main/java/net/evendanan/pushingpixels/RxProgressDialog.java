package net.evendanan.pushingpixels;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.R;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.CheckReturnValue;
import io.reactivex.functions.Function;

public class RxProgressDialog {

    @CheckReturnValue
    public static <T> Observable<T> create(@NonNull T data, @NonNull Activity activity, @Nullable CharSequence message) {
        Dialog dialog = new Dialog(activity, com.menny.android.anysoftkeyboard.R.style.ProgressDialog);
        dialog.setContentView(com.menny.android.anysoftkeyboard.R.layout.progress_window);
        if (!TextUtils.isEmpty(message)) {
            TextView messageView = dialog.findViewById(R.id.progress_message);
            messageView.setVisibility(View.VISIBLE);
            messageView.setText(message);
        }
        dialog.setTitle(null);
        dialog.setCancelable(false);
        dialog.setOwnerActivity(activity);
        dialog.show();

        return Observable.using(() -> dialog,
                (Function<Dialog, ObservableSource<T>>) d1 -> Observable.just(data),
                Dialog::dismiss,
                true);
    }

    @CheckReturnValue
    public static <T> Observable<T> create(@NonNull T data, @NonNull Activity activity) {
        return create(data, activity, null);
    }
}
