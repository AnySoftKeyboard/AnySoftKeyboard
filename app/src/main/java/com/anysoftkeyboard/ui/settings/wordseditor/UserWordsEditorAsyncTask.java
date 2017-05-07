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

package com.anysoftkeyboard.ui.settings.wordseditor;
/*Using this import require an Android Library reference from https://github.com/menny/PushingPixels*/

import net.evendanan.pushingpixels.AsyncTaskWithProgressWindow;

public abstract class UserWordsEditorAsyncTask extends AsyncTaskWithProgressWindow<Void, Void, Void, UserDictionaryEditorFragment> {

    protected UserWordsEditorAsyncTask(UserDictionaryEditorFragment userDictionaryEditor, boolean showProgressDialog) {
        super(userDictionaryEditor, showProgressDialog);
    }
}