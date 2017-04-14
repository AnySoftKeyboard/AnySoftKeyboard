package com.anysoftkeyboard.keyboards.views.preview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBase;
import com.anysoftkeyboard.utils.Logger;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import junit.framework.Assert;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class KeyPreviewsManager {

    private static final String TAG = "ASK_PPM";
    private static final KeyPreview NULL_KEY_PREVIEW = new KeyPreview() {
        @Override
        public void showPreviewForKey(Keyboard.Key key, CharSequence label, Point previewPosition) {
        }

        @Override
        public void showPreviewForKey(Keyboard.Key key, Drawable icon, Point previewPosition) {
        }

        @Override
        public void dismiss() {
        }
    };
    private final int mMaxPopupInstances;
    private final PreviewPopupTheme mPreviewPopupTheme;

    private final Queue<KeyPreview> mFreeKeyPreviews = new LinkedList<>();
    private final Queue<KeyPreview> mActiveKeyPreviews = new LinkedList<>();
    private final Map<Keyboard.Key, KeyPreview> mActivePopupByKeyMap = new HashMap<>();
    @Nullable
    private Context mContext;
    @Nullable
    private AnyKeyboardViewBase mKeyboardView;
    private final UIHandler mUiHandler;

    private boolean mEnabled = true;
    private final PositionCalculator mPositionCalculator;

    public KeyPreviewsManager(@NonNull Context context, @NonNull AnyKeyboardViewBase keyboardView, @NonNull PreviewPopupTheme previewPopupTheme) {
        mPreviewPopupTheme = previewPopupTheme;
        mContext = context;
        mKeyboardView = keyboardView;
        mMaxPopupInstances = AnyApplication.getConfig().showKeyPreviewAboveKey() ?
                context.getResources().getInteger(R.integer.maximum_instances_of_preview_popups) : 1;
        mUiHandler = new UIHandler(this, context.getResources().getInteger(R.integer.preview_dismiss_delay));
        mPositionCalculator = AnyApplication.getConfig().showKeyPreviewAboveKey() ?
                new AboveKeyPositionCalculator() : new AboveKeyboardPositionCalculator();
    }

    public void setEnabled(boolean enabled) {
        if (mContext == null || mKeyboardView == null) {
            //not allowing enabling if the context is null
            mEnabled = false;
        } else {
            mEnabled = enabled;
            cancelAllPreviews();
        }
    }

    public void hidePreviewForKey(Keyboard.Key key) {
        if (mEnabled) mUiHandler.dismissPreview(key);
    }

    public void showPreviewForKey(Keyboard.Key key, Drawable icon) {
        if (isDisabled()) return;
        KeyPreview popup = getPopupForKey(key, false);
        Point previewPosition = mPositionCalculator.calculatePositionForPreview(key, mKeyboardView, mPreviewPopupTheme, mKeyboardView.getLocationInWindow());
        popup.showPreviewForKey(key, icon, previewPosition);
    }

    private boolean isDisabled() {
        return !mEnabled;
    }

    public void showPreviewForKey(Keyboard.Key key, CharSequence label) {
        if (isDisabled()) return;
        KeyPreview popup = getPopupForKey(key, false);
        Point previewPosition = mPositionCalculator.calculatePositionForPreview(key, mKeyboardView, mPreviewPopupTheme, mKeyboardView.getLocationInWindow());
        popup.showPreviewForKey(key, label, previewPosition);
    }

    @NonNull
    private KeyPreview getPopupForKey(Keyboard.Key key, boolean onlyActivePopups) {
        mUiHandler.cancelDismissForKey(key);
        if (shouldNotShowPreview(key)) return NULL_KEY_PREVIEW;

        if (!mActivePopupByKeyMap.containsKey(key) && !onlyActivePopups) {
            //the key is not active.
            //we have several options how to fetch a popup
            //1) fetch the head from the free queue: if the queue size is not empty
            if (!mFreeKeyPreviews.isEmpty()) {
                //removing from the head
                KeyPreview keyPreview = mFreeKeyPreviews.remove();
                mActivePopupByKeyMap.put(key, keyPreview);
                mActiveKeyPreviews.add(keyPreview);
            } else {
                //we do not have unused popups, we'll need to reuse one of the actives
                //2) if queue size is not the maximum, then create a new one
                if (mActiveKeyPreviews.size() < mMaxPopupInstances) {
                    KeyPreview keyPreview = new KeyPreviewPopupWindow(mContext, mKeyboardView, mPreviewPopupTheme);
                    mActivePopupByKeyMap.put(key, keyPreview);
                    mActiveKeyPreviews.add(keyPreview);
                } else {
                    //3) we need to reused a currently active preview
                    KeyPreview keyPreview = mActiveKeyPreviews.remove();
                    //de-associating the old key with the popup
                    Keyboard.Key oldKey = null;
                    for (Map.Entry<Keyboard.Key, KeyPreview> pair : mActivePopupByKeyMap.entrySet()) {
                        if (pair.getValue() == keyPreview) {
                            oldKey = pair.getKey();
                            break;
                        }
                    }
                    Assert.assertNotNull(oldKey);
                    mActivePopupByKeyMap.remove(oldKey);
                    mActivePopupByKeyMap.put(key, keyPreview);
                    mActiveKeyPreviews.add(keyPreview);
                }
            }
        }
        return mActivePopupByKeyMap.get(key);
    }

    private boolean shouldNotShowPreview(Keyboard.Key key) {
        return key == null ||//no key, no preview
                key.modifier ||//modifiers should not preview (that's just weird)
                key.getCodesCount() == 0 ||//no key output, no preview
                (key.getCodesCount() == 1 && isKeyCodeShouldNotBeShown(key.getPrimaryCode())) ||
                mPreviewPopupTheme.getPreviewKeyTextSize() <= 0;
    }

    private boolean isKeyCodeShouldNotBeShown(int code) {
        return code <= 0 || code == KeyCodes.ENTER || code == KeyCodes.SPACE;
    }

    public void cancelAllPreviews() {
        mUiHandler.cancelAllMessages();
        for (KeyPreview keyPreview : mActiveKeyPreviews) {
            keyPreview.dismiss();
            mFreeKeyPreviews.add(keyPreview);
        }
        mActiveKeyPreviews.clear();
        mActivePopupByKeyMap.clear();
    }

    public void destroy() {
        cancelAllPreviews();
        mEnabled = false;
        mContext = null;
        mKeyboardView = null;
    }

    private static class UIHandler extends Handler {

        private final WeakReference<KeyPreviewsManager> mPopupManagerWeakReference;

        private static final int MSG_DISMISS_PREVIEW = R.id.popup_manager_dismiss_preview_message_id;
        private final long mDelayBeforeDismiss;

        public UIHandler(KeyPreviewsManager popupManager, long delayBeforeDismiss) {
            mDelayBeforeDismiss = delayBeforeDismiss;
            mPopupManagerWeakReference = new WeakReference<>(popupManager);
        }

        @Override
        public void handleMessage(Message msg) {
            KeyPreviewsManager popupManager = mPopupManagerWeakReference.get();
            if (popupManager == null || popupManager.isDisabled())
                return;

            switch (msg.what) {
                case MSG_DISMISS_PREVIEW:
                    popupManager.internalDismissPopupForKey((Keyboard.Key) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }

        public void cancelAllMessages() {
            removeMessages(MSG_DISMISS_PREVIEW);
        }

        public void dismissPreview(Keyboard.Key key) {
            cancelDismissForKey(key);
            sendMessageDelayed(obtainMessage(MSG_DISMISS_PREVIEW, key), mDelayBeforeDismiss);
        }

        public void cancelDismissForKey(Keyboard.Key key) {
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
        } finally {
            Assert.assertSame(popup, mActivePopupByKeyMap.remove(key));
            Assert.assertTrue(mFreeKeyPreviews.add(popup));
            Assert.assertTrue(mActiveKeyPreviews.remove(popup));
        }
    }
}
