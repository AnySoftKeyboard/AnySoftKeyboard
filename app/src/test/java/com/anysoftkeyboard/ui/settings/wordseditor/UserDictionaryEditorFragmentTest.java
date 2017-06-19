package com.anysoftkeyboard.ui.settings.wordseditor;

import android.Manifest;
import android.content.ContentProvider;
import android.content.pm.ProviderInfo;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.dictionaries.UserDictionary;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.shadows.ShadowApplication;

public class UserDictionaryEditorFragmentTest extends RobolectricFragmentTestCase<UserDictionaryEditorFragment> {

    @NonNull
    @Override
    protected UserDictionaryEditorFragment createFragment() {
        return new UserDictionaryEditorFragment();
    }

    @NonNull
    private UserDictionaryEditorFragment startEditorFragment() {
        UserDictionaryEditorFragment fragment = startFragment();
        fragment.getSpinnerItemSelectedListener().onItemSelected(fragment.getLanguagesSpinner(), null, 0, 0);
        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();
        return fragment;
    }

    @Test
    public void testAddNewWordFromMenuAtEmptyState() {
        UserDictionaryEditorFragment fragment = startEditorFragment();

        RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
        Assert.assertNotNull(wordsRecyclerView);
        Assert.assertEquals(1/*empty view*/, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_empty_view_row, wordsRecyclerView.getAdapter().getItemViewType(0));

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.add_user_word).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertEquals(1, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_editing_row, wordsRecyclerView.getAdapter().getItemViewType(0));
    }

    @Test
    public void testTwiceAddNewWordFromMenuAtEmptyState() {
        UserDictionaryEditorFragment fragment = startEditorFragment();

        RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
        Assert.assertNotNull(wordsRecyclerView);
        Assert.assertEquals(1/*empty view*/, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_empty_view_row, wordsRecyclerView.getAdapter().getItemViewType(0));

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.add_user_word).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        fragment.onOptionsItemSelected(menuItem);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertEquals(1, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_editing_row, wordsRecyclerView.getAdapter().getItemViewType(0));
    }

    @Test
    public void testAddNewWordFromMenuNotAtEmptyState() {
        //adding a few words to the dictionary
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        userDictionary.addWord("hello", 1);
        userDictionary.addWord("you", 2);
        userDictionary.close();

        UserDictionaryEditorFragment fragment = startEditorFragment();

        RecyclerView wordsRecyclerView = (RecyclerView) fragment.getView().findViewById(R.id.words_recycler_view);
        Assert.assertNotNull(wordsRecyclerView);
        Assert.assertEquals(3/*two words, and one AddNew*/, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
        Assert.assertEquals(R.id.word_editor_view_type_add_new_row, wordsRecyclerView.getAdapter().getItemViewType(2));

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.add_user_word).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertEquals(3, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
        Assert.assertEquals(R.id.word_editor_view_type_editing_row, wordsRecyclerView.getAdapter().getItemViewType(2));
    }

    @Test
    public void testTwiceAddNewWordFromMenuNotAtEmptyState() {
        //adding a few words to the dictionary
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        userDictionary.addWord("hello", 1);
        userDictionary.addWord("you", 2);
        userDictionary.close();

        UserDictionaryEditorFragment fragment = startEditorFragment();

        RecyclerView wordsRecyclerView = (RecyclerView) fragment.getView().findViewById(R.id.words_recycler_view);
        Assert.assertNotNull(wordsRecyclerView);
        Assert.assertEquals(3/*two words, and one AddNew*/, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
        Assert.assertEquals(R.id.word_editor_view_type_add_new_row, wordsRecyclerView.getAdapter().getItemViewType(2));

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.add_user_word).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        fragment.onOptionsItemSelected(menuItem);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertEquals(3, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
        Assert.assertEquals(R.id.word_editor_view_type_editing_row, wordsRecyclerView.getAdapter().getItemViewType(2));
    }

    @Test
    public void testDeleteWord() {
        //adding a few words to the dictionary
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        userDictionary.addWord("hello", 1);
        userDictionary.addWord("you", 2);
        userDictionary.close();

        UserDictionaryEditorFragment fragment = startEditorFragment();

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        RecyclerView wordsRecyclerView = (RecyclerView) fragment.getView().findViewById(R.id.words_recycler_view);
        //http://stackoverflow.com/questions/27052866/android-robolectric-click-recyclerview-item
        wordsRecyclerView.measure(0, 0);
        wordsRecyclerView.layout(0, 0, 100, 10000);

        Assert.assertEquals(3/*two words, and one AddNew*/, wordsRecyclerView.getAdapter().getItemCount());
        View helloRowView = wordsRecyclerView.findViewHolderForAdapterPosition(0).itemView;
        Assert.assertNotNull(helloRowView);
        View deleteButtonView = helloRowView.findViewById(R.id.delete_user_word);
        Assert.assertNotNull(deleteButtonView);
        TextView helloTextView = (TextView) helloRowView.findViewById(R.id.word_view);
        Assert.assertNotNull(helloTextView);
        Assert.assertEquals("hello", helloTextView.getText().toString());
        //deleting word
        Shadows.shadowOf(deleteButtonView).getOnClickListener().onClick(deleteButtonView);

        Robolectric.flushBackgroundThreadScheduler();
        Robolectric.flushForegroundThreadScheduler();

        Assert.assertEquals(2, wordsRecyclerView.getAdapter().getItemCount());
    }

    @Test
    public void testAndroidDictionaryLoad() {
        //adding a few words to the dictionary
        ContentProvider provider = Mockito.mock(ContentProvider.class);
        MatrixCursor initialContacts = new MatrixCursor(new String[]{android.provider.UserDictionary.Words._ID, android.provider.UserDictionary.Words.WORD, android.provider.UserDictionary.Words.FREQUENCY, android.provider.UserDictionary.Words.LOCALE});
        Mockito.doReturn(initialContacts).when(provider).query(Mockito.any(Uri.class), Mockito.any(String[].class), Mockito.anyString(), Mockito.any(String[].class), Mockito.anyString());
        initialContacts.addRow(new Object[]{1, "Dude", 1, "en"});
        initialContacts.addRow(new Object[]{1, "Dudess", 2, "en"});
        initialContacts.addRow(new Object[]{1, "shalom", 10, "iw"});
        initialContacts.addRow(new Object[]{1, "telephone", 2, "iw"});
        initialContacts.addRow(new Object[]{1, "catchall", 5, null});

        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = android.provider.UserDictionary.Words.CONTENT_URI.getAuthority();
        ContentProviderController.of(provider).create(providerInfo);

        UserDictionaryEditorFragment fragment = startEditorFragment();

        RecyclerView wordsRecyclerView = fragment.getView().findViewById(R.id.words_recycler_view);
        Assert.assertNotNull(wordsRecyclerView);
        Assert.assertEquals(6/*words, and one AddNew*/, wordsRecyclerView.getAdapter().getItemCount());
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(0));
        Assert.assertEquals(R.id.word_editor_view_type_row, wordsRecyclerView.getAdapter().getItemViewType(1));
        Assert.assertEquals(R.id.word_editor_view_type_add_new_row, wordsRecyclerView.getAdapter().getItemViewType(5));
    }

    @Test
    public void testBackup() {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        //adding a few words to the dictionary
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        userDictionary.addWord("hello", 1);
        userDictionary.addWord("you", 2);
        userDictionary.close();

        UserDictionaryEditorFragment fragment = startEditorFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.backup_words).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        //we want a success dialog here
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.user_dict_backup_success_title), ShadowApplication.getInstance().getLatestAlertDialog().getTitle());
    }

    @Test
    public void testBackupFailsWhenNoPermissions() {
        //adding a few words to the dictionary
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        userDictionary.addWord("hello", 1);
        userDictionary.addWord("you", 2);
        userDictionary.close();

        UserDictionaryEditorFragment fragment = startEditorFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.backup_words).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        //nothing happens here
        Assert.assertNull(ShadowApplication.getInstance().getLatestAlertDialog());
    }

    @Test
    public void testRestore() {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        //adding a few words to the dictionary
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        userDictionary.addWord("hello", 1);
        userDictionary.addWord("you", 2);
        userDictionary.close();

        UserDictionaryEditorFragment fragment = startEditorFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.backup_words).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        Mockito.doReturn(R.id.restore_words).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        //we want a success dialog here
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.user_dict_restore_success_title), ShadowApplication.getInstance().getLatestAlertDialog().getTitle());
    }

    @Test
    public void testRestoreFailsWhenNoFile() {
        Shadows.shadowOf(RuntimeEnvironment.application).grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

        UserDictionaryEditorFragment fragment = startEditorFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.restore_words).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        //we want a failure dialog here
        Assert.assertEquals(RuntimeEnvironment.application.getText(R.string.user_dict_restore_fail_title), ShadowApplication.getInstance().getLatestAlertDialog().getTitle());
    }

    @Test
    public void testRestoreFailsWhenNoPermissions() {
        //adding a few words to the dictionary
        UserDictionary userDictionary = new UserDictionary(RuntimeEnvironment.application, "en");
        userDictionary.loadDictionary();
        userDictionary.addWord("hello", 1);
        userDictionary.addWord("you", 2);
        userDictionary.close();

        UserDictionaryEditorFragment fragment = startEditorFragment();

        final MenuItem menuItem = Mockito.mock(MenuItem.class);
        Mockito.doReturn(R.id.restore_words).when(menuItem).getItemId();
        fragment.onOptionsItemSelected(menuItem);

        //nothing happens here
        Assert.assertNull(ShadowApplication.getInstance().getLatestAlertDialog());
    }
}