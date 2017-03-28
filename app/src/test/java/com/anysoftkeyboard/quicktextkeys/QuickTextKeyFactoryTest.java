package com.anysoftkeyboard.quicktextkeys;

import android.os.Build;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(AnySoftKeyboardTestRunner.class)
public class QuickTextKeyFactoryTest {

    @Test
    public void testDefaultOrder() {
        List<QuickTextKey> orderAddOns = QuickTextKeyFactory.getOrderedEnabledQuickKeys(RuntimeEnvironment.application);
        Assert.assertEquals(16, orderAddOns.size());
        Assert.assertEquals("698b8c20-19df-11e1-bddb-0800200c9a66", orderAddOns.get(0).getId());
        Assert.assertEquals("085020ea-f496-4c0c-80cb-45ca50635c59", orderAddOns.get(15).getId());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP_MR1)
    public void testCanParseAddOneTypesOfOutputsApi22() {
        List<QuickTextKey> addOns = QuickTextKeyFactory.getAllAvailableQuickKeys(RuntimeEnvironment.application);

        QuickTextKey emoticons = addOns.get(0);
        Assert.assertEquals("698b8c20-19df-11e1-bddb-0800200c9a66", emoticons.getId());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyOutputText());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyLabel());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.LOLLIPOP)
    public void testCanParseAddOneTypesOfOutputsApi21() {
        List<QuickTextKey> addOns = QuickTextKeyFactory.getAllAvailableQuickKeys(RuntimeEnvironment.application);

        QuickTextKey emoticons = addOns.get(0);
        Assert.assertEquals("698b8c20-19df-11e1-bddb-0800200c9a66", emoticons.getId());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyOutputText());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyLabel());
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.N)
    public void testCanParseAddOneTypesOfOutputsApi24() {
        List<QuickTextKey> addOns = QuickTextKeyFactory.getAllAvailableQuickKeys(RuntimeEnvironment.application);

        QuickTextKey emoticons = addOns.get(0);
        Assert.assertEquals("698b8c20-19df-11e1-bddb-0800200c9a66", emoticons.getId());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyOutputText());
        Assert.assertEquals("\uD83D\uDE03", emoticons.getKeyLabel());
    }

    @Test
    public void testOrderStore() {
        List<QuickTextKey> availableQuickKeys = QuickTextKeyFactory.getAllAvailableQuickKeys(RuntimeEnvironment.application);

        List<QuickTextKey> revisedQuickKeys = new ArrayList<>();
        revisedQuickKeys.add(availableQuickKeys.get(10));
        revisedQuickKeys.add(availableQuickKeys.get(1));
        QuickTextKeyFactory.storeOrderedEnabledQuickKeys(RuntimeEnvironment.application, revisedQuickKeys);

        List<QuickTextKey> orderAddOns = QuickTextKeyFactory.getOrderedEnabledQuickKeys(RuntimeEnvironment.application);
        Assert.assertEquals(2, orderAddOns.size());
        Assert.assertEquals(availableQuickKeys.get(10).getId(), orderAddOns.get(0).getId());
        Assert.assertEquals(availableQuickKeys.get(1).getId(), orderAddOns.get(1).getId());

        revisedQuickKeys.clear();
        revisedQuickKeys.add(availableQuickKeys.get(1));
        revisedQuickKeys.add(availableQuickKeys.get(10));
        revisedQuickKeys.add(availableQuickKeys.get(1));
        revisedQuickKeys.add(availableQuickKeys.get(2));
        QuickTextKeyFactory.storeOrderedEnabledQuickKeys(RuntimeEnvironment.application, revisedQuickKeys);

        orderAddOns = QuickTextKeyFactory.getOrderedEnabledQuickKeys(RuntimeEnvironment.application);
        Assert.assertEquals(3, orderAddOns.size());
        Assert.assertEquals(availableQuickKeys.get(1).getId(), orderAddOns.get(0).getId());
        Assert.assertEquals(availableQuickKeys.get(10).getId(), orderAddOns.get(1).getId());
        Assert.assertEquals(availableQuickKeys.get(2).getId(), orderAddOns.get(2).getId());
    }
}