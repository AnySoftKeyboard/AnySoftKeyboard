/*
 * Copyright (c) 2013 Menny Even-Danan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.ui.settings;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.dictionaries.RadicalOverlayRepository;
import com.menny.android.anysoftkeyboard.R;
import net.evendanan.pixel.UiUtils;

public class DictionariesFragment extends PreferenceFragmentCompat
    implements Preference.OnPreferenceClickListener {

  private static final String TAG = "DictionariesFragment";

  private ActivityResultLauncher<Uri> mPickFolderLauncher;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPickFolderLauncher =
        registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(), this::onFolderPicked);
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    addPreferencesFromResource(R.xml.prefs_dictionaries);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    findPreference(getString(R.string.user_dict_editor_key)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.abbreviation_dict_editor_key))
        .setOnPreferenceClickListener(this);
    findPreference(getString(R.string.next_word_dict_settings_key))
        .setOnPreferenceClickListener(this);
    findPreference(getString(R.string.settings_key_use_contacts_dictionary))
        .setOnPreferenceClickListener(this);
    findPreference(getString(R.string.settings_key_radical_overlay_dir))
        .setOnPreferenceClickListener(this);
    findPreference(getString(R.string.settings_key_radical_overlay_reset))
        .setOnPreferenceClickListener(this);
    findPreference(getString(R.string.settings_key_radical_overlay_refresh))
        .setOnPreferenceClickListener(this);
    updateRadicalOverlaySummary();
  }

  @Override
  public void onStart() {
    super.onStart();
    UiUtils.setActivityTitle(this, getString(R.string.special_dictionaries_group));
  }

  @Override
  public boolean onPreferenceClick(Preference preference) {
    final String key = preference.getKey();
    if (key.equals(getString(R.string.settings_key_radical_overlay_dir))) {
      launchFolderPicker();
      return true;
    }
    if (key.equals(getString(R.string.settings_key_radical_overlay_reset))) {
      resetRadicalOverlayFolder();
      return true;
    }
    if (key.equals(getString(R.string.settings_key_radical_overlay_refresh))) {
      refreshRadicalOverlay();
      return true;
    }
    final NavController navController = Navigation.findNavController(requireView());
    if (key.equals(getString(R.string.user_dict_editor_key))) {
      navController.navigate(
          DictionariesFragmentDirections
              .actionDictionariesFragmentToUserDictionaryEditorFragment());
      return true;
    } else if (key.equals(getString(R.string.abbreviation_dict_editor_key))) {
      navController.navigate(
          DictionariesFragmentDirections
              .actionDictionariesFragmentToAbbreviationDictionaryEditorFragment());
      return true;
    } else if (key.equals(getString(R.string.next_word_dict_settings_key))) {
      navController.navigate(
          DictionariesFragmentDirections.actionDictionariesFragmentToNextWordSettingsFragment());
      return true;
    } else if (key.equals(getString(R.string.settings_key_use_contacts_dictionary))
        && ((CheckBoxPreference) preference).isChecked()) {
      // user enabled Contacts!
      // ensuring we have permission to use it
      ((MainSettingsActivity) requireActivity()).startContactsPermissionRequest();
    }
    return false;
  }

  // ----- Radical overlay folder picker / reset / refresh ---------------------------------------

  private void launchFolderPicker() {
    try {
      mPickFolderLauncher.launch(null);
    } catch (Exception e) {
      Logger.w(TAG, "No activity available for ACTION_OPEN_DOCUMENT_TREE: %s", e.getMessage());
      Toast.makeText(
              requireContext(),
              getString(R.string.radical_overlay_toast_pick_failed, e.getMessage()),
              Toast.LENGTH_LONG)
          .show();
    }
  }

  private void onFolderPicked(@Nullable Uri uri) {
    if (uri == null) return; // user cancelled
    // Reject a folder we can't read anything from, otherwise the pick silently does nothing
    // and the keyboard just keeps using the bundled tables.
    if (!RadicalOverlayRepository.looksLikeOverlayFolder(requireContext(), uri)) {
      Toast.makeText(
              requireContext(),
              getString(R.string.radical_overlay_toast_no_tables, displayNameForTreeUri(uri)),
              Toast.LENGTH_LONG)
          .show();
      return;
    }
    try {
      RadicalOverlayRepository.setOverlayUri(requireContext(), uri);
    } catch (SecurityException e) {
      Logger.w(TAG, "setOverlayUri failed for %s: %s", uri, e.getMessage());
      Toast.makeText(
              requireContext(),
              getString(R.string.radical_overlay_toast_pick_failed, e.getMessage()),
              Toast.LENGTH_LONG)
          .show();
      return;
    }
    updateRadicalOverlaySummary();
    final String display = displayNameForTreeUri(uri);
    Toast.makeText(
            requireContext(),
            getString(R.string.radical_overlay_toast_picked, display),
            Toast.LENGTH_LONG)
        .show();
  }

  private void resetRadicalOverlayFolder() {
    RadicalOverlayRepository.clearOverlayUri(requireContext());
    updateRadicalOverlaySummary();
    Toast.makeText(requireContext(), R.string.radical_overlay_toast_reset, Toast.LENGTH_LONG)
        .show();
  }

  private void refreshRadicalOverlay() {
    RadicalOverlayRepository.invalidateCache(requireContext());
    Toast.makeText(requireContext(), R.string.radical_overlay_toast_refreshed, Toast.LENGTH_LONG)
        .show();
  }

  private void updateRadicalOverlaySummary() {
    Preference pref = findPreference(getString(R.string.settings_key_radical_overlay_dir));
    if (pref == null) return;
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
    final String value = prefs.getString(getString(R.string.settings_key_radical_overlay_dir), "");
    if (value == null || value.isEmpty()) {
      pref.setSummary(R.string.radical_overlay_dir_summary_default);
      return;
    }
    if (!value.startsWith("content://")) {
      // Legacy value (plain path), no longer supported; show "lost" message and clear it.
      pref.setSummary(R.string.radical_overlay_dir_summary_lost);
      return;
    }
    Uri uri = Uri.parse(value);
    if (!isUriStillAccessible(uri)) {
      pref.setSummary(R.string.radical_overlay_dir_summary_lost);
      return;
    }
    pref.setSummary(
        getString(R.string.radical_overlay_dir_summary_picked, displayNameForTreeUri(uri)));
  }

  private boolean isUriStillAccessible(@NonNull Uri uri) {
    try {
      for (android.content.UriPermission p :
          requireContext().getContentResolver().getPersistedUriPermissions()) {
        if (uri.equals(p.getUri()) && p.isReadPermission()) return true;
      }
    } catch (Exception e) {
      Logger.w(TAG, "getPersistedUriPermissions failed: %s", e.getMessage());
    }
    return false;
  }

  /** Best-effort friendly path for a SAF tree URI, e.g. {@code primary:Documents/boshiamy}. */
  @NonNull
  private static String displayNameForTreeUri(@NonNull Uri treeUri) {
    try {
      String id = DocumentsContract.getTreeDocumentId(treeUri);
      if (id != null && !id.isEmpty()) return id;
    } catch (Exception ignored) {
      // fall through
    }
    String last = treeUri.getLastPathSegment();
    return last == null ? treeUri.toString() : last;
  }
}
