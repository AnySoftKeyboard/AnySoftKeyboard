package net.evendanan.pixel;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;

public class GeneralDialogController {

    @VisibleForTesting public static final int TAG_ID = R.id.progress_dialog_message_text_view;
    @VisibleForTesting public static final String TAG_VALUE = "GeneralDialogController";

    private final Context mContext;
    private final @StyleRes int mStyle;
    private final DialogPresenter mDialogPresenter;
    private AlertDialog mDialog;

    public GeneralDialogController(
            Context context, @StyleRes int style, JustSetupDialogPresenter dialogPresenter) {
        this(context, style, new NoOpImpl(dialogPresenter));
    }

    public GeneralDialogController(
            Context context, @StyleRes int style, DialogPresenter dialogPresenter) {
        mContext = context;
        mStyle = style;
        mDialogPresenter = dialogPresenter;
    }

    public boolean dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
            return true;
        }

        return false;
    }

    public void showDialog(int optionId) {
        showDialog(optionId, null);
    }

    public void showDialog(int optionId, @Nullable Object data) {
        dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, mStyle);
        mDialogPresenter.onSetupDialogRequired(mContext, builder, optionId, data);
        mDialog = builder.create();
        mDialog.getWindow().getDecorView().setTag(TAG_ID, TAG_VALUE);
        mDialogPresenter.beforeDialogShown(mDialog, data);
        mDialog.show();
    }

    public interface DialogPresenter extends JustSetupDialogPresenter {
        void beforeDialogShown(@NonNull AlertDialog dialog, @Nullable Object data);
    }

    public interface JustSetupDialogPresenter {
        void onSetupDialogRequired(
                Context context, AlertDialog.Builder builder, int optionId, @Nullable Object data);
    }

    private static class NoOpImpl implements DialogPresenter {
        private final JustSetupDialogPresenter mDialogPresenter;

        NoOpImpl(JustSetupDialogPresenter dialogPresenter) {
            mDialogPresenter = dialogPresenter;
        }

        @Override
        public void beforeDialogShown(@NonNull AlertDialog dialog, @Nullable Object data) {}

        @Override
        public void onSetupDialogRequired(
                Context context, AlertDialog.Builder builder, int optionId, @Nullable Object data) {
            mDialogPresenter.onSetupDialogRequired(context, builder, optionId, data);
        }
    }
}
