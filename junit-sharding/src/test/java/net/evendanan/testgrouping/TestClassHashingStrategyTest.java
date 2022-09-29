package net.evendanan.testgrouping;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;
import org.mockito.Mockito;

public class TestClassHashingStrategyTest {
    @Test
    public void calculateHashFromDescription() throws Exception {
        TestClassHashingStrategy hashingStrategy = new TestClassHashingStrategy();

        Assert.assertEquals(
                hashingStrategy.calculateHashFromDescription(
                        mockDescriptionWithClassName("class1")),
                hashingStrategy.calculateHashFromDescription(
                        mockDescriptionWithClassName("class1")));

        Assert.assertNotEquals(
                hashingStrategy.calculateHashFromDescription(
                        mockDescriptionWithClassName("class1")),
                hashingStrategy.calculateHashFromDescription(
                        mockDescriptionWithClassName("class2")));
    }

    private static Description mockDescriptionWithClassName(String className) {
        Description description = Mockito.mock(Description.class);
        Mockito.when(description.getClassName()).thenReturn(className);
        return description;
    }
}
