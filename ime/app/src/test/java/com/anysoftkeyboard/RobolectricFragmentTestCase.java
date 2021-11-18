package com.anysoftkeyboard;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import org.junit.After;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;

/** Driver for a Fragment unit-tests */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.LEGACY)
public abstract class RobolectricFragmentTestCase<F extends Fragment>
        extends RobolectricFragmentActivityTestCase<
                RobolectricFragmentTestCase.TestMainSettingsActivity, F> {

    @After
    public void afterRobolectricFragmentTestCase() {
        TestMainSettingsActivity.CREATED_FRAGMENT = null;
    }

    @Override
    protected ActivityController<TestMainSettingsActivity> createActivityController(F fragment) {
        TestMainSettingsActivity.CREATED_FRAGMENT = fragment;
        return ActivityController.of(new TestMainSettingsActivity());
    }

    public static class TestMainSettingsActivity extends MainSettingsActivity {
        private static Fragment CREATED_FRAGMENT;

        @NonNull
        @Override
        protected Fragment createRootFragmentInstance() {
            return CREATED_FRAGMENT;
        }
    }
}
