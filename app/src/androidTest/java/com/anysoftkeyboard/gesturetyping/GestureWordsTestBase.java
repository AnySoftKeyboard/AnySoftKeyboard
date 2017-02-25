package com.anysoftkeyboard.gesturetyping;

import android.support.annotation.NonNull;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBase;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBaseInstrumentationTest;

import org.junit.Assert;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@SmallTest
/**
 * Base test for getGestureWords
 */
public class GestureWordsTestBase extends AnyKeyboardViewBaseInstrumentationTest {

    private List<CharSequence> wordsInPath;
    private List<Integer> frequenciesInPath = null;

    @Override
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardViewBase view) {
        super.setCreatedKeyboardView(view);

        //TODO
        wordsInPath = new ArrayList<>();
        wordsInPath.add("the");
        wordsInPath.add("quick");
        wordsInPath.add("brown");
        wordsInPath.add("fox");
        wordsInPath.add("jumped");
        wordsInPath.add("over");
        wordsInPath.add("lazy");
        wordsInPath.add("dog");
        wordsInPath.add("need");
        wordsInPath.add("more");
        wordsInPath.add("words");
    }

    protected void testGivenInput(String name, String expected, Point... recorded) {
        List<Keyboard.Key> keys = mEnglishKeyboard.getKeys();
        int width = mUnderTest.getWidth();
        int height = mUnderTest.getHeight();

        List<Point> gestureInput = new ArrayList<>();

        for (Point p : recorded) {
            gestureInput.add(new Point(p.x*width, p.y*height));
        }

        List<CharSequence> gestureTypingPossibilities = GestureTypingDetector.getGestureWords(gestureInput,
                wordsInPath, frequenciesInPath, keys);

        Log.i("GestureWordsTestBase", "For " + name + " (expecting " + expected + ") got " + gestureTypingPossibilities);
        Assert.assertTrue(gestureTypingPossibilities.size()>=1);

        GestureTypingDebugUtils.DEBUG_INPUT.clear();
        GestureTypingDebugUtils.DEBUG_INPUT.addAll(gestureInput);
        GestureTypingDebugUtils.DEBUG = true;
        GestureTypingDebugUtils.DEBUG_WORD = gestureTypingPossibilities.get(0);
        GestureTypingDebugUtils.DEBUG_KEYS = keys;
        GestureTypingDebugUtils.keyboardWidth = width;
        GestureTypingDebugUtils.keyboardHeight = height;

        saveScreenshot(name);

        Assert.assertEquals(gestureTypingPossibilities.get(0), expected);
    }
}
