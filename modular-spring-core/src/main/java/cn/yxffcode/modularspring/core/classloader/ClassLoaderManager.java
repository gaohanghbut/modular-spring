package cn.yxffcode.modularspring.core.classloader;

/**
 * @author gaohang on 2/15/18.
 */
public interface ClassLoaderManager {
  ModularClassLoader getAppClassLoader();

  void setAppClassLoader(final ModularClassLoader appClassLoader);

  ExportedClassLoader getExportedClassLoader();

  void setExportedClassLoader(ExportedClassLoader exportedClassLoader);

}
