package cn.yxffcode.modularspring.boot;

import cn.yxffcode.modularspring.boot.io.ClasspathScanner;
import cn.yxffcode.modularspring.core.context.ModuleJarEntryXmlApplicationContext;
import cn.yxffcode.modularspring.core.io.JarEntryReader;
import cn.yxffcode.modularspring.core.context.ModuleFileSystemApplicationContext;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author gaohang on 6/24/17.
 */
public class DefaultModuleLoader implements ModuleLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModuleLoader.class);

  public Map<ModuleConfig, ApplicationContext> load(ClassLoader classLoader) throws IOException {

    final Map<String, ModuleConfig> moduleConfigs = resolveModuleJsonConfigs(classLoader);


    //初始化spring
    final Map<ModuleConfig, ApplicationContext> applicationContexts = Maps.newHashMapWithExpectedSize(moduleConfigs.size());
    for (Map.Entry<String, ModuleConfig> en : moduleConfigs.entrySet()) {
      final ModuleConfig moduleConfig = en.getValue();

      final List<String> springConfigs = moduleConfig.getSpringConfigs();

      final String[] configs = new String[springConfigs.size()];
      springConfigs.toArray(configs);
      if (moduleConfig.isFromFile()) {
        applicationContexts.put(en.getValue(), new ModuleFileSystemApplicationContext(configs, false, null, en.getValue().getModuleName()));
      } else {
        applicationContexts.put(en.getValue(), new ModuleJarEntryXmlApplicationContext(configs, false, null, en.getValue().getModuleName()));
      }
    }

    //refresh
    for (Map.Entry<ModuleConfig, ApplicationContext> en : applicationContexts.entrySet()) {
      final ApplicationContext applicationContext = en.getValue();
      if (applicationContext instanceof ConfigurableApplicationContext) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        ((AbstractRefreshableApplicationContext) applicationContext).refresh();
        final long time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("加载模块{}成功,用时{}ms", en.getKey().getModuleName(), time);
      }
    }

    return applicationContexts;
  }

  protected Map<String, ModuleConfig> resolveModuleJsonConfigs(ClassLoader classLoader) throws IOException {
    final ClasspathScanner scanner = new ClasspathScanner();

    scanner.scan(classLoader, new Predicate<String>() {
      @Override
      public boolean apply(String input) {
        return input.contains("module.json") || input.contains("META-INF/spring/");
      }
    });

    final Map<String, ModuleConfig> moduleConfigs = Maps.newHashMap();

    for (ClasspathScanner.ResourceInfo resourceInfo : scanner.getResources()) {
      final String resourceName = resourceInfo.getResourceName();
      final int i = resourceName.indexOf("META-INF/");

      final String moduleKey = resourceName.substring(0, i);

      ModuleConfig moduleConfig = moduleConfigs.get(moduleKey);
      if (moduleConfig == null) {
        moduleConfig = new ModuleConfig();
        moduleConfigs.put(moduleKey, moduleConfig);
      }

      moduleConfigs.put(moduleKey, moduleConfig);
      if (resourceName.startsWith("file:")) {
        moduleConfig.setFromFile(true);
        final String fileName = resourceName.substring("file:".length());
        final File file = new File(fileName);
        if (resourceName.endsWith(".json")) {
          final Map<String, Object> map = JSON.parseObject(Files.readAllBytes(file.toPath()), Map.class);
          moduleConfig.setModuleName((String) map.get("moduleName"));
          moduleConfig.setDependenceModules((List<String>) map.get("dependenceModules"));
        } else {
          List<String> springConfigs = moduleConfig.getSpringConfigs();
          if (springConfigs == null) {
            springConfigs = Lists.newArrayList();
            moduleConfig.setSpringConfigs(springConfigs);
          }
          springConfigs.add(resourceName);
        }
      } else {
        moduleConfig.setFromFile(false);
        //jar file
        final String jarResourceName = resourceName.substring("jar:".length());
        final List<String> jarResource = Splitter.on("!/").trimResults().splitToList(jarResourceName);
        if (resourceName.endsWith(".json")) {
          final String json = JarEntryReader.toString(jarResource.get(0), jarResource.get(1));
          final Map<String, Object> map = JSON.parseObject(json, Map.class);
          moduleConfig.setModuleName((String) map.get("moduleName"));
          moduleConfig.setDependenceModules((List<String>) map.get("dependenceModules"));
        } else {
          List<String> springConfigs = moduleConfig.getSpringConfigs();
          if (springConfigs == null) {
            springConfigs = Lists.newArrayList();
            moduleConfig.setSpringConfigs(springConfigs);
          }
          springConfigs.add(resourceName);
        }
      }
    }

    return moduleConfigs;
  }

}
