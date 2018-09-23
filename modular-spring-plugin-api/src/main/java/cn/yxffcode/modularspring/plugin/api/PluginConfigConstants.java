package cn.yxffcode.modularspring.plugin.api;

/**
 * @author gaohang on 2/15/18.
 */
public abstract class PluginConfigConstants {
  public static final String PLUGIN_NAME = "pluginName";
  public static final String EXPORT_PACKAGES = "exportPackages";
  public static final String ACTIVATOR = "activator";
  public static final String PLUGIN_PKG_SUFFIX = ".zip";
  public static final String PLUGIN_PKG_PREFIX = "META-INF/plugin/";
  public static final String PLUGIN_CONFIG_PATH = "META-INF/plugin.json";

  private PluginConfigConstants() {
  }
}
