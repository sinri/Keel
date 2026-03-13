package io.github.sinri.keel.helper;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

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
     * @since 3.2.12.1 rewrite
     */
    public <R> Set<Class<? extends R>> seekClassDescendantsInPackage(@Nonnull String packageName, @Nonnull Class<R> baseClass) {
//        Reflections reflections = new Reflections(packageName);
//        return reflections.getSubTypesOf(baseClass);

        Set<Class<? extends R>> set = new HashSet<>();

        List<String> classPathList = Keel.fileHelper().getClassPathList();
        for (String classPath : classPathList) {
            if (classPath.endsWith(".jar")) {
                Set<Class<? extends R>> classes = seekClassDescendantsInPackageForProvidedJar(classPath, packageName, baseClass);
                set.addAll(classes);
            } else {
                Set<Class<? extends R>> classes = seekClassDescendantsInPackageForFileSystem(packageName, baseClass);
                set.addAll(classes);
            }
        }

        return set;
    }

    /**
     * @since 3.2.11
     */
    protected <R> Set<Class<? extends R>> seekClassDescendantsInPackageForFileSystem(@Nonnull String packageName, @Nonnull Class<R> baseClass) {
        Set<Class<? extends R>> descendantClasses = new HashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // in file system
        String packagePath = packageName.replace('.', File.separatorChar);
        try {
            // Assuming classes are in a directory on the file system (e.g., not in a JAR)
            URL resource = classLoader.getResource(packagePath);
            if (resource != null) {
                URI uri = resource.toURI();
                Path startPath = Paths.get(uri);
                Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.toString().endsWith(".class")) {
                            String className = file.toString().replace(".class", "").replace(File.separator, ".");
                            className = className.substring(className.indexOf(packageName));

                            try {
                                Class<? extends R> clazz = (Class<? extends R>) classLoader.loadClass(className);
                                if (baseClass.isAssignableFrom(clazz)) {
                                    descendantClasses.add(clazz);
                                }
                            } catch (Throwable e) {
                                Keel.getLogger().debug(getClass() + " seekClassDescendantsInPackageForFileSystem for " + className + " error: " + e.getMessage());
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        } catch (Exception e) {
            Keel.getLogger().exception(e);
        }
        return descendantClasses;
    }

    /**
     * @since 3.2.11
     */
    protected <R> Set<Class<? extends R>> seekClassDescendantsInPackageForRunningJar(@Nonnull String packageName, @Nonnull Class<R> baseClass) {
        Set<Class<? extends R>> descendantClasses = new HashSet<>();
        Set<String> strings = Keel.fileHelper().seekPackageClassFilesInRunningJar(packageName);
        for (String s : strings) {
            try {
                Class<?> aClass = Class.forName(s);
                if (baseClass.isAssignableFrom(aClass)) {
                    descendantClasses.add((Class<? extends R>) aClass);
                }
            } catch (Throwable e) {
                Keel.getLogger().debug(getClass() + " seekClassDescendantsInPackageForRunningJar for " + s + " error: " + e.getMessage());
            }
        }
        return descendantClasses;
    }

    /**
     * @since 3.2.11
     */
    protected <R> Set<Class<? extends R>> seekClassDescendantsInPackageForProvidedJar(@Nonnull String jarInClassPath, @Nonnull String packageName, @Nonnull Class<R> baseClass) {
        Set<Class<? extends R>> descendantClasses = new HashSet<>();
        List<String> classNames = Keel.fileHelper().traversalInJarFile(new File(jarInClassPath));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        classNames.forEach(className -> {
            if (className.startsWith(packageName + ".")) {
                try {
                    Class<? extends R> clazz = (Class<? extends R>) classLoader.loadClass(className);
                    if (baseClass.isAssignableFrom(clazz)) {
                        descendantClasses.add(clazz);
                    }
                } catch (Throwable e) {
                    Keel.getLogger().debug(getClass() + " seekClassDescendantsInPackageForProvidedJar for " + className + " error: " + e.getMessage());
                }
            }
        });
        return descendantClasses;
    }

    /**
     * @return Whether the given `baseClass` is the base of the given `implementClass`.
     * @since 3.0.10
     */
    public boolean isClassAssignable(@Nonnull Class<?> baseClass, @Nonnull Class<?> implementClass) {
        return baseClass.isAssignableFrom(implementClass);
    }
}
