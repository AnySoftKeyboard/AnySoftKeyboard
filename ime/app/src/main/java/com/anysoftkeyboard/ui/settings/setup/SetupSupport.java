package com.anysoftkeyboard.ui.settings.setup;

import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SetupSupport {

  public static boolean isThisKeyboardSetAsDefaultIME(Context context) {
    var inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      InputMethodInfo currentInputMethodInfo = inputManager.getCurrentInputMethodInfo();
      return currentInputMethodInfo != null
          && Objects.equals(currentInputMethodInfo.getPackageName(), context.getPackageName());
    } else {
      final String defaultIME =
          Settings.Secure.getString(
              context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
      return isThisKeyboardSetAsDefaultIMEPrior34(defaultIME, context.getPackageName());
    }
  }

  private static boolean isThisKeyboardSetAsDefaultIMEPrior34(
      String defaultIME, String myPackageName) {
    if (TextUtils.isEmpty(defaultIME)) return false;

    ComponentName defaultInputMethod = ComponentName.unflattenFromString(defaultIME);
    return defaultInputMethod != null && defaultInputMethod.getPackageName().equals(myPackageName);
  }

  public static boolean isThisKeyboardEnabled(@NonNull Context context) {
    var inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    for (var method : inputManager.getEnabledInputMethodList()) {
      if (method.getPackageName().equals(context.getPackageName())) {
        return true;
      }
    }
    return false;
  }

  /*package*/
  static boolean hasLanguagePackForCurrentLocale(
      @NonNull List<KeyboardAddOnAndBuilder> availableLanguagePacks) {
    for (KeyboardAddOnAndBuilder availableLanguagePack : availableLanguagePacks) {
      final String language = availableLanguagePack.getKeyboardLocale();
      if (TextUtils.isEmpty(language)) continue;

      if (Locale.getDefault().getLanguage().equals(new Locale(language).getLanguage())) {
        return true;
      }
    }

    return false;
  }

  public static void popupViewAnimationWithIds(View rootView, @IdRes int... viewIds) {
    View[] views = new View[viewIds.length];
    for (int viewIndex = 0; viewIndex < viewIds.length; viewIndex++) {
      int viewId = viewIds[viewIndex];
      if (viewId != 0) {
        views[viewIndex] = rootView.findViewById(viewId);
      }
    }

    popupViewAnimation(views);
  }

  public static void popupViewAnimation(View... views) {
    int offset = 500;
    final int offsetInterval = 200;
    for (View view : views) {
      if (view != null) {
        Animation animation = AnimationUtils.loadAnimation(view.getContext(), R.anim.link_popup);
        animation.setStartOffset(offset);
        view.startAnimation(animation);
      }
      offset += offsetInterval;
    }
  }
}
