package com.anysoftkeyboard.keyboards;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.XmlRes;
import androidx.collection.SparseArrayCompat;
import com.anysoftkeyboard.AddOnTestUtils;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.addons.DefaultAddOn;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.google.common.base.Preconditions;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class ExternalAnyKeyboardRowsTest {
    private static final KeyboardDimens SIMPLE_KeyboardDimens =
            new KeyboardDimens() {
                @Override
                public int getKeyboardMaxWidth() {
                    return 120;
                }

                @Override
                public float getKeyHorizontalGap() {
                    return 2;
                }

                @Override
                public float getRowVerticalGap() {
                    return 3;
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

                @Override
                public float getPaddingBottom() {
                    return 0;
                }
            };

    private KeyboardAddOnAndBuilder mKeyboardBuilder;

    @Before
    public void setUp() {
        mKeyboardBuilder =
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn();
    }

    @NonNull
    private AnyKeyboard createAndLoadKeyboardForModeWithTopRowIndex(
            @Keyboard.KeyboardRowModeId int mode, int topRowIndex) throws Exception {
        AnyKeyboard keyboard = Preconditions.checkNotNull(mKeyboardBuilder.createKeyboard(mode));

        KeyboardExtension topRow =
                AnyApplication.getTopRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(topRowIndex);
        KeyboardExtension bottomRow =
                AnyApplication.getBottomRowFactory(getApplicationContext()).getEnabledAddOn();
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens, topRow, bottomRow);

        verifyKeysLocationByListOrder(keyboard.getKeys());
        verifyAllEdgesOnKeyboardKeys(keyboard.getKeys());

        return keyboard;
    }

    @NonNull
    private AnyKeyboard createAndLoadKeyboardForModeWithBottomRowIndex(
            @Keyboard.KeyboardRowModeId int mode, int bottomRowIndex) throws Exception {
        AnyKeyboard keyboard = Preconditions.checkNotNull(mKeyboardBuilder.createKeyboard(mode));

        KeyboardExtension topRow =
                AnyApplication.getTopRowFactory(getApplicationContext()).getEnabledAddOn();
        KeyboardExtension bottomRow =
                AnyApplication.getBottomRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(bottomRowIndex);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens, topRow, bottomRow);

        verifyKeysLocationByListOrder(keyboard.getKeys());
        verifyAllEdgesOnKeyboardKeys(keyboard.getKeys());

        return keyboard;
    }

    @NonNull
    private AnyKeyboard createAndLoadKeyboardForModeWithRowsIndex(
            @Keyboard.KeyboardRowModeId int mode, int topRowIndex, int bottomRowIndex)
            throws Exception {
        AnyKeyboard keyboard = Preconditions.checkNotNull(mKeyboardBuilder.createKeyboard(mode));

        KeyboardExtension topRow =
                AnyApplication.getTopRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(topRowIndex);
        KeyboardExtension bottomRow =
                AnyApplication.getBottomRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(bottomRowIndex);
        keyboard.loadKeyboard(SIMPLE_KeyboardDimens, topRow, bottomRow);

        verifyKeysLocationByListOrder(keyboard.getKeys());
        verifyAllEdgesOnKeyboardKeys(keyboard.getKeys());

        return keyboard;
    }

    @Test
    public void testKeyboardRowNormalModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 0);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(36, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowImModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_IM, 0);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(36, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowEmailModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_EMAIL, 0);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(35, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowUrlModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_URL, 0);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(35, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowUrlModeNoneTopRowHasDomain() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_URL, 0);

        Assert.assertEquals(44, keyboard.getHeight());
        Assert.assertEquals(35, keyboard.getKeys().size());

        Keyboard.Key key =
                TestableAnySoftKeyboard.findKeyWithPrimaryKeyCode(KeyCodes.DOMAIN, keyboard);
        Assert.assertNotNull(key);

        Assert.assertEquals(R.xml.popup_domains, key.popupResId);

        Assert.assertEquals(".com", key.text);
        Assert.assertEquals(".com", key.label);

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_default_domain_text, ".org.il");

        keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_URL, 0);

        key = TestableAnySoftKeyboard.findKeyWithPrimaryKeyCode(KeyCodes.DOMAIN, keyboard);
        Assert.assertNotNull(key);

        Assert.assertEquals(".org.il", key.text);
        Assert.assertEquals(".org.il", key.label);

        SharedPrefsHelper.clearPrefsValue(R.string.settings_key_default_domain_text);

        keyboard = createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_URL, 0);

        key = TestableAnySoftKeyboard.findKeyWithPrimaryKeyCode(KeyCodes.DOMAIN, keyboard);
        Assert.assertNotNull(key);

        Assert.assertEquals(".com", key.text);
        Assert.assertEquals(".com", key.label);
    }

    @Test
    public void testKeyboardRowPasswordModeNoneTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, 0);

        Assert.assertEquals(54 /*extra row*/, keyboard.getHeight());
        Assert.assertEquals(46 /*additional 10 keys over normal*/, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowNormalModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 1);

        Assert.assertEquals(54, keyboard.getHeight());
        Assert.assertEquals(40, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowImModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_IM, 1);

        Assert.assertEquals(54, keyboard.getHeight());
        Assert.assertEquals(40, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowEmailModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_EMAIL, 1);

        Assert.assertEquals(54, keyboard.getHeight());
        Assert.assertEquals(39, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowUrlModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_URL, 1);

        Assert.assertEquals(54, keyboard.getHeight());
        Assert.assertEquals(39, keyboard.getKeys().size());
    }

    @Test
    public void testKeyboardRowPasswordModeSmallTopRow() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithTopRowIndex(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, 1);

        Assert.assertEquals(64 /*extra row*/, keyboard.getHeight());
        Assert.assertEquals(50 /*additional 10 keys over normal*/, keyboard.getKeys().size());
        // also, verify that the gap is correct.
        Assert.assertNotEquals(
                "For this test, we assume that the first key and the 5th are not on the same row.",
                keyboard.getKeys().get(0).y,
                keyboard.getKeys().get(4).y);

        final int expectedVerticalGap =
                keyboard.getKeys().get(4).y
                        - keyboard.getKeys().get(0).y
                        - keyboard.getKeys().get(0).height;
        Assert.assertTrue(expectedVerticalGap > 0);

        Keyboard.Key previousKey = keyboard.getKeys().get(0);
        for (int keyIndex = 0; keyIndex < keyboard.getKeys().size(); keyIndex++) {
            final Keyboard.Key currentKey = keyboard.getKeys().get(keyIndex);
            if (currentKey.y != previousKey.y) {
                final int currentVerticalGap = currentKey.y - previousKey.y - previousKey.height;
                Assert.assertEquals(
                        "Vertical gap is wrong for key index " + keyIndex,
                        expectedVerticalGap,
                        currentVerticalGap);
            }

            previousKey = currentKey;
        }
    }

    @Test
    public void testKeyboardRowEmailModeWhenEmailRowProvided() throws Exception {
        // ensuring that 4 is actually the bottom row without password specific row
        Assert.assertEquals(
                "3DFFC2AD-8BC8-47F3-962A-918156AD8DD0",
                AnyApplication.getBottomRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(4)
                        .getId());
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithBottomRowIndex(Keyboard.KEYBOARD_ROW_MODE_EMAIL, 4);

        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_EMAIL, keyboard.getKeyboardMode());
        Assert.assertEquals(
                KeyCodes.ENTER,
                keyboard.getKeys().get(keyboard.getKeys().size() - 1).getPrimaryCode());
    }

    @Test
    public void testKeyboardRowPasswordModeWhenNoPasswordRowProvided() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithBottomRowIndex(
                        Keyboard.KEYBOARD_ROW_MODE_PASSWORD, 4);
        // ensuring that 4 is actually the bottom row without password specific row
        Assert.assertEquals(
                "3DFFC2AD-8BC8-47F3-962A-918156AD8DD0",
                AnyApplication.getBottomRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(4)
                        .getId());

        Assert.assertEquals(Keyboard.KEYBOARD_ROW_MODE_PASSWORD, keyboard.getKeyboardMode());
        Assert.assertEquals(
                KeyCodes.ENTER,
                keyboard.getKeys().get(keyboard.getKeys().size() - 1).getPrimaryCode());
    }

    @Test
    public void testKeyboardWithoutMultiLayoutsEnabledIsWhenApplicable() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithRowsIndex(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 0, 6);
        // sanity
        Assert.assertEquals(
                "3659b9e0-dee2-11e0-9572-0800200c9a55",
                AnyApplication.getBottomRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(3)
                        .getId());
        Assert.assertFalse(
                AnyApplication.getKeyboardFactory(getApplicationContext()).hasMultipleAlphabets());

        // ensuring no language key exists
        Assert.assertEquals(35 /*one key was removed*/, keyboard.getKeys().size());
        List<Keyboard.Key> keys = keyboard.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            Keyboard.Key key = keys.get(i);
            Assert.assertNotEquals(
                    "Key at index " + i + " should not have code KeyCodes.MODE_ALPHABET!",
                    KeyCodes.MODE_ALPHABET,
                    key.getPrimaryCode());
            Assert.assertTrue("Key at index " + i + " should not have negative x", key.x >= 0);
        }
        // asserting key size
        Assert.assertEquals(11, keyboard.getKeys().get(keyboard.getKeys().size() - 1).width);
        Assert.assertEquals(107, keyboard.getKeys().get(keyboard.getKeys().size() - 1).x);
    }

    @Test
    public void testKeyboardWithMultiLayoutsEnabledAndKeyIsWhenApplicable() throws Exception {
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);

        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithBottomRowIndex(
                        Keyboard.KEYBOARD_ROW_MODE_NORMAL, 3);
        // sanity
        Assert.assertEquals(
                "3659b9e0-dee2-11e0-9572-0800200c9a55",
                AnyApplication.getBottomRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(3)
                        .getId());
        Assert.assertTrue(
                AnyApplication.getKeyboardFactory(getApplicationContext()).hasMultipleAlphabets());

        // ensuring there is a language key
        Assert.assertEquals(38, keyboard.getKeys().size());
        int foundLanguageKeys = 0;
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (KeyCodes.MODE_ALPHABET == key.getPrimaryCode()) foundLanguageKeys++;
        }

        Assert.assertEquals(2, foundLanguageKeys);

        Assert.assertEquals(16, keyboard.getKeys().get(keyboard.getKeys().size() - 1).width);
        Assert.assertEquals(103, keyboard.getKeys().get(keyboard.getKeys().size() - 1).x);
    }

    @Test
    public void testKeyboardWithoutMultiLayoutsEnabledAndKeyIsAlways() throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithRowsIndex(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 1, 6);
        // sanity
        Assert.assertEquals(
                "3659b9e0-dee2-11e0-9572-0800200c9a55",
                AnyApplication.getBottomRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(3)
                        .getId());
        Assert.assertFalse(
                AnyApplication.getKeyboardFactory(getApplicationContext()).hasMultipleAlphabets());

        // ensuring no language key exists
        Assert.assertEquals(39 /*one key was removed*/, keyboard.getKeys().size());
        int langKeysSeen = 0;
        List<Keyboard.Key> keys = keyboard.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            Keyboard.Key key = keys.get(i);
            if (KeyCodes.MODE_ALPHABET == key.getPrimaryCode()) {
                langKeysSeen++;
            }
            Assert.assertTrue("Key at index " + i + " should not have negative x", key.x >= 0);
        }
        Assert.assertEquals("Should have seen only one lang key!", 1, langKeysSeen);
        // asserting key size
        Assert.assertEquals(11, keyboard.getKeys().get(keyboard.getKeys().size() - 1).width);
    }

    @Test
    public void testKeyboardWithMultiLayoutsEnabledButPrefsDisabled() throws Exception {
        // asserting default settings
        Assert.assertFalse(KeyboardPrefs.alwaysHideLanguageKey(getApplicationContext()));
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_always_hide_language_key, true);

        // asserting change
        Assert.assertTrue(KeyboardPrefs.alwaysHideLanguageKey(getApplicationContext()));

        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithRowsIndex(Keyboard.KEYBOARD_ROW_MODE_NORMAL, 1, 6);
        // sanity
        Assert.assertEquals(
                "3659b9e0-dee2-11e0-9572-0800200c9a55",
                AnyApplication.getBottomRowFactory(getApplicationContext())
                        .getAllAddOns()
                        .get(3)
                        .getId());
        Assert.assertTrue(
                AnyApplication.getKeyboardFactory(getApplicationContext()).hasMultipleAlphabets());

        // ensuring no language key exists
        Assert.assertEquals(39 /*one was removed*/, keyboard.getKeys().size());
        int langKeysSeen = 0;
        List<Keyboard.Key> keys = keyboard.getKeys();
        for (int i = 0; i < keys.size(); i++) {
            Keyboard.Key key = keys.get(i);
            if (KeyCodes.MODE_ALPHABET == key.getPrimaryCode()) {
                langKeysSeen++;
            }
        }
        Assert.assertEquals("Should have seen only one lang key!", 1, langKeysSeen);
        Assert.assertEquals(11, keyboard.getKeys().get(keyboard.getKeys().size() - 1).width);
        Assert.assertEquals(107, keyboard.getKeys().get(keyboard.getKeys().size() - 1).x);
    }

    @Test
    public void testKeyboardWithoutMultiLayoutsEnabledTopRowPositionsAndGapsAreValid()
            throws Exception {
        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithBottomRowIndex(
                        Keyboard.KEYBOARD_ROW_MODE_NORMAL, 3);

        // should have four keys at top row
        final int topY = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
        Assert.assertEquals(topY, keyboard.getKeys().get(0).y);
        Assert.assertEquals(topY, keyboard.getKeys().get(1).y);
        Assert.assertEquals(topY, keyboard.getKeys().get(2).y);
        Assert.assertEquals(topY, keyboard.getKeys().get(3).y);
        // next row
        Assert.assertNotEquals(topY, keyboard.getKeys().get(4).y);

        // positions (note - keys are not evenly spread)
        // we have additional pixels now, since the language key was removed
        int[] keyIndices = new int[] {32, 33, 34, 35, 36};
        int[] xPositions = new int[] {1, 21, 71, 86, 100};
        int[] widths = new int[] {18, 48, 12, 12, 18};
        int[] gaps = new int[] {0, 0, 0, 0, 0};
        for (int keyIndexIndex = 0; keyIndexIndex < keyIndices.length; keyIndexIndex++) {
            final int keyIndex = keyIndices[keyIndexIndex];
            final int expectedX = xPositions[keyIndexIndex];
            final int expectedWidth = widths[keyIndexIndex];
            final int expectedGap = gaps[keyIndexIndex];
            final Keyboard.Key ketToTest = keyboard.getKeys().get(keyIndex);
            Assert.assertEquals(
                    "Key at index "
                            + keyIndex
                            + ", "
                            + keyIndexIndex
                            + " is not positioned correctly.",
                    expectedX,
                    ketToTest.x);
            Assert.assertEquals(
                    "Key at index "
                            + keyIndex
                            + ", "
                            + keyIndexIndex
                            + " is not the correct width.",
                    expectedWidth,
                    ketToTest.width);
            Assert.assertEquals(
                    "Key at index " + keyIndex + ", " + keyIndexIndex + " has the wrong gap.",
                    expectedGap,
                    ketToTest.gap);
        }
    }

    @Test
    public void testKeyboardWithMultiLayoutsEnabledTopRowPositionsAndGapsAreValid()
            throws Exception {
        AddOnTestUtils.ensureKeyboardAtIndexEnabled(1, true);

        AnyKeyboard keyboard =
                createAndLoadKeyboardForModeWithBottomRowIndex(
                        Keyboard.KEYBOARD_ROW_MODE_NORMAL, 3);

        // should have four keys at top row
        final int topY = (int) SIMPLE_KeyboardDimens.getRowVerticalGap();
        Assert.assertEquals(topY, keyboard.getKeys().get(0).y);
        Assert.assertEquals(topY, keyboard.getKeys().get(1).y);
        Assert.assertEquals(topY, keyboard.getKeys().get(2).y);
        Assert.assertEquals(topY, keyboard.getKeys().get(3).y);
        // next row
        Assert.assertNotEquals(topY, keyboard.getKeys().get(4).y);

        // positions (note - keys are not evenly spread)
        int[] keyIndices = new int[] {32, 33, 34, 35, 36, 37};
        int[] xPositions = new int[] {1, 19, 31, 79, 91, 103};
        int[] widths = new int[] {16, 10, 46, 10, 10, 16};
        int[] gaps = new int[] {0, 0, 0, 0, 0, 0};
        for (int keyIndexIndex = 0; keyIndexIndex < keyIndices.length; keyIndexIndex++) {
            final int keyIndex = keyIndices[keyIndexIndex];
            final int expectedX = xPositions[keyIndexIndex];
            final int expectedWidth = widths[keyIndexIndex];
            final int expectedGap = gaps[keyIndexIndex];
            final Keyboard.Key ketToTest = keyboard.getKeys().get(keyIndex);
            Assert.assertEquals(
                    "Key at index "
                            + keyIndex
                            + ", "
                            + keyIndexIndex
                            + " is not positioned correctly.",
                    expectedX,
                    ketToTest.x);
            Assert.assertEquals(
                    "Key at index "
                            + keyIndex
                            + ", "
                            + keyIndexIndex
                            + " is not the correct width.",
                    expectedWidth,
                    ketToTest.width);
            Assert.assertEquals(
                    "Key at index " + keyIndex + ", " + keyIndexIndex + " has the wrong gap.",
                    expectedGap,
                    ketToTest.gap);
        }
    }

    @Test
    public void testLetKeyboardOverrideGenericRows() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_allow_layouts_to_provide_generic_rows, true);
        TestingAnyKeyboard keyboardWithRows =
                new TestingAnyKeyboard(R.xml.keyboard_with_top_bottom_rows);
        Assert.assertEquals(6, keyboardWithRows.getKeys().size());

        TestingAnyKeyboard keyboardWithoutRows =
                new TestingAnyKeyboard(R.xml.keyboard_without_top_bottom_rows);
        Assert.assertEquals(18, keyboardWithoutRows.getKeys().size());
    }

    @Test
    public void testDoNotLetKeyboardOverrideGenericRows() {
        SharedPrefsHelper.setPrefsValue(
                R.string.settings_key_allow_layouts_to_provide_generic_rows, false);
        TestingAnyKeyboard keyboardWithRows =
                new TestingAnyKeyboard(R.xml.keyboard_with_top_bottom_rows);
        Assert.assertEquals(18, keyboardWithRows.getKeys().size());

        TestingAnyKeyboard keyboardWithoutRows =
                new TestingAnyKeyboard(R.xml.keyboard_without_top_bottom_rows);
        Assert.assertEquals(18, keyboardWithoutRows.getKeys().size());
    }

    private static class TestingAnyKeyboard extends ExternalAnyKeyboard {
        private TestingAnyKeyboard(@XmlRes int layoutResId) {
            this(getApplicationContext(), layoutResId);
        }

        private TestingAnyKeyboard(@NonNull Context context, @XmlRes int layoutResId) {
            super(
                    new DefaultAddOn(context, context),
                    context,
                    layoutResId,
                    layoutResId,
                    "name",
                    0,
                    0,
                    "en",
                    "",
                    "",
                    KEYBOARD_ROW_MODE_NORMAL);
            loadKeyboard(SIMPLE_KeyboardDimens);
        }
    }

    private void verifyLeftEdgeKeys(List<Keyboard.Key> keys) {
        Set<Integer> rowsSeen = new HashSet<>();
        for (Keyboard.Key key : keys) {
            if (rowsSeen.contains(key.y)) {
                Assert.assertFalse(
                        "Key with code "
                                + key.getPrimaryCode()
                                + ", at row Y "
                                + key.y
                                + ", should NOT have edge flag Keyboard.EDGE_LEFT!",
                        (key.edgeFlags & Keyboard.EDGE_LEFT) == Keyboard.EDGE_LEFT);
            } else {
                Assert.assertTrue(
                        "Key with code "
                                + key.getPrimaryCode()
                                + ", at row Y "
                                + key.y
                                + ", should have edge flag Keyboard.EDGE_LEFT!",
                        (key.edgeFlags & Keyboard.EDGE_LEFT) == Keyboard.EDGE_LEFT);
            }
            rowsSeen.add(key.y);
        }
    }

    private void verifyRightEdgeKeys(List<Keyboard.Key> keys) {
        SparseArrayCompat<Keyboard.Key> lastKeysAtRow = new SparseArrayCompat<>();
        for (Keyboard.Key key : keys) {
            final Keyboard.Key previousLastKey = lastKeysAtRow.get(key.y);
            if (previousLastKey != null && previousLastKey.x > key.x) continue;
            lastKeysAtRow.put(key.y, key);
        }

        for (Keyboard.Key key : keys) {
            Keyboard.Key lastKeyForRow = lastKeysAtRow.get(key.y);

            if (lastKeyForRow != key) {
                Assert.assertFalse(
                        "Key with code "
                                + key.getPrimaryCode()
                                + ", at row Y "
                                + key.y
                                + ", should NOT have edge flag Keyboard.EDGE_RIGHT!",
                        (key.edgeFlags & Keyboard.EDGE_RIGHT) == Keyboard.EDGE_RIGHT);
            } else {
                Assert.assertTrue(
                        "Key with code "
                                + key.getPrimaryCode()
                                + ", at row Y "
                                + key.y
                                + ", should have edge flag Keyboard.EDGE_RIGHT!",
                        (key.edgeFlags & Keyboard.EDGE_RIGHT) == Keyboard.EDGE_RIGHT);
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
                Assert.assertTrue(
                        "Key with code "
                                + key.getPrimaryCode()
                                + ", at row Y "
                                + key.y
                                + ", should have edge flag Keyboard.EDGE_TOP!",
                        (key.edgeFlags & Keyboard.EDGE_TOP) == Keyboard.EDGE_TOP);
            } else {
                Assert.assertFalse(
                        "Key with code "
                                + key.getPrimaryCode()
                                + ", at row Y "
                                + key.y
                                + ", should NOT have edge flag Keyboard.EDGE_TOP!",
                        (key.edgeFlags & Keyboard.EDGE_TOP) == Keyboard.EDGE_TOP);
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
                Assert.assertTrue(
                        "Key with code "
                                + key.getPrimaryCode()
                                + ", at row Y "
                                + key.y
                                + ", should have edge flag Keyboard.EDGE_BOTTOM!",
                        (key.edgeFlags & Keyboard.EDGE_BOTTOM) == Keyboard.EDGE_BOTTOM);
            } else {
                Assert.assertFalse(
                        "Key with code "
                                + key.getPrimaryCode()
                                + ", at row Y "
                                + key.y
                                + ", should NOT have edge flag Keyboard.EDGE_BOTTOM!",
                        (key.edgeFlags & Keyboard.EDGE_BOTTOM) == Keyboard.EDGE_BOTTOM);
            }
        }
    }

    private void verifyKeysLocationByListOrder(List<Keyboard.Key> keys) throws Exception {
        Keyboard.Key previousKey = null;
        for (Keyboard.Key key : keys) {
            if (previousKey != null) {
                Assert.assertTrue(
                        "Key should always be either at the next row or the same. previous: "
                                + previousKey.y
                                + ". next: "
                                + key.y,
                        previousKey.y <= key.y);
                Assert.assertTrue(
                        "Key should always be either at the next column or in a new row. previous: "
                                + previousKey.x
                                + ","
                                + previousKey.y
                                + ". next: "
                                + key.x
                                + ","
                                + key.y,
                        previousKey.y < key.y || previousKey.x < key.x);
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
