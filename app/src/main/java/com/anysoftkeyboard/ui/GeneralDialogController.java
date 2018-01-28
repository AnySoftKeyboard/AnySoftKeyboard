package com.anysoftkeyboard.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.menny.android.anysoftkeyboard.R;

public class GeneralDialogController {

    private final Context mContext;
    private final DialogPresenter mDialogPresenter;
    private AlertDialog mDialog;

    public GeneralDialogController(Context context, DialogPresenter dialogPresenter) {
        mContext = context;
        mDialogPresenter = dialogPresenter;
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    public void showDialog(int optionId) {
        showDialog(optionId, null);
    }

    public void showDialog(int optionId, @Nullable Object data) {
        dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.Theme_AppCompat_Dialog_Alert);
        mDialogPresenter.onSetupDialogRequired(builder, optionId, data);
        mDialog = builder.create();
        mDialog.show();
    }

    public interface DialogPresenter {
        void onSetupDialogRequired(AlertDialog.Builder builder, int optionId, @Nullable Object data);
    }
}
