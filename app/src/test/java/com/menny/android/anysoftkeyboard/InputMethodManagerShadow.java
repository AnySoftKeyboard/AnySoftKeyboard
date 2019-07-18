package com.menny.android.anysoftkeyboard;

import android.os.IBinder;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = InputMethodManager.class)
public class InputMethodManagerShadow extends org.robolectric.shadows.ShadowInputMethodManager {

    private final List<InputMethodInfo> mInputMethodInfos = new ArrayList<>();
    private final Set<String> mEnabledInputMethods = new HashSet<>();

    private boolean mStatusIconShown;
    private String mLastStatusIconPackageName;
    private int mLastStatusIconId;
    private IBinder mLastStatusIconImeToken;

    public InputMethodManagerShadow() {
        // adding three IMEs, ASK, Google, and AOSP (disabled)
        mInputMethodInfos.add(
                new InputMethodInfo(
                        "com.menny.android.anysoftkeyboard",
                        "SoftKeyboard",
                        "AnySoftKeyboard",
                        ".MainSettingsActivity"));
        mEnabledInputMethods.add("com.menny.android.anysoftkeyboard");
        mInputMethodInfos.add(
                new InputMethodInfo(
                        "com.google.keyboard",
                        "GoogleKeyboard",
                        "GoogleKeyboard",
                        ".MainSettingsActivity"));
        mEnabledInputMethods.add("com.google.keyboard");
        mInputMethodInfos.add(
                new InputMethodInfo(
                        "android.ime.KeyboardService",
                        "SoftKeyboard",
                        "AOSP Keyboard",
                        ".MainSettingsActivity"));
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

    @Implementation
    public List<InputMethodInfo> getInputMethodList() {
        return Collections.unmodifiableList(mInputMethodInfos);
    }

    @Implementation
    public List<InputMethodInfo> getEnabledInputMethodList() {
        List<InputMethodInfo> enabledIme = new ArrayList<>();
        for (InputMethodInfo ime : getInputMethodList()) {
            if (mEnabledInputMethods.contains(ime.getPackageName())) enabledIme.add(ime);
        }
        return Collections.unmodifiableList(enabledIme);
    }

    public List<InputMethodInfo> getModifiableInputMethodList() {
        return mInputMethodInfos;
    }

    public void setImeEnabled(String imePackageName, boolean isEnabled) {
        if (isEnabled && !mEnabledInputMethods.contains(imePackageName))
            mEnabledInputMethods.add(imePackageName);
        else if (mEnabledInputMethods.contains(imePackageName) && !isEnabled)
            mEnabledInputMethods.remove(imePackageName);
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
