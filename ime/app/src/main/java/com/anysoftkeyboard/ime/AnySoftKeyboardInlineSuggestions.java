package com.anysoftkeyboard.ime;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InlineSuggestion;
import android.view.inputmethod.InlineSuggestionsRequest;
import android.view.inputmethod.InlineSuggestionsResponse;
import android.widget.TextView;
import android.widget.inline.InlinePresentationSpec;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.arch.core.util.Function;
import androidx.autofill.inline.UiVersions;
import androidx.autofill.inline.v1.InlineSuggestionUi;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.evendanan.pixel.ScrollViewAsMainChild;

public abstract class AnySoftKeyboardInlineSuggestions extends AnySoftKeyboardSuggestions {

  private final InlineSuggestionsAction mInlineSuggestionAction;

  public AnySoftKeyboardInlineSuggestions() {
    super();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      mInlineSuggestionAction =
          new InlineSuggestionsAction(this::showSuggestions, this::removeActionStrip);
    } else {
      mInlineSuggestionAction = new InlineSuggestionsAction(l -> null, this::removeActionStrip);
    }
  }

  @Override
  public void onFinishInputView(boolean finishingInput) {
    super.onFinishInputView(finishingInput);
    cleanUpInlineLayouts(true);
    removeActionStrip();
  }

  @Override
  protected boolean handleCloseRequest() {
    return super.handleCloseRequest() || cleanUpInlineLayouts(true);
  }

  @RequiresApi(Build.VERSION_CODES.R)
  @Nullable @Override
  public InlineSuggestionsRequest onCreateInlineSuggestionsRequest(@NonNull Bundle uiExtras) {
    final var inputViewContainer = getInputViewContainer();
    if (inputViewContainer == null) return null;
    // min size is a thumb
    final Size smallestSize =
        new Size(
            getResources().getDimensionPixelOffset(R.dimen.inline_suggestion_min_width),
            getResources().getDimensionPixelOffset(R.dimen.inline_suggestion_min_height));
    // max size is the keyboard
    final Size biggestSize =
        new Size(
            inputViewContainer.getWidth(),
            getResources().getDimensionPixelOffset(R.dimen.inline_suggestion_max_height));

    UiVersions.StylesBuilder stylesBuilder = UiVersions.newStylesBuilder();

    InlineSuggestionUi.Style style = InlineSuggestionUi.newStyleBuilder().build();
    stylesBuilder.addStyle(style);

    Bundle stylesBundle = stylesBuilder.build();

    InlinePresentationSpec spec =
        new InlinePresentationSpec.Builder(smallestSize, biggestSize)
            .setStyle(stylesBundle)
            .build();

    List<InlinePresentationSpec> specList = new ArrayList<>();
    specList.add(spec);

    InlineSuggestionsRequest.Builder builder = new InlineSuggestionsRequest.Builder(specList);

    return builder
        .setMaxSuggestionCount(InlineSuggestionsRequest.SUGGESTION_COUNT_UNLIMITED)
        .build();
  }

  @RequiresApi(Build.VERSION_CODES.R)
  @Override
  public boolean onInlineSuggestionsResponse(@NonNull InlineSuggestionsResponse response) {
    final List<InlineSuggestion> inlineSuggestions = response.getInlineSuggestions();

    if (inlineSuggestions.size() > 0) {
      mInlineSuggestionAction.onNewSuggestions(inlineSuggestions);
      getInputViewContainer().addStripAction(mInlineSuggestionAction, true);
      getInputViewContainer().setActionsStripVisibility(true);
    }

    return inlineSuggestions.size() > 0;
  }

  private void removeActionStrip() {
    getInputViewContainer().removeStripAction(mInlineSuggestionAction);
  }

  private boolean cleanUpInlineLayouts(boolean reshowStandardKeyboard) {
    if (reshowStandardKeyboard) {
      View standardKeyboardView = (View) getInputView();
      if (standardKeyboardView != null) {
        standardKeyboardView.setVisibility(View.VISIBLE);
      }
    }
    var inputViewContainer = getInputViewContainer();
    if (inputViewContainer != null) {
      if (inputViewContainer.findViewById(R.id.inline_suggestions_list)
          instanceof ScrollViewAsMainChild lister) {
        lister.removeAllListItems();
        inputViewContainer.removeView(lister);
        return true;
      }
    }
    return false;
  }

  @RequiresApi(Build.VERSION_CODES.R)
  private Void showSuggestions(List<InlineSuggestion> inlineSuggestions) {
    cleanUpInlineLayouts(false);

    var inputViewContainer = getInputViewContainer();
    Context viewContext = inputViewContainer.getContext();
    var lister =
        (ScrollViewAsMainChild)
            LayoutInflater.from(viewContext)
                .inflate(R.layout.inline_suggestions_list, inputViewContainer, false);
    final var actualInputView = (AnyKeyboardView) getInputView();
    actualInputView.resetInputView();
    var params = lister.getLayoutParams();
    params.height = inputViewContainer.getHeight();
    params.width = inputViewContainer.getWidth();
    lister.setLayoutParams(params);
    lister.setBackground(actualInputView.getBackground());
    inputViewContainer.addView(lister);

    // inflating all inline-suggestion view and pushing into the linear-layout
    // I could not find a way to use RecyclerView for this
    var size =
        new Size(
            actualInputView.getWidth(),
            getResources().getDimensionPixelOffset(R.dimen.inline_suggestion_min_height));

    // breaking suggestions to priority
    var pinned = new ArrayList<InlineSuggestion>();
    var notPinned = new ArrayList<InlineSuggestion>();
    for (InlineSuggestion inlineSuggestion : inlineSuggestions) {
      if (inlineSuggestion.getInfo().isPinned()) pinned.add(inlineSuggestion);
      else notPinned.add(inlineSuggestion);
    }
    for (InlineSuggestion inlineSuggestion : pinned) {
      addInlineSuggestionToList(viewContext, lister, size, inlineSuggestion);
    }
    for (InlineSuggestion inlineSuggestion : notPinned) {
      addInlineSuggestionToList(viewContext, lister, size, inlineSuggestion);
    }

    actualInputView.setVisibility(View.GONE);
    return null;
  }

  @RequiresApi(Build.VERSION_CODES.R)
  private void addInlineSuggestionToList(
      @NonNull Context viewContext,
      @NonNull ScrollViewAsMainChild lister,
      @NonNull Size size,
      @NonNull InlineSuggestion inlineSuggestion) {
    var info = inlineSuggestion.getInfo();
    Logger.i(
        "ASK_Suggestion",
        "Suggestion source '%s', is pinned %s, type '%s', hints '%s'",
        info.getSource(),
        info.isPinned(),
        info.getType(),
        String.join(",", info.getAutofillHints()));
    inlineSuggestion.inflate(
        viewContext,
        size,
        getMainExecutor(),
        v -> {
          v.setOnClickListener(v1 -> cleanUpInlineLayouts(true));
          lister.addListItem(v);
        });
  }

  static class InlineSuggestionsAction implements KeyboardViewContainerView.StripActionProvider {
    private final Function<List<InlineSuggestion>, Void> mShowSuggestionsFunction;
    private final Runnable mRemoveStripAction;
    private final List<InlineSuggestion> mCurrentSuggestions;
    @Nullable private TextView mSuggestionsCount;

    InlineSuggestionsAction(
        Function<List<InlineSuggestion>, Void> showSuggestionsFunction,
        Runnable removeStripAction) {
      mShowSuggestionsFunction = showSuggestionsFunction;
      mRemoveStripAction = removeStripAction;
      mCurrentSuggestions = new ArrayList<>();
    }

    @Override
    public @NonNull View inflateActionView(@NonNull ViewGroup parent) {
      View root =
          LayoutInflater.from(parent.getContext())
              .inflate(R.layout.inline_suggestions_available_action, parent, false);

      root.setOnClickListener(
          view -> {
            Logger.d(TAG, "auto-fill action icon clicked");
            mShowSuggestionsFunction.apply(mCurrentSuggestions);
            mRemoveStripAction.run();
          });

      mSuggestionsCount = root.findViewById(R.id.inline_suggestions_strip_text);
      mSuggestionsCount.setText(
          String.format(Locale.getDefault(), "%d", mCurrentSuggestions.size()));
      return root;
    }

    @Override
    public void onRemoved() {
      mCurrentSuggestions.clear();
      mSuggestionsCount = null;
    }

    void onNewSuggestions(List<InlineSuggestion> suggestions) {
      mCurrentSuggestions.clear();
      mCurrentSuggestions.addAll(suggestions);
      if (mSuggestionsCount != null) {
        mSuggestionsCount.setText(
            String.format(Locale.getDefault(), "%d", mCurrentSuggestions.size()));
      }
    }
  }
}
