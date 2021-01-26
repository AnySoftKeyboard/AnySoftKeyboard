package com.anysoftkeyboard.keyboards.views.preview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class KeyPreviewsManager implements KeyPreviewsController {

    private static final String TAG = "ASKKeyPreviewsManager";
    private static final KeyPreview NULL_KEY_PREVIEW =
            new KeyPreview() {
                @Override
                public void showPreviewForKey(
                        Keyboard.Key key, CharSequence label, Point previewPosition) {}

                @Override
                public void showPreviewForKey(
                        Keyboard.Key key, Drawable icon, Point previewPosition) {}

                @Override
                public void dismiss() {}
            };

    private final int[] mThisWindowOffset = new int[2];

    private final int mMaxPopupInstances;

    private final Queue<KeyPreview> mFreeKeyPreviews = new ArrayDeque<>();
    private final Queue<KeyPreview> mActiveKeyPreviews = new ArrayDeque<>();
    private final Map<Keyboard.Key, KeyPreview> mActivePopupByKeyMap = new HashMap<>();
    private final Context mContext;
    private final UIHandler mUiHandler;
    private final PositionCalculator mPositionCalculator;

    public KeyPreviewsManager(
            @NonNull Context context,
            @NonNull PositionCalculator positionCalculator,
            int maxPopupInstances) {
        mContext = context;
        mPositionCalculator = positionCalculator;
        mUiHandler =
                new UIHandler(
                        this, context.getResources().getInteger(R.integer.preview_dismiss_delay));
        mMaxPopupInstances = maxPopupInstances;
    }

    @Override
    public void hidePreviewForKey(Keyboard.Key key) {
        mUiHandler.dismissPreview(key);
    }

    @Override
    public void showPreviewForKey(
            Keyboard.Key key, Drawable icon, View parentView, PreviewPopupTheme previewPopupTheme) {
        KeyPreview popup = getPopupForKey(key, parentView, previewPopupTheme);
        Point previewPosition =
                mPositionCalculator.calculatePositionForPreview(
                        key, previewPopupTheme, getLocationInWindow(parentView));
        popup.showPreviewForKey(key, icon, previewPosition);
    }

    private int[] getLocationInWindow(View parentView) {
        parentView.getLocationInWindow(mThisWindowOffset);
        return mThisWindowOffset;
    }

    @Override
    public void showPreviewForKey(
            Keyboard.Key key,
            CharSequence label,
            View parentView,
            PreviewPopupTheme previewPopupTheme) {
        KeyPreview popup = getPopupForKey(key, parentView, previewPopupTheme);
        Point previewPosition =
                mPositionCalculator.calculatePositionForPreview(
                        key, previewPopupTheme, getLocationInWindow(parentView));
        popup.showPreviewForKey(key, label, previewPosition);
    }

    @NonNull
    private KeyPreview getPopupForKey(
            Keyboard.Key key, View parentView, PreviewPopupTheme previewPopupTheme) {
        mUiHandler.cancelDismissForKey(key);
        if (shouldNotShowPreview(key) || previewPopupTheme.getPreviewKeyTextSize() <= 0)
            return NULL_KEY_PREVIEW;

        if (!mActivePopupByKeyMap.containsKey(key)) {
            // the key is not active.
            // we have several options how to fetch a popup
            // 1) fetch the head from the free queue: if the queue size is not empty
            if (!mFreeKeyPreviews.isEmpty()) {
                // removing from the head
                KeyPreview keyPreview = mFreeKeyPreviews.remove();
                mActivePopupByKeyMap.put(key, keyPreview);
                mActiveKeyPreviews.add(keyPreview);
            } else {
                // we do not have unused popups, we'll need to reuse one of the actives
                // 2) if queue size is not the maximum, then create a new one
                if (mActiveKeyPreviews.size() < mMaxPopupInstances) {
                    KeyPreview keyPreview =
                            new KeyPreviewPopupWindow(mContext, parentView, previewPopupTheme);
                    mActivePopupByKeyMap.put(key, keyPreview);
                    mActiveKeyPreviews.add(keyPreview);
                } else {
                    // 3) we need to reused a currently active preview
                    KeyPreview keyPreview = mActiveKeyPreviews.remove();
                    // de-associating the old key with the popup
                    Keyboard.Key oldKey = null;
                    for (Map.Entry<Keyboard.Key, KeyPreview> pair :
                            mActivePopupByKeyMap.entrySet()) {
                        if (pair.getValue() == keyPreview) {
                            oldKey = pair.getKey();
                            break;
                        }
                    }

                    mActivePopupByKeyMap.remove(oldKey);
                    mActivePopupByKeyMap.put(key, keyPreview);
                    mActiveKeyPreviews.add(keyPreview);
                }
            }
        }
        return mActivePopupByKeyMap.get(key);
    }

    private boolean shouldNotShowPreview(Keyboard.Key key) {
        return key == null
                || // no key, no preview
                key.modifier
                || // modifiers should not preview (that's just weird)
                !key.showPreview
                || // maybe the layout author doesn't want us to preview
                key.getCodesCount() == 0
                || // no key output, no preview
                (key.getCodesCount() == 1 && isKeyCodeShouldNotBeShown(key.getPrimaryCode()));
    }

    private boolean isKeyCodeShouldNotBeShown(int code) {
        return code <= 0 || code == KeyCodes.ENTER || code == KeyCodes.SPACE;
    }

    @Override
    public void cancelAllPreviews() {
        mUiHandler.cancelAllMessages();
        for (KeyPreview keyPreview : mActiveKeyPreviews) {
            keyPreview.dismiss();
            mFreeKeyPreviews.add(keyPreview);
        }
        mActiveKeyPreviews.clear();
        mActivePopupByKeyMap.clear();
    }

    @Override
    public void destroy() {
        cancelAllPreviews();
        mFreeKeyPreviews.clear();
    }

    @VisibleForTesting
    public PositionCalculator getPositionCalculator() {
        return mPositionCalculator;
    }

    private static class UIHandler extends Handler {

        private final WeakReference<KeyPreviewsManager> mPopupManagerWeakReference;

        private static final int MSG_DISMISS_PREVIEW =
                R.id.popup_manager_dismiss_preview_message_id;
        private final long mDelayBeforeDismiss;

        UIHandler(KeyPreviewsManager popupManager, long delayBeforeDismiss) {
            mDelayBeforeDismiss = delayBeforeDismiss;
            mPopupManagerWeakReference = new WeakReference<>(popupManager);
        }

        @Override
        public void handleMessage(Message msg) {
            final KeyPreviewsManager popupManager = mPopupManagerWeakReference.get();
            if (popupManager != null) {
                if (msg.what == MSG_DISMISS_PREVIEW) {
                    popupManager.internalDismissPopupForKey((Keyboard.Key) msg.obj);
                } else {
                    super.handleMessage(msg);
                }
            }
        }

        void cancelAllMessages() {
            removeMessages(MSG_DISMISS_PREVIEW);
        }

        void dismissPreview(Keyboard.Key key) {
            cancelDismissForKey(key);
            sendMessageDelayed(obtainMessage(MSG_DISMISS_PREVIEW, key), mDelayBeforeDismiss);
        }

        void cancelDismissForKey(Keyboard.Key key) {
            removeMessages(MSG_DISMISS_PREVIEW, key);
        }
    }

    private void internalDismissPopupForKey(Keyboard.Key key) {
        if (shouldNotShowPreview(key) || !mActivePopupByKeyMap.containsKey(key)) return;
        KeyPreview popup = mActivePopupByKeyMap.get(key);
        try {
            popup.dismiss();
        } catch (IllegalArgumentException e) {
            Logger.w(TAG, e, "Failed to dismiss popup, probably the view is gone already.");
        }
    }
}
