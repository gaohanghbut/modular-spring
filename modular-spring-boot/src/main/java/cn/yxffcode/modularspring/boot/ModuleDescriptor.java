package cn.yxffcode.modularspring.boot;

import java.net.URL;
import java.util.List;

/**
 * @author gaohang on 6/24/17.
 */
public final class ModuleDescriptor {
  private final URL moduleJsonFile;
  private final List<URL> springConfigurations;

  public ModuleDescriptor(URL moduleJsonFile, List<URL> springConfigurations) {
    this.moduleJsonFile = moduleJsonFile;
    this.springConfigurations = springConfigurations;
  }

  public URL getModuleJsonFile() {
    return moduleJsonFile;
  }

  public List<URL> getSpringConfigurations() {
    return springConfigurations;
  }
}
