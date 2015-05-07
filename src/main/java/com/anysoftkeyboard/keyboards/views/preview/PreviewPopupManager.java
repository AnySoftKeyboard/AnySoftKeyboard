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

import junit.framework.Assert;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class PreviewPopupManager {

	private final int mMaxPopupInstances;
	private PreviewPopupTheme mPreviewPopupTheme = new PreviewPopupTheme();

	private final Queue<PreviewPopup> mPreviewPopupsQueue = new LinkedList<>();
	private final Map<Keyboard.Key, PreviewPopup> mActivePopups = new HashMap<>();
	private final Context mContext;
	private final AnyKeyboardBaseView mKeyboardView;
	private final UIHandler mUIHandler;

	private boolean mEnabled = true;

	public PreviewPopupManager(Context context, AnyKeyboardBaseView keyboardView) {
		mContext = context;
		mKeyboardView = keyboardView;
		mMaxPopupInstances = context.getResources().getInteger(R.integer.maximum_instances_of_preview_popups);
		mUIHandler = new UIHandler(this);
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
		cancelAllPreviews();
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
		if (!mActivePopups.containsKey(key)) {
			//the key is not active.
			//we have several options how to fetch a popup
			//1) fetch the head from the queue: if actives count is less than queue size
			if (mActivePopups.size() < mPreviewPopupsQueue.size()) {
				//removing from the head
				PreviewPopup previewPopup = mPreviewPopupsQueue.remove();
				mActivePopups.put(key, previewPopup);
				//adding back to the tail
				mPreviewPopupsQueue.add(previewPopup);
			} else {
				//this MUST mean that actives and queue size are the same
				Assert.assertEquals(mActivePopups.size(), mPreviewPopupsQueue.size());
				//2) if queue size is not the maximum, then create a new one
				if (mPreviewPopupsQueue.size() < mMaxPopupInstances) {
					PreviewPopup previewPopup = new PreviewPopup(mContext, mKeyboardView, mPreviewPopupTheme);
					mActivePopups.put(key, previewPopup);
					//adding back to the tail
					mPreviewPopupsQueue.add(previewPopup);
				} else {
					//3) the maximum size of the queue has reached. We'll use the head again.
					//but first, we need to de-associate it from its current key
					PreviewPopup previewPopup = mPreviewPopupsQueue.remove();
					//locating the preview's key
					Keyboard.Key oldKey = null;
					for (Map.Entry<Keyboard.Key, PreviewPopup> pair : mActivePopups.entrySet()) {
						if (pair.getValue() == previewPopup) {
							oldKey = pair.getKey();
							break;
						}
					}
					Assert.assertNotNull(oldKey);
					mActivePopups.remove(oldKey);
					mActivePopups.put(key, previewPopup);
					//adding back to the tail
					mPreviewPopupsQueue.add(previewPopup);
				}
			}
		}
		return mActivePopups.get(key);
	}

	public void cancelAllPreviews() {
		mUIHandler.cancelAllMessages();
		for (PreviewPopup previewPopup : mPreviewPopupsQueue) {
			previewPopup.dismiss();
		}
		mActivePopups.clear();
	}

	public PreviewPopupTheme getPreviewPopupTheme() {
		return mPreviewPopupTheme;
	}

	public void resetAllPreviews() {
		cancelAllPreviews();
		CompatUtils.unbindDrawable(mPreviewPopupTheme.getPreviewKeyBackground());
		mPreviewPopupsQueue.clear();
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
}
