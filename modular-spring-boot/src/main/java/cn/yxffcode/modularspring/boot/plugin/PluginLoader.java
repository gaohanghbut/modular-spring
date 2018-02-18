package cn.yxffcode.modularspring.boot.plugin;

import cn.yxffcode.modularspring.core.plugin.Plugin;

import java.net.URLClassLoader;
import java.util.List;

/**
 * @author gaohang on 2/10/18.
 */
public interface PluginLoader {
  List<Plugin> load(final URLClassLoader classpathClassLoader);
}
