package com.anysoftkeyboard;

import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.ime.InputViewBinder;
import com.anysoftkeyboard.utils.GeneralDialogTestUtil;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardViewRelatedTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testSettingsBasic() throws Exception {
        Assert.assertEquals(GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SETTINGS);
        final AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);

        Assert.assertEquals("AnySoftKeyboard", GeneralDialogTestUtil.getTitleFromDialog(latestAlertDialog));
        Assert.assertEquals(4, latestAlertDialog.getListView().getCount());

    }

    @Test
    public void testSettingsIncognito() throws Exception {
        //initial watermark
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_incognito);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        Assert.assertEquals(GeneralDialogTestUtil.NO_DIALOG, GeneralDialogTestUtil.getLatestShownDialog());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SETTINGS);

        AlertDialog latestShownDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertEquals("\uD83D\uDD75️ Incognito Mode", latestShownDialog.getListView().getAdapter().getItem(3));

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getQuickKeyHistoryRecords().isIncognitoMode());

        Shadows.shadowOf(latestShownDialog.getListView()).performItemClick(3);

        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getQuickKeyHistoryRecords().isIncognitoMode());
        ViewTestUtils.assertCurrentWatermarkHasDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_incognito);

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SETTINGS);
        latestShownDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Shadows.shadowOf(latestShownDialog.getListView()).performItemClick(3);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getQuickKeyHistoryRecords().isIncognitoMode());
        ViewTestUtils.assertCurrentWatermarkDoesNotHaveDrawable(mAnySoftKeyboardUnderTest.getInputView(), R.drawable.ic_watermark_incognito);
    }

    @Test
    public void testSettingsOverrideDictionary() throws Exception {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SETTINGS);
        final AlertDialog settingsAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();

        Assert.assertEquals("Override default dictionary", settingsAlertDialog.getListView().getAdapter().getItem(1));

        Shadows.shadowOf(settingsAlertDialog.getListView()).performItemClick(1);

        final AlertDialog dictionaryAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotSame(dictionaryAlertDialog, settingsAlertDialog);

        Assert.assertEquals("Override English dictionary", GeneralDialogTestUtil.getTitleFromDialog(dictionaryAlertDialog));
        View.OnClickListener positiveListener = Shadows.shadowOf(dictionaryAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)).getOnClickListener();
        View.OnClickListener negativeListener = Shadows.shadowOf(dictionaryAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)).getOnClickListener();
        View.OnClickListener clearListener = Shadows.shadowOf(dictionaryAlertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)).getOnClickListener();

        Assert.assertNotNull(positiveListener);
        Assert.assertNotNull(negativeListener);
        Assert.assertNotNull(clearListener);
    }

    @Test
    public void testSetInputViewClippingIssues() throws Exception {
        Assert.assertFalse(mAnySoftKeyboardUnderTest.isFullscreenMode());
        final Window window = mAnySoftKeyboardUnderTest.getWindow().getWindow();
        Assert.assertNotNull(window);
        Assert.assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, window.getAttributes().height);

        final View inputArea = window.findViewById(android.R.id.inputArea);
        Assert.assertNotNull(inputArea);
        Assert.assertNotNull(inputArea.getParent());

        final View parentView = (View) inputArea.getParent();
        Assert.assertEquals(ViewGroup.LayoutParams.WRAP_CONTENT, parentView.getLayoutParams().height);
        Assert.assertEquals(Gravity.BOTTOM, ((FrameLayout.LayoutParams) parentView.getLayoutParams()).gravity);
    }

    @Test
    @Config(qualifiers = "w480dp-h800dp-land-mdpi")
    public void testSetInputViewClippingIssuesInLandscape() throws Exception {
        Assert.assertTrue(mAnySoftKeyboardUnderTest.isFullscreenMode());
        final Window window = mAnySoftKeyboardUnderTest.getWindow().getWindow();
        Assert.assertNotNull(window);
        Assert.assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, window.getAttributes().height);

        final View inputArea = window.findViewById(android.R.id.inputArea);
        Assert.assertNotNull(inputArea);
        Assert.assertNotNull(inputArea.getParent());

        final View parentView = (View) inputArea.getParent();
        Assert.assertEquals(ViewGroup.LayoutParams.MATCH_PARENT, parentView.getLayoutParams().height);
        Assert.assertEquals(Gravity.BOTTOM, ((FrameLayout.LayoutParams) parentView.getLayoutParams()).gravity);
    }

    @Test
    public void testResetViewOnAddOnChange() throws Exception {
        final InputViewBinder inputView = mAnySoftKeyboardUnderTest.getInputView();
        Assert.assertNotNull(inputView);
        mAnySoftKeyboardUnderTest.onAddOnsCriticalChange();
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getInputView());
        Assert.assertSame(inputView, mAnySoftKeyboardUnderTest.getInputView());
    }
}