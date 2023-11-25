package com.anysoftkeyboard.saywhat;

import android.text.TextUtils;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.AnyRoboApplication;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class PublicNoticesTest {
  @Test
  public void testUniqueNames() {
    AnyRoboApplication application = ApplicationProvider.getApplicationContext();
    Set<String> seenNames = new HashSet<>();
    for (PublicNotice publicNotice : application.getPublicNoticesProduction()) {
      Assert.assertNotNull(publicNotice);
      Assert.assertFalse(TextUtils.isEmpty(publicNotice.getName()));
      Assert.assertTrue(
          publicNotice.getName() + " should be unique", seenNames.add(publicNotice.getName()));
    }
  }

  @Test
  public void testSameNoticesInstancesInTestingOfEachType() {
    AnyRoboApplication application = ApplicationProvider.getApplicationContext();
    List<PublicNotice> publicNoticesProduction = application.getPublicNotices();
    Assert.assertEquals(3, publicNoticesProduction.size());
    Assert.assertNotSame(publicNoticesProduction, application.getPublicNotices());
    Assert.assertEquals(publicNoticesProduction.size(), application.getPublicNotices().size());

    Assert.assertEquals(
        1, publicNoticesProduction.stream().filter(p -> p instanceof OnKey).count());
    Assert.assertEquals(
        1, publicNoticesProduction.stream().filter(p -> p instanceof OnVisible).count());
    Assert.assertEquals(
        1, publicNoticesProduction.stream().filter(p -> p instanceof OnUiPage).count());
  }

  @Test
  public void testSameNoticesInstancesInProduction() {
    AnyRoboApplication application = ApplicationProvider.getApplicationContext();
    List<PublicNotice> publicNoticesProduction = application.getPublicNoticesProduction();
    Assert.assertFalse(publicNoticesProduction.isEmpty());
    List<PublicNotice> publicNoticesProduction2 = application.getPublicNoticesProduction();
    Assert.assertNotSame(publicNoticesProduction, publicNoticesProduction2);
    Assert.assertEquals(publicNoticesProduction.size(), publicNoticesProduction2.size());

    for (int i = 0; i < publicNoticesProduction.size(); i++) {
      PublicNotice publicNotice = publicNoticesProduction.get(i);
      PublicNotice publicNotice2 = publicNoticesProduction2.get(i);
      Assert.assertSame(publicNotice, publicNotice2);
    }
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCanNotChangePublicNoticesList() {
    AnyRoboApplication application = ApplicationProvider.getApplicationContext();
    List<PublicNotice> publicNoticesProduction = application.getPublicNoticesProduction();

    publicNoticesProduction.remove(0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCanNotChangePublicNoticesListInTesting() {
    AnyRoboApplication application = ApplicationProvider.getApplicationContext();
    List<PublicNotice> publicNoticesProduction = application.getPublicNotices();

    publicNoticesProduction.remove(0);
  }
}
