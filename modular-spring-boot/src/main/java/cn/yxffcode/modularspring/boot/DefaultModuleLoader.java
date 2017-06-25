package cn.yxffcode.modularspring.boot;

import cn.yxffcode.modularspring.core.ModuleApplicationContext;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author gaohang on 6/24/17.
 */
public class DefaultModuleLoader implements ModuleLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultModuleLoader.class);

  public Map<ModuleConfig, ApplicationContext> load(ClassLoader classLoader) throws IOException {
    final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(classLoader);

    final Map<String, ModuleConfig> moduleConfigs = resolveModuleJsonConfigs(resourcePatternResolver);

    final Multimap<String, ClassPathResource> springConfigs = resolveSpringConfigs(resourcePatternResolver);

    //初始化spring
    final Map<ModuleConfig, ApplicationContext> applicationContexts = Maps.newHashMapWithExpectedSize(moduleConfigs.size());
    for (Map.Entry<String, ModuleConfig> en : moduleConfigs.entrySet()) {
      final Collection<ClassPathResource> moduleSpringConfigs = springConfigs.get(en.getKey());
      checkState(!CollectionUtils.isEmpty(moduleSpringConfigs), "模块%s的spring配置文件不存在", en.getValue().getModuleName());
      final String[] configs = (String[]) moduleSpringConfigs.stream().map(ClassPathResource::getPath).toArray();

      final Stopwatch stopwatch = Stopwatch.createStarted();
      final ModuleApplicationContext ctx = new ModuleApplicationContext(configs, false, null, en.getValue().getModuleName());
      applicationContexts.put(en.getValue(), ctx);
      final long time = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS);
      LOGGER.info("加载模块{}成功,用时{}ms", en.getValue().getModuleName(), time);
    }

    return applicationContexts;
  }

  protected Multimap<String, ClassPathResource> resolveSpringConfigs(ResourcePatternResolver resourcePatternResolver) throws IOException {
    final Resource[] springConfigResources = resourcePatternResolver.getResources("classpath:/META-INF/spring/*.xml");

    final Multimap<String, ClassPathResource> springConfigs = HashMultimap.create();

    for (Resource springConfigResource : springConfigResources) {
      final String filename = springConfigResource.getFilename();
      final String pathUniqueId = filename.substring(0, filename.indexOf("/META-INF/"));

      final ClassPathResource rs = (ClassPathResource) springConfigResource;
      springConfigs.put(pathUniqueId, rs);
    }
    return springConfigs;
  }

  protected Map<String, ModuleConfig> resolveModuleJsonConfigs(ResourcePatternResolver resourcePatternResolver) throws IOException {
    final Map<String, ModuleConfig> moduleConfigs = Maps.newHashMap();
    final Resource[] moduleJsonResources = resourcePatternResolver.getResources("classpath:" + "/META-INF/module.json");

    for (Resource moduleJsonResource : moduleJsonResources) {
      final String filename = moduleJsonResource.getFilename();
      final String pathUniqueId = filename.substring(0, filename.indexOf("/META-INF/"));

      try (Reader in = new InputStreamReader(moduleJsonResource.getInputStream())) {
        final String json = CharStreams.toString(in);
        final ModuleConfig moduleConfig = JSON.parseObject(json, ModuleConfig.class);
        moduleConfigs.put(pathUniqueId, moduleConfig);
      }

    }
    return moduleConfigs;
  }

}
