package com.anysoftkeyboard;

import android.graphics.Point;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.view.MotionEvent;
import android.view.View;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowSystemClock;

import java.util.ArrayList;
import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ViewTestUtils {

    public static Point getKeyCenterPoint(Keyboard.Key key) {
        return new Point(key.gap + key.x + key.width / 2, key.y + key.height / 2);
    }

    public static int navigateFromTo(final View view, final int startX, final int startY, final int endX, final int endY, final int duration, final boolean alsoDown, final boolean alsoUp) {
        final long startTime = SystemClock.uptimeMillis();
        MotionEvent motionEvent = MotionEvent.obtain(startTime, startTime, MotionEvent.ACTION_DOWN, startX, startY, 0);
        if (alsoDown) {
            view.onTouchEvent(motionEvent);
        }
        motionEvent.recycle();

        final float timeEventBreaking = 1000f / 60f/*60 frames per second*/;
        final float callsToMake = duration / timeEventBreaking;

        final float xDistance = endX - startX;
        final float yDistance = endY - startY;

        final float xStep = xDistance / callsToMake;
        final float yStep = yDistance / callsToMake;
        final float timeStep = duration / callsToMake;

        float currentX = startX;
        float currentY = startY;
        float currentTime = startTime;

        int callsDone = 0;
        while (currentTime < startTime + duration) {
            currentX += xStep;
            currentY += yStep;
            currentTime += timeStep;
            ShadowSystemClock.setCurrentTimeMillis((long) currentTime);
            motionEvent = MotionEvent.obtain(startTime, (long) currentTime, MotionEvent.ACTION_MOVE, currentX, currentY, 0);
            view.onTouchEvent(motionEvent);
            motionEvent.recycle();
            callsDone++;
        }

        if (alsoUp) {
            motionEvent = MotionEvent.obtain(startTime, startTime + duration, MotionEvent.ACTION_UP, endX, endY, 0);
            view.onTouchEvent(motionEvent);
            motionEvent.recycle();
        }

        return callsDone;
    }

    public static int navigateFromTo(final View view, Point start, Point end, final int duration, final boolean alsoDown, final boolean alsoUp) {
        return navigateFromTo(view, start.x, start.y, end.x, end.y, duration, alsoDown, alsoUp);
    }

    public static int navigateFromTo(final View view, Keyboard.Key start, Keyboard.Key end, final int duration, final boolean alsoDown, final boolean alsoUp) {
        return navigateFromTo(view, getKeyCenterPoint(start), getKeyCenterPoint(end), duration, alsoDown, alsoUp);
    }

    @NonNull
    public static Fragment navigateByClicking(Fragment rootFragment, int viewToClick) {
        final View viewById = rootFragment.getView().findViewById(viewToClick);
        Assert.assertNotNull(viewById);
        final View.OnClickListener onClickListener = Shadows.shadowOf(viewById).getOnClickListener();
        Assert.assertNotNull(onClickListener);
        onClickListener.onClick(viewById);
        Robolectric.flushForegroundThreadScheduler();
        return rootFragment.getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
    }

    private static class MotionEventData {
        public final int action;
        public final float x;
        public final float y;
        public final long eventTime;
        public final long downTime;


        private MotionEventData(MotionEvent event) {
            action = event.getAction();
            x = event.getX();
            y = event.getY();
            eventTime = event.getEventTime();
            downTime = event.getDownTime();
        }
    }

    @Test
    public void testNavigateFromToHelpMethod() {
        View view = Mockito.mock(View.class);

        final List<MotionEventData> actions = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            actions.add(new MotionEventData(invocation.getArgument(0)));
            return null;
        }).when(view).onTouchEvent(Mockito.any());

        final long startTime = SystemClock.uptimeMillis();
        navigateFromTo(view, 10, 15, 100, 150, 200, true, true);

        Assert.assertEquals(14, actions.size());

        Assert.assertEquals(MotionEvent.ACTION_DOWN, actions.get(0).action);
        Assert.assertEquals(10f, actions.get(0).x, 0.01f);
        Assert.assertEquals(15f, actions.get(0).y, 0.01f);
        Assert.assertEquals(startTime, actions.get(0).eventTime);
        Assert.assertEquals(startTime, actions.get(0).downTime);

        for (int i = 1; i < actions.size() - 1; i++) {
            Assert.assertEquals(MotionEvent.ACTION_MOVE, actions.get(i).action);
            Assert.assertEquals(startTime, (int) actions.get(i).downTime);
            Assert.assertNotEquals(startTime, actions.get(i).eventTime);
        }

        Assert.assertEquals(MotionEvent.ACTION_UP, actions.get(actions.size() - 1).action);
        Assert.assertEquals(100, actions.get(actions.size() - 1).x, 0.01f);
        Assert.assertEquals(150, actions.get(actions.size() - 1).y, 0.01f);
        Assert.assertEquals(200 + startTime, actions.get(actions.size() - 1).eventTime);
        Assert.assertEquals(startTime, actions.get(actions.size() - 1).downTime);
    }

    @Test
    public void testNavigateFromToHelpMethodNoDown() {
        final View view = Mockito.mock(View.class);
        final List<MotionEventData> actions = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            actions.add(new MotionEventData(invocation.getArgument(0)));
            return null;
        }).when(view).onTouchEvent(Mockito.any());

        navigateFromTo(view, 10, 15, 100, 150, 200, false, true);

        Assert.assertEquals(13, actions.size());

        for (int i = 0; i < actions.size() - 1; i++) {
            Assert.assertEquals(MotionEvent.ACTION_MOVE, actions.get(i).action);
        }
        Assert.assertEquals(MotionEvent.ACTION_UP, actions.get(actions.size() - 1).action);
    }

    @Test
    public void testNavigateFromToHelpMethodNoUp() {
        final View view = Mockito.mock(View.class);
        final List<MotionEventData> actions = new ArrayList<>();
        Mockito.doAnswer(invocation -> {
            actions.add(new MotionEventData(invocation.getArgument(0)));
            return null;
        }).when(view).onTouchEvent(Mockito.any());

        navigateFromTo(view, 10, 15, 100, 150, 200, true, false);

        Assert.assertEquals(13, actions.size());

        Assert.assertEquals(MotionEvent.ACTION_DOWN, actions.get(0).action);
        for (int i = 1; i < actions.size(); i++) {
            Assert.assertEquals(MotionEvent.ACTION_MOVE, actions.get(i).action);
        }
    }

    @Test
    public void testNavigateFromToHelpMethodTimeProgress() {
        final long startTime = SystemClock.uptimeMillis();
        View view = Mockito.mock(View.class);
        navigateFromTo(view, 10, 15, 100, 150, 200, false, false);

        Assert.assertEquals(startTime + 200, SystemClock.uptimeMillis());
    }

    @SuppressWarnings("RestrictTo")
    public static void performClick(Preference preference) {
        preference.performClick();
    }
}
