package com.anysoftkeyboard.addons;

import com.anysoftkeyboard.AnySoftKeyboardTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

@RunWith(AnySoftKeyboardTestRunner.class)
public class AddOnImplTest {

    @Test
    public void testEquals() {
        TestableAddOn addOn1 = new TestableAddOn("id1", "name", 8);
        TestableAddOn addOn2 = new TestableAddOn("id2", "name", 8);
        TestableAddOn addOn11 = new TestableAddOn("id1", "name111", 8);
        TestableAddOn addOn1DifferentApiVersion = new TestableAddOn("id1", "name", 7);

        Assert.assertEquals(addOn1, addOn11);
        Assert.assertNotEquals(addOn1, addOn2);
        Assert.assertEquals(addOn1.hashCode(), addOn11.hashCode());
        Assert.assertNotEquals(addOn1.hashCode(), addOn2.hashCode());
        Assert.assertNotEquals(addOn1, addOn1DifferentApiVersion);

        Assert.assertNotEquals(new Object(), addOn1);
    }

    @Test
    public void testToString() {
        String toString = new TestableAddOn("id1", "name111", 8).toString();

        Assert.assertTrue(toString.contains("name111"));
        Assert.assertTrue(toString.contains("id1"));
        Assert.assertTrue(toString.contains("TestableAddOn"));
        Assert.assertTrue(toString.contains(RuntimeEnvironment.application.getPackageName()));
        Assert.assertTrue(toString.contains("API-8"));
    }

    private static class TestableAddOn extends AddOnImpl {

        protected TestableAddOn(CharSequence id, CharSequence name, int apiVersion) {
            super(RuntimeEnvironment.application, RuntimeEnvironment.application, apiVersion, id, name, name.toString() + id.toString(), false, 1);
        }
    }
}