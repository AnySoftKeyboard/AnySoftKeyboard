package com.anysoftkeyboard.keyboards;

import android.text.TextUtils;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class KeyboardAddOnTest {

	public static final String ASK_ENGLISH_1 = "c7535083-4fe6-49dc-81aa-c5438a1a343a";
	public static final String TESTER_KEYBOARD_1 = "aef7f690-f485-11e2-b778-0800200c9a60";
	public static final String TESTER_KEYBOARD_2 = "aef7f690-f485-11e2-b778-0800200c9a61";
	public static final String TESTER_KEYBOARD_3 = "aef7f690-f485-11e2-b778-0800200c9a62";

	@Ignore("For some reason Robolectric(?) doesn't like missing attributes, although Android is fine with that")
	@Test
	public void testGetKeyboardDefaultEnabled() throws Exception {
		List<KeyboardAddOnAndBuilder> enabledKeyboards = KeyboardFactory.getEnabledKeyboards(Robolectric.application);
		//checking that ASK English is enabled
		boolean askEnglishEnabled = false;
		for (KeyboardAddOnAndBuilder addOnAndBuilder : enabledKeyboards) {
			if (addOnAndBuilder.getId().contains(ASK_ENGLISH_1)) {
				assertTrue(addOnAndBuilder.getKeyboardDefaultEnabled());
				assertEquals(addOnAndBuilder.getPackageName(), Robolectric.application.getPackageName());
				askEnglishEnabled = true;
			}
		}
		assertTrue(askEnglishEnabled);

		//now checking my tester keyboard
		boolean testerEnglishEnabled = false;
		for (KeyboardAddOnAndBuilder addOnAndBuilder : enabledKeyboards) {
			if (addOnAndBuilder.getId().contains(TESTER_KEYBOARD_1)) {
				assertTrue(addOnAndBuilder.getKeyboardDefaultEnabled());
				assertEquals(addOnAndBuilder.getPackageName(), Robolectric.application.getPackageName());
				testerEnglishEnabled = true;
			}
		}
		assertTrue(testerEnglishEnabled);

		//now checking my tester keyboard 2
		boolean tester2EnglishEnabled = false;
		for (KeyboardAddOnAndBuilder addOnAndBuilder : enabledKeyboards) {
			if (addOnAndBuilder.getId().contains(TESTER_KEYBOARD_2)) {
				tester2EnglishEnabled = true;
			}
		}
		assertFalse(tester2EnglishEnabled);
	}

	private KeyboardAddOnAndBuilder getKeyboardFromFactory(String id) {
		List<KeyboardAddOnAndBuilder> keyboards = KeyboardFactory.getAllAvailableKeyboards(Robolectric.application);

		for (KeyboardAddOnAndBuilder addOnAndBuilder : keyboards) {
			if (addOnAndBuilder.getId().equals(KeyboardAddOnAndBuilder.KEYBOARD_PREF_PREFIX + id)) {
				return addOnAndBuilder;
			}
		}

		return null;
	}

	@Ignore("For some reason Robolectric(?) doesn't like missing attributes, although Android is fine with that")
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

	@Test
	public void testGetIcon() throws Exception {

	}

	@Ignore("For some reason Robolectric(?) doesn't like missing attributes, although Android is fine with that")
	@Test
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

	@Test
	public void testGetScreenshot() throws Exception {

	}

	@Test
	public void testCreateKeyboard() throws Exception {

	}
}
