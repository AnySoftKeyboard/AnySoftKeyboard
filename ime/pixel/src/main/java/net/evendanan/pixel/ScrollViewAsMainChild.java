package net.evendanan.pixel;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

public class ScrollViewAsMainChild extends ScrollView implements MainChild {
  private LinearLayout mItemsHolder;
  private View mBottomGap;

  public ScrollViewAsMainChild(Context context) {
    super(context);
    init();
  }

  public ScrollViewAsMainChild(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ScrollViewAsMainChild(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  public ScrollViewAsMainChild(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    inflate(getContext(), R.layout.scroll_view_internal, this);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mItemsHolder = findViewById(R.id.inner_layout);
    mBottomGap = findViewById(R.id.bottom_gap_view);

    // okay.. this is super weird:
    // Since the items in the list are remote-views, they are drawn on top of our UI.
    // this means that they think that itemsContainer is very large and so they
    // draw themselves outside the scroll window.
    // The only nice why I found to deal with this is to set them to INVISIBLE
    // when they scroll out of view.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      setOnScrollChangeListener(
          (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int childIndex;
            // hiding all the children that above the scroll Y
            for (childIndex = 0; childIndex < mItemsHolder.getChildCount(); childIndex++) {
              var child = mItemsHolder.getChildAt(childIndex);
              child.setScaleX(1f);
              child.setScaleY(1f);
              if (child.getY() < scrollY) child.setVisibility(View.INVISIBLE);
              else break;
            }

            final int topItemIndex = childIndex;

            for (
            /*childIndex holds the first visible child*/ ;
                childIndex < mItemsHolder.getChildCount();
                childIndex++) {
              var child = mItemsHolder.getChildAt(childIndex);
              child.setVisibility(View.VISIBLE);
              child.setScaleX(1f);
              child.setScaleY(1f);
            }
            // how much do we need to scale-down the top item
            final var topVisibleChild = mItemsHolder.getChildAt(topItemIndex);
            final int visiblePartPixels = scrollY - topVisibleChild.getHeight();
            float scaleFactor = ((float) visiblePartPixels) / topVisibleChild.getHeight();
            topVisibleChild.setScaleX(scaleFactor);
            topVisibleChild.setScaleY(scaleFactor);
            topVisibleChild.setPivotY(visiblePartPixels);
          });
    }
  }

  public void addListItem(@NonNull View view) {
    // this will insert the new view above the bottom gap view
    mItemsHolder.addView(view, mItemsHolder.getChildCount() - 1);

    requestLayout();
  }

  public void removeAllListItems() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      setOnScrollChangeListener(null);
    }
    while (mItemsHolder.getChildCount() > 1) {
      mItemsHolder.removeViewAt(0);
    }

    requestLayout();
  }

  @Override
  public void setBottomOffset(int offset) {
    // the extra padding is a child at the end of the list
    var lp = mBottomGap.getLayoutParams();
    lp.height = offset;
    mBottomGap.setLayoutParams(lp);
  }

  public int getItemsCount() {
    return mItemsHolder.getChildCount() - 1;
  }
}
