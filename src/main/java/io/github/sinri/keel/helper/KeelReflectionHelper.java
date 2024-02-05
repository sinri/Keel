package io.github.sinri.keel.helper;

import org.reflections.Reflections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @since 2.6
 */
public class KeelReflectionHelper {
    private static final KeelReflectionHelper instance = new KeelReflectionHelper();

    private KeelReflectionHelper() {

    }

    static KeelReflectionHelper getInstance() {
        return instance;
    }

    /**
     * @param <T> class of target annotation
     * @return target annotation
     * @since 1.13
     */
    @Nullable
    public <T extends Annotation> T getAnnotationOfMethod(@Nonnull Method method, @Nonnull Class<T> classOfAnnotation, @Nullable T defaultAnnotation) {
        T annotation = method.getAnnotation(classOfAnnotation);
        if (annotation == null) {
            return defaultAnnotation;
        }
        return annotation;
    }

    /**
     * @since 2.6
     */
    @Nullable
    public <T extends Annotation> T getAnnotationOfMethod(@Nonnull Method method, @Nonnull Class<T> classOfAnnotation) {
        return getAnnotationOfMethod(method, classOfAnnotation, null);
    }

    /**
     * @return Returns this element's annotation for the specified type if such an annotation is present, else null.
     * @throws NullPointerException – if the given annotation class is null
     *                              Note that any annotation returned by this method is a declaration annotation.
     * @since 2.8
     */
    @Nullable
    public <T extends Annotation> T getAnnotationOfClass(@Nonnull Class<?> anyClass, @Nonnull Class<T> classOfAnnotation) {
        return anyClass.getAnnotation(classOfAnnotation);
    }

    /**
     * @since 3.1.8
     * For the repeatable annotations.
     */
    @Nonnull
    public <T extends Annotation> T[] getAnnotationsOfClass(@Nonnull Class<?> anyClass, @Nonnull Class<T> classOfAnnotation) {
        return anyClass.getAnnotationsByType(classOfAnnotation);
    }

    /**
     * @param packageName In this package
     * @param baseClass   seek any class implementations of this class
     * @param <R>         the target base class to seek its implementations
     * @return the sought classes in a set
     * @since 3.0.6
     */
    public <R> Set<Class<? extends R>> seekClassDescendantsInPackage(@Nonnull String packageName, @Nonnull Class<R> baseClass) {
        Reflections reflections = new Reflections(packageName);
        return reflections.getSubTypesOf(baseClass);
    }

    /**
     * @return Whether the given `baseClass` is the base of the given `implementClass`.
     * @since 3.0.10
     */
    public boolean isClassAssignable(@Nonnull Class<?> baseClass, @Nonnull Class<?> implementClass) {
        return baseClass.isAssignableFrom(implementClass);
    }
}
