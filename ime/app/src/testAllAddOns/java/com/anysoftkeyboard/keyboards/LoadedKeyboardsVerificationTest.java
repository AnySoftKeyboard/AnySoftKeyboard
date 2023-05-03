package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.text.TextUtils;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class LoadedKeyboardsVerificationTest {

  private KeyboardFactory mKeyboardFactory;

  private static final KeyboardDimens sTestKeyboardDimens =
      new KeyboardDimens() {
        @Override
        public int getKeyboardMaxWidth() {
          return 1024;
        }

        @Override
        public float getKeyHorizontalGap() {
          return 3;
        }

        @Override
        public float getRowVerticalGap() {
          return 3;
        }

        @Override
        public int getNormalKeyHeight() {
          return 5;
        }

        @Override
        public int getSmallKeyHeight() {
          return 3;
        }

        @Override
        public int getLargeKeyHeight() {
          return 7;
        }

        @Override
        public float getPaddingBottom() {
          return 2;
        }
      };

  @Before
  public void setUp() throws Exception {
    mKeyboardFactory = AnyApplication.getKeyboardFactory(getApplicationContext());
  }

  @Test
  public void testAllKeyboardsHaveValidMetadata() throws Exception {
    List<KeyboardAddOnAndBuilder> addOns = mKeyboardFactory.getAllAddOns();
    final int apiApiVersion =
        getApplicationContext()
            .getResources()
            .getInteger(R.integer.anysoftkeyboard_api_version_code);
    for (KeyboardAddOnAndBuilder addOn : addOns) {
      final String addOnIdString = "Add-on with ID " + addOn.getId();
      Assert.assertEquals(addOnIdString, apiApiVersion, addOn.getApiVersion());
      Assert.assertFalse(addOnIdString, TextUtils.isEmpty(addOn.getId()));
      Assert.assertFalse(addOnIdString, TextUtils.isEmpty(addOn.getName()));
      Assert.assertFalse(addOnIdString, TextUtils.isEmpty(addOn.getDescription()));
    }
  }

  @Test
  public void testAllKeyboardsCanBeCreated() throws Exception {
    List<KeyboardAddOnAndBuilder> addOns = mKeyboardFactory.getAllAddOns();
    final int[] modes =
        new int[] {
          Keyboard.KEYBOARD_ROW_MODE_NORMAL,
          Keyboard.KEYBOARD_ROW_MODE_IM,
          Keyboard.KEYBOARD_ROW_MODE_URL,
          Keyboard.KEYBOARD_ROW_MODE_EMAIL,
          Keyboard.KEYBOARD_ROW_MODE_PASSWORD
        };
    for (KeyboardAddOnAndBuilder addOn : addOns) {
      for (int mode : modes) {
        final String addOnIdString = "Add-on with ID " + addOn.getId() + " mode " + mode;
        final AnyKeyboard keyboard = addOn.createKeyboard(mode);
        Assert.assertNotNull(addOnIdString, keyboard);
        keyboard.loadKeyboard(sTestKeyboardDimens);
      }
    }
  }

  @Test
  public void testAllKeysAreInsideKeyboard() throws Exception {
    List<KeyboardAddOnAndBuilder> addOns = mKeyboardFactory.getAllAddOns();
    final int[] modes =
        new int[] {
          Keyboard.KEYBOARD_ROW_MODE_NORMAL,
          Keyboard.KEYBOARD_ROW_MODE_IM,
          Keyboard.KEYBOARD_ROW_MODE_URL,
          Keyboard.KEYBOARD_ROW_MODE_EMAIL,
          Keyboard.KEYBOARD_ROW_MODE_PASSWORD
        };
    for (KeyboardAddOnAndBuilder addOn : addOns) {
      for (int mode : modes) {
        final String addOnIdString = "Add-on with ID " + addOn.getId() + " mode " + mode;
        final AnyKeyboard keyboard = addOn.createKeyboard(mode);
        keyboard.loadKeyboard(sTestKeyboardDimens);
        for (Keyboard.Key key : keyboard.getKeys()) {
          final String keyId =
              addOnIdString
                  + " key "
                  + key.getPrimaryCode()
                  + " char "
                  + ((char) key.getPrimaryCode());
          Assert.assertTrue(keyId, key.x >= 0);
          Assert.assertTrue(keyId, key.centerX <= sTestKeyboardDimens.getKeyboardMaxWidth());
          Assert.assertTrue(keyId, key.y >= 0);
        }
      }
    }
  }
}
