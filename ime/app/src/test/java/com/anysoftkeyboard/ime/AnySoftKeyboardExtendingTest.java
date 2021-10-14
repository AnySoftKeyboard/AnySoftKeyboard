package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardExtendingTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testAnySoftKeyboardClassHierarchy() throws Exception {
        final Set<Class<?>> allPossibleClasses =
                new HashSet<>(
                        Arrays.asList(
                                com.anysoftkeyboard.ime.AnySoftKeyboardBase.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardClipboard.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardTagsSearcher.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardMediaInsertion.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardNightMode.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardPowerSaving.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardPressEffects.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardColorizeNavBar.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardWithGestureTyping.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardSwipeListener.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardWithQuickText.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardSuggestions.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardInlineSuggestions.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardThemeOverlay.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardHardware.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardIncognito.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardDialogProvider.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardPopText.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardRxPrefs.class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardKeyboardSwitchedListener
                                        .class,
                                com.anysoftkeyboard.ime.AnySoftKeyboardService.class,
                                com.anysoftkeyboard.saywhat.PublicNotices.class,
                                com.anysoftkeyboard.AnySoftKeyboard.class));

        Class<?> superclass = SoftKeyboard.class.getSuperclass();
        Assert.assertNotNull(superclass);
        while (!superclass.equals(AnySoftKeyboardBase.class)) {
            Assert.assertTrue(
                    "Class "
                            + superclass
                            + " is not in the allPossibleClasses set! Was it removed?",
                    allPossibleClasses.remove(superclass));
            superclass = superclass.getSuperclass();
            Assert.assertNotNull(superclass);
        }

        final String errorMessage =
                "Still have classes in set: "
                        + String.join(
                                ", ",
                                allPossibleClasses.stream()
                                        .map(Object::toString)
                                        .collect(Collectors.toList()));

        Assert.assertEquals(errorMessage, 1, allPossibleClasses.size());
        Assert.assertTrue(allPossibleClasses.contains(AnySoftKeyboardBase.class));
    }
}
