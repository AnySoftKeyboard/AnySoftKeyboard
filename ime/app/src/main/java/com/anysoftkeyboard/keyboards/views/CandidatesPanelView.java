/*
 * Copyright (c) 2026 AnySoftKeyboard contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;

/**
 * A panel with all candidates in a scrollable grid, shown in place of the keyboard. Tapping a
 * candidate reports its strip index; the button at the top closes the panel.
 */
public class CandidatesPanelView extends LinearLayout {

  /** Receives the user's choice. */
  public interface Listener {
    void onCandidatePicked(int index, @NonNull CharSequence candidate);

    void onCandidatesPanelCloseRequested();
  }

  private final FlowLayout mGrid;
  private final ImageView mClose;
  @Nullable private Listener mListener;
  private int mTextColor;
  private float mTextSizePx;

  public CandidatesPanelView(@NonNull Context context) {
    super(context);
    setId(R.id.candidates_panel_root);
    setOrientation(VERTICAL);

    mClose = new ImageView(context);
    mClose.setImageResource(R.drawable.ic_candidates_collapse);
    mClose.setScaleType(ImageView.ScaleType.FIT_CENTER);
    mClose.setContentDescription(context.getText(R.string.candidates_panel_collapse));
    // 64 x 48 dp with a 32 dp icon: a real touch target, not just the glyph
    mClose.setPadding(dp(16), dp(8), dp(16), dp(8));
    mClose.setOnClickListener(
        v -> {
          if (mListener != null) mListener.onCandidatesPanelCloseRequested();
        });
    final LayoutParams closeParams = new LayoutParams(dp(64), dp(48));
    closeParams.gravity = Gravity.END;
    addView(mClose, closeParams);

    mGrid = new FlowLayout(context);
    final ScrollView scroll = new ScrollView(context);
    scroll.addView(
        mGrid,
        new ScrollView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    addView(scroll, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
  }

  public void setListener(@Nullable Listener listener) {
    mListener = listener;
  }

  /** Applies the keyboard's colors so the panel matches the current theme. */
  public void setThemeValues(int textColor, float textSizePx, @Nullable Drawable background) {
    mTextColor = textColor;
    mTextSizePx = textSizePx;
    mClose.setColorFilter(textColor);
    setBackground(background);
  }

  public void setCandidates(@NonNull List<? extends CharSequence> candidates) {
    mGrid.removeAllViews();
    final int horizontalPadding = dp(14);
    final int verticalPadding = dp(10);
    for (int i = 0; i < candidates.size(); i++) {
      final int index = i;
      final CharSequence candidate = candidates.get(i);
      final TextView item = new TextView(getContext());
      item.setText(candidate);
      item.setTextColor(mTextColor);
      item.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSizePx);
      item.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
      item.setOnClickListener(
          v -> {
            if (mListener != null) mListener.onCandidatePicked(index, candidate);
          });
      mGrid.addView(item);
    }
  }

  private int dp(int value) {
    return Math.round(value * getResources().getDisplayMetrics().density);
  }

  /** Lays children out left to right, wrapping into rows. */
  static class FlowLayout extends ViewGroup {
    FlowLayout(Context context) {
      super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
      final int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
      final int childSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
      int x = 0;
      int y = 0;
      int rowHeight = 0;
      for (int i = 0; i < getChildCount(); i++) {
        final View child = getChildAt(i);
        child.measure(childSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        if (x > 0 && x + child.getMeasuredWidth() > maxWidth) {
          x = 0;
          y += rowHeight;
          rowHeight = 0;
        }
        x += child.getMeasuredWidth();
        rowHeight = Math.max(rowHeight, child.getMeasuredHeight());
      }
      setMeasuredDimension(maxWidth, resolveSize(y + rowHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      final int maxWidth = r - l;
      int x = 0;
      int y = 0;
      int rowHeight = 0;
      for (int i = 0; i < getChildCount(); i++) {
        final View child = getChildAt(i);
        final int w = child.getMeasuredWidth();
        final int h = child.getMeasuredHeight();
        if (x > 0 && x + w > maxWidth) {
          x = 0;
          y += rowHeight;
          rowHeight = 0;
        }
        child.layout(x, y, x + w, y + h);
        x += w;
        rowHeight = Math.max(rowHeight, h);
      }
    }
  }
}
