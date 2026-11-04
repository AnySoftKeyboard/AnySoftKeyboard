package com.anysoftkeyboard.saywhat;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.text.TextUtils;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.menny.android.anysoftkeyboard.AnyRoboApplication;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class PublicNoticesTest extends AnySoftKeyboardBaseTest {

  @After
  public void tearDownNotices() {
    AnyRoboApplication application = getApplicationContext();
    for (PublicNotice notice : application.getPublicNotices()) {
      Mockito.reset(notice);
    }
  }

  @Test
  public void testUniqueNames() {
    AnyRoboApplication application = getApplicationContext();
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
    AnyRoboApplication application = getApplicationContext();
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
    AnyRoboApplication application = getApplicationContext();
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
    AnyRoboApplication application = getApplicationContext();
    List<PublicNotice> publicNoticesProduction = application.getPublicNoticesProduction();

    publicNoticesProduction.remove(0);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCanNotChangePublicNoticesListInTesting() {
    AnyRoboApplication application = getApplicationContext();
    List<PublicNotice> publicNoticesProduction = application.getPublicNotices();

    publicNoticesProduction.remove(0);
  }

  @Test
  public void testOnKeyCircuitBreakerCatchesAndDisablesNotice() {
    AnyRoboApplication application = getApplicationContext();
    OnKey onKey =
        (OnKey)
            application.getPublicNotices().stream()
                .filter(p -> p instanceof OnKey)
                .findFirst()
                .orElseThrow();

    Mockito.clearInvocations(onKey);
    Mockito.doThrow(new RuntimeException("Simulated onKey crash"))
        .when(onKey)
        .onKey(Mockito.any(), Mockito.anyInt(), Mockito.any());

    mAnySoftKeyboardUnderTest.simulateKeyPress('a');
    Mockito.verify(onKey, Mockito.times(1))
        .onKey(Mockito.any(), Mockito.eq((int) 'a'), Mockito.any());

    mAnySoftKeyboardUnderTest.simulateKeyPress('b');
    Mockito.verify(onKey, Mockito.times(1)).onKey(Mockito.any(), Mockito.anyInt(), Mockito.any());
  }

  @Test
  public void testOnVisibleCircuitBreakerCatchesAndDisablesNotice() {
    AnyRoboApplication application = getApplicationContext();
    OnVisible onVisible =
        (OnVisible)
            application.getPublicNotices().stream()
                .filter(p -> p instanceof OnVisible)
                .findFirst()
                .orElseThrow();

    Mockito.clearInvocations(onVisible);
    Mockito.doThrow(new RuntimeException("Simulated onVisible crash"))
        .when(onVisible)
        .onVisible(Mockito.any(), Mockito.any(), Mockito.any());

    simulateOnStartInputFlow();
    Mockito.verify(onVisible, Mockito.times(1))
        .onVisible(Mockito.any(), Mockito.any(), Mockito.any());

    simulateOnStartInputFlow();
    Mockito.verify(onVisible, Mockito.times(1))
        .onVisible(Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  public void testOnHiddenCircuitBreakerCatchesAndDisablesNotice() {
    AnyRoboApplication application = getApplicationContext();
    OnVisible onVisible =
        (OnVisible)
            application.getPublicNotices().stream()
                .filter(p -> p instanceof OnVisible)
                .findFirst()
                .orElseThrow();

    Mockito.clearInvocations(onVisible);
    Mockito.doThrow(new RuntimeException("Simulated onHidden crash"))
        .when(onVisible)
        .onHidden(Mockito.any(), Mockito.any());

    simulateFinishInputFlow();
    Mockito.verify(onVisible, Mockito.times(1)).onHidden(Mockito.any(), Mockito.any());

    simulateFinishInputFlow();
    Mockito.verify(onVisible, Mockito.times(1)).onHidden(Mockito.any(), Mockito.any());
  }
}
