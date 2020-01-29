/*
 * Copyright (c) 2016 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ime;

import android.content.DialogInterface;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.anysoftkeyboard.base.utils.Logger;
import net.evendanan.pixel.GeneralDialogController;

public abstract class AnySoftKeyboardDialogProvider extends AnySoftKeyboardService {

    private static final int OPTIONS_DIALOG = 123123;
    private GeneralDialogController mGeneralDialogController;
    private GeneralDialogController.DialogPresenter mDialogPresenter;

    @Override
    public void onCreate() {
        super.onCreate();
        mDialogPresenter = new ImeDialogPresenter();
        mGeneralDialogController = new GeneralDialogController(this, mDialogPresenter);
    }

    protected void showToastMessage(@StringRes int resId, boolean forShortTime) {
        showToastMessage(getResources().getText(resId), forShortTime);
    }

    protected void showToastMessage(CharSequence text, boolean forShortTime) {
        int duration = forShortTime ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
        Toast.makeText(this.getApplication(), text, duration).show();
    }

    protected void showOptionsDialogWithData(
            @StringRes int title,
            @DrawableRes int iconRedId,
            final CharSequence[] entries,
            final DialogInterface.OnClickListener listener) {
        showOptionsDialogWithData(getText(title), iconRedId, entries, listener);
    }

    protected void showOptionsDialogWithData(
            CharSequence title,
            @DrawableRes int iconRedId,
            final CharSequence[] entries,
            final DialogInterface.OnClickListener listener) {
        showOptionsDialogWithData(title, iconRedId, entries, listener, null);
    }

    protected void showOptionsDialogWithData(
            CharSequence title,
            @DrawableRes int iconRedId,
            final CharSequence[] entries,
            final DialogInterface.OnClickListener listener,
            @Nullable GeneralDialogController.DialogPresenter extraPresenter) {
        mGeneralDialogController.showDialog(
                OPTIONS_DIALOG,
                new OptionsDialogData(title, iconRedId, entries, listener, extraPresenter));
    }

    @Override
    public View onCreateInputView() {
        // resetting UI token
        mGeneralDialogController.dismiss();

        return super.onCreateInputView();
    }

    @CallSuper
    @Override
    protected boolean handleCloseRequest() {
        if (mGeneralDialogController.dismiss()) {
            return true;
        } else {
            return super.handleCloseRequest();
        }
    }

    protected class OptionsDialogData {
        private final CharSequence mTitle;
        @DrawableRes private final int mIcon;
        private final CharSequence[] mOptions;
        private final DialogInterface.OnClickListener mOnClickListener;
        @Nullable private final GeneralDialogController.DialogPresenter mExtraPresenter;

        public OptionsDialogData(
                CharSequence title,
                int icon,
                CharSequence[] options,
                DialogInterface.OnClickListener onClickListener,
                @Nullable GeneralDialogController.DialogPresenter extraPresenter) {
            mTitle = title;
            mIcon = icon;
            mOptions = options;
            mOnClickListener = onClickListener;
            mExtraPresenter = extraPresenter;
        }

        public void dialogOptionHandler(DialogInterface dialog, int which) {
            mGeneralDialogController.dismiss();

            if ((which < 0) || (which >= mOptions.length)) {
                Logger.d(TAG, "Selection dialog popup canceled");
            } else {
                Logger.d(TAG, "User selected '%s' at position %d", mOptions[which], which);
                mOnClickListener.onClick(dialog, which);
            }
        }
    }

    private class ImeDialogPresenter implements GeneralDialogController.DialogPresenter {
        @Override
        public void onSetupDialogRequired(
                AlertDialog.Builder builder, int optionId, @Nullable Object data) {
            OptionsDialogData dialogData = (OptionsDialogData) data;
            builder.setCancelable(true);
            builder.setIcon(dialogData.mIcon);
            builder.setTitle(dialogData.mTitle);
            builder.setNegativeButton(android.R.string.cancel, null);

            builder.setItems(dialogData.mOptions, dialogData::dialogOptionHandler);

            getInputView().resetInputView();

            if (dialogData.mExtraPresenter != null) {
                dialogData.mExtraPresenter.onSetupDialogRequired(builder, optionId, data);
            }
        }

        @Override
        public void beforeDialogShown(@NonNull AlertDialog dialog, @Nullable Object data) {
            OptionsDialogData dialogData = (OptionsDialogData) data;
            Window window = dialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.token = ((View) getInputView()).getWindowToken();
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            if (dialogData.mExtraPresenter != null) {
                dialogData.mExtraPresenter.beforeDialogShown(dialog, data);
            }
        }
    }
}
