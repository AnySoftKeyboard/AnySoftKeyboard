package com.anysoftkeyboard.ui.settings.wordseditor;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.anysoftkeyboard.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.PassengerFragmentSupport;

import java.util.List;

public class AbbreviationDictionaryEditorFragment extends UserDictionaryEditorFragment {

    @Override
    public void onStart() {
        super.onStart();
        PassengerFragmentSupport.setActivityTitle(this, getString(R.string.abbreviation_dict_settings_titlebar));
    }

    @Override
    protected EditableDictionary getEditableDictionary(String locale) {
        return new AbbreviationsDictionary(getActivity().getApplicationContext(), locale);
    }

    @Override
    protected EditorWordsAdapter createAdapterForWords(List<EditorWord> wordsList) {
        return new AbbreviationEditorWordsAdapter(wordsList, getActivity(), this);
    }

    private static class AbbreviationEditorWordsAdapter extends EditorWordsAdapter {

        private final Context mContext;

        public AbbreviationEditorWordsAdapter(List<EditorWord> editorWords, Context context, DictionaryCallbacks dictionaryCallbacks) {
            super(editorWords, LayoutInflater.from(context), dictionaryCallbacks);
            mContext = context;
        }

        @Override
        protected EditorWord.Editing createEmptyNewEditing() {
            return new EditorWord.Editing("", 0);
        }

        protected void bindNormalWordViewText(TextView wordView, EditorWord editorWord) {
            wordView.setText(mContext.getString(R.string.abbreviation_dict_word_template,
                    getAbbreviation(editorWord), getExplodedSentence(editorWord)));
        }

        @Override
        protected View inflateEditingRowView(LayoutInflater layoutInflater, ViewGroup parent) {
            return layoutInflater.inflate(R.layout.abbreviation_dictionary_word_row_edit, parent, false);
        }

        @Override
        protected void bindEditingWordViewText(EditText wordView, EditorWord editorWord) {
            wordView.setText(getAbbreviation(editorWord));
            EditText explodedSentence = (EditText) ((View)wordView.getParent()).findViewById(R.id.word_target_view);
            explodedSentence.setText(getExplodedSentence(editorWord));
        }

        @Override
        protected EditorWord createNewEditorWord(EditText wordView, EditorWord oldEditorWord) {
            EditText explodedSentenceView = (EditText) ((View)wordView.getParent()).findViewById(R.id.word_target_view);
            final String newAbbreviation = wordView.getText().toString();
            final String newExplodedSentence = explodedSentenceView.getText().toString();
            if (TextUtils.isEmpty(newAbbreviation) || TextUtils.isEmpty(newExplodedSentence)) {
                return oldEditorWord;
            } else {
                return new EditorWord(newAbbreviation + newExplodedSentence, newAbbreviation.length());
            }
        }

        private static String getAbbreviation(@Nullable EditorWord word) {
            if (word == null) return "";
            return AbbreviationsDictionary.getAbbreviation(word.word, word.frequency);
        }

        private static String getExplodedSentence(@Nullable EditorWord word) {
            if (word == null) return "";
            return AbbreviationsDictionary.getExplodedSentence(word.word, word.frequency);
        }
    }
}
