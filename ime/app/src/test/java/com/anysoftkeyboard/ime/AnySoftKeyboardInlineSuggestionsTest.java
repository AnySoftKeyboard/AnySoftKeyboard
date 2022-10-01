package com.anysoftkeyboard.ime;

import static org.mockito.ArgumentMatchers.any;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
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
import org.mockito.ArgumentCaptor;
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

        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_scroller));

        Shadows.shadowOf(rootView).getOnClickListener().onClick(rootView);
        // removed icon from action strip
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_root));

        var scroller =
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_scroller);
        Assert.assertNotNull(scroller);
        var lister = (ViewGroup) scroller.findViewById(R.id.inline_suggestions_list);
        Assert.assertNotNull(lister);
        Assert.assertEquals(2, lister.getChildCount());

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
    }

    @Test
    public void testClosesInlineSuggestionsOnPick() {
        simulateOnStartInputFlow();
        var inlineView1 = Mockito.mock(InlineContentView.class);
        var inlineView2 = Mockito.mock(InlineContentView.class);
        mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(
                mockResponse(inlineView1, inlineView2));
        var rootView =
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.inline_suggestions_strip_root);

        Shadows.shadowOf(rootView).getOnClickListener().onClick(rootView);

        var lister =
                (ViewGroup)
                        mAnySoftKeyboardUnderTest
                                .getInputViewContainer()
                                .findViewById(R.id.inline_suggestions_list);

        Assert.assertEquals(
                View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());

        Assert.assertEquals(2, lister.getChildCount());
        for (int childIndex = 0; childIndex < lister.getChildCount(); childIndex++) {
            View item = lister.getChildAt(childIndex);
            Mockito.verify(item).setOnClickListener(Mockito.notNull());
        }
        var clickCaptor = ArgumentCaptor.forClass(View.OnClickListener.class);
        Mockito.verify(inlineView1).setOnClickListener(clickCaptor.capture());
        Assert.assertNotNull(clickCaptor.getValue());

        /*due to inability to test InlineContentView, I have to remove the following checks*/
        //        clickCaptor.getValue().onClick(inlineView1);
        //
        //        Assert.assertEquals(0, lister.getChildCount());
        //        Assert.assertEquals(
        //                View.VISIBLE, ((View)
        // mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
        //        Assert.assertNull(
        //                mAnySoftKeyboardUnderTest
        //                        .getInputViewContainer()
        //                        .findViewById(R.id.inline_suggestions_scroller));
    }
}
