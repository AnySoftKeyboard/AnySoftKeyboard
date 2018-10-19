package com.anysoftkeyboard.quicktextkeys;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.os.Build;

import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import com.menny.android.anysoftkeyboard.AnyApplication;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class QuickTextKeyFactoryTest {

    @Test
    public void testDefaultOrder() {
        List<QuickTextKey> orderAddOns = AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOns();
        Assert.assertEquals(17, orderAddOns.size());
        Assert.assertEquals("698b8c20-19df-11e1-bddb-0800200c9a66", orderAddOns.get(0).getId());
        Assert.assertEquals("085020ea-f496-4c0c-80cb-45ca50635c59", orderAddOns.get(15).getId());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void testCanParseAddOneTypesOfOutputsApi22() {
        List<QuickTextKey> addOns = AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getAllAddOns();

        QuickTextKey emoticons = addOns.get(0);
        Assert.assertEquals("698b8c20-19df-11e1-bddb-0800200c9a66", emoticons.getId());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyOutputText());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyLabel());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void testCanParseAddOneTypesOfOutputsApi21() {
        List<QuickTextKey> addOns = AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getAllAddOns();

        QuickTextKey emoticons = addOns.get(0);
        Assert.assertEquals("698b8c20-19df-11e1-bddb-0800200c9a66", emoticons.getId());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyOutputText());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyLabel());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testCanParseAddOneTypesOfOutputsApi24() {
        List<QuickTextKey> addOns = AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getAllAddOns();

        QuickTextKey emoticons = addOns.get(0);
        Assert.assertEquals("698b8c20-19df-11e1-bddb-0800200c9a66", emoticons.getId());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyOutputText());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyLabel());
    }

    @Test
    public void testOrderStore() {
        List<QuickTextKey> availableQuickKeys = AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getAllAddOns();

        List<QuickTextKey> revisedQuickKeys = new ArrayList<>();
        revisedQuickKeys.add(availableQuickKeys.get(10));
        revisedQuickKeys.add(availableQuickKeys.get(1));
        AnyApplication.getQuickTextKeyFactory(getApplicationContext()).setAddOnsOrder(revisedQuickKeys);

        List<QuickTextKey> orderAddOns = AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOns();
        Assert.assertEquals(17, orderAddOns.size());
        Assert.assertEquals(revisedQuickKeys.get(0).getId(), orderAddOns.get(0).getId());
        Assert.assertEquals(revisedQuickKeys.get(1).getId(), orderAddOns.get(1).getId());

        revisedQuickKeys.clear();
        revisedQuickKeys.add(availableQuickKeys.get(1));
        revisedQuickKeys.add(availableQuickKeys.get(10));
        revisedQuickKeys.add(availableQuickKeys.get(1));
        revisedQuickKeys.add(availableQuickKeys.get(2));
        AnyApplication.getQuickTextKeyFactory(getApplicationContext()).setAddOnsOrder(revisedQuickKeys);

        orderAddOns = AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getAllAddOns();
        Assert.assertEquals(17, orderAddOns.size());
        Assert.assertEquals(revisedQuickKeys.get(0).getId(), orderAddOns.get(0).getId());
        Assert.assertEquals(revisedQuickKeys.get(1).getId(), orderAddOns.get(1).getId());
        //this is a repeat in the re-order, so it is not repeating in the final list
        Assert.assertNotEquals(revisedQuickKeys.get(2).getId(), orderAddOns.get(2).getId());
        Assert.assertEquals(revisedQuickKeys.get(3).getId(), orderAddOns.get(2).getId());
        final CharSequence expected2ndId = orderAddOns.get(3).getId();

        AnyApplication.getQuickTextKeyFactory(getApplicationContext()).setAddOnEnabled(orderAddOns.get(0).getId(), false);
        AnyApplication.getQuickTextKeyFactory(getApplicationContext()).setAddOnEnabled(orderAddOns.get(2).getId(), false);

        orderAddOns = AnyApplication.getQuickTextKeyFactory(getApplicationContext()).getEnabledAddOns();
        Assert.assertEquals(15, orderAddOns.size());
        Assert.assertEquals(revisedQuickKeys.get(1).getId(), orderAddOns.get(0).getId());
        Assert.assertEquals(expected2ndId, orderAddOns.get(1).getId());
    }
}