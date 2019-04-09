package com.anysoftkeyboard.ime;

import android.content.ClipDescription;
import android.net.Uri;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.view.inputmethod.EditorInfo;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.remote.InsertionRequestCallback;
import com.anysoftkeyboard.remote.MediaType;
import com.anysoftkeyboard.remote.RemoteInsertion;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AnySoftKeyboardMediaInsertionTest extends AnySoftKeyboardBaseTest {

    private AnySoftKeyboardMediaInsertion mPackageScope;
    private RemoteInsertion mRemoteInsertion;

    @Before
    public void setup() {
        mPackageScope = mAnySoftKeyboardUnderTest;
        //it says 'createRemoteInsertion', but it actually returns a mock
        mRemoteInsertion = mPackageScope.createRemoteInsertion();
        Assert.assertTrue(Mockito.mockingDetails(mRemoteInsertion).isMock());
    }

    @Test
    public void testReportsMediaTypesAndClearsOnFinish() {
        simulateFinishInputFlow();
        EditorInfo info = createEditorInfoTextWithSuggestionsForSetUp();

        simulateOnStartInputFlow(false, info);
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().isEmpty());
        simulateFinishInputFlow();
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().isEmpty());


        EditorInfoCompat.setContentMimeTypes(info, new String[]{"image/jpg"});
        simulateOnStartInputFlow(false, info);
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().contains(MediaType.Image));
        Assert.assertFalse(mPackageScope.getSupportedMediaTypesForInput().contains(MediaType.Gif));
        simulateFinishInputFlow();
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().isEmpty());


        EditorInfoCompat.setContentMimeTypes(info, new String[]{"image/gif"});
        simulateOnStartInputFlow(false, info);
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().contains(MediaType.Image));
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().contains(MediaType.Gif));
        simulateFinishInputFlow();
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().isEmpty());

        EditorInfoCompat.setContentMimeTypes(info, new String[]{"image/menny_image"});
        simulateOnStartInputFlow(false, info);
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().contains(MediaType.Image));
        Assert.assertFalse(mPackageScope.getSupportedMediaTypesForInput().contains(MediaType.Gif));
        simulateFinishInputFlow();
        Assert.assertTrue(mPackageScope.getSupportedMediaTypesForInput().isEmpty());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testMediaTypesIsUnmodifiable() {
        mPackageScope.getSupportedMediaTypesForInput().add(MediaType.Image);
    }

    @Test
    public void testCallsRemoteInsertionWithCorrectArguments() {
        simulateFinishInputFlow();
        EditorInfo info = createEditorInfoTextWithSuggestionsForSetUp();
        EditorInfoCompat.setContentMimeTypes(info, new String[]{"image/gif"});
        simulateOnStartInputFlow(false, info);

        Mockito.verify(mRemoteInsertion, Mockito.never()).startMediaRequest(Mockito.anySet(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any());

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.IMAGE_MEDIA_POPUP);

        Mockito.verify(mRemoteInsertion).startMediaRequest(
                Mockito.same(mPackageScope.getSupportedMediaTypesForInput()),
                Mockito.same(mPackageScope.getCurrentInputConnection()),
                Mockito.same(mPackageScope.getCurrentInputEditorInfo()),
                Mockito.eq(AnySoftKeyboardMediaInsertion.getIdForInsertionRequest(info)),
                Mockito.any());
    }

    @Test
    public void testDoesNotCommitIfInputFieldIsDifferent() {
        simulateFinishInputFlow();
        EditorInfo info = createEditorInfoTextWithSuggestionsForSetUp();
        EditorInfoCompat.setContentMimeTypes(info, new String[]{"image/gif"});
        simulateOnStartInputFlow(false, info);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.IMAGE_MEDIA_POPUP);

        ArgumentCaptor<InsertionRequestCallback> argumentCaptor = ArgumentCaptor.forClass(InsertionRequestCallback.class);
        Mockito.verify(mRemoteInsertion).startMediaRequest(
                Mockito.anySet(),
                Mockito.any(),
                Mockito.any(),
                Mockito.anyInt(),
                argumentCaptor.capture());


        argumentCaptor.getValue().onMediaRequestDone(1 + AnySoftKeyboardMediaInsertion.getIdForInsertionRequest(info),
                new InputContentInfoCompat(Uri.EMPTY, new ClipDescription("", EditorInfoCompat.getContentMimeTypes(info)), null));

        Assert.assertNull(mAnySoftKeyboardUnderTest.getTestInputConnection().getInputContentInfo());
    }

    @Test
    public void testCommitsIfInputFieldIsSame() {
        simulateFinishInputFlow();
        EditorInfo info = createEditorInfoTextWithSuggestionsForSetUp();
        EditorInfoCompat.setContentMimeTypes(info, new String[]{"image/gif"});
        simulateOnStartInputFlow(false, info);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.IMAGE_MEDIA_POPUP);

        ArgumentCaptor<InsertionRequestCallback> argumentCaptor = ArgumentCaptor.forClass(InsertionRequestCallback.class);
        Mockito.verify(mRemoteInsertion).startMediaRequest(
                Mockito.anySet(),
                Mockito.any(),
                Mockito.any(),
                Mockito.anyInt(),
                argumentCaptor.capture());


        argumentCaptor.getValue().onMediaRequestDone(AnySoftKeyboardMediaInsertion.getIdForInsertionRequest(info),
                new InputContentInfoCompat(Uri.EMPTY, new ClipDescription("", EditorInfoCompat.getContentMimeTypes(info)), null));

        Assert.assertNull(mAnySoftKeyboardUnderTest.getTestInputConnection().getInputContentInfo());
    }

    @Test
    public void testCommitsIfRequestCancelled() {
        simulateFinishInputFlow();
        EditorInfo info = createEditorInfoTextWithSuggestionsForSetUp();
        EditorInfoCompat.setContentMimeTypes(info, new String[]{"image/gif"});
        simulateOnStartInputFlow(false, info);

        mAnySoftKeyboardUnderTest.simulateKeyPress(KeyCodes.IMAGE_MEDIA_POPUP);

        ArgumentCaptor<InsertionRequestCallback> argumentCaptor = ArgumentCaptor.forClass(InsertionRequestCallback.class);
        Mockito.verify(mRemoteInsertion).startMediaRequest(
                Mockito.anySet(),
                Mockito.any(),
                Mockito.any(),
                Mockito.anyInt(),
                argumentCaptor.capture());

        argumentCaptor.getValue().onMediaRequestCancelled(AnySoftKeyboardMediaInsertion.getIdForInsertionRequest(info));

        Assert.assertNull(mAnySoftKeyboardUnderTest.getTestInputConnection().getInputContentInfo());
    }
}