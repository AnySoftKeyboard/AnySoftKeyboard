package com.anysoftkeyboard.saywhat;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class PeriodsTimeProviderTest {

  private static final long ONE_DAY = 24 * 60 * 60 * 1000;

  @Test
  public void testHappyPath() {
    Calendar instance = Calendar.getInstance();
    PeriodsTimeProvider underTest = new PeriodsTimeProvider(() -> instance, 100, 115, 200, 230);

    instance.set(2020, Calendar.JANUARY, 1, 10, 10, 10);
    Assert.assertEquals(99 * ONE_DAY, underTest.getNextTimeOffset(0));
    instance.set(Calendar.DAY_OF_YEAR, 98);
    Assert.assertEquals(2 * ONE_DAY, underTest.getNextTimeOffset(1));
    instance.set(Calendar.DAY_OF_YEAR, 100);
    Assert.assertEquals(1 * ONE_DAY, underTest.getNextTimeOffset(2));
    instance.set(Calendar.DAY_OF_YEAR, 101);
    Assert.assertEquals(1 * ONE_DAY, underTest.getNextTimeOffset(3));
    instance.set(Calendar.DAY_OF_YEAR, 112);
    Assert.assertEquals(1 * ONE_DAY, underTest.getNextTimeOffset(4));
    instance.set(Calendar.DAY_OF_YEAR, 114);
    Assert.assertEquals(1 * ONE_DAY, underTest.getNextTimeOffset(5));
    instance.set(Calendar.DAY_OF_YEAR, 115);
    Assert.assertEquals(85 * ONE_DAY, underTest.getNextTimeOffset(6));
    instance.set(Calendar.DAY_OF_YEAR, 116);
    Assert.assertEquals(84 * ONE_DAY, underTest.getNextTimeOffset(1));
    instance.set(Calendar.DAY_OF_YEAR, 198);
    Assert.assertEquals(2 * ONE_DAY, underTest.getNextTimeOffset(2));
    instance.set(Calendar.DAY_OF_YEAR, 200);
    Assert.assertEquals(1 * ONE_DAY, underTest.getNextTimeOffset(3));
    instance.set(Calendar.DAY_OF_YEAR, 204);
    Assert.assertEquals(1 * ONE_DAY, underTest.getNextTimeOffset(4));
    instance.set(Calendar.DAY_OF_YEAR, 228);
    Assert.assertEquals(1 * ONE_DAY, underTest.getNextTimeOffset(5));
    instance.set(Calendar.DAY_OF_YEAR, 230);
    Assert.assertEquals((365 - 230 + 100) * ONE_DAY, underTest.getNextTimeOffset(6));
  }
}
