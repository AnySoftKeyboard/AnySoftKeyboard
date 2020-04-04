package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.text.TextUtils;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.AnyApplication;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class KeyboardAddOnTest {

    private static final String ASK_ENGLISH_1_ID = "c7535083-4fe6-49dc-81aa-c5438a1a343a";
    private static final String ASK_ENGLISH_16_KEYS_ID = "12335055-4aa6-49dc-8456-c7d38a1a5123";

    @Test
    public void testGetKeyboardDefaultEnabled() throws Exception {
        List<KeyboardAddOnAndBuilder> enabledKeyboards =
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOns();
        // checking that ASK English is enabled
        boolean askEnglishEnabled = false;
        for (KeyboardAddOnAndBuilder addOnAndBuilder : enabledKeyboards) {
            if (addOnAndBuilder.getId().equals(ASK_ENGLISH_1_ID)) {
                assertTrue(addOnAndBuilder.getKeyboardDefaultEnabled());
                assertEquals(
                        addOnAndBuilder.getPackageName(), getApplicationContext().getPackageName());
                askEnglishEnabled = true;
            }
        }
        assertTrue(askEnglishEnabled);
        // only one enabled keyboard
        Assert.assertEquals(1, enabledKeyboards.size());
    }

    @Test
    public void testGetEnabledDefaultFromAllKeyboards() throws Exception {
        List<KeyboardAddOnAndBuilder> allAvailableKeyboards =
                AnyApplication.getKeyboardFactory(getApplicationContext()).getAllAddOns();

        Map<CharSequence, Boolean> keyboardsEnabled = new HashMap<>();
        for (KeyboardAddOnAndBuilder addOnAndBuilder : allAvailableKeyboards) {
            keyboardsEnabled.put(
                    addOnAndBuilder.getId(), addOnAndBuilder.getKeyboardDefaultEnabled());
        }

        Assert.assertEquals(11, keyboardsEnabled.size());
        Assert.assertTrue(keyboardsEnabled.containsKey(ASK_ENGLISH_1_ID));
        Assert.assertTrue(keyboardsEnabled.get(ASK_ENGLISH_1_ID));
        Assert.assertTrue(keyboardsEnabled.containsKey(ASK_ENGLISH_16_KEYS_ID));
        Assert.assertFalse(keyboardsEnabled.get(ASK_ENGLISH_16_KEYS_ID));
    }

    private KeyboardAddOnAndBuilder getKeyboardFromFactory(String id) {
        List<KeyboardAddOnAndBuilder> keyboards =
                AnyApplication.getKeyboardFactory(getApplicationContext()).getAllAddOns();

        for (KeyboardAddOnAndBuilder addOnAndBuilder : keyboards) {
            if (addOnAndBuilder.getId().equals(id)) {
                return addOnAndBuilder;
            }
        }

        return null;
    }

    @Test
    public void testGetKeyboardLocale() throws Exception {
        KeyboardAddOnAndBuilder askEnglish = getKeyboardFromFactory(ASK_ENGLISH_1_ID);
        assertNotNull(askEnglish);
        assertEquals(askEnglish.getKeyboardLocale(), "en");

        KeyboardAddOnAndBuilder testerEnglish = getKeyboardFromFactory(ASK_ENGLISH_16_KEYS_ID);
        assertNotNull(testerEnglish);
        assertEquals(testerEnglish.getKeyboardLocale(), "en");

        KeyboardAddOnAndBuilder testerTerminal =
                getKeyboardFromFactory("b1c24b40-02ce-4857-9fb8-fb9e4e3b4318");
        assertNotNull(testerTerminal);
        assertTrue(TextUtils.isEmpty(testerTerminal.getKeyboardLocale()));
    }

    @Test
    public void testCreateKeyboard() throws Exception {}
}
