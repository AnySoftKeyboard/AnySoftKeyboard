package com.anysoftkeyboard.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import android.app.Dialog;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowDialog;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GeneralDialogControllerTest {

    private GeneralDialogController mUnderTest;
    private GeneralDialogController.DialogPresenter mPresenter;

    @Before
    public void setUp() {
        mPresenter = Mockito.mock(GeneralDialogController.DialogPresenter.class);
        mUnderTest = new GeneralDialogController(RuntimeEnvironment.application, mPresenter);
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

        mUnderTest.showDialog(32);
        Mockito.verify(mPresenter).onSetupDialogRequired(any(), eq(32), isNull());
        Mockito.verifyNoMoreInteractions(mPresenter);

        final Dialog latestAlertDialog = ShadowDialog.getLatestDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertTrue(latestAlertDialog.isShowing());

        mUnderTest.dismiss();
        Assert.assertFalse(latestAlertDialog.isShowing());
        Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());
    }

    @Test
    public void testDismissBeforeNewDialog() {
        Assert.assertNull(ShadowDialog.getLatestDialog());

        mUnderTest.showDialog(32);
        Mockito.verify(mPresenter).onSetupDialogRequired(any(), eq(32), isNull());
        Mockito.verifyNoMoreInteractions(mPresenter);

        final Dialog alertDialogFor32 = ShadowDialog.getLatestDialog();
        Assert.assertNotNull(alertDialogFor32);
        Assert.assertTrue(alertDialogFor32.isShowing());

        mUnderTest.showDialog(11, "DATA");
        Mockito.verify(mPresenter).onSetupDialogRequired(any(), eq(11), eq("DATA"));
        Assert.assertFalse(alertDialogFor32.isShowing());
        final Dialog alertDialogFor11 = ShadowDialog.getLatestDialog();
        Assert.assertNotNull(alertDialogFor11);
        Assert.assertTrue(alertDialogFor11.isShowing());

        Assert.assertNotSame(alertDialogFor11, alertDialogFor32);

        mUnderTest.dismiss();
        Assert.assertFalse(alertDialogFor11.isShowing());
        Assert.assertFalse(ShadowDialog.getLatestDialog().isShowing());
    }
}