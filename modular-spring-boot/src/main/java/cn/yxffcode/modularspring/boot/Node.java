package cn.yxffcode.modularspring.boot;

import java.util.List;

/**
 * @author gaohang on 6/24/17.
 */
class Node {
  private final ModuleConfig moduleConfig;

  private List<ModuleConfig> children;

  Node(ModuleConfig moduleConfig) {
    this.moduleConfig = moduleConfig;
  }

}
