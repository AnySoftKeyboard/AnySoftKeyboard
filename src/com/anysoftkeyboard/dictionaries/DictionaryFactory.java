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

package com.anysoftkeyboard.dictionaries;

import android.content.Context;
import com.anysoftkeyboard.dictionaries.content.ContactsDictionary;
import com.anysoftkeyboard.dictionaries.sqlite.AutoDictionary;
import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.AnyApplication;
import net.evendanan.frankenrobot.Diagram;

public class DictionaryFactory {

    private static final String TAG = "ASK DictFactory";
    private AutoDictionary mAutoDictionary = null;
    private String mUserDictionaryLocale = null;
    private EditableDictionary mUserDictionary = null;

    public DictionaryFactory() {
    }

    private static boolean equalsString(String a, String b) {
        if (a == null && b == null) return true;
        else if (a == null || b == null) return false;
        else return a.equals(b);
    }

    public synchronized EditableDictionary createUserDictionary(Context context, String locale) {
        if (mUserDictionary != null) {
            if (!mUserDictionary.isClosed() && equalsString(mUserDictionaryLocale, locale)) {
                Log.d(TAG, "Returning cached user-dictionary for locale " + mUserDictionaryLocale);
                return mUserDictionary;
            } else {
                mUserDictionary.close();
            }
        }
        Log.d(TAG, "Creating a new UserDictionary for locale " + locale);
        mUserDictionary = new UserDictionary(context, locale);
        DictionaryASyncLoader loader = new DictionaryASyncLoader(null);
        loader.execute(mUserDictionary);
        //this will help us in the really rare case that an access to the dictionary is done before the loading started.
        loader.waitTillLoadingStarted();

        mUserDictionaryLocale = locale;
        return mUserDictionary;
    }

    public synchronized Dictionary createContactsDictionary(Context context) {
        return AnyApplication.getFrankenRobot().embody(new ContactsDictionaryDiagram(context.getApplicationContext()));
    }

    public synchronized AutoDictionary createAutoDictionary(Context context, String currentAutoDictionaryLocale) {
        if (AnyApplication.getConfig().getAutoDictionaryInsertionThreshold() < 0) return null;

        if (mAutoDictionary != null && !mAutoDictionary.isClosed()) {
            if (equalsString(mAutoDictionary.getLocale(), currentAutoDictionaryLocale)) {
                return mAutoDictionary;
            } else {
                //will create a new one shortly.
                mAutoDictionary.close();
            }
        }

        Log.d(TAG, "Creating AutoDictionary for locale: " + currentAutoDictionaryLocale);

        mAutoDictionary = new AutoDictionary(context, currentAutoDictionaryLocale);

        DictionaryASyncLoader loader = new DictionaryASyncLoader(null);
        loader.execute(mAutoDictionary);
        //this will help us in the really rare case that an access to the dictionary is done before the loading started.
        loader.waitTillLoadingStarted();

        return mAutoDictionary;
    }

    public static final class ContactsDictionaryDiagram extends Diagram<ContactsDictionary> {
        private final Context mAppContext;

        public ContactsDictionaryDiagram(Context appContext) {
            mAppContext = appContext;
        }

        public Context getAppContext() {
            return mAppContext;
        }
    }
}
