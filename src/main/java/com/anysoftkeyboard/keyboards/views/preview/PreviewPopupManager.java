package com.anysoftkeyboard.keyboards.views.preview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

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

	private final Queue<PreviewPopup> mFreePreviewPopups = new LinkedList<>();
	private final Queue<PreviewPopup> mActivePreviewPopups = new LinkedList<>();
	private final Map<Keyboard.Key, PreviewPopup> mActivePopupByKeyMap = new HashMap<>();
	private final Context mContext;
	private final AnyKeyboardBaseView mKeyboardView;
	private final UIHandler mUIHandler;

	private boolean mEnabled = true;

	public PreviewPopupManager(Context context, AnyKeyboardBaseView keyboardView) {
		mContext = context;
		mKeyboardView = keyboardView;
		mMaxPopupInstances = context.getResources().getInteger(R.integer.maximum_instances_of_preview_popups);
		mUIHandler = new UIHandler(this, context.getResources().getInteger(R.integer.preview_in_animation_duration));
	}

	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
		cancelAllPreviews();
	}

	public void hidePreviewForKey(Keyboard.Key key) {
		mUIHandler.dismissPreview(key);
	}

	public void showPreviewForKey(Keyboard.Key key, Drawable icon) {
		if (isDisabled()) return;
		PreviewPopup popup = getPopupForKey(key, false);
		if (popup != null) {
			Point previewPosition = PreviewPopupPositionCalculator.calculatePositionForPreview(key, mKeyboardView, mPreviewPopupTheme);
			popup.showPreviewForKey(key, icon, previewPosition);
		}
	}

	private boolean isDisabled() {
		return !mEnabled || mPreviewPopupTheme.getPreviewKeyTextSize() <= 0;
	}

	public void showPreviewForKey(Keyboard.Key key, CharSequence label) {
		if (isDisabled()) return;
		PreviewPopup popup = getPopupForKey(key, false);
		if (popup != null) {
			Point previewPosition = PreviewPopupPositionCalculator.calculatePositionForPreview(key, mKeyboardView, mPreviewPopupTheme);
			popup.showPreviewForKey(key, label, previewPosition);
		}
	}

	@Nullable
	private PreviewPopup getPopupForKey(Keyboard.Key key, boolean onlyActivePopups) {
		if (!mActivePopupByKeyMap.containsKey(key) && !onlyActivePopups) {
			//the key is not active.
			//we have several options how to fetch a popup
			//1) fetch the head from the free queue: if the queue size is not empty
			if (!mFreePreviewPopups.isEmpty()) {
				//removing from the head
				PreviewPopup previewPopup = mFreePreviewPopups.remove();
				mActivePopupByKeyMap.put(key, previewPopup);
				mActivePreviewPopups.add(previewPopup);
			} else {
				//we do not have unused popups, we'll need to reuse one of the actives
				//2) if queue size is not the maximum, then create a new one
				if (mActivePreviewPopups.size() < mMaxPopupInstances) {
					PreviewPopup previewPopup = new PreviewPopup(mContext, mKeyboardView, mPreviewPopupTheme);
					mActivePopupByKeyMap.put(key, previewPopup);
					mActivePreviewPopups.add(previewPopup);
				} else {
					//3) we need to reused a currently active preview
					PreviewPopup previewPopup = mActivePreviewPopups.remove();
					//de-associating the old key with the popup
					Keyboard.Key oldKey = null;
					for (Map.Entry<Keyboard.Key, PreviewPopup> pair : mActivePopupByKeyMap.entrySet()) {
						if (pair.getValue() == previewPopup) {
							oldKey = pair.getKey();
							break;
						}
					}
					Assert.assertNotNull(oldKey);
					mActivePopupByKeyMap.remove(oldKey);
					mActivePopupByKeyMap.put(key, previewPopup);
					mActivePreviewPopups.add(previewPopup);
				}
			}
		}
		return mActivePopupByKeyMap.get(key);
	}

	public void cancelAllPreviews() {
		mUIHandler.cancelAllMessages();
		for (PreviewPopup previewPopup : mActivePreviewPopups) {
			previewPopup.dismiss();
			mFreePreviewPopups.add(previewPopup);
		}
		mActivePreviewPopups.clear();
		mActivePopupByKeyMap.clear();
	}

	public PreviewPopupTheme getPreviewPopupTheme() {
		return mPreviewPopupTheme;
	}

	public void resetAllPreviews() {
		cancelAllPreviews();
		CompatUtils.unbindDrawable(mPreviewPopupTheme.getPreviewKeyBackground());
		mActivePreviewPopups.clear();
	}

	private static class UIHandler extends Handler {

		private final WeakReference<PreviewPopupManager> mPopupManagerWeakReference;

		private static final int MSG_DISMISS_PREVIEW = R.id.popup_manager_dismiss_preview_message_id;
		private final long mDelayBeforeDismiss;

		public UIHandler(PreviewPopupManager popupManager, long delayBeforeDismiss) {
			mDelayBeforeDismiss = delayBeforeDismiss;
			mPopupManagerWeakReference = new WeakReference<>(popupManager);
		}

		@Override
		public void handleMessage(Message msg) {
			PreviewPopupManager popupManager = mPopupManagerWeakReference.get();
			if (popupManager == null)
				return;
			switch (msg.what) {
				case MSG_DISMISS_PREVIEW:
					popupManager.internalDismissPopupForKey((Keyboard.Key) msg.obj);
					break;
			}
		}

		public void cancelAllMessages() {
			removeMessages(MSG_DISMISS_PREVIEW);
		}

		public void dismissPreview(Keyboard.Key key) {
			sendMessageDelayed(obtainMessage(MSG_DISMISS_PREVIEW, key), mDelayBeforeDismiss);
		}
	}

	private void internalDismissPopupForKey(Keyboard.Key key) {
		PreviewPopup popup = getPopupForKey(key, true);
		if (popup != null) {
			popup.dismiss();
			Assert.assertSame(popup, mActivePopupByKeyMap.remove(key));
			Assert.assertTrue(mFreePreviewPopups.add(popup));
			Assert.assertTrue(mActivePreviewPopups.remove(popup));
		}
	}
}
