package com.anysoftkeyboard.notification;

import android.text.TextUtils;
import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class NotificationChannelsTest {
  @Test
  public void testValuesAreClear() {
    for (NotificationChannels v : NotificationChannels.values()) {
      Assert.assertEquals(v.name(), v.mChannelId, v.name());
      Assert.assertFalse(v.name(), TextUtils.isEmpty(v.mDescription));
    }
  }
}
