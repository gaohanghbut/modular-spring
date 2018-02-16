package cn.yxffcode.modularspring.core.plugin;

import java.net.URLClassLoader;
import java.util.List;

/**
 * @author gaohang on 2/10/18.
 */
public interface PluginLoader {
  List<Plugin> load(final URLClassLoader classpathClassLoader);
}
