package cn.yxffcode.modularspring.boot;

import com.google.common.base.Objects;

import java.util.List;

/**
 * @author gaohang on 6/24/17.
 */
public class ModuleConfig {
  private String moduleName;
  private List<String> dependenceModules;

  public String getModuleName() {
    return moduleName;
  }

  public void setModuleName(String moduleName) {
    this.moduleName = moduleName;
  }

  public List<String> getDependenceModules() {
    return dependenceModules;
  }

  public void setDependenceModules(List<String> dependenceModules) {
    this.dependenceModules = dependenceModules;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ModuleConfig that = (ModuleConfig) o;
    return Objects.equal(moduleName, that.moduleName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(moduleName);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
            .add("moduleName", moduleName)
            .add("dependenceModules", dependenceModules)
            .toString();
  }
}
