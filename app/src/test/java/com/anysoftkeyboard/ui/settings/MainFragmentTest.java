package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;

public class MainFragmentTest extends RobolectricFragmentTestCase<MainFragment> {

    @NonNull
    @Override
    protected MainFragment createFragment() {
        return new MainFragment();
    }

    @Test
    public void testShowsChangelog() throws Exception {
        MainFragment fragment = startFragment();
        final Fragment changeLogFragment = fragment.getChildFragmentManager().findFragmentById(R.id.change_log_fragment);
        Assert.assertNotNull(changeLogFragment);
        Assert.assertTrue(changeLogFragment.isVisible());
        Assert.assertEquals(View.VISIBLE, changeLogFragment.getView().getVisibility());
    }

    @Test
    public void testAboutMenuCommand() throws Exception {
        final MainFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.about_menu_option);
        Assert.assertNotNull(item);
        Assert.assertTrue(item.isVisible());

        fragment.onOptionsItemSelected(item);
        Robolectric.flushForegroundThreadScheduler();

        Fragment aboutFragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertNotNull(aboutFragment);
        Assert.assertTrue(aboutFragment instanceof AboutAnySoftKeyboardFragment);
    }

    @Test
    public void testTweaksMenuCommand() throws Exception {
        final MainFragment fragment = startFragment();
        final FragmentActivity activity = fragment.getActivity();

        Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNotNull(menu);
        final MenuItem item = menu.findItem(R.id.tweaks_menu_option);
        Assert.assertNotNull(item);
        Assert.assertTrue(item.isVisible());

        fragment.onOptionsItemSelected(item);
        Robolectric.flushForegroundThreadScheduler();

        Fragment aboutFragment = activity.getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
        Assert.assertNotNull(aboutFragment);
        Assert.assertTrue(aboutFragment instanceof MainTweaksFragment);
    }
}