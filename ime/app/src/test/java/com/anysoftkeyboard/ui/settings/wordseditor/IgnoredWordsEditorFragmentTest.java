package com.anysoftkeyboard.ui.settings.wordseditor;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;

public class IgnoredWordsEditorFragmentTest
    extends RobolectricFragmentTestCase<IgnoredWordsEditorFragment> {

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.ignoredWordsEditorFragment;
  }

  @NonNull
  private IgnoredWordsEditorFragment startEditorFragment() {
    IgnoredWordsEditorFragment fragment = startFragment();
    fragment
        .getSpinnerItemSelectedListener()
        .onItemSelected(fragment.getLanguagesSpinner(), null, 0, 0);
    TestRxSchedulers.drainAllTasks();
    return fragment;
  }

  @Test
  public void testInitialEmptyState() {
    IgnoredWordsEditorFragment fragment = startEditorFragment();

    RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
    Assert.assertNotNull(wordsRecyclerView);
    Assert.assertEquals(1 /*empty view*/, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_empty_view_row,
        wordsRecyclerView.getAdapter().getItemViewType(0));

    RecyclerView.ViewHolder viewHolder = wordsRecyclerView.findViewHolderForAdapterPosition(0);
    if (viewHolder == null) {
      viewHolder =
          wordsRecyclerView
              .getAdapter()
              .createViewHolder(wordsRecyclerView, R.id.word_editor_view_type_empty_view_row);
      wordsRecyclerView.getAdapter().bindViewHolder(viewHolder, 0);
    }
    android.widget.TextView emptyTextView =
        viewHolder.itemView.findViewById(R.id.empty_user_words_editor_text);
    Assert.assertNotNull(emptyTextView);
    Assert.assertEquals(
        fragment.getString(R.string.ignored_words_dict_empty_text),
        emptyTextView.getText().toString());
  }
}
