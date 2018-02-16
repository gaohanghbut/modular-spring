package cn.yxffcode.modularspring.core.plugin.classloader;

import java.net.URL;
import java.net.URLClassLoader;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 2/15/18.
 */
public class PluginClassLoader extends URLClassLoader {
  private final ClassLoader parent;

  public PluginClassLoader(URL[] urls, ClassLoader parent) {
    super(urls);
    this.parent = checkNotNull(parent);
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    //这里与双亲委派不同的是，先自己加载，加载不到再使用parent加载

    if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("com.sun.misc.")) {
      return parent.loadClass(name);
    }
    //先自己加载
    Class<?> clazz = null;
    try {
      clazz = super.loadClass(name, resolve);
    } catch (ClassNotFoundException e) {
      //ignore
    }

    if (clazz == null) {
      //代理给parent
      clazz = parent.loadClass(name);
    }

    if (resolve) {
      resolveClass(clazz);
    }

    return clazz;
  }
}
