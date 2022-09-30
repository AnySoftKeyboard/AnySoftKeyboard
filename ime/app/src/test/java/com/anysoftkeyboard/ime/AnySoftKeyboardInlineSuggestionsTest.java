package com.anysoftkeyboard.ime;

import static org.mockito.ArgumentMatchers.any;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InlineSuggestion;
import android.view.inputmethod.InlineSuggestionsResponse;
import android.widget.TextView;
import android.widget.inline.InlineContentView;
import androidx.core.util.Pair;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.R;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.R)
public class AnySoftKeyboardInlineSuggestionsTest extends AnySoftKeyboardBaseTest {
    private static InlineSuggestionsResponse mockResponse(InlineContentView... views) {
        var response = Mockito.mock(InlineSuggestionsResponse.class);
        Mockito.doReturn(
                        Arrays.stream(views)
                                .map(v -> Pair.create(Mockito.mock(InlineSuggestion.class), v))
                                .map(
                                        p -> {
                                            Mockito.doAnswer(
                                                            i -> {
                                                                Consumer<InlineContentView>
                                                                        callback = i.getArgument(3);
                                                                callback.accept(p.second);
                                                                return null;
                                                            })
                                                    .when(p.first)
                                                    .inflate(any(), any(), any(), any());
                                            return p.first;
                                        })
                                .collect(Collectors.toList()))
                .when(response)
                .getInlineSuggestions();
        return response;
    }

    @Test
    public void testActionStripNotAddedIfEmptySuggestions() {
        simulateOnStartInputFlow();
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_root));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(mockResponse()));
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_root));
    }

    @Test
    public void testActionStripAdded() {
        simulateOnStartInputFlow();
        mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(
                mockResponse(Mockito.mock(InlineContentView.class)));
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_root));
        TextView countText =
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_text);
        Assert.assertNotNull(countText);
        Assert.assertEquals("1", countText.getText().toString());
    }

    @Test
    public void testCreatesCorrectRequest() {
        simulateOnStartInputFlow();
        var request = mAnySoftKeyboardUnderTest.onCreateInlineSuggestionsRequest(new Bundle());
        Assert.assertNotNull(request);
        var specs = request.getInlinePresentationSpecs();
        Assert.assertEquals(1, specs.size());
    }

    @Test
    public void testShowsSuggestionsOnClickAction() {
        simulateOnStartInputFlow();
        var inlineView1 = Mockito.mock(InlineContentView.class);
        var inlineView2 = Mockito.mock(InlineContentView.class);
        mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(
                mockResponse(inlineView1, inlineView2));
        var rootView =
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_root);
        TextView countText =
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_text);
        Assert.assertEquals("2", countText.getText().toString());

        Assert.assertEquals(
                View.GONE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getInlineScrollView()
                        .getVisibility());
        Assert.assertEquals(
                0,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getInlineAutofillView()
                        .getChildCount());

        Shadows.shadowOf(rootView).getOnClickListener().onClick(rootView);
        // removed icon from action strip
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_root));

        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getInlineScrollView()
                        .getVisibility());
        Assert.assertEquals(
                View.VISIBLE,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getInlineAutofillView()
                        .getVisibility());
        Assert.assertEquals(
                2,
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .getInlineAutofillView()
                        .getChildCount());
    }
}
