package cn.yxffcode.modularspring.plugin.api;

/**
 * @author gaohang on 2/10/18.
 */
public interface PluginActivator {
  void onLoad();

  void onDestroy();
}
