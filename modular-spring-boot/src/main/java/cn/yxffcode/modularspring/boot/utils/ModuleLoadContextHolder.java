package cn.yxffcode.modularspring.boot.utils;

/**
 * @author gaohang on 7/9/17.
 */
public final class ModuleLoadContextHolder {
  private ModuleLoadContextHolder() {
  }

  private static String loadingModulePath;

  public static String getLoadingModulePath() {
    return loadingModulePath;
  }

  public static void setLoadingModulePath(String loadingModulePath) {
    ModuleLoadContextHolder.loadingModulePath = loadingModulePath;
  }

  public static void clean() {
    loadingModulePath = null;
  }
}
