package com.anysoftkeyboard.dictionaries;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class TextEntryStateTest {
    @Before
    public void setUp() throws Exception {
        TextEntryState.newSession();
    }

    @After
    public void tearDown() throws Exception {
        TextEntryState.reset();
    }

    @Test
    public void testReset() throws Exception {
        Assert.assertEquals(TextEntryState.State.START, TextEntryState.getState());
        TextEntryState.typedCharacter('h', false);
        Assert.assertNotEquals(TextEntryState.State.START, TextEntryState.getState());

        TextEntryState.reset();
        Assert.assertEquals(TextEntryState.State.START, TextEntryState.getState());
    }
}