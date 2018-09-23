package cn.yxffcode.modularspring.boot.utils;

import cn.yxffcode.modularspring.boot.ModuleConfig;

import java.io.File;

/**
 * @author gaohang on 7/9/17.
 */
public final class ModuleLoadContextHolder {
  private static String loadingModulePath;
  private static ModuleConfig loadingModuleConfig;
  private ModuleLoadContextHolder() {
  }

  public static String getLoadingModulePath() {
    return loadingModulePath;
  }

  public static void setLoadingModulePath(String loadingModulePath) {
    if (File.separator != "/") {
      ModuleLoadContextHolder.loadingModulePath = loadingModulePath.replace(File.separator, "/");
    } else {
      ModuleLoadContextHolder.loadingModulePath = loadingModulePath;
    }
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
