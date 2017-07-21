package cn.yxffcode.modularspring.boot.utils;

import cn.yxffcode.modularspring.boot.ModuleConfig;

/**
 * @author gaohang on 7/22/17.
 */
public final class ModuleUtils {
  private ModuleUtils() {
  }

  public static String getSimpleModuleName(ModuleConfig moduleConfig) {
    final int i = moduleConfig.getModuleName().lastIndexOf('.');
    return i < 0 ? moduleConfig.getModuleName() : moduleConfig.getModuleName().substring(i + 1);
  }
}
