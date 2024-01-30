package com.anysoftkeyboard.keyboards.views;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.mockito.ArgumentMatchers.any;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyboardViewContainerViewTest {

  private KeyboardViewContainerView mUnderTest;

  @Before
  public void setup() {
    mUnderTest =
        (KeyboardViewContainerView)
            LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.main_keyboard_layout, null, false);
    Assert.assertNotNull(mUnderTest.getCandidateView());
    Assert.assertNotNull(mUnderTest.getStandardKeyboardView());
  }

  @Test
  public void testDefaultInflation() {
    Assert.assertEquals(2, mUnderTest.getChildCount());
    Assert.assertTrue(mUnderTest.getChildAt(0) instanceof CandidateView);
    Assert.assertTrue(mUnderTest.getChildAt(1) instanceof AnyKeyboardView);
  }

  @Test
  public void testSettingPadding() {
    AnyKeyboardView mock = Mockito.mock(AnyKeyboardView.class);
    mUnderTest.addView(mock);
    Mockito.verify(mock).setBottomOffset(0);

    View mockRegular = Mockito.mock(View.class);
    mUnderTest.addView(mockRegular);

    mUnderTest.setBottomPadding(10);
    Mockito.verify(mock).setBottomOffset(10);

    AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);
    mUnderTest.addView(mock2);
    Mockito.verify(mock2).setBottomOffset(10);
  }

  @Test
  public void testAddView() {
    AnyKeyboardView mock = Mockito.mock(AnyKeyboardView.class);
    mUnderTest.addView(mock);

    Assert.assertEquals(3, mUnderTest.getChildCount());
    Assert.assertSame(mock, mUnderTest.getChildAt(2));

    Mockito.verify(mock, Mockito.never()).setKeyboardTheme(any());
    Mockito.verify(mock, Mockito.never()).setThemeOverlay(any());
  }

  @Test
  public void testAddViewWhenHasThemeWasSet() {
    mUnderTest.setKeyboardTheme(
        AnyApplication.getKeyboardThemeFactory(ApplicationProvider.getApplicationContext())
            .getEnabledAddOn());
    AnyKeyboardView mock = Mockito.mock(AnyKeyboardView.class);
    mUnderTest.addView(mock);

    Assert.assertEquals(3, mUnderTest.getChildCount());
    Assert.assertSame(mock, mUnderTest.getChildAt(2));

    Mockito.verify(mock).setKeyboardTheme(any());
    Mockito.verify(mock).setThemeOverlay(any());
  }

  @Test
  public void testSetOnKeyboardActionListener() {
    AnyKeyboardView mock1 = Mockito.mock(AnyKeyboardView.class);
    AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);

    mUnderTest.removeAllViews();

    mUnderTest.addView(mock1);

    Mockito.verify(mock1, Mockito.never())
        .setOnKeyboardActionListener(any(OnKeyboardActionListener.class));

    final OnKeyboardActionListener listener = Mockito.mock(OnKeyboardActionListener.class);

    mUnderTest.setOnKeyboardActionListener(listener);

    Mockito.verify(mock1).setOnKeyboardActionListener(listener);

    mUnderTest.addView(mock2);

    Mockito.verify(mock2).setOnKeyboardActionListener(listener);
  }

  @Test
  public void testGetStandardKeyboardView() {
    final InputViewBinder originalView = mUnderTest.getStandardKeyboardView();
    Assert.assertNotNull(originalView);
    Assert.assertTrue(originalView instanceof AnyKeyboardView);

    AnyKeyboardView mock1 = Mockito.mock(AnyKeyboardView.class);
    AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);

    mUnderTest.addView(mock1);
    mUnderTest.addView(mock2);

    Assert.assertSame(originalView, mUnderTest.getStandardKeyboardView());
  }

  @Test
  public void testGetCandidateView() {
    final CandidateView originalView = mUnderTest.getCandidateView();
    Assert.assertNotNull(originalView);

    AnyKeyboardView mock2 = Mockito.mock(AnyKeyboardView.class);

    mUnderTest.addView(mock2);

    Assert.assertSame(originalView, mUnderTest.getCandidateView());

    mUnderTest.removeView(mock2);

    Assert.assertSame(originalView, mUnderTest.getCandidateView());
  }

  @Test
  public void testCandidateThemeSet() {
    final CandidateView originalView = mUnderTest.getCandidateView();
    Assert.assertNotNull(originalView);
    final KeyboardThemeFactory keyboardThemeFactory =
        AnyApplication.getKeyboardThemeFactory(getApplicationContext());

    // switching to light icon
    keyboardThemeFactory.setAddOnEnabled("18c558ef-bc8c-433a-a36e-92c3ca3be4dd", true);
    mUnderTest.setKeyboardTheme(keyboardThemeFactory.getEnabledAddOn());
    final Drawable lightIcon = originalView.getCloseIcon();
    Assert.assertNotNull(lightIcon);
    Assert.assertEquals(
        R.drawable.close_suggestions_light, Shadows.shadowOf(lightIcon).getCreatedFromResId());

    // switching to dark icon
    keyboardThemeFactory.setAddOnEnabled("8774f99e-fb4a-49fa-b8d0-4083f762250a", true);
    mUnderTest.setKeyboardTheme(keyboardThemeFactory.getEnabledAddOn());
    final Drawable darkIcon = originalView.getCloseIcon();
    Assert.assertNotNull(darkIcon);
    Assert.assertEquals(
        R.drawable.yochees_dark_close_suggetions, Shadows.shadowOf(darkIcon).getCreatedFromResId());
  }

  @Test
  public void testAddRemoveAction() {
    View view = new View(mUnderTest.getContext());
    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());

    mUnderTest.addStripAction(provider, false);

    Mockito.verify(provider).inflateActionView(mUnderTest);
    Mockito.verify(provider, Mockito.never()).onRemoved();
    Assert.assertEquals(3, mUnderTest.getChildCount());
    Assert.assertSame(view, mUnderTest.getChildAt(2));

    mUnderTest.removeStripAction(provider);
    Mockito.verify(provider).onRemoved();
    Assert.assertEquals(2, mUnderTest.getChildCount());
  }

  @Test
  public void testHighPriority() {
    View view = new View(mUnderTest.getContext());
    View viewHigh = new View(mUnderTest.getContext());
    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());
    KeyboardViewContainerView.StripActionProvider providerHigh =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(viewHigh).when(providerHigh).inflateActionView(any());

    mUnderTest.addStripAction(provider, false);
    Assert.assertEquals(3, mUnderTest.getChildCount());
    Assert.assertSame(view, mUnderTest.getChildAt(2));

    mUnderTest.addStripAction(providerHigh, true);
    Assert.assertEquals(4, mUnderTest.getChildCount());
    Assert.assertSame(viewHigh, mUnderTest.getChildAt(2));
    Assert.assertSame(view, mUnderTest.getChildAt(3));
  }

  @Test
  public void testHigh2Priority() {
    View view = new View(mUnderTest.getContext());
    View viewHigh = new View(mUnderTest.getContext());
    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());
    KeyboardViewContainerView.StripActionProvider providerHigh =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(viewHigh).when(providerHigh).inflateActionView(any());

    mUnderTest.addStripAction(providerHigh, true);
    Assert.assertEquals(3, mUnderTest.getChildCount());
    Assert.assertSame(viewHigh, mUnderTest.getChildAt(2));

    mUnderTest.addStripAction(provider, false);
    Assert.assertEquals(4, mUnderTest.getChildCount());
    Assert.assertSame(viewHigh, mUnderTest.getChildAt(2));
    Assert.assertSame(view, mUnderTest.getChildAt(3));
  }

  @Test
  public void testHighBothPriority() {
    View view = new View(mUnderTest.getContext());
    View view2 = new View(mUnderTest.getContext());
    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());
    KeyboardViewContainerView.StripActionProvider provider2 =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view2).when(provider2).inflateActionView(any());

    mUnderTest.addStripAction(provider, true);
    Assert.assertEquals(3, mUnderTest.getChildCount());
    Assert.assertSame(view, mUnderTest.getChildAt(2));

    mUnderTest.addStripAction(provider2, true);
    Assert.assertEquals(4, mUnderTest.getChildCount());
    Assert.assertSame(view2, mUnderTest.getChildAt(2));
    Assert.assertSame(view, mUnderTest.getChildAt(3));
  }

  @Test
  public void testStripVisibility() {
    final int initialChildCount = mUnderTest.getChildCount();
    final int actionViewId = R.id.demo_keyboard_view /*can be whatever*/;
    View view = new View(mUnderTest.getContext());
    view.setId(actionViewId);
    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());

    mUnderTest.addStripAction(provider, false);
    Assert.assertEquals(View.VISIBLE, mUnderTest.getCandidateView().getVisibility());
    Assert.assertSame(view, mUnderTest.findViewById(actionViewId));
    Assert.assertEquals(initialChildCount + 1, mUnderTest.getChildCount());

    mUnderTest.setActionsStripVisibility(false);
    Assert.assertEquals(View.GONE, mUnderTest.getCandidateView().getVisibility());
    Assert.assertNull(mUnderTest.findViewById(actionViewId));
    Assert.assertEquals(initialChildCount, mUnderTest.getChildCount());

    mUnderTest.setActionsStripVisibility(true);
    Assert.assertEquals(View.VISIBLE, mUnderTest.getCandidateView().getVisibility());
    Assert.assertSame(view, mUnderTest.findViewById(actionViewId));
    Assert.assertEquals(initialChildCount + 1, mUnderTest.getChildCount());

    mUnderTest.setActionsStripVisibility(true);
    Assert.assertEquals(View.VISIBLE, mUnderTest.getCandidateView().getVisibility());
    Assert.assertSame(view, mUnderTest.findViewById(actionViewId));
    Assert.assertEquals(initialChildCount + 1, mUnderTest.getChildCount());
  }

  @Test
  public void testQueueActionViewForAdditionWhenNotVisible() {
    final int actionViewId = R.id.demo_keyboard_view /*can be whatever*/;
    View view = new View(mUnderTest.getContext());
    view.setId(actionViewId);
    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());

    final int actionViewId2 = R.id.demo_keyboard_view_background /*can be whatever*/;
    View view2 = new View(mUnderTest.getContext());
    view2.setId(actionViewId2);
    KeyboardViewContainerView.StripActionProvider provider2 =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view2).when(provider2).inflateActionView(any());

    mUnderTest.addStripAction(provider, false);
    Assert.assertSame(view, mUnderTest.findViewById(actionViewId));
    Mockito.verify(provider).inflateActionView(any());

    mUnderTest.setActionsStripVisibility(false);
    Assert.assertNull(mUnderTest.findViewById(actionViewId));

    mUnderTest.addStripAction(provider2, false);
    Assert.assertNull(mUnderTest.findViewById(actionViewId));
    Assert.assertNull(mUnderTest.findViewById(actionViewId2));
    Mockito.verify(provider).inflateActionView(any());
    Mockito.verify(provider).inflateActionView(any());

    mUnderTest.setActionsStripVisibility(true);
    Assert.assertSame(view, mUnderTest.findViewById(actionViewId));
    Assert.assertSame(view2, mUnderTest.findViewById(actionViewId2));
    // no additional calls, still once.
    Mockito.verify(provider).inflateActionView(any());
    Mockito.verify(provider).inflateActionView(any());
  }

  @Test
  public void testDoubleAddDoesNotAddAgain() {
    View view = new View(mUnderTest.getContext());
    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());

    mUnderTest.addStripAction(provider, false);
    mUnderTest.addStripAction(provider, false);

    Mockito.verify(provider).inflateActionView(mUnderTest);
    Mockito.verify(provider, Mockito.never()).onRemoved();
    Assert.assertEquals(3, mUnderTest.getChildCount());
    Assert.assertSame(view, mUnderTest.getChildAt(2));
  }

  @Test(expected = IllegalStateException.class)
  public void testFailIfViewAddedWithParent() {
    mUnderTest.setActionsStripVisibility(false);
    View view = new View(mUnderTest.getContext());
    // adding parent
    FrameLayout parent = new FrameLayout(mUnderTest.getContext());
    parent.addView(view);
    Assert.assertSame(parent, view.getParent());

    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());
    // this will fail
    mUnderTest.addStripAction(provider, false);
  }

  @Test
  public void testDoubleAddViewDoesNotCrash() {
    mUnderTest.setActionsStripVisibility(false);
    View view = new View(mUnderTest.getContext());
    KeyboardViewContainerView.StripActionProvider provider =
        Mockito.mock(KeyboardViewContainerView.StripActionProvider.class);
    Mockito.doReturn(view).when(provider).inflateActionView(any());

    mUnderTest.addStripAction(provider, false);
    mUnderTest.setActionsStripVisibility(true);
    mUnderTest.setActionsStripVisibility(true);
    Assert.assertEquals(3, mUnderTest.getChildCount());
    Assert.assertSame(view, mUnderTest.getChildAt(2));
    Assert.assertSame(mUnderTest, view.getParent());
  }

  @Test
  public void testMeasure() {
    mUnderTest.onMeasure(
        View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST));
    Assert.assertEquals(View.VISIBLE, mUnderTest.getCandidateView().getVisibility());

    Assert.assertEquals(1024, mUnderTest.getMeasuredWidth());
    Assert.assertEquals(1068, mUnderTest.getMeasuredHeight());

    mUnderTest.setActionsStripVisibility(false);
    mUnderTest.onMeasure(
        View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST));
    Assert.assertEquals(View.GONE, mUnderTest.getCandidateView().getVisibility());

    Assert.assertEquals(1024, mUnderTest.getMeasuredWidth());
    Assert.assertEquals(1024, mUnderTest.getMeasuredHeight());
  }

  @Test
  public void testOffsetTouchesToMainKeyboard() {
    mUnderTest.onMeasure(
        View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST));
    mUnderTest.onLayout(false, 0, 0, 1024, 1024);

    final float candidateY = mUnderTest.getResources().getDimension(R.dimen.candidate_strip_height);
    final float midX = mUnderTest.getWidth() / 2f;

    // inside the keyboard
    MotionEvent e = MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, midX, 2f * candidateY, 0);
    Assert.assertFalse(mUnderTest.onInterceptTouchEvent(e));
    Assert.assertEquals(midX, e.getX(), 0.01f);
    Assert.assertEquals(2f * candidateY, e.getY(), 0.01f);

    // way above the keyboard
    e = MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, midX, candidateY / 2f, 0);
    Assert.assertFalse(mUnderTest.onInterceptTouchEvent(e));
    Assert.assertEquals(midX, e.getX(), 0.01f);
    // not being offset
    Assert.assertEquals(candidateY / 2f, e.getY(), 0.01f);

    // a bit above the keyboard
    e = MotionEvent.obtain(10, 10, MotionEvent.ACTION_DOWN, midX, candidateY * 0.8f, 0);
    Assert.assertFalse(mUnderTest.onInterceptTouchEvent(e));
    Assert.assertEquals(midX, e.getX(), 0.01f);
    // being offset
    Assert.assertEquals(candidateY + 1f, e.getY(), 0.01f);
  }
}
