package com.anysoftkeyboard.ui.settings.setup;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import java.util.Locale;

public class SetupSupport {

  public static boolean isThisKeyboardSetAsDefaultIME(Context context) {
    final String defaultIME =
        Settings.Secure.getString(
            context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
    return isThisKeyboardSetAsDefaultIME(defaultIME, context.getPackageName());
  }

  @VisibleForTesting
  /*package*/ static boolean isThisKeyboardSetAsDefaultIME(
      String defaultIME, String myPackageName) {
    if (TextUtils.isEmpty(defaultIME)) return false;

    ComponentName defaultInputMethod = ComponentName.unflattenFromString(defaultIME);
    if (defaultInputMethod.getPackageName().equals(myPackageName)) {
      return true;
    } else {
      return false;
    }
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

  @VisibleForTesting
  /*package*/ static boolean isThisKeyboardEnabled(String enabledIMEList, String myPackageName) {
    if (TextUtils.isEmpty(enabledIMEList)) return false;

    String[] enabledIMEs = enabledIMEList.split(":", -1);
    for (String enabledIMEId : enabledIMEs) {
      ComponentName enabledIME = ComponentName.unflattenFromString(enabledIMEId);
      if (enabledIME != null && enabledIME.getPackageName().equals(myPackageName)) {
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
