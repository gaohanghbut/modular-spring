package cn.yxffcode.modularspring.boot;

/**
 * @author gaohang on 2/16/18.
 */
public class DefaultModuleLoaderFactory implements ModuleLoaderFactory {

  @Override
  public ModuleLoader newModuleLoader(final ClassLoader classLoader) {
    try {
      final Class<?> type = classLoader.loadClass("cn.yxffcode.modularspring.boot.DefaultModuleLoader");
      return (ModuleLoader) type.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
