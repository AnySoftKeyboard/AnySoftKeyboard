package com.anysoftkeyboard.keyboards;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AnySoftKeyboardTestRunner.class)
public class KeyboardAddOnTest {

    public static final String ASK_ENGLISH_1_ID = "keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a";
    public static final String ASK_ENGLISH_16_KEYS_ID = "keyboard_12335055-4aa6-49dc-8456-c7d38a1a5123";

    @Test
    public void testGetKeyboardDefaultEnabled() throws Exception {
        List<KeyboardAddOnAndBuilder> enabledKeyboards = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application);
        //checking that ASK English is enabled
        boolean askEnglishEnabled = false;
        for (KeyboardAddOnAndBuilder addOnAndBuilder : enabledKeyboards) {
            if (addOnAndBuilder.getId().equals(ASK_ENGLISH_1_ID)) {
                assertTrue(addOnAndBuilder.getKeyboardDefaultEnabled());
                assertEquals(addOnAndBuilder.getPackageName(), RuntimeEnvironment.application.getPackageName());
                askEnglishEnabled = true;
            }
        }
        assertTrue(askEnglishEnabled);
        //only one enabled keyboard
        Assert.assertEquals(1, enabledKeyboards.size());
    }

    @Test
    public void testGetEnabledDefaultFromAllKeyboards() throws Exception {
        List<KeyboardAddOnAndBuilder> allAvailableKeyboards = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application);

        Map<String, Boolean> keyboardsEnabled = new HashMap<>();
        for (KeyboardAddOnAndBuilder addOnAndBuilder : allAvailableKeyboards) {
            keyboardsEnabled.put(addOnAndBuilder.getId(), addOnAndBuilder.getKeyboardDefaultEnabled());
        }

        Assert.assertEquals(3, keyboardsEnabled.size());
        Assert.assertTrue(keyboardsEnabled.containsKey(ASK_ENGLISH_1_ID));
        Assert.assertTrue(keyboardsEnabled.get(ASK_ENGLISH_1_ID));
        Assert.assertTrue(keyboardsEnabled.containsKey(ASK_ENGLISH_16_KEYS_ID));
        Assert.assertFalse(keyboardsEnabled.get(ASK_ENGLISH_16_KEYS_ID));
    }

    private KeyboardAddOnAndBuilder getKeyboardFromFactory(String id) {
        List<KeyboardAddOnAndBuilder> keyboards = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application);

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
    }

    @Test
    public void testCreateKeyboard() throws Exception {

    }
}
