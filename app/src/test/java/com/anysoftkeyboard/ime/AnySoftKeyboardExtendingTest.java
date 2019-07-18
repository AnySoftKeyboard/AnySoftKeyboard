package com.anysoftkeyboard.ime;

import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.SoftKeyboard;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardExtendingTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testAnySoftKeyboardClassHierarchy() throws Exception {
        final String imePackage = "com.anysoftkeyboard.ime";
        final Set<Class<?>> allPossibleClasses =
                Collections.list(
                                SoftKeyboard.class
                                        .getClassLoader()
                                        .getResources(imePackage.replace('.', '/')))
                        .stream()
                        .peek(url -> System.out.println("testAnySoftKeyboardClassHierarchy " + url))
                        .map(URL::getFile)
                        .map(File::new)
                        .filter(File::isDirectory)
                        .map(File::list)
                        .flatMap(Stream::of)
                        .filter(fileName -> fileName.endsWith(".class"))
                        .filter(name -> !name.contains("Test"))
                        .filter(name -> !name.contains("$"))
                        .filter(name -> name.contains("AnySoftKeyboard"))
                        .map(
                                fileName ->
                                        fileName.substring(
                                                0, fileName.length() - ".class".length()))
                        .map(className -> String.format(Locale.US, "%s.%s", imePackage, className))
                        .map(
                                fullClassName -> {
                                    try {
                                        return Class.forName(
                                                fullClassName,
                                                true,
                                                SoftKeyboard.class.getClassLoader());
                                    } catch (ClassNotFoundException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .collect(Collectors.toSet());

        allPossibleClasses.add(AnySoftKeyboard.class);

        Class<?> superclass = SoftKeyboard.class.getSuperclass();
        Assert.assertNotNull(superclass);
        while (!superclass.equals(AnySoftKeyboardBase.class)) {
            System.out.println("ASK super " + superclass);
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
