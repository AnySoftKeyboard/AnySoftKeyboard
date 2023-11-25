package com.anysoftkeyboard.ui.settings.wordseditor;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.anysoftkeyboard.dictionaries.content.AndroidUserDictionaryTest;
import com.anysoftkeyboard.dictionaries.sqlite.WordsSQLiteConnection;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.SharedPrefsHelper;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;

@Config(sdk = Build.VERSION_CODES.M)
public class UserDictionaryEditorFragmentTest
    extends RobolectricFragmentTestCase<UserDictionaryEditorFragment> {

  @Override
  protected int getStartFragmentNavigationId() {
    return R.id.userDictionaryEditorFragment;
  }

  @NonNull private UserDictionaryEditorFragment startEditorFragment() {
    UserDictionaryEditorFragment fragment = startFragment();
    fragment
        .getSpinnerItemSelectedListener()
        .onItemSelected(fragment.getLanguagesSpinner(), null, 0, 0);
    TestRxSchedulers.drainAllTasks();
    return fragment;
  }

  @Test
  public void testAddNewWordFromMenuAtEmptyState() {
    UserDictionaryEditorFragment fragment = startEditorFragment();

    RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
    Assert.assertNotNull(wordsRecyclerView);
    Assert.assertEquals(1 /*empty view*/, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_empty_view_row,
        wordsRecyclerView.getAdapter().getItemViewType(0));

    final MenuItem menuItem = Mockito.mock(MenuItem.class);
    Mockito.doReturn(R.id.add_user_word).when(menuItem).getItemId();
    fragment.onOptionsItemSelected(menuItem);
    TestRxSchedulers.drainAllTasks();

    Assert.assertEquals(1, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_editing_row, wordsRecyclerView.getAdapter().getItemViewType(0));
  }

  @Test
  public void testTwiceAddNewWordFromMenuAtEmptyState() {
    UserDictionaryEditorFragment fragment = startEditorFragment();

    RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
    Assert.assertNotNull(wordsRecyclerView);
    Assert.assertEquals(1 /*empty view*/, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_empty_view_row,
        wordsRecyclerView.getAdapter().getItemViewType(0));

    final MenuItem menuItem = Mockito.mock(MenuItem.class);
    Mockito.doReturn(R.id.add_user_word).when(menuItem).getItemId();
    fragment.onOptionsItemSelected(menuItem);
    TestRxSchedulers.drainAllTasks();

    fragment.onOptionsItemSelected(menuItem);
    TestRxSchedulers.drainAllTasks();

    Assert.assertEquals(1, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_editing_row, wordsRecyclerView.getAdapter().getItemViewType(0));
  }

  @Test
  public void testAddNewWordFromMenuNotAtEmptyState() {
    // adding a few words to the dictionary
    UserDictionary userDictionary = new UserDictionary(getApplicationContext(), "en");
    userDictionary.loadDictionary();
    userDictionary.addWord("hello", 1);
    userDictionary.addWord("you", 2);
    userDictionary.close();

    UserDictionaryEditorFragment fragment = startEditorFragment();

    RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
    Assert.assertNotNull(wordsRecyclerView);
    Assert.assertEquals(
        3 /*two words, and one AddNew*/, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
    Assert.assertEquals(
        R.id.word_editor_view_type_add_new_row, wordsRecyclerView.getAdapter().getItemViewType(2));

    final MenuItem menuItem = Mockito.mock(MenuItem.class);
    Mockito.doReturn(R.id.add_user_word).when(menuItem).getItemId();
    fragment.onOptionsItemSelected(menuItem);
    TestRxSchedulers.drainAllTasks();

    Assert.assertEquals(3, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
    Assert.assertEquals(
        R.id.word_editor_view_type_editing_row, wordsRecyclerView.getAdapter().getItemViewType(2));
  }

  @Test
  public void testTwiceAddNewWordFromMenuNotAtEmptyState() {
    // adding a few words to the dictionary
    UserDictionary userDictionary = new UserDictionary(getApplicationContext(), "en");
    userDictionary.loadDictionary();
    userDictionary.addWord("hello", 1);
    userDictionary.addWord("you", 2);
    userDictionary.close();

    UserDictionaryEditorFragment fragment = startEditorFragment();

    RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
    Assert.assertNotNull(wordsRecyclerView);
    Assert.assertEquals(
        3 /*two words, and one AddNew*/, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
    Assert.assertEquals(
        R.id.word_editor_view_type_add_new_row, wordsRecyclerView.getAdapter().getItemViewType(2));

    final MenuItem menuItem = Mockito.mock(MenuItem.class);
    Mockito.doReturn(R.id.add_user_word).when(menuItem).getItemId();
    fragment.onOptionsItemSelected(menuItem);
    TestRxSchedulers.drainAllTasks();

    fragment.onOptionsItemSelected(menuItem);
    TestRxSchedulers.drainAllTasks();

    Assert.assertEquals(3, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
    Assert.assertEquals(
        R.id.word_editor_view_type_editing_row, wordsRecyclerView.getAdapter().getItemViewType(2));
  }

  @Test
  public void testDeleteWord() {
    // adding a few words to the dictionary
    UserDictionary userDictionary = new UserDictionary(getApplicationContext(), "en");
    userDictionary.loadDictionary();
    userDictionary.addWord("hello", 1);
    userDictionary.addWord("you", 2);
    userDictionary.close();

    UserDictionaryEditorFragment fragment = startEditorFragment();
    TestRxSchedulers.drainAllTasks();

    RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
    // http://stackoverflow.com/questions/27052866/android-robolectric-click-recyclerview-item
    wordsRecyclerView.measure(0, 0);
    wordsRecyclerView.layout(0, 0, 100, 10000);

    Assert.assertEquals(
        3 /*two words, and one AddNew*/, wordsRecyclerView.getAdapter().getItemCount());
    View helloRowView = wordsRecyclerView.findViewHolderForAdapterPosition(0).itemView;
    Assert.assertNotNull(helloRowView);
    View deleteButtonView = helloRowView.findViewById(R.id.delete_user_word);
    Assert.assertNotNull(deleteButtonView);
    TextView helloTextView = helloRowView.findViewById(R.id.word_view);
    Assert.assertNotNull(helloTextView);
    Assert.assertEquals("hello", helloTextView.getText().toString());
    // deleting word
    Shadows.shadowOf(deleteButtonView).getOnClickListener().onClick(deleteButtonView);
    TestRxSchedulers.drainAllTasks();

    Assert.assertEquals(2, wordsRecyclerView.getAdapter().getItemCount());
  }

  @Test
  public void testAndroidDictionaryLoad() {
    SharedPrefsHelper.setPrefsValue(
        R.string.settings_key_always_use_fallback_user_dictionary, false);
    // adding a few words to the dictionary
    AndroidUserDictionaryTest.AUDContentProvider provider =
        new AndroidUserDictionaryTest.AUDContentProvider();
    ContentProviderController.of(provider).create(provider.getAuthority());
    // setting up some dummy words
    provider.addRow(1, "Dude", 1, "en");
    provider.addRow(2, "Dudess", 2, "en");
    provider.addRow(3, "shalom", 10, "iw");
    provider.addRow(4, "telephone", 2, "iw");
    provider.addRow(5, "catchall", 5, null);

    UserDictionaryEditorFragment fragment = startEditorFragment();

    RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
    Assert.assertNotNull(wordsRecyclerView);
    // we're expecting 3 items - 2 english words and one AddNew.
    Assert.assertEquals(3, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
    Assert.assertEquals(
        R.id.word_editor_view_type_add_new_row, wordsRecyclerView.getAdapter().getItemViewType(2));
  }

  @Test
  public void testFallbackDictionaryLoad() {
    SharedPrefsHelper.setPrefsValue(
        R.string.settings_key_always_use_fallback_user_dictionary, true);
    // adding a few words to the dictionary
    WordsSQLiteConnection connectionEn =
        new WordsSQLiteConnection(ApplicationProvider.getApplicationContext(), "fallback.db", "en");
    connectionEn.addWord("Dude", 2);
    connectionEn.addWord("Dudess", 3);

    WordsSQLiteConnection connectionFr =
        new WordsSQLiteConnection(ApplicationProvider.getApplicationContext(), "fallback.db", "fr");
    connectionFr.addWord("Oui", 2);
    connectionFr.addWord("No", 3);

    UserDictionaryEditorFragment fragment = startEditorFragment();

    RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
    Assert.assertNotNull(wordsRecyclerView);
    // we're expecting 3 items - 2 english words and one AddNew.
    Assert.assertEquals(3, wordsRecyclerView.getAdapter().getItemCount());
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
    Assert.assertEquals(
        R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
    Assert.assertEquals(
        R.id.word_editor_view_type_add_new_row, wordsRecyclerView.getAdapter().getItemViewType(2));
  }
}
