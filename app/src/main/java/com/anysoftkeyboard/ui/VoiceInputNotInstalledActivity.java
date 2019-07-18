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

package com.anysoftkeyboard.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.menny.android.anysoftkeyboard.R;

public class VoiceInputNotInstalledActivity extends Activity implements android.view.View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.voice_input_not_installed);
        findViewById(R.id.install_button).setOnClickListener(this);
        findViewById(R.id.no_button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.install_button) {
            Intent search = new Intent(Intent.ACTION_VIEW);
            search.setData(Uri.parse("market://search?q=pname:com.google.android.voicesearch"));
            search.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                getApplicationContext().startActivity(search);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getText(R.string.voice_input_not_voice_pack_in_market), Toast.LENGTH_LONG).show();
            }
        }

        finish();
    }
}
