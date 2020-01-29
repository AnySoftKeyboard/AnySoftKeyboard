package com.anysoftkeyboard.prefs.backup;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class PrefsRootTest {

    @Test
    public void testProperties() {
        PrefsRoot root = new PrefsRoot(3);
        Assert.assertEquals(3, root.getVersion());
    }
}
