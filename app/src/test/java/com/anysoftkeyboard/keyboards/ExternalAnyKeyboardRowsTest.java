package com.anysoftkeyboard.keyboards;

import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.google.common.base.Preconditions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
public class ExternalAnyKeyboardRowsTest {
    private static final KeyboardDimens SIMPLE_KeyboardDimens = new KeyboardDimens() {
        @Override
        public int getKeyboardMaxWidth() {
            return 120;
        }

        @Override
        public int getKeyMaxWidth() {
            return 10;
        }

        @Override
        public float getKeyHorizontalGap() {
            return 1;
        }

        @Override
        public float getRowVerticalGap() {
            return 2;
        }

        @Override
        public int getNormalKeyHeight() {
            return 5;
        }

        @Override
        public int getSmallKeyHeight() {
            return 4;
        }

        @Override
        public int getLargeKeyHeight() {
            return 6;
        }
    };

    KeyboardAddOnAndBuilder mKeyboardBuilder;

    @Before
    public void setUp() {
        mKeyboardBuilder = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application).get(0);
    }

    @NonNull
    private AnyKeyboard createAndLoadKeyboardForModeWithTopRowIndex(@Keyboard.KeyboardRowModeId int mode, int topRowIndex) throws Exception {
        AnyKeyboard keyboard = Preconditions.checkNotNull(mKeyboardBuilder.createKeyboard(RuntimeEnvironment.application, mode));

        KeyboardExtension topRow = KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_TOP).get(topRowIndex);
        KeyboardExtension bottomRow = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens, topRow, bottomRow);

        verifyKeysLocationByListOrder(keyboard.getKeys());
        verifyAllEdgesOnKeyboardKeys(keyboard.getKeys());

        return keyboard;
    }

    @NonNull
    private AnyKeyboard createAndLoadKeyboardForModeWithBottomRowIndex(@Keyboard.KeyboardRowModeId int mode, int bottomRowIndex) throws Exception {
        AnyKeyboard keyboard = Preconditions.checkNotNull(mKeyboardBuilder.createKeyboard(RuntimeEnvironment.application, mode));

        KeyboardExtension topRow = KeyboardExtensionFactory.getCurrentKeyboardExtension(RuntimeEnvironment.application, KeyboardExtension.TYPE_TOP);
        KeyboardExtension bottomRow = KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM).get(bottomRowIndex);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens, topRow, bottomRow);

        verifyKeysLocationByListOrder(keyboard.getKeys());
        verifyAllEdgesOnKeyboardKeys(keyboard.getKeys());

        return keyboard;
    }

    @Test
    public void testKeyboardRowNormalModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 0);

        Assert.assertEquals(40, keyboard.getHeight());
        Assert.assertEquals(36, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowImModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_IM, 0);

        Assert.assertEquals(40, keyboard.getHeight());
        Assert.assertEquals(36, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowEmailModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_EMAIL, 0);

        Assert.assertEquals(40, keyboard.getHeight());
        Assert.assertEquals(35, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowUrlModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_URL, 0);

        Assert.assertEquals(40, keyboard.getHeight());
        Assert.assertEquals(35, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowPasswordModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, 0);

        Assert.assertEquals(46/*extra row*/, keyboard.getHeight());
        Assert.assertEquals(46/*additional 10 keys over normal*/, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowNormalModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 1);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(40, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowImModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_IM, 1);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(40, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowEmailModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_EMAIL, 1);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(39, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowUrlModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_URL, 1);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(39, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowPasswordModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, 1);

        Assert.assertEquals(50/*extra row*/, keyboard.getHeight());
        Assert.assertEquals(50/*additional 10 keys over normal*/, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowEmailModeWhenEmailRowProvided() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithBottomRowIndex(Keyboard.KEYBOARD_ROW_MODE_EMAIL, 4);
        //ensuring that 4 is actually the bottom row without password specific row
        Assert.assertEquals("3DFFC2AD-8BC8-47F3-962A-918156AD8DD0", KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM).get(4).getId());

        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_EMAIL, keyboard.getKeyboardMode());
        Assert.assertEquals(KeyCodes.ENTER, keyboard.getKeys().get(keyboard.getKeys().size() - 1).getPrimaryCode());
    }

    @Test
    public void testKeyboardRowPasswordModeWhenNoPasswordRowProvided() throws Exception {
        AnyKeyboard keyboard = createAndLoadKeyboardForModeWithBottomRowIndex(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, 4);
        //ensuring that 4 is actually the bottom row without password specific row
        Assert.assertEquals("3DFFC2AD-8BC8-47F3-962A-918156AD8DD0", KeyboardExtensionFactory.getAllAvailableExtensions(RuntimeEnvironment.application, KeyboardExtension.TYPE_BOTTOM).get(4).getId());

        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, keyboard.getKeyboardMode());
        Assert.assertEquals(KeyCodes.ENTER, keyboard.getKeys().get(keyboard.getKeys().size() - 1).getPrimaryCode());
    }

    private void verifyLeftEdgeKeys(List<Keyboard.Key> keys) throws Exception {
        Set<Integer> rowsSeen = new HashSet<>();
        for (Keyboard.Key key : keys) {
            if (rowsSeen.contains(key.y)) {
                Assert.assertFalse("Key with code " + key.codes[0] + ", at row Y " + key.y + ", should NOT have edge flag Keyboard.EDGE_LEFT!", (key.edgeFlags & Keyboard.EDGE_LEFT) == Keyboard.EDGE_LEFT);
            } else {
                Assert.assertTrue("Key with code " + key.codes[0] + ", at row Y " + key.y + ", should have edge flag Keyboard.EDGE_LEFT!", (key.edgeFlags & Keyboard.EDGE_LEFT) == Keyboard.EDGE_LEFT);
            }
            rowsSeen.add(key.y);
        }
    }

    private void verifyRightEdgeKeys(List<Keyboard.Key> keys) throws Exception {
        SparseArrayCompat<Keyboard.Key> lastKeysAtRow = new SparseArrayCompat<>();
        for (Keyboard.Key key : keys) {
            final Keyboard.Key previousLastKey = lastKeysAtRow.get(key.y);
            if (previousLastKey != null && previousLastKey.x > key.x) continue;
            lastKeysAtRow.put(key.y, key);
        }

        for (Keyboard.Key key : keys) {
            Keyboard.Key lastKeyForRow = lastKeysAtRow.get(key.y);

            if (lastKeyForRow != key) {
                Assert.assertFalse("Key with code " + key.codes[0] + ", at row Y " + key.y + ", should NOT have edge flag Keyboard.EDGE_RIGHT!", (key.edgeFlags & Keyboard.EDGE_RIGHT) == Keyboard.EDGE_RIGHT);
            } else {
                Assert.assertTrue("Key with code " + key.codes[0] + ", at row Y " + key.y + ", should have edge flag Keyboard.EDGE_RIGHT!", (key.edgeFlags & Keyboard.EDGE_RIGHT) == Keyboard.EDGE_RIGHT);
            }
        }
    }

    private void verifyTopEdgeKeys(List<Keyboard.Key> keys) throws Exception {
        int topY = Integer.MAX_VALUE;
        for (Keyboard.Key key : keys) {
            if (key.y < topY) topY = key.y;
        }

        for (Keyboard.Key key : keys) {
            if (key.y == topY) {
                Assert.assertTrue("Key with code " + key.codes[0] + ", at row Y " + key.y + ", should have edge flag Keyboard.EDGE_TOP!", (key.edgeFlags & Keyboard.EDGE_TOP) == Keyboard.EDGE_TOP);
            } else {
                Assert.assertFalse("Key with code " + key.codes[0] + ", at row Y " + key.y + ", should NOT have edge flag Keyboard.EDGE_TOP!", (key.edgeFlags & Keyboard.EDGE_TOP) == Keyboard.EDGE_TOP);
            }
        }
    }

    private void verifyBottomEdgeKeys(List<Keyboard.Key> keys) throws Exception {
        int lastY = 0;
        for (Keyboard.Key key : keys) {
            if (key.y > lastY) lastY = key.y;
        }

        for (Keyboard.Key key : keys) {
            if (key.y == lastY) {
                Assert.assertTrue("Key with code " + key.codes[0] + ", at row Y " + key.y + ", should have edge flag Keyboard.EDGE_BOTTOM!", (key.edgeFlags & Keyboard.EDGE_BOTTOM) == Keyboard.EDGE_BOTTOM);
            } else {
                Assert.assertFalse("Key with code " + key.codes[0] + ", at row Y " + key.y + ", should NOT have edge flag Keyboard.EDGE_BOTTOM!", (key.edgeFlags & Keyboard.EDGE_BOTTOM) == Keyboard.EDGE_BOTTOM);
            }
        }
    }

    private void verifyKeysLocationByListOrder(List<Keyboard.Key> keys) throws Exception {
        Keyboard.Key previousKey = null;
        for (Keyboard.Key key : keys) {
            if (previousKey != null) {
                Assert.assertTrue("Key should always be either at the next row or the same", previousKey.y <= key.y);
                Assert.assertTrue("Key should always be either at the next column or in a new row", previousKey.y < key.y || previousKey.x < key.x);

            }

            previousKey = key;
        }
    }

    private void verifyAllEdgesOnKeyboardKeys(List<Keyboard.Key> keys) throws Exception {
        verifyTopEdgeKeys(keys);
        verifyBottomEdgeKeys(keys);
        verifyRightEdgeKeys(keys);
        verifyLeftEdgeKeys(keys);
    }
}