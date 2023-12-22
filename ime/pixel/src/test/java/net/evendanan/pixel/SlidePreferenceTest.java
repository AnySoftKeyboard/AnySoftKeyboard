package net.evendanan.pixel;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.anysoftkeyboard.rx.TestRxSchedulers;
import com.anysoftkeyboard.test.TestFragmentActivity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SlidePreferenceTest {

  private TestPrefFragment mTestPrefFragment;
  private SlidePreference mTestSlide;
  private SharedPreferences mSharedPreferences;

  private void runTest(Runnable runnable) {
    mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    try (var scenario = ActivityScenario.launch(TestFragmentActivity.class)) {
      scenario.onActivity(
          activity -> {
            activity.setContentView(R.layout.test_activity);
            activity.setTheme(R.style.TestApp);
            mTestPrefFragment = new TestPrefFragment();
            activity
                .getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.root_test_fragment, mTestPrefFragment, "test_fragment")
                .commit();

            TestRxSchedulers.foregroundFlushAllJobs();

            mTestSlide = mTestPrefFragment.findPreference("test_slide");
            Assert.assertNotNull(mTestSlide);

            runnable.run();
          });
    }
  }

  @Test
  public void testCorrectlyReadsAttrs() {
    runTest(
        () -> {
          Assert.assertEquals(12, mTestSlide.getMin());
          Assert.assertEquals(57, mTestSlide.getMax());
          Assert.assertEquals(23, mTestSlide.getValue());
        });
  }

  @Test
  public void testValueTemplateChanges() {
    runTest(
        () -> {
          TextView templateView = mTestPrefFragment.getView().findViewById(R.id.pref_current_value);
          Assert.assertNotNull(templateView);
          Assert.assertEquals("23 milliseconds", templateView.getText().toString());
          mTestSlide.onProgressChanged(
              Mockito.mock(SeekBar.class), 15 /*this is zero-based*/, false);
          Assert.assertEquals("27 milliseconds", templateView.getText().toString());
        });
  }

  @Test
  public void testSlideChanges() {
    runTest(
        () -> {
          mTestSlide.onProgressChanged(
              Mockito.mock(SeekBar.class), 15 /*this is zero-based*/, false);
          Assert.assertEquals(15 + mTestSlide.getMin(), mTestSlide.getValue());
          Assert.assertEquals(
              15 + mTestSlide.getMin(), mSharedPreferences.getInt("test_slide", 11));
        });
  }

  public static class TestPrefFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
      addPreferencesFromResource(R.xml.slide_pref_test);
    }
  }
}
