package cn.yxffcode.modularspring.boot.utils;

import cn.yxffcode.modularspring.boot.ModuleConfig;

/**
 * @author gaohang on 7/22/17.
 */
public final class ModuleUtils {
  private ModuleUtils() {
  }

  public static String getSimpleModuleName(ModuleConfig moduleConfig) {
    return getSimpleModuleName(moduleConfig.getModuleName());
  }

  public static String getSimpleModuleName(String moduleName) {
    final int i = moduleName.lastIndexOf('.');
    return i < 0 ? moduleName : moduleName.substring(i + 1);
  }
}
