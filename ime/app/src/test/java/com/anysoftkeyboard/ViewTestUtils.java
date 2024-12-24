package com.anysoftkeyboard;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;
import androidx.test.core.view.MotionEventBuilder;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ViewTestUtils {

  public static class Finger {
    private final float mStartX;
    private final float mStartY;
    private final float mEndX;
    private final float mEndY;

    public Finger(Point start, Point end) {
      this(start.x, start.y, end.x, end.y);
    }

    public Finger(float startX, float startY, float endX, float endY) {
      mStartX = startX;
      mStartY = startY;
      mEndX = endX;
      mEndY = endY;
    }

    float getStepX(float callsToMake) {
      final float distance = mEndX - mStartX;
      return distance / callsToMake;
    }

    float getStepY(float callsToMake) {
      final float distance = mEndY - mStartY;
      return distance / callsToMake;
    }
  }

  public static Point getKeyCenterPoint(Keyboard.Key key) {
    return new Point(Keyboard.Key.getCenterX(key), Keyboard.Key.getCenterY(key));
  }

  public static int navigateFromTo(
      final View view,
      final int startX,
      final int startY,
      final int endX,
      final int endY,
      final int duration,
      final boolean alsoDown,
      final boolean alsoUp) {
    return navigateFromTo(
        view,
        Collections.singletonList(new Finger(startX, startY, endX, endY)),
        duration,
        Collections.singletonList(alsoDown),
        Collections.singletonList(alsoUp));
  }

  public static int navigateFromTo(
      final View view,
      final List<Finger> fingers,
      final int duration,
      final List<Boolean> alsoDown,
      final List<Boolean> alsoUp) {
    final long startTime = SystemClock.uptimeMillis();
    for (int fingerIndex = 0; fingerIndex < fingers.size(); fingerIndex++) {
      if (alsoDown.get(fingerIndex).equals(Boolean.TRUE)) {
        final MotionEventBuilder eventBuilder =
            MotionEventBuilder.newBuilder()
                .setAction(MotionEvent.ACTION_DOWN)
                .setActionIndex(fingerIndex)
                .setDownTime(startTime)
                .setEventTime(startTime)
                .setMetaState(0);

        for (int initialFingers = 0; initialFingers <= fingerIndex; initialFingers++) {
          final Finger finger = fingers.get(initialFingers);
          eventBuilder.setPointer(finger.mStartX, finger.mStartY);
        }
        // also adding any after pointers that are already pressed (alsoDown != true)
        for (int initialFingers = fingerIndex + 1;
            initialFingers < fingers.size();
            initialFingers++) {
          if (alsoDown.get(initialFingers).equals(Boolean.FALSE)) {
            final Finger finger = fingers.get(initialFingers);
            eventBuilder.setPointer(finger.mStartX, finger.mStartY);
          }
        }

        final MotionEvent motionEvent = eventBuilder.build();
        view.onTouchEvent(motionEvent);
        motionEvent.recycle();
      }
    }

    final float timeEventBreaking = 1000f / 60f /*60 frames per second*/;
    final float callsToMake = duration / timeEventBreaking;
    final float timeStep = duration / callsToMake;

    float currentTime = startTime;

    int callsDone = 0;
    while (currentTime < startTime + duration) {
      currentTime += timeStep;
      SystemClock.setCurrentTimeMillis((long) currentTime);
      final MotionEventBuilder eventBuilder =
          MotionEventBuilder.newBuilder()
              .setAction(MotionEvent.ACTION_MOVE)
              .setDownTime(startTime)
              .setEventTime((long) currentTime)
              .setMetaState(0);
      for (Finger finger : fingers) {
        float currentX = finger.mStartX + callsDone * finger.getStepX(callsToMake);
        float currentY = finger.mStartY + callsDone * finger.getStepY(callsToMake);
        eventBuilder.setPointer(currentX, currentY);
      }
      final MotionEvent motionEvent = eventBuilder.build();
      view.onTouchEvent(motionEvent);
      motionEvent.recycle();
      callsDone++;
    }

    int removedFingers = 0;
    for (int fingerIndex = 0; fingerIndex < fingers.size(); fingerIndex++) {
      if (alsoUp.get(fingerIndex).equals(Boolean.TRUE)) {
        final MotionEventBuilder eventBuilder =
            MotionEventBuilder.newBuilder()
                .setAction(MotionEvent.ACTION_UP)
                .setActionIndex(fingerIndex - removedFingers)
                .setDownTime(startTime)
                .setEventTime(startTime + duration)
                .setMetaState(0);

        // also adding any after pointers that are kept pressed (alsoUp != true)
        for (int initialFingers = 0; initialFingers < fingerIndex; initialFingers++) {
          if (alsoUp.get(initialFingers).equals(Boolean.FALSE)) {
            final Finger finger = fingers.get(initialFingers);
            eventBuilder.setPointer(finger.mEndX, finger.mEndY);
          }
        }
        for (int fingersLeft = fingerIndex; fingersLeft < fingers.size(); fingersLeft++) {
          final Finger finger = fingers.get(fingersLeft);
          eventBuilder.setPointer(finger.mEndX, finger.mEndY);
        }

        final MotionEvent motionEvent = eventBuilder.build();
        view.onTouchEvent(motionEvent);
        motionEvent.recycle();
        removedFingers++;
      }
    }

    return callsDone;
  }

  public static int navigateFromTo(
      final View view,
      @NonNull Point start,
      @NonNull Point end,
      final int duration,
      final boolean alsoDown,
      final boolean alsoUp) {
    return navigateFromTo(view, start.x, start.y, end.x, end.y, duration, alsoDown, alsoUp);
  }

  public static int navigateFromTo(
      @NonNull View view,
      @NonNull Keyboard.Key start,
      @NonNull Keyboard.Key end,
      final int duration,
      final boolean alsoDown,
      final boolean alsoUp) {
    return navigateFromTo(
        view, getKeyCenterPoint(start), getKeyCenterPoint(end), duration, alsoDown, alsoUp);
  }

  @NonNull
  public static Fragment navigateByClicking(Fragment rootFragment, int viewToClick) {
    final FragmentActivity activity = rootFragment.getActivity();
    final View viewById = rootFragment.getView().findViewById(viewToClick);
    Assert.assertNotNull(viewById);
    final View.OnClickListener onClickListener = Shadows.shadowOf(viewById).getOnClickListener();
    Assert.assertNotNull(onClickListener);
    onClickListener.onClick(viewById);
    TestRxSchedulers.foregroundFlushAllJobs();
    return RobolectricFragmentTestCase.getCurrentFragmentFromActivity(activity);
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
    Mockito.doAnswer(
            invocation -> {
              actions.add(new MotionEventData(invocation.getArgument(0)));
              return null;
            })
        .when(view)
        .onTouchEvent(Mockito.any());

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
      Assert.assertEquals(startTime, actions.get(i).downTime);
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
    Mockito.doAnswer(
            invocation -> {
              actions.add(new MotionEventData(invocation.getArgument(0)));
              return null;
            })
        .when(view)
        .onTouchEvent(Mockito.any());

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
    Mockito.doAnswer(
            invocation -> {
              actions.add(new MotionEventData(invocation.getArgument(0)));
              return null;
            })
        .when(view)
        .onTouchEvent(Mockito.any());

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

  @SuppressWarnings("unchecked")
  private static void assertCurrentWatermark(
      InputViewBinder view, final boolean has, @DrawableRes final int drawableRes) {
    ArgumentCaptor<List<Drawable>> watermarkCaptor = ArgumentCaptor.forClass(List.class);
    Mockito.verify(view, Mockito.atLeastOnce()).setWatermark(watermarkCaptor.capture());
    List<String> seenDrawables = new ArrayList<>();

    boolean found = false;
    for (Drawable drawable : watermarkCaptor.getValue()) {
      final int aDrawableRes = Shadows.shadowOf(drawable).getCreatedFromResId();
      if (aDrawableRes == drawableRes) {
        found = true;
      }

      seenDrawables.add(String.valueOf(aDrawableRes));
    }
    Assert.assertEquals(
        String.format(
            "Assert for Drawable with value %d failed (has = %s). Found: %s",
            drawableRes, has, String.join(",", seenDrawables)),
        has,
        found);
  }

  public static void assertZeroWatermarkInteractions(InputViewBinder view) {
    Mockito.verify(view, Mockito.never()).setWatermark(Mockito.anyList());
  }

  public static void assertCurrentWatermarkHasDrawable(
      InputViewBinder view, @DrawableRes final int drawableRes) {
    assertCurrentWatermark(view, true, drawableRes);
  }

  public static void assertCurrentWatermarkDoesNotHaveDrawable(
      InputViewBinder view, @DrawableRes final int drawableRes) {
    assertCurrentWatermark(view, false, drawableRes);
  }
}
