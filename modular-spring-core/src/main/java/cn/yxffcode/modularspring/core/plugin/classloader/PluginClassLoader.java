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

    if (name.startsWith("java.") || name.startsWith("javax.")
        || name.startsWith("com.sun.misc.") || name.startsWith("org.springframework.")
        || name.startsWith("cn.yxffcode.modularspring.")) {
      return parent.loadClass(name);
    }

    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (c == null) {
        long t0 = System.nanoTime();
        try {
          c = findClass(name);
        } catch (ClassNotFoundException e) {
          // ClassNotFoundException thrown if class not found
          // from the non-null parent class loader
        }

        if (c == null) {
          // If still not found, then invoke findClass in order
          // to find the class.
          long t1 = System.nanoTime();

          c = parent.loadClass(name);
          // this is the defining class loader; record the stats
          sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
          sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
          sun.misc.PerfCounter.getFindClasses().increment();
        }
      }
      if (resolve) {
        resolveClass(c);
      }
      return c;
    }
  }
}
