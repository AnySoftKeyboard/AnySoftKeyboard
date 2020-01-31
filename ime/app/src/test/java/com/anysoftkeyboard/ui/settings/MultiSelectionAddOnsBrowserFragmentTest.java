package com.anysoftkeyboard.ui.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.View;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MultiSelectionAddOnsBrowserFragmentTest
        extends RobolectricFragmentTestCase<KeyboardAddOnBrowserFragment> {

    @NonNull
    @Override
    protected KeyboardAddOnBrowserFragment createFragment() {
        return new KeyboardAddOnBrowserFragment();
    }

    @Test
    public void testNoDemoKeyboardViewAtRoot() {
        KeyboardAddOnBrowserFragment fragment = startFragment();
        View demoView = fragment.getView().findViewById(R.id.demo_keyboard_view);
        Assert.assertNull(demoView);
    }

    @Test
    @Config(qualifiers = "w480dp-h800dp-land-mdpi")
    public void testNoDemoKeyboardViewInLandscape() {
        Fragment fragment = startFragment();
        View demoView = fragment.getView().findViewById(R.id.demo_keyboard_view);
        Assert.assertNull(demoView);
    }

    @Test
    public void testNoListShadow() {
        Fragment fragment = startFragment();
        View foreground = fragment.getView().findViewById(R.id.list_foreground);
        Assert.assertNull(foreground);
    }

    @Test
    @Config(qualifiers = "w480dp-h800dp-land-mdpi")
    public void testNoListShadowInLandscape() {
        Fragment fragment = startFragment();
        View foreground = fragment.getView().findViewById(R.id.list_foreground);
        Assert.assertNull(foreground);
    }

    @Test
    public void testJustRecyclerRoot() {
        Fragment fragment = startFragment();
        View rootView = fragment.getView();
        Assert.assertNotNull(rootView);
        Assert.assertTrue(rootView instanceof RecyclerView);
    }

    @Test
    @Config(qualifiers = "w480dp-h800dp-land-mdpi")
    public void testJustRecyclerInLandscape() {
        Fragment fragment = startFragment();
        View rootView = fragment.getView();
        Assert.assertNotNull(rootView);
        Assert.assertTrue(rootView instanceof RecyclerView);
    }

    @Test
    public void testHasTweaksAndMarket() {
        KeyboardAddOnBrowserFragment fragment = startFragment();
        Assert.assertNotEquals(0, fragment.getMarketSearchTitle());
        Menu menu = Shadows.shadowOf(fragment.getActivity()).getOptionsMenu();
        Assert.assertNotNull(menu);
        Assert.assertNotNull(menu.findItem(R.id.tweaks_menu_option));
        Assert.assertFalse(menu.findItem(R.id.tweaks_menu_option).isVisible());

        Assert.assertNotNull(menu);
        Assert.assertNotNull(menu.findItem(R.id.add_on_market_search_menu_option));
        Assert.assertTrue(menu.findItem(R.id.add_on_market_search_menu_option).isVisible());
    }
}
