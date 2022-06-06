package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.anysoftkeyboard.ime.InputViewActionsProvider;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.overlay.OverlayData;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;

public class KeyboardViewContainerView extends ViewGroup implements ThemeableChild {

    private static final int PROVIDER_TAG_ID = R.id.keyboard_container_provider_tag_id;
    private static final int FIRST_PROVIDER_VIEW_INDEX = 3;

    private final int mActionStripHeight;
    private boolean mShowActionStrip = true;
    private final List<View> mStripActionViews = new ArrayList<>();
    private InputViewBinder mStandardKeyboardView;
    private CandidateView mCandidateView;
    private OnKeyboardActionListener mKeyboardActionListener;
    private KeyboardTheme mKeyboardTheme;
    private OverlayData mOverlayData = new OverlayData();
    private LinearLayout mInlineAutofillView;
    private HorizontalScrollView mInlineScrollView;

    public KeyboardViewContainerView(Context context) {
        super(context);
        setWillNotDraw(false);
        mActionStripHeight = getResources().getDimensionPixelSize(R.dimen.candidate_strip_height);
    }

    public KeyboardViewContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        mActionStripHeight = getResources().getDimensionPixelSize(R.dimen.candidate_strip_height);
    }

    public KeyboardViewContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
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
        setActionsStripVisibility(mShowActionStrip);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        setActionsStripVisibility(mShowActionStrip);
    }

    public void setActionsStripVisibility(boolean requestedVisibility) {
        mShowActionStrip = requestedVisibility;
        if (mCandidateView != null) {
            // calculating the actual needed visibility:
            // at least one visible view which is a ActionsStripSupportedChild
            boolean visible = false;
            for (int childIndex = 0; childIndex < getChildCount(); childIndex++) {
                View child = getChildAt(childIndex);
                if (child.getVisibility() == View.VISIBLE) {
                    if (child instanceof ActionsStripSupportedChild) {
                        visible = requestedVisibility;
                        break;
                    }
                }
            }

            final int targetVisibility = visible ? View.VISIBLE : View.GONE;
            if (targetVisibility != mCandidateView.getVisibility()) {
                mCandidateView.setVisibility(targetVisibility);

                for (View stripActionView : mStripActionViews) {
                    if (visible) {
                        // it might already be visible
                        if (stripActionView.getParent() == null) {
                            addView(stripActionView);
                        }
                    } else {
                        removeView(stripActionView);
                    }
                }

                invalidate();
            }
        }
    }

    public void addStripAction(@NonNull StripActionProvider provider, boolean highPriority) {
        for (View stripActionView : mStripActionViews) {
            if (stripActionView.getTag(PROVIDER_TAG_ID) == provider) {
                return;
            }
        }

        View actionView = provider.inflateActionView(this);
        if (actionView.getParent() != null)
            throw new IllegalStateException("StripActionProvider inflated a view with a parent!");
        actionView.setTag(PROVIDER_TAG_ID, provider);
        if (mShowActionStrip) {
            if (highPriority) {
                addView(actionView, FIRST_PROVIDER_VIEW_INDEX);
            } else {
                addView(actionView);
            }
        }

        if (highPriority) {
            mStripActionViews.add(0, actionView);
        } else {
            mStripActionViews.add(actionView);
        }

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
        int totalHeight = mCandidateView.getVisibility() == View.VISIBLE ? mActionStripHeight : 0;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
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

    public HorizontalScrollView getInlineScrollView() {
        if (mInlineScrollView == null) {
            mInlineScrollView = (HorizontalScrollView) getChildAt(0);
        }

        return mInlineScrollView;
    }

    public LinearLayout getInlineAutofillView() {
        if (mInlineAutofillView == null) {
            mInlineAutofillView = (LinearLayout) getInlineScrollView().getChildAt(0);
        }

        return mInlineAutofillView;
    }

    public CandidateView getCandidateView() {
        if (mCandidateView == null) {
            mCandidateView = (CandidateView) getChildAt(1);
        }
        return mCandidateView;
    }

    public InputViewBinder getStandardKeyboardView() {
        if (mStandardKeyboardView == null) {
            mStandardKeyboardView = (InputViewBinder) getChildAt(2);
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
