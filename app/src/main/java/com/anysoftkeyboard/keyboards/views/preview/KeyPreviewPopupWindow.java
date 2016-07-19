package com.anysoftkeyboard.keyboards.views.preview;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.anysoftkeyboard.AskPrefs;
import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

public class KeyPreviewPopupWindow implements KeyPreview {

    private static final int[] LONG_PRESSABLE_STATE_SET = {android.R.attr.state_long_pressable};
    private static final int[] EMPTY_STATE_SET = {};

    private int mPreviewPaddingWidth = -1;
    private int mPreviewPaddingHeight = -1;

    private final ViewGroup mPreviewLayout;
    private final TextView mPreviewText;
    private final ImageView mPreviewIcon;

    private final View mParentView;
    private final PopupWindow mPopupWindow;
    private final PreviewPopupTheme mPreviewPopupTheme;
    private final boolean mOffsetContentByKeyHeight;

    public KeyPreviewPopupWindow(Context context, View parentView, PreviewPopupTheme previewPopupTheme) {
        mParentView = parentView;
        mPreviewPopupTheme = previewPopupTheme;
        mPopupWindow = new PopupWindow(context);
        CompatUtils.setPopupUnattachedToDecor(mPopupWindow);
        mPopupWindow.setClippingEnabled(false);

        LayoutInflater inflate = LayoutInflater.from(context);
        if (mPreviewPopupTheme.getPreviewKeyTextSize() > 0) {
            mPreviewLayout = (ViewGroup) inflate.inflate(R.layout.key_preview, null);
            mPreviewText = (TextView) mPreviewLayout.findViewById(R.id.key_preview_text);
            mPreviewText.setTextColor(mPreviewPopupTheme.getPreviewKeyTextColor());
            mPreviewText.setTypeface(mPreviewPopupTheme.getKeyStyle());
            mPreviewIcon = (ImageView) mPreviewLayout.findViewById(R.id.key_preview_icon);
            mPopupWindow.setBackgroundDrawable(mPreviewPopupTheme.getPreviewKeyBackground().getConstantState().newDrawable(context.getResources()));
            mPopupWindow.setContentView(mPreviewLayout);
        } else {
            mPreviewIcon = null;
            mPreviewLayout = null;
            mPreviewText = null;
        }
        mOffsetContentByKeyHeight = AnyApplication.getConfig().showKeyPreviewAboveKey();
        mPopupWindow.setTouchable(false);
        mPopupWindow.setAnimationStyle((AnyApplication.getConfig().getAnimationsLevel() == AskPrefs.AnimationsLevel.None) ? 0 : R.style.KeyPreviewAnimation);
    }

    @Override
    public void showPreviewForKey(Keyboard.Key key, CharSequence label, Point previewPosition) {
        mPreviewIcon.setVisibility(View.GONE);
        mPreviewText.setVisibility(View.VISIBLE);
        mPreviewIcon.setImageDrawable(null);
        mPreviewText.setTextColor(mPreviewPopupTheme.getPreviewKeyTextColor());

        mPreviewText.setText(label);
        if (label.length() > 1 && key.getCodesCount() < 2) {
            mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    mPreviewPopupTheme.getPreviewLabelTextSize());
        } else {
            mPreviewText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    mPreviewPopupTheme.getPreviewKeyTextSize());
        }

        mPreviewText.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        showPopup(key, mPreviewText.getMeasuredWidth(), mPreviewText.getMeasuredHeight(), previewPosition);
    }

    @Override
    public void showPreviewForKey(Keyboard.Key key, Drawable icon, Point previewPosition) {
        mPreviewIcon.setVisibility(View.VISIBLE);
        mPreviewText.setVisibility(View.GONE);
        mPreviewIcon.setImageState(icon.getState(), false);
        // end of hack. You see, the drawable comes with a state, this state
        // is overridden by the ImageView. No more.
        mPreviewIcon.setImageDrawable(icon);
        mPreviewIcon.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        mPreviewText.setText(null);
        showPopup(key, mPreviewIcon.getMeasuredWidth(), mPreviewIcon.getMeasuredHeight(), previewPosition);
    }

    private void showPopup(Keyboard.Key key, int contentWidth, int contentHeight, Point previewPosition) {
        contentWidth = Math.max(contentWidth, key.width);
        if (mOffsetContentByKeyHeight) contentHeight += key.height;
        contentHeight = Math.max(contentHeight, key.height);
        mPreviewLayout.setPadding(0, 0, 0, mOffsetContentByKeyHeight ? key.height : 0);
        final Drawable previewKeyBackground = mPreviewPopupTheme.getPreviewKeyBackground();
        if (mPreviewPaddingHeight < 0) {
            mPreviewPaddingWidth = 0;
            mPreviewPaddingHeight = 0;

            if (previewKeyBackground != null) {
                Rect padding = new Rect();
                previewKeyBackground.getPadding(padding);
                mPreviewPaddingWidth += (padding.left + padding.right);
                mPreviewPaddingHeight += (padding.top + padding.bottom);
            }
        }
        contentWidth += mPreviewPaddingWidth;
        contentHeight += mPreviewPaddingHeight;

        // and checking that the width and height are big enough for the
        // background.
        if (previewKeyBackground != null) {
            contentWidth = Math.max(previewKeyBackground.getMinimumWidth(), contentWidth);
            contentHeight = Math.max(previewKeyBackground.getMinimumHeight(), contentHeight);
        }

        final int popupPreviewX = previewPosition.x - contentWidth / 2;
        final int popupPreviewY = previewPosition.y - contentHeight;

        if (mPopupWindow.isShowing()) {
            mPopupWindow.update(popupPreviewX, popupPreviewY, contentWidth, contentHeight);
        } else {
            mPopupWindow.setWidth(contentWidth);
            mPopupWindow.setHeight(contentHeight);
            try {
                // https://github.com/AnySoftKeyboard/AnySoftKeyboard/issues/6
                // I don't understand why this should happen, and only with MIUI
                // ROMs.
                // anyhow, it easy to hide :)
                mPopupWindow.showAtLocation(mParentView, Gravity.NO_GRAVITY, popupPreviewX, popupPreviewY);
            } catch (RuntimeException e) {
                // nothing to do here. I think.
            }

        }
        mPreviewLayout.setVisibility(View.VISIBLE);

        // Set the preview background state
        if (previewKeyBackground != null) {
            previewKeyBackground.setState(key.popupResId != 0 ? LONG_PRESSABLE_STATE_SET : EMPTY_STATE_SET);
        }

        mPreviewLayout.requestLayout();
        mPreviewLayout.invalidate();
    }

    @Override
    public void dismiss() {
        mPopupWindow.dismiss();
    }
}
