package com.anysoftkeyboard.ui.settings.setup;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Application;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.keyboards.KeyboardSupport;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.AnyRoboApplication;
import com.menny.android.anysoftkeyboard.InputMethodManagerShadow;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SetupSupportTest {

  private AnyRoboApplication mApplication;

  @Before
  public void setup() {
    Locale.setDefault(Locale.US);
    mApplication = getApplicationContext();
  }

  @After
  public void tearDown() {
    Locale.setDefault(Locale.US);
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testIsThisKeyboardSetAsDefaultIME_before34() throws Exception {
    var app = RuntimeEnvironment.getApplication();
    InputMethodManagerShadow.setKeyboardAsCurrent(app, true);
    assertTrue(SetupSupport.isThisKeyboardSetAsDefaultIME(app));

    InputMethodManagerShadow.setKeyboardAsCurrent(app, false);
    assertFalse(SetupSupport.isThisKeyboardSetAsDefaultIME(app));
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testIsThisKeyboardSetAsDefaultIME_api34AndAbove() throws Exception {
    var app = RuntimeEnvironment.getApplication();
    InputMethodManagerShadow.setKeyboardAsCurrent(app, true);
    assertTrue(SetupSupport.isThisKeyboardSetAsDefaultIME(app));

    InputMethodManagerShadow.setKeyboardAsCurrent(app, false);
    assertFalse(SetupSupport.isThisKeyboardSetAsDefaultIME(app));
  }

  @Test
  public void testIsThisKeyboardEnabled() throws Exception {
    Application application = RuntimeEnvironment.getApplication();
    assertTrue(SetupSupport.isThisKeyboardEnabled(application));

    InputMethodManagerShadow.setKeyboardEnabled(application, false);
    assertFalse(SetupSupport.isThisKeyboardEnabled(application));
  }

  @Test
  public void testHasLanguagePackForCurrentLocale() {
    final KeyboardFactory spiedKeyboardFactory = mApplication.getSpiedKeyboardFactory();
    ArrayList<KeyboardAddOnAndBuilder> mockResponse =
        new ArrayList<>(spiedKeyboardFactory.getAllAddOns());

    Assert.assertTrue(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

    Locale.setDefault(Locale.FRENCH);
    Assert.assertFalse(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

    KeyboardAddOnAndBuilder frenchBuilder = Mockito.mock(KeyboardAddOnAndBuilder.class);
    Mockito.doReturn("fr").when(frenchBuilder).getKeyboardLocale();
    mockResponse.add(frenchBuilder);

    Assert.assertTrue(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

    Locale.setDefault(new Locale("he"));
    Assert.assertFalse(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

    KeyboardAddOnAndBuilder hebrewBuilder = Mockito.mock(KeyboardAddOnAndBuilder.class);
    Mockito.doReturn("iw").when(hebrewBuilder).getKeyboardLocale();
    mockResponse.add(hebrewBuilder);

    Locale.setDefault(new Locale("iw"));
    Assert.assertTrue(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

    Locale.setDefault(new Locale("ru"));
    Assert.assertFalse(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));

    Mockito.doReturn("ru").when(hebrewBuilder).getKeyboardLocale();
    Assert.assertTrue(SetupSupport.hasLanguagePackForCurrentLocale(mockResponse));
  }

  @Test
  public void testPopupAnimation() {
    View v1 = Mockito.mock(View.class);
    View v2 = Mockito.mock(View.class);
    Mockito.doReturn(mApplication).when(v1).getContext();
    Mockito.doReturn(mApplication).when(v2).getContext();

    SetupSupport.popupViewAnimation(v1, v2);

    ArgumentCaptor<Animation> animation1Captor = ArgumentCaptor.forClass(Animation.class);
    ArgumentCaptor<Animation> animation2Captor = ArgumentCaptor.forClass(Animation.class);
    Mockito.verify(v1).startAnimation(animation1Captor.capture());
    Mockito.verify(v2).startAnimation(animation2Captor.capture());

    Animation animation1 = animation1Captor.getValue();
    Animation animation2 = animation2Captor.getValue();

    Assert.assertEquals(500, animation1.getStartOffset());
    Assert.assertEquals(700, animation2.getStartOffset());
  }

  @Test
  public void testPopupViewAnimationWithIds() {
    View v1 = Mockito.mock(View.class);
    View v2 = Mockito.mock(View.class);
    Mockito.doReturn(mApplication).when(v1).getContext();
    Mockito.doReturn(mApplication).when(v2).getContext();

    View rootView = Mockito.mock(View.class);
    Mockito.doReturn(v1).when(rootView).findViewById(1);
    Mockito.doReturn(v2).when(rootView).findViewById(2);

    SetupSupport.popupViewAnimationWithIds(rootView, 1, 0, 2);

    ArgumentCaptor<Animation> animation1Captor = ArgumentCaptor.forClass(Animation.class);
    ArgumentCaptor<Animation> animation2Captor = ArgumentCaptor.forClass(Animation.class);
    Mockito.verify(v1).startAnimation(animation1Captor.capture());
    Mockito.verify(v2).startAnimation(animation2Captor.capture());

    Animation animation1 = animation1Captor.getValue();
    Animation animation2 = animation2Captor.getValue();

    Assert.assertEquals(500, animation1.getStartOffset());
    Assert.assertEquals(900, animation2.getStartOffset());
  }

  @Test
  public void testKeyboardZoomFactorPortrait() {
    var context = RuntimeEnvironment.getApplication();
    context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;

    List<Float> values = new ArrayList<>();
    Disposable disposable = KeyboardSupport.getKeyboardHeightFactor(context).subscribe(values::add);

    // default value
    Assert.assertEquals(1.0f, values.remove(0), 0.001f);

    // landscape does not affect it
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_zoom_percent_in_landscape, 120);
    Assert.assertEquals(0, values.size());

    // changing value
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_zoom_percent_in_portrait, 150);
    Assert.assertEquals(1.5f, values.remove(0), 0.001f);
    disposable.dispose();
  }

  @Test
  public void testKeyboardZoomFactorLandscape() {
    var context = RuntimeEnvironment.getApplication();
    context.getResources().getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;

    List<Float> values = new ArrayList<>();
    Disposable disposable = KeyboardSupport.getKeyboardHeightFactor(context).subscribe(values::add);

    // default value
    Assert.assertEquals(1.0f, values.remove(0), 0.001f);

    // portrait does not affect it
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_zoom_percent_in_portrait, 120);
    Assert.assertEquals(0, values.size());

    // changing value
    SharedPrefsHelper.setPrefsValue(R.string.settings_key_zoom_percent_in_landscape, 150);
    Assert.assertEquals(1.5f, values.remove(0), 0.001f);
    disposable.dispose();
  }
}
