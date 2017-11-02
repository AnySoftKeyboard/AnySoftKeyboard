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
        TestableAddOn addOn1 = new TestableAddOn("id1", "name");
        TestableAddOn addOn2 = new TestableAddOn("id2", "name");
        TestableAddOn addOn11 = new TestableAddOn("id1", "name111");

        Assert.assertEquals(addOn1, addOn11);
        Assert.assertNotEquals(addOn1, addOn2);
        Assert.assertEquals(addOn1.hashCode(), addOn11.hashCode());
        Assert.assertNotEquals(addOn1.hashCode(), addOn2.hashCode());

        Assert.assertNotEquals(new Object(), addOn1);
    }

    @Test
    public void testToString() {
        String toString = new TestableAddOn("id1", "name111").toString();

        Assert.assertTrue(toString.contains("name111"));
        Assert.assertTrue(toString.contains("id1"));
        Assert.assertTrue(toString.contains("TestableAddOn"));
        Assert.assertTrue(toString.contains(RuntimeEnvironment.application.getPackageName()));
    }

    private static class TestableAddOn extends AddOnImpl {

        protected TestableAddOn(CharSequence id, CharSequence name) {
            super(RuntimeEnvironment.application, RuntimeEnvironment.application, id, name, name.toString() + id.toString(), false, 1);
        }
    }
}