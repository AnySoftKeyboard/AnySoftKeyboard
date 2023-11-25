package com.anysoftkeyboard.base.utils;

import org.junit.Assert;
import org.junit.Test;

public class CompatUtilsTest {

  @Test
  public void testObjectsEqual() {
    Assert.assertTrue(CompatUtils.objectEquals(null, null));
    Assert.assertFalse(CompatUtils.objectEquals(null, new Object()));
    Assert.assertFalse(CompatUtils.objectEquals(new Object(), null));
    Assert.assertTrue(CompatUtils.objectEquals(new String("test"), new String("test")));
    Assert.assertFalse(CompatUtils.objectEquals(new String("test"), new String("test1")));
  }
}
