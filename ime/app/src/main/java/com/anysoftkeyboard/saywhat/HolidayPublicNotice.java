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
import com.f2prateek.rx.preferences2.Preference;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;

public abstract class HolidayPublicNotice implements PublicNotice, OnKey, OnVisible {

  static final String PREF_KEY_PREFIX = "public_notice_dismiss_state_";
  static final String STATE_PRE_DISMISSED = "pre-dismissed";
  static final String STATE_COMPLETED = "completed";

  private final String mName;
  private final OnKeyWordHelper[] mWordHelpers;
  private final String mExtraDrawText;
  private final HolidayStripAction mStripAction;
  private boolean mIsActive;
  private boolean mShouldShowStrip;
  @Nullable private KeyboardViewContainerView mAddedToContainer;
  @Nullable private Preference<String> mDismissStatePref;
  private int mCachedYear = Integer.MIN_VALUE;
  private String mCachedPreDismissedValue;
  private String mCachedCompletedValue;
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

  /**
   * Returns true if {@code now} falls on the main holiday day itself (rather than a pre-holiday
   * lead-up day). Single-day notices default to {@link #isDateActive(Calendar, Locale)}.
   */
  protected boolean isMainDayActive(@NonNull Calendar now, @NonNull Locale locale) {
    return isDateActive(now, locale);
  }

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

  @Nullable
  private Preference<String> getDismissStatePref(@NonNull Context context) {
    if (mDismissStatePref == null) {
      final Context appContext = context.getApplicationContext();
      if (appContext instanceof AnyApplication) {
        mDismissStatePref =
            AnyApplication.prefs(appContext)
                .getString(PREF_KEY_PREFIX + mName, R.string.settings_default_empty);
      }
    }
    return mDismissStatePref;
  }

  private void ensureYearValues(int year) {
    if (mCachedYear != year) {
      mCachedYear = year;
      mCachedPreDismissedValue = year + ":" + STATE_PRE_DISMISSED;
      mCachedCompletedValue = year + ":" + STATE_COMPLETED;
    }
  }

  protected boolean isDismissed(
      @NonNull Context context, @NonNull Calendar now, @NonNull Locale locale) {
    final Preference<String> pref = getDismissStatePref(context);
    if (pref == null) {
      return false;
    }
    ensureYearValues(now.get(Calendar.YEAR));
    final String state = pref.get();
    if (mCachedCompletedValue.equals(state)) {
      return true;
    }
    return !isMainDayActive(now, locale) && mCachedPreDismissedValue.equals(state);
  }

  private void markDismissed(
      @NonNull Context context, @NonNull Calendar now, @NonNull Locale locale) {
    final Preference<String> pref = getDismissStatePref(context);
    if (pref == null) {
      return;
    }
    ensureYearValues(now.get(Calendar.YEAR));
    pref.set(isMainDayActive(now, locale) ? mCachedCompletedValue : mCachedPreDismissedValue);
  }

  private void removeStripAction(@Nullable KeyboardViewContainerView container) {
    if (mStripAction == null) {
      return;
    }
    if (container != null) {
      container.removeStripAction(mStripAction);
    }
    if (mAddedToContainer != null && mAddedToContainer != container) {
      mAddedToContainer.removeStripAction(mStripAction);
    }
    mAddedToContainer = null;
  }

  private void onStripActionClicked(
      @NonNull Context context, @Nullable KeyboardViewContainerView container) {
    markDismissed(context, getCurrentCalendar(), getCurrentLocale(context));
    mShouldShowStrip = false;
    removeStripAction(container != null ? container : mAddedToContainer);
  }

  private final Paint mDrawPaint = new Paint();

  @Override
  public void onVisible(PublicNotices ime, AnyKeyboard keyboard, EditorInfo editorInfo) {
    final Calendar now = getCurrentCalendar();
    final Locale locale = getCurrentLocale(ime);
    mIsActive = isDateActive(now, locale);
    mShouldShowStrip = mIsActive && !isDismissed(ime, now, locale);
    final KeyboardViewContainerView container = ime.getInputViewContainer();
    if (mShouldShowStrip && mStripAction != null) {
      if (container != null && mAddedToContainer != container) {
        container.addStripAction(mStripAction, true);
        mAddedToContainer = container;
      }
    } else if (mAddedToContainer != null) {
      removeStripAction(container);
    }
  }

  @Override
  public void onHidden(PublicNotices ime, AnyKeyboard keyboard) {
    if (mAddedToContainer == null) {
      return;
    }
    removeStripAction(ime.getInputViewContainer());
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
      if (inputView instanceof AnyKeyboardViewWithExtraDraw anyKeyboardView) {
        anyKeyboardView.addExtraDraw(
            new TypingExtraDraw(
                mExtraDrawText,
                new Point(anyKeyboardView.getWidth() / 2, anyKeyboardView.getHeight() / 2),
                120,
                this::adjustPaint));
        if (mShouldShowStrip && mStripAction != null) {
          final KeyboardViewContainerView container = ime.getInputViewContainer();
          if (container != null && mAddedToContainer != container) {
            container.addStripAction(mStripAction, true);
            mAddedToContainer = container;
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

  private class HolidayStripAction implements KeyboardViewContainerView.StripActionProvider {
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
      final KeyboardViewContainerView container =
          parent instanceof KeyboardViewContainerView ? (KeyboardViewContainerView) parent : null;
      root.setOnClickListener(
          v -> {
            if (mToastGreeting != null) {
              Toast.makeText(v.getContext(), mToastGreeting, Toast.LENGTH_SHORT).show();
            }
            onStripActionClicked(v.getContext(), container);
          });
      return root;
    }

    @Override
    public void onRemoved() {}
  }
}
