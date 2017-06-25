package cn.yxffcode.modularspring.boot;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author gaohang on 6/24/17.
 */
public class Bootstrap {

  private Map<ModuleConfig, ApplicationContext> applicationContexts = Collections.emptyMap();

  public void boot() {
    final ModuleLoader moduleLoader = new DefaultModuleLoader();
    try {
      this.applicationContexts = moduleLoader.load(Bootstrap.class.getClassLoader());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }
}
