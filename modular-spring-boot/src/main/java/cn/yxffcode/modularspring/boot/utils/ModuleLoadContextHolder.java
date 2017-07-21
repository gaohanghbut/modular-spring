package cn.yxffcode.modularspring.boot.utils;

import cn.yxffcode.modularspring.boot.ModuleConfig;

/**
 * @author gaohang on 7/9/17.
 */
public final class ModuleLoadContextHolder {
  private ModuleLoadContextHolder() {
  }

  private static String loadingModulePath;
  private static ModuleConfig loadingModuleConfig;

  public static String getLoadingModulePath() {
    return loadingModulePath;
  }

  public static void setLoadingModulePath(String loadingModulePath) {
    ModuleLoadContextHolder.loadingModulePath = loadingModulePath;
  }

  public static ModuleConfig getLoadingModuleConfig() {
    return loadingModuleConfig;
  }

  public static void setLoadingModuleConfig(ModuleConfig loadingModuleConfig) {
    ModuleLoadContextHolder.loadingModuleConfig = loadingModuleConfig;
  }

  public static void clean() {
    loadingModulePath = null;
    loadingModuleConfig = null;
  }
}
