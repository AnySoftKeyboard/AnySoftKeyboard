package com.anysoftkeyboard.ui.settings.wordseditor;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.anysoftkeyboard.dictionaries.EditableDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AbbreviationsDictionary;
import com.menny.android.anysoftkeyboard.R;

import java.util.List;

import javax.annotation.Nullable;

public class AbbreviationDictionaryEditorFragment extends UserDictionaryEditorFragment {

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(getString(R.string.abbreviation_dict_settings_titlebar));
    }

    @Override
    protected EditableDictionary getEditableDictionary(String locale) {
        return new AbbreviationsDictionary(getActivity().getApplicationContext(), locale);
    }

    @Override
    protected ListAdapter getWordsListAdapter(List<UserWordsListAdapter.Word> wordsList) {
        return new AbbreviationWordsListAdapter(
                getActivity(),
                wordsList,
                this);
    }

    private static class AbbreviationWordsListAdapter extends UserWordsListAdapter {

        public AbbreviationWordsListAdapter(Context context, List<Word> words, AdapterCallbacks callbacks) {
            super(context, words, callbacks);
        }

        private static String getAbbreviation(@Nullable Word word) {
            if (word == null) return "";
            return word.word.substring(0, word.frequency);
        }

        private static String getExplodedSentence(@Nullable Word word) {
            if (word == null) return "";
            return word.word.substring(word.frequency);
        }

        @Override
        protected void updateEditedWordRow(View rootView, TextView wordView, Word word) {
            wordView.setText(getAbbreviation(word));
            EditText explodedSentence = (EditText)rootView.findViewById(R.id.word_target_view);
            explodedSentence.setText(getExplodedSentence(word));
        }

        @Override
        protected void updateNormalWordRow(View rootView, TextView wordView, Word word) {
            wordView.setText(getContext().getString(R.string.abbreviation_dict_word_template,
                    getAbbreviation(word), getExplodedSentence(word)));
        }

        @Override
        protected View inflateEditedWordRow(LayoutInflater inflater, ViewGroup parent) {
            return inflater.inflate(R.layout.abbreviation_dictionary_word_row_edit, parent, false);
        }

        @Override
        protected Word onWordEditApproved(View approveButton, @Nullable Word oldWord) {
            View parent = ((View) approveButton.getParent());
            EditText abbreviationView = (EditText) parent.findViewById(R.id.word_view);
            EditText explodedSentenceView = (EditText) parent.findViewById(R.id.word_target_view);
            final String newAbbreviation = abbreviationView.getText().toString();
            final String newExplodedSentence = explodedSentenceView.getText().toString();
            if (TextUtils.isEmpty(newAbbreviation) || TextUtils.isEmpty(newExplodedSentence)) {
                return null;
            } else {
                return new Word(newAbbreviation+newExplodedSentence, newAbbreviation.length());
            }
        }
    }

}
