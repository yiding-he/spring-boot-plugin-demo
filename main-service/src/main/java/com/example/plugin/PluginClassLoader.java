package com.example.plugin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

/**
 * 用于加载插件的 ClassLoader
 */
@Slf4j
public class PluginClassLoader extends URLClassLoader {

    private static URL[] fromFiles(List<Path> jarFiles) {
        return jarFiles.stream().map(Path::toUri).map(uri -> {
            try {
                return uri.toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }).toArray(URL[]::new);
    }

    private final Class<?> pluginClass;

    @Getter
    private final String name;

    public PluginClassLoader(ClassLoader parent, String name, List<Path> sources, Class<?> pluginClass) {
        super(fromFiles(sources), parent);
        this.pluginClass = pluginClass;
        this.name = name;
    }

    public List<Class<?>> listPluginClasses() {
        try {
            List<Class<?>> list = new ArrayList<>();
            String pluginClassName = pluginClass.getName();
            Enumeration<URL> urls = this.getResources("META-INF/services/" + pluginClassName);

            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();

                List<String> classNames;
                try (var is = url.openStream()) {
                    classNames = Stream.of(
                        new String(is.readAllBytes(), StandardCharsets.UTF_8).split("\n")
                    ).filter(
                        s -> s != null && !s.isBlank()
                    ).toList();
                }

                for (String className : classNames) {
                    Class<?> implClass = this.loadClass(className);
                    list.add(implClass);
                }
            }
            return list;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
