package com.anysoftkeyboard;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.view.KeyEvent;
import android.widget.Toast;

import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowToast;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardClipboardTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testSelectsAllText() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing something very long";
        inputConnection.commitText(expectedText, 1);

        Assert.assertEquals("", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT_ALL);
        Assert.assertEquals(expectedText, inputConnection.getSelectedText(0).toString());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardCopy() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing something very long";
        inputConnection.commitText(expectedText, 1);
        inputConnection.setSelection("testing ".length(), "testing something".length());
        Assert.assertEquals("something", inputConnection.getSelectedText(0).toString());

        ClipboardManager clipboardManager = (ClipboardManager) RuntimeEnvironment.application.getSystemService(Service.CLIPBOARD_SERVICE);
        Assert.assertNull(clipboardManager.getPrimaryClip());

        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        //text stays the same
        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        //and clipboard has the copied text
        Assert.assertEquals(1, clipboardManager.getPrimaryClip().getItemCount());
        Assert.assertEquals("something", clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardCut() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String originalText = "testing something very long";
        final String textToCut = "something";
        final String expectedText = "testing  very long";
        inputConnection.commitText(originalText, 1);
        inputConnection.setSelection("testing ".length(), "testing something".length());
        Assert.assertEquals(textToCut, inputConnection.getSelectedText(0).toString());

        ClipboardManager clipboardManager = (ClipboardManager) RuntimeEnvironment.application.getSystemService(Service.CLIPBOARD_SERVICE);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_CUT);

        //text without "something"
        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        //and clipboard has the copied text
        Assert.assertEquals(1, clipboardManager.getPrimaryClip().getItemCount());
        Assert.assertEquals("something", clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardPaste() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        ClipboardManager clipboardManager = (ClipboardManager) RuntimeEnvironment.application.getSystemService(Service.CLIPBOARD_SERVICE);
        final String expectedText = "some text";
        clipboardManager.setPrimaryClip(new ClipData("ask", new String[]{"text"}, new ClipData.Item(expectedText)));

        Assert.assertEquals("", inputConnection.getSelectedText(0).toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE);
        Assert.assertEquals(expectedText, inputConnection.getCurrentTextInInputConnection());
        //and backspace DOES NOT deletes the pasted text
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals(expectedText.substring(0, expectedText.length() - 1), inputConnection.getCurrentTextInInputConnection());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardPasteWhenEmptyClipboard() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE);
        Assert.assertEquals("", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals(mAnySoftKeyboardUnderTest.getText(R.string.clipboard_is_empty_toast), ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testSelectionExpending_AtEndOfInput() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        inputConnection.commitText("some text in the input connection", 1);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        Assert.assertEquals("", inputConnection.getSelectedText(0).toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
        Assert.assertEquals("n", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
        Assert.assertEquals("on", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("on", inputConnection.getSelectedText(0).toString());
    }

    @Test
    public void testSelectionExpending_AtMiddleOfInput() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        inputConnection.commitText("some text in the input connection", 1);
        inputConnection.setSelection("some ".length(), "some ".length());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        Assert.assertEquals("", inputConnection.getSelectedText(0).toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("t", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("te", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
        Assert.assertEquals(" te", inputConnection.getSelectedText(0).toString());
    }

    @Test
    public void testSelectionExpendingCancel() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        inputConnection.commitText("some text in the input connection", 1);
        inputConnection.setSelection("some ".length(), "some ".length());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        Assert.assertEquals("", inputConnection.getSelectedText(0).toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("t", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("te", inputConnection.getSelectedText(0).toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress('k');
        //selection was replaced with space
        Assert.assertEquals("", inputConnection.getSelectedText(0).toString());
        Assert.assertEquals("some kxt in the input connection", inputConnection.getCurrentTextInInputConnection());
        Assert.assertEquals("some k".length(), inputConnection.getCurrentStartPosition());
        //and we are no longer is select state
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("", inputConnection.getSelectedText(0).toString());
    }

    @Test
    public void testSelectionExpendingWithAlreadySelectedText() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        inputConnection.commitText("some text in the input connection", 1);
        inputConnection.setSelection("some ".length(), "some text".length());
        //we already have selection set
        Assert.assertEquals("text", inputConnection.getSelectedText(0).toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        Assert.assertEquals("text", inputConnection.getSelectedText(0).toString());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("text ", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("text i", inputConnection.getSelectedText(0).toString());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
        Assert.assertEquals(" text i", inputConnection.getSelectedText(0).toString());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardCopyToast() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing something very long";
        inputConnection.commitText(expectedText, 1);
        inputConnection.setSelection("testing ".length(), "testing something".length());

        //first five times should include a tip
        for (int copyCount = 0; copyCount < 5; copyCount++) {
            mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
            final Toast latestToast = ShadowToast.getLatestToast();
            Assert.assertNotNull(latestToast);
            Assert.assertEquals(Toast.LENGTH_SHORT, latestToast.getDuration());
            Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.clipboard_copy_done_toast_with_long_press_tip), ShadowToast.getTextOfLatestToast());
        }

        //following copy operations should show a toast WITHOUT the tip
        for (int copyCount = 0; copyCount < 5; copyCount++) {
            mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
            final Toast latestToast = ShadowToast.getLatestToast();
            Assert.assertNotNull(latestToast);
            Assert.assertEquals(Toast.LENGTH_SHORT, latestToast.getDuration());
            Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.clipboard_copy_done_toast), ShadowToast.getTextOfLatestToast());
        }
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardFineSelectToast() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing something very long";
        inputConnection.commitText(expectedText, 1);
        inputConnection.setSelection("testing ".length(), "testing something".length());

        //first five times should include a tip
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.clipboard_copy_done_toast_with_long_press_tip), ShadowToast.getTextOfLatestToast());

        //now, we'll do long-press
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        final Toast latestToast = ShadowToast.getLatestToast();
        Assert.assertNotNull(latestToast);
        Assert.assertEquals(Toast.LENGTH_SHORT, latestToast.getDuration());
        Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.clipboard_fine_select_enabled_toast), ShadowToast.getTextOfLatestToast());

        //and if we try to copy again, we should not see the long-press tip
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        Assert.assertEquals(RuntimeEnvironment.application.getString(R.string.clipboard_copy_done_toast), ShadowToast.getTextOfLatestToast());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardShowsOptionsToCopy() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        final String expectedText = "testing something very long";
        inputConnection.commitText(expectedText, 1);
        inputConnection.setSelection("testing ".length(), "testing something very".length());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        inputConnection.setSelection(0, "testing ".length());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        //now, we'll do long-press
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        final AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);
        final ShadowAlertDialog shadowAlertDialog = Shadows.shadowOf(latestAlertDialog);
        Assert.assertEquals("Pick text to paste", shadowAlertDialog.getTitle());
        Assert.assertEquals(2, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals("testing ", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals("something very", latestAlertDialog.getListView().getAdapter().getItem(1).toString());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardShowsOptionsWhenPrimaryClipChanged() {
        ClipboardManager shadowManager = (ClipboardManager) RuntimeEnvironment.application.getSystemService(Context.CLIPBOARD_SERVICE);
        shadowManager.setPrimaryClip(new ClipData("text 1", new String[0], new ClipData.Item("text 1")));
        shadowManager.setPrimaryClip(new ClipData("text 2", new String[0], new ClipData.Item("text 2")));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(2, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals("text 2", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals("text 1", latestAlertDialog.getListView().getAdapter().getItem(1).toString());

        latestAlertDialog.cancel();

        mAnySoftKeyboardUnderTest.simulateTextTyping("text 3");
        mAnySoftKeyboardUnderTest.getCurrentInputConnection().setSelection(1, 4);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(3, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals("ext", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals("text 2", latestAlertDialog.getListView().getAdapter().getItem(1).toString());
        Assert.assertEquals("text 1", latestAlertDialog.getListView().getAdapter().getItem(2).toString());

        latestAlertDialog.cancel();

        for (int clipIndex = 0; clipIndex < 100; clipIndex++) {
            shadowManager.setPrimaryClip(new ClipData("text " + clipIndex, new String[0], new ClipData.Item("text " + clipIndex)));
        }

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(16, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals("text 99", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals("text 98", latestAlertDialog.getListView().getAdapter().getItem(1).toString());
        Assert.assertEquals("text 97", latestAlertDialog.getListView().getAdapter().getItem(2).toString());
        Assert.assertEquals("text 96", latestAlertDialog.getListView().getAdapter().getItem(3).toString());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testClipboardDoesNotShowsOptionsWhenPrimaryClipChangedAndSyncIsDisabled() {
        ClipboardManager shadowManager = (ClipboardManager) RuntimeEnvironment.application.getSystemService(Context.CLIPBOARD_SERVICE);
        shadowManager.setPrimaryClip(new ClipData("text 1", new String[0], new ClipData.Item("text 1")));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_os_clipboard_sync, false);

        shadowManager.setPrimaryClip(new ClipData("text 2", new String[0], new ClipData.Item("text 2")));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        AlertDialog latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(1, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals("text 1", latestAlertDialog.getListView().getAdapter().getItem(0).toString());

        latestAlertDialog.cancel();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_os_clipboard_sync, true);

        shadowManager.setPrimaryClip(new ClipData("text 3", new String[0], new ClipData.Item("text 3")));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        latestAlertDialog = ShadowAlertDialog.getLatestAlertDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(2, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals("text 3", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals("text 1", latestAlertDialog.getListView().getAdapter().getItem(1).toString());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testUndo() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.UNDO);
        ArgumentCaptor<KeyEvent> keyEventArgumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        Mockito.verify(inputConnection, Mockito.times(2)).sendKeyEvent(keyEventArgumentCaptor.capture());

        Assert.assertEquals(KeyEvent.ACTION_DOWN, keyEventArgumentCaptor.getAllValues().get(0).getAction());
        Assert.assertEquals(KeyEvent.META_CTRL_ON, keyEventArgumentCaptor.getAllValues().get(0).getMetaState());
        Assert.assertEquals(KeyEvent.KEYCODE_Z, keyEventArgumentCaptor.getAllValues().get(0).getKeyCode());

        Assert.assertEquals(KeyEvent.ACTION_UP, keyEventArgumentCaptor.getAllValues().get(1).getAction());
        Assert.assertEquals(KeyEvent.META_CTRL_ON, keyEventArgumentCaptor.getAllValues().get(1).getMetaState());
        Assert.assertEquals(KeyEvent.KEYCODE_Z, keyEventArgumentCaptor.getAllValues().get(1).getKeyCode());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void testRedo() {
        TestInputConnection inputConnection = (TestInputConnection) mAnySoftKeyboardUnderTest.getCurrentInputConnection();
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.REDO);
        ArgumentCaptor<KeyEvent> keyEventArgumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        Mockito.verify(inputConnection, Mockito.times(2)).sendKeyEvent(keyEventArgumentCaptor.capture());

        Assert.assertEquals(KeyEvent.ACTION_DOWN, keyEventArgumentCaptor.getAllValues().get(0).getAction());
        Assert.assertEquals(KeyEvent.META_CTRL_ON | KeyEvent.META_SHIFT_ON, keyEventArgumentCaptor.getAllValues().get(0).getMetaState());
        Assert.assertEquals(KeyEvent.KEYCODE_Z, keyEventArgumentCaptor.getAllValues().get(0).getKeyCode());

        Assert.assertEquals(KeyEvent.ACTION_UP, keyEventArgumentCaptor.getAllValues().get(1).getAction());
        Assert.assertEquals(KeyEvent.META_CTRL_ON | KeyEvent.META_SHIFT_ON, keyEventArgumentCaptor.getAllValues().get(1).getMetaState());
        Assert.assertEquals(KeyEvent.KEYCODE_Z, keyEventArgumentCaptor.getAllValues().get(1).getKeyCode());
    }
}