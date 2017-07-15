# 如何做到按照模块加载

## 按模块获取spring配置
在初始化每个模块的Spring时，需要保证每个模块只加载此模块自己的spring配置，那么创建Spring
依赖注入容器的步骤应该是：

* 获取META-INF/spring/目录下的所有xml文件
* 通过上一步中获取的xml文件

在modular-spring中，各模块之间通过Spring的ApplicationContext做隔离，但ClassLoader
没有隔离。因为模块中的spring配置文件不需要（也不能）通过配置的方式指定，所以加载时需要识别
出类路径下哪些配置属于一个模块，因此，在初始化spring时，不能通过spring提供的ResourceLoader
加载spring配置，因为ResourceLoader会将类路径下所有spring的配置取出，而Resource名都是
以META-INF/spring开头，无法区分出每个模块的配置，可以使用ClassLoader遍历类路径，通过路径
来判断哪些配置属于同一个模块：
```java
private void scanDirectory(
          File directory, ClassLoader classloader, String packagePrefix,
          ImmutableSet<File> ancestors, Predicate<String> accepter) throws IOException {
    File canonical = directory.getCanonicalFile();
    if (ancestors.contains(canonical)) {
      // A cycle in the filesystem, for example due to a symbolic link.
      return;
    }
    File[] files = directory.listFiles();
    if (files == null) {
      logger.warn("Cannot read directory {}", directory);
      // IO error, just skip the directory
      return;
    }
    ImmutableSet<File> newAncestors = ImmutableSet.<File>builder()
            .addAll(ancestors)
            .add(canonical)
            .build();
    for (File f : files) {
      String name = f.getName();
      if (f.isDirectory()) {
        scanDirectory(f, classloader, packagePrefix + name + "/", newAncestors, accepter);
      } else {
        String resourceName = packagePrefix + name;
        if (!resourceName.equals(JarFile.MANIFEST_NAME) && accepter.apply(resourceName)) {
          resources.add(ResourceInfo.of("file:" + f.getAbsolutePath(), classloader));
        }
      }
    }
  }

  private void scanJar(File file, ClassLoader classloader, Predicate<String> acceptor) throws IOException {
    JarFile jarFile;
    try {
      jarFile = new JarFile(file);
    } catch (IOException e) {
      // Not a jar file
      return;
    }
    try {
      for (URI uri : getClassPathFromManifest(file, jarFile.getManifest())) {
        scan(uri, classloader, acceptor);
      }
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
          continue;
        }
        if (acceptor.apply(entry.getName())) {
          resources.add(ResourceInfo.of("jar:" + jarFile.getName() + "!/" + entry.getName(), classloader));
        }
      }
    } finally {
      try {
        jarFile.close();
      } catch (IOException ignored) {
      }
    }
  }
```
详细代码见ClasspathScanner类。

## 按模块初始化Spring
获取到每个模块的spring配置后，按照每个模块加载配置，创建ApplicationContext对象。
但是获取到的spring配置有以下两种存在形式：
* 存在于一个目录中，比如在本地运行测试代码时，工程目录下的配置
* 存在于jar包中，模块部署前会被打成jar

对于这两种形式的配置，读取的方式不一样，可扩展spring的AbstractXmlApplicationContext
来读取不同存在形式的配置，例如读取目录中的配置：
```java
public class ModuleFileSystemApplicationContext extends AbstractModuleApplicationContext {
  @Override
  protected Resource getResourceByPath(String path) {
    if (path != null && path.startsWith("/")) {
      path = path.substring(1);
    }
    return new FileSystemResource(path);
  }
}
```
读取jar中的配置创建ApplicationContext
```java
public class ModuleJarEntryXmlApplicationContext extends AbstractModuleApplicationContext {
  @Override
  protected Resource getResourceByPath(String path) {
    return new JarEntryResource(path);
  }
}
```
AbstractXmlApplicationContext的作用后文再提.