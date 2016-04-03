package com.anysoftkeyboard;

import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.CandidateView;
import com.menny.android.anysoftkeyboard.AskGradleTestRunner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.util.ServiceController;

import java.util.List;

@RunWith(AskGradleTestRunner.class)
public class AnySoftKeyboardClipboardTest {

    private TestableAnySoftKeyboard mAnySoftKeyboardUnderTest;

    @Before
    public void setUp() throws Exception {
        ServiceController<TestableAnySoftKeyboard> anySoftKeyboardController = Robolectric.buildService(TestableAnySoftKeyboard.class);
        mAnySoftKeyboardUnderTest = anySoftKeyboardController.attach().create().get();

        final EditorInfo editorInfo = TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions();
        mAnySoftKeyboardUnderTest.setInputView(mAnySoftKeyboardUnderTest.onCreateInputView());
        mAnySoftKeyboardUnderTest.onStartInput(editorInfo, false);
        mAnySoftKeyboardUnderTest.onStartInputView(editorInfo, false);

        Robolectric.flushBackgroundThreadScheduler();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSelectsAllText() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing something very long";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals("", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT_ALL);
        Assert.assertEquals(expectedText, inputConnection.getSelectedText(0).toString());
    }
}