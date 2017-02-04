package com.anysoftkeyboard;

import android.graphics.Color;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.menny.android.anysoftkeyboard.R;
import com.menny.android.anysoftkeyboard.SoftKeyboard;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.util.ServiceController;

@RunWith(RobolectricTestRunner.class)
public class AnySoftKeyboardTest {

    private ServiceController<SoftKeyboard> mAnySoftKeyboardUnderTest;

    @Before
    public void setUp() throws Exception {
        mAnySoftKeyboardUnderTest = Robolectric.buildService(SoftKeyboard.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSimpleLifeCycle() throws Exception {
        mAnySoftKeyboardUnderTest.attach().create().destroy();
    }

    @Test
    public void testOnCreateInputView() throws Exception {
        View mainKeyboardView = mAnySoftKeyboardUnderTest.attach().create().get().onCreateInputView();
        Assert.assertNotNull(mainKeyboardView);
        Assert.assertTrue(mainKeyboardView instanceof KeyboardViewContainerView);
        KeyboardViewContainerView containerView = (KeyboardViewContainerView) mainKeyboardView;
        Assert.assertEquals(1, containerView.getChildCount());
        Assert.assertNotNull(containerView.getChildAt(0));
        Assert.assertTrue(containerView.getChildAt(0) instanceof AnyKeyboardView);
    }

    @Test
    public void testOnCreateCandidatesView() throws Exception {
        View candidatesRootView = mAnySoftKeyboardUnderTest.attach().create().get().onCreateCandidatesView();
        Assert.assertNotNull(candidatesRootView);
        View candidateView = candidatesRootView.findViewById(R.id.candidates);
        Assert.assertNotNull(candidateView);
        Assert.assertTrue(candidateView instanceof CandidateView);

        mAnySoftKeyboardUnderTest.get().setCandidatesView(candidatesRootView);

        View closeStripView = candidatesRootView.findViewById(R.id.close_suggestions_strip_icon);
        Assert.assertNotNull(closeStripView);
        Assert.assertTrue(closeStripView instanceof ImageView);
        View closeStripTextView = candidatesRootView.findViewById(R.id.close_suggestions_strip_text);
        Assert.assertNotNull(closeStripTextView);
        Assert.assertTrue(closeStripTextView instanceof TextView);
    }

    @Test
    public void testCandidateViewCloseTextAnimation() throws Exception {
        View candidatesRootView = mAnySoftKeyboardUnderTest.attach().create().get().onCreateCandidatesView();
        mAnySoftKeyboardUnderTest.get().setCandidatesView(candidatesRootView);

        View closeStripTextView = candidatesRootView.findViewById(R.id.close_suggestions_strip_text);
        View closeStripView = candidatesRootView.findViewById(R.id.close_suggestions_strip_icon);
        View.OnClickListener closeListener = Shadows.shadowOf(closeStripView).getOnClickListener();
        Assert.assertNotNull(closeListener);

        Assert.assertEquals(View.GONE, closeStripTextView.getVisibility());
        closeListener.onClick(closeStripView);
        Assert.assertEquals(View.VISIBLE, closeStripTextView.getVisibility());

        final long doubleTapDelay = 2 * 1000 - 50;

        ShadowSystemClock.sleep(doubleTapDelay - 1);
        Assert.assertEquals(View.VISIBLE, closeStripTextView.getVisibility());
        ShadowSystemClock.sleep(2);
        Assert.assertEquals(View.GONE, closeStripTextView.getVisibility());
    }

    @Test
    public void testCandidateViewCloseBehavior() throws Exception {
        View candidatesRootView = mAnySoftKeyboardUnderTest.attach().create().get().onCreateCandidatesView();
        mAnySoftKeyboardUnderTest.get().setCandidatesView(candidatesRootView);

        View closeStripTextView = candidatesRootView.findViewById(R.id.close_suggestions_strip_text);
        View closeStripView = candidatesRootView.findViewById(R.id.close_suggestions_strip_icon);
        View.OnClickListener closeIconListener = Shadows.shadowOf(closeStripView).getOnClickListener();
        closeIconListener.onClick(closeStripView);
        View.OnClickListener closeListener = Shadows.shadowOf(closeStripTextView).getOnClickListener();

        closeIconListener.onClick(closeStripView);
        closeListener.onClick(closeStripTextView);

        Assert.assertEquals(View.GONE, closeStripTextView.getVisibility());
    }

    @Test
    public void testKeyboardHiddenBehavior() throws Exception {
        ServiceController<TestableAnySoftKeyboard> testableAnySoftKeyboardServiceController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        TestableAnySoftKeyboard testableAnySoftKeyboard = testableAnySoftKeyboardServiceController.attach().create().get();
        Assert.assertTrue(testableAnySoftKeyboard.isKeyboardViewHidden());

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();

        testableAnySoftKeyboard.onCreateInputView();
        testableAnySoftKeyboard.onStartInput(editorInfo, false);

        Assert.assertTrue(testableAnySoftKeyboard.isKeyboardViewHidden());
        testableAnySoftKeyboard.onStartInputView(editorInfo, false);
        Assert.assertFalse(testableAnySoftKeyboard.isKeyboardViewHidden());

        testableAnySoftKeyboardServiceController.destroy();
        Assert.assertTrue(testableAnySoftKeyboard.isKeyboardViewHidden());
    }

    @Test
    public void testKeyboardDoesNotCloseWhenUserCancelKey() throws Exception {
        ServiceController<TestableAnySoftKeyboard> testableAnySoftKeyboardServiceController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        TestableAnySoftKeyboard testableAnySoftKeyboard = testableAnySoftKeyboardServiceController.attach().create().get();
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();

        testableAnySoftKeyboard.onCreateInputView();
        testableAnySoftKeyboard.onStartInput(editorInfo, false);
        testableAnySoftKeyboard.onStartInputView(editorInfo, false);
        Assert.assertFalse(testableAnySoftKeyboard.isKeyboardViewHidden());

        testableAnySoftKeyboard.onCancel();
        Assert.assertFalse(testableAnySoftKeyboard.isKeyboardViewHidden());
    }

    @Test
    public void testExtractViewThemeSet() throws Exception {
        ServiceController<TestableAnySoftKeyboard> testableAnySoftKeyboardServiceController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        TestableAnySoftKeyboard testableAnySoftKeyboard = testableAnySoftKeyboardServiceController.attach().create().get();
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();

        testableAnySoftKeyboard.onCreateInputView();
        testableAnySoftKeyboard.onStartInput(editorInfo, false);
        testableAnySoftKeyboard.onStartInputView(editorInfo, false);

        final View extractView = testableAnySoftKeyboard.onCreateExtractTextView();
        Assert.assertNotNull(extractView);

        final EditText extractEditText = (EditText) extractView.findViewById(android.R.id.inputExtractEditText);
        Assert.assertNotNull(extractEditText);

        testableAnySoftKeyboard.updateFullscreenMode();

        Assert.assertEquals(R.drawable.lean_dark_gray_keyboard_background, Shadows.shadowOf(extractView.getBackground()).getCreatedFromResId());
        Assert.assertEquals(Color.WHITE, extractEditText.getTextColors().getDefaultColor());
    }



    @Test
    public void testExtractViewThemeNotSetWithoutInputViewCreated() throws Exception {
        ServiceController<TestableAnySoftKeyboard> testableAnySoftKeyboardServiceController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        TestableAnySoftKeyboard testableAnySoftKeyboard = testableAnySoftKeyboardServiceController.attach().create().get();
        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();

        final View extractView = testableAnySoftKeyboard.onCreateExtractTextView();
        Assert.assertNotNull(extractView);

        final EditText extractEditText = (EditText) extractView.findViewById(android.R.id.inputExtractEditText);
        Assert.assertNotNull(extractEditText);

        testableAnySoftKeyboard.updateFullscreenMode();

        Assert.assertNull(extractView.getBackground());
        Assert.assertNotEquals(Color.WHITE, extractEditText.getTextColors().getDefaultColor());

    }

}