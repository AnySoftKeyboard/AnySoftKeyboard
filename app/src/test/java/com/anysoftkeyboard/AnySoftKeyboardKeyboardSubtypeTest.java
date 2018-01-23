package com.anysoftkeyboard;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.os.Build;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.addons.SupportTest;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardKeyboardSubtypeTest extends AnySoftKeyboardBaseTest {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testSubtypeReported() {
        ArgumentCaptor<InputMethodSubtype> subtypeArgumentCaptor = ArgumentCaptor.forClass(InputMethodSubtype.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setInputMethodAndSubtype(
                Mockito.notNull(),
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
                subtypeArgumentCaptor.capture());
        final InputMethodSubtype subtypeArgumentCaptorValue = subtypeArgumentCaptor.getValue();
        Assert.assertNotNull(subtypeArgumentCaptorValue);
        Assert.assertEquals("en", subtypeArgumentCaptorValue.getLocale());
        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", subtypeArgumentCaptorValue.getExtraValue());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Test
    public void testAvailableSubtypesReported() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());
        ArgumentCaptor<InputMethodSubtype[]> subtypesCaptor = ArgumentCaptor.forClass(InputMethodSubtype[].class);
        final List<KeyboardAddOnAndBuilder> keyboardBuilders = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns();
        mAnySoftKeyboardUnderTest.onAvailableKeyboardsChanged(keyboardBuilders);

        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setAdditionalInputMethodSubtypes(
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
                subtypesCaptor.capture());

        InputMethodSubtype[] reportedSubtypes = subtypesCaptor.getValue();
        Assert.assertNotNull(reportedSubtypes);
        Assert.assertEquals(7, keyboardBuilders.size());
        Assert.assertEquals(6, reportedSubtypes.length);
        final int[] expectedSubtypeId = new int[]{
                1912895432,
                -1829357470,
                390463609,
                1819490062,
                1618259652,
                -1601329810
        };
        Assert.assertEquals(reportedSubtypes.length, expectedSubtypeId.length);
        int reportedIndex = 0;
        for (KeyboardAddOnAndBuilder builder : keyboardBuilders) {
            if (TextUtils.isEmpty(builder.getKeyboardLocale())) {
                //Terminal does not have a locale, and should not be in the list of languages.
                Assert.assertEquals("Terminal", builder.getName());
                Assert.assertEquals("b1c24b40-02ce-4857-9fb8-fb9e4e3b4318", builder.getId());
            } else {
                InputMethodSubtype subtype = reportedSubtypes[reportedIndex];
                Assert.assertEquals(builder.getKeyboardLocale(), subtype.getLocale());
                Assert.assertEquals(builder.getId(), subtype.getExtraValue());
                Assert.assertEquals("keyboard", subtype.getMode());
                Assert.assertEquals("Expected different subtypeid for " + builder.getId(), expectedSubtypeId[reportedIndex], ReflectionHelpers.<Integer>getField(subtype, "mSubtypeId").intValue());

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

        ArgumentCaptor<InputMethodSubtype[]> subtypesCaptor = ArgumentCaptor.forClass(InputMethodSubtype[].class);
        final List<KeyboardAddOnAndBuilder> keyboardBuilders = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns();
        mAnySoftKeyboardUnderTest.onAvailableKeyboardsChanged(keyboardBuilders);

        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setAdditionalInputMethodSubtypes(
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
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
        //enabling ALL keyboards for this test
        for (int i = 0; i < AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns().size(); i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        final KeyboardAddOnAndBuilder keyboardBuilder = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(1);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());
        InputMethodSubtype subtype = new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilder.getId().toString())
                .setSubtypeLocale(keyboardBuilder.getKeyboardLocale())
                .build();
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(subtype);
        ArgumentCaptor<InputMethodSubtype> subtypeArgumentCaptor = ArgumentCaptor.forClass(InputMethodSubtype.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setInputMethodAndSubtype(
                Mockito.notNull(),
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
                subtypeArgumentCaptor.capture());
        final InputMethodSubtype subtypeArgumentCaptorValue = subtypeArgumentCaptor.getValue();
        Assert.assertNotNull(subtypeArgumentCaptorValue);
        Assert.assertEquals(keyboardBuilder.getKeyboardLocale(), subtypeArgumentCaptorValue.getLocale());
        Assert.assertEquals(keyboardBuilder.getId(), subtypeArgumentCaptorValue.getExtraValue());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardDoesNotSwitchOnCurrentSubtypeReported() {
        //enabling ALL keyboards for this test
        for (int i = 0; i < AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns().size(); i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilder = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(1);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilder.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());

        //now simulating the report from the OS
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilder.getId().toString())
                .setSubtypeLocale(keyboardBuilder.getKeyboardLocale())
                .build());

        //ensuring the keyboard WAS NOT changed
        Assert.assertSame(keyboardBuilder.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardDoesNotSwitchOnDelayedSubtypeReported() {
        //enabling ALL keyboards for this test
        for (int i = 0; i < AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns().size(); i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(1);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderOne.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());

        //NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        //ensuring keyboard was changed
        final KeyboardAddOnAndBuilder keyboardBuilderTwo = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(2);
        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());

        //now simulating the report from the OS for the first change
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderOne.getId().toString())
                .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                .build());

        //ensuring the keyboard WAS NOT changed
        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardDoesSwitchIfNoDelayedSubtypeReported() {
        //enabling ALL keyboards for this test
        for (int i = 0; i < AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns().size(); i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(1);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderOne.getId().toString())
                .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                .build());
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderOne.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());

        //NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderTwo = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(2);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderTwo.getId().toString())
                .setSubtypeLocale(keyboardBuilderTwo.getKeyboardLocale())
                .build());
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());

        //and changing again (loop the keyboard)
        final KeyboardAddOnAndBuilder keyboardBuilderZero = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOn();
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderZero.getId().toString())
                .setSubtypeLocale(keyboardBuilderZero.getKeyboardLocale())
                .build());
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderZero.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardSwitchOnUserSubtypeChanged() {
        //enabling ALL keyboards for this test
        for (int i = 0; i < AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns().size(); i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(1);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderOne.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        //now simulating the report from the OS for the first change
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderOne.getId().toString())
                .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                .build());

        //simulating a user subtype switch
        final KeyboardAddOnAndBuilder keyboardBuilderTwo = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(2);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderTwo.getId().toString())
                .setSubtypeLocale(keyboardBuilderTwo.getKeyboardLocale())
                .build());

        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());

        //and changing again (loop the keyboard)
        final KeyboardAddOnAndBuilder nextKeyboard = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(3);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        //ensuring keyboard was changed
        Assert.assertSame(nextKeyboard.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
    }

    @Test
    @Ignore("Robolectric does not support gingerbread")
    @Config(sdk = Build.VERSION_CODES.GINGERBREAD_MR1)
    public void testKeyboardDoesSwitchWithoutSubtypeReported() {
        //enabling ALL keyboards for this test
        for (int i = 0; i < AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getAllAddOns().size(); i++) {
            SupportTest.ensureKeyboardAtIndexEnabled(i, true);
        }

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(1);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderOne.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
        //NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderTwo = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(2);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderThree = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application).getEnabledAddOns().get(3);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderThree.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardId());
    }

}