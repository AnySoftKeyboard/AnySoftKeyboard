package com.anysoftkeyboard;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AskPrefsImplTest {

    @Test
    public void testConvertsOldEnabledKeyboardsToNewEnabledKeyboardsValue() {
        //keyboard_ID
    }

    @Test
    public void testDoesNotConvertsOldEnabledKeyboardsToNewEnabledKeyboardsValueIfConfigVersionIsHighEnough() {

    }

    @Test
    public void testDoesNotConvertsOldDictionaryOverrideToNewEnabledKeyboardsValue() {

    }

    @Test
    public void testConvertsOldDictionaryOverrideToNewDictionaryOverrideValue() {
        //KEYBOARD_PREFIX + ID + DICTIONARY_PREFIX + ID override_dictionary
        //value is dictionary_ID
    }

    @Test
    public void testDoesNotConvertsOldDictionaryOverrideToNewDictionaryOverrideValueIfConfigVersionIsHighEnough() {

    }

}