package org.anarchadia.quasar.api.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReflectionUtil {

    public static List<Class<?>> find(String scannedPackage) {
        String scannedPath = scannedPackage.replace('.', '/');
        URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
        if (scannedUrl == null) {
            throw new IllegalArgumentException("Package " + scannedPackage + " not found.");
        }

        File scannedDir;
        try {
            scannedDir = new File(scannedUrl.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Package " + scannedPackage + " is not a valid URL.", e);
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File file : Objects.requireNonNull(scannedDir.listFiles())) {
            classes.addAll(findClasses(file, scannedPackage));
        }

        return classes;
    }

    private static List<Class<?>> findClasses(File file, String scannedPackage) {
        List<Class<?>> classes = new ArrayList<>();
        String resource = scannedPackage + '.' + file.getName();
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                classes.addAll(findClasses(child, resource));
            }
        } else if (resource.endsWith(".class")) {
            int endIndex = resource.length() - ".class".length();
            String className = resource.substring(0, endIndex);
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ignore) {}
        }
        return classes;
    }
}
