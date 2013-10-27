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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.menny.android.anysoftkeyboard.R;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class FragmentChauffeurActivity extends ActionBarActivity {

    public static enum FragmentUiContext {
        RootFragment,
        DeeperExperience,
        ExpandedItem,
        IncomingAlert
    }

    private static final String TAG = "chauffeur";

    private static final String ROOT_FRAGMENT_TAG = "FragmentChauffeurActivity_ROOT_FRAGMENT_TAG";

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //setting up the root of the UI.
        setRootFragment(createRootFragmentInstance());
    }

    protected abstract int getFragmentRootUiElementId();

    protected abstract Fragment createRootFragmentInstance();

    public void returnToRootFragment() {
        getSupportFragmentManager().popBackStackImmediate(ROOT_FRAGMENT_TAG, 0 /*don't pop the root*/);
    }

    public void setRootFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(ROOT_FRAGMENT_TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.ui_context_root_add_in, R.anim.ui_context_root_add_out,
                R.anim.ui_context_root_pop_in, R.anim.ui_context_root_pop_out);
        transaction.replace(getFragmentRootUiElementId(), fragment);
        //bookmarking, so I can return easily.
        transaction.addToBackStack(ROOT_FRAGMENT_TAG);
        transaction.commit();
    }

    public void addFragmentToUi(@Nonnull Fragment fragment, FragmentUiContext experience) {
        addFragmentToUi(fragment, experience, null);
    }

    /**
     * Adds the given fragment into the UI using the specified UI-context animation.
     * @param fragment any generic Fragment. For the ExpandedItem animation it is best to use a PassengerFragment
     * @param experience
     * @param originateView a hint view which will be used to fine-tune the ExpandedItem animation
     */
    public void addFragmentToUi(@Nonnull Fragment fragment, FragmentUiContext experience, @Nullable View originateView) {
        if (experience == FragmentUiContext.RootFragment) {
            //in this case, I need to pop all the other fragments till the root.
            returnToRootFragment();
        }
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
                //although this managing Activity can handle any generic Fragment, in this case we'll need some help from the fragment.
                //it is required to fine tune the pivot of the scale animation.
                //so, I'll need the specialized fragment PassengerFragment
                if (fragment instanceof PassengerFragment && originateView != null) {
                    View fragmentParent = findViewById(getFragmentRootUiElementId());
                    float pivotX = ((float)(originateView.getWidth()/2 + originateView.getLeft()) / ((float)fragmentParent.getWidth()));
                    float pivotY = ((float)(originateView.getHeight()/2 + originateView.getTop()) / ((float)fragmentParent.getHeight()));
                    float scaleX = ((float)(originateView.getWidth()) / ((float)fragmentParent.getWidth()));
                    float scaleY = ((float)(originateView.getHeight()) / ((float)fragmentParent.getHeight()));

                    PassengerFragment passengerFragment = (PassengerFragment)fragment;
                    passengerFragment.setItemExpandExtraData(pivotX, pivotY, scaleX, scaleY);
                    transaction.setCustomAnimations(R.anim.ui_context_expand_add_in, R.anim.ui_context_expand_add_out,
                            R.anim.ui_context_expand_pop_in, R.anim.ui_context_expand_pop_out);
                } else {
                    //using the default scale animation, no pivot changes can be done on a generic fragment.
                    transaction.setCustomAnimations(R.anim.ui_context_expand_add_in_default, R.anim.ui_context_expand_add_out,
                            R.anim.ui_context_expand_pop_in, R.anim.ui_context_expand_pop_out_default);
                }
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

    @Override
    public void onBackPressed() {
        //I know I'm doing something wrong here!
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            //This is the last fragment on the stack, so I don't want to pop it and leave the UI empty
            //So, I'm forcibly poping it, and then going on to super.onBackPressed(), which will cause the Activity
            //to finish
            getSupportFragmentManager().popBackStackImmediate();
        }
        super.onBackPressed();
    }
}
