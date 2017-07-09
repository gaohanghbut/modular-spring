package cn.yxffcode.modularspring.boot;

import cn.yxffcode.modularspring.boot.listener.ModuleLoadListener;
import cn.yxffcode.modularspring.core.context.ModuleApplicationContext;
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

  private Map<ModuleConfig, ModuleApplicationContext> applicationContexts = Collections.emptyMap();

  private final ModuleLoader moduleLoader;

  public Lifecycle() {
    this(new DefaultModuleLoader());
  }

  public Lifecycle(ModuleLoader moduleLoader) {
    this.moduleLoader = moduleLoader;
  }

  public void boot() {
    try {
      this.applicationContexts = moduleLoader.load(Lifecycle.class.getClassLoader());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public void addModuleLoadListener(ModuleLoadListener moduleLoadListener) {
    moduleLoader.addModuleLoadListener(moduleLoadListener);
  }

  public void destroy() {
    for (ApplicationContext applicationContext : applicationContexts.values()) {
      if (applicationContext instanceof ConfigurableApplicationContext) {
        ((ConfigurableApplicationContext) applicationContext).close();
      }
    }
  }
}
