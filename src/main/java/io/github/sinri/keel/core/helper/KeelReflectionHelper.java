package io.github.sinri.keel.core.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @since 2.6
 */
public class KeelReflectionHelper {
    private static final KeelReflectionHelper instance = new KeelReflectionHelper();

    private KeelReflectionHelper() {

    }

    public static KeelReflectionHelper getInstance() {
        return instance;
    }

    /**
     * @param <T> class of target annotation
     * @return target annotation
     * @since 1.13
     */
    public <T extends Annotation> T getAnnotationOfMethod(Method method, Class<T> classOfAnnotation, T defaultAnnotation) {
        T annotation = method.getAnnotation(classOfAnnotation);
        if (annotation == null) {
            return defaultAnnotation;
        }
        return annotation;
    }

    /**
     * @since 2.6
     */
    public <T extends Annotation> T getAnnotationOfMethod(Method method, Class<T> classOfAnnotation) {
        return getAnnotationOfMethod(method, classOfAnnotation, null);
    }

    /**
     * @return Returns this element's annotation for the specified type if such an annotation is present, else null.
     * @throws NullPointerException – if the given annotation class is null
     * Note that any annotation returned by this method is a declaration annotation.
     * @since 2.8
     */
    public <T extends Annotation> T getAnnotationOfClass(Class<?> anyClass, Class<T> classOfAnnotation) {
        return anyClass.getAnnotation(classOfAnnotation);
    }
}
