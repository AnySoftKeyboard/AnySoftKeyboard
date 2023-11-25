/*
 * Copyright (c) 2013 Menny Even-Danan
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

package com.menny.android.anysoftkeyboard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.multidex.MultiDexApplication;
import com.anysoftkeyboard.AnySoftKeyboard;
import com.anysoftkeyboard.addons.AddOnsFactory;
import com.anysoftkeyboard.android.NightMode;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.base.utils.NullLogProvider;
import com.anysoftkeyboard.chewbacca.ChewbaccaUncaughtExceptionHandler;
import com.anysoftkeyboard.devicespecific.DeviceSpecific;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV15;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV16;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV19;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV24;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV26;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV28;
import com.anysoftkeyboard.devicespecific.DeviceSpecificV29;
import com.anysoftkeyboard.dictionaries.ExternalDictionaryFactory;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtension;
import com.anysoftkeyboard.keyboardextensions.KeyboardExtensionFactory;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.prefs.DirectBootAwareSharedPreferences;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.prefs.RxSharedPrefs;
import com.anysoftkeyboard.quicktextkeys.QuickTextKeyFactory;
import com.anysoftkeyboard.saywhat.EasterEggs;
import com.anysoftkeyboard.saywhat.Notices;
import com.anysoftkeyboard.saywhat.PublicNotice;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.anysoftkeyboard.ui.SendBugReportUiActivity;
import com.anysoftkeyboard.ui.dev.DeveloperUtils;
import com.anysoftkeyboard.ui.tutorials.TutorialsProvider;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnyApplication extends MultiDexApplication {

  static final String PREF_KEYS_FIRST_INSTALLED_APP_VERSION =
      "settings_key_first_app_version_installed";
  static final String PREF_KEYS_FIRST_INSTALLED_APP_TIME = "settings_key_first_time_app_installed";
  static final String PREF_KEYS_LAST_INSTALLED_APP_VERSION =
      "settings_key_last_app_version_installed";
  static final String PREF_KEYS_LAST_INSTALLED_APP_TIME =
      "settings_key_first_time_current_version_installed";
  private static final String TAG = "ASKApp";
  private static DeviceSpecific msDeviceSpecific;
  private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
  private final Subject<Boolean> mNightModeSubject = ReplaySubject.createWithSize(1);
  private KeyboardFactory mKeyboardFactory;
  private ExternalDictionaryFactory mExternalDictionaryFactory;
  private KeyboardExtensionFactory mBottomRowFactory;
  private KeyboardExtensionFactory mTopRowFactory;
  private KeyboardExtensionFactory mExtensionKeyboardFactory;
  private KeyboardThemeFactory mKeyboardThemeFactory;
  private QuickTextKeyFactory mQuickTextKeyFactory;
  private RxSharedPrefs mRxSharedPrefs;
  private ArrayList<PublicNotice> mPublicNotices;

  public static DeviceSpecific getDeviceSpecific() {
    return msDeviceSpecific;
  }

  public static KeyboardFactory getKeyboardFactory(Context context) {
    return ((AnyApplication) context.getApplicationContext()).mKeyboardFactory;
  }

  public static KeyboardExtensionFactory getTopRowFactory(Context context) {
    return ((AnyApplication) context.getApplicationContext()).mTopRowFactory;
  }

  public static KeyboardExtensionFactory getBottomRowFactory(Context context) {
    return ((AnyApplication) context.getApplicationContext()).mBottomRowFactory;
  }

  public static KeyboardExtensionFactory getKeyboardExtensionFactory(Context context) {
    return ((AnyApplication) context.getApplicationContext()).mExtensionKeyboardFactory;
  }

  public static ExternalDictionaryFactory getExternalDictionaryFactory(Context context) {
    return ((AnyApplication) context.getApplicationContext()).mExternalDictionaryFactory;
  }

  public static KeyboardThemeFactory getKeyboardThemeFactory(Context context) {
    return ((AnyApplication) context.getApplicationContext()).mKeyboardThemeFactory;
  }

  public static QuickTextKeyFactory getQuickTextKeyFactory(Context context) {
    return ((AnyApplication) context.getApplicationContext()).mQuickTextKeyFactory;
  }

  public static long getCurrentVersionInstallTime(Context appContext) {
    SharedPreferences sp = DirectBootAwareSharedPreferences.create(appContext);
    return sp.getLong(PREF_KEYS_LAST_INSTALLED_APP_TIME, 0);
  }

  public static int getFirstAppVersionInstalled(Context appContext) {
    SharedPreferences sp = DirectBootAwareSharedPreferences.create(appContext);
    return sp.getInt(PREF_KEYS_FIRST_INSTALLED_APP_VERSION, 0);
  }

  public static RxSharedPrefs prefs(Context context) {
    final Context applicationContext = context.getApplicationContext();
    if (applicationContext instanceof AnyApplication) {
      return ((AnyApplication) applicationContext).mRxSharedPrefs;
    } else {
      throw new IllegalStateException(
          "What? expected 'context.getApplicationContext()' to be AnyApplication, but was"
              + " '"
              + applicationContext.getClass()
              + "'!!");
    }
  }

  private static DeviceSpecific createDeviceSpecificImplementation(final int apiLevel) {
    if (apiLevel < 16) return new DeviceSpecificV15();
    if (apiLevel < 19) return new DeviceSpecificV16();
    if (apiLevel < 24) return new DeviceSpecificV19();
    if (apiLevel < 26) return new DeviceSpecificV24();
    if (apiLevel < 28) return new DeviceSpecificV26();
    if (apiLevel < 29) return new DeviceSpecificV28();
    return new DeviceSpecificV29();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    DirectBootAwareSharedPreferences.create(this, this::onSharedPreferencesReady);

    Logger.d(TAG, "** Starting application in DEBUG mode.");
    Logger.i(TAG, "** Version: " + BuildConfig.VERSION_NAME);
    Logger.i(TAG, "** Release code: " + BuildConfig.VERSION_CODE);
    Logger.i(TAG, "** BUILD_TYPE: " + BuildConfig.BUILD_TYPE);
    Logger.i(TAG, "** DEBUG: " + BuildConfig.DEBUG);
    Logger.i(TAG, "** TESTING_BUILD: " + BuildConfig.TESTING_BUILD);
    msDeviceSpecific = createDeviceSpecificImplementation(Build.VERSION.SDK_INT);
    Logger.i(
        TAG,
        "Loaded DeviceSpecific "
            + msDeviceSpecific.getApiLevel()
            + " concrete class "
            + msDeviceSpecific.getClass().getName());

    mRxSharedPrefs = new RxSharedPrefs(this, this::prefsAutoRestoreFunction);

    mKeyboardFactory = createKeyboardFactory();
    mExternalDictionaryFactory = createExternalDictionaryFactory();
    mBottomRowFactory = createBottomKeyboardExtensionFactory();
    mTopRowFactory = createTopKeyboardExtensionFactory();
    mExtensionKeyboardFactory = createToolsKeyboardExtensionFactory();
    mKeyboardThemeFactory = createKeyboardThemeFactory();
    mQuickTextKeyFactory = createQuickTextKeyFactory();

    mCompositeDisposable.add(
        mRxSharedPrefs
            .getBoolean(
                R.string.settings_key_show_settings_app, R.bool.settings_default_show_settings_app)
            .asObservable()
            .subscribe(
                showApp -> {
                  PackageManager pm = getPackageManager();
                  pm.setComponentEnabledSetting(
                      new ComponentName(getApplicationContext(), LauncherSettingsActivity.class),
                      showApp
                          ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                          : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                      PackageManager.DONT_KILL_APP);
                }));
    mCompositeDisposable.add(
        NightMode.observeNightModeState(
                this,
                R.string.settings_key_night_mode_app_theme_control,
                R.bool.settings_default_true)
            .subscribe(
                nightMode ->
                    AppCompatDelegate.setDefaultNightMode(
                        nightMode
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO)));
    mNightModeSubject.onNext(
        (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            == Configuration.UI_MODE_NIGHT_YES);

    mPublicNotices = new ArrayList<>(EasterEggs.create());
    mPublicNotices.addAll(Notices.create(this));
  }

  private void onSharedPreferencesReady(@NonNull SharedPreferences sp) {
    setupCrashHandler(sp);
    updateStatistics(sp);
    TutorialsProvider.showDragonsIfNeeded(getApplicationContext());
  }

  private void prefsAutoRestoreFunction(@NonNull File file) {
    Logger.d(TAG, "Starting prefsAutoRestoreFunction for '%s'", file);
    // NOTE: shared_prefs_provider_name is the only supported prefs. All others require
    // dictionaries to load prior.
    final Pair<List<GlobalPrefsBackup.ProviderDetails>, Boolean[]> providers =
        Observable.fromIterable(GlobalPrefsBackup.getAllAutoApplyPrefsProviders(this))
            .map(p -> Pair.create(p, true))
            .collectInto(
                Pair.create(
                    new ArrayList<GlobalPrefsBackup.ProviderDetails>(), new ArrayList<Boolean>()),
                (collectInto, aPair) -> {
                  collectInto.first.add(aPair.first);
                  collectInto.second.add(aPair.second);
                })
            .map(
                p ->
                    Pair.create(
                        (List<GlobalPrefsBackup.ProviderDetails>) p.first,
                        p.second.toArray(new Boolean[0])))
            .blockingGet();

    try {
      GlobalPrefsBackup.restore(providers, new FileInputStream(file))
          .blockingForEach(
              providerDetails ->
                  Logger.i(
                      TAG, "Restored prefs for '%s'", getString(providerDetails.providerTitle)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Logger.w(TAG, e, "Failed to load auto-apply file!");
    }
  }

  public List<PublicNotice> getPublicNotices() {
    return Collections.unmodifiableList(mPublicNotices);
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mNightModeSubject.onNext(
        (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    mNightModeSubject.onComplete();
  }

  public Observable<Boolean> getNightModeObservable() {
    return mNightModeSubject;
  }

  private void updateStatistics(@NonNull SharedPreferences sp) {
    boolean firstAppInstall = false;
    boolean firstVersionInstall = false;

    if (!sp.contains(PREF_KEYS_FIRST_INSTALLED_APP_VERSION)) {
      firstAppInstall = true;
    }

    if (sp.getInt(PREF_KEYS_LAST_INSTALLED_APP_VERSION, 0) != BuildConfig.VERSION_CODE) {
      firstVersionInstall = true;
    }

    if (firstAppInstall || firstVersionInstall) {
      SharedPreferences.Editor editor = sp.edit();

      final long installTime = System.currentTimeMillis();
      if (firstAppInstall) {
        editor.putInt(PREF_KEYS_FIRST_INSTALLED_APP_VERSION, BuildConfig.VERSION_CODE);
        editor.putLong(PREF_KEYS_FIRST_INSTALLED_APP_TIME, installTime);
      }

      if (firstVersionInstall) {
        editor.putInt(PREF_KEYS_LAST_INSTALLED_APP_VERSION, BuildConfig.VERSION_CODE);
        editor.putLong(PREF_KEYS_LAST_INSTALLED_APP_TIME, installTime);
      }
      editor.apply();
    }
  }

  @NonNull protected QuickTextKeyFactory createQuickTextKeyFactory() {
    return new QuickTextKeyFactory(this);
  }

  @NonNull protected KeyboardThemeFactory createKeyboardThemeFactory() {
    return new KeyboardThemeFactory(this);
  }

  @NonNull protected KeyboardExtensionFactory createToolsKeyboardExtensionFactory() {
    return new KeyboardExtensionFactory(
        this,
        R.string.settings_default_ext_keyboard_key,
        KeyboardExtensionFactory.EXT_PREF_ID_PREFIX,
        KeyboardExtension.TYPE_EXTENSION);
  }

  @NonNull protected KeyboardExtensionFactory createTopKeyboardExtensionFactory() {
    return new KeyboardExtensionFactory(
        this,
        R.string.settings_default_top_row_key,
        KeyboardExtensionFactory.TOP_ROW_PREF_ID_PREFIX,
        KeyboardExtension.TYPE_TOP);
  }

  @NonNull protected KeyboardExtensionFactory createBottomKeyboardExtensionFactory() {
    return new KeyboardExtensionFactory(
        this,
        R.string.settings_default_ext_kbd_bottom_row_key,
        KeyboardExtensionFactory.BOTTOM_ROW_PREF_ID_PREFIX,
        KeyboardExtension.TYPE_BOTTOM);
  }

  @NonNull protected ExternalDictionaryFactory createExternalDictionaryFactory() {
    return new ExternalDictionaryFactory(this);
  }

  @NonNull protected KeyboardFactory createKeyboardFactory() {
    return new KeyboardFactory(this);
  }

  @CallSuper
  protected void setupCrashHandler(SharedPreferences sp) {
    JustPrintExceptionHandler globalErrorHandler = new JustPrintExceptionHandler();
    RxJavaPlugins.setErrorHandler(globalErrorHandler);
    Thread.setDefaultUncaughtExceptionHandler(globalErrorHandler);
    final Resources resources = getResources();
    if (sp.getBoolean(
        resources.getString(R.string.settings_key_show_chewbacca),
        resources.getBoolean(R.bool.settings_default_show_chewbacca))) {
      final ChewbaccaUncaughtExceptionHandler chewbaccaUncaughtExceptionHandler =
          new AnyChewbaccaUncaughtExceptionHandler(this, globalErrorHandler);
      Thread.setDefaultUncaughtExceptionHandler(chewbaccaUncaughtExceptionHandler);
      RxJavaPlugins.setErrorHandler(
          e -> chewbaccaUncaughtExceptionHandler.uncaughtException(Thread.currentThread(), e));

      if (chewbaccaUncaughtExceptionHandler.performCrashDetectingFlow()) {
        Logger.w(TAG, "Previous crash detected and reported!");
      }
    }

    Logger.setLogProvider(new NullLogProvider());
  }

  public void onPackageChanged(final Intent eventIntent, final AnySoftKeyboard ask) {
    AddOnsFactory.onExternalPackChanged(
        eventIntent,
        ask::onAddOnsCriticalChange,
        mTopRowFactory,
        mBottomRowFactory,
        mExtensionKeyboardFactory,
        mExternalDictionaryFactory,
        mKeyboardFactory,
        mKeyboardThemeFactory,
        mQuickTextKeyFactory);
  }

  public List<Drawable> getInitialWatermarksList() {
    return new ArrayList<>();
  }

  private static class JustPrintExceptionHandler
      implements Consumer<Throwable>, Thread.UncaughtExceptionHandler {
    @Override
    public void accept(Throwable throwable) throws Exception {
      throwable.printStackTrace();
      Logger.e("ASK_FATAL", throwable, "Fatal RxJava error %s", throwable.getMessage());
    }

    @Override
    public void uncaughtException(Thread t, Throwable throwable) {
      throwable.printStackTrace();
      Logger.e(
          "ASK_FATAL",
          throwable,
          "Fatal Java error '%s' on thread '%s'",
          throwable.getMessage(),
          t.toString());
    }
  }

  private static class AnyChewbaccaUncaughtExceptionHandler
      extends ChewbaccaUncaughtExceptionHandler {

    public AnyChewbaccaUncaughtExceptionHandler(
        @NonNull Context app, @Nullable Thread.UncaughtExceptionHandler previous) {
      super(app, previous);
    }

    @NonNull @Override
    protected Intent createBugReportingActivityIntent() {
      return new Intent(mApp, SendBugReportUiActivity.class);
    }

    @Override
    protected void setupNotification(@NonNull NotificationCompat.Builder builder) {
      builder
          .setSmallIcon(R.drawable.ic_notification_error)
          .setColor(ContextCompat.getColor(mApp, R.color.notification_background_error))
          .setTicker(mApp.getText(R.string.ime_crashed_ticker))
          .setContentTitle(mApp.getText(R.string.ime_name))
          .setContentText(mApp.getText(R.string.ime_crashed_sub_text));
    }

    @NonNull @Override
    protected String getAppDetails() {
      return DeveloperUtils.getAppDetails(mApp);
    }
  }
}
