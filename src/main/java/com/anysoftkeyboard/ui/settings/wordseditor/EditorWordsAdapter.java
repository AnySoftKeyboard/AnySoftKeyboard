package com.anysoftkeyboard.ui.settings.wordseditor;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public class EditorWordsAdapter extends RecyclerView.Adapter<EditorWordsAdapter.EditorWordViewHolder> {

    protected final List<EditorWord> mEditorWords;
    private final LayoutInflater mLayoutInflater;
    private final DictionaryCallbacks mDictionaryCallbacks;
    public EditorWordsAdapter(List<EditorWord> editorWords, LayoutInflater layoutInflater, DictionaryCallbacks dictionaryCallbacks) {
        mEditorWords = new ArrayList<>(editorWords);
        mEditorWords.add(new EditorWord.AddNew());
        mLayoutInflater = layoutInflater;
        mDictionaryCallbacks = dictionaryCallbacks;
    }

    @Override
    public int getItemViewType(int position) {
        EditorWord editorWord = mEditorWords.get(position);
        if (editorWord instanceof EditorWord.Editing) {
            return R.id.word_editor_view_type_editing_row;
        } else if (editorWord instanceof EditorWord.AddNew) {
            if (position == 0) {
                return R.id.word_editor_view_type_empty_view_row;
            } else {
                return R.id.word_editor_view_type_add_new_row;
            }
        } else {
            return R.id.word_editor_view_type_row;
        }
    }

    @Override
    public EditorWordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.id.word_editor_view_type_editing_row:
                return new EditorWordViewHolderEditing(inflateEditingRowView(mLayoutInflater, parent));
            case R.id.word_editor_view_type_empty_view_row:
                return new EditorWordViewHolderAddNew(mLayoutInflater.inflate(R.layout.word_editor_empty_view, parent, false));
            case R.id.word_editor_view_type_add_new_row:
                return new EditorWordViewHolderAddNew(mLayoutInflater.inflate(R.layout.user_dictionary_word_row_add, parent, false));
            default:
                return new EditorWordViewHolderNormal(mLayoutInflater.inflate(R.layout.user_dictionary_word_row, parent, false));
        }
    }

    protected View inflateEditingRowView(LayoutInflater layoutInflater, ViewGroup parent) {
        return layoutInflater.inflate(R.layout.user_dictionary_word_row_edit, parent, false);
    }

    @Override
    public void onBindViewHolder(EditorWordViewHolder holder, int position) {
        holder.bind(mEditorWords.get(position));
    }

    @Override
    public int getItemCount() {
        return mEditorWords.size();
    }

    public void addNewWordAtEnd(RecyclerView wordsRecyclerView) {
        final int lastLocation = mEditorWords.size() - 1;
        EditorWord editorWord = mEditorWords.get(lastLocation);
        if (editorWord instanceof EditorWord.AddNew) {
            mEditorWords.remove(lastLocation);
        }
        final int newLastLocation = mEditorWords.size() - 1;
        mEditorWords.add(createEmptyNewEditing());
        notifyItemChanged(newLastLocation);
        wordsRecyclerView.smoothScrollToPosition(newLastLocation);
    }

    protected EditorWord.Editing createEmptyNewEditing() {
        return new EditorWord.Editing("", 128);
    }

    protected void bindNormalWordViewText(TextView wordView, EditorWord editorWord) {
        wordView.setText(editorWord.word);
    }

    protected void bindEditingWordViewText(EditText wordView, EditorWord editorWord) {
        wordView.setText(editorWord.word);
    }

    protected EditorWord createNewEditorWord(EditText wordView, EditorWord oldEditorWord) {
        return new EditorWord(wordView.getText().toString(), oldEditorWord.frequency);
    }

    /*package*/ abstract class EditorWordViewHolder extends RecyclerView.ViewHolder {
        private EditorWord mWord;

        public EditorWordViewHolder(View itemView) {
            super(itemView);
        }

        protected int getItemPosition() {
            return mEditorWords.indexOf(mWord);
        }

        public void bind(EditorWord editorWord) {
            mWord = editorWord;
        }
    }

    private class EditorWordViewHolderAddNew extends EditorWordViewHolder implements View.OnClickListener {

        public EditorWordViewHolderAddNew(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final int itemPosition = getItemPosition();
            mEditorWords.remove(itemPosition);
            mEditorWords.add(itemPosition, createEmptyNewEditing());
            notifyItemChanged(itemPosition);
        }
    }

    private class EditorWordViewHolderNormal extends EditorWordViewHolder implements View.OnClickListener {
        private final TextView mWordView;

        public EditorWordViewHolderNormal(View itemView) {
            super(itemView);
            mWordView = (TextView) itemView.findViewById(R.id.word_view);
            mWordView.setOnClickListener(this);
            itemView.findViewById(R.id.delete_user_word).setOnClickListener(this);
        }

        @Override
        public void bind(EditorWord editorWord) {
            super.bind(editorWord);
            bindNormalWordViewText(mWordView, editorWord);
        }

        @Override
        public void onClick(View v) {
            final int itemPosition = getItemPosition();
            if (itemPosition < 0) return;//this means that the view has already detached from the window.

            if (v == mWordView) {
                EditorWord editorWord = mEditorWords.remove(itemPosition);
                mEditorWords.add(itemPosition, new EditorWord.Editing(editorWord.word, editorWord.frequency));
                notifyItemChanged(itemPosition);
            } else if (v.getId() == R.id.delete_user_word) {
                EditorWord editorWord = mEditorWords.remove(itemPosition);
                notifyItemRemoved(itemPosition);
                mDictionaryCallbacks.onWordDeleted(editorWord);
            }
        }
    }

    private class EditorWordViewHolderEditing extends EditorWordViewHolder implements View.OnClickListener {
        private final EditText mWordView;

        public EditorWordViewHolderEditing(View itemView) {
            super(itemView);
            mWordView = (EditText) itemView.findViewById(R.id.word_view);
            itemView.findViewById(R.id.approve_user_word).setOnClickListener(this);
            itemView.findViewById(R.id.cancel_user_word).setOnClickListener(this);
        }

        @Override
        public void bind(EditorWord editorWord) {
            super.bind(editorWord);
            bindEditingWordViewText(mWordView, editorWord);
        }

        @Override
        public void onClick(View v) {
            final int itemPosition = getItemPosition();
            final boolean addNewRow = (itemPosition == mEditorWords.size()-1);
            if (v.getId() == R.id.cancel_user_word || TextUtils.isEmpty(mWordView.getText())) {
                EditorWord editorWord = mEditorWords.remove(itemPosition);
                if (addNewRow) {
                    mEditorWords.add(itemPosition, new EditorWord.AddNew());
                } else {
                    mEditorWords.add(itemPosition, new EditorWord(editorWord.word, editorWord.frequency));
                }
            } else if (v.getId() == R.id.approve_user_word) {
                EditorWord editorWord = mEditorWords.remove(itemPosition);
                EditorWord newEditorWord = createNewEditorWord(mWordView, editorWord);
                mEditorWords.add(itemPosition, newEditorWord);
                if (addNewRow) {
                    mEditorWords.add(new EditorWord.AddNew());
                    notifyItemInserted(mEditorWords.size() - 1);
                }
                mDictionaryCallbacks.onWordUpdated(editorWord.word, newEditorWord);
            }
            notifyItemChanged(itemPosition);
        }
    }

    public interface DictionaryCallbacks {
        void onWordDeleted(final EditorWord word);
        void onWordUpdated(final String oldWord, final EditorWord newWord);
    }
}
