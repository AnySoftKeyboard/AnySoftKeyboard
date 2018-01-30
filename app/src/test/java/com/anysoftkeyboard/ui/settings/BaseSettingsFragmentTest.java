package com.anysoftkeyboard.ui.settings;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public abstract class BaseSettingsFragmentTest<T extends Fragment> extends RobolectricFragmentTestCase<T> {

    @Config(qualifiers = "land")
    @Test
    public void testLandscape() {
        RuntimeEnvironment.application.getResources().getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        final T fragment = startFragment();
        final LinearLayout rootView = fragment.getView().findViewById(R.id.settings_root);

        Assert.assertEquals(LinearLayout.HORIZONTAL, rootView.getOrientation());
        Assert.assertEquals(rootView.getChildCount(), rootView.getWeightSum(), 0f);
    }

    @Test
    public void testPortrait() {
        RuntimeEnvironment.application.getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
        final T fragment = startFragment();
        final LinearLayout rootView = (LinearLayout) fragment.getView();

        Assert.assertEquals(LinearLayout.VERTICAL, rootView.getOrientation());
    }

}