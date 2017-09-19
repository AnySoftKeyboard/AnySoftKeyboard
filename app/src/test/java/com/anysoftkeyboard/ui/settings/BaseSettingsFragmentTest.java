package com.anysoftkeyboard.ui.settings;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AnySoftKeyboardTestRunner.class)
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

    @NonNull
    protected Fragment navigateByClicking(Fragment rootFragment, int viewToClick) {
        final View viewById = rootFragment.getView().findViewById(viewToClick);
        Assert.assertNotNull(viewById);
        final View.OnClickListener onClickListener = Shadows.shadowOf(viewById).getOnClickListener();
        Assert.assertNotNull(onClickListener);
        onClickListener.onClick(viewById);
        Robolectric.flushForegroundThreadScheduler();
        return rootFragment.getActivity().getSupportFragmentManager().findFragmentById(R.id.main_ui_content);
    }
}