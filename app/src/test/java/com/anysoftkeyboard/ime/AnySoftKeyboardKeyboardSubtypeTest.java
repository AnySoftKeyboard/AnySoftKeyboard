package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.os.Build;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodSubtype;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.SupportTest;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.menny.android.anysoftkeyboard.AnyApplication;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardKeyboardSubtypeTest extends AnySoftKeyboardBaseTest {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testSubtypeReported() {
        ArgumentCaptor<InputMethodSubtype> subtypeArgumentCaptor =
                ArgumentCaptor.forClass(InputMethodSubtype.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager())
                .setInputMethodAndSubtype(
                        Mockito.notNull(),
                        Mockito.eq(
                                new ComponentName(
                                                "com.menny.android.anysoftkeyboard",
                                                "com.menny.android.anysoftkeyboard.SoftKeyboard")
                                        .flattenToShortString()),
                        subtypeArgumentCaptor.capture());
        final InputMethodSubtype subtypeArgumentCaptorValue = subtypeArgumentCaptor.getValue();
        Assert.assertNotNull(subtypeArgumentCaptorValue);
        Assert.assertEquals("en", subtypeArgumentCaptorValue.getLocale());
        Assert.assertEquals(
                "c7535083-4fe6-49dc-81aa-c5438a1a343a", subtypeArgumentCaptorValue.getExtraValue());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Test
    public void testAvailableSubtypesReported() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());
        ArgumentCaptor<InputMethodSubtype[]> subtypesCaptor =
                ArgumentCaptor.forClass(InputMethodSubtype[].class);
        final List<KeyboardAddOnAndBuilder> keyboardBuilders =
                AnyApplication.getKeyboardFactory(getApplicationContext()).getAllAddOns();
        mAnySoftKeyboardUnderTest.onAvailableKeyboardsChanged(keyboardBuilders);

        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager())
                .setAdditionalInputMethodSubtypes(
                        Mockito.eq(
                                new ComponentName(
                                                "com.menny.android.anysoftkeyboard",
                                                "com.menny.android.anysoftkeyboard.SoftKeyboard")
                                        .flattenToShortString()),
                        subtypesCaptor.capture());

        InputMethodSubtype[] reportedSubtypes = subtypesCaptor.getValue();
        Assert.assertNotNull(reportedSubtypes);
        Assert.assertEquals(10, keyboardBuilders.size());
        Assert.assertEquals(8, reportedSubtypes.length);
        final int[] expectedSubtypeId =
                new int[] {
                    1912895432,
                    -1829357470,
                    390463609,
                    1819490062,
                    1618259652,
                    -517805346,
                    -1601329810,
                    -1835196376
                };
        Assert.assertEquals(reportedSubtypes.length, expectedSubtypeId.length);
        int reportedIndex = 0;
        for (KeyboardAddOnAndBuilder builder : keyboardBuilders) {
            if (!TextUtils.isEmpty(builder.getKeyboardLocale())) {
                InputMethodSubtype subtype = reportedSubtypes[reportedIndex];
                Assert.assertEquals(builder.getKeyboardLocale(), subtype.getLocale());
                Assert.assertEquals(builder.getId(), subtype.getExtraValue());
                Assert.assertEquals("keyboard", subtype.getMode());
                Assert.assertEquals(
                        "Expected different subtypeid for " + builder.getId() + " " + reportedIndex,
                        expectedSubtypeId[reportedIndex],
                        ReflectionHelpers.<Integer>getField(subtype, "mSubtypeId").intValue());

                reportedIndex++;
            }
        }
        Assert.assertEquals(reportedIndex, reportedSubtypes.length);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testAvailableSubtypesReportedWithLanguageTag() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());

        ArgumentCaptor<InputMethodSubtype[]> subtypesCaptor =
                ArgumentCaptor.forClass(InputMethodSubtype[].class);
        final List<KeyboardAddOnAndBuilder> keyboardBuilders =
                AnyApplication.getKeyboardFactory(getApplicationContext()).getAllAddOns();
        mAnySoftKeyboardUnderTest.onAvailableKeyboardsChanged(keyboardBuilders);

        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager())
                .setAdditionalInputMethodSubtypes(
                        Mockito.eq(
                                new ComponentName(
                                                "com.menny.android.anysoftkeyboard",
                                                "com.menny.android.anysoftkeyboard.SoftKeyboard")
                                        .flattenToShortString()),
                        subtypesCaptor.capture());

        InputMethodSubtype[] reportedSubtypes = subtypesCaptor.getValue();
        Assert.assertNotNull(reportedSubtypes);

        int reportedIndex = 0;
        for (KeyboardAddOnAndBuilder builder : keyboardBuilders) {
            if (!TextUtils.isEmpty(builder.getKeyboardLocale())) {
                InputMethodSubtype subtype = reportedSubtypes[reportedIndex];
                Assert.assertEquals(builder.getKeyboardLocale(), subtype.getLocale());
                Assert.assertEquals(builder.getKeyboardLocale(), subtype.getLanguageTag());
                reportedIndex++;
            }
        }
        Assert.assertEquals(reportedIndex, reportedSubtypes.length);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardSwitchedOnCurrentInputMethodSubtypeChanged() {
        // enabling ALL keyboards for this test
        for (int i = 0;
                i
                        < AnyApplication.getKeyboardFactory(getApplicationContext())
                                .getAllAddOns()
                                .size();
                i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        final KeyboardAddOnAndBuilder keyboardBuilder =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(1);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());
        InputMethodSubtype subtype =
                new InputMethodSubtype.InputMethodSubtypeBuilder()
                        .setSubtypeExtraValue(keyboardBuilder.getId().toString())
                        .setSubtypeLocale(keyboardBuilder.getKeyboardLocale())
                        .build();
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(subtype);
        ArgumentCaptor<InputMethodSubtype> subtypeArgumentCaptor =
                ArgumentCaptor.forClass(InputMethodSubtype.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager())
                .setInputMethodAndSubtype(
                        Mockito.notNull(),
                        Mockito.eq(
                                new ComponentName(
                                                "com.menny.android.anysoftkeyboard",
                                                "com.menny.android.anysoftkeyboard.SoftKeyboard")
                                        .flattenToShortString()),
                        subtypeArgumentCaptor.capture());
        final InputMethodSubtype subtypeArgumentCaptorValue = subtypeArgumentCaptor.getValue();
        Assert.assertNotNull(subtypeArgumentCaptorValue);
        Assert.assertEquals(
                keyboardBuilder.getKeyboardLocale(), subtypeArgumentCaptorValue.getLocale());
        Assert.assertEquals(keyboardBuilder.getId(), subtypeArgumentCaptorValue.getExtraValue());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardDoesNotSwitchOnCurrentSubtypeReported() {
        // enabling ALL keyboards for this test
        for (int i = 0;
                i
                        < AnyApplication.getKeyboardFactory(getApplicationContext())
                                .getAllAddOns()
                                .size();
                i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }
        simulateOnStartInputFlow();

        // switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilder =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(1);
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilder.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        // now simulating the report from the OS
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(
                new InputMethodSubtype.InputMethodSubtypeBuilder()
                        .setSubtypeExtraValue(keyboardBuilder.getId().toString())
                        .setSubtypeLocale(keyboardBuilder.getKeyboardLocale())
                        .build());

        // ensuring the keyboard WAS NOT changed
        Assert.assertSame(
                keyboardBuilder.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardDoesNotSwitchOnDelayedSubtypeReported() {
        // enabling ALL keyboards for this test
        for (int i = 0;
                i
                        < AnyApplication.getKeyboardFactory(getApplicationContext())
                                .getAllAddOns()
                                .size();
                i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        simulateOnStartInputFlow();
        // switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(1);
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilderOne.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        // NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        // ensuring keyboard was changed
        final KeyboardAddOnAndBuilder keyboardBuilderTwo =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(2);
        Assert.assertSame(
                keyboardBuilderTwo.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        // now simulating the report from the OS for the first change
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(
                new InputMethodSubtype.InputMethodSubtypeBuilder()
                        .setSubtypeExtraValue(keyboardBuilderOne.getId().toString())
                        .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                        .build());

        // ensuring the keyboard WAS NOT changed
        Assert.assertSame(
                keyboardBuilderTwo.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardDoesSwitchIfNoDelayedSubtypeReported() {
        // enabling ALL keyboards for this test
        for (int i = 0;
                i
                        < AnyApplication.getKeyboardFactory(getApplicationContext())
                                .getAllAddOns()
                                .size();
                i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        simulateOnStartInputFlow();
        // switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(1);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(
                new InputMethodSubtype.InputMethodSubtypeBuilder()
                        .setSubtypeExtraValue(keyboardBuilderOne.getId().toString())
                        .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                        .build());
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilderOne.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        // NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderTwo =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(2);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(
                new InputMethodSubtype.InputMethodSubtypeBuilder()
                        .setSubtypeExtraValue(keyboardBuilderTwo.getId().toString())
                        .setSubtypeLocale(keyboardBuilderTwo.getKeyboardLocale())
                        .build());
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilderTwo.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        // and changing again (loop the keyboard)
        final KeyboardAddOnAndBuilder keyboardBuilderZero =
                AnyApplication.getKeyboardFactory(getApplicationContext()).getEnabledAddOn();
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(
                new InputMethodSubtype.InputMethodSubtypeBuilder()
                        .setSubtypeExtraValue(keyboardBuilderZero.getId().toString())
                        .setSubtypeLocale(keyboardBuilderZero.getKeyboardLocale())
                        .build());
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilderZero.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardSwitchOnUserSubtypeChanged() {
        // enabling ALL keyboards for this test
        for (int i = 0;
                i
                        < AnyApplication.getKeyboardFactory(getApplicationContext())
                                .getAllAddOns()
                                .size();
                i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        simulateOnStartInputFlow();
        // switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(1);
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilderOne.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
        // now simulating the report from the OS for the first change
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(
                new InputMethodSubtype.InputMethodSubtypeBuilder()
                        .setSubtypeExtraValue(keyboardBuilderOne.getId().toString())
                        .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                        .build());

        // simulating a user subtype switch
        final KeyboardAddOnAndBuilder keyboardBuilderTwo =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(2);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(
                new InputMethodSubtype.InputMethodSubtypeBuilder()
                        .setSubtypeExtraValue(keyboardBuilderTwo.getId().toString())
                        .setSubtypeLocale(keyboardBuilderTwo.getKeyboardLocale())
                        .build());

        Assert.assertSame(
                keyboardBuilderTwo.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        // and changing again (loop the keyboard)
        final KeyboardAddOnAndBuilder nextKeyboard =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(3);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        // ensuring keyboard was changed
        Assert.assertSame(
                nextKeyboard.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }

    @Test
    @Ignore("Robolectric does not support gingerbread")
    @Config(sdk = Build.VERSION_CODES.GINGERBREAD_MR1)
    public void testKeyboardDoesSwitchWithoutSubtypeReported() {
        // enabling ALL keyboards for this test
        for (int i = 0;
                i
                        < AnyApplication.getKeyboardFactory(getApplicationContext())
                                .getAllAddOns()
                                .size();
                i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        simulateOnStartInputFlow();
        // switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(1);
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilderOne.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
        // NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderTwo =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(2);
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilderTwo.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderThree =
                AnyApplication.getKeyboardFactory(getApplicationContext())
                        .getEnabledAddOns()
                        .get(3);
        // ensuring keyboard was changed
        Assert.assertSame(
                keyboardBuilderThree.getId(),
                mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId().toString());
    }
}
