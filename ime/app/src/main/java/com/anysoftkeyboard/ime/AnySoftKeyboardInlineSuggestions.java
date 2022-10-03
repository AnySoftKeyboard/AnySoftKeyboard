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
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

public abstract class AnySoftKeyboardInlineSuggestions extends AnySoftKeyboardSuggestions {

    private final InlineSuggestionsAction mInlineSuggestionAction;

    public AnySoftKeyboardInlineSuggestions() {
        super();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mInlineSuggestionAction =
                    new InlineSuggestionsAction(this::showSuggestions, this::removeActionStrip);
        } else {
            mInlineSuggestionAction =
                    new InlineSuggestionsAction(l -> null, this::removeActionStrip);
        }
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
        if (finishingInput) {
            cleanUpInlineLayouts(true);
            removeActionStrip();
        }
    }

    @Override
    protected boolean handleCloseRequest() {
        return super.handleCloseRequest() || cleanUpInlineLayouts(true);
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Nullable
    @Override
    public InlineSuggestionsRequest onCreateInlineSuggestionsRequest(@NonNull Bundle uiExtras) {
        final var inputViewContainer = getInputViewContainer();
        if (inputViewContainer == null) return null;
        // min size is a thumb
        final Size smallestSize =
                new Size(
                        getResources().getDimensionPixelOffset(R.dimen.inline_suggestion_min_width),
                        getResources()
                                .getDimensionPixelOffset(R.dimen.inline_suggestion_min_height));
        // max size is the keyboard
        final Size biggestSize =
                new Size(
                        inputViewContainer.getWidth(),
                        getResources()
                                .getDimensionPixelOffset(R.dimen.inline_suggestion_max_height));

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

        return builder.setMaxSuggestionCount(InlineSuggestionsRequest.SUGGESTION_COUNT_UNLIMITED)
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
            var list = inputViewContainer.findViewById(R.id.inline_suggestions_scroller);
            if (list != null) {
                var itemsContainer = (ViewGroup) list.findViewById(R.id.inline_suggestions_list);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    list.setOnScrollChangeListener(null);
                }
                itemsContainer.removeAllViews();
                inputViewContainer.removeView(list);
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
                (ScrollView)
                        LayoutInflater.from(viewContext)
                                .inflate(
                                        R.layout.inline_suggestions_list,
                                        inputViewContainer,
                                        false);
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
                        getResources()
                                .getDimensionPixelOffset(R.dimen.inline_suggestion_min_height));
        final LinearLayout itemsContainer = lister.findViewById(R.id.inline_suggestions_list);
        for (InlineSuggestion inlineSuggestion : inlineSuggestions) {
            inlineSuggestion.inflate(
                    viewContext,
                    size,
                    getMainExecutor(),
                    v -> {
                        v.setOnClickListener(v1 -> cleanUpInlineLayouts(true));
                        itemsContainer.addView(v);
                    });
        }
        // okay.. this is super weird:
        // Since the items in the list are remote-views, they are drawn on top of our UI.
        // this means that they think that itemsContainer is very large and so they
        // draw themselves outside the scroll window.
        // The only nice why I found to deal with this is to set them to INVISIBLE
        // when they scroll out of view.
        lister.setOnScrollChangeListener(
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    final int itemsOnTop = scrollY / size.getHeight();
                    for (int childIndex = 0; childIndex < itemsOnTop; childIndex++) {
                        itemsContainer.getChildAt(childIndex).setVisibility(View.INVISIBLE);
                    }
                    for (int childIndex = itemsOnTop;
                            childIndex < itemsContainer.getChildCount();
                            childIndex++) {
                        var child = itemsContainer.getChildAt(childIndex);
                        child.setVisibility(View.VISIBLE);
                        child.setScaleX(1f);
                        child.setScaleY(1f);
                    }
                    // how much do we need to scale-down the top item
                    float partOfTopItemShown = scrollY - (size.getHeight() * itemsOnTop);
                    final float scaleFactor = 1f - partOfTopItemShown / size.getHeight();
                    var topVisibleChild = itemsContainer.getChildAt(itemsOnTop);
                    topVisibleChild.setScaleX(scaleFactor);
                    topVisibleChild.setScaleY(scaleFactor);
                    topVisibleChild.setPivotY(size.getHeight());
                });

        actualInputView.setVisibility(View.GONE);
        return null;
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
        public View inflateActionView(ViewGroup parent) {
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
