package net.evendanan.pixel;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.M) /*this view has no usage before API 30*/
public class ScrollViewAsMainChildTest {

  private ScrollViewAsMainChild buildViewUnderTest() {
    final ScrollViewAsMainChild view =
        (ScrollViewAsMainChild)
            LayoutInflater.from(RuntimeEnvironment.getApplication())
                .inflate(R.layout.settings_scroll_view_test_layout, null);
    TestRxSchedulers.foregroundFlushAllJobs();
    return view;
  }

  @Before
  public void setUp() throws Exception {}

  @Test
  public void testCurrentInnerLayout() {
    var underTest = buildViewUnderTest();

    Assert.assertEquals(1, underTest.getChildCount());

    var itemsHolder = (LinearLayout) underTest.findViewById(R.id.inner_layout);
    Assert.assertEquals(1, itemsHolder.getChildCount());
    Assert.assertEquals(View.VISIBLE, itemsHolder.getVisibility());
    View spacer = itemsHolder.getChildAt(0);
    Assert.assertEquals(R.id.bottom_gap_view, spacer.getId());
    Assert.assertEquals(View.VISIBLE, spacer.getVisibility());
  }

  @Test
  public void testItemsCount() {
    var underTest = buildViewUnderTest();
    var itemsHolder = (LinearLayout) underTest.findViewById(R.id.inner_layout);
    final var bottomSpace = itemsHolder.getChildAt(0);

    Assert.assertEquals(0, underTest.getItemsCount());

    var view1 = new View(underTest.getContext());
    underTest.addListItem(view1);
    Assert.assertEquals(1, underTest.getItemsCount());

    var view2 = new View(underTest.getContext());
    underTest.addListItem(view2);
    Assert.assertEquals(2, underTest.getItemsCount());

    Assert.assertEquals(3, itemsHolder.getChildCount());

    Assert.assertSame(view1, itemsHolder.getChildAt(0));
    Assert.assertSame(view2, itemsHolder.getChildAt(1));
    Assert.assertSame(bottomSpace, itemsHolder.getChildAt(2));

    underTest.removeAllListItems();
    Assert.assertEquals(0, underTest.getItemsCount());
    Assert.assertEquals(1, itemsHolder.getChildCount());
    Assert.assertSame(bottomSpace, itemsHolder.getChildAt(0));
  }

  @Test
  public void testSetPadding() {
    var underTest = buildViewUnderTest();

    var spacer = underTest.findViewById(R.id.bottom_gap_view);
    Assert.assertEquals(0, spacer.getHeight());

    underTest.setBottomOffset(11);
    Assert.assertEquals(11, spacer.getLayoutParams().height);
  }

  @Test
  public void testLayoutViewsCorrectlyWhileScrolling() {
    var underTest = buildViewUnderTest();

    final int childCount = 20;
    final int childHeight = 22;
    for (int i = 0; i < childCount; i++) {
      var v = Mockito.spy(new View(underTest.getContext()));
      v.setLayoutParams(
          new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, childHeight));

      Mockito.doReturn(childHeight * (1 + i)).when(v).getBottom();
      Mockito.doReturn(childHeight).when(v).getHeight();

      underTest.addListItem(v);
    }

    underTest.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);

    TestRxSchedulers.foregroundFlushAllJobs();

    final var itemsHolder = (LinearLayout) underTest.findViewById(R.id.inner_layout);
    itemsHolder.measure(View.MeasureSpec.EXACTLY, View.MeasureSpec.EXACTLY);

    // when scroll is at the top, all views are visible
    underTest.onScrollChanged(underTest, 0, 0, 0, 0);

    for (int i = 0; i < childCount; i++) {
      Assert.assertEquals(View.VISIBLE, itemsHolder.getChildAt(i).getVisibility());
      Assert.assertEquals("item " + i, 1f, itemsHolder.getChildAt(i).getScaleX(), 0.001f);
      Assert.assertEquals("item " + i, 1f, itemsHolder.getChildAt(i).getScaleY(), 0.001f);
    }

    // scrolling a bit, will scale down the top item
    underTest.onScrollChanged(underTest, 0, 11, 0, 0);

    Assert.assertEquals(View.VISIBLE, itemsHolder.getChildAt(0).getVisibility());
    Assert.assertEquals(0.5f, itemsHolder.getChildAt(0).getScaleX(), 0.001f);
    Assert.assertEquals(0.5f, itemsHolder.getChildAt(0).getScaleY(), 0.001f);

    // the reset are the same
    for (int i = 1; i < childCount; i++) {
      Assert.assertEquals(View.VISIBLE, itemsHolder.getChildAt(i).getVisibility());
      Assert.assertEquals("item " + i, 1f, itemsHolder.getChildAt(i).getScaleX(), 0.001f);
      Assert.assertEquals("item " + i, 1f, itemsHolder.getChildAt(i).getScaleY(), 0.001f);
    }

    // scrolling more, will scale down the top item and hide the ones outside
    underTest.onScrollChanged(underTest, 0, 22 * 4 + 2, 0, 11);

    for (int i = 0; i < 4; i++) {
      Assert.assertEquals("item " + i, View.INVISIBLE, itemsHolder.getChildAt(i).getVisibility());
    }

    Assert.assertEquals(View.VISIBLE, itemsHolder.getChildAt(4).getVisibility());
    Assert.assertEquals(0.9f, itemsHolder.getChildAt(4).getScaleX(), 0.01f);
    Assert.assertEquals(0.9f, itemsHolder.getChildAt(4).getScaleY(), 0.01f);

    // the reset are the same
    for (int i = 5; i < childCount; i++) {
      Assert.assertEquals(View.VISIBLE, itemsHolder.getChildAt(i).getVisibility());
      Assert.assertEquals(1f, itemsHolder.getChildAt(i).getScaleX(), 0.001f);
      Assert.assertEquals(1f, itemsHolder.getChildAt(i).getScaleY(), 0.001f);
    }

    // now scrolling back
    underTest.onScrollChanged(underTest, 0, 11, 0, 22 * 4 + 2);

    Assert.assertEquals(View.VISIBLE, itemsHolder.getChildAt(0).getVisibility());
    Assert.assertEquals(0.5f, itemsHolder.getChildAt(0).getScaleX(), 0.001f);
    Assert.assertEquals(0.5f, itemsHolder.getChildAt(0).getScaleY(), 0.001f);

    // the reset are the same
    for (int i = 1; i < childCount; i++) {
      Assert.assertEquals(View.VISIBLE, itemsHolder.getChildAt(i).getVisibility());
      Assert.assertEquals("item " + i, 1f, itemsHolder.getChildAt(i).getScaleX(), 0.001f);
      Assert.assertEquals("item " + i, 1f, itemsHolder.getChildAt(i).getScaleY(), 0.001f);
    }
  }
}
