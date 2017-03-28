/*
 * DragSortRecycler
 *
 * Added drag and drop functionality to your RecyclerView
 *
 *
 * Copyright 2014 Emile Belanger.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emtronics.dragsortrecycler;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


public class DragSortRecycler extends RecyclerView.ItemDecoration implements OnItemTouchListener {

    private static final String TAG = "DragSortRecycler";

    private static final boolean DEBUG = false;
    private final Paint mBackgroundColor = new Paint();
    private OnItemMovedListener mMoveInterface;
    @Nullable
    private OnDragStateChangedListener mDragStateChangedListener;
    private int mDragHandleWidth = 0;
    private int mSelectedDragItemPos = -1;
    private int mFingerAnchorY;
    private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            debugLog("Scrolled: " + dx + " " + dy);
            mFingerAnchorY -= dy;
        }
    };

    private int mFingerY;
    private int mFingerOffsetInViewY;
    private float mAutoScrollWindow = 0.1f;
    private float mAutoScrollSpeed = 0.5f;
    private BitmapDrawable mFloatingItem;
    private Rect mFloatingItemStatingBounds;
    private Rect mFloatingItemBounds;
    private float mFloatingItemAlpha = 0.5f;
    private int mFloatingItemBgColor = 0;
    private int mViewHandleId = -1;
    private boolean mIsDragging;

    private void debugLog(String log) {
        if (DEBUG)
            Log.d(TAG, log);
    }

    public RecyclerView.OnScrollListener getScrollListener() {
        return mScrollListener;
    }

    /*
     * Set the item move interface
     */
    public void setOnItemMovedListener(OnItemMovedListener swif) {
        mMoveInterface = swif;
    }

    public void setViewHandleId(int id) {
        mViewHandleId = id;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView rv, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, rv, state);

        debugLog("getItemOffsets");

        debugLog("View top = " + view.getTop());
        if (mSelectedDragItemPos != -1) {
            int itemPos = rv.getChildPosition(view);
            debugLog("itemPos =" + itemPos);

            if (!canDragOver(itemPos)) {
                return;
            }

            if (itemPos == mSelectedDragItemPos) {
                view.setVisibility(View.INVISIBLE);
            } else {
                //Make view visible incase invisible
                view.setVisibility(View.VISIBLE);

                //Find middle of the mFloatingItem
                float floatMiddleY = mFloatingItemBounds.top + mFloatingItemBounds.height() / 2;

                //Moving down the list
                //These will auto-animate if the device continually sends touch motion events
                if ((itemPos > mSelectedDragItemPos) && (view.getTop() < floatMiddleY)) {
                    float amountUp = (floatMiddleY - view.getTop()) / (float) view.getHeight();
                    //  amountUp *= 0.5f;
                    if (amountUp > 1)
                        amountUp = 1;

                    outRect.top = -(int) (mFloatingItemBounds.height() * amountUp);
                    outRect.bottom = (int) (mFloatingItemBounds.height() * amountUp);
                }
                if ((itemPos < mSelectedDragItemPos) && (view.getBottom() > floatMiddleY)) {
                    float amountDown = ((float) view.getBottom() - floatMiddleY) / (float) view.getHeight();
                    //  amountDown *= 0.5f;
                    if (amountDown > 1)
                        amountDown = 1;

                    outRect.top = (int) (mFloatingItemBounds.height() * amountDown);
                    outRect.bottom = -(int) (mFloatingItemBounds.height() * amountDown);
                }
            }
        } else {
            outRect.top = 0;
            outRect.bottom = 0;
            //Make view visible in case invisible
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Find the new position by scanning through the items on
     * screen and finding the positional relationship.
     * This *seems* to work, another method would be to use
     * getItemOffsets, but I think that could miss items?..
     */
    private int getNewPostion(RecyclerView rv) {
        int itemsOnScreen = rv.getLayoutManager().getChildCount();

        float floatMiddleY = mFloatingItemBounds.top + mFloatingItemBounds.height() / 2;

        int above = 0;
        int below = Integer.MAX_VALUE;
        for (int n = 0; n < itemsOnScreen; n++) //Scan though items on screen, however they may not
        {                                   // be in order!

            View view = rv.getLayoutManager().getChildAt(n);

            if (view.getVisibility() != View.VISIBLE)
                continue;

            int itemPos = rv.getChildPosition(view);

            if (itemPos == mSelectedDragItemPos) //Don't check against itself!
                continue;

            float viewMiddleY = view.getTop() + view.getHeight() / 2;
            if (floatMiddleY > viewMiddleY) //Is above this item
            {
                if (itemPos > above)
                    above = itemPos;
            } else if (floatMiddleY <= viewMiddleY) //Is below this item
            {
                if (itemPos < below)
                    below = itemPos;
            }
        }
        debugLog("above = " + above + " below = " + below);

        if (below != Integer.MAX_VALUE) {
            if (below < mSelectedDragItemPos) //Need to count itself
                below++;
            return below - 1;
        } else {
            if (above < mSelectedDragItemPos)
                above++;

            return above;
        }
    }


    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        debugLog("onInterceptTouchEvent");

        //if (e.getAction() == MotionEvent.ACTION_DOWN)
        {
            View itemView = rv.findChildViewUnder(e.getX(), e.getY());

            if (itemView == null)
                return false;

            boolean dragging = false;

            if ((mDragHandleWidth > 0) && (e.getX() < mDragHandleWidth)) {
                dragging = true;
            } else if (mViewHandleId != -1) {
                //Find the handle in the list item
                View handleView = itemView.findViewById(mViewHandleId);

                if (handleView == null) {
                    Log.e(TAG, "The view ID " + mViewHandleId + " was not found in the RecycleView item");
                    return false;
                }

                //View should be visible to drag
                if (handleView.getVisibility() != View.VISIBLE) {
                    return false;
                }

                //We need to find the relative position of the handle to the mParent view
                //Then we can work out if the touch is within the handle
                int[] parentItemPos = new int[2];
                itemView.getLocationInWindow(parentItemPos);

                int[] handlePos = new int[2];
                handleView.getLocationInWindow(handlePos);

                int xRel = handlePos[0] - parentItemPos[0];
                int yRel = handlePos[1] - parentItemPos[1];

                Rect touchBounds = new Rect(itemView.getLeft() + xRel, itemView.getTop() + yRel,
                        itemView.getLeft() + xRel + handleView.getWidth(),
                        itemView.getTop() + yRel + handleView.getHeight()
                );

                if (touchBounds.contains((int) e.getX(), (int) e.getY()))
                    dragging = true;

                debugLog("parentItemPos = " + parentItemPos[0] + " " + parentItemPos[1]);
                debugLog("handlePos = " + handlePos[0] + " " + handlePos[1]);
            }


            if (dragging) {
                debugLog("Started Drag");

                setIsDragging(true);

                mFloatingItem = createFloatingBitmap(itemView);

                mFingerAnchorY = (int) e.getY();
                mFingerOffsetInViewY = mFingerAnchorY - itemView.getTop();
                mFingerY = mFingerAnchorY;

                mSelectedDragItemPos = rv.getChildPosition(itemView);
                debugLog("mSelectedDragItemPos = " + mSelectedDragItemPos);

                return true;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        debugLog("onTouchEvent");

        if ((e.getAction() == MotionEvent.ACTION_UP) ||
                (e.getAction() == MotionEvent.ACTION_CANCEL)) {
            if ((e.getAction() == MotionEvent.ACTION_UP) && mSelectedDragItemPos != -1) {
                int newPos = getNewPostion(rv);
                if (mMoveInterface != null)
                    mMoveInterface.onItemMoved(rv, mSelectedDragItemPos, newPos);
            }

            setIsDragging(false);
            mSelectedDragItemPos = -1;
            mFloatingItem = null;
            rv.invalidateItemDecorations();
            return;
        }


        mFingerY = (int) e.getY();

        if (mFloatingItem != null) {
            mFloatingItemBounds.top = mFingerY - mFingerOffsetInViewY;

            if (mFloatingItemBounds.top < -mFloatingItemStatingBounds.height() / 2) //Allow half the view out the top
                mFloatingItemBounds.top = -mFloatingItemStatingBounds.height() / 2;

            mFloatingItemBounds.bottom = mFloatingItemBounds.top + mFloatingItemStatingBounds.height();

            mFloatingItem.setBounds(mFloatingItemBounds);
        }

        //Do auto scrolling at end of list
        float scrollAmount = 0;
        if (mFingerY > (rv.getHeight() * (1 - mAutoScrollWindow))) {
            scrollAmount = (mFingerY - (rv.getHeight() * (1 - mAutoScrollWindow)));
        } else if (mFingerY < (rv.getHeight() * mAutoScrollWindow)) {
            scrollAmount = (mFingerY - (rv.getHeight() * mAutoScrollWindow));
        }
        debugLog("Scroll: " + scrollAmount);

        scrollAmount *= mAutoScrollSpeed;
        rv.scrollBy(0, (int) scrollAmount);

        rv.invalidateItemDecorations();// Redraw
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    private void setIsDragging(final boolean dragging) {
        if (dragging != mIsDragging) {
            mIsDragging = dragging;
            if (mDragStateChangedListener != null) {
                if (mIsDragging) {
                    mDragStateChangedListener.onDragStart();
                } else {
                    mDragStateChangedListener.onDragStop();
                }
            }
        }
    }

    public void setOnDragStateChangedListener(final OnDragStateChangedListener dragStateChangedListener) {
        this.mDragStateChangedListener = dragStateChangedListener;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mFloatingItem != null) {
            mFloatingItem.setAlpha((int) (255 * mFloatingItemAlpha));
            mBackgroundColor.setColor(mFloatingItemBgColor);
            c.drawRect(mFloatingItemBounds, mBackgroundColor);
            mFloatingItem.draw(c);
        }
    }

    /**
     * @param position
     * @return True if we can drag the item over this position, False if not.
     */
    protected boolean canDragOver(int position) {
        return true;
    }

    private BitmapDrawable createFloatingBitmap(View v) {
        mFloatingItemStatingBounds = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        mFloatingItemBounds = new Rect(mFloatingItemStatingBounds);

        Bitmap bitmap = Bitmap.createBitmap(mFloatingItemStatingBounds.width(),
                mFloatingItemStatingBounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);

        BitmapDrawable retDrawable = new BitmapDrawable(v.getResources(), bitmap);
        retDrawable.setBounds(mFloatingItemBounds);

        return retDrawable;
    }

    public interface OnItemMovedListener {
        void onItemMoved(RecyclerView rv, int from, int to);
    }


    public interface OnDragStateChangedListener {
        void onDragStart();

        void onDragStop();
    }
}