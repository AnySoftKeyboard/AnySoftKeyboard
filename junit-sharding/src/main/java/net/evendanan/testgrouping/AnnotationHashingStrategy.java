package net.evendanan.testgrouping;

import java.lang.annotation.Annotation;
import org.junit.runner.Description;

/**
 * Groups tests according to a marker {@link Annotation} on the {@link Description#getTestClass()}.
 * Tests with no marching annotation will be grouped into yet another group.
 */
public class AnnotationHashingStrategy extends SimpleHashingStrategyBase {

  private final Class<? extends Annotation>[] mAnnotationGroups;

  /**
   * Constructs a AnnotationHashingStrategy that groups test-classes by a marker annotation. Note:
   * The marker annotations must have {@link java.lang.annotation.RetentionPolicy#RUNTIME}!
   */
  @SafeVarargs
  public AnnotationHashingStrategy(Class<? extends Annotation>... annotationGroups) {
    mAnnotationGroups = annotationGroups;
  }

  @Override
  public int calculateHashFromDescription(Description description) {
    final Class<?> testClass = description.getTestClass();
    int group = 0;
    for (Class<? extends Annotation> annotationGroup : mAnnotationGroups) {
      if (testClass.getAnnotation(annotationGroup) != null) return group;
      group++;
    }

    return group;
  }
}
