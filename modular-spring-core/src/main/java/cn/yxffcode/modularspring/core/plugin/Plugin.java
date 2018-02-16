package cn.yxffcode.modularspring.core.plugin;

import cn.yxffcode.modularspring.core.plugin.classloader.PluginClassLoader;
import cn.yxffcode.modularspring.plugin.api.PluginActivator;

import java.util.List;

/**
 * @author gaohang on 2/10/18.
 */
public class Plugin {
  private String name;
  private PluginActivator pluginActivator;
  private List<String> exportPackages;
  private PluginClassLoader pluginClassLoader;

  public List<String> getExportPackages() {
    return exportPackages;
  }

  public void setExportPackages(List<String> exportPackages) {
    this.exportPackages = exportPackages;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PluginActivator getPluginActivator() {
    return pluginActivator;
  }

  public void setPluginActivator(PluginActivator pluginActivator) {
    this.pluginActivator = pluginActivator;
  }

  public PluginClassLoader getPluginClassLoader() {
    return pluginClassLoader;
  }

  public void setPluginClassLoader(PluginClassLoader pluginClassLoader) {
    this.pluginClassLoader = pluginClassLoader;
  }
}
