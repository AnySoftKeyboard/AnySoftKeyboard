package com.anysoftkeyboard.ui.settings;

import static com.anysoftkeyboard.TestableAnySoftKeyboard.createEditorInfoTextWithSuggestions;
import static com.anysoftkeyboard.keyboards.KeyboardSwitcher.INPUT_MODE_TEXT;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.TestableAnySoftKeyboard;
import com.anysoftkeyboard.addons.SupportTest;
import com.anysoftkeyboard.api.KeyCodes;
import com.anysoftkeyboard.keyboards.views.DemoAnyKeyboardView;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class SingleSelectionAddOnsBrowserFragmentTest extends RobolectricFragmentTestCase<KeyboardThemeSelectorFragment> {

    @NonNull
    @Override
    protected KeyboardThemeSelectorFragment createFragment() {
        return new KeyboardThemeSelectorFragment();
    }

    @Test
    public void testHasDemoKeyboardView() {
        Fragment fragment = startFragment();
        View demoView = fragment.getView().findViewById(R.id.demo_keyboard_view);
        Assert.assertNotNull(demoView);
        Assert.assertEquals(View.VISIBLE, demoView.getVisibility());
        Assert.assertTrue(demoView instanceof DemoAnyKeyboardView);
    }

    @Test
    @Config(qualifiers = "land")
    public void testHasDemoKeyboardViewInLandscape() {
        Fragment fragment = startFragment();
        View demoView = fragment.getView().findViewById(R.id.demo_keyboard_view);
        Assert.assertNotNull(demoView);
        Assert.assertEquals(View.VISIBLE, demoView.getVisibility());
        Assert.assertTrue(demoView instanceof DemoAnyKeyboardView);
    }

    @Test
    @Ignore
    public void testDemoKeyboardShowsLastUsedKeyboardAlphabet() {
        SupportTest.ensureKeyboardAtIndexEnabled(0, true);
        SupportTest.ensureKeyboardAtIndexEnabled(1, true);

        TestableAnySoftKeyboard service = Robolectric.setupService(TestableAnySoftKeyboard.class);
        service.getKeyboardSwitcherForTests().setKeyboardMode(INPUT_MODE_TEXT, createEditorInfoTextWithSuggestions(), false);
        service.simulateTextTyping("start");

        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", service.getCurrentKeyboardForTests().getKeyboardId());
        service.simulateKeyPress(KeyCodes.KEYBOARD_CYCLE);
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", service.getCurrentKeyboardForTests().getKeyboardId());

        Fragment fragment = startFragment();
        View demoView = fragment.getView().findViewById(R.id.demo_keyboard_view);
        Assert.assertNotNull(demoView);
        Assert.assertEquals(View.VISIBLE, demoView.getVisibility());
        Assert.assertTrue(demoView instanceof DemoAnyKeyboardView);

        DemoAnyKeyboardView demoAnyKeyboardView = (DemoAnyKeyboardView) demoView;
        Assert.assertEquals("12335055-4aa6-49dc-8456-c7d38a1a5123", demoAnyKeyboardView.getKeyboard().getKeyboardId());
    }

    @Test
    @Ignore
    public void testDemoKeyboardShowsLastUsedKeyboardSymbols() {
        TestableAnySoftKeyboard service = Robolectric.setupService(TestableAnySoftKeyboard.class);
        service.getKeyboardSwitcherForTests().setKeyboardMode(INPUT_MODE_TEXT, createEditorInfoTextWithSuggestions(), false);
        service.simulateTextTyping("start");

        Assert.assertEquals("c7535083-4fe6-49dc-81aa-c5438a1a343a", service.getCurrentKeyboardForTests().getKeyboardId());
        service.simulateKeyPress(KeyCodes.KEYBOARD_MODE_CHANGE);
        Assert.assertEquals("symbols_keyboard", service.getCurrentKeyboardForTests().getKeyboardId());

        Fragment fragment = startFragment();
        View demoView = fragment.getView().findViewById(R.id.demo_keyboard_view);
        Assert.assertNotNull(demoView);
        Assert.assertEquals(View.VISIBLE, demoView.getVisibility());
        Assert.assertTrue(demoView instanceof DemoAnyKeyboardView);

        DemoAnyKeyboardView demoAnyKeyboardView = (DemoAnyKeyboardView) demoView;
        Assert.assertEquals("symbols_keyboard", demoAnyKeyboardView.getKeyboard().getKeyboardId());
    }

    @Test
    public void testHasListShadow() {
        Fragment fragment = startFragment();
        View foreground = fragment.getView().findViewById(R.id.list_foreground);
        Assert.assertNotNull(foreground);
        Assert.assertEquals(View.VISIBLE, foreground.getVisibility());
        Assert.assertTrue(foreground instanceof FrameLayout);
        FrameLayout frameLayout = (FrameLayout) foreground;
        Assert.assertEquals(R.drawable.drop_shadow_for_top, Shadows.shadowOf(frameLayout.getForeground()).getCreatedFromResId());
        Assert.assertEquals(Gravity.TOP | Gravity.FILL_HORIZONTAL, frameLayout.getForegroundGravity());
    }

    @Test
    @Config(qualifiers = "land")
    public void testHasListShadowInLandscape() {
        Fragment fragment = startFragment();
        View foreground = fragment.getView().findViewById(R.id.list_foreground);
        Assert.assertNotNull(foreground);
        Assert.assertEquals(View.VISIBLE, foreground.getVisibility());
        Assert.assertTrue(foreground instanceof FrameLayout);
        FrameLayout frameLayout = (FrameLayout) foreground;
        Assert.assertEquals(R.drawable.drop_shadow_for_left, Shadows.shadowOf(frameLayout.getForeground()).getCreatedFromResId());
        Assert.assertEquals(Gravity.LEFT | Gravity.FILL_VERTICAL, frameLayout.getForegroundGravity());
    }

    @Test
    public void testRootIsVertical() {
        Fragment fragment = startFragment();
        View rootView = fragment.getView().findViewById(R.id.add_on_selection_root);
        Assert.assertNotNull(rootView);
        Assert.assertTrue(rootView instanceof LinearLayout);
        Assert.assertEquals(LinearLayout.VERTICAL, ((LinearLayout) rootView).getOrientation());
    }

    @Test
    @Config(qualifiers = "land")
    public void testRootIsHorizontalInLandscape() {
        Fragment fragment = startFragment();
        View rootView = fragment.getView().findViewById(R.id.add_on_selection_root);
        Assert.assertNotNull(rootView);
        Assert.assertTrue(rootView instanceof LinearLayout);
        Assert.assertEquals(LinearLayout.HORIZONTAL, ((LinearLayout) rootView).getOrientation());
    }

    @Test
    public void testLayoutIsNotWeightedInPortrait() {
        Fragment fragment = startFragment();
        LinearLayout rootView = (LinearLayout) fragment.getView().findViewById(R.id.add_on_selection_root);
        Assert.assertEquals(-1f, rootView.getWeightSum(), 0f);

        View demoRoot = fragment.getView().findViewById(R.id.demo_keyboard_view_background);
        Assert.assertEquals(LinearLayout.LayoutParams.MATCH_PARENT, demoRoot.getLayoutParams().width);
        Assert.assertEquals(LinearLayout.LayoutParams.WRAP_CONTENT, demoRoot.getLayoutParams().height);

        View listRoot = fragment.getView().findViewById(R.id.list_foreground);
        Assert.assertEquals(LinearLayout.LayoutParams.MATCH_PARENT, listRoot.getLayoutParams().width);
        Assert.assertEquals(LinearLayout.LayoutParams.MATCH_PARENT, listRoot.getLayoutParams().height);
    }

    @Test
    @Config(qualifiers = "land")
    public void testLayoutIsWeightedInLandscape() {
        Fragment fragment = startFragment();
        LinearLayout rootView = (LinearLayout) fragment.getView().findViewById(R.id.add_on_selection_root);
        Assert.assertEquals(2f, rootView.getWeightSum(), 0f);

        View demoRoot = fragment.getView().findViewById(R.id.demo_keyboard_view_background);
        Assert.assertEquals(0, demoRoot.getLayoutParams().width);
        Assert.assertEquals(LinearLayout.LayoutParams.WRAP_CONTENT, demoRoot.getLayoutParams().height);
        Assert.assertEquals(1f, ((LinearLayout.LayoutParams) demoRoot.getLayoutParams()).weight, 0f);

        View listRoot = fragment.getView().findViewById(R.id.list_foreground);
        Assert.assertEquals(0, listRoot.getLayoutParams().width);
        Assert.assertEquals(LinearLayout.LayoutParams.MATCH_PARENT, listRoot.getLayoutParams().height);
        Assert.assertEquals(1f, ((LinearLayout.LayoutParams) listRoot.getLayoutParams()).weight, 0f);
    }

    @Test
    public void testHasTweaksAndMarket() {
        KeyboardThemeSelectorFragment fragment = startFragment();
        Assert.assertNotEquals(0, fragment.getMarketSearchTitle());

        Menu menu = Shadows.shadowOf(fragment.getActivity()).getOptionsMenu();
        Assert.assertNotNull(menu);
        Assert.assertNotNull(menu.findItem(R.id.tweaks_menu_option));
        Assert.assertTrue(menu.findItem(R.id.tweaks_menu_option).isVisible());

        Assert.assertNotNull(menu);
        Assert.assertNotNull(menu.findItem(R.id.add_on_market_search_menu_option));
        Assert.assertTrue(menu.findItem(R.id.add_on_market_search_menu_option).isVisible());
    }
}