package com.anysoftkeyboard;

import android.os.Looper;
import androidx.annotation.NonNull;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import java.lang.reflect.Method;
import net.evendanan.testgrouping.TestClassHashingStrategy;
import net.evendanan.testgrouping.TestsGroupingFilter;
import org.junit.runners.model.InitializationError;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.android.util.concurrent.PausedExecutorService;
import org.robolectric.util.Logger;

/** Just a way to add general things on-top RobolectricTestRunner. */
public class AnySoftKeyboardRobolectricTestRunner extends RobolectricTestRunner {
  public AnySoftKeyboardRobolectricTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
    TestsGroupingFilter.addTestsGroupingFilterWithSystemPropertiesData(
        this, new TestClassHashingStrategy(), false /*so running from AS will work*/);
  }

  @NonNull
  @Override
  protected Class<? extends TestLifecycle> getTestLifecycleClass() {
    return AnySoftKeyboardRobolectricTestLifeCycle.class;
  }

  public static class AnySoftKeyboardRobolectricTestLifeCycle extends DefaultTestLifecycle {
    @Override
    public void beforeTest(Method method) {
      Logger.info("***** Starting test '%s' *****", method);
      TestRxSchedulers.setSchedulers(Looper.getMainLooper(), new PausedExecutorService());
      super.beforeTest(method);
    }

    @Override
    public void afterTest(Method method) {
      Logger.info("***** Finished test '%s' *****", method);
      super.afterTest(method);
      TestRxSchedulers.destroySchedulers();
    }
  }
}
