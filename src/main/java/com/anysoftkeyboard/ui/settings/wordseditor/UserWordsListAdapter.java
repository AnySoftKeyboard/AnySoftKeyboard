package com.anysoftkeyboard.ui.settings.wordseditor;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.R;

import java.util.List;

/**
 * List adapter to be used with the words editor fragment.
 */
class UserWordsListAdapter extends ArrayAdapter<String> {

    public static interface AdapterCallbacks {
        void onWordDeleted(String word);

        void onWordUpdated(String oldWord, String newWord);

        void performDiscardEdit();
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
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        switch(getItemViewType(position)) {
            case TYPE_EDIT:
                return 1;
            case TYPE_ADD:
                return 2;
            default:
                final String word = getItem(position);
                return word.hashCode();
        }
    }

    @Override
    public int getCount() {
        final int baseCount = super.getCount();
        if (baseCount == 0 && mCurrentlyEditPosition == NONE_POSITION)
            return 0;//in the case that there are no words (and not editing the first word), I have a special "empty state"

        return super.getCount() + 1;//the plus one is for the "Add new";
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
            switch (viewType) {
                case TYPE_NORMAL:
                    convertView = mInflater.inflate(R.layout.user_dictionary_word_row, parent, false);
                    convertView.findViewById(R.id.delete_user_word).setOnClickListener(mOnDeleteWordClickListener);
                    break;
                case TYPE_EDIT:
                    convertView = mInflater.inflate(R.layout.user_dictionary_word_row_edit, parent, false);
                    convertView.findViewById(R.id.approve_user_word).setOnClickListener(mOnWordEditApprovedClickListener);
                    EditText editBox = ((EditText)convertView.findViewById(R.id.word_view));
                    editBox.setOnKeyListener(mOnEditBoxKeyPressedListener);
                    editBox.addTextChangedListener(mOnEditBoxTextChangedListener);
                    editBox.setOnEditorActionListener(mEditBoxActionListener);
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
            ((TextView) convertView.findViewById(R.id.word_view)).setText(word);
            convertView.setTag(word);
        }
        if (viewType == TYPE_EDIT) {
            //I want the text-box to take the focus now.
            final View edit = convertView.findViewById(R.id.word_view);
            edit.requestFocus();
        }
        return convertView;
    }

    public void onItemClicked(AdapterView<?> listView, int position) {
        if (mCurrentlyEditPosition == NONE_POSITION && position >= 0) {
            //nothing was in edit mode, so we start a new one
            mCurrentlyEditPosition = position;
            //see http://stackoverflow.com/a/2680077/1324235
            listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        } else {
            //there was an edit in progress. Clicking out side will cause DISCARD.
            mCurrentlyEditPosition = NONE_POSITION;
            //see http://stackoverflow.com/a/2680077/1324235
            listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            listView.requestFocus();
        }
        notifyDataSetChanged();
    }

    private final View.OnClickListener mOnDeleteWordClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final String word = ((View) v.getParent()).getTag().toString();
            mCallbacksListener.onWordDeleted(word);
        }
    };

    private final View.OnClickListener mOnWordEditApprovedClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            View parent = ((View) v.getParent());
            final String oldWord = parent.getTag().toString();
            EditText editBox = (EditText) parent.findViewById(R.id.word_view);
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

    private final View.OnKeyListener mOnEditBoxKeyPressedListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    //discarded!
                    mCallbacksListener.performDiscardEdit();
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                    //v is the editbox. Need to pass the APPROVE view
                    View parent = (View)v.getParent();
                    View approveButton = parent.findViewById(R.id.approve_user_word);
                    mOnWordEditApprovedClickListener.onClick(approveButton);
                    return true;
                default:
                    return false;
            }
        }
    };

    private final TextView.OnEditorActionListener mEditBoxActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (v.getId() != R.id.word_view) return false;
            View parent = (View)v.getParent();
            View approveButton = parent.findViewById(R.id.approve_user_word);
            mOnWordEditApprovedClickListener.onClick(approveButton);
            return true;
        }
    };

    private final TextWatcher mOnEditBoxTextChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

}
