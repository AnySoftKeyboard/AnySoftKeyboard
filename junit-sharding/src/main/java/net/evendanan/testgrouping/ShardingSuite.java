package net.evendanan.testgrouping;

import static net.evendanan.testgrouping.TestsGroupingFilter.TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY;
import static net.evendanan.testgrouping.TestsGroupingFilter.TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY;

import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class ShardingSuite extends Suite {

    /**
     * The <code>ShardUsing</code> annotation specifies the {@link HashingStrategy} to be used when
     * sharding the test-classes identified by {@link ShardingSuite}.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Inherited
    public @interface ShardUsing {

        /** Returns the classes to be run */
        Class<? extends HashingStrategy> value();
    }

    /**
     * Called reflectively on classes annotated with <code>@RunWith(Suite.class)</code>
     *
     * @param klass the root class
     * @param builder builds runners for classes in the suite
     */
    public ShardingSuite(final Class<?> klass, final RunnerBuilder builder)
            throws InitializationError {
        super(
                builder,
                klass,
                getClassesForShard(
                        getAllTestClasses(klass), getHashingStrategyFromAnnotation(klass)));
    }

    private static Class<?>[] getClassesForShard(
            final Class<?>[] classes, final Class<? extends HashingStrategy> strategyClass)
            throws InitializationError {
        try {
            final HashingStrategy strategy = strategyClass.getDeclaredConstructor().newInstance();

            final int groupCount;
            final int groupToExecute;
            Properties systemProperties = System.getProperties();
            if (systemProperties.containsKey(TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY)
                    && systemProperties.containsKey(TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY)) {
                groupCount =
                        Integer.parseInt(
                                systemProperties.getProperty(
                                        TEST_GROUPS_COUNT_SYSTEM_PROPERTY_KEY));
                groupToExecute =
                        Integer.parseInt(
                                systemProperties.getProperty(
                                        TEST_GROUP_TO_EXECUTE_SYSTEM_PROPERTY_KEY));
            } else {
                groupCount = 1;
                groupToExecute = 0;
            }

            final ArrayList<Class<?>> classesForShard = new ArrayList<>();
            for (final Class<?> aClass : classes) {
                final Description testDescription =
                        Description.createTestDescription(aClass, "ClassToFilter");
                if (strategy.calculateHashFromDescription(testDescription, groupCount)
                        == groupToExecute) {
                    classesForShard.add(aClass);
                }
            }

            return classesForShard.toArray(new Class<?>[0]);
        } catch (NoSuchMethodException
                | InvocationTargetException
                | InstantiationException
                | IllegalAccessException e) {
            throw new InitializationError(e);
        }
    }

    private static Class<?>[] getAllTestClasses(Class<?> klass) {
        final TreeSet<Class<?>> result = new TreeSet<>(new TestClassNameComparator());
        for (Class<?> clazz : findAllClassesClasses(klass)) {
            if (isTestClass(clazz)) {
                result.add(clazz);
            }
        }
        return result.toArray(new Class<?>[0]);
    }

    private static Class<? extends HashingStrategy> getHashingStrategyFromAnnotation(
            final Class<?> klass) throws InitializationError {
        ShardUsing annotation = klass.getAnnotation(ShardUsing.class);
        if (annotation == null) {
            throw new InitializationError(
                    String.format(
                            Locale.US,
                            "class '%s' must have a ShardUsing annotation",
                            klass.getName()));
        }
        return annotation.value();
    }

    /**
     * The container class has a {@link RunWith} annotation OR the class has at least one method
     * that passes the {@link #isTestMethod} check.
     */
    private static boolean isJunit4Test(Class<?> container) {
        if (container.isAnnotationPresent(RunWith.class)) {
            return true;
        } else {
            for (Method method : container.getMethods()) {
                if (isTestMethod(method)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean isTestMethod(Method method) {
        return Modifier.isPublic(method.getModifiers()) && method.getAnnotation(Test.class) != null;
    }

    private static boolean isAnnotatedWithSuite(Class<?> container) {
        final RunWith runWith = container.getAnnotation(RunWith.class);
        return runWith != null && isSuiteClass(runWith.value());
    }

    private static boolean isSuiteClass(Class<?> clazz) {
        if (clazz == null) {
            return false;
        } else if (clazz == Suite.class) {
            return true;
        } else {
            return isSuiteClass(clazz.getSuperclass());
        }
    }

    /**
     * Determines if a given class is a test class. The default implementation is checking: if it's
     * a Junit class - {@link #isJunit4Test(Class)}. And is not a {@link Suite} runner. And it's a
     * non-abstract, public class.
     *
     * @param container class to test
     * @return {@code true} if the test is a test class.
     */
    private static boolean isTestClass(Class<?> container) {
        return isJunit4Test(container)
                && !isAnnotatedWithSuite(container)
                && Modifier.isPublic(container.getModifiers())
                && !Modifier.isAbstract(container.getModifiers());
    }

    private static class TestClassNameComparator implements Comparator<Class<?>> {

        @Override
        public int compare(Class<?> o1, Class<?> o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /** Finds all classes that live in or below the given package. */
    private static Set<Class<?>> findAllClassesClasses(Class<?> clazz) {
        final String packageName = clazz.getPackage().getName();
        TreeSet<Class<?>> result = new TreeSet<>(new TestClassNameComparator());
        final String packagePrefix = (packageName + '.').replace('/', '.');
        try {
            for (ClassPath.ClassInfo ci : ClassPath.from(clazz.getClassLoader()).getAllClasses()) {
                if (ci.getName().startsWith(packagePrefix)) {
                    try {
                        result.add(ci.load());
                    } catch (UnsatisfiedLinkError | NoClassDefFoundError unused) {
                        // Ignore: we're most likely running on a different platform.
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return result;
    }
}
