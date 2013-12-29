package com.anysoftkeyboard.ui.settings.wordseditor;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.R;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * List adapter to be used with the words editor fragment.
 */
class UserWordsListAdapter extends ArrayAdapter<UserWordsListAdapter.Word> implements View.OnClickListener {

    public static class Word {
        @Nonnull
        public final String word;
        public final int frequency;

        public Word(@Nonnull String word, int frequency) {
            this.word = word;
            this.frequency = frequency;
        }

        @Override
        public int hashCode() {
            return word.hashCode() + frequency;
        }

        @Override
        public String toString() {
            return word;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Word) {
                Word otherWord = (Word) o;
                return otherWord.frequency == frequency && otherWord.word.equals(word);
            } else {
                return false;
            }
        }
    }

    public static interface AdapterCallbacks {
        void onWordDeleted(Word word);

        void onWordUpdated(String oldWord, Word newWord);

        void performDiscardEdit();
    }

    private final LayoutInflater mInflater;
    private AdapterCallbacks mCallbacksListener;

    private final int NONE_POSITION = -1;
    private int mCurrentlyEditPosition = NONE_POSITION;

    private final int TYPE_NORMAL = 0;
    private final int TYPE_EDIT = 1;
    private final int TYPE_ADD = 2;

    public UserWordsListAdapter(Context context, List<Word> words, AdapterCallbacks callbacks) {
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
        switch (getItemViewType(position)) {
            case TYPE_EDIT:
                return 1;
            case TYPE_ADD:
                return 2;
            default:
                final Word word = getItem(position);
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
        TextView wordView;
        if (convertView == null) {
            switch (viewType) {
                case TYPE_NORMAL:
                    convertView = inflateNormalWordRow(mInflater, parent);
                    assert convertView != null;
                    final View deleteButton = convertView.findViewById(R.id.delete_user_word);
                    deleteButton.setOnClickListener(this);
                    break;
                case TYPE_EDIT:
                    convertView = inflateEditedWordRow(mInflater, parent);
                    assert convertView != null;
                    final View approveButton = convertView.findViewById(R.id.approve_user_word);
                    approveButton.setOnClickListener(this);
                    wordView = ((TextView) convertView.findViewById(R.id.word_view));
                    wordView.setOnKeyListener(mOnEditBoxKeyPressedListener);
                    wordView.addTextChangedListener(mOnEditBoxTextChangedListener);
                    wordView.setOnEditorActionListener(mEditBoxActionListener);
                    break;
                case TYPE_ADD:
                    convertView = inflateAddWordRow(mInflater, parent);
                    assert convertView != null;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown view type!");
            }
        }

        wordView = ((TextView) convertView.findViewById(R.id.word_view));
        final Word word;
        //why to check the position against the super.getCount, and not the view type?
        //good question! In the state where we adding a new word, the underling array is still one short,
        //so the view type will be "EDIT", but the count will still be one less.
        if (position == super.getCount()) {
            word = null;/*empty word at the "add new word" row*/
        } else {
            word = getItem(position);
        }
        convertView.setTag(word);

        switch (viewType) {
            case TYPE_NORMAL:
                updateNormalWordRow(convertView, wordView, word);
                break;
            case TYPE_EDIT:
                updateEditedWordRow(convertView, wordView, word);
                //I want the text-box to take the focus now.
                wordView.requestFocus();
                break;
        }
        return convertView;
    }

    protected void updateEditedWordRow(View rootView, TextView wordView, Word word) {
        wordView.setText(word == null? "" : word.word);
    }

    protected void updateNormalWordRow(View rootView, TextView wordView, Word word) {
        wordView.setText(word.word);
    }

    protected View inflateAddWordRow(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.user_dictionary_word_row_add, parent, false);
    }

    protected View inflateEditedWordRow(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.user_dictionary_word_row_edit, parent, false);
    }

    protected View inflateNormalWordRow(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.user_dictionary_word_row, parent, false);
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


    @Override
    public final void onClick(View v) {
        @Nullable final Word word = (Word) ((View) v.getParent()).getTag();
        switch (v.getId()) {
            case R.id.delete_user_word:
                onWordDeleted(word);
                break;
            case R.id.approve_user_word:
                Word newWord = onWordEditApproved(v, word);
                mCurrentlyEditPosition = NONE_POSITION;
                if (newWord == null || TextUtils.isEmpty(newWord.word) || newWord.frequency == 0) {
                    //this is weird.. The user wanted the word to be deleted?
                    //why not clicking on the delete icon?!
                    //I'm ignoring.
                    notifyDataSetChanged();//reloading the list.
                } else {
                    mCallbacksListener.onWordUpdated(word == null? "" : word.word, newWord);
                }
                break;
        }
    }

    protected void onWordDeleted(Word word) {
        mCallbacksListener.onWordDeleted(word);
    }

    protected Word onWordEditApproved(View approveButton, @Nullable Word oldWord) {
        View parent = ((View) approveButton.getParent());
        EditText editBox = (EditText) parent.findViewById(R.id.word_view);
        final String newWord = editBox.getText().toString();
        if (TextUtils.isEmpty(newWord)) {
            return null;
        } else {
            return new Word(newWord, oldWord == null? 128 : oldWord.frequency);
        }
    }

    protected final View.OnKeyListener mOnEditBoxKeyPressedListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    //discarded!
                    mCallbacksListener.performDiscardEdit();
                    return true;
                case KeyEvent.KEYCODE_ENTER:
                    EditText edit = (EditText)v;
                    if ((edit.getImeOptions() & EditorInfo.IME_ACTION_DONE) == EditorInfo.IME_ACTION_DONE) {
                        View parent = (View) v.getParent();
                        View approveButton = parent.findViewById(R.id.approve_user_word);
                        onClick(approveButton);
                    } else if ((edit.getImeOptions() & EditorInfo.IME_ACTION_NEXT) == EditorInfo.IME_ACTION_NEXT) {
                        View nextField = edit.focusSearch(View.FOCUS_RIGHT);
                        if (nextField != null)
                            nextField.requestFocus();
                    }
                    return true;
                default:
                    return false;
            }
        }
    };

    protected final TextView.OnEditorActionListener mEditBoxActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            View parent = (View) v.getParent();
            View approveButton = parent.findViewById(R.id.approve_user_word);
            onClick(approveButton);
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
