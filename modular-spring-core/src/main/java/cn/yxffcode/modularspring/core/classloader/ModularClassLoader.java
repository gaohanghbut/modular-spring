package cn.yxffcode.modularspring.core.classloader;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author gaohang on 2/10/18.
 */
public class ModularClassLoader extends ClassLoader {

  public ModularClassLoader(final ExportedClassLoader exportedClassLoader) {
    super(exportedClassLoader);
  }

  public URL[] getClassPathURLs() {
    final URLClassLoader parent = (URLClassLoader) getParent();
    return parent.getURLs();
  }
}
