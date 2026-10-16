package net.evendanan.pixel;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import android.window.OnBackInvokedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;

public class GeneralDialogController {

  public static final int TAG_ID = R.id.progress_dialog_message_text_view;
  public static final String TAG_VALUE = "GeneralDialogController";

  private final Context mContext;
  private final @StyleRes int mStyle;
  private final DialogPresenter mDialogPresenter;
  private AlertDialog mDialog;
  @Nullable private BackAnimationHelper mBackAnimationHelper;

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
      if (mBackAnimationHelper != null) {
        mBackAnimationHelper.unregister();
        mBackAnimationHelper = null;
      }
      AlertDialog dialog = mDialog;
      mDialog = null;
      dialog.dismiss();
      if (mContext instanceof OnDialogStateChangedListener) {
        ((OnDialogStateChangedListener) mContext).onDialogStateChanged(false);
      }
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
    mDialogPresenter.onSetupDialogRequired(builder, optionId, data);
    mDialog = builder.create();
    mDialog.getWindow().getDecorView().setTag(TAG_ID, TAG_VALUE);
    mDialogPresenter.beforeDialogShown(mDialog, data);
    if (mContext instanceof OnDialogStateChangedListener) {
      ((OnDialogStateChangedListener) mContext).onDialogStateChanged(true);
    }
    mDialog.setOnDismissListener(
        dialog -> {
          if (mDialog == dialog) {
            if (mBackAnimationHelper != null) {
              mBackAnimationHelper.unregister();
              mBackAnimationHelper = null;
            }
            mDialog = null;
            if (mContext instanceof OnDialogStateChangedListener) {
              ((OnDialogStateChangedListener) mContext).onDialogStateChanged(false);
            }
          }
        });
    mDialog.show();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      mBackAnimationHelper = new Api34BackAnimationHelper(mDialog, this::dismiss);
      ((Api34BackAnimationHelper) mBackAnimationHelper).register();
    }
  }

  @VisibleForTesting
  @Nullable
  public Api34BackAnimationHelper getBackAnimationHelper() {
    return (Api34BackAnimationHelper) mBackAnimationHelper;
  }

  public interface OnDialogStateChangedListener {
    void onDialogStateChanged(boolean isShowing);
  }

  public interface DialogPresenter extends JustSetupDialogPresenter {
    void beforeDialogShown(@NonNull AlertDialog dialog, @Nullable Object data);
  }

  public interface JustSetupDialogPresenter {
    void onSetupDialogRequired(AlertDialog.Builder builder, int optionId, @Nullable Object data);
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
        AlertDialog.Builder builder, int optionId, @Nullable Object data) {
      mDialogPresenter.onSetupDialogRequired(builder, optionId, data);
    }
  }

  interface BackAnimationHelper {
    void unregister();
  }

  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public static class Api34BackAnimationHelper implements BackAnimationHelper {
    @NonNull private final AlertDialog mDialog;
    @NonNull private final OnBackAnimationCallback mCallback;

    Api34BackAnimationHelper(@NonNull AlertDialog dialog, @NonNull Runnable dismissRunnable) {
      mDialog = dialog;
      mCallback = new DialogBackAnimationCallback(dialog, dismissRunnable);
    }

    void register() {
      Window window = mDialog.getWindow();
      if (window != null) {
        window
            .getOnBackInvokedDispatcher()
            .registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY, mCallback);
      }
    }

    @Override
    public void unregister() {
      Window window = mDialog.getWindow();
      if (window != null) {
        window.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(mCallback);
        View decorView = window.getDecorView();
        if (decorView != null) {
          decorView.setScaleX(1.0f);
          decorView.setScaleY(1.0f);
          decorView.setAlpha(1.0f);
          decorView.setTranslationX(0f);
        }
      }
    }

    @VisibleForTesting
    @NonNull
    public OnBackAnimationCallback getCallback() {
      return mCallback;
    }

    private static class DialogBackAnimationCallback implements OnBackAnimationCallback {
      private final AlertDialog mDialog;
      private final Runnable mDismissRunnable;

      DialogBackAnimationCallback(@NonNull AlertDialog dialog, @NonNull Runnable dismissRunnable) {
        mDialog = dialog;
        mDismissRunnable = dismissRunnable;
      }

      @Override
      public void onBackStarted(@NonNull BackEvent backEvent) {
        Window window = mDialog.getWindow();
        if (window == null) return;
        View decorView = window.getDecorView();
        if (decorView == null) return;
        decorView.animate().cancel();
        decorView.setPivotX(decorView.getWidth() / 2.0f);
        decorView.setPivotY(decorView.getHeight() / 2.0f);
      }

      @Override
      public void onBackProgressed(@NonNull BackEvent backEvent) {
        Window window = mDialog.getWindow();
        if (window == null) return;
        View decorView = window.getDecorView();
        if (decorView == null) return;
        float progress = backEvent.getProgress();
        float scale = 1.0f - (progress * 0.1f);
        float alpha = 1.0f - (progress * 0.5f);
        int swipeEdge = backEvent.getSwipeEdge();
        float maxTranslation = decorView.getWidth() * 0.05f;
        float translationX =
            (swipeEdge == BackEvent.EDGE_LEFT ? 1f : -1f) * (maxTranslation * progress);

        decorView.setScaleX(scale);
        decorView.setScaleY(scale);
        decorView.setAlpha(alpha);
        decorView.setTranslationX(translationX);
      }

      @Override
      public void onBackCancelled() {
        Window window = mDialog.getWindow();
        if (window == null) return;
        View decorView = window.getDecorView();
        if (decorView == null) return;
        decorView
            .animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .alpha(1.0f)
            .translationX(0f)
            .setDuration(150)
            .start();
      }

      @Override
      public void onBackInvoked() {
        mDismissRunnable.run();
      }
    }
  }
}
