/*
 * Copyright (c) 2016 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ime;

import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewWithExtraDraw;
import com.anysoftkeyboard.keyboards.views.extradraw.PopTextExtraDraw;
import com.anysoftkeyboard.rx.GenericOnError;
import com.menny.android.anysoftkeyboard.R;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public abstract class AnySoftKeyboardPopText extends AnySoftKeyboardPowerSaving {

    private boolean mPopTextOnCorrection = true;
    private boolean mPopTextOnWord = false;
    private boolean mPopTextOnKeyPress = false;

    @Nullable private PopTextExtraDraw.PopOut mLastTextPop;
    private Keyboard.Key mLastKey;

    @Override
    public void onCreate() {
        super.onCreate();

        addDisposable(
                prefs().getString(
                                R.string.settings_key_pop_text_option,
                                R.string.settings_default_pop_text_option)
                        .asObservable()
                        .subscribe(
                                this::updatePopTextPrefs,
                                GenericOnError.onError("settings_key_pop_text_option")));
    }

    @SuppressFBWarnings("SF_SWITCH_FALLTHROUGH")
    @SuppressWarnings("fallthrough")
    private void updatePopTextPrefs(String newValue) {
        mPopTextOnCorrection = false;
        mPopTextOnWord = false;
        mPopTextOnKeyPress = false;
        // letting the switch cases to fall-through - each value level enables additional flag
        switch (newValue) {
            case "any_key":
                mPopTextOnKeyPress = true;
                // letting the switch cases to fall-through - each value level enables additional
                // flag
                // fall through
            case "on_word":
                mPopTextOnWord = true;
                // letting the switch cases to fall-through - each value level enables additional
                // flag
                // fall through
            case "on_correction":
                mPopTextOnCorrection = true;
                break;
            default:
                // keeping everything off.
                break;
        }
    }

    @Override
    public void pickSuggestionManually(
            int index, CharSequence suggestion, boolean withAutoSpaceEnabled) {
        // we do not want to pop text when user picks from the suggestions bar
        mLastKey = null;
        super.pickSuggestionManually(index, suggestion, withAutoSpaceEnabled);
    }

    private void popText(CharSequence textToPop) {
        if (mLastKey == null) {
            return; // could be because of manually picked word
        }

        if (getInputView() instanceof AnyKeyboardViewWithExtraDraw) {
            final AnyKeyboardViewWithExtraDraw anyKeyboardViewWithExtraDraw =
                    (AnyKeyboardViewWithExtraDraw) getInputView();
            mLastTextPop =
                    new PopTextExtraDraw.PopOut(
                            textToPop,
                            new Point(mLastKey.x + mLastKey.width / 2, mLastKey.y),
                            mLastKey.y - anyKeyboardViewWithExtraDraw.getHeight() / 2);
            anyKeyboardViewWithExtraDraw.addExtraDraw(mLastTextPop);
        }
    }

    @Override
    public void onKey(
            int primaryCode,
            Keyboard.Key key,
            int multiTapIndex,
            int[] nearByKeyCodes,
            boolean fromUI) {
        super.onKey(primaryCode, key, multiTapIndex, nearByKeyCodes, fromUI);
        mLastKey = key;
        if (mPopTextOnKeyPress && isAlphabet(primaryCode)) {
            popText(Character.toString((char) primaryCode));
        }
    }

    @Override
    protected void commitWordToInput(@NonNull CharSequence wordToCommit, boolean correcting) {
        super.commitWordToInput(wordToCommit, correcting);
        final boolean toPopText = (mPopTextOnCorrection && correcting) || mPopTextOnWord;
        if (toPopText) {
            popText(wordToCommit.toString());
        }
    }

    @Override
    public void revertLastWord() {
        super.revertLastWord();

        revertLastPopText();
    }

    private void revertLastPopText() {
        final PopTextExtraDraw.PopOut lastTextPop = mLastTextPop;
        if (lastTextPop != null && !lastTextPop.isDone()) {
            final InputViewBinder inputView = getInputView();
            if (inputView instanceof AnyKeyboardViewWithExtraDraw) {
                ((AnyKeyboardViewWithExtraDraw) inputView)
                        .addExtraDraw(lastTextPop.generateRevert());
            }

            mLastTextPop = null;
        }
    }
}
