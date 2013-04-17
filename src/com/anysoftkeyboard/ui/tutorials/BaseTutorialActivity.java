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

package com.anysoftkeyboard.ui.tutorials;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import com.menny.android.anysoftkeyboard.R;

public abstract class BaseTutorialActivity extends Activity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getTitleResId());
        setContentView(getLayoutResId());


        View content = getWindow().getDecorView();
        //now to listen on all known buttons
        setClickHandler(content);
    }

    protected void setClickHandler(View content) {
        if (content instanceof ViewGroup) {
            for (View touchable : content.getTouchables()) {
                setClickHandler(touchable);
            }
        } else {
            if ((content instanceof Button) || (content instanceof ImageButton)) {
                content.setOnClickListener(this);
            }
        }
    }

    protected abstract int getLayoutResId();

    protected abstract int getTitleResId();

    public void onCloseClicked(View view) {
        finish();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_button:
                onCloseClicked(v);
                break;
        }
    }
}
