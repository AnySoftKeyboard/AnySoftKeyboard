package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.anysoftkeyboard.ime.InputViewActionsProvider;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;

public class KeyboardViewContainerView extends ViewGroup implements ThemeableChild {

    private static final int PROVIDER_TAG_ID = R.id.keyboard_container_provider_tag_id;

    private final int mActionStripHeight;
    private boolean mShowActionStrip = true;
    private final List<View> mStripActionViews = new ArrayList<>();
    private InputViewBinder mStandardKeyboardView;
    private CandidateView mCandidateView;
    private OnKeyboardActionListener mKeyboardActionListener;
    private KeyboardTheme mKeyboardTheme;
    private OverlayData mOverlayData = new OverlayData();

    public KeyboardViewContainerView(Context context) {
        super(context);
        mActionStripHeight = getResources().getDimensionPixelSize(R.dimen.candidate_strip_height);
    }

    public KeyboardViewContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActionStripHeight = getResources().getDimensionPixelSize(R.dimen.candidate_strip_height);
    }

    public KeyboardViewContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mActionStripHeight = getResources().getDimensionPixelSize(R.dimen.candidate_strip_height);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public KeyboardViewContainerView(
            Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mActionStripHeight = getResources().getDimensionPixelSize(R.dimen.candidate_strip_height);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (mKeyboardActionListener != null && child instanceof InputViewActionsProvider) {
            ((InputViewActionsProvider) child).setOnKeyboardActionListener(mKeyboardActionListener);
        }

        setThemeForChildView(child);
    }

    public void setStripActionsVisibility(boolean visible) {
        if (mShowActionStrip != visible) {
            mShowActionStrip = visible;
            if (mCandidateView != null)
                mCandidateView.setVisibility(visible ? View.VISIBLE : View.GONE);

            for (View stripActionView : mStripActionViews) {
                if (visible) {
                    addView(stripActionView);
                } else {
                    removeView(stripActionView);
                }
            }

            invalidate();
        }
    }

    public void addStripAction(@NonNull StripActionProvider provider) {
        for (View stripActionView : mStripActionViews) {
            if (stripActionView.getTag(PROVIDER_TAG_ID) == provider) {
                return;
            }
        }

        View actionView = provider.inflateActionView(this);
        actionView.setTag(PROVIDER_TAG_ID, provider);
        if (mShowActionStrip) {
            addView(actionView);
        }
        mStripActionViews.add(actionView);

        invalidate();
    }

    public void removeStripAction(@NonNull StripActionProvider provider) {
        for (View stripActionView : mStripActionViews) {
            if (stripActionView.getTag(PROVIDER_TAG_ID) == provider) {
                removeView(stripActionView);
                provider.onRemoved();
                mStripActionViews.remove(stripActionView);
                invalidate();
                return;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        final int left = l + getPaddingLeft();
        final int right = r - getPaddingRight();
        int currentTop = t + getPaddingTop();
        final int actionsTop = t + getPaddingTop();
        int actionRight = r - getPaddingRight();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            if (child.getTag(PROVIDER_TAG_ID) == null) {
                child.layout(left, currentTop, right, currentTop + child.getMeasuredHeight());
                currentTop += child.getMeasuredHeight();
            } else {
                // this is an action. It lives on the candidates-view
                child.layout(
                        actionRight - child.getMeasuredWidth(),
                        actionsTop,
                        actionRight,
                        actionsTop + child.getMeasuredHeight());
                actionRight -= child.getMeasuredWidth();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int totalWidth = 0;
        int totalHeight = mShowActionStrip ? mActionStripHeight : 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            if (child.getTag(PROVIDER_TAG_ID) != null || child == mCandidateView) {
                // this is an action. we just need to make sure it is measured.
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
            } else {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                totalWidth = Math.max(totalWidth, child.getMeasuredWidth());
                totalHeight += child.getMeasuredHeight();
            }
        }

        setMeasuredDimension(totalWidth, totalHeight);
    }

    private void setThemeForChildView(View child) {
        if (child instanceof ThemeableChild && mKeyboardTheme != null) {
            ((ThemeableChild) child).setKeyboardTheme(mKeyboardTheme);
            ((ThemeableChild) child).setThemeOverlay(mOverlayData);
        }
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener keyboardActionListener) {
        mKeyboardActionListener = keyboardActionListener;
        for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
            View child = getChildAt(childIndex);
            if (child instanceof InputViewActionsProvider) {
                ((InputViewActionsProvider) child)
                        .setOnKeyboardActionListener(keyboardActionListener);
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

    public void setBottomPadding(int bottomPadding) {
        ((AnyKeyboardView) getStandardKeyboardView()).setBottomOffset(bottomPadding);
    }

    public interface StripActionProvider {
        View inflateActionView(ViewGroup parent);

        void onRemoved();
    }
}
