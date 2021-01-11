package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardRxPrefsTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testPopupCharactersOrderChangesFlushKeyboards() {
        // this resets the flushed flag, so it will only return true the first time
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
        // veryfing:
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsNotFlushed();
        // changing this preference should auto-flush the keyboard:
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_popup_characters_order,
                R.string.settings_key_popup_characters_order_default);
        // verifying:
        mAnySoftKeyboardUnderTest.getKeyboardSwitcherForTests().verifyKeyboardsFlushed();
    }
}
