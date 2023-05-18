import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {
    int priority() default 5;
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface BeforeSuite {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface AfterSuite {}

public class TestRunner {

    public static void start(Class<?> testClass) {
        try {
            Object instance = testClass.getDeclaredConstructor().newInstance();
            Method[] methods = testClass.getDeclaredMethods();
            List<Method> beforeSuiteMethods = new ArrayList<>();
            List<Method> afterSuiteMethods = new ArrayList<>();
            List<Method> testMethods = new ArrayList<>();

            for (Method method : methods) {
                if (method.isAnnotationPresent(BeforeSuite.class)) {
                    beforeSuiteMethods.add(method);
                } else if (method.isAnnotationPresent(AfterSuite.class)) {
                    afterSuiteMethods.add(method);
                } else if (method.isAnnotationPresent(Test.class)) {
                    testMethods.add(method);
                }
            }

            if (beforeSuiteMethods.size() > 1 || afterSuiteMethods.size() > 1) {
                throw new RuntimeException("BeforeSuite and AfterSuite methods should be present in a single instance");
            }

            if (!beforeSuiteMethods.isEmpty()) {
                beforeSuiteMethods.get(0).invoke(instance);
            }

            testMethods.sort(Comparator.comparingInt(m -> m.getAnnotation(Test.class).priority()));

            for (Method method : testMethods) {
                method.invoke(instance);
            }

            if (!afterSuiteMethods.isEmpty()) {
                afterSuiteMethods.get(0).invoke(instance);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}