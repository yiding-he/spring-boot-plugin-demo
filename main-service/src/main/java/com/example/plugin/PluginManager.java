package com.example.plugin;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 插件服务：扫描插件目录，加载插件
 */
@Component
@Slf4j
public class PluginManager implements AutoCloseable {

    private static final Predicate<Path> VALID_PLUGIN_PATH = p -> {
        if (Files.isDirectory(p)) {
            // 这个判断是给开发用，插件可以在 main-service 服务的
            // src/test 下编写，Maven 会将其编译到 test-classes 目录下
            // 然后在单元测试中检查插件是否正确
            return p.getFileName().toString().equals("test-classes");

        } else if (Files.isRegularFile(p) || Files.isSymbolicLink(p)) {
            // 这个判断是正式运行时使用的
            return p.getFileName().toString().endsWith(".jar");
        }
        return false;
    };

    @Value("${application.plugin-directory:plugins}")
    private String applicationPluginDirectory;

    @Autowired
    private ApplicationContext rootApplicationContext;

    ////////////////////////////////////////

    private PluginClassLoader pluginClassLoader;

    private AnnotationConfigApplicationContext pluginApplicationContext;

    @PostConstruct
    public void init() {
        var path = Path.of(this.applicationPluginDirectory).toAbsolutePath();
        if (!Files.exists(path)) {
            log.error("Plugin directory not found: {}", path);
            return;
        }

        // 1. 扫描插件目录，加载插件实现类
        this.pluginClassLoader = createClassLoader(this.applicationPluginDirectory, BeforeAddUserPlugin.class);
        var pluginClasses = this.pluginClassLoader.listPluginClasses();
        log.info("Found {} plugin classes: {}", pluginClasses.size(), pluginClasses);

        // 2. 初始化插件专用的 Spring ApplicationContext
        this.pluginApplicationContext = new AnnotationConfigApplicationContext();
        this.pluginApplicationContext.setClassLoader(pluginClassLoader);
        this.pluginApplicationContext.setParent(rootApplicationContext);

        // 3. 插件专用的 Spring ApplicationContext 扫面插件所在目录，
        //    将合适的类加载到 Spring ApplicationContext 中
        //    这个过程不影响 main-service 的 Spring ApplicationContext
        var packages = pluginClasses.stream()
            .map(Class::getPackageName)
            .distinct()
            .toArray(String[]::new);

        this.pluginApplicationContext.scan(packages);
        this.pluginApplicationContext.refresh();
    }

    /**
     * 获取已经加载的全部 BeforeAddUserPlugin 插件实例
     */
    public List<BeforeAddUserPlugin> getPlugins() {
        return new ArrayList<>(
            this.pluginApplicationContext.getBeansOfType(BeforeAddUserPlugin.class).values()
        );
    }

    @Override
    public void close() throws IOException {
        this.pluginClassLoader.close();
    }

    private static PluginClassLoader createClassLoader(String pluginDir, Class<?> pluginClass) {
        log.info("Creating class loader for path {}", pluginDir);
        try {
            List<Path> dirOrJarFiles;
            try (var list = Files.list(Path.of(pluginDir))) {
                dirOrJarFiles = list
                    .filter(VALID_PLUGIN_PATH)
                    .collect(Collectors.toList());
            }
            var classLoaderName = pluginClass.getSimpleName() + "_plugins";
            return new PluginClassLoader(
                PluginManager.class.getClassLoader(), classLoaderName, dirOrJarFiles, pluginClass
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
