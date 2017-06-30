package cn.yxffcode.modularspring.boot;

import cn.yxffcode.modularspring.core.ModuleApplicationContext;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.CharStreams;
import com.google.common.reflect.ClassPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkState;

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
      final ClassPathResource rs = (ClassPathResource) springConfigResource;
      final String filename = rs.getPath();
      final String pathUniqueId = filename.substring(0, filename.indexOf("/META-INF/"));

      springConfigs.put(pathUniqueId, rs);
    }
    return springConfigs;
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
      if (resourceInfo.getResourceName().startsWith("file:")) {
//        new File()
      }
    }

    return moduleConfigs;
  }

}
