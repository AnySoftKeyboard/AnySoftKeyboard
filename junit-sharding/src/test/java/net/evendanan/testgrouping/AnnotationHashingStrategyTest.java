package net.evendanan.testgrouping;

import static org.junit.Assert.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;
import org.mockito.Mockito;

public class AnnotationHashingStrategyTest {
  @Test
  public void calculateHashFromDescription() throws Exception {
    AnnotationHashingStrategy hashingStrategy =
        new AnnotationHashingStrategy(Group1.class, Group2.class);

    Assert.assertEquals(
        0,
        hashingStrategy.calculateHashFromDescription(
            mockDescriptionWithTestClass(TestClass1.class)));
    Assert.assertEquals(
        1,
        hashingStrategy.calculateHashFromDescription(
            mockDescriptionWithTestClass(TestClass2.class)));
    Assert.assertEquals(
        1,
        hashingStrategy.calculateHashFromDescription(
            mockDescriptionWithTestClass(TestClass2b.class)));
    Assert.assertEquals(
        2,
        hashingStrategy.calculateHashFromDescription(
            mockDescriptionWithTestClass(TestClass3.class)));
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Group1 {}

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Group2 {}

  @Group1
  public static class TestClass1 {}

  @Group2
  public static class TestClass2 {}

  @Group2
  public static class TestClass2b {}

  public static class TestClass3 {}

  private static Description mockDescriptionWithTestClass(Class clazz) {
    Description description = Mockito.mock(Description.class);
    Mockito.when(description.getTestClass()).thenReturn(clazz);
    return description;
  }
}
