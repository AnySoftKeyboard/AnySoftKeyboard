package com.anysoftkeyboard.base.utils;

import com.anysoftkeyboard.AnySoftKeyboardPlainTestRunner;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AnySoftKeyboardPlainTestRunner.class)
public class OptionalCompatTest {

    @Test
    public void testPropertiesNull() {
        final OptionalCompat<Object> nullObject = OptionalCompat.of(null);
        Assert.assertNotNull(nullObject);
        Assert.assertNull(nullObject.get());
        Assert.assertTrue(nullObject.isEmpty());
        Assert.assertFalse(nullObject.isPresent());
        Assert.assertEquals("EQUALS", nullObject.getOrElse("EQUALS"));
        Assert.assertNotEquals(OptionalCompat.of("NOT_EQUALS"), nullObject);
        Assert.assertNotEquals(OptionalCompat.of(new Object()), nullObject);
        Assert.assertEquals(OptionalCompat.of(null), nullObject);
        Assert.assertEquals(OptionalCompat.of(null).hashCode(), nullObject.hashCode());
    }

    @Test
    public void testPropertiesNotNull() {
        final OptionalCompat<Integer> nonNullInt = OptionalCompat.of(1);
        Assert.assertNotNull(nonNullInt);
        Assert.assertNotNull(nonNullInt.get());
        Assert.assertFalse(nonNullInt.isEmpty());
        Assert.assertTrue(nonNullInt.isPresent());
        Assert.assertNotEquals(Integer.valueOf(4), nonNullInt.getOrElse(4));
        Assert.assertEquals(Integer.valueOf(1), nonNullInt.getOrElse(4));
        Assert.assertNotEquals(OptionalCompat.of(4), nonNullInt);
        Assert.assertNotEquals(OptionalCompat.of(new Object()), nonNullInt);
        Assert.assertNotEquals(OptionalCompat.of(null), nonNullInt);
        Assert.assertEquals(OptionalCompat.of(1), nonNullInt);
        Assert.assertEquals(OptionalCompat.of(1).hashCode(), nonNullInt.hashCode());
        Assert.assertNotEquals(OptionalCompat.of(4).hashCode(), nonNullInt.hashCode());
        Assert.assertNotEquals(OptionalCompat.of("ggg").hashCode(), nonNullInt.hashCode());
    }

}