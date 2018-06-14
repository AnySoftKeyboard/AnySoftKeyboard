package com.anysoftkeyboard;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.AnyKeyboardView;
import com.anysoftkeyboard.keyboards.views.KeyboardViewContainerView;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardViewRelatedTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testOnCreateInputView() throws Exception {
        View mainKeyboardView = mAnySoftKeyboardUnderTest.getInputViewContainer();
        Assert.assertNotNull(mainKeyboardView);
        Assert.assertTrue(mainKeyboardView instanceof KeyboardViewContainerView);
        KeyboardViewContainerView containerView = (KeyboardViewContainerView) mainKeyboardView;
        Assert.assertEquals(1, containerView.getChildCount());
        final View inputView = containerView.getChildAt(0);
        Assert.assertNotNull(inputView);
        Assert.assertTrue(inputView instanceof AnyKeyboardView);
        Assert.assertSame(inputView, containerView.getStandardKeyboardView());
        Mockito.verify(containerView.getStandardKeyboardView()).setWatermark("α\uD83D\uDD25");
    }

    @Test
    public void testSettingsBasic() throws Exception {
        Assert.assertNull(ShadowAlertDialog.getLatestAlertDialog());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SETTINGS);
        final AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);

        final ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(latestAlertDialog);
        Assert.assertEquals("AnySoftKeyboard", shadowAlertDialog.getTitle());
        Assert.assertEquals(4, shadowAlertDialog.getItems().length);

    }

    @Test
    public void testSettingsIncognito() throws Exception {
        //initial watermark
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setWatermark(Mockito.contains("\uD83D\uDD75"));

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        Assert.assertNull(ShadowAlertDialog.getLatestAlertDialog());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SETTINGS);
        final AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        final ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(latestAlertDialog);

        Assert.assertEquals("\uD83D\uDD75️ Incognito Mode", shadowAlertDialog.getItems()[3]);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getQuickKeyHistoryRecords().isIncognitoMode());

        shadowAlertDialog.clickOnItem(3);

        Assert.assertTrue(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getQuickKeyHistoryRecords().isIncognitoMode());
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView()).setWatermark(Mockito.contains("\uD83D\uDD75"));

        Mockito.reset(mAnySoftKeyboardUnderTest.getInputView());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SETTINGS);
        Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog()).clickOnItem(3);

        Assert.assertFalse(mAnySoftKeyboardUnderTest.getSpiedSuggest().isIncognitoMode());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getQuickKeyHistoryRecords().isIncognitoMode());
        Mockito.verify(mAnySoftKeyboardUnderTest.getInputView(), Mockito.never()).setWatermark(Mockito.contains("\uD83D\uDD75"));
    }

    @Test
    public void testSettingsOverrideDictionary() throws Exception {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.SETTINGS);
        final AlertDialog settingsAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        final ShadowAlertDialog shadowSettingsAlertDialog = Shadows.shadowOf(settingsAlertDialog);

        Assert.assertEquals("Override default dictionary", shadowSettingsAlertDialog.getItems()[1]);

        shadowSettingsAlertDialog.clickOnItem(1);

        final AlertDialog dictionaryAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotSame(dictionaryAlertDialog, settingsAlertDialog);
        final ShadowAlertDialog shadowDictionaryAlertDialog = Shadows.shadowOf(dictionaryAlertDialog);

        Assert.assertEquals("Override English dictionary", shadowDictionaryAlertDialog.getTitle());
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
    @Config(qualifiers = "land")
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
}