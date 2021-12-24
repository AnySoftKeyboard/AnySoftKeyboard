package com.anysoftkeyboard.ui.settings.setup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.RobolectricFragmentActivityTestCase;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.LEGACY)
public abstract class RobolectricWizardFragmentTestCase<F extends Fragment>
        extends RobolectricFragmentActivityTestCase<
                RobolectricWizardFragmentTestCase.TestableSetupWizardActivity<F>, F> {

    @NonNull
    protected abstract F createFragment();

    @Override
    protected Fragment getCurrentFragment() {
        return getActivityController().get().mFragment;
    }

    @Override
    protected ActivityController<TestableSetupWizardActivity<F>> createActivityController() {
        TestableSetupWizardActivity<F> activity = new TestableSetupWizardActivity<F>();
        activity.mFragment = createFragment();
        return ActivityController.of(activity);
    }

    public static class TestableSetupWizardActivity<F extends Fragment>
            extends SetupWizardActivity {
        private F mFragment;

        @NonNull
        @Override
        protected FragmentPagerAdapter createPagesAdapter() {
            return new FragmentPagerAdapter(getSupportFragmentManager()) {
                @NonNull
                @Override
                public Fragment getItem(int position) {
                    return mFragment;
                }

                @Override
                public int getCount() {
                    return 1;
                }
            };
        }
    }
}
