package io.github.sinri.keel.helper;

import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 2.6
 */
public class KeelFileHelper {
    private static final KeelFileHelper instance = new KeelFileHelper();

    private KeelFileHelper() {

    }

    static KeelFileHelper getInstance() {
        return instance;
    }

    public byte[] readFileAsByteArray(@NotNull String filePath, boolean seekInsideJarWhenNotFound) throws IOException {
        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            if (seekInsideJarWhenNotFound) {
                try (
                        InputStream resourceAsStream = KeelFileHelper.class.getClassLoader().getResourceAsStream(filePath)
                ) {
                    if (resourceAsStream == null) {
                        // not found resource
                        throw new IOException("file also not in jar", e);
                    }
                    return resourceAsStream.readAllBytes();
                }
            } else {
                throw e;
            }
        }
    }

    /**
     * @param filePath path string of the target file, or directory
     * @return the URL of target file; if not there, null return.
     * @since 3.2.12.1 original name is `getUrlOfFileInJar`.
     */
    @Nullable
    public URL getUrlOfFileInRunningJar(@NotNull String filePath) {
        return KeelFileHelper.class.getClassLoader().getResource(filePath);
    }

    /**
     * Seek in JAR, under the root (exclusive)
     *
     * @param root ends with '/' for a directory
     * @return list of JarEntry
     * @since 3.2.12.1 original name is `traversalInJar`.
     */
    @NotNull
    public List<JarEntry> traversalInRunningJar(@NotNull String root) {
        List<JarEntry> jarEntryList = new ArrayList<>();
        try {
            // should root ends with '/'?
            URL url = KeelFileHelper.class.getClassLoader().getResource(root);
            if (url == null) {
                throw new RuntimeException("Resource is not found");
            }
            if (!url.toString().contains("!/")) {
                throw new RuntimeException("Resource is not in JAR");
            }
            String jarPath = url.toString().substring(0, url.toString().indexOf("!/") + 2);

            URL jarURL = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            var baseJarEntry = jarFile.getJarEntry(root);
            var pathOfBaseJarEntry = Path.of(baseJarEntry.getName());

            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();

                Path entryPath = Path.of(entry.getName());
                if (entryPath.getParent() == null) {
                    continue;
                }
                if (entryPath.getParent().compareTo(pathOfBaseJarEntry) == 0) {
                    jarEntryList.add(entry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jarEntryList;
    }

    /**
     * @return absolute created Temp File path
     * @since 3.0.0
     */
    public Future<String> crateTempFile(@Nullable String prefix, @Nullable String suffix) {
        return Keel.getVertx().fileSystem().createTempFile(prefix, suffix);
    }

    /**
     * @since 3.2.11
     * @since 3.2.12.1 Changed the implementation with checking class paths.
     * Check if this process is running with JAR file.
     */
    public boolean isRunningFromJAR() {
        List<String> classPathList = getClassPathList();
        for (var classPath : classPathList) {
            if (!classPath.endsWith(".jar")) {
                return false;
            }
        }
        return true;
    }

    /**
     * @since 3.2.12.1
     */
    public List<String> getClassPathList() {
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        return new ArrayList<>(Arrays.asList(classpathEntries));
    }

    /**
     * The in-class classes, i.e. subclasses, would be neglected.
     *
     * @since 3.2.12.1 original name is `seekPackageClassFilesInJar`.
     */
    public Set<String> seekPackageClassFilesInRunningJar(@NotNull String packageName) {
        Set<String> classes = new HashSet<>();
        // Get the current class's class loader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Get the URL of the JAR file containing the current class
        String currentClassUrlInJarFile = getClass().getName().replace('.', '/') + ".class";
        URL jarUrl = classLoader.getResource(currentClassUrlInJarFile);

        if (jarUrl != null && jarUrl.getProtocol().equals("jar")) {
            // Extract the JAR file path
            String jarPath = jarUrl.getPath().substring(5, jarUrl.getPath().indexOf("!"));

            // Open the JAR file
            try (JarFile jarFile = new JarFile(jarPath)) {
                // Iterate through the entries of the JAR file
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    // Check if the entry is a class
                    if (entryName.endsWith(".class")) {
                        // Convert the entry name to a fully qualified class name
                        String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
                        if (className.startsWith(packageName + ".") && !className.contains("$")) {
                            classes.add(className);
                        }
                    }
                }
            } catch (IOException e) {
                Keel.getLogger().debug(getClass() + " seekPackageClassFilesInRunningJar for package " + packageName + " error: " + e.getMessage());
            }
        }

        return classes;
    }

    /**
     * @param jarFile File built from JAR in class path.
     * @since 3.2.12.1
     */
    public List<String> traversalInJarFile(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            List<String> list = new ArrayList<>();

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (
                        entryName.endsWith(".class")
                                && !entryName.contains("$")
                                && !entryName.startsWith("META-INF")
                ) {
                    // 将路径形式的类名转换为 Java 类名
                    String className = entryName.replace("/", ".").replace(".class", "");
                    list.add(className);
                }
            }

            return list;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
