package cn.yxffcode.modularspring.boot;

import cn.yxffcode.modularspring.boot.graph.DirectedAcyclicGraph;
import cn.yxffcode.modularspring.boot.listener.ModuleLoadListener;
import cn.yxffcode.modularspring.boot.utils.ModuleLoadContextHolder;
import cn.yxffcode.modularspring.core.context.DefaultModuleApplicationContext;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
import cn.yxffcode.modularspring.core.io.ClasspathScanner;
import cn.yxffcode.modularspring.core.io.JarEntryReader;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author gaohang on 6/24/17.
 */
public class DefaultModuleLoader implements ModuleLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModuleLoader.class);

  private List<ModuleLoadListener> moduleLoadListeners;

  public Map<ModuleConfig, ModuleApplicationContext> load(ClassLoader classLoader) throws IOException {

    final BiMap<String, ModuleConfig> moduleConfigs = resolveModuleJsonConfigs(classLoader);

    //初始化spring
    final Map<ModuleConfig, ModuleApplicationContext> applicationContexts = createApplicationContexts(moduleConfigs.values());

    final List<ModuleConfig> topological = topologicalSort(moduleConfigs.values());
    //refresh
    for (ModuleConfig moduleConfig : topological) {
      final ModuleApplicationContext applicationContext = applicationContexts.get(moduleConfig);
      if (applicationContext instanceof ConfigurableApplicationContext) {
        attachModulePath(moduleConfigs, moduleConfig);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        invokeBeforeRefresh(moduleConfig, applicationContext);
        applicationContext.refresh();
        final long time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
        invokeAfterRefresh(moduleConfig, applicationContext);
        LOGGER.info("加载模块{}成功,用时{}ms", moduleConfig.getModuleName(), time);
      }
    }
    ModuleLoadContextHolder.clean();
    return applicationContexts;
  }

  private void attachModulePath(BiMap<String, ModuleConfig> moduleConfigs, ModuleConfig moduleConfig) {
    final String loadingModulePath = moduleConfigs.inverse().get(moduleConfig);
    if (loadingModulePath.startsWith("jar:")) {
      ModuleLoadContextHolder.setLoadingModulePath(loadingModulePath.substring("jar:".length()));
    } else if (loadingModulePath.startsWith("file:")) {
      ModuleLoadContextHolder.setLoadingModulePath(loadingModulePath.substring("file:".length()));
    } else {
      ModuleLoadContextHolder.setLoadingModulePath(loadingModulePath);
    }
    ModuleLoadContextHolder.setLoadingModuleConfig(moduleConfig);
  }

  private void invokeAfterRefresh(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
    if (!CollectionUtils.isEmpty(moduleLoadListeners)) {
      for (ModuleLoadListener moduleLoadListener : moduleLoadListeners) {
        moduleLoadListener.afterModuleLoad(moduleConfig, applicationContext);
      }
    }
  }

  private void invokeBeforeRefresh(ModuleConfig moduleConfig, ModuleApplicationContext applicationContext) {
    if (!CollectionUtils.isEmpty(moduleLoadListeners)) {
      for (ModuleLoadListener moduleLoadListener : moduleLoadListeners) {
        moduleLoadListener.beforeModuleLoad(moduleConfig, applicationContext);
      }
    }
  }

  @Override
  public void addModuleLoadListener(ModuleLoadListener moduleLoadListener) {
    checkNotNull(moduleLoadListener);
    if (moduleLoadListeners == null) {
      moduleLoadListeners = Lists.newArrayList();
    }
    moduleLoadListeners.add(moduleLoadListener);
  }

  private Map<ModuleConfig, ModuleApplicationContext> createApplicationContexts(Collection<ModuleConfig> moduleConfigs) {
    final Map<ModuleConfig, ModuleApplicationContext> applicationContexts = Maps.newHashMapWithExpectedSize(moduleConfigs.size());
    for (ModuleConfig moduleConfig : moduleConfigs) {

      final List<String> springConfigs = moduleConfig.getSpringConfigs();

      final String[] configs = new String[springConfigs.size()];
      springConfigs.toArray(configs);
      final ModuleApplicationContext applicationContext = createModuleApplicationContext(moduleConfig, configs);
      applicationContexts.put(moduleConfig, applicationContext);
    }
    return applicationContexts;
  }

  protected ModuleApplicationContext createModuleApplicationContext(ModuleConfig moduleConfig, String[] configs) {
    return new DefaultModuleApplicationContext(configs,
        false, null, moduleConfig.getModuleName());
  }

  protected BiMap<String, ModuleConfig> resolveModuleJsonConfigs(ClassLoader classLoader) throws IOException {
    final ClasspathScanner scanner = new ClasspathScanner();

    scanner.scan(classLoader, new Predicate<String>() {
      @Override
      public boolean apply(String input) {
        return input.contains("module.json") || input.contains("META-INF/spring/");
      }
    });

    final BiMap<String, ModuleConfig> moduleConfigs = HashBiMap.create();

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

    for (final Iterator<Map.Entry<String, ModuleConfig>> iterator = moduleConfigs.entrySet().iterator(); iterator.hasNext(); ) {
      final Map.Entry<String, ModuleConfig> en = iterator.next();
      if (StringUtils.isBlank(en.getValue().getModuleName()) || CollectionUtils.isEmpty(en.getValue().getSpringConfigs())) {
        iterator.remove();
      }
    }
    return moduleConfigs;
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
