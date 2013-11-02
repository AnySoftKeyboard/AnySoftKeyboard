package com.anysoftkeyboard.ui.tutorials;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created by menny on 10/24/13.
 */
public class TipLayoutsSupport {
    private static final String TAG = "TipLayoutsSupport";

    public static void getAvailableTipsLayouts(Context appContext, List<Integer> layoutsToShow) {
        layoutsToShow.clear();
        Resources res = appContext.getResources();
        int currentTipLoadingIndex = 1;
        boolean haveMore = true;
        while (haveMore) {
            final String layoutResourceName = "tip_layout_" + currentTipLoadingIndex;
            Log.d(TAG, "Looking for tip " + layoutResourceName);
            final int resId = res.getIdentifier(layoutResourceName, "layout", appContext.getPackageName());
            haveMore = (resId != 0);
            if (resId != 0) {
                layoutsToShow.add(Integer.valueOf(resId));
            }
            currentTipLoadingIndex++;
        }
    }

    public static void filterOutViewedTips(Resources res, List<Integer> tipResIds, SharedPreferences appPrefs) {
        int tipIndex = 0;
        while(tipIndex < tipResIds.size()) {
            String layoutName = res.getResourceName(tipResIds.get(tipIndex));
            if (appPrefs.getBoolean(layoutName, false)) {
                //the tip was shown, I can remove
                tipResIds.remove(tipIndex);
            } else {
                tipIndex++;
            }
        }
    }

    public static void addTipToCandidate(final Context appContext, @Nonnull final TextView tipsNotification, @Nonnull final String TIPS_NOTIFICATION_KEY, final View.OnClickListener onClickListener) {
        if (AnyApplication.getConfig().hasNotificationClicked(TIPS_NOTIFICATION_KEY)) {
            tipsNotification.setVisibility(View.GONE);
            ViewGroup p = tipsNotification.getParent() instanceof ViewGroup? (ViewGroup)tipsNotification.getParent() : null;
            if (p != null)
                p.removeView(tipsNotification);// removing for memory releasing
        }

        tipsNotification.setVisibility(View.VISIBLE);
        if (!AnyApplication.getConfig().hasNotificationAnimated(TIPS_NOTIFICATION_KEY)) {
            Log.d(TAG, "Tip with key "+TIPS_NOTIFICATION_KEY+" has not been animated before.");
            Animation tipsInAnimation = AnimationUtils.loadAnimation(appContext, R.anim.tips_flip_in);
            tipsInAnimation.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    tipsNotification.setText("?");
                }
            });
            tipsNotification.setAnimation(tipsInAnimation);
            AnyApplication.getConfig().setNotificationAnimated(TIPS_NOTIFICATION_KEY);
        } else {
            tipsNotification.setText("?");
            Log.d(TAG, "Tip with key "+TIPS_NOTIFICATION_KEY+" WAS animated before.");
        }

        //setting click listener.
        tipsNotification.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {
                AnyApplication.getConfig().setNotificationClicked(TIPS_NOTIFICATION_KEY);
                Animation gone = AnimationUtils.loadAnimation(
                        appContext, R.anim.tips_flip_out);
                gone.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        tipsNotification.setVisibility(View.GONE);
                        if (onClickListener != null)
                            onClickListener.onClick(null);
                    }
                });
                tipsNotification.startAnimation(gone);
            }
        });
    }
}
