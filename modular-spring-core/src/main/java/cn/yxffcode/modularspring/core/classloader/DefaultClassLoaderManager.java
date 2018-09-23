package cn.yxffcode.modularspring.core.classloader;

/**
 * @author gaohang on 2/15/18.
 */
public class DefaultClassLoaderManager implements ClassLoaderManager {
  private ModularClassLoader appClassLoader;
  private ExportedClassLoader exportedClassLoader;

  @Override
  public ModularClassLoader getAppClassLoader() {
    return appClassLoader;
  }

  @Override
  public void setAppClassLoader(ModularClassLoader appClassLoader) {
    this.appClassLoader = appClassLoader;
  }

  @Override
  public ExportedClassLoader getExportedClassLoader() {
    return exportedClassLoader;
  }

  @Override
  public void setExportedClassLoader(ExportedClassLoader exportedClassLoader) {
    this.exportedClassLoader = exportedClassLoader;
  }
}
