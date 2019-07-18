package net.evendanan.pixel;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.SeekBar;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class SlidePreferenceTest {

    private TestPrefFragment mTestPrefFragment;
    private SlidePreference mTestSlide;
    private SharedPreferences mSharedPreferences;

    @Before
    public void setup() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final FragmentActivity activity = Robolectric.setupActivity(FragmentActivity.class);
        activity.setContentView(R.layout.test_activity);
        activity.setTheme(R.style.TestApp);
        mTestPrefFragment = new TestPrefFragment();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.root_test_fragment, mTestPrefFragment, "test_fragment")
                .commit();

        Robolectric.flushForegroundThreadScheduler();

        mTestSlide = (SlidePreference) mTestPrefFragment.findPreference("test_slide");
        Assert.assertNotNull(mTestSlide);
    }

    @Test
    public void testCorrectlyReadsAttrs() {
        Assert.assertEquals(12, mTestSlide.getMin());
        Assert.assertEquals(57, mTestSlide.getMax());
        Assert.assertEquals(23, mTestSlide.getValue());
    }

    @Test
    public void testSlideChanges() {
        mTestSlide.onProgressChanged(Mockito.mock(SeekBar.class), 15 /*this is zero-based*/, false);
        Assert.assertEquals(15 + mTestSlide.getMin(), mTestSlide.getValue());
        Assert.assertEquals(15 + mTestSlide.getMin(), mSharedPreferences.getInt("test_slide", 11));
    }

    public static class TestPrefFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.slide_pref_test);
        }
    }
}
