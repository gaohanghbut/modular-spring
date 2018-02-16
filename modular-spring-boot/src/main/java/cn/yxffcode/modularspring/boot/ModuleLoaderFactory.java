package cn.yxffcode.modularspring.boot;

/**
 * @author gaohang on 2/16/18.
 */
public interface ModuleLoaderFactory {
  ModuleLoader newModuleLoader(final ClassLoader classLoader);
}
