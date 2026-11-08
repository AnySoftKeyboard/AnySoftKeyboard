package com.anysoftkeyboard.test;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.test.GeneralDialogTestUtil.getLatestShownDialog;
import static com.anysoftkeyboard.test.GeneralDialogTestUtil.getTitleFromDialog;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import android.app.Dialog;
import android.os.Build;
import android.view.View;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import androidx.appcompat.app.AlertDialog;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import net.evendanan.pixel.GeneralDialogController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowDialog;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GeneralDialogControllerTest {

  private GeneralDialogController mUnderTest;
  private GeneralDialogController.DialogPresenter mPresenter;

  @Before
  public void setUp() {
    mPresenter = Mockito.mock(GeneralDialogController.DialogPresenter.class);
    mUnderTest =
        new GeneralDialogController(
            getApplicationContext(),
            androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog_Alert,
            mPresenter);
  }

  @Test
  public void testDismissWithoutShow() {
    Assert.assertNull(ShadowDialog.getLatestDialog());
    mUnderTest.dismiss();
    Assert.assertNull(ShadowDialog.getLatestDialog());
  }

  @Test
  public void testHappyPath() {
    Assert.assertNull(ShadowDialog.getLatestDialog());

    Mockito.doAnswer(
            invocation -> {
              AlertDialog.Builder builder = invocation.getArgument(0);
              builder.setTitle("TEST 32");

              return null;
            })
        .when(mPresenter)
        .onSetupDialogRequired(any(), eq(32), isNull());

    mUnderTest.showDialog(32);
    Mockito.verify(mPresenter).onSetupDialogRequired(any(), eq(32), isNull());
    Mockito.verify(mPresenter).beforeDialogShown(any(), isNull());
    Mockito.verifyNoMoreInteractions(mPresenter);

    final Dialog latestAlertDialog = ShadowDialog.getLatestDialog();
    Assert.assertNotNull(latestAlertDialog);
    Assert.assertTrue(latestAlertDialog.isShowing());
    Assert.assertEquals(
        GeneralDialogController.TAG_VALUE,
        latestAlertDialog.getWindow().getDecorView().getTag(GeneralDialogController.TAG_ID));
    Assert.assertEquals("TEST 32", getTitleFromDialog(latestAlertDialog));

    mUnderTest.dismiss();
    Assert.assertFalse(latestAlertDialog.isShowing());
    Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());
  }

  @Test
  public void testAlsoCallBeforeShow() {
    Mockito.doAnswer(
            invocation -> {
              AlertDialog.Builder builder = invocation.getArgument(0);
              builder.setTitle("TEST 32");

              return null;
            })
        .when(mPresenter)
        .onSetupDialogRequired(any(), eq(32), isNull());

    final ArgumentCaptor<AlertDialog> argumentCaptor = ArgumentCaptor.forClass(AlertDialog.class);
    mUnderTest.showDialog(32);
    Mockito.verify(mPresenter).onSetupDialogRequired(any(), eq(32), isNull());
    Mockito.verify(mPresenter).beforeDialogShown(argumentCaptor.capture(), isNull());
    Mockito.verifyNoMoreInteractions(mPresenter);

    Assert.assertSame(ShadowDialog.getLatestDialog(), argumentCaptor.getValue());
  }

  @Test
  public void testDismissBeforeNewDialog() {
    Assert.assertNull(ShadowDialog.getLatestDialog());

    mUnderTest.showDialog(32);
    Mockito.verify(mPresenter).onSetupDialogRequired(any(), eq(32), isNull());
    Mockito.verify(mPresenter).beforeDialogShown(any(), isNull());
    Mockito.verifyNoMoreInteractions(mPresenter);

    final Dialog alertDialogFor32 = ShadowDialog.getLatestDialog();
    Assert.assertNotNull(alertDialogFor32);
    Assert.assertTrue(alertDialogFor32.isShowing());
    Assert.assertSame(getLatestShownDialog(), alertDialogFor32);

    mUnderTest.showDialog(11, "DATA");
    Mockito.verify(mPresenter).onSetupDialogRequired(any(), eq(11), eq("DATA"));
    Mockito.verify(mPresenter).beforeDialogShown(any(), eq("DATA"));
    Assert.assertFalse(alertDialogFor32.isShowing());
    final Dialog alertDialogFor11 = ShadowDialog.getLatestDialog();
    Assert.assertNotNull(alertDialogFor11);
    Assert.assertTrue(alertDialogFor11.isShowing());
    Assert.assertSame(getLatestShownDialog(), alertDialogFor11);

    Assert.assertNotSame(alertDialogFor11, alertDialogFor32);

    mUnderTest.dismiss();
    Assert.assertFalse(alertDialogFor11.isShowing());
    Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void testPredictiveBackAnimationLifecycleOnApi34() {
    mUnderTest.showDialog(32);
    final Dialog dialog = ShadowDialog.getLatestDialog();
    Assert.assertNotNull(dialog);
    Assert.assertTrue(dialog.isShowing());

    final var helper = mUnderTest.getBackAnimationHelper();
    Assert.assertNotNull(helper);

    final OnBackAnimationCallback callback = helper.getCallback();
    Assert.assertNotNull(callback);

    final View decorView = dialog.getWindow().getDecorView();
    decorView.layout(0, 0, 200, 300);

    callback.onBackStarted(new BackEvent(10f, 10f, 0.0f, BackEvent.EDGE_LEFT));
    Assert.assertEquals(100f, decorView.getPivotX(), 0.01f);
    Assert.assertEquals(150f, decorView.getPivotY(), 0.01f);

    callback.onBackProgressed(new BackEvent(50f, 10f, 0.5f, BackEvent.EDGE_LEFT));
    Assert.assertEquals(0.95f, decorView.getScaleX(), 0.01f);
    Assert.assertEquals(0.95f, decorView.getScaleY(), 0.01f);
    Assert.assertEquals(0.75f, decorView.getAlpha(), 0.01f);
    Assert.assertEquals(5.0f, decorView.getTranslationX(), 0.01f);

    callback.onBackProgressed(new BackEvent(50f, 10f, 0.5f, BackEvent.EDGE_RIGHT));
    Assert.assertEquals(-5.0f, decorView.getTranslationX(), 0.01f);

    callback.onBackCancelled();

    callback.onBackInvoked();
    Assert.assertFalse(dialog.isShowing());
    Assert.assertNull(mUnderTest.getBackAnimationHelper());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.TIRAMISU)
  public void testNoPredictiveBackOnApi33() {
    mUnderTest.showDialog(32);
    final Dialog dialog = ShadowDialog.getLatestDialog();
    Assert.assertNotNull(dialog);
    Assert.assertTrue(dialog.isShowing());

    Assert.assertNull(mUnderTest.getBackAnimationHelper());

    mUnderTest.dismiss();
    Assert.assertFalse(dialog.isShowing());
  }
}
