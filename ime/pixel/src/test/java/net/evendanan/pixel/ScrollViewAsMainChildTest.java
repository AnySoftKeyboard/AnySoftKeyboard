package net.evendanan.pixel;

import static org.junit.Assert.*;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
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
}
