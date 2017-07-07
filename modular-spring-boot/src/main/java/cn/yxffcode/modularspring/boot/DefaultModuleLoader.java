package cn.yxffcode.modularspring.boot;

import cn.yxffcode.modularspring.boot.graph.DirectedAcyclicGraph;
import cn.yxffcode.modularspring.boot.io.ClasspathScanner;
import cn.yxffcode.modularspring.core.context.ModuleJarEntryXmlApplicationContext;
import cn.yxffcode.modularspring.core.io.JarEntryReader;
import cn.yxffcode.modularspring.core.context.ModuleFileSystemApplicationContext;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 6/24/17.
 */
public class DefaultModuleLoader implements ModuleLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModuleLoader.class);

  public Map<ModuleConfig, ApplicationContext> load(ClassLoader classLoader) throws IOException {

    final Collection<ModuleConfig> moduleConfigs = resolveModuleJsonConfigs(classLoader);


    //初始化spring
    final Map<ModuleConfig, ApplicationContext> applicationContexts = createApplicationContexts(moduleConfigs);

    final List<ModuleConfig> topological = topologicalSort(moduleConfigs);
    //refresh
    for (ModuleConfig moduleConfig : topological) {
      final ApplicationContext applicationContext = applicationContexts.get(moduleConfig);
      if (applicationContext instanceof ConfigurableApplicationContext) {
        final Stopwatch stopwatch = Stopwatch.createStarted();
        ((AbstractRefreshableApplicationContext) applicationContext).refresh();
        final long time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        LOGGER.info("加载模块{}成功,用时{}ms", moduleConfig.getModuleName(), time);
      }
    }
    return applicationContexts;
  }

  private Map<ModuleConfig, ApplicationContext> createApplicationContexts(Collection<ModuleConfig> moduleConfigs) {
    final Map<ModuleConfig, ApplicationContext> applicationContexts = Maps.newHashMapWithExpectedSize(moduleConfigs.size());
    for (ModuleConfig moduleConfig : moduleConfigs) {

      final List<String> springConfigs = moduleConfig.getSpringConfigs();

      final String[] configs = new String[springConfigs.size()];
      springConfigs.toArray(configs);
      if (moduleConfig.isFromFile()) {
        applicationContexts.put(moduleConfig, new ModuleFileSystemApplicationContext(configs, false, null, moduleConfig.getModuleName()));
      } else {
        applicationContexts.put(moduleConfig, new ModuleJarEntryXmlApplicationContext(configs, false, null, moduleConfig.getModuleName()));
      }
    }
    return applicationContexts;
  }

  protected Collection<ModuleConfig> resolveModuleJsonConfigs(ClassLoader classLoader) throws IOException {
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

    return moduleConfigs.values();
  }

  private List<ModuleConfig> topologicalSort(Collection<ModuleConfig> moduleConfigs) {
    final Map<String, ModuleConfig> modules = Maps.newHashMap();
    for (ModuleConfig moduleConfig : moduleConfigs) {
      modules.put(moduleConfig.getModuleName(), moduleConfig);
    }
    //dependence
    final Map<ModuleConfig, List<ModuleConfig>> dependenceMap = Maps.newHashMap();
    for (ModuleConfig moduleConfig : moduleConfigs) {
      final List<String> dependenceModules = moduleConfig.getDependenceModules();
      if (CollectionUtils.isEmpty(dependenceModules)) {
        dependenceMap.put(moduleConfig, Collections.emptyList());
      } else {
        final List<ModuleConfig> deps = Lists.newArrayList();
        for (String dependenceModule : dependenceModules) {
          final ModuleConfig module = modules.get(dependenceModule);
          checkNotNull(module, "模块%s不存在", dependenceModule);
          deps.add(module);
        }
        dependenceMap.put(moduleConfig, deps);
      }
    }
    final DirectedAcyclicGraph<ModuleConfig> graph = new DirectedAcyclicGraph<>();
    for (Map.Entry<ModuleConfig, List<ModuleConfig>> en : dependenceMap.entrySet()) {
      graph.link(en.getKey(), en.getValue());
    }
    return graph.topological();
  }

}
