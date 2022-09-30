package com.anysoftkeyboard.ime;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InlineSuggestion;
import android.view.inputmethod.InlineSuggestionsRequest;
import android.view.inputmethod.InlineSuggestionsResponse;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.inline.InlinePresentationSpec;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.arch.core.util.Function;
import androidx.autofill.inline.UiVersions;
import androidx.autofill.inline.v1.InlineSuggestionUi;
import com.anysoftkeyboard.base.utils.Logger;
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

        KeyboardViewContainerView inputViewContainer = getInputViewContainer();
        inputViewContainer.getInlineAutofillView().removeAllViews();
        inputViewContainer.getInlineScrollView().setVisibility(View.GONE);
        removeActionStrip();
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Nullable
    @Override
    public InlineSuggestionsRequest onCreateInlineSuggestionsRequest(@NonNull Bundle uiExtras) {
        // min size is a thumb
        final Size smallestSize =
                new Size(
                        getResources().getDimensionPixelOffset(R.dimen.inline_suggestion_min_width),
                        getResources()
                                .getDimensionPixelOffset(R.dimen.inline_suggestion_min_height));
        // max size is the keyboard
        final Size biggestSize =
                new Size(
                        getInputViewContainer().getWidth(),
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

    @RequiresApi(Build.VERSION_CODES.R)
    private Void showSuggestions(List<InlineSuggestion> inlineSuggestions) {
        KeyboardViewContainerView inputViewContainer = getInputViewContainer();

        if (inputViewContainer != null) {
            final LinearLayout inlineAutofillLayout = inputViewContainer.getInlineAutofillView();
            Size autofillSize =
                    new Size(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            getResources()
                                    .getDimensionPixelOffset(R.dimen.inline_suggestion_min_height));

            inlineAutofillLayout.removeAllViews();

            for (InlineSuggestion inlineSuggestion : inlineSuggestions) {
                inputViewContainer.getInlineScrollView().setVisibility(View.VISIBLE);
                try {
                    inlineSuggestion.inflate(
                            this, autofillSize, getMainExecutor(), inlineAutofillLayout::addView);
                } catch (Exception e) {
                    Log.e(TAG, "onInlineSuggestionsResponse - inlineSuggestion.infLate - ", e);
                }
            }
        }

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
