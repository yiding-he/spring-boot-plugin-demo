# spring-boot-plugin-demo

这是一个在 Spring Boot 项目中实现插件机制的例子。其中包含三个模块：

- plugin-api：插件接口
- plugin-sample：插件实现
- main-service: 使用插件的主服务

其中 main-service 和 plugin-sample 都依赖 plugin-api 模块。

这个插件机制的特点在于：

1. 插件独立开发打包，不依赖主服务的代码；
2. 插件的加载使用单独的类加载器，避免插件和主服务之间相互依赖；
3. 插件加载后自动被纳入 Spring IoC 容器管，并可以使用容器提供的各种环境；

## 运行方法：

1. 首先对整个项目执行 mvn package，生成的插件包会输出到 plugins 目录下；
2. 运行 main-service 项目，启动后会自动加载插件包并执行对应的逻辑；
