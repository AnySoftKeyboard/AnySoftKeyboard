package com.anysoftkeyboard.addons;

import android.content.Context;
import android.util.AttributeSet;

import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.RobolectricTestRunner;

import java.util.HashSet;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
public class AddOnsFactoryTest {
	private static class TestAddOn extends AddOnImpl {

		protected TestAddOn(Context askContext, Context packageContext, String id, int nameResId, String description, int sortIndex) {
			super(askContext, packageContext, id, nameResId, description, sortIndex);
		}
	}

	private static class TestableAddOnsFactory extends AddOnsFactory<TestAddOn> {

		protected TestableAddOnsFactory() {
			super("ASK_TADF",
					"com.anysoftkeyboard.plugin.QUICK_TEXT_KEY",
					"com.anysoftkeyboard.plugindata.quicktextkeys",
					"QuickTextKeys", "QuickTextKey", R.xml.quick_text_keys, true);
		}

		@Override
		protected TestAddOn createConcreteAddOn(Context askContext, Context context, String prefId, int nameId, String description, int sortIndex, AttributeSet attrs) {
			return new TestAddOn(askContext, context, prefId, nameId, description, sortIndex);
		}
	}

	@Ignore()
	@Test(expected = UnsupportedOperationException.class)
	@Config(reportSdk = 18, emulateSdk = 18)
	public void testGetAllAddOns() throws Exception {
		TestableAddOnsFactory factory = new TestableAddOnsFactory();
		List<TestAddOn> list = factory.getAllAddOns(Robolectric.application);
		Assert.assertTrue(list.size() > 0);

		HashSet<String> seenIds = new HashSet<>();
		for (AddOn addOn : list) {
			Assert.assertNotNull(addOn);
			Assert.assertFalse(seenIds.contains(addOn.getId()));
			seenIds.add(addOn.getId());
		}
	}

	@Ignore()
	@Test(expected = UnsupportedOperationException.class)
	public void testGetAllAddOnsReturnsUnmodifiableList() throws Exception {
		TestableAddOnsFactory factory = new TestableAddOnsFactory();
		List<TestAddOn> list = factory.getAllAddOns(Robolectric.application);

		list.remove(0);
	}
}