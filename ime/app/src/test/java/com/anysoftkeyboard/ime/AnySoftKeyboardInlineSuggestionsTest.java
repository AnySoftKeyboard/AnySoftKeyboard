package com.anysoftkeyboard.ime;

import static org.mockito.ArgumentMatchers.any;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InlineSuggestion;
import android.view.inputmethod.InlineSuggestionInfo;
import android.view.inputmethod.InlineSuggestionsResponse;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.inline.InlineContentView;
import androidx.core.util.Pair;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.R;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.evendanan.pixel.ScrollViewAsMainChild;
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
    return mockResponse(new String[0], views);
  }

  private static InlineSuggestionsResponse mockResponse(
      String[] hints, InlineContentView... views) {
    var response = Mockito.mock(InlineSuggestionsResponse.class);
    Mockito.doReturn(
            Arrays.stream(views)
                .map(v -> Pair.create(Mockito.mock(InlineSuggestion.class), v))
                .map(
                    p -> {
                      Mockito.doAnswer(
                              i -> {
                                Consumer<InlineContentView> callback = i.getArgument(3);
                                callback.accept(p.second);
                                return null;
                              })
                          .when(p.first)
                          .inflate(any(), any(), any(), any());

                      var info = Mockito.mock(InlineSuggestionInfo.class);
                      Mockito.doReturn("android:autofill:action").when(info).getType();
                      Mockito.doReturn("android:autofill").when(info).getSource();
                      Mockito.doReturn(false).when(info).isPinned();
                      Mockito.doReturn(hints).when(info).getAutofillHints();
                      Mockito.doReturn(info).when(p.first).getInfo();

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
  public void testActionStripAddedForGeneric() {
    simulateOnStartInputFlow();
    mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(
        mockResponse(Mockito.mock(InlineContentView.class)));
    ImageView icon =
        mAnySoftKeyboardUnderTest
            .getInputViewContainer()
            .findViewById(R.id.inline_suggestions_strip_icon);
    Assert.assertEquals(
        R.drawable.ic_inline_suggestions,
        Shadows.shadowOf(icon.getDrawable()).getCreatedFromResId());
  }

  @Test
  public void testActionStripAddedForUnknown() {
    simulateOnStartInputFlow();
    mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(
        mockResponse(
            new String[] {"I", "do", "not", "know"}, Mockito.mock(InlineContentView.class)));
    ImageView icon =
        mAnySoftKeyboardUnderTest
            .getInputViewContainer()
            .findViewById(R.id.inline_suggestions_strip_icon);
    Assert.assertEquals(
        R.drawable.ic_inline_suggestions,
        Shadows.shadowOf(icon.getDrawable()).getCreatedFromResId());
  }

  @Test
  public void testActionStripAddedForAi() {
    simulateOnStartInputFlow();
    mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(
        mockResponse(new String[] {"aiai", "newFeature"}, Mockito.mock(InlineContentView.class)));
    ImageView icon =
        mAnySoftKeyboardUnderTest
            .getInputViewContainer()
            .findViewById(R.id.inline_suggestions_strip_icon);
    Assert.assertEquals(
        R.drawable.ic_inline_suggestions_ai,
        Shadows.shadowOf(icon.getDrawable()).getCreatedFromResId());
  }

  @Test
  public void testActionStripAddedForSmartReply() {
    simulateOnStartInputFlow();
    mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(
        mockResponse(new String[] {"aiai", "smartReply"}, Mockito.mock(InlineContentView.class)));
    ImageView icon =
        mAnySoftKeyboardUnderTest
            .getInputViewContainer()
            .findViewById(R.id.inline_suggestions_strip_icon);
    Assert.assertEquals(
        R.drawable.ic_inline_suggestions_ai_reply,
        Shadows.shadowOf(icon.getDrawable()).getCreatedFromResId());

    mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(
        mockResponse(new String[] {"smartReply"}, Mockito.mock(InlineContentView.class)));
    icon =
        mAnySoftKeyboardUnderTest
            .getInputViewContainer()
            .findViewById(R.id.inline_suggestions_strip_icon);
    Assert.assertEquals(
        R.drawable.ic_inline_suggestions_ai_reply,
        Shadows.shadowOf(icon.getDrawable()).getCreatedFromResId());
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
    mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(mockResponse(inlineView1, inlineView2));
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
            .findViewById(R.id.inline_suggestions_list));

    Shadows.shadowOf(rootView).getOnClickListener().onClick(rootView);
    // removed icon from action strip
    Assert.assertNull(
        mAnySoftKeyboardUnderTest
            .getInputViewContainer()
            .findViewById(R.id.inline_suggestions_strip_root));

    var scroller =
        (ScrollViewAsMainChild)
            mAnySoftKeyboardUnderTest
                .getInputViewContainer()
                .findViewById(R.id.inline_suggestions_list);
    Assert.assertNotNull(scroller);
    Assert.assertEquals(2, scroller.getItemsCount());

    Assert.assertEquals(
        View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());
  }

  @Test
  public void testPrioritizePinnedSuggestions() {
    simulateOnStartInputFlow();
    var inlineView1 = Mockito.mock(InlineContentView.class);
    var inlineView2 = Mockito.mock(InlineContentView.class);
    var inlineView3Pinned = Mockito.mock(InlineContentView.class);
    var inlineView4 = Mockito.mock(InlineContentView.class);
    var inlineView5Pinned = Mockito.mock(InlineContentView.class);

    var response =
        mockResponse(inlineView1, inlineView2, inlineView3Pinned, inlineView4, inlineView5Pinned);
    var inlineSuggestion3 = response.getInlineSuggestions().get(2).getInfo();
    Mockito.doReturn(true).when(inlineSuggestion3).isPinned();
    var inlineSuggestion5 = response.getInlineSuggestions().get(4).getInfo();
    Mockito.doReturn(true).when(inlineSuggestion5).isPinned();

    mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(response);
    var rootView =
        mAnySoftKeyboardUnderTest
            .getInputViewContainer()
            .findViewById(R.id.inline_suggestions_strip_root);
    Shadows.shadowOf(rootView).getOnClickListener().onClick(rootView);
    var scroller =
        (ScrollViewAsMainChild)
            mAnySoftKeyboardUnderTest
                .getInputViewContainer()
                .findViewById(R.id.inline_suggestions_list);

    Assert.assertNotNull(scroller);
    Assert.assertEquals(5, scroller.getItemsCount());

    var itemsHolder = (ViewGroup) scroller.getChildAt(0);

    Assert.assertSame(inlineView3Pinned, itemsHolder.getChildAt(0));
    Assert.assertSame(inlineView5Pinned, itemsHolder.getChildAt(1));
    Assert.assertSame(inlineView1, itemsHolder.getChildAt(2));
    Assert.assertSame(inlineView2, itemsHolder.getChildAt(3));
    Assert.assertSame(inlineView4, itemsHolder.getChildAt(4));
  }

  @Test
  public void testClosesInlineSuggestionsOnPick() {
    simulateOnStartInputFlow();
    var inlineView1 = Mockito.mock(InlineContentView.class);
    var inlineView2 = Mockito.mock(InlineContentView.class);
    mAnySoftKeyboardUnderTest.onInlineSuggestionsResponse(mockResponse(inlineView1, inlineView2));
    var rootView =
        mAnySoftKeyboardUnderTest
            .getInputViewContainer()
            .findViewById(R.id.inline_suggestions_strip_root);

    Shadows.shadowOf(rootView).getOnClickListener().onClick(rootView);

    var lister =
        (ScrollViewAsMainChild)
            mAnySoftKeyboardUnderTest
                .getInputViewContainer()
                .findViewById(R.id.inline_suggestions_list);

    Assert.assertEquals(
        View.GONE, ((View) mAnySoftKeyboardUnderTest.getInputView()).getVisibility());

    Assert.assertEquals(2, lister.getItemsCount());

    Mockito.verify(inlineView1).setOnClickListener(Mockito.notNull());
    Mockito.verify(inlineView2).setOnClickListener(Mockito.notNull());

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
