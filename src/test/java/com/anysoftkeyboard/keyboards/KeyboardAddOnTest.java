package com.anysoftkeyboard.keyboards;

import android.text.TextUtils;

import com.anysoftkeyboard.RobolectricAPI18TestRunner;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricAPI18TestRunner.class)
public class KeyboardAddOnTest {

    public static final String ASK_ENGLISH_1 = "c7535083-4fe6-49dc-81aa-c5438a1a343a";
    public static final String TESTER_KEYBOARD_1 = "aef7f690-f485-11e2-b778-0800200c9a60";
    public static final String TESTER_KEYBOARD_2 = "aef7f690-f485-11e2-b778-0800200c9a61";
    public static final String TESTER_KEYBOARD_3 = "aef7f690-f485-11e2-b778-0800200c9a62";

	@Test
	public void testKeyboardAddOneApiAttributeValues() throws Exception {
        //since I suppose to be backward compatible, these attributes values MUST NOT change!
        assertEquals(R.styleable.KeyboardLayout_android_horizontalGap, 2);
        assertEquals(R.styleable.KeyboardLayout_android_verticalGap, 3);
        assertEquals(R.styleable.KeyboardLayout_android_keyHeight, 1);
        assertEquals(R.styleable.KeyboardLayout_android_keyWidth, 0);
        assertEquals(R.styleable.KeyboardLayout_Key_android_codes, 0);
        assertEquals(R.styleable.KeyboardLayout_Key_android_iconPreview, 7);
        assertEquals(R.styleable.KeyboardLayout_Key_android_isModifier, 4);
        assertEquals(R.styleable.KeyboardLayout_Key_android_isRepeatable, 6);
        assertEquals(R.styleable.KeyboardLayout_Key_android_isSticky, 5);
        assertEquals(R.styleable.KeyboardLayout_Key_android_keyboardMode, 11);
        assertEquals(R.styleable.KeyboardLayout_Key_android_keyEdgeFlags, 3);
        assertEquals(R.styleable.KeyboardLayout_Key_android_keyIcon, 10);
        assertEquals(R.styleable.KeyboardLayout_Key_android_keyLabel, 9);
        assertEquals(R.styleable.KeyboardLayout_Key_android_keyOutputText, 8);
        assertEquals(R.styleable.KeyboardLayout_Key_android_popupCharacters, 2);
        assertEquals(R.styleable.KeyboardLayout_Key_android_popupKeyboard, 1);
        assertEquals(R.styleable.KeyboardLayout_Key_hintLabel, 16);
        assertEquals(R.styleable.KeyboardLayout_Key_isFunctional, 13);
        assertEquals(R.styleable.KeyboardLayout_Key_keyDynamicEmblem, 18);
        assertEquals(R.styleable.KeyboardLayout_Key_longPressCode, 12);
        assertEquals(R.styleable.KeyboardLayout_Key_shiftedCodes, 14);
        assertEquals(R.styleable.KeyboardLayout_Key_showPreview, 17);
        assertEquals(R.styleable.KeyboardLayout_Key_shiftedKeyLabel, 15);
        assertEquals(R.styleable.KeyboardLayout_Row_android_keyboardMode, 1);
        assertEquals(R.styleable.KeyboardLayout_Row_android_rowEdgeFlags, 0);
    }

	@Test
	public void testGetKeyboardDefaultEnabled() throws Exception {
        ArrayList<KeyboardAddOnAndBuilder> enabledKeyboards =  KeyboardFactory.getEnabledKeyboards(Robolectric.application);
        //checking that ASK English is enabled
        boolean askEnglishEnabled = false;
        for(KeyboardAddOnAndBuilder addOnAndBuilder : enabledKeyboards) {
            if (addOnAndBuilder.getId().contains(ASK_ENGLISH_1)) {
                assertTrue(addOnAndBuilder.getKeyboardDefaultEnabled());
                assertEquals(addOnAndBuilder.getPackageName(), Robolectric.application.getPackageName());
                askEnglishEnabled = true;
            }
        }
        assertTrue(askEnglishEnabled);

        //now checking my tester keyboard
        boolean testerEnglishEnabled = false;
        for(KeyboardAddOnAndBuilder addOnAndBuilder : enabledKeyboards) {
            if (addOnAndBuilder.getId().contains(TESTER_KEYBOARD_1)) {
                assertTrue(addOnAndBuilder.getKeyboardDefaultEnabled());
                assertEquals(addOnAndBuilder.getPackageName(), Robolectric.application.getPackageName());
                testerEnglishEnabled = true;
            }
        }
        assertTrue(testerEnglishEnabled);

        //now checking my tester keyboard 2
        boolean tester2EnglishEnabled = false;
        for(KeyboardAddOnAndBuilder addOnAndBuilder : enabledKeyboards) {
            if (addOnAndBuilder.getId().contains(TESTER_KEYBOARD_2)) {
                tester2EnglishEnabled = true;
            }
        }
        assertFalse(tester2EnglishEnabled);
    }

    private KeyboardAddOnAndBuilder getKeyboardFromFactory(String id) {
        ArrayList<KeyboardAddOnAndBuilder> keyboards =  KeyboardFactory.getAllAvailableKeyboards(Robolectric.application);

        for(KeyboardAddOnAndBuilder addOnAndBuilder : keyboards) {
            if (addOnAndBuilder.getId().equals(KeyboardAddOnAndBuilder.KEYBOARD_PREF_PREFIX + id)) {
                return addOnAndBuilder;
            }
        }

        return null;
    }

	@Test
	public void testGetKeyboardLocale() throws Exception {
        KeyboardAddOnAndBuilder askEnglish = getKeyboardFromFactory(ASK_ENGLISH_1);
        assertNotNull(askEnglish);
        assertEquals(askEnglish.getKeyboardLocale(), "en");

        KeyboardAddOnAndBuilder testerEnglish = getKeyboardFromFactory(TESTER_KEYBOARD_1);
        assertNotNull(testerEnglish);
        assertEquals(testerEnglish.getKeyboardLocale(), "en");

        KeyboardAddOnAndBuilder tester2Hebrew = getKeyboardFromFactory(TESTER_KEYBOARD_2);
        assertNotNull(tester2Hebrew);
        assertEquals(tester2Hebrew.getKeyboardLocale(), "iw");

        KeyboardAddOnAndBuilder tester3Console = getKeyboardFromFactory(TESTER_KEYBOARD_3);
        assertNotNull(tester3Console);
        assertTrue(TextUtils.isEmpty(tester3Console.getKeyboardLocale()));
    }



    public void testGetIcon() throws Exception {

    }

    public void testHasScreenshot() throws Exception {
        KeyboardAddOnAndBuilder askEnglish = getKeyboardFromFactory(ASK_ENGLISH_1);
        assertNotNull(askEnglish);
        assertTrue(askEnglish.hasScreenshot());

        KeyboardAddOnAndBuilder testerEnglish = getKeyboardFromFactory(TESTER_KEYBOARD_1);
        assertNotNull(testerEnglish);
        assertTrue(askEnglish.hasScreenshot());

        KeyboardAddOnAndBuilder tester3Console = getKeyboardFromFactory(TESTER_KEYBOARD_3);
        assertNotNull(tester3Console);
        assertFalse(tester3Console.hasScreenshot());
    }

    public void testGetScreenshot() throws Exception {

    }

    public void testCreateKeyboard() throws Exception {

    }
}
