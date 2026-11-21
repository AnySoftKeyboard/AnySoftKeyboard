package com.anysoftkeyboard.keyboards;

import org.junit.Assert;
import org.junit.Test;

public class KeyboardSupportHeightTest {

  private static KeyboardDimens dimens(int normal, int small, int large, float rowGap) {
    return new KeyboardDimens() {
      @Override
      public int getKeyboardMaxWidth() {
        return 1080;
      }

      @Override
      public float getKeyHorizontalGap() {
        return 0;
      }

      @Override
      public float getRowVerticalGap() {
        return rowGap;
      }

      @Override
      public int getNormalKeyHeight() {
        return normal;
      }

      @Override
      public int getSmallKeyHeight() {
        return small;
      }

      @Override
      public int getLargeKeyHeight() {
        return large;
      }

      @Override
      public float getPaddingBottom() {
        return 0;
      }
    };
  }

  @Test
  public void testExistingCodes() {
    KeyboardDimens d = dimens(144, 96, 162, 6f);
    Assert.assertEquals(144, KeyboardSupport.getKeyHeightFromHeightCode(d, -1, 1f));
    Assert.assertEquals(96, KeyboardSupport.getKeyHeightFromHeightCode(d, -2, 1f));
    Assert.assertEquals(162, KeyboardSupport.getKeyHeightFromHeightCode(d, -3, 1f));
    Assert.assertEquals(72, KeyboardSupport.getKeyHeightFromHeightCode(d, -1, 0.5f));
  }

  @Test
  public void testCompactFourRowsMatchThreeNormalRows() {
    for (float factor : new float[] {0.6f, 1f, 1.2f}) {
      KeyboardDimens d = dimens(144, 96, 162, 6f);
      int normal = KeyboardSupport.getKeyHeightFromHeightCode(d, -1, factor);
      int compact = KeyboardSupport.getKeyHeightFromHeightCode(d, -4, factor);
      float threeNormalRows = 3 * (normal + 6f);
      float fourCompactRows = 4 * (compact + 6f);
      Assert.assertEquals("factor " + factor, threeNormalRows, fourCompactRows, 4f);
    }
  }
}
