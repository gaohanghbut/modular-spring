package cn.yxffcode.modularspring.boot;

import com.google.common.base.Throwables;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author gaohang on 6/24/17.
 */
public class Lifecycle {

  private Map<ModuleConfig, ApplicationContext> applicationContexts = Collections.emptyMap();

  public void boot() {
    final ModuleLoader moduleLoader = new DefaultModuleLoader();
    try {
      this.applicationContexts = moduleLoader.load(Lifecycle.class.getClassLoader());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public void destroy() {
    for (ApplicationContext applicationContext : applicationContexts.values()) {
      if (applicationContext instanceof ConfigurableApplicationContext) {
        ((ConfigurableApplicationContext) applicationContext).close();
      }
    }
  }
}
