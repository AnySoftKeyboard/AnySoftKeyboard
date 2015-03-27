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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.anysoftkeyboard.utils.Log;
import com.menny.android.anysoftkeyboard.R;

public abstract class FragmentChauffeurActivity extends ActionBarActivity {

    public static enum FragmentUiContext {
        RootFragment,
        DeeperExperience,
        ExpandedItem,
        IncomingAlert
    }

    private static final String TAG = "chauffeur";

    private static final String ROOT_FRAGMENT_TAG = "FragmentChauffeurActivity_ROOT_FRAGMENT_TAG";

    private static final String KEY_FRAGMENT_CLASS_TO_ADD = "KEY_FRAGMENT_CLASS_TO_ADD";
    private static final String KEY_FRAGMENT_ARGS_TO_ADD = "KEY_FRAGMENT_ARGS_TO_ADD";
	private static final String KEY_FRAGMENT_AS_ROOT = "KEY_FRAGMENT_AS_ROOT";

    public static void addIntentArgsForAddingFragmentToUi(@NonNull Intent intent, @NonNull Class<? extends Fragment> fragmentClass, @Nullable Bundle fragmentArgs) {
        intent.putExtra(KEY_FRAGMENT_CLASS_TO_ADD, fragmentClass);
        if (fragmentArgs != null) {
	        intent.putExtra(KEY_FRAGMENT_ARGS_TO_ADD, fragmentArgs);
        }
	    intent.putExtra(KEY_FRAGMENT_AS_ROOT, false);
    }

	public static void addIntentArgsForSettingRootFragmentToUi(@NonNull Intent intent, @NonNull Class<? extends Fragment> fragmentClass, @Nullable Bundle fragmentArgs) {
		intent.putExtra(KEY_FRAGMENT_CLASS_TO_ADD, fragmentClass);
		if (fragmentArgs != null) {
			intent.putExtra(KEY_FRAGMENT_ARGS_TO_ADD, fragmentArgs);
		}
		intent.putExtra(KEY_FRAGMENT_AS_ROOT, true);
	}

	private boolean mIsActivityShown = false;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
	    mIsActivityShown = true;
	    if (savedInstanceState == null) {
		    Bundle activityArgs = getIntent().getExtras();
		    if (activityArgs == null || (!activityArgs.containsKey(KEY_FRAGMENT_CLASS_TO_ADD)) || (!activityArgs.getBoolean(KEY_FRAGMENT_AS_ROOT, false))) {
		        //setting up the root of the UI.
		        setRootFragment(createRootFragmentInstance());
	        }
        }
    }

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleFragmentIntentValues();
	}

	private void handleFragmentIntentValues() {
		Bundle activityArgs = getIntent().getExtras();
		if (activityArgs != null && activityArgs.containsKey(KEY_FRAGMENT_CLASS_TO_ADD)) {
		    Class<? extends Fragment> fragmentClass = (Class<? extends Fragment>) activityArgs.get(KEY_FRAGMENT_CLASS_TO_ADD);
		    //not sure that this is a best-practice, but I still need to remove this from the activity's args
		    activityArgs.remove(KEY_FRAGMENT_CLASS_TO_ADD);
		    try {
		        Fragment fragment = fragmentClass.newInstance();
		        if (activityArgs.containsKey(KEY_FRAGMENT_ARGS_TO_ADD)) {
		            fragment.setArguments(activityArgs.getBundle(KEY_FRAGMENT_ARGS_TO_ADD));
		            activityArgs.remove(KEY_FRAGMENT_CLASS_TO_ADD);
		        }
			    if (activityArgs.getBoolean(KEY_FRAGMENT_AS_ROOT, false)) {
				    setRootFragment(fragment);
			    } else {
				    addFragmentToUi(fragment, FragmentUiContext.RootFragment);
			    }
		    } catch (InstantiationException e) {
		        e.printStackTrace();
		    } catch (IllegalAccessException e) {
		        e.printStackTrace();
		    }
		}
	}

	protected abstract int getFragmentRootUiElementId();

    protected abstract Fragment createRootFragmentInstance();

    public void returnToRootFragment() {
	    if (!mIsActivityShown) return;

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

    public void addFragmentToUi(@NonNull Fragment fragment, FragmentUiContext experience) {
        addFragmentToUi(fragment, experience, null);
    }

    /**
     * Adds the given fragment into the UI using the specified UI-context animation.
     *
     * @param fragment      any generic Fragment. For the ExpandedItem animation it is best to use a PassengerFragment
     * @param experience
     * @param originateView a hint view which will be used to fine-tune the ExpandedItem animation
     */
    public void addFragmentToUi(@NonNull Fragment fragment, FragmentUiContext experience, @Nullable View originateView) {
	    if (!mIsActivityShown) return;

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
                if (fragment instanceof Passengerable && originateView != null) {
                    View fragmentParent = findViewById(getFragmentRootUiElementId());

                    // Idea taken from:
                    // http://developer.android.com/training/animation/zoom.html
                    final float scaleX = ((float) originateView.getWidth())
                            / ((float) fragmentParent.getWidth());
                    final float scaleY = ((float) originateView.getHeight())
                            / ((float) fragmentParent.getHeight());
                    // some preparations
                    // the Y pivot is tricky, it should be the middle of the button, but in
                    // the fragmentParent coordinates
                    int[] originateLocation = new int[2];
                    originateView.getLocationInWindow(originateLocation);
                    int[] parentLocation = new int[2];
                    fragmentParent.getLocationInWindow(parentLocation);
                    final int pivotY = originateLocation[1] - parentLocation[1] + (originateView.getHeight() / 2);
                    final int pivotX = originateLocation[0] - parentLocation[0] + (originateView.getWidth() / 2);

                    Passengerable passengerFragment = (Passengerable) fragment;
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
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //the UI is empty. I can safely finish the activity
            finish();
        }
    }

	@Override
	protected void onStart() {
		super.onStart();
		mIsActivityShown = true;
		//now, checking if there is a request to add a fragment on-top of this one.
		handleFragmentIntentValues();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mIsActivityShown = false;
	}

	public final boolean isChaufferActivityVisible() {
		return mIsActivityShown;
	}
}
