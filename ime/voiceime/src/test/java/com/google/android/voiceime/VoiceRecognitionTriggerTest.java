package com.google.android.voiceime;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.inputmethodservice.InputMethodService;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class VoiceRecognitionTriggerTest {

  InputMethodManager mMockInputMethodManager;
  InputMethodService mMockInputMethodService;
  PackageManager mMockPackageManager;

  List<InputMethodInfo> inputMethods;
  List<ResolveInfo> voiceActivities;

  @Before
  public void setUp() {
    inputMethods = new ArrayList<>();
    voiceActivities = new ArrayList<>();

    mMockInputMethodManager = Mockito.mock(InputMethodManager.class);
    Mockito.when(mMockInputMethodManager.getEnabledInputMethodList()).thenReturn(inputMethods);

    mMockInputMethodService = Mockito.mock(InputMethodService.class);
    Mockito.when(mMockInputMethodService.getSystemService(Context.INPUT_METHOD_SERVICE))
        .thenReturn(mMockInputMethodManager);

    mMockPackageManager = Mockito.mock(PackageManager.class);
    Mockito.when(mMockInputMethodService.getPackageManager()).thenReturn(mMockPackageManager);

    Mockito.when(mMockPackageManager.queryIntentActivities(Mockito.any(), Mockito.eq(0)))
        .thenReturn(voiceActivities);
  }

  private void addInputMethodInfo(List<String> modes) {
    InputMethodInfo inputMethod = Mockito.mock(InputMethodInfo.class);

    Mockito.when(inputMethod.getSubtypeCount()).thenReturn(modes.size());

    for (int i = 0; i < modes.size(); i++) {
      InputMethodSubtype subtype = Mockito.mock(InputMethodSubtype.class);
      Mockito.when(subtype.getMode()).thenReturn(modes.get(i));
      Mockito.when(inputMethod.getSubtypeAt(i)).thenReturn(subtype);
    }

    inputMethods.add(inputMethod);
  }

  @Test
  public void testImeNotInstalledWhenNoVoice() {
    addInputMethodInfo(List.of("keyboard", "keyboard", "handwriting"));
    addInputMethodInfo(List.of("handwriting"));
    addInputMethodInfo(List.of("handwriting", "keyboard", "keyboard", "keyboard"));

    Assert.assertFalse(ImeTrigger.isInstalled(mMockInputMethodService));
  }

  @Test
  public void testImeInstalledWhenOnlyVoice() {
    addInputMethodInfo(List.of("voice"));

    Assert.assertTrue(ImeTrigger.isInstalled(mMockInputMethodService));
  }

  @Test
  public void testImeInstalledWhenMixedVoice() {
    addInputMethodInfo(List.of("keyboard", "keyboard", "handwriting"));
    addInputMethodInfo(List.of("handwriting"));
    addInputMethodInfo(List.of("handwriting", "keyboard", "voice", "keyboard", "keyboard"));

    Assert.assertTrue(ImeTrigger.isInstalled(mMockInputMethodService));
  }

  @Test
  public void testIntentNotInstalledWhenNoActivities() {
    Assert.assertFalse(IntentApiTrigger.isInstalled(mMockInputMethodService));
  }

  @Test
  public void testIntentInstalledWhenSomeActivity() {
    voiceActivities.add(new ResolveInfo());
    Assert.assertTrue(IntentApiTrigger.isInstalled(mMockInputMethodService));
  }

  @Test
  public void testVoiceRecognitionTriggerNoneWhenNothing() {
    VoiceRecognitionTrigger trigger = new VoiceRecognitionTrigger(mMockInputMethodService);
    Assert.assertEquals("none", trigger.getKind());
  }

  @Test
  public void testVoiceRecognitionTriggerPrioritizesIme() {
    addInputMethodInfo(List.of("voice"));
    voiceActivities.add(new ResolveInfo());

    VoiceRecognitionTrigger trigger = new VoiceRecognitionTrigger(mMockInputMethodService);
    Assert.assertEquals("ime", trigger.getKind());
  }

  @Test
  public void testVoiceRecognitionTriggerFallsBackToIntent() {
    addInputMethodInfo(List.of("keyboard"));
    voiceActivities.add(new ResolveInfo());

    VoiceRecognitionTrigger trigger = new VoiceRecognitionTrigger(mMockInputMethodService);
    Assert.assertEquals("intent", trigger.getKind());
  }

  @Test
  public void testVoiceRecognitionTriggerAcceptsIme() {
    addInputMethodInfo(List.of("voice"));

    VoiceRecognitionTrigger trigger = new VoiceRecognitionTrigger(mMockInputMethodService);
    Assert.assertEquals("ime", trigger.getKind());
  }
}
