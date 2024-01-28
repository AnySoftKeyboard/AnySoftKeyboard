package com.anysoftkeyboard.keyboards.views.preview;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.menny.android.anysoftkeyboard.R.drawable.blacktheme_preview_background;

import android.app.Application;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBase;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyPreviewsManagerTest {

  private Keyboard.Key[] mTestKeys;
  private PreviewPopupTheme mTheme;
  private AnyKeyboardViewBase mKeyboardView;
  private PositionCalculator mPositionCalculator;

  private static PopupWindow getLatestCreatedPopupWindow() {
    TestRxSchedulers.foregroundAdvanceBy(1000);
    return Shadows.shadowOf((Application) ApplicationProvider.getApplicationContext())
        .getLatestPopupWindow();
  }

  @Before
  public void setup() {
    mPositionCalculator = Mockito.mock(PositionCalculator.class);
    Mockito.doReturn(new Point(2, 3))
        .when(mPositionCalculator)
        .calculatePositionForPreview(Mockito.any(), Mockito.any(), Mockito.any());

    mKeyboardView = Mockito.mock(AnyKeyboardViewBase.class);
    Mockito.doAnswer(
            a -> {
              int[] location = a.getArgument(0);
              location[0] = 1;
              location[1] = 2;
              return null;
            })
        .when(mKeyboardView)
        .getLocationInWindow(Mockito.any());

    mTestKeys = new Keyboard.Key[10];
    for (int keyIndex = 0; keyIndex < 10; keyIndex++) {
      Keyboard.Key key = Mockito.mock(Keyboard.Key.class);
      key.width = 10;
      key.showPreview = true;
      key.height = 20;
      key.x = (1 + keyIndex * 10);
      key.y = 11;
      key.label = "" + ((char) ('a' + keyIndex));
      Mockito.doReturn((int) 'a' + keyIndex).when(key).getPrimaryCode();
      Mockito.doReturn(1).when(key).getCodesCount();
      mTestKeys[keyIndex] = key;
    }

    mTheme = new PreviewPopupTheme();
    mTheme.setPreviewKeyBackground(
        ContextCompat.getDrawable(getApplicationContext(), blacktheme_preview_background));
    mTheme.setPreviewKeyTextSize(1);
  }

  @Test
  public void testNoPopupForEnter() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

    PopupWindow createdPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNull(createdPopupWindow);

    Mockito.doReturn(KeyCodes.ENTER).when(mTestKeys[0]).getPrimaryCode();
    underTest.showPreviewForKey(mTestKeys[0], "", mKeyboardView, mTheme);

    createdPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNull(createdPopupWindow);
  }

  @Test
  public void testNoPopupForNoPreview() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

    PopupWindow createdPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNull(createdPopupWindow);

    mTestKeys[0].showPreview = false;
    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);

    createdPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNull(createdPopupWindow);
  }

  @Test
  public void testNoPopupForModifier() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

    PopupWindow createdPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNull(createdPopupWindow);

    mTestKeys[0].modifier = true;
    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);

    createdPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNull(createdPopupWindow);
  }

  @Test
  public void testPopupForRegularKey() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

    Assert.assertNull(getLatestCreatedPopupWindow());

    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);

    Assert.assertNotNull(getLatestCreatedPopupWindow());
  }

  @Test
  public void testNoPopupWhenTextSizeIsZero() {
    mTheme.setPreviewKeyTextSize(0);
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

    Assert.assertNull(getLatestCreatedPopupWindow());

    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);

    Assert.assertNull(getLatestCreatedPopupWindow());
  }

  @Test
  public void testReuseForTheSameKey() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);
    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);

    final PopupWindow firstPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNotNull(firstPopupWindow);

    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);
    Assert.assertSame(firstPopupWindow, getLatestCreatedPopupWindow());
  }

  @Test
  public void testDoNotReuseForTheOtherKey() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);
    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);

    final PopupWindow firstPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNotNull(firstPopupWindow);

    underTest.showPreviewForKey(mTestKeys[1], "t", mKeyboardView, mTheme);
    Assert.assertNotSame(firstPopupWindow, getLatestCreatedPopupWindow());
  }

  @Test
  public void testCycleThroughPopupQueueWhenAllAreActive() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

    final int[] reuseIndex = new int[] {0, 1, 2, 0, 1, 2, 0};
    final List<TextView> usedWindows = new ArrayList<>();

    for (int index = 0; index < reuseIndex.length; index++) {
      underTest.showPreviewForKey(mTestKeys[index], mTestKeys[index].label, mKeyboardView, mTheme);

      usedWindows.add(
          getLatestCreatedPopupWindow().getContentView().findViewById(R.id.key_preview_text));
      final TextView textView = usedWindows.get(reuseIndex[index]);
      Assert.assertEquals(textView.getText().toString(), mTestKeys[index].label);
    }
  }

  @Test
  public void testTakeLatestDeactivated() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);

    underTest.showPreviewForKey(mTestKeys[0], mTestKeys[0].label, mKeyboardView, mTheme);
    final PopupWindow first = getLatestCreatedPopupWindow();
    underTest.showPreviewForKey(mTestKeys[1], mTestKeys[1].label, mKeyboardView, mTheme);
    final PopupWindow second = getLatestCreatedPopupWindow();

    underTest.hidePreviewForKey(mTestKeys[0]);
    TestRxSchedulers.foregroundAdvanceBy(1000);

    underTest.showPreviewForKey(mTestKeys[2], mTestKeys[2].label, mKeyboardView, mTheme);
    final PopupWindow third = getLatestCreatedPopupWindow();

    Assert.assertNotSame(first, second);
    Assert.assertNotSame(third, second);
    Assert.assertSame(third, first);
  }

  @Test
  public void testCancelAllPreviewsStillReusePreviews() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);
    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);

    final PopupWindow firstPopupWindow = getLatestCreatedPopupWindow();
    Assert.assertNotNull(firstPopupWindow);

    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);
    Assert.assertSame(firstPopupWindow, getLatestCreatedPopupWindow());

    underTest.cancelAllPreviews();

    TestRxSchedulers.foregroundAdvanceBy(1000);

    underTest.showPreviewForKey(mTestKeys[0], "y", mKeyboardView, mTheme);
    Assert.assertSame(firstPopupWindow, getLatestCreatedPopupWindow());
  }

  @Test
  public void testSetupPopupLayoutForKeyLabel() {
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);
    underTest.showPreviewForKey(mTestKeys[0], mTestKeys[0].label, mKeyboardView, mTheme);

    final PopupWindow window = getLatestCreatedPopupWindow();
    final TextView textView = window.getContentView().findViewById(R.id.key_preview_text);
    Assert.assertEquals(textView.getText().toString(), mTestKeys[0].label);
    Assert.assertEquals(View.VISIBLE, textView.getVisibility());
    final ImageView imageView = window.getContentView().findViewById(R.id.key_preview_icon);
    Assert.assertEquals(View.GONE, imageView.getVisibility());
  }

  @Test
  public void testSetupPopupLayoutForKeyDrawable() {
    final Drawable drawable = getApplicationContext().getDrawable(R.drawable.ic_accept);
    KeyPreviewsManager underTest =
        new KeyPreviewsManager(getApplicationContext(), mPositionCalculator, 3);
    underTest.showPreviewForKey(mTestKeys[0], drawable, mKeyboardView, mTheme);

    final PopupWindow window = getLatestCreatedPopupWindow();
    final TextView textView = window.getContentView().findViewById(R.id.key_preview_text);
    Assert.assertEquals(View.GONE, textView.getVisibility());
    final ImageView imageView = window.getContentView().findViewById(R.id.key_preview_icon);
    Assert.assertEquals(View.VISIBLE, imageView.getVisibility());
    Assert.assertSame(drawable, imageView.getDrawable());
  }
}
