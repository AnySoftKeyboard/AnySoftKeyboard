package com.anysoftkeyboard.ui.settings;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.res.Configuration;
import android.support.v4.app.Fragment;
import android.widget.LinearLayout;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public abstract class BaseSettingsFragmentTest<T extends Fragment>
        extends RobolectricFragmentTestCase<T> {

    @Test
    @Config(qualifiers = "w480dp-h800dp-land-mdpi")
    public void testLandscape() {
        getApplicationContext().getResources().getConfiguration().orientation =
                Configuration.ORIENTATION_LANDSCAPE;
        final T fragment = startFragment();
        final LinearLayout rootView = fragment.getView().findViewById(R.id.settings_root);

        Assert.assertEquals(LinearLayout.HORIZONTAL, rootView.getOrientation());
        Assert.assertEquals(rootView.getChildCount(), rootView.getWeightSum(), 0f);
    }

    @Test
    public void testPortrait() {
        getApplicationContext().getResources().getConfiguration().orientation =
                Configuration.ORIENTATION_PORTRAIT;
        final T fragment = startFragment();
        final LinearLayout rootView = (LinearLayout) fragment.getView();

        Assert.assertEquals(LinearLayout.VERTICAL, rootView.getOrientation());
    }
}
