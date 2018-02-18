package cn.yxffcode.modularspring.boot.plugin;

import cn.yxffcode.modularspring.core.ModularSpringConfiguration;
import cn.yxffcode.modularspring.core.plugin.Plugin;
import cn.yxffcode.modularspring.core.plugin.classloader.PluginClassLoader;
import cn.yxffcode.modularspring.core.io.ClasspathScanner;
import cn.yxffcode.modularspring.core.io.ZipUtils;
import cn.yxffcode.modularspring.plugin.api.PluginActivator;
import cn.yxffcode.modularspring.plugin.api.PluginConfigConstants;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;

/**
 * @author gaohang on 2/15/18.
 */
public class ClassPathPluginLoader implements PluginLoader {

  private final ModularSpringConfiguration modularSpringConfiguration;

  public ClassPathPluginLoader(ModularSpringConfiguration modularSpringConfiguration) {
    this.modularSpringConfiguration = modularSpringConfiguration;
  }

  @Override
  public List<Plugin> load(URLClassLoader classpathClassLoader) {
    final ClasspathScanner classpathScanner = new ClasspathScanner();
    try {
      classpathScanner.scan(classpathClassLoader, new Predicate<String>() {
        @Override
        public boolean apply(String input) {
          return input.endsWith(PluginConfigConstants.PLUGIN_PKG_SUFFIX);
        }
      });
    } catch (IOException e) {
      throw new PluginLoadException(e);
    }
    final ImmutableSortedSet<ClasspathScanner.ResourceInfo> resources = classpathScanner.getResources();
    return copyPlugins(classpathClassLoader, resources);
  }

  private List<Plugin> copyPlugins(URLClassLoader classpathClassLoader, ImmutableSortedSet<ClasspathScanner.ResourceInfo> resources) {
    final List<Plugin> plugins = Lists.newArrayList();
    for (ClasspathScanner.ResourceInfo resource : resources) {
      String pluginFile = resource.getResourceName();
      if (!pluginFile.endsWith(PluginConfigConstants.PLUGIN_PKG_SUFFIX)) {
        continue;
      }
      int idx = pluginFile.lastIndexOf('/');
      final String filename = pluginFile.substring(idx < 0 ? 0 : idx + 1, pluginFile.length() - PluginConfigConstants.PLUGIN_PKG_SUFFIX.length());
      final String pluginDir = modularSpringConfiguration.getPluginPath();
      //copy to dir and unzip
      if (pluginFile.startsWith("file:")) {
        pluginFile = pluginFile.substring("file:".length());
      }
      unzipPlugin(pluginFile, pluginDir);
      final List<URL> urls = toClasspath(pluginDir + filename);
      if (CollectionUtils.isEmpty(urls)) {
        continue;
      }
      final PluginClassLoader pluginClassLoader = createPluginClassLoader(classpathClassLoader, urls);
      final Plugin plugin = doLoad(pluginClassLoader);
      plugins.add(plugin);
    }
    return plugins;
  }

  private PluginClassLoader createPluginClassLoader(URLClassLoader classpathClassLoader, List<URL> urls) {
    final URL[] arr = new URL[urls.size()];
    urls.toArray(arr);
    return new PluginClassLoader(arr, classpathClassLoader);
  }

  private List<URL> toClasspath(String pluginDir) {
    final List<URL> urls = Lists.newArrayList();
    final File file = new File(pluginDir);
    final File[] files = file.listFiles();
    if (ArrayUtils.isEmpty(files)) {
      return null;
    }
    for (File f : files) {
      try {
        urls.add(f.toURL());
      } catch (MalformedURLException e) {
        throw new PluginLoadException(e);
      }
    }
    return urls;
  }

  private void unzipPlugin(String pluginFile, String pluginDir) {
    try {
      ZipUtils.decompressZip(pluginFile, pluginDir);
    } catch (IOException e) {
      throw new PluginLoadException(e);
    }
  }

  private Plugin doLoad(final PluginClassLoader classLoader) {
    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      final Map<String, Object> cfg = getPluginConfig(classLoader);
      final Plugin plugin = new Plugin();
      plugin.setName((String) cfg.get(PluginConfigConstants.PLUGIN_NAME));
      plugin.setExportPackages((List<String>) cfg.get(PluginConfigConstants.EXPORT_PACKAGES));
      Thread.currentThread().setContextClassLoader(classLoader);
      plugin.setPluginActivator((PluginActivator) classLoader.loadClass((String) cfg.get(PluginConfigConstants.ACTIVATOR)).newInstance());
      plugin.setPluginClassLoader(classLoader);
      return plugin;
    } catch (Exception e) {
      throw new PluginLoadException(e);
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  private Map<String, Object> getPluginConfig(final ClassLoader classLoader) throws IOException {
    try (InputStream in = classLoader.getResourceAsStream(PluginConfigConstants.PLUGIN_CONFIG_PATH)) {
      return JSON.parseObject(in, Map.class);
    }
  }
}
