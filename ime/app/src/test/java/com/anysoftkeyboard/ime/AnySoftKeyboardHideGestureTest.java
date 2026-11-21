/*
 * Copyright (c) 2026 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ime;

import android.os.Build;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardHideGestureTest {

  private static class TestableKeyboard extends SoftKeyboard {
    int hideWindowCalls;
    int requestHideSelfCalls;

    @Override
    public void hideWindow() {
      hideWindowCalls++;
    }

    @Override
    public void requestHideSelf(int flags) {
      requestHideSelfCalls++;
    }
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testGestureHideHidesWindowAndAsksSystemToHideOnApi30Plus() {
    final TestableKeyboard ime = new TestableKeyboard();

    ime.hideKeyboardFromUserGesture();

    Assert.assertEquals(1, ime.hideWindowCalls);
    // On API 30+ the IME must also go through requestHideSelf(): a plain hideWindow() only
    // hides the IME window locally, and on Android 15+ the system then re-shows the IME
    // because the focused client still believes the IME is visible.
    Assert.assertEquals(1, ime.requestHideSelfCalls);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.N)
  public void testGestureHideOnlyHidesWindowBeforeApi30() {
    final TestableKeyboard ime = new TestableKeyboard();

    ime.hideKeyboardFromUserGesture();

    Assert.assertEquals(1, ime.hideWindowCalls);
    Assert.assertEquals(0, ime.requestHideSelfCalls);
  }
}
