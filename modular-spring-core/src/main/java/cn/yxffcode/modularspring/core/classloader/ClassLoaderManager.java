package cn.yxffcode.modularspring.core.classloader;

/**
 * @author gaohang on 2/15/18.
 */
public interface ClassLoaderManager {
  void setAppClassLoader(final ModularClassLoader appClassLoader);

  ModularClassLoader getAppClassLoader();

  ExportedClassLoader getExportedClassLoader();

  void setExportedClassLoader(ExportedClassLoader exportedClassLoader);

}
