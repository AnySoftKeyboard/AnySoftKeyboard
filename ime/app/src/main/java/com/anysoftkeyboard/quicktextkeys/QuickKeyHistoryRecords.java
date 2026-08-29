package com.anysoftkeyboard.quicktextkeys;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.anysoftkeyboard.rx.GenericOnError;
import com.f2prateek.rx.preferences2.Preference;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuickKeyHistoryRecords implements Disposable {
  private static final String TAG = "QuickKeyHistoryRecords";
  static final int MAX_LIST_SIZE = 30;

  public static final String DEFAULT_EMOJI = "\uD83D\uDE03";
  private final List<HistoryKey> mLoadedKeys = new ArrayList<>(MAX_LIST_SIZE);
  @NonNull private final Preference<String> mRxPref;
  @NonNull private final Disposable mDisposable;
  private boolean mIncognitoMode;

  public QuickKeyHistoryRecords(@NonNull RxSharedPrefs rxSharedPrefs) {
    mRxPref =
        rxSharedPrefs.getString(
            R.string.settings_key_quick_text_history, R.string.settings_default_empty);
    mDisposable =
        mRxPref
            .asObservable()
            .subscribe(
                this::onHistoryPreferenceChanged,
                GenericOnError.onError("settings_key_quick_text_history"));
  }

  private void onHistoryPreferenceChanged(@NonNull String encodedHistory) {
    mLoadedKeys.clear();
    if (!TextUtils.isEmpty(encodedHistory)) {
      decodeFromJson(encodedHistory, mLoadedKeys);
    }
    if (mLoadedKeys.isEmpty()) {
      mLoadedKeys.add(new HistoryKey(DEFAULT_EMOJI, DEFAULT_EMOJI));
    }
  }

  private static void decodeFromJson(
      @NonNull String encodedHistory, @NonNull List<HistoryKey> outputSet) {
    try {
      JSONArray jsonArray = new JSONArray(encodedHistory);
      for (int i = 0; i < jsonArray.length() && outputSet.size() < MAX_LIST_SIZE; i++) {
        JSONObject jsonObject = jsonArray.getJSONObject(i);
        String name = jsonObject.optString("name", null);
        String value = jsonObject.optString("value", null);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(value)) {
          outputSet.add(new HistoryKey(name, value));
        }
      }
    } catch (JSONException e) {
      Logger.w(TAG, e, "Failed to decode history from JSON");
    }
  }

  public void store(@NonNull String name, @NonNull String value) {
    if (mIncognitoMode) return;

    final HistoryKey usedKey = new HistoryKey(name, value);
    mLoadedKeys.remove(usedKey);
    mLoadedKeys.add(usedKey);

    while (mLoadedKeys.size() > MAX_LIST_SIZE) {
      mLoadedKeys.remove(0 /*dropping the first key*/);
    }

    final String encodedHistory = encodeToJson(mLoadedKeys);

    mRxPref.set(encodedHistory);
  }

  private static String encodeToJson(@NonNull List<HistoryKey> outputSet) {
    JSONArray jsonArray = new JSONArray();
    for (HistoryKey historyKey : outputSet) {
      JSONObject jsonObject = new JSONObject();
      try {
        jsonObject.put("name", historyKey.name);
        jsonObject.put("value", historyKey.value);
        jsonArray.put(jsonObject);
      } catch (JSONException e) {
        Logger.w(TAG, e, "Failed to encode HistoryKey to JSON");
      }
    }
    return jsonArray.toString();
  }

  public void clearHistory() {
    mLoadedKeys.clear();
    mLoadedKeys.add(new HistoryKey(DEFAULT_EMOJI, DEFAULT_EMOJI));
    final String encodedHistory = encodeToJson(mLoadedKeys);
    mRxPref.set(encodedHistory);
  }

  public List<HistoryKey> getCurrentHistory() {
    if (mLoadedKeys.isEmpty()) {
      mLoadedKeys.add(new HistoryKey(DEFAULT_EMOJI, DEFAULT_EMOJI));
    }
    return Collections.unmodifiableList(mLoadedKeys);
  }

  @VisibleForTesting
  public boolean isIncognitoMode() {
    return mIncognitoMode;
  }

  public void setIncognitoMode(boolean incognitoMode) {
    mIncognitoMode = incognitoMode;
  }

  @Override
  public void dispose() {
    mDisposable.dispose();
  }

  @Override
  public boolean isDisposed() {
    return mDisposable.isDisposed();
  }

  public static class HistoryKey {
    public final String name;
    public final String value;

    public HistoryKey(String name, String value) {
      this.name = name;
      this.value = value;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof HistoryKey && ((HistoryKey) o).name.equals(name);
    }

    @Override
    public int hashCode() {
      return name.hashCode();
    }
  }
}
