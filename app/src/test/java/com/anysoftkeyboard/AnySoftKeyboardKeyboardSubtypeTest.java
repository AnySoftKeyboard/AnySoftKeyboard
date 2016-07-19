package com.anysoftkeyboard;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.inputmethod.InputMethodSubtype;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AnySoftKeyboardKeyboardSubtypeTest extends AnySoftKeyboardBaseTest {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Test
    public void testSubtypeReported() {
        ArgumentCaptor<InputMethodSubtype> subtypeArgumentCaptor = ArgumentCaptor.forClass(InputMethodSubtype.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setInputMethodAndSubtype(
                Mockito.notNull(IBinder.class),
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
                subtypeArgumentCaptor.capture());
        final InputMethodSubtype subtypeArgumentCaptorValue = subtypeArgumentCaptor.getValue();
        Assert.assertNotNull(subtypeArgumentCaptorValue);
        Assert.assertEquals("en", subtypeArgumentCaptorValue.getLocale());
        Assert.assertEquals("keyboard_c7535083-4fe6-49dc-81aa-c5438a1a343a", subtypeArgumentCaptorValue.getExtraValue());
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Test
    public void testAvailableSubtypesReported() {
        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());
        //inputMethodManager.setAdditionalInputMethodSubtypes(imeId, subtypes.toArray(new InputMethodSubtype[subtypes.size()]));
        ArgumentCaptor<InputMethodSubtype[]> subtypesCaptor = ArgumentCaptor.forClass(InputMethodSubtype[].class);
        final List<KeyboardAddOnAndBuilder> keyboardBuilders = KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application);
        mAnySoftKeyboardUnderTest.onAvailableKeyboardsChanged(keyboardBuilders);

        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setAdditionalInputMethodSubtypes(
                Mockito.eq(new ComponentName("com.menny.android.anysoftkeyboard", "com.menny.android.anysoftkeyboard.SoftKeyboard").flattenToShortString()),
                subtypesCaptor.capture());

        InputMethodSubtype[] reportedSubtypes = subtypesCaptor.getValue();
        Assert.assertNotNull(reportedSubtypes);
        Assert.assertTrue(reportedSubtypes.length > 0);
        Assert.assertEquals(keyboardBuilders.size(), reportedSubtypes.length);
        for (int builderIndex = 0; builderIndex < keyboardBuilders.size(); builderIndex++) {
            KeyboardAddOnAndBuilder builder = keyboardBuilders.get(builderIndex);
            InputMethodSubtype subtype = reportedSubtypes[builderIndex];
            Assert.assertEquals(builder.getKeyboardLocale(), subtype.getLocale());
            Assert.assertEquals(builder.getId(), subtype.getExtraValue());
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardSwitchedOnCurrentInputMethodSubtypeChanged() {
        //enabling ALL keyboards for this test
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).edit();
        for (KeyboardAddOnAndBuilder builder : KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application)) {
            editor.putBoolean(builder.getId(), true);
        }
        editor.commit();

        final KeyboardAddOnAndBuilder keyboardBuilder = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(1);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputMethodManager());
        InputMethodSubtype subtype = new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilder.getId())
                .setSubtypeLocale(keyboardBuilder.getKeyboardLocale())
                .build();
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(subtype);
        ArgumentCaptor<InputMethodSubtype> subtypeArgumentCaptor = ArgumentCaptor.forClass(InputMethodSubtype.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputMethodManager()).setInputMethodAndSubtype(
                Mockito.notNull(IBinder.class),
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
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).edit();
        for (KeyboardAddOnAndBuilder builder : KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application)) {
            editor.putBoolean(builder.getId(), true);
        }
        editor.commit();

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilder = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(1);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilder.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());

        //now simulating the report from the OS
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilder.getId())
                .setSubtypeLocale(keyboardBuilder.getKeyboardLocale())
                .build());

        //ensuring the keyboard WAS NOT changed
        Assert.assertSame(keyboardBuilder.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardDoesNotSwitchOnDelayedSubtypeReported() {
        //enabling ALL keyboards for this test
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).edit();
        for (KeyboardAddOnAndBuilder builder : KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application)) {
            editor.putBoolean(builder.getId(), true);
        }
        editor.commit();

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(1);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderOne.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());

        //NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        //ensuring keyboard was changed
        final KeyboardAddOnAndBuilder keyboardBuilderTwo = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(2);
        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());

        //now simulating the report from the OS for the first change
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderOne.getId())
                .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                .build());

        //ensuring the keyboard WAS NOT changed
        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardDoesSwitchIfNoDelayedSubtypeReported() {
        //enabling ALL keyboards for this test
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).edit();
        for (KeyboardAddOnAndBuilder builder : KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application)) {
            editor.putBoolean(builder.getId(), true);
        }
        editor.commit();

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(1);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderOne.getId())
                .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                .build());
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderOne.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());

        //NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderTwo = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(2);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderTwo.getId())
                .setSubtypeLocale(keyboardBuilderTwo.getKeyboardLocale())
                .build());
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());

        //and changing again (loop the keyboard)
        final KeyboardAddOnAndBuilder keyboardBuilderZero = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(0);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderZero.getId())
                .setSubtypeLocale(keyboardBuilderZero.getKeyboardLocale())
                .build());
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderZero.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testKeyboardSwitchOnUserSubtypeChanged() {
        //enabling ALL keyboards for this test
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).edit();
        for (KeyboardAddOnAndBuilder builder : KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application)) {
            editor.putBoolean(builder.getId(), true);
        }
        editor.commit();

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(1);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderOne.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        //now simulating the report from the OS for the first change
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderOne.getId())
                .setSubtypeLocale(keyboardBuilderOne.getKeyboardLocale())
                .build());

        //simulating a user subtype switch
        final KeyboardAddOnAndBuilder keyboardBuilderTwo = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(2);
        mAnySoftKeyboardUnderTest.simulateCurrentSubtypeChanged(new InputMethodSubtype.InputMethodSubtypeBuilder()
                .setSubtypeExtraValue(keyboardBuilderTwo.getId())
                .setSubtypeLocale(keyboardBuilderTwo.getKeyboardLocale())
                .build());

        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());

        //and changing again (loop the keyboard)
        final KeyboardAddOnAndBuilder keyboardBuilderZero = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(0);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderZero.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

    @Test
    @Ignore("Robolectric does not support gingerbread")
    @Config(sdk=Build.VERSION_CODES.GINGERBREAD_MR1)
    public void testKeyboardDoesSwitchWithoutSubtypeReported() {
        //enabling ALL keyboards for this test
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application).edit();
        for (KeyboardAddOnAndBuilder builder : KeyboardFactory.getAllAvailableKeyboards(RuntimeEnvironment.application)) {
            editor.putBoolean(builder.getId(), true);
        }
        editor.commit();

        //switching to the next keyboard
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderOne = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(1);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderOne.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
        //NOT reporting, and performing another language change
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderTwo = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(2);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderTwo.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.MODE_ALPHABET);
        final KeyboardAddOnAndBuilder keyboardBuilderThree = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application).get(3);
        //ensuring keyboard was changed
        Assert.assertSame(keyboardBuilderThree.getId(), mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeyboardPrefId());
    }

}