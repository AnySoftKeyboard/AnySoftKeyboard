package com.anysoftkeyboard.ui.settings;

import android.support.annotation.NonNull;
import android.support.v7.preference.ListPreference;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;
import com.anysoftkeyboard.RobolectricFragmentTestCase;
import com.anysoftkeyboard.SharedPrefsHelper;
import com.anysoftkeyboard.keyboards.KeyboardAddOnAndBuilder;
import com.anysoftkeyboard.keyboards.KeyboardFactory;
import com.menny.android.anysoftkeyboard.AnyApplication;
import com.menny.android.anysoftkeyboard.R;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.Scheduler;

@RunWith(AnySoftKeyboardTestRunner.class)
public class LanguageTweaksFragmentTest extends RobolectricFragmentTestCase<LanguageTweaksFragment> {

    @NonNull
    @Override
    protected LanguageTweaksFragment createFragment() {
        return new LanguageTweaksFragment();
    }

    @Test
    public void testShowEnabledKeyboardsPlusNoneEntries() {
        Robolectric.getForegroundThreadScheduler().setIdleState(Scheduler.IdleState.CONSTANT_IDLE);
        Robolectric.getBackgroundThreadScheduler().setIdleState(Scheduler.IdleState.CONSTANT_IDLE);

        final KeyboardFactory keyboardFactory = AnyApplication.getKeyboardFactory(RuntimeEnvironment.application);

        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(0, true);
        SharedPrefsHelper.ensureKeyboardAtIndexEnabled(1, true);

        LanguageTweaksFragment fragment = startFragment();
        ListPreference listPreference = (ListPreference) fragment.findPreference(fragment.getString(R.string.settings_key_layout_for_internet_fields));
        Assert.assertNotNull(listPreference);

        Assert.assertEquals(2, keyboardFactory.getEnabledIds().size());
        Assert.assertEquals(3, listPreference.getEntries().length);
        Assert.assertEquals(3, listPreference.getEntryValues().length);
        Assert.assertEquals(keyboardFactory.getEnabledAddOn().getId(), listPreference.getValue());

        Assert.assertEquals("None", listPreference.getEntries()[0]);
        Assert.assertEquals("none", listPreference.getEntryValues()[0]);

        for (int enabledKeyboardIndex = 0; enabledKeyboardIndex < keyboardFactory.getEnabledAddOns().size(); enabledKeyboardIndex++) {
            final KeyboardAddOnAndBuilder builder = keyboardFactory.getEnabledAddOns().get(enabledKeyboardIndex);
            Assert.assertTrue(listPreference.getEntries()[enabledKeyboardIndex + 1].toString().contains(builder.getName()));
            Assert.assertTrue(listPreference.getEntries()[enabledKeyboardIndex + 1].toString().contains(builder.getDescription()));
            Assert.assertEquals(listPreference.getEntryValues()[enabledKeyboardIndex + 1], builder.getId());
        }
    }
}