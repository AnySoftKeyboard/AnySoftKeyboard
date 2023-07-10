package com.anysoftkeyboard.addons.ui;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.addons.R;
import com.anysoftkeyboard.test.GeneralDialogTestUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowApplication;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AddOnStoreSearchControllerTest {

  @Test
  public void testSearchHappyPath() {
    Application context = ApplicationProvider.getApplicationContext();
    ShadowApplication shadowApplication = Shadows.shadowOf(context);

    final AddOnStoreSearchController underTest = new AddOnStoreSearchController(context, "add on");

    underTest.searchForAddOns();

    var leaveDialog = GeneralDialogTestUtil.getLatestShownDialog();
    Assert.assertEquals("Leaving AnySoftKeyboard", GeneralDialogTestUtil.getTitleFromDialog(leaveDialog));

    var button = leaveDialog.getButton(DialogInterface.BUTTON_POSITIVE);
    Shadows.shadowOf(button).getOnClickListener().onClick(button);

    Assert.assertSame(GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

    var intent = shadowApplication.getNextStartedActivity();
    Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
    Assert.assertEquals(
            Uri.parse("market://search?q=AnySoftKeyboard%20add%20on"), intent.getData());
  }

  @Test
  public void testCancelHappyPath() {
    Application context = ApplicationProvider.getApplicationContext();
    ShadowApplication shadowApplication = Shadows.shadowOf(context);

    final AddOnStoreSearchController underTest = new AddOnStoreSearchController(context, "add on");

    underTest.searchForAddOns();

    var leaveDialog = GeneralDialogTestUtil.getLatestShownDialog();
    Assert.assertEquals("Leaving AnySoftKeyboard", GeneralDialogTestUtil.getTitleFromDialog(leaveDialog));

    var button = leaveDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
    Shadows.shadowOf(button).getOnClickListener().onClick(button);

    Assert.assertSame(GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

    Assert.assertNull(shadowApplication.getNextStartedActivity());
  }

  @Test
  public void testDismiss() {
    Application context = ApplicationProvider.getApplicationContext();
    ShadowApplication shadowApplication = Shadows.shadowOf(context);

    final AddOnStoreSearchController underTest = new AddOnStoreSearchController(context, "add on");

    underTest.searchForAddOns();
    Assert.assertNotSame(GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());

    underTest.dismiss();
    Assert.assertSame(GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());
  }

  @Test
  public void testUtilityStart() {
    Application context = ApplicationProvider.getApplicationContext();
    Application spy = Mockito.spy(context);
    Mockito.doThrow(new RuntimeException()).when(spy).startActivity(Mockito.any());
    Assert.assertFalse(AddOnStoreSearchController.startMarketActivity(spy, "play"));
  }

  @Test
  public void testUtilityNoMarketError() {
    Application context = ApplicationProvider.getApplicationContext();
    Assert.assertTrue(AddOnStoreSearchController.startMarketActivity(context, "play"));

    var intent = Shadows.shadowOf(context).getNextStartedActivity();
    Assert.assertEquals(Intent.ACTION_VIEW, intent.getAction());
    Assert.assertEquals(Uri.parse("market://search?q=AnySoftKeyboard%20play"), intent.getData());
  }
}
