package net.evendanan.testgrouping;

import static net.evendanan.testgrouping.TestsGroupingFilter.TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY;
import static net.evendanan.testgrouping.TestsGroupingFilter.TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY;

import java.util.Collections;
import net.evendanan.testgrouping.inputs.SuiteToTest;
import net.evendanan.testgrouping.inputs.SuiteToTestWithoutShardUsing;
import net.evendanan.testgrouping.inputs.TestClassWithRunnerAnnotation;
import net.evendanan.testgrouping.inputs.TestClassWithTestMethod;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Runner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class ShardingSuiteTest {

  private Class[] mCapturedTestClasses;

  @Before
  public void setup() throws Exception {
    System.clearProperty(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY);
    System.clearProperty(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY);

    final RunnerBuilder runnerBuilder = Mockito.mock(RunnerBuilder.class);
    Mockito.doReturn(Collections.<Runner>emptyList())
        .when(runnerBuilder)
        .runners(Mockito.any(Class.class), Mockito.any(Class[].class));
    final ShardingSuite suiteUnderTest = new ShardingSuite(SuiteToTest.class, runnerBuilder);
    Assert.assertEquals(SuiteToTest.class, suiteUnderTest.getTestClass().getJavaClass());
    ArgumentCaptor<Class[]> testClasses = ArgumentCaptor.forClass(Class[].class);
    Mockito.verify(runnerBuilder).runners(Mockito.eq(SuiteToTest.class), testClasses.capture());
    mCapturedTestClasses = testClasses.getValue();
    Assert.assertEquals(2, mCapturedTestClasses.length);
  }

  @After
  public void tearDown() {
    System.clearProperty(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY);
    System.clearProperty(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY);
  }

  @Test
  public void testDoesNotIncludeClassesWithoutTestAnnotation() {
    for (final Class capturedTestClass : mCapturedTestClasses) {
      Assert.assertFalse(
          capturedTestClass.getName().contains("NoneTestClassWithoutAnyAnnotations"));
    }
  }

  @Test
  public void testDoesNotIncludeClassesWithSuiteAnnotation() {
    for (final Class capturedTestClass : mCapturedTestClasses) {
      Assert.assertFalse(capturedTestClass.getName().contains("NoneTestClassWithSuiteAnnotation"));
    }
  }

  @Test
  public void testDoesNotIncludeAbstractClasses() {
    for (final Class capturedTestClass : mCapturedTestClasses) {
      Assert.assertFalse(
          capturedTestClass.getName().contains("NoneAbstractTestClassWithTestMethod"));
    }
  }

  @Test
  public void testDoesNotIncludeNonPublicClasses() {
    for (final Class capturedTestClass : mCapturedTestClasses) {
      Assert.assertFalse(
          capturedTestClass.getName().contains("NonePackageTestClassWithTestMethod"));
    }
  }

  @Test
  public void testSkipsOutOfRangeHashedClasses() {
    for (final Class capturedTestClass : mCapturedTestClasses) {
      Assert.assertFalse(capturedTestClass.getName().contains("TestClassWithTestMethodToSkip"));
    }
  }

  @Test
  public void testIncludesClassesWithRunWithRunnerAnnotations() {
    Class testClass = null;
    for (final Class capturedTestClass : mCapturedTestClasses) {
      if (capturedTestClass.getName().contains("TestClassWithRunnerAnnotation")) {
        testClass = capturedTestClass;
        break;
      }
    }

    Assert.assertNotNull(testClass);
    Assert.assertEquals(TestClassWithRunnerAnnotation.class, testClass);
  }

  @Test
  public void testIncludesClassesWithMethodsWithTestAnnotation() {
    Class testClass = null;
    for (final Class capturedTestClass : mCapturedTestClasses) {
      if (capturedTestClass.getName().contains("TestClassWithTestMethod")) {
        testClass = capturedTestClass;
        break;
      }
    }

    Assert.assertNotNull(testClass);
    Assert.assertEquals(TestClassWithTestMethod.class, testClass);
  }

  @Test
  public void testShardingCorrectlyGroup0() throws Exception {
    System.setProperty(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY, "2");
    System.setProperty(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY, "0");
    final RunnerBuilder runnerBuilder = Mockito.mock(RunnerBuilder.class);
    Mockito.doReturn(Collections.<Runner>emptyList())
        .when(runnerBuilder)
        .runners(Mockito.any(Class.class), Mockito.any(Class[].class));
    final ShardingSuite suiteUnderTest = new ShardingSuite(SuiteToTest.class, runnerBuilder);
    Assert.assertEquals(SuiteToTest.class, suiteUnderTest.getTestClass().getJavaClass());
    ArgumentCaptor<Class[]> testClasses = ArgumentCaptor.forClass(Class[].class);
    Mockito.verify(runnerBuilder).runners(Mockito.eq(SuiteToTest.class), testClasses.capture());
    mCapturedTestClasses = testClasses.getValue();
    Assert.assertEquals(1, mCapturedTestClasses.length);
    Assert.assertEquals(TestClassWithRunnerAnnotation.class, mCapturedTestClasses[0]);
  }

  @Test
  public void testShardingCorrectlyGroup1() throws Exception {
    System.setProperty(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY, "2");
    System.setProperty(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY, "1");
    final RunnerBuilder runnerBuilder = Mockito.mock(RunnerBuilder.class);
    Mockito.doReturn(Collections.<Runner>emptyList())
        .when(runnerBuilder)
        .runners(Mockito.any(Class.class), Mockito.any(Class[].class));
    final ShardingSuite suiteUnderTest = new ShardingSuite(SuiteToTest.class, runnerBuilder);
    Assert.assertEquals(SuiteToTest.class, suiteUnderTest.getTestClass().getJavaClass());
    ArgumentCaptor<Class[]> testClasses = ArgumentCaptor.forClass(Class[].class);
    Mockito.verify(runnerBuilder).runners(Mockito.eq(SuiteToTest.class), testClasses.capture());
    mCapturedTestClasses = testClasses.getValue();
    Assert.assertEquals(1, mCapturedTestClasses.length);
    Assert.assertEquals(TestClassWithTestMethod.class, mCapturedTestClasses[0]);
  }

  @Test(expected = InitializationError.class)
  public void testMustHashShardingWithAnnotation() throws Exception {
    final RunnerBuilder runnerBuilder = Mockito.mock(RunnerBuilder.class);
    Mockito.doReturn(Collections.<Runner>emptyList())
        .when(runnerBuilder)
        .runners(Mockito.any(Class.class), Mockito.any(Class[].class));
    final ShardingSuite suiteUnderTest =
        new ShardingSuite(SuiteToTestWithoutShardUsing.class, runnerBuilder);
    Assert.assertEquals(SuiteToTest.class, suiteUnderTest.getTestClass().getJavaClass());
  }
}
