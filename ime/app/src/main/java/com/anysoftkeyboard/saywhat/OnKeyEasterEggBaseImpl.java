package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewWithExtraDraw;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.keyboards.views.extradraw.TypingExtraDraw;
import com.menny.android.anysoftkeyboard.R;

class OnKeyEasterEggBaseImpl implements OnKey, OnVisible {
    interface StringSupplier {
        String giveMeSomeString();
    }

    private final char[] mWord;
    private final StringSupplier mExtraDrawText;
    private final EasterEggAction mSuggestionAction;
    private int mWaitingForIndex = 0;

    protected OnKeyEasterEggBaseImpl(
            String word, String suggestion, String extraDrawText, @DrawableRes int image) {
        this(word, suggestion, () -> extraDrawText, image);
    }

    protected OnKeyEasterEggBaseImpl(
            String word, String suggestion, StringSupplier extraDrawText, @DrawableRes int image) {
        mWord = word.toCharArray();
        mSuggestionAction = new EasterEggAction(suggestion, image);
        mExtraDrawText = extraDrawText;
    }

    @Override
    public void onKey(PublicNotices ime, int primaryCode, Keyboard.Key key) {
        if (key != null && key.getPrimaryCode() == mWord[mWaitingForIndex]) {
            mWaitingForIndex++;
            if (mWaitingForIndex == mWord.length) {
                mWaitingForIndex = 0;
                final InputViewBinder inputView = ime.getInputView();
                if (inputView instanceof AnyKeyboardViewWithExtraDraw) {
                    final AnyKeyboardViewWithExtraDraw anyKeyboardViewWithExtraDraw =
                            (AnyKeyboardViewWithExtraDraw) inputView;
                    anyKeyboardViewWithExtraDraw.addExtraDraw(
                            new TypingExtraDraw(
                                    mExtraDrawText.giveMeSomeString(),
                                    new Point(
                                            anyKeyboardViewWithExtraDraw.getWidth() / 2,
                                            anyKeyboardViewWithExtraDraw.getHeight() / 2),
                                    120,
                                    this::adjustPaint));
                    ime.getInputViewContainer().addStripAction(mSuggestionAction);
                }
            }
        } else {
            mWaitingForIndex = 0;
        }
    }

    @Override
    public void onVisible(PublicNotices ime, AnyKeyboard keyboard, EditorInfo editorInfo) {}

    @Override
    public void onHidden(PublicNotices ime, AnyKeyboard keyboard) {
        ime.getInputViewContainer().removeStripAction(mSuggestionAction);
    }

    private Paint adjustPaint(Paint paint, AnyKeyboardViewWithExtraDraw ime, Float fraction) {
        Paint newPaint = new Paint(paint);
        ime.setPaintToKeyText(newPaint);
        newPaint.setTextSkewX(0.3f);
        newPaint.setAlpha((int) (255 * (1f - fraction)));
        newPaint.setTextScaleX(1 + fraction);
        newPaint.setShadowLayer(5, 0, 0, Color.BLACK);

        return newPaint;
    }

    @Override
    public String getName() {
        return "EasterEgg" + new String(mWord);
    }

    private static class EasterEggAction implements KeyboardViewContainerView.StripActionProvider {

        private final Intent mWebPage;
        @DrawableRes private final int mImage;

        private EasterEggAction(String url, @DrawableRes int image) {
            mWebPage = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mImage = image;
            mWebPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        @Override
        public View inflateActionView(ViewGroup parent) {
            final Context context = parent.getContext();
            View root =
                    LayoutInflater.from(context).inflate(R.layout.easter_egg_action, parent, false);

            root.setOnClickListener(view -> view.getContext().startActivity(mWebPage));
            ImageView image = root.findViewById(R.id.easter_egg_icon);
            image.setImageResource(mImage);
            return root;
        }

        @Override
        public void onRemoved() {}
    }
}
