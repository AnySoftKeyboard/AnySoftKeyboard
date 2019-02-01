package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.anysoftkeyboard.ime.InputViewActionsProvider;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.theme.KeyboardTheme;


public class KeyboardViewContainerView extends ViewGroup implements ThemeableChild {
    private InputViewBinder mStandardKeyboardView;
    private CandidateView mCandidateView;
    private OnKeyboardActionListener mKeyboardActionListener;
    private KeyboardTheme mKeyboardTheme;
    private OverlayData mOverlayData;

    public KeyboardViewContainerView(Context context) {
        super(context);
        setWillNotDraw(false);
    }

    public KeyboardViewContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public KeyboardViewContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KeyboardViewContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (mKeyboardActionListener != null && child instanceof InputViewActionsProvider) {
            ((InputViewActionsProvider) child).setOnKeyboardActionListener(mKeyboardActionListener);
        }

        setThemeForChildView(child);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        final int left = l + getPaddingLeft();
        final int right = r - getPaddingRight();
        int currentTop = t + getPaddingTop();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            child.layout(left, currentTop, right, currentTop + child.getMeasuredHeight());
            currentTop += child.getMeasuredHeight();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalWidth = 0;
        int totalHeight = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;

            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            totalWidth = Math.max(totalWidth, child.getMeasuredWidth());
            totalHeight += child.getMeasuredHeight();
        }

        setMeasuredDimension(totalWidth, totalHeight);
    }

    private void setThemeForChildView(View child) {
        if (child instanceof ThemeableChild) {
            if (mKeyboardTheme != null) {
                ((ThemeableChild) child).setKeyboardTheme(mKeyboardTheme);
                ((ThemeableChild) child).setThemeOverlay(mOverlayData);
            }
        }
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener keyboardActionListener) {
        mKeyboardActionListener = keyboardActionListener;
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            View child = getChildAt(childIndex);
            if (child instanceof InputViewActionsProvider) {
                ((InputViewActionsProvider) child).setOnKeyboardActionListener(keyboardActionListener);
            }
        }
    }

    public CandidateView getCandidateView() {
        if (mCandidateView == null) {
            mCandidateView = (CandidateView) getChildAt(0);
        }
        return mCandidateView;
    }

    public InputViewBinder getStandardKeyboardView() {
        if (mStandardKeyboardView == null) {
            mStandardKeyboardView = (InputViewBinder) getChildAt(1);
        }
        return mStandardKeyboardView;
    }

    @Override
    public void setKeyboardTheme(KeyboardTheme theme) {
        mKeyboardTheme = theme;
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            setThemeForChildView(getChildAt(childIndex));
        }
    }

    @Override
    public void setThemeOverlay(OverlayData overlay) {
        mOverlayData = overlay;
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            setThemeForChildView(getChildAt(childIndex));
        }
    }

}
