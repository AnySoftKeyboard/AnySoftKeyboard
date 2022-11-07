package com.anysoftkeyboard.gesturetyping;

import static org.mockito.ArgumentMatchers.eq;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GestureTypingPathDrawHelperTest {

    private static MotionEvent createMove(float x, float y) {
        return MotionEvent.obtain(10, 10, MotionEvent.ACTION_MOVE, x, y, 1, 1, 0, 1, 1, 1, 0);
    }

    private static MotionEvent createDown(float x, float y) {
        return MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, x, y, 1, 1, 0, 1, 1, 1, 0);
    }

    @Test
    public void testCreatesNoOp() {
        final AtomicInteger invalidates = new AtomicInteger();
        Assert.assertSame(
                GestureTypingPathDrawHelper.NO_OP,
                GestureTypingPathDrawHelper.create(
                        invalidates::incrementAndGet,
                        new GestureTrailTheme(
                                Color.argb(200, 60, 120, 240),
                                Color.argb(100, 30, 240, 200),
                                100f,
                                20f,
                                0)));
    }

    @Test
    public void testNoOpDoesNotInteractWithInputs() {
        final Canvas canvas = Mockito.mock(Canvas.class);
        GestureTypingPathDrawHelper.NO_OP.draw(canvas);
        Mockito.verifyZeroInteractions(canvas);

        final MotionEvent me = Mockito.mock(MotionEvent.class);
        GestureTypingPathDrawHelper.NO_OP.handleTouchEvent(me);
        Mockito.verifyZeroInteractions(me);
    }

    @Test
    public void testCreatesImpl() {
        final AtomicInteger invalidates = new AtomicInteger();
        GestureTypingPathDraw actual =
                GestureTypingPathDrawHelper.create(
                        invalidates::incrementAndGet,
                        new GestureTrailTheme(
                                Color.argb(200, 60, 120, 240),
                                Color.argb(100, 30, 240, 200),
                                100f,
                                20f,
                                10));
        Assert.assertNotSame(GestureTypingPathDrawHelper.NO_OP, actual);
        Assert.assertTrue(actual instanceof GestureTypingPathDrawHelper);
    }

    @Test
    public void testDoesNotCrashIfFirstEventIsNotDown() {
        GestureTypingPathDraw underTest =
                GestureTypingPathDrawHelper.create(
                        () -> {},
                        new GestureTrailTheme(
                                Color.argb(200, 60, 120, 240),
                                Color.argb(100, 30, 240, 200),
                                100f,
                                20f,
                                20));

        underTest.handleTouchEvent(createMove(10f, 10f));
    }

    @Test
    public void testDrawsHappyPath() {
        Canvas canvas = Mockito.mock(Canvas.class);
        final AtomicInteger invalidates = new AtomicInteger();
        GestureTypingPathDraw underTest =
                GestureTypingPathDrawHelper.create(
                        invalidates::incrementAndGet,
                        new GestureTrailTheme(
                                Color.argb(200, 60, 120, 240),
                                Color.argb(100, 30, 240, 200),
                                100f,
                                20f,
                                20));

        underTest.handleTouchEvent(createDown(10, 10));
        Assert.assertEquals(1, invalidates.get());
        underTest.draw(canvas);
        Mockito.verifyZeroInteractions(canvas);
        underTest.handleTouchEvent(createMove(10, 10));
        Assert.assertEquals(2, invalidates.get());
        underTest.handleTouchEvent(createMove(11, 11));
        Assert.assertEquals(3, invalidates.get());

        underTest.draw(canvas);
        Assert.assertEquals(4, invalidates.get());
        InOrder inOrder = Mockito.inOrder(canvas);
        inOrder.verify(canvas).drawLine(eq(11f), eq(11f), eq(10f), eq(10f), Mockito.notNull());
        inOrder.verify(canvas).drawLine(eq(10f), eq(10f), eq(10f), eq(10f), Mockito.notNull());
        inOrder.verifyNoMoreInteractions();

        Mockito.reset(canvas);
        for (int i = 0; i <= 30; i++) {
            underTest.handleTouchEvent(createMove(i, i));
        }
        underTest.draw(canvas);
        inOrder = Mockito.inOrder(canvas);
        Set<Paint> notSeen = new HashSet<>();
        for (int i = 30; i > 11; i--) {
            ArgumentCaptor<Paint> paintArgumentCaptor = ArgumentCaptor.forClass(Paint.class);
            inOrder.verify(canvas)
                    .drawLine(
                            eq((float) i),
                            eq((float) i),
                            eq((float) i - 1),
                            eq((float) i - 1),
                            paintArgumentCaptor.capture());
            Assert.assertTrue("for index " + i, notSeen.add(paintArgumentCaptor.getValue()));
        }
        inOrder.verifyNoMoreInteractions();

        Mockito.reset(canvas);
        underTest.handleTouchEvent(createMove(41, 41));
        underTest.draw(canvas);
        ArgumentCaptor<Paint> paintArgumentCaptor = ArgumentCaptor.forClass(Paint.class);
        Mockito.verify(canvas)
                .drawLine(eq(41f), eq(41f), eq(30f), eq(30f), paintArgumentCaptor.capture());
        // reused
        Assert.assertFalse(notSeen.add(paintArgumentCaptor.getValue()));
        Mockito.verify(canvas)
                .drawLine(eq(30f), eq(30f), eq(29f), eq(29f), paintArgumentCaptor.capture());
        // reused
        Assert.assertFalse(notSeen.add(paintArgumentCaptor.getValue()));
        Mockito.verify(canvas)
                .drawLine(eq(13f), eq(13f), eq(12f), eq(12f), paintArgumentCaptor.capture());
        // reused
        Assert.assertFalse(notSeen.add(paintArgumentCaptor.getValue()));
        Mockito.verify(canvas, Mockito.never())
                .drawLine(eq(12f), eq(12f), eq(11f), eq(11f), paintArgumentCaptor.capture());
        // reused
        Assert.assertFalse(notSeen.add(paintArgumentCaptor.getValue()));

        // lifting finger should reset the path
        invalidates.set(0);
        Mockito.reset(canvas);
        underTest.handleTouchEvent(createDown(100, 100));
        Assert.assertEquals(1, invalidates.get());
        underTest.draw(canvas);
        Mockito.verifyZeroInteractions(canvas);
        underTest.handleTouchEvent(createMove(100, 100));
        Assert.assertEquals(2, invalidates.get());
        underTest.handleTouchEvent(createMove(110, 110));
        Assert.assertEquals(3, invalidates.get());

        underTest.draw(canvas);
        Assert.assertEquals(4, invalidates.get());
        inOrder = Mockito.inOrder(canvas);
        inOrder.verify(canvas).drawLine(eq(110f), eq(110f), eq(100f), eq(100f), Mockito.notNull());
        inOrder.verify(canvas).drawLine(eq(100f), eq(100f), eq(100f), eq(100f), Mockito.notNull());
        inOrder.verifyNoMoreInteractions();
    }
}
