package com.anysoftkeyboard.ime;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.inputmethod.InlineSuggestion;
import android.view.inputmethod.InlineSuggestionsRequest;
import android.view.inputmethod.InlineSuggestionsResponse;
import android.widget.LinearLayout;
import android.widget.inline.InlinePresentationSpec;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.autofill.inline.UiVersions;
import androidx.autofill.inline.v1.InlineSuggestionUi;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import java.util.ArrayList;
import java.util.List;

public abstract class AnySoftKeyboardInlineSuggestions extends AnySoftKeyboardSuggestions {

    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);

        KeyboardViewContainerView inputViewContainer = getInputViewContainer();

        if (inputViewContainer != null) {
            inputViewContainer.removeAllViews();
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Nullable
    @Override
    public InlineSuggestionsRequest onCreateInlineSuggestionsRequest(@NonNull Bundle uiExtras) {
        Size smallestSize = new Size(0, 0);
        Size biggestSize = new Size(Integer.MAX_VALUE, Integer.MAX_VALUE);

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
        List<InlineSuggestion> inlineSuggestions = response.getInlineSuggestions();

        KeyboardViewContainerView inputViewContainer = getInputViewContainer();

        if (inputViewContainer != null) {
            LinearLayout inlineAutofillLayout = inputViewContainer.getInlineAutofillView();
            float height =
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
            Size autofillSize = new Size(ViewGroup.LayoutParams.WRAP_CONTENT, ((int) height));

            inlineAutofillLayout.removeAllViews();

            for (InlineSuggestion inlineSuggestion : inlineSuggestions) {
                try {
                    inlineSuggestion.inflate(
                            this, autofillSize, getMainExecutor(), inlineAutofillLayout::addView);
                } catch (Exception e) {
                    Log.e(
                            TAG,
                            "onInlineSuggestionsResponse - inlineSuggestion.infLate - "
                                    + e.toString());
                }
            }
        }
        return true;
    }
}
