package cn.yxffcode.modularspring.boot;

import cn.yxffcode.modularspring.boot.listener.ModuleLoadListener;
import cn.yxffcode.modularspring.boot.plugin.ClassPathPluginLoader;
import cn.yxffcode.modularspring.boot.plugin.PluginLoader;
import cn.yxffcode.modularspring.core.ModularSpringConfiguration;
import cn.yxffcode.modularspring.core.classloader.ClassLoaderManager;
import cn.yxffcode.modularspring.core.classloader.DefaultClassLoaderManager;
import cn.yxffcode.modularspring.core.classloader.ExportedClassLoader;
import cn.yxffcode.modularspring.core.classloader.ModularClassLoader;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import cn.yxffcode.modularspring.core.plugin.Plugin;
import cn.yxffcode.modularspring.plugin.api.PluginActivator;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author gaohang on 6/24/17.
 */
public class ApplicationManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationManager.class);

  private Map<ModuleConfig, ModuleApplicationContext> applicationContexts = Collections.emptyMap();

  private ModuleLoaderFactory moduleLoaderFactory;
  private final PluginLoader pluginLoader;
  private final ModularSpringConfiguration modularSpringConfiguration;

  private final ClassLoaderManager classLoaderManager = new DefaultClassLoaderManager();
  private List<Plugin> plugins;
  private ModuleLoader moduleLoader;

  public ApplicationManager(final ModularSpringConfiguration modularSpringConfiguration, final ModuleLoaderFactory moduleLoaderFactory) {
    this(modularSpringConfiguration, moduleLoaderFactory, new ClassPathPluginLoader(modularSpringConfiguration));
  }

  public ApplicationManager(final ModularSpringConfiguration modularSpringConfiguration, ModuleLoaderFactory moduleLoaderFactory, PluginLoader pluginLoader) {
    this.moduleLoaderFactory = moduleLoaderFactory;
    this.pluginLoader = pluginLoader;
    this.modularSpringConfiguration = modularSpringConfiguration;
  }

  public void boot() {

    final URLClassLoader classpathClassLoader = (URLClassLoader) ApplicationManager.class.getClassLoader();
    final ExportedClassLoader exportedClassLoader = new ExportedClassLoader(classpathClassLoader);
    classLoaderManager.setExportedClassLoader(exportedClassLoader);
    final ModularClassLoader modularClassLoader = new ModularClassLoader(exportedClassLoader);
    classLoaderManager.setAppClassLoader(modularClassLoader);
    Thread.currentThread().setContextClassLoader(modularClassLoader);

    loadPlugins(classpathClassLoader);

    final ModuleLoader moduleLoader = moduleLoaderFactory.newModuleLoader(classLoaderManager.getAppClassLoader());
    this.moduleLoader = moduleLoader;
    try {
      this.applicationContexts = moduleLoader.load(modularClassLoader);
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  private void loadPlugins(URLClassLoader classpathClassLoader) {
    plugins = pluginLoader.load(classpathClassLoader);
    if (CollectionUtils.isEmpty(plugins)) {
      return;
    }
    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      for (Plugin plugin : plugins) {
        Thread.currentThread().setContextClassLoader(plugin.getPluginClassLoader());
        final PluginActivator pluginActivator = plugin.getPluginActivator();
        pluginActivator.onLoad();

        final List<String> exportPackages = plugin.getExportPackages();
        if (CollectionUtils.isEmpty(exportPackages)) {
          continue;
        }

        final ExportedClassLoader exportedClassLoader = classLoaderManager.getExportedClassLoader();

        for (String exportPackage : exportPackages) {
          exportedClassLoader.addClassLoader(exportPackage, plugin.getPluginClassLoader());
        }
      }
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  public void addModuleLoadListener(ModuleLoadListener moduleLoadListener) {
    moduleLoader.addModuleLoadListener(moduleLoadListener);
  }

  public void destroy() {
    for (ApplicationContext applicationContext : applicationContexts.values()) {
      if (applicationContext instanceof ConfigurableApplicationContext) {
        try {
          ((ConfigurableApplicationContext) applicationContext).close();
        } catch (Exception e) {
          LOGGER.error("close ApplicationContext failed", e);
        }
      }
    }
    if (CollectionUtils.isEmpty(plugins)) {
      return;
    }
    final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      for (Plugin plugin : plugins) {
        Thread.currentThread().setContextClassLoader(plugin.getPluginClassLoader());
        final PluginActivator pluginActivator = plugin.getPluginActivator();
        pluginActivator.onDestroy();
      }
    } finally {
      Thread.currentThread().setContextClassLoader(oldClassLoader);
    }
  }

  public ClassLoaderManager getClassLoaderManager() {
    return classLoaderManager;
  }
}
