package com.anysoftkeyboard.ime;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.anysoftkeyboard.TestableAnySoftKeyboard.createEditorInfo;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.anysoftkeyboard.utils.GeneralDialogTestUtil;
import com.menny.android.anysoftkeyboard.R;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowSystemClock;
import org.robolectric.shadows.ShadowToast;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardClipboardTest extends AnySoftKeyboardBaseTest {

    @Test
    public void testSelectsAllText() {
        final String expectedText = "testing something very long";
        mAnySoftKeyboardUnderTest.simulateTextTyping(expectedText);

        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT_ALL);
        Assert.assertEquals(expectedText, mAnySoftKeyboardUnderTest.getCurrentSelectedText());
    }

    @Test
    public void testClipboardCopy() {
        final String expectedText = "testing something very long";
        mAnySoftKeyboardUnderTest.simulateTextTyping(expectedText);
        mAnySoftKeyboardUnderTest.setSelectedText(
                "testing ".length(), "testing something".length(), true);
        Assert.assertEquals("something", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Service.CLIPBOARD_SERVICE);
        Assert.assertNull(clipboardManager.getPrimaryClip());

        Assert.assertEquals(
                expectedText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        // text stays the same
        Assert.assertEquals(
                expectedText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        // and clipboard has the copied text
        Assert.assertEquals(1, clipboardManager.getPrimaryClip().getItemCount());
        Assert.assertEquals(
                "something", clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
    }

    @Test
    public void testClipboardCut() {
        final String originalText = "testing something very long";
        final String textToCut = "something";
        final String expectedText = "testing  very long";
        mAnySoftKeyboardUnderTest.simulateTextTyping(originalText);
        mAnySoftKeyboardUnderTest.setSelectedText(
                "testing ".length(), "testing something".length(), true);
        Assert.assertEquals(textToCut, mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Service.CLIPBOARD_SERVICE);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_CUT);

        // text without "something"
        Assert.assertEquals(
                expectedText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        // and clipboard has the copied text
        Assert.assertEquals(1, clipboardManager.getPrimaryClip().getItemCount());
        Assert.assertEquals(
                "something", clipboardManager.getPrimaryClip().getItemAt(0).getText().toString());
    }

    @Test
    public void testClipboardPaste() {
        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Service.CLIPBOARD_SERVICE);
        final String expectedText = "some text";
        clipboardManager.setPrimaryClip(
                new ClipData("ask", new String[] {"text"}, new ClipData.Item(expectedText)));

        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE);
        Assert.assertEquals(
                expectedText, mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        // and backspace DOES NOT deletes the pasted text
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.DELETE);
        Assert.assertEquals(
                expectedText.substring(0, expectedText.length() - 1),
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
    }

    @Test
    public void testClipboardPasteWhenEmptyClipboard() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertEquals(
                mAnySoftKeyboardUnderTest.getText(R.string.clipboard_is_empty_toast),
                ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testSelectionExpending_AtEndOfInput() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("some text in the input connection");

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
        Assert.assertEquals("n", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
        Assert.assertEquals("on", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("on", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
    }

    @Test
    public void testSelectionExpending_AtMiddleOfInput() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("some text in the input connection");
        mAnySoftKeyboardUnderTest.moveCursorToPosition("some ".length(), true);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("t", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("te", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
        Assert.assertEquals(" te", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
    }

    @Test
    public void testSelectionExpendingCancel() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("some text in the input connection");
        mAnySoftKeyboardUnderTest.moveCursorToPosition("some ".length(), true);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("t", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("te", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        mAnySoftKeyboardUnderTest.simulateKeyPress('k');
        // selection ('te') was replaced with the letter 'k'
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        Assert.assertEquals(
                "some kxt in the input connection",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertEquals(
                "some k".length(),
                mAnySoftKeyboardUnderTest
                        .getCurrentTestInputConnection()
                        .getCurrentStartPosition());
        // and we are no longer is select state
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
    }

    @Test
    public void testSelectionExpendingWithAlreadySelectedText() {
        mAnySoftKeyboardUnderTest.simulateTextTyping("some text in the input connection");
        mAnySoftKeyboardUnderTest.setSelectedText("some ".length(), "some text".length(), true);
        // we already have selection set
        Assert.assertEquals("text", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        Assert.assertEquals("text", mAnySoftKeyboardUnderTest.getCurrentSelectedText());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("text ", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_RIGHT);
        Assert.assertEquals("text i", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.ARROW_LEFT);
        Assert.assertEquals(" text i", mAnySoftKeyboardUnderTest.getCurrentSelectedText());
    }

    @Test
    public void testClipboardFineSelectToast() {
        final String expectedText = "testing something very long";
        mAnySoftKeyboardUnderTest.simulateTextTyping(expectedText);
        mAnySoftKeyboardUnderTest.setSelectedText(
                "testing ".length(), "testing something".length(), true);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        Assert.assertEquals(
                getApplicationContext().getString(R.string.clipboard_copy_done_toast),
                ShadowToast.getTextOfLatestToast());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_SELECT);
        final Toast latestToast = ShadowToast.getLatestToast();
        Assert.assertNotNull(latestToast);
        Assert.assertEquals(Toast.LENGTH_SHORT, latestToast.getDuration());
        Assert.assertEquals(
                getApplicationContext().getString(R.string.clipboard_fine_select_enabled_toast),
                ShadowToast.getTextOfLatestToast());

        // and if we try to copy again, we should not see the long-press tip
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        Assert.assertEquals(
                getApplicationContext().getString(R.string.clipboard_copy_done_toast),
                ShadowToast.getTextOfLatestToast());
    }

    @Test
    public void testClipboardShowsOptionsToCopy() {
        final String expectedText = "testing something very long";
        mAnySoftKeyboardUnderTest.simulateTextTyping(expectedText);
        mAnySoftKeyboardUnderTest.setSelectedText(
                "testing ".length(), "testing something very".length(), true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        mAnySoftKeyboardUnderTest.setSelectedText(0, "testing ".length(), true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        // now, we'll do long-press
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        final AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(
                "Pick text to paste", GeneralDialogTestUtil.getTitleFromDialog(latestAlertDialog));
        Assert.assertEquals(2, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "testing ", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals(
                "something very",
                latestAlertDialog.getListView().getAdapter().getItem(1).toString());
    }

    @Test
    public void testDeleteFirstEntry() {
        ClipboardManager shadowManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        final String expectedText = "testing something very long";
        mAnySoftKeyboardUnderTest.simulateTextTyping(expectedText);
        mAnySoftKeyboardUnderTest.setSelectedText(
                "testing ".length(), "testing something very".length(), true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        mAnySoftKeyboardUnderTest.setSelectedText(0, "testing ".length(), true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(2, latestAlertDialog.getListView().getAdapter().getCount());
        latestAlertDialog
                .getListView()
                .getAdapter()
                .getView(0, null, latestAlertDialog.getListView())
                .findViewById(R.id.clipboard_entry_delete)
                .performClick();

        Assert.assertFalse(latestAlertDialog.isShowing());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);
        latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertEquals(1, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "something very",
                latestAlertDialog.getListView().getAdapter().getItem(0).toString());

        latestAlertDialog.dismiss();

        // also, pasting should paste the previous entry
        Assert.assertEquals(
                "testing something very long",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE);
        Assert.assertEquals(
                "something verysomething very long",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        Assert.assertEquals("", shadowManager.getPrimaryClip().getItemAt(0).getText());
    }

    @Test
    @TargetApi(Build.VERSION_CODES.P)
    @Config(sdk = Build.VERSION_CODES.P)
    public void testDeleteFirstEntryForApi28() {
        final String expectedText = "testing something very long";
        mAnySoftKeyboardUnderTest.simulateTextTyping(expectedText);
        mAnySoftKeyboardUnderTest.setSelectedText(
                "testing ".length(), "testing something very".length(), true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        mAnySoftKeyboardUnderTest.setSelectedText(0, "testing ".length(), true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(2, latestAlertDialog.getListView().getAdapter().getCount());
        latestAlertDialog
                .getListView()
                .getAdapter()
                .getView(0, null, latestAlertDialog.getListView())
                .findViewById(R.id.clipboard_entry_delete)
                .performClick();

        Assert.assertFalse(latestAlertDialog.isShowing());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);
        latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertEquals(1, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "something very",
                latestAlertDialog.getListView().getAdapter().getItem(0).toString());

        latestAlertDialog.dismiss();

        // also, pasting should paste nothing (we deleted the primary clip)
        Assert.assertEquals(
                "testing something very long",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE);
        Assert.assertEquals(
                "something verysomething very long",
                mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        // actually deletes the primary clip
        // TODO: I think this is broken with Robolectric 4.3.1
        // Assert.assertFalse(shadowManager.hasPrimaryClip());
    }

    @Test
    public void testDeleteNotFirstEntry() {
        ClipboardManager shadowManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        final String expectedText = "testing something very long";
        mAnySoftKeyboardUnderTest.simulateTextTyping(expectedText);
        mAnySoftKeyboardUnderTest.setSelectedText(
                "testing ".length(), "testing something very".length(), true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);
        mAnySoftKeyboardUnderTest.setSelectedText(0, "testing ".length(), true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(2, latestAlertDialog.getListView().getAdapter().getCount());
        latestAlertDialog
                .getListView()
                .getAdapter()
                .getView(1, null, latestAlertDialog.getListView())
                .findViewById(R.id.clipboard_entry_delete)
                .performClick();

        Assert.assertFalse(latestAlertDialog.isShowing());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);
        latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertEquals(1, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "testing ", latestAlertDialog.getListView().getAdapter().getItem(0).toString());

        Assert.assertEquals(
                "testing ", shadowManager.getPrimaryClip().getItemAt(0).getText().toString());
    }

    @Test
    public void testClipboardShowsOptionsWhenPrimaryClipChanged() {
        ClipboardManager shadowManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        shadowManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));
        shadowManager.setPrimaryClip(
                new ClipData("text 2", new String[0], new ClipData.Item("text 2")));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(2, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "text 2", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals(
                "text 1", latestAlertDialog.getListView().getAdapter().getItem(1).toString());

        latestAlertDialog.cancel();

        mAnySoftKeyboardUnderTest.simulateTextTyping("text 3");
        mAnySoftKeyboardUnderTest.setSelectedText(1, 4, true);
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_COPY);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(3, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "ext", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals(
                "text 2", latestAlertDialog.getListView().getAdapter().getItem(1).toString());
        Assert.assertEquals(
                "text 1", latestAlertDialog.getListView().getAdapter().getItem(2).toString());

        latestAlertDialog.cancel();

        for (int clipIndex = 0; clipIndex < 100; clipIndex++) {
            shadowManager.setPrimaryClip(
                    new ClipData(
                            "text " + clipIndex,
                            new String[0],
                            new ClipData.Item("text " + clipIndex)));
        }

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(15, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "text 99", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
        Assert.assertEquals(
                "text 98", latestAlertDialog.getListView().getAdapter().getItem(1).toString());
        Assert.assertEquals(
                "text 97", latestAlertDialog.getListView().getAdapter().getItem(2).toString());
        Assert.assertEquals(
                "text 96", latestAlertDialog.getListView().getAdapter().getItem(3).toString());
    }

    @Test
    public void testClipboardDoesNotShowsOptionsWhenPrimaryClipChangedAndSyncIsDisabled() {
        ClipboardManager shadowManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        shadowManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_os_clipboard_sync, false);

        shadowManager.setPrimaryClip(
                new ClipData("text 2", new String[0], new ClipData.Item("text 2")));

        Assert.assertNull(ShadowToast.getLatestToast());
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        Assert.assertNotNull(ShadowToast.getLatestToast());
        ShadowToast.reset();

        SharedPrefsHelper.setPrefsValue(R.string.settings_key_os_clipboard_sync, true);

        shadowManager.setPrimaryClip(
                new ClipData("text 3", new String[0], new ClipData.Item("text 3")));

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.CLIPBOARD_PASTE_POPUP);

        Assert.assertNull(ShadowToast.getLatestToast());
        AlertDialog latestAlertDialog = GeneralDialogTestUtil.getLatestShownDialog();
        Assert.assertNotNull(latestAlertDialog);
        Assert.assertEquals(1, latestAlertDialog.getListView().getAdapter().getCount());
        Assert.assertEquals(
                "text 3", latestAlertDialog.getListView().getAdapter().getItem(0).toString());
    }

    @Test
    public void testUndo() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.UNDO);
        ArgumentCaptor<KeyEvent> keyEventArgumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getCurrentTestInputConnection(), Mockito.times(2))
                .sendKeyEvent(keyEventArgumentCaptor.capture());

        Assert.assertEquals(
                KeyEvent.ACTION_DOWN, keyEventArgumentCaptor.getAllValues().get(0).getAction());
        Assert.assertEquals(
                KeyEvent.META_CTRL_ON, keyEventArgumentCaptor.getAllValues().get(0).getMetaState());
        Assert.assertEquals(
                KeyEvent.KEYCODE_Z, keyEventArgumentCaptor.getAllValues().get(0).getKeyCode());

        Assert.assertEquals(
                KeyEvent.ACTION_UP, keyEventArgumentCaptor.getAllValues().get(1).getAction());
        Assert.assertEquals(
                KeyEvent.META_CTRL_ON, keyEventArgumentCaptor.getAllValues().get(1).getMetaState());
        Assert.assertEquals(
                KeyEvent.KEYCODE_Z, keyEventArgumentCaptor.getAllValues().get(1).getKeyCode());
    }

    @Test
    public void testRedo() {
        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.REDO);
        ArgumentCaptor<KeyEvent> keyEventArgumentCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        Mockito.verify(mAnySoftKeyboardUnderTest.getCurrentTestInputConnection(), Mockito.times(2))
                .sendKeyEvent(keyEventArgumentCaptor.capture());

        Assert.assertEquals(
                KeyEvent.ACTION_DOWN, keyEventArgumentCaptor.getAllValues().get(0).getAction());
        Assert.assertEquals(
                KeyEvent.META_CTRL_ON | KeyEvent.META_SHIFT_ON,
                keyEventArgumentCaptor.getAllValues().get(0).getMetaState());
        Assert.assertEquals(
                KeyEvent.KEYCODE_Z, keyEventArgumentCaptor.getAllValues().get(0).getKeyCode());

        Assert.assertEquals(
                KeyEvent.ACTION_UP, keyEventArgumentCaptor.getAllValues().get(1).getAction());
        Assert.assertEquals(
                KeyEvent.META_CTRL_ON | KeyEvent.META_SHIFT_ON,
                keyEventArgumentCaptor.getAllValues().get(1).getMetaState());
        Assert.assertEquals(
                KeyEvent.KEYCODE_Z, keyEventArgumentCaptor.getAllValues().get(1).getKeyCode());
    }

    @Test
    public void testBasicStripActionIfClipboard() {
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getClipboardActionOwnerImpl());
        Assert.assertSame(
                mAnySoftKeyboardUnderTest.getClipboardActionOwnerImpl().getContext(),
                mAnySoftKeyboardUnderTest);
        Assert.assertNotNull(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider());

        final View rootStripView =
                mAnySoftKeyboardUnderTest
                        .getClipboardStripActionProvider()
                        .inflateActionView(new LinearLayout(mAnySoftKeyboardUnderTest));
        Assert.assertNotNull(rootStripView);
        Assert.assertNotNull(rootStripView.findViewById(R.id.clipboard_suggestion_text));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
        mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().onRemoved();
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
    }

    @Test
    public void testDoesNotShowStripActionIfClipboardIsEmpty() {
        simulateFinishInputFlow();
        simulateOnStartInputFlow();
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
    }

    @Test
    public void testShowStripActionIfClipboardIsNotEmptyHappyPath() {
        simulateFinishInputFlow();
        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));

        simulateOnStartInputFlow();
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
        Assert.assertEquals("", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());

        final TextView clipboardView =
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text);
        Assert.assertNotNull(clipboardView);
        Assert.assertEquals("text 1", clipboardView.getText().toString());
        ((View) clipboardView.getParent()).performClick();
        Assert.assertEquals("text 1", mAnySoftKeyboardUnderTest.getCurrentInputConnectionText());
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text));
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
        Assert.assertFalse(
                mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isFullyVisible());
    }

    @Test
    public void testHideActionIfKeyPressedButLeavesHintForDuration() {
        simulateFinishInputFlow();
        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));

        simulateOnStartInputFlow();
        Assert.assertTrue(
                mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isFullyVisible());
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text));
        mAnySoftKeyboardUnderTest.simulateKeyPress('a');
        Assert.assertFalse(
                mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isFullyVisible());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
        ShadowSystemClock.advanceBy(Duration.of(2, ChronoUnit.MINUTES));
        mAnySoftKeyboardUnderTest.simulateKeyPress('a');
        Assert.assertFalse(
                mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isFullyVisible());
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text));
    }

    @Test
    public void testShowStripActionAsPasswordIfClipboardIsNotEmptyInPasswordField() {
        simulateFinishInputFlow();
        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));

        int[] variations =
                new int[] {
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                };

        for (int variation : variations) {
            simulateOnStartInputFlow(
                    false,
                    createEditorInfo(
                            EditorInfo.IME_ACTION_NONE, InputType.TYPE_CLASS_TEXT | variation));

            final TextView clipboardView =
                    mAnySoftKeyboardUnderTest
                            .getInputViewContainer()
                            .findViewById(R.id.clipboard_suggestion_text);
            Assert.assertNotNull("for " + variation, clipboardView);
            Assert.assertEquals(
                    "for " + variation, "**********", clipboardView.getText().toString());

            simulateFinishInputFlow();
        }
    }

    @Test
    public void testShowStripActionAsPasswordIfClipboardWasOriginatedInPassword() {
        simulateFinishInputFlow();

        simulateOnStartInputFlow(
                false,
                createEditorInfo(
                        EditorInfo.IME_ACTION_NONE,
                        InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD));

        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));

        simulateFinishInputFlow();
        simulateOnStartInputFlow();

        final TextView clipboardView =
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text);
        Assert.assertNotNull(clipboardView);
        Assert.assertEquals("**********", clipboardView.getText().toString());

        simulateFinishInputFlow();
    }

    @Test
    public void testShowStripActionAsNonPasswordIfClipboardIsNotEmptyInNonPasswordField() {
        simulateFinishInputFlow();
        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));

        int[] variations =
                new int[] {
                    InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT,
                    InputType.TYPE_TEXT_VARIATION_FILTER,
                    InputType.TYPE_TEXT_VARIATION_PHONETIC,
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_PERSON_NAME,
                    InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE,
                    InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE,
                    InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT,
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_URI,
                    InputType.TYPE_TEXT_VARIATION_NORMAL,
                };

        for (int variation : variations) {
            simulateOnStartInputFlow(
                    false,
                    createEditorInfo(
                            EditorInfo.IME_ACTION_NONE, InputType.TYPE_CLASS_TEXT | variation));

            final TextView clipboardView =
                    mAnySoftKeyboardUnderTest
                            .getInputViewContainer()
                            .findViewById(R.id.clipboard_suggestion_text);
            Assert.assertNotNull("for " + variation, clipboardView);
            Assert.assertEquals("for " + variation, "text 1", clipboardView.getText().toString());

            simulateFinishInputFlow();
        }
    }

    @Test
    public void testDoesNotShowStripActionIfClipboardEntryIsOld() {
        simulateFinishInputFlow();
        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));
        ShadowSystemClock.advanceBy(Duration.of(121, ChronoUnit.SECONDS));
        simulateOnStartInputFlow();
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text));
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
    }

    @Test
    public void testShowHintStripActionIfClipboardEntryIsKindaOld() {
        simulateFinishInputFlow();
        ClipboardManager clipboardManager =
                (ClipboardManager)
                        getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(
                new ClipData("text 1", new String[0], new ClipData.Item("text 1")));
        ShadowSystemClock.advanceBy(Duration.of(16, ChronoUnit.SECONDS));
        simulateOnStartInputFlow();
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text));
        Assert.assertFalse(
                mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isFullyVisible());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
        ShadowSystemClock.advanceBy(Duration.of(120, ChronoUnit.SECONDS));
        Assert.assertFalse(
                mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isFullyVisible());
        Assert.assertTrue(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
        Assert.assertNotNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text));
        mAnySoftKeyboardUnderTest.simulateKeyPress('a');
        Assert.assertFalse(mAnySoftKeyboardUnderTest.getClipboardStripActionProvider().isVisible());
        Assert.assertNull(
                mAnySoftKeyboardUnderTest
                        .getInputViewContainer()
                        .findViewById(R.id.clipboard_suggestion_text));
    }
}
