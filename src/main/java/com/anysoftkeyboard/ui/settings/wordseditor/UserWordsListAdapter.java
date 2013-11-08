package com.anysoftkeyboard.ui.settings.wordseditor;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

/**
* Created by menny on 11/7/13.
*/
class UserWordsListAdapter extends ArrayAdapter<String> {

    private static final String TAG = "UserWordsListAdapter";

    public static interface AdapterCallbacks {
        void onWordDeleted(String word);
        void onWordUpdated(String oldWord, String newWord);
    }

    private final LayoutInflater mInflater;
    private AdapterCallbacks mCallbacksListener;

    private final int NONE_POSITION = -1;
    private int mCurrentlyEditPosition = NONE_POSITION;

    private final int TYPE_NORMAL = 0;
    private final int TYPE_EDIT = 1;
    private final int TYPE_ADD = 2;

    public UserWordsListAdapter(Context context, List<String> words, AdapterCallbacks callbacks) {
        super(context, R.id.word_view, words);
        mCallbacksListener = callbacks;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        //one for normal, and the second type is "editing"
        //it will inflate the same layout on both occasions, but will allow use to stop re-creation and re-use of the EDIT view.
        //the third one is for the "add new word"
        return 3;
    }

    @Override
    public int getCount() {
        final int baseCount = super.getCount();
        if (baseCount == 0)
            return 0;//in the case that there are no words, I have a special "empty state"

        return super.getCount()+1;//the plus one is for the "Add new";
    }

    @Override
    public int getItemViewType(int position) {
        if (mCurrentlyEditPosition == position)
            return TYPE_EDIT;
        else if (position == super.getCount())//this is the last item, which is an "Add word" item.
            return TYPE_ADD;
        else
            return TYPE_NORMAL;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int viewType = getItemViewType(position);
        if (convertView == null) {
            Log.d(TAG, "Creating a new view of type "+viewType+" at position "+position);
            switch (viewType) {
                case TYPE_NORMAL:
                    convertView = mInflater.inflate(R.layout.user_dictionary_word_row, parent, false);
                    convertView.findViewById(R.id.delete_user_word).setOnClickListener(mOnDeleteWordClickListener);
                    break;
                case TYPE_EDIT:
                    convertView = mInflater.inflate(R.layout.user_dictionary_word_row_edit, parent, false);
                    convertView.findViewById(R.id.approve_user_word).setOnClickListener(mOnWordEditApprovedClickListener);
                    break;
                case TYPE_ADD:
                    convertView = mInflater.inflate(R.layout.user_dictionary_word_row_add, parent, false);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown view type!");
            }
        }

        //why to check the position against the super.getCount, and not the view type?
        //good question! In the state where we adding a new word, the underling array is still one short,
        //so the view type will be "EDIT", but the count will still be one less.
        if (position == super.getCount()) {
            convertView.setTag(""/*empty word*/);
        } else {
            final String word = getItem(position);
            Log.d(TAG, "Updating word for view of type "+viewType+" at position "+position+". Word: "+word);
            ((TextView)convertView.findViewById(R.id.word_view)).setText(word);
            convertView.setTag(word);
        }
        return convertView;
    }

    public void onItemClicked(int position) {
        if (mCurrentlyEditPosition == NONE_POSITION) {
            //nothing was in edit mode, so we start a new one
            mCurrentlyEditPosition = position;
        } else {
            //there was an edit in progress. Clicking out side will cause DISCARD.
            mCurrentlyEditPosition = NONE_POSITION;
        }
        notifyDataSetChanged();
    }

    private final View.OnClickListener mOnDeleteWordClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final String word = ((View)v.getParent()).getTag().toString();
            mCallbacksListener.onWordDeleted(word);
        }
    };

    private final View.OnClickListener mOnWordEditApprovedClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            View parent = ((View)v.getParent());
            final String oldWord = parent.getTag().toString();
            EditText editBox = (EditText)parent.findViewById(R.id.word_view);
            final String newWord = editBox.getText().toString();
            mCurrentlyEditPosition = NONE_POSITION;
            if (TextUtils.isEmpty(newWord)) {
                //this is weird.. The user wanted the word to be deleted?
                //why not clicking on the delete icon?!
                //I'm ignoring.
                notifyDataSetChanged();//reloading the list.
            } else {
                mCallbacksListener.onWordUpdated(oldWord, newWord);
            }
        }
    };
}
