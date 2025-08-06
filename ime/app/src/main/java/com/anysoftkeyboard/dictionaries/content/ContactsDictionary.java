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

package com.anysoftkeyboard.dictionaries.content;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract.Contacts;
import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.content.ContextCompat;
import com.anysoftkeyboard.base.utils.CompatUtils;
import com.anysoftkeyboard.nextword.NextWord;
import com.anysoftkeyboard.nextword.NextWordSuggestions;
import com.anysoftkeyboard.notification.NotificationIds;
import com.anysoftkeyboard.prefs.PrefType;
import com.anysoftkeyboard.prefs.SharedPreferencesChangeReceiver;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ContactsDictionary extends ContentObserverDictionary implements NextWordSuggestions {

  protected static final String TAG = "ASKContactsDict";

  /** A contact is a valid word in a language, and it usually very frequent. */
  private static final int MINIMUM_CONTACT_WORD_FREQUENCY = 64;

  private static final String[] PROJECTION = {
    Contacts._ID, Contacts.DISPLAY_NAME, Contacts.STARRED, Contacts.TIMES_CONTACTED
  };
  private static final int INDEX_NAME = 1;
  private static final int INDEX_STARRED = 2;
  private static final int INDEX_TIMES = 3;
  private final Map<String, String[]> mNextNameParts = new ArrayMap<>();
  private final Map<String, Map<String, NextWord>> mLoadingPhaseNextNames = new ArrayMap<>();

  public ContactsDictionary(Context context) {
    super("ContactsDictionary", context, Contacts.CONTENT_URI);
  }

  @Override
  protected void loadAllResources() {
    super.loadAllResources();
    mNextNameParts.clear();
    // converting the loaded NextWord into a simple, static array
    for (Map.Entry<String, Map<String, NextWord>> entry : mLoadingPhaseNextNames.entrySet()) {
      final String firstWord = entry.getKey();
      List<NextWord> nextWordList = new ArrayList<>(entry.getValue().values());
      Collections.sort(nextWordList, new NextWord.NextWordComparator());
      String[] nextParts = new String[nextWordList.size()];
      for (int index = 0; index < nextParts.length; index++)
        nextParts[index] = nextWordList.get(index).nextWord;
      mNextNameParts.put(firstWord, nextParts);
    }
    mLoadingPhaseNextNames.clear();
  }

  @Override
  protected void readWordsFromActualStorage(WordReadListener listener) {
    // we required Contacts permission
    if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS)
        != PackageManager.PERMISSION_GRANTED) {
      Intent intent = new Intent(MainSettingsActivity.ACTION_REQUEST_PERMISSION_ACTIVITY);
      intent.putExtra(
          MainSettingsActivity.EXTRA_KEY_ACTION_REQUEST_PERMISSION_ACTIVITY,
          Manifest.permission.READ_CONTACTS);
      intent.setClass(mContext, MainSettingsActivity.class);
      // we are running OUTSIDE an Activity
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      // showing a notification, so the user's flow will not be interrupted.
      final int approveRequestCode = 456451;
      PendingIntent approvePendingIntent =
          PendingIntent.getActivity(
              mContext, approveRequestCode, intent, CompatUtils.appendImmutableFlag(0));

      final int dismissRequestCode = 456452;
      Intent dismissIntent =
          new Intent(mContext.getApplicationContext(), SharedPreferencesChangeReceiver.class);
      dismissIntent.setAction(SharedPreferencesChangeReceiver.ACTION_CHANGE_PREF);
      dismissIntent.putExtra(
          SharedPreferencesChangeReceiver.EXTRA_PREF_KEY,
          mContext.getString(R.string.settings_key_use_contacts_dictionary));
      dismissIntent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_TYPE, PrefType.BOOLEAN);
      // we want to turn this feature OFF
      dismissIntent.putExtra(SharedPreferencesChangeReceiver.EXTRA_PREF_VALUE, false);
      PendingIntent dismissPendingIntent =
          PendingIntent.getBroadcast(
              mContext,
              dismissRequestCode,
              dismissIntent,
              CompatUtils.appendImmutableFlag(PendingIntent.FLAG_UPDATE_CURRENT));

      var notifier = AnyApplication.notifier(mContext);
      var builder =
          notifier
              .buildNotification(
                  NotificationIds.RequestContactsPermission,
                  R.drawable.ic_notification_contacts_permission_required,
                  R.string.notification_read_contacts_title)
              .setContentText(mContext.getString(R.string.notification_read_contacts_text))
              .setTicker(mContext.getString(R.string.notification_read_contacts_ticker))
              .setContentIntent(approvePendingIntent)
              .addAction(
                  R.drawable.ic_notification_action_approve_permission,
                  mContext.getString(R.string.notification_action_approve_permission),
                  approvePendingIntent)
              .addAction(
                  R.drawable.ic_notification_action_dismiss_permission,
                  mContext.getString(R.string.notification_action_dismiss_permission),
                  dismissPendingIntent)
              .setAutoCancel(true);

      notifier.notify(builder, true);
      // and failing. So it will try to read contacts again
      throw new RuntimeException("We do not have permission to read contacts!");
    }

    try (Cursor cursor =
        mContext
            .getContentResolver()
            .query(
                Contacts.CONTENT_URI,
                PROJECTION,
                Contacts.IN_VISIBLE_GROUP + "=?",
                new String[] {"1"},
                null)) {
      if (cursor != null && cursor.moveToFirst()) {
        while (!cursor.isAfterLast()) {
          final String fullname = cursor.getString(INDEX_NAME);
          final int freq;
          // in contacts, the frequency is a bit tricky:
          // stared contacts are really high
          final boolean isStarred = cursor.getInt(INDEX_STARRED) > 0;
          if (isStarred) {
            freq = MAX_WORD_FREQUENCY; // WOW! important!
          } else {
            // times contacted will be our frequency
            final int frequencyContacted = cursor.getInt(INDEX_TIMES);
            // A contact is a valid word in a language, and it usually very frequent.
            final int minimumAdjustedFrequencyContacted =
                Math.max(MINIMUM_CONTACT_WORD_FREQUENCY, frequencyContacted);
            // but no more than the max allowed
            freq = Math.min(minimumAdjustedFrequencyContacted, MAX_WORD_FREQUENCY);
          }
          if (!listener.onWordRead(fullname, freq)) break;
          cursor.moveToNext();
        }
      }
    }
  }

  @Override
  protected void addWordFromStorageToMemory(String name, int frequency) {
    // the word in Contacts is actually the full name,
    // so, let's break it to individual words.
    int len = name.length();

    // TODO: Better tokenization for non-Latin writing systems
    String previousNamePart = null;
    for (int i = 0; i < len; i++) {
      if (Character.isLetter(name.charAt(i))) {
        int j;
        for (j = i + 1; j < len; j++) {
          char c = name.charAt(j);

          if (c != '-' && c != QUOTE && c != CURLY_QUOTE && !Character.isLetter(c)) {
            break;
          }
        }

        String namePart = name.substring(i, j);
        i = j - 1;

        // Safeguard against adding really long
        // words. Stack
        // may overflow due to recursion
        // Also don't add single letter words,
        // possibly confuses
        // capitalization of i.
        final int namePartLength = namePart.length();
        if (namePartLength < MAX_WORD_LENGTH && namePartLength > 1) {
          // adding to next-namePart dictionary
          if (previousNamePart != null) {
            Map<String, NextWord> nextWords;
            if (mLoadingPhaseNextNames.containsKey(previousNamePart)) {
              nextWords = mLoadingPhaseNextNames.get(previousNamePart);
            } else {
              nextWords = new ArrayMap<>();
              mLoadingPhaseNextNames.put(previousNamePart, nextWords);
            }

            if (nextWords.containsKey(namePart)) nextWords.get(namePart).markAsUsed();
            else nextWords.put(namePart, new NextWord(namePart));
          }

          int oldFrequency = getWordFrequency(namePart);
          // ensuring that frequencies do not go lower
          if (oldFrequency < frequency) {
            super.addWordFromStorageToMemory(namePart, frequency);
          }
        }
        // remembering this for the next loop
        previousNamePart = namePart;
      }
    }
  }

  @Override
  protected void deleteWordFromStorage(String word) {
    // not going to support deletion of contacts!
  }

  @Override
  protected void addWordToStorage(String word, int frequency) {
    // not going to support addition of contacts!
  }

  @Override
  protected void closeStorage() {
    /*nothing to close here*/
  }

  @Override
  public void notifyNextTypedWord(@NonNull String currentWord) {
    /*not learning in this dictionary*/
  }

  @Override
  @NonNull
  public Iterable<String> getNextWords(
      @NonNull String currentWord, int maxResults, int minWordUsage) {
    if (mNextNameParts.containsKey(currentWord)) {
      return Arrays.asList(mNextNameParts.get(currentWord));
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public void resetSentence() {
    /*no-op*/
  }
}
