package com.menny.android.anysoftkeyboard;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.os.IBinder;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

@Implements(value = InputMethodManager.class)
public class InputMethodManagerShadow extends org.robolectric.shadows.ShadowInputMethodManager {

  private boolean mStatusIconShown;
  private String mLastStatusIconPackageName;
  private int mLastStatusIconId;
  private IBinder mLastStatusIconImeToken;

  public InputMethodManagerShadow() {
    // adding three IMEs, ASK, Google, and AOSP (disabled)
    final List<InputMethodInfo> inputMethodInfos = new ArrayList<>();
    final List<InputMethodInfo> enabledInputMethods = new ArrayList<>();

    final InputMethodInfo askIme =
        new InputMethodInfo(
            "com.menny.android.anysoftkeyboard",
            "com.anysoftkeyboard.TestableAnySoftKeyboard",
            "AnySoftKeyboard",
            ".MainSettingsActivity");
    final InputMethodInfo gBoardIme =
        new InputMethodInfo(
            "com.google.keyboard", "GoogleKeyboard", "GoogleKeyboard", ".MainSettingsActivity");
    final InputMethodInfo aospIme =
        new InputMethodInfo(
            "android.ime.KeyboardService",
            "SoftKeyboard",
            "AOSP Keyboard",
            ".MainSettingsActivity");

    inputMethodInfos.add(askIme);
    enabledInputMethods.add(askIme);
    inputMethodInfos.add(gBoardIme);
    enabledInputMethods.add(gBoardIme);
    inputMethodInfos.add(aospIme);

    setInputMethodInfoList(ImmutableList.copyOf(inputMethodInfos));
    setEnabledInputMethodInfoList(ImmutableList.copyOf(enabledInputMethods));
  }

  public static void setKeyboardEnabled(Context context, boolean enabled) {
    InputMethodManager imeService =
        (InputMethodManager) context.getSystemService(Service.INPUT_METHOD_SERVICE);
    var inputMethodManagerShadow = (InputMethodManagerShadow) Shadow.extract(imeService);
    List<InputMethodInfo> allInputs = imeService.getInputMethodList();
    inputMethodManagerShadow.setEnabledInputMethodInfoList(
        allInputs.stream()
            .filter(
                ime -> enabled || !Objects.equals(ime.getPackageName(), context.getPackageName()))
            .toList());
  }

  public static void setKeyboardAsCurrent(Context context, boolean isCurrent) {
    // TODO support API 34
    var currentFlat =
        isCurrent
            ? new ComponentName(context, ".SoftKeyboard").flattenToString()
            : new ComponentName("com.example", ".OtherSoftKeyboard").flattenToString();
    Settings.Secure.putString(
        context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD, currentFlat);
  }

  @Implementation
  public void showStatusIcon(IBinder imeToken, String packageName, int iconId) {
    mLastStatusIconImeToken = imeToken;
    mLastStatusIconPackageName = packageName;
    mLastStatusIconId = iconId;
    mStatusIconShown = true;
  }

  public void clearStatusIconDetails() {
    mLastStatusIconImeToken = null;
    mLastStatusIconPackageName = null;
    mLastStatusIconId = 0;
  }

  @Implementation
  public void hideStatusIcon(IBinder imeToken) {
    mLastStatusIconImeToken = imeToken;
    mStatusIconShown = false;
  }

  public boolean isStatusIconShown() {
    return mStatusIconShown;
  }

  public String getLastStatusIconPackageName() {
    return mLastStatusIconPackageName;
  }

  public int getLastStatusIconId() {
    return mLastStatusIconId;
  }

  public IBinder getLastStatusIconImeToken() {
    return mLastStatusIconImeToken;
  }
}
