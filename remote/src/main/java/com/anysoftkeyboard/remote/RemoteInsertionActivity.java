package com.anysoftkeyboard.remote;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;

import com.anysoftkeyboard.api.MediaInsertion;

public class RemoteInsertionActivity extends FragmentActivity {

    private static final int PICK_IMAGE_ACTIVITY_REQUEST = 1;
    private int mRequestId;
    private String[] mRequestMimeTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            final Bundle extras = getIntent().getExtras();
            if (extras == null) {
                throw new IllegalArgumentException("RemoteInsertionActivity was started without any extras!");
            }
            mRequestId = extras.getInt(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_REQUEST_ID_KEY);
            mRequestMimeTypes = extras.getStringArray(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_MIMES_KEY);

            doPickIntent();
        } else {
            //restart activity, we do not do another request
            mRequestId = savedInstanceState.getInt(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_REQUEST_ID_KEY);
            mRequestMimeTypes = savedInstanceState.getStringArray(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_MIMES_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_REQUEST_ID_KEY, mRequestId);
        outState.putStringArray(MediaInsertion.INTENT_MEDIA_INSERTION_REQUEST_MEDIA_MIMES_KEY, mRequestMimeTypes);
    }

    private void doPickIntent() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        Intent chooserIntent = Intent.createChooser(pickIntent, getText(R.string.media_pick_chooser_title));

        startActivityForResult(chooserIntent, PICK_IMAGE_ACTIVITY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final Intent intent = new Intent(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_AVAILABLE_ACTION);
        intent.putExtra(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_REQUEST_ID_KEY, mRequestId);
        intent.putExtra(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_MEDIA_MIMES_KEY, mRequestMimeTypes);

        if (requestCode == PICK_IMAGE_ACTIVITY_REQUEST && resultCode == FragmentActivity.RESULT_OK) {
            intent.putExtra(MediaInsertion.BROADCAST_INTENT_MEDIA_INSERTION_MEDIA_URI_KEY, data.getData());
        }

        sendBroadcast(intent);
        finish();
    }
}
