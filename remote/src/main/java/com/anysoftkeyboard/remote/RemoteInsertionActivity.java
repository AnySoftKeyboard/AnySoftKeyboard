package com.anysoftkeyboard.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

public class RemoteInsertionActivity extends FragmentActivity {

    @NonNull
    static Intent createPickingActivityIntent(@NonNull String[] mimeTypes, int requestId, Context context) {
        final Intent intent = new Intent(context, RemoteInsertionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_REQUEST_ID, requestId);
        intent.putExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY, mimeTypes);
        return intent;
    }

    private static final int PICK_IMAGE_ACTIVITY_REQUEST = 1;
    private int mRequestId;
    private String[] mRequestMimeTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mRequestId = getIntent().getExtras().getInt(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_REQUEST_ID);
            mRequestMimeTypes = getIntent().getExtras().getStringArray(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY);

            doPickIntent();
        } else {
            //restart activity, we do not do another request
            mRequestId = savedInstanceState.getInt(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_REQUEST_ID);
            mRequestMimeTypes = savedInstanceState.getStringArray(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_URI_BUNDLE_KEY, mRequestId);
        outState.putStringArray(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY, mRequestMimeTypes);
    }

    private void doPickIntent() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        //pickIntent.putExtra("crop", "true");
        //pickIntent.putExtra("scale", true);

        Intent chooserIntent = Intent.createChooser(pickIntent, getText(R.string.media_pick_chooser_title));

        startActivityForResult(chooserIntent, PICK_IMAGE_ACTIVITY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Intent intent = new Intent(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_INSERTION_AVAILABLE);
        intent.putExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_REQUEST_ID, mRequestId);

        if (requestCode == PICK_IMAGE_ACTIVITY_REQUEST && resultCode == FragmentActivity.RESULT_OK) {
            intent.putExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_URI_BUNDLE_KEY, data.getData());
            intent.putExtra(RemoteInsertionImpl.MediaInsertionAvailableReceiver.MEDIA_MIMES_BUNDLE_KEY, mRequestMimeTypes);
        }

        sendBroadcast(intent);
        finish();
    }
}
