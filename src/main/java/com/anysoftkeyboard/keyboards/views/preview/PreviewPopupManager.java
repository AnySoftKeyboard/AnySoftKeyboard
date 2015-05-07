package com.anysoftkeyboard.keyboards.views.preview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardBaseView;
import com.anysoftkeyboard.utils.CompatUtils;
import com.menny.android.anysoftkeyboard.R;

import java.lang.ref.WeakReference;

public class PreviewPopupManager {

	private PreviewPopupTheme mPreviewPopupTheme = new PreviewPopupTheme();

	private PreviewPopup mPreviewPopup;
	private boolean mEnabled = true;

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
		cancelAllPreviews();
	}

	private static class UIHandler extends Handler {

		private final WeakReference<PreviewPopupManager> mPopupManagerWeakReference;

		private static final int MSG_DISMISS_PREVIEW = R.id.popup_manager_dismiss_preview_message_id;

		public UIHandler(PreviewPopupManager popupManager) {
			mPopupManagerWeakReference = new WeakReference<>(popupManager);
		}

		@Override
		public void handleMessage(Message msg) {
			PreviewPopupManager popupManager = mPopupManagerWeakReference.get();
			if (popupManager == null)
				return;
			switch (msg.what) {
				case MSG_DISMISS_PREVIEW:
					PreviewPopup popup = popupManager.getPopupForKey((Keyboard.Key) msg.obj);
					popup.dismiss();
					break;
			}
		}

		public void cancelAllMessages() {
			removeMessages(MSG_DISMISS_PREVIEW);
		}

		public void dismissPreview(Keyboard.Key key) {
			sendMessageDelayed(obtainMessage(MSG_DISMISS_PREVIEW, key), 10);
		}
	}

	private final Context mContext;
	private final AnyKeyboardBaseView mKeyboardView;
	private final UIHandler mUIHandler;
	public PreviewPopupManager(Context context, AnyKeyboardBaseView keyboardView) {
		mContext = context;
		mKeyboardView = keyboardView;
		mUIHandler = new UIHandler(this);
	}

	public void hidePreviewForKey(Keyboard.Key key) {
		mUIHandler.dismissPreview(key);
	}

	public void showPreviewForKey(Keyboard.Key key, Drawable icon) {
		if (!mEnabled) return;
		PreviewPopup popup = getPopupForKey(key);
		Point previewPosition = PreviewPopupPositionCalculator.calculatePositionForPreview(key, mPreviewPopupTheme, mKeyboardView);
		popup.showPreviewForKey(key, icon, previewPosition);
	}

	public void showPreviewForKey(Keyboard.Key key, CharSequence label) {
		if (!mEnabled) return;
		PreviewPopup popup = getPopupForKey(key);
		Point previewPosition = PreviewPopupPositionCalculator.calculatePositionForPreview(key, mPreviewPopupTheme, mKeyboardView);
		popup.showPreviewForKey(key, label, previewPosition);
	}

	private PreviewPopup getPopupForKey(Keyboard.Key key) {
		if (mPreviewPopup == null) {
			mPreviewPopup = new PreviewPopup(mContext, mKeyboardView, mPreviewPopupTheme);
		}
		return mPreviewPopup;
	}

	public void cancelAllPreviews() {
		mUIHandler.cancelAllMessages();
		if (mPreviewPopup != null) mPreviewPopup.dismiss();
	}

	public PreviewPopupTheme getPreviewPopupTheme() {
		return mPreviewPopupTheme;
	}

	public void resetAllPreviews() {
		cancelAllPreviews();
		CompatUtils.unbindDrawable(mPreviewPopupTheme.getPreviewKeyBackground());
		mPreviewPopup = null;
	}
}
