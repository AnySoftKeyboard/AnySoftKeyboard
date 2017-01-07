package com.anysoftkeyboard;

import com.anysoftkeyboard.gesturetyping.Point;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewBaseTest;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AnySoftKeyboardGestureTypingTest extends AnySoftKeyboardBaseTest {

    private static class TestData {
        List<Point> pathPoints;
        int[] keyCodesInPath;
        int pathLength;
    }

    private TestData generateTestDataForPath(int... keyCodesInPath) {
        List<Point> pointsList = new ArrayList<>();

        for (int keyCodeIndex = 0; keyCodeIndex < keyCodesInPath.length; keyCodeIndex++) {
            Keyboard.Key key = AnyKeyboardViewBaseTest.findKey(mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests(), keyCodesInPath[keyCodeIndex]);
            pointsList.add(new Point(key.x, key.y));
        }

        TestData testData = new TestData();
        testData.pathPoints = pointsList;
        testData.keyCodesInPath = keyCodesInPath;
        testData.pathLength = keyCodesInPath.length;

        return testData;
    }

    @Before
    @Override
    public void setUpForAnySoftKeyboardBase() throws Exception {
        SharedPrefsHelper.setPrefsValue(R.string.settings_key_gesture_typing, true);
        super.setUpForAnySoftKeyboardBase();
    }

    @Test
    public void testNoSuggestionsAndNoOutputIfThereAreEmptySuggestions() {
        mAnySoftKeyboardUnderTest.resetMockCandidateView();

        ((TestableAnySoftKeyboard.TestableSuggest) mAnySoftKeyboardUnderTest.getSpiedSuggest()).setSuggestionsForPath(/*empty*/);
        TestData testData = generateTestDataForPath('a', 's');
        mAnySoftKeyboardUnderTest.onGestureTypingInput(testData.pathPoints, testData.keyCodesInPath, testData.pathLength);

        //no output
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        //no interaction
        verifyNoSuggestionsInteractions();
    }

    @Test
    public void testNoSuggestionsButHasOutputIfThereIsOneSuggestions() {
        mAnySoftKeyboardUnderTest.resetMockCandidateView();

        ((TestableAnySoftKeyboard.TestableSuggest) mAnySoftKeyboardUnderTest.getSpiedSuggest()).setSuggestionsForPath("as", 1);
        TestData testData = generateTestDataForPath('a', 's');
        mAnySoftKeyboardUnderTest.onGestureTypingInput(testData.pathPoints, testData.keyCodesInPath, testData.pathLength);

        Assert.assertEquals("as", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        //was set to empty suggestions
        verifySuggestions(true);
    }

    @Test
    public void testHasSuggestionsButHasOutputIfThereMoreThanOneSuggestions() {
        mAnySoftKeyboardUnderTest.resetMockCandidateView();

        ((TestableAnySoftKeyboard.TestableSuggest) mAnySoftKeyboardUnderTest.getSpiedSuggest()).setSuggestionsForPath("as", 2, "ass", 1);
        TestData testData = generateTestDataForPath('a', 's');
        mAnySoftKeyboardUnderTest.onGestureTypingInput(testData.pathPoints, testData.keyCodesInPath, testData.pathLength);

        Assert.assertEquals("as", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        verifySuggestions(true, "as", "ass");
    }

    @Test
    public void testAutoSpaceAfterSecondGesture() {
        ((TestableAnySoftKeyboard.TestableSuggest) mAnySoftKeyboardUnderTest.getSpiedSuggest()).setSuggestionsForPath("as", 2, "ass", 1);
        TestData testData = generateTestDataForPath('a', 's');
        mAnySoftKeyboardUnderTest.onGestureTypingInput(testData.pathPoints, testData.keyCodesInPath, testData.pathLength);
        Assert.assertEquals("as", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.onGestureTypingInput(testData.pathPoints, testData.keyCodesInPath, testData.pathLength);
        Assert.assertEquals("as as", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.onGestureTypingInput(testData.pathPoints, testData.keyCodesInPath, testData.pathLength);
        Assert.assertEquals("as as as", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }
}