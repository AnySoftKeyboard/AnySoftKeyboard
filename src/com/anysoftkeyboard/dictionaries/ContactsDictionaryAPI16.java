/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anysoftkeyboard.dictionaries;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;

import com.anysoftkeyboard.utils.Log;

@TargetApi(16)
public class ContactsDictionaryAPI16 extends ContactsDictionary {

	public ContactsDictionaryAPI16(Context context) throws Exception {
		super(context);
	}

	@Override
	protected ContentObserver createContactContectObserver() {
		return new ContentObserver(null) {

			@Override
			public void onChange(boolean selfChange, Uri uri) {
				Log.d(TAG, "Contacts list modified (self: " + selfChange
							+ ", uri: " + uri + "). Reloading...");
				super.onChange(selfChange, uri);
				loadDictionary();
			}

			@Override
			public void onChange(boolean selfChange) {
				Log.d(TAG, "Contacts list modified (self: " + selfChange
							+ "). Reloading...");
				super.onChange(selfChange);
				loadDictionary();
			}

			@Override
			public boolean deliverSelfNotifications() {
				return true;
			}
		};
	}

}
