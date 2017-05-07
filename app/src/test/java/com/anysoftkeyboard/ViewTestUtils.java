package com.anysoftkeyboard;

import android.graphics.Point;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import com.anysoftkeyboard.keyboards.Keyboard;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.shadows.ShadowSystemClock;

import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
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

    @Test
    public void testNavigateFromToHelpMethod() {
        View view = Mockito.mock(View.class);
        final long startTime = SystemClock.uptimeMillis();
        navigateFromTo(view, 10, 15, 100, 150, 200, true, true);

        ArgumentCaptor<MotionEvent> motionEventArgumentCaptor = ArgumentCaptor.forClass(MotionEvent.class);
        Mockito.verify(view, Mockito.times(14)).onTouchEvent(motionEventArgumentCaptor.capture());

        final List<MotionEvent> allMotionEvents = motionEventArgumentCaptor.getAllValues();

        Assert.assertEquals(14, allMotionEvents.size());

        Assert.assertEquals(MotionEvent.ACTION_DOWN, allMotionEvents.get(0).getAction());
        Assert.assertEquals(10, (int) allMotionEvents.get(0).getX());
        Assert.assertEquals(15, (int) allMotionEvents.get(0).getY());
        Assert.assertEquals(startTime, allMotionEvents.get(0).getEventTime());
        Assert.assertEquals(startTime, allMotionEvents.get(0).getDownTime());

        for (int i = 1; i < allMotionEvents.size() - 1; i++) {
            Assert.assertEquals(MotionEvent.ACTION_MOVE, allMotionEvents.get(i).getAction());
            Assert.assertEquals(startTime, (int) allMotionEvents.get(i).getDownTime());
            Assert.assertNotEquals(startTime, allMotionEvents.get(i).getEventTime());
        }

        Assert.assertEquals(MotionEvent.ACTION_UP, allMotionEvents.get(allMotionEvents.size() - 1).getAction());
        Assert.assertEquals(100, (int) allMotionEvents.get(allMotionEvents.size() - 1).getX());
        Assert.assertEquals(150, (int) allMotionEvents.get(allMotionEvents.size() - 1).getY());
        Assert.assertEquals(200 + startTime, (int) allMotionEvents.get(allMotionEvents.size() - 1).getEventTime());
        Assert.assertEquals(startTime, (int) allMotionEvents.get(allMotionEvents.size() - 1).getDownTime());
    }

    @Test
    public void testNavigateFromToHelpMethodNoDown() {
        View view = Mockito.mock(View.class);
        navigateFromTo(view, 10, 15, 100, 150, 200, false, true);

        ArgumentCaptor<MotionEvent> motionEventArgumentCaptor = ArgumentCaptor.forClass(MotionEvent.class);
        Mockito.verify(view, Mockito.times(13)).onTouchEvent(motionEventArgumentCaptor.capture());

        final List<MotionEvent> allMotionEvents = motionEventArgumentCaptor.getAllValues();


        for (int i = 0; i < allMotionEvents.size() - 1; i++) {
            Assert.assertEquals(MotionEvent.ACTION_MOVE, allMotionEvents.get(i).getAction());
        }
        Assert.assertEquals(MotionEvent.ACTION_UP, allMotionEvents.get(allMotionEvents.size() - 1).getAction());
    }

    @Test
    public void testNavigateFromToHelpMethodNoUp() {
        View view = Mockito.mock(View.class);
        navigateFromTo(view, 10, 15, 100, 150, 200, true, false);

        ArgumentCaptor<MotionEvent> motionEventArgumentCaptor = ArgumentCaptor.forClass(MotionEvent.class);
        Mockito.verify(view, Mockito.times(13)).onTouchEvent(motionEventArgumentCaptor.capture());

        final List<MotionEvent> allMotionEvents = motionEventArgumentCaptor.getAllValues();

        Assert.assertEquals(MotionEvent.ACTION_DOWN, allMotionEvents.get(0).getAction());
        for (int i = 1; i < allMotionEvents.size(); i++) {
            Assert.assertEquals(MotionEvent.ACTION_MOVE, allMotionEvents.get(i).getAction());
        }
    }

    @Test
    public void testNavigateFromToHelpMethodTimeProgress() {
        final long startTime = SystemClock.uptimeMillis();
        View view = Mockito.mock(View.class);
        navigateFromTo(view, 10, 15, 100, 150, 200, false, false);

        Assert.assertEquals(startTime + 200, SystemClock.uptimeMillis());
    }
}
