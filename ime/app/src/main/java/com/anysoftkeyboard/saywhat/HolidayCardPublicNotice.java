package com.anysoftkeyboard.saywhat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.menny.android.anysoftkeyboard.R;
import java.util.Calendar;
import java.util.Locale;

public abstract class HolidayCardPublicNotice extends HolidayPublicNotice implements OnUiPage {

  @StringRes private final int mCardTitleResId;
  @StringRes private final int mCardMessageResId;

  protected HolidayCardPublicNotice(
      @NonNull String name,
      @Nullable String[] triggerWords,
      @Nullable String extraDrawText,
      @Nullable String stripEmoji,
      @Nullable String toastGreeting,
      @StringRes int cardTitleResId,
      @StringRes int cardMessageResId) {
    super(name, triggerWords, extraDrawText, stripEmoji, toastGreeting);
    mCardTitleResId = cardTitleResId;
    mCardMessageResId = cardMessageResId;
  }

  protected boolean isUiActive(@NonNull Calendar now, @NonNull Locale locale) {
    return isDateActive(now, locale);
  }

  @SuppressLint("InflateParams")
  @Nullable
  @Override
  public View inflateContentView(@NonNull Context context) {
    if (!isUiActive(getCurrentCalendar(), getCurrentLocale(context))) {
      return null;
    }
    final View view =
        LayoutInflater.from(context).inflate(R.layout.holiday_public_notice_card, null);
    final TextView titleView = view.findViewById(R.id.public_notice_card_title);
    final TextView messageView = view.findViewById(R.id.public_notice_card_message);
    if (titleView != null) {
      titleView.setText(mCardTitleResId);
    }
    if (messageView != null) {
      messageView.setText(mCardMessageResId);
    }
    return view;
  }
}
