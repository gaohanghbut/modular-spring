package cn.yxffcode.modularspring.core.classloader;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 2/15/18.
 */
public class ExportedClassLoader extends ClassLoader {

  /**
   * 导出类的类加载器
   */
  private Map<String, ClassLoader> delegatedClassLoaders = Maps.newHashMap();

  /**
   * className -> class
   */
  private ConcurrentMap<String, Class<?>> loadedClasses = Maps.newConcurrentMap();

  public ExportedClassLoader(final ClassLoader parent) {
    super(parent);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    final Class<?> clazz = findClass(name);
    if (clazz == null) {
      return getParent().loadClass(name);
    }
    if (resolve) {
      resolveClass(clazz);
    }
    return clazz;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    final Class<?> type = loadedClasses.get(name);
    if (type != null) {
      return type;
    }
    for (Map.Entry<String, ClassLoader> en : delegatedClassLoaders.entrySet()) {
      final String pkg = en.getKey();
      if (name.startsWith(pkg)) {
        final Class<?> clazz = en.getValue().loadClass(name);
        loadedClasses.putIfAbsent(name, clazz);
        return clazz;
      }
    }
    return null;
  }

  /**
   * 导出包
   *
   * @param packageName
   * @param classLoader
   */
  public void addClassLoader(final String packageName, final ClassLoader classLoader) {
    delegatedClassLoaders.put(packageName, checkNotNull(classLoader));
  }
}
