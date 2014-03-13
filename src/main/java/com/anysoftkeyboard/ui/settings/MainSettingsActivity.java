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
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.anysoftkeyboard.theme.KeyboardTheme;
import com.anysoftkeyboard.theme.KeyboardThemeFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pushingpixels.EdgeEffectHacker;
import net.evendanan.pushingpixels.FragmentChauffeurActivity;

public class MainSettingsActivity extends FragmentChauffeurActivity {

    private static final String TAG = "ASK_MAIN";
	private static final String SP_KEY_TIMES_MENU_TUTORIAL_SHOWN = "SP_KEY_TIMES_MENU_TUTORIAL_SHOWN";
	private static final int TIMES_MENU_TUTORIAL_TO_BE_SHOWN = 2;
	private static final long TUTORIAL_SHOWING_DELAY = 1500;
	private static final int MSG_KEY_SHOW_MENU_TUTORIAL_POPUP = 2341;

	private DrawerLayout mDrawerRootLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private SharedPreferences.OnSharedPreferenceChangeListener menuExtraUpdaterOnConfigChange = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateMenuExtraData();
        }
    };
	private PopupWindow mTutorialPopup = null;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (!isChaufferActivityVisible()) return;
			switch (msg.what) {
				case MSG_KEY_SHOW_MENU_TUTORIAL_POPUP:
					if (mDrawerRootLayout.isDrawerOpen(Gravity.LEFT)) return;
					mTutorialPopup = new PopupWindow(MainSettingsActivity.this);
					mTutorialPopup.setWindowLayoutMode(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
					View content = getLayoutInflater().inflate(R.layout.tutorial_menu_opening, mDrawerRootLayout, false);
					mTutorialPopup.setContentView(content);
					Drawable background = getResources().getDrawable(R.drawable.lean_dark_popup_keyboard_background);
					mTutorialPopup.setBackgroundDrawable(background);
					mTutorialPopup.setAnimationStyle(R.style.TutorialWindowAnimation);
					mTutorialPopup.showAtLocation(findViewById(getFragmentRootUiElementId()),
							Gravity.TOP, -(background.getIntrinsicWidth()/4), background.getIntrinsicHeight());
					break;
			}
		}
	};

	@Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main_ui);

        mTitle = mDrawerTitle = getTitle();

        mDrawerRootLayout = (DrawerLayout) findViewById(R.id.main_root_layout);
        mDrawerRootLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerRootLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                ActivityCompat.invalidateOptionsMenu(MainSettingsActivity.this);// creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                ActivityCompat.invalidateOptionsMenu(MainSettingsActivity.this);// creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerRootLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        AnyApplication.getConfig().addChangedListener(menuExtraUpdaterOnConfigChange);


	    //menu tutorial
	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
	    int timesMenuTutorialShown = sp.getInt(SP_KEY_TIMES_MENU_TUTORIAL_SHOWN, 0);
	    if (timesMenuTutorialShown < TIMES_MENU_TUTORIAL_TO_BE_SHOWN) {
		    mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_KEY_SHOW_MENU_TUTORIAL_POPUP), TUTORIAL_SHOWING_DELAY);
	    }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
	    //applying my very own Edge-Effect color
	    EdgeEffectHacker.brandGlowEffect(getApplicationContext(), getResources().getColor(R.color.menu_divider));
    }

    @Override
    protected Fragment createRootFragmentInstance() {
        return new MainFragment();
    }

    @Override
    protected int getFragmentRootUiElementId() {
        return R.id.main_ui_content;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //updating menu's data
        updateMenuExtraData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
	    dismissTutorialPopupWindow();
        AnyApplication.getConfig().removeChangedListener(menuExtraUpdaterOnConfigChange);
    }

    private void updateMenuExtraData() {
        TextView keyboardsData = (TextView)findViewById(R.id.keyboards_group_extra_data);
        final int all = KeyboardFactory.getAllAvailableKeyboards(getApplicationContext()).size();
        final int enabled = KeyboardFactory.getEnabledKeyboards(getApplicationContext()).size();
        keyboardsData.setText(getString(R.string.keyboards_group_extra_template, enabled, all));

        TextView themeData = (TextView)findViewById(R.id.theme_extra_data);
        KeyboardTheme theme = KeyboardThemeFactory.getCurrentKeyboardTheme(getApplicationContext());
        if (theme == null)
            theme = KeyboardThemeFactory.getFallbackTheme(getApplicationContext());
        themeData.setText(getString(R.string.selected_add_on_summary, theme.getName()));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
	        dismissTutorialPopupWindow();
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

	private void dismissTutorialPopupWindow() {
		if (mTutorialPopup != null && mTutorialPopup.isShowing()) {
			mTutorialPopup.dismiss();
			markMenuTutorialShown();
		}
		mTutorialPopup = null;
	}

	private void markMenuTutorialShown() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		int timesMenuTutorialShown = sp.getInt(SP_KEY_TIMES_MENU_TUTORIAL_SHOWN, 0);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(SP_KEY_TIMES_MENU_TUTORIAL_SHOWN, timesMenuTutorialShown+1);
		editor.commit();
	}

	@Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    //side menu navigation methods

    public void onNavigateToRootClicked(View v) {
        mDrawerRootLayout.closeDrawers();
        returnToRootFragment();
    }

    public void onNavigateToKeyboardAddonSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new KeyboardAddOnSettingsFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToDictionarySettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new DictionariesFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToLanguageSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AdditionalLanguageSettingsFragment(), FragmentUiContext.RootFragment);

    }

    public void onNavigateToKeyboardThemeSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new KeyboardThemeSelectorFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToEffectsSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new EffectsSettingsFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToGestureSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new GesturesSettingsFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToUserInterfaceSettings(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AdditionalUiSettingsFragment(), FragmentUiContext.RootFragment);
    }

    public void onNavigateToAboutClicked(View v) {
        mDrawerRootLayout.closeDrawers();
        addFragmentToUi(new AboutAnySoftKeyboardFragment(), FragmentUiContext.RootFragment);
    }

    public void setFullScreen(boolean fullScreen) {
        if (fullScreen) {
            getSupportActionBar().hide();
            mDrawerRootLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            getSupportActionBar().show();
            mDrawerRootLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void openDrawer() {
        mDrawerRootLayout.openDrawer(Gravity.LEFT);
    }
}
