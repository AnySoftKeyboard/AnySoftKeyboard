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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.anysoftkeyboard.AskPrefs;
import com.anysoftkeyboard.base.utils.GCUtils;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.keyboards.views.OnKeyboardActionListener;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.BuildConfig;
import com.menny.android.anysoftkeyboard.R;

public abstract class AnySoftKeyboardBase
        extends InputMethodService
        implements OnKeyboardActionListener, SharedPreferences.OnSharedPreferenceChangeListener {
    protected final static String TAG = "ASK";
    protected final AskPrefs mAskPrefs;

    private SharedPreferences mPrefs;

    private KeyboardViewContainerView mInputViewContainer;
    private InputViewBinder mInputView;

    private AlertDialog mOptionsDialog;

    private InputMethodManager mInputMethodManager;

    protected Suggest mSuggest;

    public AnySoftKeyboardBase() {
        mAskPrefs = AnyApplication.getConfig();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if ((!BuildConfig.DEBUG) && DeveloperUtils.hasTracingRequested(getApplicationContext())) {
            try {
                DeveloperUtils.startTracing();
                Toast.makeText(getApplicationContext(), R.string.debug_tracing_starting, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                //see issue https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/105
                //I might get a "Permission denied" error.
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.debug_tracing_starting_failed, Toast.LENGTH_LONG).show();
            }
        }
        Logger.i(TAG, "****** AnySoftKeyboard v%s (%d) service started.", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mSuggest = createSuggest();
    }

    protected SharedPreferences getSharedPrefs() {
        return mPrefs;
    }

    public InputViewBinder getInputView() {
        return mInputView;
    }

    public ViewGroup getInputViewContainer() {
        return mInputViewContainer;
    }

    protected abstract String getSettingsInputMethodId();

    protected InputMethodManager getInputMethodManager() {
        return mInputMethodManager;
    }

    @Override
    public void onComputeInsets(@NonNull Insets outInsets) {
        super.onComputeInsets(outInsets);
        if (!isFullscreenMode()) {
            outInsets.contentTopInsets = outInsets.visibleTopInsets;
        }
    }

    protected void showToastMessage(@StringRes int resId, boolean forShortTime) {
        showToastMessage(getResources().getText(resId), forShortTime);
    }

    protected void showToastMessage(CharSequence text, boolean forShortTime) {
        int duration = forShortTime ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG;
        Toast.makeText(this.getApplication(), text, duration).show();
    }

    protected void showOptionsDialogWithData(CharSequence title, @DrawableRes int iconRedId,
                                             final CharSequence[] entries, final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setIcon(iconRedId);
        builder.setTitle(title);
        builder.setNegativeButton(android.R.string.cancel, null);

        builder.setItems(entries, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int position) {
                di.dismiss();
                if (di == mOptionsDialog) mOptionsDialog = null;

                if ((position < 0) || (position >= entries.length)) {
                    Logger.d(TAG, "Selection dialog popup canceled");
                } else {
                    Logger.d(TAG, "User selected '%s' at position %d", entries[position], position);
                    listener.onClick(di, position);
                }
            }
        });

        if (mOptionsDialog != null && mOptionsDialog.isShowing()) mOptionsDialog.dismiss();
        mOptionsDialog = builder.create();
        Window window = mOptionsDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = ((View) getInputView()).getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mOptionsDialog.show();
    }

    @Override
    public View onCreateInputView() {
        if (getInputView() != null) getInputView().onViewNotRequired();
        mInputView = null;

        GCUtils.getInstance().performOperationWithMemRetry(TAG,
                new GCUtils.MemRelatedOperation() {
                    @SuppressLint("InflateParams")
                    public void operation() {
                        mInputViewContainer = createInputViewContainer();
                        mInputViewContainer.setBackgroundResource(R.drawable.ask_wallpaper);
                    }
                }, true);
        // resetting token users
        mOptionsDialog = null;

        mInputView = mInputViewContainer.getStandardKeyboardView();
        mInputViewContainer.setOnKeyboardActionListener(this);
        return mInputViewContainer;
    }

    protected KeyboardViewContainerView createInputViewContainer() {
        return (KeyboardViewContainerView) getLayoutInflater().inflate(R.layout.main_keyboard_layout, null);
    }

    @Override
    public void hideWindow() {
        super.hideWindow();
        if (mOptionsDialog != null && mOptionsDialog.isShowing()) {
            mOptionsDialog.dismiss();
            mOptionsDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        if (getInputView() != null) getInputView().onViewNotRequired();
        mInputView = null;

        super.onDestroy();
    }

    @NonNull
    protected Suggest createSuggest() {
        return new Suggest(this);
    }

    protected abstract boolean isAlphabet(int code);

    protected abstract boolean isSuggestionAffectingCharacter(int code);

    @CallSuper
    protected void onLoadSettingsRequired(SharedPreferences sharedPreferences) {

    }

    @CallSuper
    protected void abortCorrectionAndResetPredictionState(boolean forever) {
        mSuggest.resetNextWordSentence();
    }
}
