package com.anysoftkeyboard;

import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.ui.settings.MainSettingsActivity;
import com.menny.android.anysoftkeyboard.R;
import java.util.List;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.LooperMode;

/** Driver for a Fragment unit-tests */
@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
@LooperMode(LooperMode.Mode.LEGACY)
public abstract class RobolectricFragmentTestCase<F extends Fragment>
    extends RobolectricFragmentActivityTestCase<
        RobolectricFragmentTestCase.TestMainSettingsActivity, F> {

  @IdRes
  protected abstract int getStartFragmentNavigationId();

  @Override
  protected Fragment getCurrentFragment() {
    return getCurrentFragmentFromActivity(getActivityController().get());
  }

  @NonNull public static Fragment getCurrentFragmentFromActivity(@NonNull FragmentActivity activity) {
    NavHostFragment navHostFragment =
        (NavHostFragment)
            activity.getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    Assert.assertNotNull(navHostFragment);
    final List<Fragment> fragments = navHostFragment.getChildFragmentManager().getFragments();
    Assert.assertFalse(fragments.isEmpty());
    final Fragment fragment = fragments.get(0);
    Assert.assertNotNull(fragment);
    return fragment;
  }

  @Override
  protected ActivityController<TestMainSettingsActivity> createActivityController() {
    TestMainSettingsActivity.CREATED_FRAGMENT = getStartFragmentNavigationId();
    return ActivityController.of(new TestMainSettingsActivity());
  }

  public static class TestMainSettingsActivity extends MainSettingsActivity {
    @IdRes private static int CREATED_FRAGMENT;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
      super.onPostCreate(savedInstanceState);
      final NavController navController =
          ((NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment))
              .getNavController();
      navController.navigate(CREATED_FRAGMENT);
      TestRxSchedulers.drainAllTasks();
    }
  }
}
