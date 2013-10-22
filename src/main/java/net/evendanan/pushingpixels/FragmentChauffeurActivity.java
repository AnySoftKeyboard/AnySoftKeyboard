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

package net.evendanan.pushingpixels;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.menny.android.anysoftkeyboard.R;

public abstract class FragmentChauffeurActivity extends ActionBarActivity {

    public static enum FragmentUiContext {
        RootFragment,
        DeeperExperience,
        ExpandedItem,
        IncomingAlert
    }

    private static final String TAG = "chauffeur";

    protected abstract int getFragmentRootUiElementId();

    public void addFragmentToUi(Fragment fragment, FragmentUiContext experience) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        //note: the animation should be declared before the fragment replace call, so the transaction will know to which fragment change it should be associated with.
        switch (experience) {
            case RootFragment:
                transaction.setCustomAnimations(R.anim.ui_context_root_add_in, R.anim.ui_context_root_add_out,
                        R.anim.ui_context_root_pop_in, R.anim.ui_context_root_pop_out);
                break;
            case DeeperExperience:
                transaction.setCustomAnimations(R.anim.ui_context_deeper_add_in, R.anim.ui_context_deeper_add_out,
                        R.anim.ui_context_deeper_pop_in, R.anim.ui_context_deeper_pop_out);
                break;
            case ExpandedItem:
                //ideally, the source of the animation would be the View which we expanding
                //consider having 9 animations sets: one for each quadrant of the screen, each of the sets will have
                //a pivotX, pivotY:
                //1 2 3
                //4 5 6
                //7 8 9
                transaction.setCustomAnimations(R.anim.ui_context_expand_add_in, R.anim.ui_context_expand_add_out,
                        R.anim.ui_context_expand_pop_in, R.anim.ui_context_expand_pop_out);
                break;
            case IncomingAlert:
                transaction.setCustomAnimations(R.anim.ui_context_dialog_add_in, R.anim.ui_context_dialog_add_out,
                        R.anim.ui_context_dialog_pop_in, R.anim.ui_context_dialog_pop_out);
                break;
            default:
                Log.wtf(TAG, "I don't know what is this UI experience type: " + experience);
                break;
        }
        //these two calls will make sure the back-button will switch to previous fragment
        transaction.replace(getFragmentRootUiElementId(), fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
