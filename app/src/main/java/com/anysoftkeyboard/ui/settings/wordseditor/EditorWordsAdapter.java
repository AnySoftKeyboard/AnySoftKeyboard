package com.anysoftkeyboard.ui.settings.wordseditor;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.anysoftkeyboard.base.dictionaries.LoadedWord;
import com.menny.android.anysoftkeyboard.R;

import java.util.ArrayList;
import java.util.List;

public class EditorWordsAdapter extends RecyclerView.Adapter<EditorWordsAdapter.EditorWordViewHolder> {

    protected final List<LoadedWord> mEditorWords;
    private final LayoutInflater mLayoutInflater;
    private final DictionaryCallbacks mDictionaryCallbacks;

    public EditorWordsAdapter(List<LoadedWord> editorWords, LayoutInflater layoutInflater, DictionaryCallbacks dictionaryCallbacks) {
        mEditorWords = new ArrayList<>(editorWords);
        mEditorWords.add(new AddNew());
        mLayoutInflater = layoutInflater;
        mDictionaryCallbacks = dictionaryCallbacks;
    }

    @Override
    public int getItemViewType(int position) {
        LoadedWord editorWord = mEditorWords.get(position);
        if (editorWord instanceof Editing) {
            return R.id.word_editor_view_type_editing_row;
        } else if (editorWord instanceof AddNew) {
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
        int editNewItemLocation = mEditorWords.size() - 1;
        LoadedWord editorWord = mEditorWords.get(editNewItemLocation);
        if (editorWord instanceof AddNew || editorWord instanceof Editing) {
            mEditorWords.remove(editNewItemLocation);
        } else {
            editNewItemLocation++;//add after that
        }
        mEditorWords.add(createEmptyNewEditing());
        notifyItemChanged(editNewItemLocation);
        wordsRecyclerView.smoothScrollToPosition(editNewItemLocation);
    }

    protected Editing createEmptyNewEditing() {
        return new Editing("", 128);
    }

    protected void bindNormalWordViewText(TextView wordView, LoadedWord editorWord) {
        wordView.setText(editorWord.word);
    }

    protected void bindEditingWordViewText(EditText wordView, LoadedWord editorWord) {
        wordView.setText(editorWord.word);
    }

    protected LoadedWord createNewEditorWord(EditText wordView, LoadedWord oldEditorWord) {
        return new LoadedWord(wordView.getText().toString(), oldEditorWord.freq);
    }

    /*package*/ abstract class EditorWordViewHolder extends RecyclerView.ViewHolder {
        private LoadedWord mWord;

        public EditorWordViewHolder(View itemView) {
            super(itemView);
        }

        protected int getItemPosition() {
            return mEditorWords.indexOf(mWord);
        }

        public void bind(LoadedWord editorWord) {
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
            if (itemPosition == -1) return;//somehow, the word is not in the list of words anymore.
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
        public void bind(LoadedWord editorWord) {
            super.bind(editorWord);
            bindNormalWordViewText(mWordView, editorWord);
        }

        @Override
        public void onClick(View v) {
            final int itemPosition = getItemPosition();
            if (itemPosition < 0)
                return;//this means that the view has already detached from the window.

            if (v == mWordView) {
                LoadedWord editorWord = mEditorWords.remove(itemPosition);
                mEditorWords.add(itemPosition, new Editing(editorWord.word, editorWord.freq));
                notifyItemChanged(itemPosition);
            } else if (v.getId() == R.id.delete_user_word) {
                LoadedWord editorWord = mEditorWords.remove(itemPosition);
                notifyItemRemoved(itemPosition);
                mDictionaryCallbacks.onWordDeleted(editorWord);
            }
        }
    }

    public static class Editing extends LoadedWord {
        public Editing(@NonNull String word, int frequency) {
            super(word, frequency);
        }
    }

    public static class AddNew extends LoadedWord {
        public AddNew() {
            super("", -1);
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
        public void bind(LoadedWord editorWord) {
            super.bind(editorWord);
            bindEditingWordViewText(mWordView, editorWord);
        }

        @Override
        public void onClick(View v) {
            final int itemPosition = getItemPosition();
            if (itemPosition == -1) return;//somehow, the word is not in the list of words anymore.

            final boolean addNewRow = (itemPosition == mEditorWords.size() - 1);
            if (v.getId() == R.id.cancel_user_word || TextUtils.isEmpty(mWordView.getText())) {
                LoadedWord editorWord = mEditorWords.remove(itemPosition);
                if (addNewRow) {
                    mEditorWords.add(itemPosition, new AddNew());
                } else {
                    mEditorWords.add(itemPosition, new LoadedWord(editorWord.word, editorWord.freq));
                }
            } else if (v.getId() == R.id.approve_user_word) {
                LoadedWord editorWord = mEditorWords.remove(itemPosition);
                LoadedWord newEditorWord = createNewEditorWord(mWordView, editorWord);
                mEditorWords.add(itemPosition, newEditorWord);
                if (addNewRow) {
                    mEditorWords.add(new AddNew());
                    notifyItemInserted(mEditorWords.size() - 1);
                }
                mDictionaryCallbacks.onWordUpdated(editorWord.word, newEditorWord);
            }
            notifyItemChanged(itemPosition);
        }
    }

    public interface DictionaryCallbacks {
        void onWordDeleted(final LoadedWord word);

        void onWordUpdated(final String oldWord, final LoadedWord newWord);
    }
}
