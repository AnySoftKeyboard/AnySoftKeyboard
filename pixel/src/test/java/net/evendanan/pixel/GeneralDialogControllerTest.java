package net.evendanan.pixel;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static net.evendanan.pixel.GeneralDialogTestUtil.getLatestShownDialog;
import static net.evendanan.pixel.GeneralDialogTestUtil.getTitleFromDialog;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import android.app.Dialog;
import android.support.v7.app.AlertDialog;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.shadows.ShadowDialog;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class GeneralDialogControllerTest {

    private GeneralDialogController mUnderTest;
    private GeneralDialogController.DialogPresenter mPresenter;

    @Before
    public void setUp() {
        mPresenter = Mockito.mock(GeneralDialogController.DialogPresenter.class);
        mUnderTest = new GeneralDialogController(getApplicationContext(), mPresenter);
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
                latestAlertDialog
                        .getWindow()
                        .getDecorView()
                        .getTag(GeneralDialogController.TAG_ID));
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

        final ArgumentCaptor<AlertDialog> argumentCaptor =
                ArgumentCaptor.forClass(AlertDialog.class);
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
}
