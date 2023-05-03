package com.anysoftkeyboard.api;

import android.content.res.Resources;
import androidx.test.core.app.ApplicationProvider;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class KeyCodesTest {

  @Test
  public void testVerifyKeyCodesHasUniques() throws Exception {
    HashSet<Integer> seenValues = new HashSet<>();

    for (Field field : KeyCodes.class.getFields()) {
      final int intValue = (int) field.get(null /*This is a static field*/);
      Assert.assertTrue("Field " + field, seenValues.add(intValue));
    }

    // verifying that the R integers match
    testVerifyKeyCodesResourcesHasUniques(seenValues);
  }

  private void testVerifyKeyCodesResourcesHasUniques(HashSet<Integer> seenValues) throws Exception {
    Resources resources = ApplicationProvider.getApplicationContext().getResources();
    for (Field field : R.integer.class.getFields()) {
      if (field.getName().startsWith("key_code_")) {
        final int idValue = (int) field.get(null /*This is a static field*/);
        final int intValue = resources.getInteger(idValue);

        Assert.assertTrue("Field " + field, seenValues.remove(intValue));
      }
    }

    Assert.assertEquals(
        seenValues.stream()
            .map(integer -> integer.toString())
            .reduce((s, s2) -> s + ", " + s2)
            .orElse("EMPTY"),
        0,
        seenValues.size());
  }

  @Test
  public void testAllFieldsArePublicStaticFinalInt() {
    for (Field field : KeyCodes.class.getFields()) {
      Assert.assertEquals(
          "Field " + field, Modifier.PUBLIC, field.getModifiers() & Modifier.PUBLIC);
      Assert.assertEquals(
          "Field " + field, Modifier.STATIC, field.getModifiers() & Modifier.STATIC);
      Assert.assertEquals("Field " + field, Modifier.FINAL, field.getModifiers() & Modifier.FINAL);
      Assert.assertEquals("Field " + field, int.class, field.getType());
    }
  }
}
