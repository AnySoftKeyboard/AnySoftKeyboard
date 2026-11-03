package com.anysoftkeyboard.saywhat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardViewWithExtraDraw;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;
import com.anysoftkeyboard.keyboards.views.extradraw.TypingExtraDraw;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;

public abstract class HolidayPublicNotice implements PublicNotice, OnKey, OnVisible {

  private final String mName;
  private final OnKeyWordHelper[] mWordHelpers;
  private final String mExtraDrawText;
  private final KeyboardViewContainerView.StripActionProvider mStripAction;
  private boolean mIsActive;
  private Calendar mOverrideCalendar;
  private Locale mOverrideLocale;

  protected HolidayPublicNotice(
      @NonNull String name,
      @Nullable String[] triggerWords,
      @Nullable String extraDrawText,
      @Nullable String stripEmoji,
      @Nullable String toastGreeting) {
    mName = name;
    mExtraDrawText = extraDrawText;
    if (triggerWords != null && triggerWords.length > 0) {
      mWordHelpers = new OnKeyWordHelper[triggerWords.length];
      for (int i = 0; i < triggerWords.length; i++) {
        mWordHelpers[i] = new OnKeyWordHelper(triggerWords[i]);
      }
    } else {
      mWordHelpers = null;
    }
    if (stripEmoji != null) {
      mStripAction = new HolidayStripAction(stripEmoji, toastGreeting);
    } else {
      mStripAction = null;
    }
  }

  @NonNull
  @Override
  public String getName() {
    return mName;
  }

  protected abstract boolean isDateActive(@NonNull Calendar now, @NonNull Locale locale);

  @VisibleForTesting
  public void setTestCalendar(@Nullable Calendar calendar) {
    mOverrideCalendar = calendar;
  }

  @VisibleForTesting
  public void setTestLocale(@Nullable Locale locale) {
    mOverrideLocale = locale;
  }

  @NonNull
  protected Calendar getCurrentCalendar() {
    if (mOverrideCalendar != null) {
      return (Calendar) mOverrideCalendar.clone();
    }
    return Calendar.getInstance();
  }

  @NonNull
  protected Locale getCurrentLocale(@NonNull Context context) {
    if (mOverrideLocale != null) {
      return mOverrideLocale;
    }
    final var resources = context.getResources();
    if (resources != null) {
      final var configuration = resources.getConfiguration();
      if (configuration != null && configuration.locale != null) {
        return configuration.locale;
      }
    }
    return Locale.getDefault();
  }

  private final Paint mDrawPaint = new Paint();

  @Override
  public void onVisible(PublicNotices ime, AnyKeyboard keyboard, EditorInfo editorInfo) {
    mIsActive = isDateActive(getCurrentCalendar(), getCurrentLocale(ime));
    if (mIsActive && mStripAction != null) {
      final KeyboardViewContainerView container = ime.getInputViewContainer();
      if (container != null) {
        container.addStripAction(mStripAction, true);
      }
    }
  }

  @Override
  public void onHidden(PublicNotices ime, AnyKeyboard keyboard) {
    if (mStripAction != null) {
      final KeyboardViewContainerView container = ime.getInputViewContainer();
      if (container != null) {
        container.removeStripAction(mStripAction);
      }
    }
  }

  @Override
  public void onKey(PublicNotices ime, int primaryCode, Keyboard.Key key) {
    if (!mIsActive || mWordHelpers == null || mExtraDrawText == null) {
      return;
    }
    final int lowerCode = Character.toLowerCase(primaryCode);
    boolean matched = false;
    for (int i = 0; i < mWordHelpers.length; i++) {
      matched |= mWordHelpers[i].shouldShow(lowerCode);
    }
    if (matched) {
      final InputViewBinder inputView = ime.getInputView();
      if (inputView instanceof AnyKeyboardViewWithExtraDraw) {
        final AnyKeyboardViewWithExtraDraw anyKeyboardView =
            (AnyKeyboardViewWithExtraDraw) inputView;
        anyKeyboardView.addExtraDraw(
            new TypingExtraDraw(
                mExtraDrawText,
                new Point(anyKeyboardView.getWidth() / 2, anyKeyboardView.getHeight() / 2),
                120,
                this::adjustPaint));
        if (mStripAction != null) {
          final KeyboardViewContainerView container = ime.getInputViewContainer();
          if (container != null) {
            container.addStripAction(mStripAction, true);
          }
        }
      }
    }
  }

  private Paint adjustPaint(Paint paint, AnyKeyboardViewWithExtraDraw ime, Float fraction) {
    mDrawPaint.set(paint);
    ime.setPaintToKeyText(mDrawPaint);
    mDrawPaint.setTextSkewX(0.2f);
    mDrawPaint.setAlpha((int) (255 * (1f - fraction)));
    mDrawPaint.setTextScaleX(1 + fraction);
    mDrawPaint.setShadowLayer(5, 0, 0, Color.BLACK);
    return mDrawPaint;
  }

  private static class HolidayStripAction implements KeyboardViewContainerView.StripActionProvider {
    @NonNull private final String mEmoji;
    @Nullable private final String mToastGreeting;

    HolidayStripAction(@NonNull String emoji, @Nullable String toastGreeting) {
      mEmoji = emoji;
      mToastGreeting = toastGreeting;
    }

    @NonNull
    @Override
    public View inflateActionView(@NonNull ViewGroup parent) {
      final Context context = parent.getContext();
      final TextView root =
          (TextView)
              LayoutInflater.from(context).inflate(R.layout.holiday_strip_action, parent, false);
      root.setText(mEmoji);
      root.setContentDescription(mToastGreeting != null ? mToastGreeting : mEmoji);
      if (mToastGreeting != null) {
        root.setOnClickListener(
            v -> Toast.makeText(v.getContext(), mToastGreeting, Toast.LENGTH_SHORT).show());
      }
      return root;
    }

    @Override
    public void onRemoved() {}
  }
}
